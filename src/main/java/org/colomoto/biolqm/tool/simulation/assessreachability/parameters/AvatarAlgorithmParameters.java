package org.colomoto.biolqm.tool.simulation.assessreachability.parameters;

import java.util.Arrays;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.simulation.assessreachability.algorithm.AvatarSimulation.AvatarStrategy;

public class AvatarAlgorithmParameters extends SimulationParameters{
	
	public int runs;
	public int expansionLimit; 
	public int rewiringLimit; 
	public int maxDepth = 1000000;
	public int minTransientSize = 200;	
	public int minStatesRewiring = 4;
	public int tau = 2;	
	public AvatarStrategy strategy = AvatarStrategy.MatrixInversion;
	public boolean keepTransients = true;
	public double[] ratesUp;
	public double[] ratesDown;
	public String priorityClass;	
	private boolean keepOracle = true;
	
	public AvatarAlgorithmParameters(LogicalModel model, double tau, double runs, double maxDepth, double minStatesRewiring, double smallStateSpace, double expansionLimit,
			double minTransientSize, double rewiringLimit, boolean keepTransients, boolean keepOracle, boolean quiet,
			AvatarStrategy strategy, EnumUpdaters updaters, String priorityClass, double[] ratesUp, double[] ratesDown) {
		this(model, (int)runs, (int)expansionLimit, (int)rewiringLimit);
		this.tau = (int) tau;
		this.maxDepth = (int) maxDepth;
		this.minTransientSize = (int) minTransientSize;
		this.minStatesRewiring = (int) minStatesRewiring;
		this.setKeepTransients(keepTransients);
		this.setKeepOracle(keepOracle);
		this.setVerbose(quiet);
		this.strategy = strategy;
		this.priorityClass = priorityClass;	
	}

	public AvatarAlgorithmParameters(LogicalModel model) {
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
	
	public AvatarAlgorithmParameters(LogicalModel model, int runs, int expansionLimit, int rewiringLimit) {
		this(model);
		this.runs = runs;
		this.expansionLimit = expansionLimit;
		this.rewiringLimit = rewiringLimit;
	}
	
	public static String getAvailableParameters() {
		return  "--runs (MANDATORY)\n"+
				"--expansionLimit (MANDATORY)\n"+
				"--rewiringLimit (MANDATORY)\n"+
				"--maxDepth (DEFAULT=1000000)\n"+
				"--minTransientSize (DEFAULT=200)\n"+
				"--minStatesRewiring (DEFAULT=4)\n"+
				"--tau (DEFAULT=3)\n"+
				"--strategy [MatrixInversion/RandomExit] (default=MatrixInversion)\n"+
				"--notKeepTransients (default=keepingTransients)\n"+
				"--verbose (default=false)\n"+
				"--rates [rates separated by ,] (G1:Rate,G2[+]:Rate)\n"+
				"--priorityClass [classes separated by /] (G1/G2:G3/G4)";
		}

	public String toString() {
		StringBuffer sb = new StringBuffer("  Runs=" + runs + "\n  Expansion #states limit=" + expansionLimit +
				"\n  Rewiring #states limit="+ rewiringLimit + "\n  Keep transients=" + keepTransients + 
				"\n  Min transient size=" + minTransientSize + "\n  Keep oracles=" + isKeepOracle() + "\n  Tau=" 
				+ tau  + "\n  Min #states SCC to rewire=" + minStatesRewiring + "\n  Max depth=" + maxDepth);
		if (this.priorityClass != null)
			sb.append("\n  Priority Classes="+this.priorityClass.toString());
		if (this.ratesUp != null || this.ratesDown != null) {
			sb.append("\n  Rates Up=" + Arrays.toString(this.ratesUp));
			sb.append("\n  Rates Down=" + Arrays.toString(this.ratesDown));
		}
		return sb.toString();
	}

	public boolean isKeepTransients() {
		return keepTransients;
	}


	public void setKeepTransients(boolean keepTransients) {
		this.keepTransients = keepTransients;
	}


	public boolean isKeepOracle() {
		return keepOracle;
	}


	public void setKeepOracle(boolean keepOracle) {
		this.keepOracle = keepOracle;
	}


	public boolean isQuiet() {
		return !verbose;
	}


	public void setVerbose(boolean quiet) {
		this.verbose = quiet;
	}
	
}
