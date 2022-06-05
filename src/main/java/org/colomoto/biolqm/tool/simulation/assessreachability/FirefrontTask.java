package org.colomoto.biolqm.tool.simulation.assessreachability;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.AbstractToolTask;
import org.colomoto.biolqm.tool.simulation.assessreachability.algorithm.FirefrontSimulation;
import org.colomoto.biolqm.tool.simulation.assessreachability.algorithm.Simulation;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.FirefrontParameters;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AlgorithmException;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.SimulationUtils;

public class FirefrontTask extends AbstractToolTask<Result> {

	public FirefrontParameters parameters;

	public FirefrontTask(LogicalModel model) {
		super(model);
		this.parameters = new FirefrontParameters(model);
	}


	public Simulation getFirefrontSimulation(FirefrontParameters parameters, LogicalModel model) {
		FirefrontSimulation sim = new FirefrontSimulation(parameters, model);
		return sim;
	}
	
	@Override
	public void cli() {
		try {
			Result result = performTask();
			System.out.println(result);
		} catch (Exception e) {
		}
	}

	@Override
	public void setParameters(String[] parameters) {
		if (parameters == null || parameters.length < 2) 
			throw new AlgorithmException("Supported parameters: \n" + FirefrontParameters.getAvailableParameters());
		for (int i = 0; i < parameters.length; i++) {
			String s = parameters[i].trim();
			if (s == null || s.length() < 2)
				continue;
			if (s.charAt(0) == '-') {
				i++;
				switch (s) {
				case "--alpha":
					this.parameters.alpha = Double.parseDouble(parameters[i]);
					continue;
				case "--beta":
					this.parameters.beta = Double.parseDouble(parameters[i]);
					continue;
				case "--maxDepth":
					this.parameters.depth = Integer.parseInt(parameters[i]);
					continue;
				case "--expansionLimit":
					this.parameters.maxExpand = Integer.parseInt(parameters[i]);
					continue;
				case "--inputs":
					this.parameters.initialStates = SimulationUtils.setInputs(model, parameters[i], this.parameters.initialStates);
					continue;
				case "--verbose":
					this.parameters.verbose = true;
					i--;
					continue;
				case "--priorityClass":
					this.parameters.priorityClass = this.parameters.priorityClass;
					continue;
				default:
					System.err.println("Unsupported parameter " + s);
					throw new AlgorithmException("Supported parameters: \n" + FirefrontParameters.getAvailableParameters());
				}
			}
		}
	}
	

	@Override
	protected Result performTask() throws Exception {
		if(parameters.initialStates==null) 
			throw new AlgorithmException("Firefront needs a initial state.\n" + FirefrontParameters.getAvailableParameters());
		
		Simulation sim = getFirefrontSimulation(parameters, model);
		return sim.runSimulation();
	}

}
