package org.colomoto.biolqm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.colomoto.biolqm.tool.simulation.InitialStateFactory;

public class InitialStates {
	
	List<byte []> initialStates;
	String pattern;
	LogicalModel model;
	
	public InitialStates() {
		initialStates = new ArrayList<byte[]>();
	}
	
	public static class LazyHolder { /*thread-safe singleton */
		public static final InitialStates instance = new InitialStates();
	}
	
	public static InitialStates getInstace() {
		return LazyHolder.instance;
	}

	public void parsePatternToStates(LogicalModel model, NamedState namedStates) {
		this.model = model;
		int modelSize = model.getComponents().size();
		if (pattern == null) {
			byte[] stateSpace = new byte[modelSize];
			Arrays.fill(stateSpace, (byte)-1);
			initialStates.add(stateSpace);
			return;
		}
		
		if(!pattern.matches("^[\\\\*012-]+$") && namedStates != null) 
			namedStates.namedStates.entrySet().stream().
			filter(namedState -> Objects.equals(namedState.getKey(), pattern))
			.map(Map.Entry::getValue)
			.forEachOrdered(initialStates::add);
		else
			Arrays.stream(pattern.split("-")).
			map(state -> InitialStateFactory.parseInitialState(modelSize, state)).
			forEachOrdered(initialStates::add);
	}
	

	public byte[] getRandomState() {
		List<NodeInfo> nodes = model.getComponents();
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
	

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
