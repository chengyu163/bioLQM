package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.modifier.perturbation.FixedValuePerturbation;
import org.colomoto.biolqm.tool.simulation.InitialStateFactory;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.State;


public final class SimulationUtils {

	/**public static byte[] getRandomState(LogicalModel model, List<byte[]> states) {
		List<NodeInfo> nodes = model.getComponents();
		Random r = new Random();

		
		byte[] state = states.get(r.nextInt(states.size()));
		
		byte[] newstate = new byte[state.length];
	
		for(int i=0, l=nodes.size(); i<l; i++) 
			if(state[i]==-1) newstate[i]=(byte)r.nextInt(nodes.get(i).getMax()+1);
			else newstate[i]=state[i];
		return newstate;
	}**/

	public static List<byte[]> getListOfInitialStates(LogicalModel model, String string) {
		List<byte[]> initialStatesList = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(string)));
			String line;
			while ((line = br.readLine()) != null) {
				initialStatesList.add(InitialStateFactory.parseInitialState(model.getComponents().size(), String.join("", line.split(","))));
			}
			br.close();
		} catch (IOException e) {
			String[] stringInitialStatesList = string.split("-");
			for (String stringState : stringInitialStatesList) {
				initialStatesList.add(InitialStateFactory.parseInitialState(model.getComponents().size(), stringState));
			}
		}
		return initialStatesList;
	}

	public static Map<byte[], Double> applyRateToStates(byte[] state, List<byte[]> successors, double[] ratesUp, double[] ratesDown) {
		Map<byte[], Double> successorsWithRates = new HashMap<byte[], Double>();
		for (byte[] successor : successors) {
			for (int i = 0; i < successor.length; i++) {
				if (successor[i] > state[i])
					successorsWithRates.put(successor, ratesUp[i]);
				else if(successor[i] < state[i])
					successorsWithRates.put(successor, ratesDown[i]);
			}
		}	
		return successorsWithRates;
	}


	public static List<byte[]> setInputs(LogicalModel model, String pattern, List<byte[]> initialStatesList) {
		String[] inputs = pattern.split(",");
		for (String input : inputs) {
			String[] node = input.split(":");
			model.getComponent(node[0]).setInput(true);
			FixedValuePerturbation fix = new FixedValuePerturbation(model.getComponent(node[0]), Integer.parseInt(node[1]));
			fix.update(model);
			for (byte[] state : initialStatesList) 
				state[model.getComponentIndex(node[0])] = Byte.parseByte(node[1]);	
		}
		return initialStatesList;
	}

	
	public static Map<String, String> parseNamedStates(String fileName){
		Map<String, String> namedStates = new HashMap<>();
		String line;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			while ((line = br.readLine()) != null) {
				String[] states = line.split(",");
				namedStates.put(states[0], states[1]);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return namedStates;
	}
	

	public static String convertoToNamedStates(Map<String, String> namedStates, String attractors) {
		if (namedStates == null) 
			return attractors;
		StringBuffer sb = new StringBuffer();
		/** prepare matcher to the namedStates */
		Matcher matcher = Pattern.compile("("+String.join("|", 
				namedStates.keySet())+")").matcher(attractors);

		while(matcher.find()) {
		    matcher.appendReplacement(sb, namedStates.get(matcher.group(1)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

}