package org.colomoto.biolqm.tool.simulation.assessreachability;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.AbstractToolTask;
import org.colomoto.biolqm.tool.simulation.assessreachability.algorithm.AvatarSimulation;
import org.colomoto.biolqm.tool.simulation.assessreachability.algorithm.AvatarSimulation.AvatarStrategy;
import org.colomoto.biolqm.tool.simulation.assessreachability.algorithm.Simulation;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.AvatarAlgorithmParameters;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AlgorithmException;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.SimulationUtils;

public class AvatarTask extends AbstractToolTask<Result> {

	public AvatarAlgorithmParameters parameters;

	public AvatarTask(LogicalModel model) {
		super(model);
		this.parameters = new AvatarAlgorithmParameters(model);
	}

	public Simulation getAvatarSimulation(AvatarAlgorithmParameters parameters, LogicalModel model) {
		AvatarSimulation sim = new AvatarSimulation(parameters, model);
		return sim;
	}

	@Override
	public void cli() {
		if (this.parameters.runs == 0 || this.parameters.expansionLimit == 0 || this.parameters.rewiringLimit == 0)
			printHelpMessage();
		try {
			Result result = performTask();
			System.out.println(result);
		} catch (Exception e) {
			throw new AlgorithmException("Error performing simulation", e);
		}
	}

	@Override
	public void setParameters(String[] parameters) {
		if (parameters == null || parameters.length < 6) {
			printHelpMessage();
		}
		for (int i = 0; i < parameters.length; i++) {
			String s = parameters[i].trim();
			if (s == null || s.length() < 1)
				continue;
			if (s.charAt(0) == '-') {
				i++;
				switch (s) {
				case "--tau":
					this.parameters.tau = Integer.parseInt(parameters[i]);
					continue;
				case "--runs":
					this.parameters.runs = Integer.parseInt(parameters[i]);
					continue;
				case "--maxDepth":
					this.parameters.maxDepth = Integer.parseInt(parameters[i]);
					continue;
				case "--rewiringLimit":
					this.parameters.rewiringLimit = Integer.parseInt(parameters[i]);
					continue;
				case "--minTransientSize":
					this.parameters.minTransientSize = Integer.parseInt(parameters[i]);
					continue;
				case "--expansionLimit":
					this.parameters.expansionLimit = Integer.parseInt(parameters[i]);
					continue;
				case "--minStatesRewiring":
					this.parameters.minStatesRewiring = Integer.parseInt(parameters[i]);
					continue;
				case "--strategy":
					this.parameters.strategy = AvatarStrategy.valueOf(parameters[i]);
					continue;
				case "--verbose":
					this.parameters.verbose = true;
					i--;
					continue;
				case "--notKeepTransients":
					this.parameters.keepTransients = false;
					i--;
					continue;
				case "--priorityClass":
					this.parameters.priorityClass = parameters[i];
					continue;
				case "--rates":
					parseStringToRates(model, parameters[i]);
					continue;
				case "--inputs":
					this.parameters.initialStates = SimulationUtils.setInputs(model, parameters[i], this.parameters.initialStates);
					continue;
				default:
					System.err.println("Unsupported parameter " + s);
					printHelpMessage();
				}
			}
		}
	}

	private void parseStringToRates(LogicalModel model, String string) {
		if (this.parameters.ratesUp == null || this.parameters.ratesDown == null) {
			this.parameters.ratesUp = new double[model.getComponents().size()];
			Arrays.fill(this.parameters.ratesUp, 1);
			this.parameters.ratesDown = this.parameters.ratesUp.clone();
		}
		String[] arrayOfNodeRate = string.split(",");
		for (String element : arrayOfNodeRate) {
			String[] nodeRate = element.split(":");
			String node = Pattern.compile("\\[[^\\[]*\\]", Pattern.MULTILINE).matcher(nodeRate[0]).replaceAll(""); /*replace all [ ]*/
			int index = model.getComponentIndex(node);
			if (index == -1) {
				System.err.println(node+ " not present");
				System.exit(-1);
			}
			double rate = Double.parseDouble(nodeRate[1]);
			if (nodeRate[0].endsWith("[+]")) 
				this.parameters.ratesUp[index] = rate;
			else if (nodeRate[0].endsWith("[-]")) 
				this.parameters.ratesDown[index] = rate;
			else {
				this.parameters.ratesUp[index] = rate;
				this.parameters.ratesDown[index] = rate;
			}
		}
	}


	private void printHelpMessage() {
		System.err.println("Supported parameters: \n" + AvatarAlgorithmParameters.getAvailableParameters());
		System.exit(-1);
	}

	@Override
	protected Result performTask() throws Exception {
		Simulation sim = getAvatarSimulation(parameters, model);
		Result result = sim.runSimulation();
		return result;
	}

}