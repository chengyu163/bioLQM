package org.colomoto.biolqm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.colomoto.biolqm.tool.simulation.InitialStateFactory;

public class NamedState {
	
	public static NamedState single_instance = null;

	Map<String, byte[]> namedStates;
	int modelSize;

	public NamedState() {
	}
	
	public static class LazyHolder { /*thread-safe singleton */
		public static final NamedState instance = new NamedState();
	}

	public static NamedState getInstance() {
		return LazyHolder.instance;
	}

	public void parseNamedStateFile(String fileName) {
		namedStates = new HashMap<String, byte[]>();
		String line;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) {
			        continue;
			    }
				String[] namedState = line.split(" ");
				byte[] s = InitialStateFactory.parseInitialState(modelSize, namedState[1]);
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
			if(state.matches("^.*" + entry.getValue().replace("[0-2]", ".*") + ".*$" )) {
				sb.append(entry.getKey()+" ");
			}
		}
		return sb.toString();
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
	

	public void setModelSize(int size) {
		modelSize = size;
	}

}
