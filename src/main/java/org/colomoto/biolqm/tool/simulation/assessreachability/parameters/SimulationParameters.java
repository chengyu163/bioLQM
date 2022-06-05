package org.colomoto.biolqm.tool.simulation.assessreachability.parameters;

import java.util.List;
import java.util.Map;

public abstract class SimulationParameters {
		
	public List<List<byte[]>> oracles;
	
	public List<byte[]> initialStates;
	
	public boolean verbose=false;
	
	public Map<String, String> namedStates;
	
	public List<List<byte[]>> getOracles(){
		return oracles;
	}
	
	public List<byte[]> getInitialStates(){
		return initialStates;
	}
}
