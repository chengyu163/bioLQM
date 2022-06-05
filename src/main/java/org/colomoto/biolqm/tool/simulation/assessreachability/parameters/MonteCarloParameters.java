package org.colomoto.biolqm.tool.simulation.assessreachability.parameters;

import java.util.Arrays;

import org.colomoto.biolqm.LogicalModel;

public class MonteCarloParameters extends SimulationParameters{
	
	public int runs;
	public int maxDepth = 10000;
	public String priorityClass;
	public double[] ratesUp;
	public double[] ratesDown;
	
	public MonteCarloParameters(LogicalModel model, int runs, double maxSteps, boolean quiet, String priorityClass, double[] ratesUp, double[] ratesDown) {
		this(model, runs);
		int nstates = model.getComponents().size();
		byte[] state = new byte[nstates];
		Arrays.fill(state, (byte)-1);
		initialStates = Arrays.asList(state);
		this.maxDepth = (int) maxSteps;
		this.verbose = quiet;
		this.priorityClass = priorityClass;
		this.ratesUp = ratesUp;
		this.ratesDown = ratesDown;
	}

	public MonteCarloParameters(LogicalModel model) {
		initialStates = model.getInitialStates();
		if(ratesUp == null) {
			this.ratesUp = new  double[model.getComponents().size()];
			Arrays.fill(this.ratesUp, 1.0);
		}
		if(ratesDown == null) {
			this.ratesDown = new  double[model.getComponents().size()];
			Arrays.fill(this.ratesDown, 1.0);
		}
	}
	
	public MonteCarloParameters(LogicalModel model, int runs) {
		this(model);
		this.runs = (int) runs;
	}

	
	public static String getAvailableParameters() {
		return "--runs (MANDATORY)"
				+ "\n--maxDepth (DEFAULT=1000)"
				+"\n--verbose (default=false)"
				+"\n--rates [rates separated by ,] (G1:Rate,G2[+]:Rate)"
				+"\n--priorityClass [classes separated by /] (G1/G2:G3/G4)";
		}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("  Runs=" + this.runs + "\n  Max depth=" + this.maxDepth);
		if (this.priorityClass != null)
			sb.append("\n  Priority Classes="+this.priorityClass.toString());
		if(ratesUp!= null || ratesDown!= null) {
			sb.append("\n  Rates Up=" + Arrays.toString(this.ratesUp));
			sb.append("\n  Rates Down=" + Arrays.toString(this.ratesDown));
		}
		return sb.toString();
	}

}
