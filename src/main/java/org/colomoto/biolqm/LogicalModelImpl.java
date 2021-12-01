package org.colomoto.biolqm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.colomoto.mddlib.MDDManager;
import org.json.JSONObject;
import org.colomoto.biolqm.metadata.AnnotationModule;
import org.colomoto.biolqm.metadata.NodeInfoPair;
import org.colomoto.biolqm.metadata.annotations.Metadata;
import org.colomoto.biolqm.tool.simulation.InitialStateFactory;

/**
 * Implementation of the LogicalModel interface.
 * 
 * @author Aurelien Naldi
 */
public class LogicalModelImpl implements LogicalModel {

	private final MDDManager ddmanager;
	
	private final List<NodeInfo> coreNodes;
	private final int[] coreFunctions;
	
	private final List<NodeInfo> extraNodes;
	private final int[] extraFunctions;
	
	private List<byte []> initialStates;
	private String pattern;
	private Map<String, byte[]> namedStates;

	private ModelLayout layout = null;
	
	private AnnotationModule annotationModule;

	public LogicalModelImpl(MDDManager ddmanager, List<NodeInfo> coreNodes, int[] coreFunctions, List<NodeInfo> extraNodes, int[] extraFunctions) {
		this.ddmanager = ddmanager.getManager(coreNodes);
		this.coreNodes = coreNodes;
		this.coreFunctions = coreFunctions;
		initialStates = new ArrayList<byte[]>();
		namedStates = new HashMap<String, byte[]>();
		
		if (extraNodes == null) {
			this.extraNodes = new ArrayList<>();
			this.extraFunctions = new int[0];
		} else {
			this.extraNodes = extraNodes;
			this.extraFunctions = extraFunctions;
		}
		
		for (int f: this.coreFunctions) {
			this.ddmanager.use(f);
		}
		for (int f: this.extraFunctions) {
			this.ddmanager.use(f);
		}
		
		try {
			this.annotationModule = new AnnotationModule();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LogicalModelImpl(List<NodeInfo> nodeOrder, MDDManager ddmanager, int[] functions) {
		this(ddmanager, nodeOrder, functions, null, null);
	}

	@Override
	public MDDManager getMDDManager() {
		return ddmanager;
	}

	@Override
	public List<NodeInfo> getComponents() {
		return coreNodes;
	}
	
	@Override
	public int[] getLogicalFunctions() {
		return coreFunctions;
	}

	@Override
	public List<NodeInfo> getExtraComponents() {
		return extraNodes;
	}

	public boolean hasExtraComponents() {
		return extraFunctions != null && extraFunctions.length > 0;
	}

	@Override
	public int[] getExtraLogicalFunctions() {
		return extraFunctions;
	}

	@Override
	public LogicalModel clone() {
		return this.clone(true);
	}

	@Override
	public LogicalModel clone(boolean keepExtra) {
		LogicalModel newModel;
		if (keepExtra) {
			newModel = new LogicalModelImpl(ddmanager, cloneNodes(coreNodes), coreFunctions.clone(), cloneNodes(extraNodes), extraFunctions.clone());
		} else {
			newModel = new LogicalModelImpl(ddmanager, cloneNodes(coreNodes), coreFunctions.clone(), new ArrayList<>(), new int[0]);
		}

		// Transfer the booleanized groups to the cloned model
		cloneBooleanizedInfo(coreNodes);
		if (keepExtra) {
			cloneBooleanizedInfo(extraNodes);
		}

		// Also copy the model layout
		if (this.hasLayout()) {
			ModelLayout mlayout = getLayout();
			ModelLayout newLayout = newModel.getLayout();
			copyLayout(getComponents(), newModel.getComponents(), mlayout, newLayout);
			if (keepExtra) {
				copyLayout(getExtraComponents(), newModel.getExtraComponents(), mlayout, newLayout);
			}
		}
		return newModel;
	}

	private void copyLayout(List<NodeInfo> sourceNodes, List<NodeInfo> targetNodes, ModelLayout sourceLayout, ModelLayout targetLayout) {
		int n = targetNodes.size();
		for (int i=0 ; i<n ; i++) {
			targetLayout.copy(targetNodes.get(i), sourceLayout.getInfo( sourceNodes.get(i)));
		}
	}

	private List<NodeInfo> cloneNodes(List<NodeInfo> source) {
		List<NodeInfo> result = new ArrayList<>(source.size());
		for (NodeInfo ni: source) {
			result.add(ni.clone());
		}
		return result;
	}

	private void cloneBooleanizedInfo(List<NodeInfo> source) {
		for (NodeInfo ni: source) {
			NodeInfo[] grp = ni.getBooleanizedGroup();
			if (grp != null) {
				NodeInfo[] result = new NodeInfo[grp.length];
				NodeInfo target = this.getComponent(ni.getNodeID());
				for (int i=0 ; i< grp.length ; i++) {
					result[i] = this.getComponent(grp[i].getNodeID());
				}
				target.setBooleanizedGroup(result);
			}
		}
	}

	@Override
	public byte getTargetValue(int nodeIdx, byte[] state) {
		return ddmanager.reach(coreFunctions[nodeIdx], state);
	}

	@Override
	public byte getExtraValue(int nodeIdx, byte[] state) {
		return ddmanager.reach(extraFunctions[nodeIdx], state);
	}

	@Override
	public void fillExtraValues(byte[] state, byte[] extra) {
		for (int i=0 ; i<extra.length ; i++) {
			extra[i] = getExtraValue(i, state);
		}
	}

	@Override
	public LogicalModel getView(List<NodeInfo> neworder) {
		
		MDDManager newmanager = ddmanager.getManager(neworder);

		int[] newcorefunctions = new int[coreFunctions.length];
		for (int i=0 ; i<coreFunctions.length ; i++) {
			NodeInfo ni = coreNodes.get(i);
			int newidx = neworder.indexOf(ni);
			
			newcorefunctions[newidx] = coreFunctions[i];
		}
		
		return new LogicalModelImpl(newmanager, neworder, newcorefunctions, extraNodes, extraFunctions);
	}

    @Override
    public boolean isBoolean() {
        for (NodeInfo ni: getComponents()) {
            if (ni.getMax() > 1) {
                return false;
            }
        }
        for (NodeInfo ni: getExtraComponents()) {
            if (ni.getMax() > 1) {
                return false;
            }
        }

        return true;
    }

	@Override
	public NodeInfo getComponent(String id) {
		if (id == null) {
			return null;
		}

		for (NodeInfo ni: getComponents()) {
			if (id.equals(ni.getNodeID())) {
				return ni;
			}
		}
		for (NodeInfo ni: getExtraComponents()) {
			if (id.equals(ni.getNodeID())) {
				return ni;
			}
		}

		return null;
	}

	@Override
	public int getComponentIndex(String id) {
		if (id == null) {
			return -1;
		}

		int idx = 0;
		for (NodeInfo ni: getComponents()) {
			if (id.equals(ni.getNodeID())) {
				return idx;
			}
			idx++;
		}
		for (NodeInfo ni: getExtraComponents()) {
			if (id.equals(ni.getNodeID())) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	@Override
	public Map<String, NodeInfo[]> getBooleanizedMap() {
		
		Map<String, NodeInfo[]> bmap = null;
		
		for (NodeInfo ni: getComponents()) {
			NodeInfo[] group = ni.getBooleanizedGroup();
			if (group != null && group[0] == ni) {
				if (bmap == null) {
					bmap = new HashMap<>();
				}
				String key = ni.getNodeID();
				if (key.endsWith("_b1")) {
					key = key.substring(0, key.length()-3);
				}
				bmap.put(key, group);
			}
		}
		return bmap;
	}

	@Override
	public boolean hasLayout() {
		return this.layout != null;
	}

	@Override
	public ModelLayout getLayout() {
		if (this.layout == null) {
			this.layout = new ModelLayout();
		}
		return this.layout;
	}

	@Override
	public Metadata createMetadataOfNode(NodeInfo node) throws Exception {
		return this.annotationModule.createMetadataOfNode(node);
	}

	@Override
	public Metadata createMetadataOfEdge(NodeInfoPair edge) throws Exception {
		return this.annotationModule.createMetadataOfEdge(edge);
	}

	@Override
	public Metadata getMetadataOfModel() {
		return this.annotationModule.getMetadataOfModel();
	}

	@Override
	public boolean isSetMetadataOfNode(NodeInfo node) {
		return this.annotationModule.isSetMetadataOfNode(node);
	}

	@Override
	public boolean isSetMetadataOfEdge(NodeInfoPair edge) {
		return this.annotationModule.isSetMetadataOfEdge(edge);
	}

	@Override
	public Metadata getMetadataOfNode(NodeInfo node) throws Exception {
		return this.annotationModule.getMetadataOfNode(node);
	}

	@Override
	public Metadata getMetadataOfEdge(NodeInfoPair edge) throws Exception {
		return this.annotationModule.getMetadataOfEdge(edge);
	}

	@Override
	public Metadata getMetadataOfEdge(NodeInfo node1, NodeInfo node2) throws Exception {
		return this.annotationModule.getMetadataOfEdge(node1, node2);
	}

	@Override
	public void exportMetadata(String filename) {
		ConnectivityMatrix matrix = new ConnectivityMatrix(this);
		this.annotationModule.exportMetadata(filename, coreNodes, extraNodes, matrix);
	}

	@Override
	public void importMetadata(String filename) {
		this.annotationModule.importMetadata(filename, coreNodes, extraNodes);
	}
	
	@Override
	public void setAnnotationModule(AnnotationModule newAnnotationModule) {
		this.annotationModule = newAnnotationModule;
	}

	@Override
	public AnnotationModule getAnnotationModule() {
		return this.annotationModule;
	}
	
	/** functions to get/set intialstate and named states, as well as their parsing**/
	
	public List<byte[]> getInitialStates(){
		return initialStates;
	}

	public void parsePatternToStates() {
		int modelSize = this.getComponents().size();
		if (pattern == null) {
			byte[] stateSpace = new byte[modelSize];
			Arrays.fill(stateSpace, (byte)-1);
			initialStates.add(stateSpace);
			return;
		}
		if(!pattern.matches("^[\\\\*012-]+$") && !namedStates.isEmpty()) 
			namedStates.entrySet().stream().
			filter(namedState -> Objects.equals(namedState.getKey(), pattern))
			.map(Map.Entry::getValue)
			.forEachOrdered(initialStates::add);
		else
			Arrays.stream(pattern.split("-")).
			map(state -> InitialStateFactory.parseInitialState(modelSize, state)).
			forEachOrdered(initialStates::add);
	}
	
	public void parseNamedStateFile(String fileName) {
		String line;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) {
			        continue;
			    }
				String[] namedState = line.split(" ");
				byte[] s = InitialStateFactory.parseInitialState(this.getComponents().size(), namedState[1]);
				namedStates.put(namedState[0], s);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String stateToNamedState(byte[] state) {
		String stateInString = stateToStringPattern(state);
		Map<String, String> ns = new HashMap<String, String>();
		for (Entry<String, byte[]> entry : namedStates.entrySet()) 
			ns.put(entry.getKey(), stateToStringPattern(entry.getValue()));
		
		Matcher matcher = Pattern.compile("("+String.join("|", 
				ns.values())+")").matcher(stateInString);
				
		StringBuffer pattern = new StringBuffer();
		while(matcher.find()) {
		    matcher.appendReplacement(pattern, getPatternFromState(matcher.group(), ns));
		}
		matcher.appendTail(pattern);
		return pattern.toString();
	}
	
	public String getPatternFromState(String state, Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		for (Entry<String, String> entry : map.entrySet()) {
			if(state.matches(entry.getValue())) {
				sb.append("["+entry.getKey()+"]");
			}
		}
		return sb.toString();
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	private String stateToStringPattern(byte[] values) {
		StringBuffer sb = new StringBuffer();
		for (byte b : values) {
			if(b == -1) 
				sb.append("[0-2]");
			else 
				sb.append(b);
		}
		return sb.toString();
	}
	
	public byte[] getRandomState() {
		List<NodeInfo> nodes = this.getComponents();
		Random r = new Random();

		/** A: select state conditions **/
		byte[] state = initialStates.get(r.nextInt(initialStates.size()));
		
		/** B: generate state satisfying conditions **/
		byte[] newstate = new byte[state.length];
	
		for(int i=0, l=nodes.size(); i<l; i++) 
			if(state[i]==-1) newstate[i]=(byte)r.nextInt(nodes.get(i).getMax()+1);
			else newstate[i]=state[i];
		return newstate;
	}
}
