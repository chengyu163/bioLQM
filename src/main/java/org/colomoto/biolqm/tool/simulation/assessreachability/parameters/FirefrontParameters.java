package org.colomoto.biolqm.tool.simulation.assessreachability.parameters;

import org.colomoto.biolqm.LogicalModel;

public class FirefrontParameters extends SimulationParameters{
	
	public int depth = 10000;
	public double alpha = 0.00001;
	public double beta = -1;
	public int maxExpand = 10000;
	public String priorityClass;
	
	public FirefrontParameters(LogicalModel model, int depth, double alpha, 
			byte[] initialState, double beta, int maxExpand, boolean quiet, 
			String priorityClass) {
		this(model);
		this.depth = depth;
		this.alpha = alpha;
		this.beta = beta;
		this.maxExpand = maxExpand;
		this.verbose = quiet;
		this.priorityClass = priorityClass;
	}

	public FirefrontParameters(LogicalModel model) {
		initialStates = model.getInitialStates();
	}

	public static String getAvailableParameters() {
		return 	 "--expansionLimit (DEFAULT=10000)\n"
				+ "--maxDepth (DEFAULT=10000)\n"
				+ "--alpha (DEFAULT=0.00001)\n"
				+ "--beta (DEFAULT=-1)\n"
				+ "--verbose (DEFAULT=false)\n"
				+ "--priorityClass [classes separated by /] (G1/G2:G3/G4)\n"
				+ "--inputs (G1_input:0)\n";
		}
	

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("  Alpha=" + this.alpha + 
				"\n  Beta=" + this.beta+
				"\n  Max depth=" + this.depth+
				"\n  Expansion limit=" + this.maxExpand);
		if (this.priorityClass != null)
			sb.append("\n  Priority Classes="+this.priorityClass.toString());
		return sb.toString();
		
	}
	
}
