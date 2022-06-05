package org.colomoto.biolqm.tool.simulation.assessreachability.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.EnumAlgorithm;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.State;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.StateSet;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.AvatarUpdaterFactory;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.MonteCarloParameters;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.MathFunctions;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.SimulationUtils;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AbstractMultipleSuccessorUpdater;

/**
 * Monte Carlo simulation for the analysis of point attractors
 * 
 * @author Rui Henriques
 * @author Pedro T. Monteiro
 * @version 1.0
 */
public class MonteCarloSimulation extends Simulation {


	private AbstractMultipleSuccessorUpdater updater;
	
	public MonteCarloParameters parameters;

	/**
	 * Instantiates a Monte Carlo simulation
	 */
	public MonteCarloSimulation(MonteCarloParameters parameters, LogicalModel model) {
		super.addModel(model, parameters);
		this.parameters = parameters;
		this.updater = AvatarUpdaterFactory.getUpdater(model, parameters.priorityClass);
	}	



	public Result runSim() throws IOException {
		Result result = new Result();
		List<Integer> depth = new ArrayList<Integer>();
		int truncated = 0;
		for (int sn = 1, i = 0; sn <= this.parameters.runs; sn++, i = 0) {
			State currentState = new State(model.getRandomState());
			output("Iteration: " + sn + "/"+this.parameters.runs + " state=" + currentState.toShortString());

			while (true) {

				boolean complex = false;
				for (StateSet trans : result.complexAttractors.values()) {
					if (trans.contains(currentState)) {
						result.incrementComplexAttractor(trans.getKey(), i);
						complex = true;
						if (this.parameters.verbose)
							output("  Incrementing attractor!");
						break;
					}
				}
				if (!complex) {
					for (StateSet trans : oracle) {
						if (trans.contains(currentState)) {
							result.add(trans, i);
							complex = true;
							if (this.parameters.verbose)
								output("  Incrementing attractor!");
							break;
						}
					}
				}
				if (complex)
					break;

				byte[] s = pickSuccessors(currentState.state, updater.getSuccessors(currentState.state), parameters.ratesUp, parameters.ratesDown);
				result.memory = (int) Math.max(result.memory, Runtime.getRuntime().totalMemory() / 1024);
				if (s == null) {
					if (result.contains(currentState)) {
						result.increment(currentState);
					} else {
						result.add(currentState);
					}
					result.attractorsDepths.get(currentState.key).add(i);
					break;
				}
				currentState = new State(s);
				i++;
				if (this.parameters.maxDepth > 0 && i >= this.parameters.maxDepth) {
					if (this.parameters.verbose)
						output("  Reached maximum depth: quitting current simulation");
					truncated++;
					break;
				}
			}
			depth.add(i);
		}
		double sum = 0;
		for (State a : result.pointAttractors.values()) sum += result.attractorsCount.get(a.key) / (double) this.parameters.runs;
		for (State a : result.pointAttractors.values()) {
			try {
				double prob = result.attractorsCount.get(a.key) / (double) this.parameters.runs; 
				result.setBounds(a.key, prob, prob+(1-sum));
			} catch (Exception e) {
				result.setBounds(a.key, Double.NaN, Double.NaN);
			}
		}


		result.algorithm = EnumAlgorithm.MONTE_CARLO;
		result.runs = this.parameters.runs;
		result.truncated = truncated;
		result.performed = this.parameters.runs - truncated;
		result.parameters = parameters.toString();
		result.iconditions = parameters.getInitialStates();
		if (this.parameters.verbose)
			output("Discovery depth: minimum: " + MathFunctions.min(depth) + ", maximum: " + MathFunctions.max(depth)
					+ ", average: " + MathFunctions.mean(depth));
		if (this.parameters.verbose)
			output("Simulations asked: " + this.parameters.runs + ", successful: " + (this.parameters.runs - truncated));
		return result;
	}

	private byte[] pickSuccessors(byte[] state, List<byte[]> successors, double[] ratesUp, double[] ratesDown) {
		Map<byte[], Double> successorsWithRates = SimulationUtils.applyRateToStates(state, successors, ratesUp, ratesDown);
		double totalRate =  successorsWithRates.values().stream().mapToDouble(Double::doubleValue).sum();
		double point = new Random().nextDouble()*totalRate;
		double sum = 0;
		for(Entry<byte[], Double> stateSelected : successorsWithRates.entrySet()) {
			sum += stateSelected.getValue();
			if(point<=sum) return stateSelected.getKey();
		}
		return null;
	}


	@Override
	public void dynamicUpdateValues() {
		List<NodeInfo> components = model.getComponents();
		int allStates = 1;
		for (NodeInfo comp : components)
			allStates *= comp.getMax() + 1;
		this.parameters.runs = Math.max(10000, allStates);
		this.parameters.maxDepth = allStates;
		this.parameters.verbose = false;
	}

	@Override
	public String getName() {
		return "MonteCarlo";
	}
}
