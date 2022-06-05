package org.colomoto.biolqm.tool.simulation.assessreachability.algorithm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.EnumAlgorithm;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.ExhaustiveFinalPaths;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.State;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.StateSet;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.AvatarAlgorithmParameters;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.AvatarUpdaterFactory;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AlgorithmException;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AvatarUtils;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.ChartGNUPlot;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.MathFunctions;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.SimulationUtils;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.StateSetComparator;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AbstractMultipleSuccessorUpdater;
import org.ejml.simple.SimpleMatrix;

/**
 * Avatar simulation for the discovery of point and complex attractors.<br>
 * Class providing all the functionalities to explore STGs, extend and rewire
 * cycles.
 * 
 * @author Rui Henriques
 * @author Pedro T. Monteiro
 * @author Nuno Mendes
 * @version 1.0
 */
public class AvatarSimulation extends Simulation {

	/** enum specifying the different behavioral strategies of Avatar */
	public enum AvatarStrategy {
		MatrixInversion, RandomExit
	}

	/** maximum depth for the approximate rewiring parameters.strategy */
	public int approxDepth = -1;

	protected AbstractMultipleSuccessorUpdater avatarUpdater;
	
	public AvatarAlgorithmParameters parameters;

	/**
	 * Instantiates an Avatar simulation
	 */
	public AvatarSimulation(AvatarAlgorithmParameters parameters, LogicalModel model) {
		super.addModel(model, parameters);
		this.parameters = parameters;
		this.avatarUpdater = AvatarUpdaterFactory.getUpdater(model, parameters.priorityClass);
	}

	/***************/
	/** MAIN CODE **/
	/***************/

	public Result runSim() throws Exception {

		if (parameters.verbose)
			output("parameters.strategy: " + parameters.strategy.toString());
		
		/** I: Initializations **/
		Result result = new Result();
		int performed = 0, truncated = 0, avgSteps = 0;
		int space = Math.max(parameters.runs / 100, 1), psize = 0;
		int largestFoundTransient = -1;
		int limitTransients = 10;
		Map<String, List<Double>> plotProbs = new HashMap<String, List<Double>>();
		PriorityQueue<StateSet> savedTransients = new PriorityQueue<StateSet>(new StateSetComparator());

		if (parameters.verbose)
			output("!parameters.verbose=" + !parameters.verbose + "\nNode order: " + model.getComponents() + "\nPSize=" + psize + ",parameters.expansionLimit="
					+ parameters.expansionLimit);

		/** II: Simulation **/

		for (int sn = 1, time = 0, tau = parameters.tau; sn <= parameters.runs; sn++, time = 0, avgSteps = 0, psize = 0, tau = parameters.tau) {
			if (parameters.keepTransients && savedTransients.size() > limitTransients) {
				PriorityQueue<StateSet> pqnew = new PriorityQueue<StateSet>(new StateSetComparator());
				while (pqnew.size() < 10)
					pqnew.add(savedTransients.poll());
				savedTransients = pqnew;
			}

			List<StateSet> temporaryTransients = new ArrayList<StateSet>();
			State istate = new State(model.getRandomState());
			Map<String, Integer> discoveryTime = new HashMap<String, Integer>();
			output("Iteration " + sn + "/" + parameters.runs + " state=" + istate.toShortString());

			/** A: Initialize Simulation **/

			ExhaustiveFinalPaths exitProbs =  new ExhaustiveFinalPaths();
			StateSet D = new StateSet(), F = new StateSet(istate), exitStates = new StateSet();
			/** B: Do Reincarnations **/

			StateSet localTransient = null;
			while (!F.isEmpty()) {
				State s = F.getProbableRandomState();
					
				if (parameters.verbose)
					output("  Popped state=" + s + " Sim=" + sn + ", Reincarnation=" + time + ", #F=" + F.size()
							+ ", #D=" + D.size() + ", #A=" + result.attractorsCount.keySet().size());

				/** C: Check whether state belongs a transient or terminal cycle **/

				for (StateSet itrans : temporaryTransients) {
					StateSet trans =  itrans;
					if (trans.contains(s)) {
						
						if (parameters.strategy.equals(AvatarStrategy.RandomExit))
							s = trans.getExitStateSet().getProbableRandomState();
						else {
							if (!trans.hasPaths() || !trans.hasExits()) {
								continue;
							}
							s = ((StateSet) trans).getProbableExitState(s);
						}
						avgSteps += Math.ceil((double) trans.size() / 2.0);
						localTransient = trans;
						if (parameters.verbose)
							output("  Identified transient and getting out of it through state = " + s);
						break;
					}
				}
				if (s == null)
					System.out.println("state is null");

				if (parameters.keepTransients) {
					for (StateSet itrans : savedTransients) {
						StateSet trans = itrans;
						if (trans.contains(s)) {
							avgSteps += Math.ceil((double) trans.size() / 2.0);
							if (!trans.hasPaths() || !trans.hasExits())
								continue;
							s = ((StateSet) trans).getProbableExitState(s);		
							localTransient = trans;
							if (parameters.verbose)
								output("  Identified transient and getting out of it through state = " + s);
							break;
						}
					}
				}

				if (parameters.isKeepOracle()) {
					boolean complex = false;
					for (StateSet trans : result.complexAttractors.values()) {
						if (trans.contains(s)) {
							result.incrementComplexAttractor(trans.getKey(), avgSteps);
							complex = true;
							if (parameters.verbose)
								output("  Incrementing attractor!");
							break;
						}
					}
					if (!complex) {
						for (StateSet trans : oracle) {
							if (trans.contains(s)) {
								result.add(trans, avgSteps);
								complex = true;
								if (parameters.verbose)
									output("  Incrementing attractor!");
								break;
							}
						}
					}
					if (complex)
						break;
				}

				/** D: Cycle to Rewire **/

				if (D.contains(s)) {
					StateSet Ct = new StateSet();
					time = discoveryTime.get(s.key);
					for (State ds : D.getStates())
						if (discoveryTime.get(ds.key) >= time)
							Ct.add(ds); 

					boolean extending = localTransient != null && localTransient.contains(s);
					if (extending) {
						for (State ds : localTransient.getStates())
							Ct.add(ds);
					}
					/** D1: Extend Cycle: check whether a larger cycle can be identified **/

					int prev_cycle_size = 0;
					double exitRatio = 0;
					tau = (parameters.tau > 0) ? parameters.tau : 2;

					StateSet cycleToRewire = new StateSet(Ct), exitStatesRewiring = new StateSet();

					exitStates = null;
					do {
						prev_cycle_size = Ct.size();
						if (parameters.verbose)
							output("  Tau updated from " + tau + " to " + (tau * 2) + " (prev cycle=#" + prev_cycle_size
									+ ")");
						if (prev_cycle_size > 0 && parameters.verbose)
							output("  Trying another round of cycle extension..");

						StateSet newstates = new StateSet();
						extendCycle(null, Ct, exitStates, newstates, 0, tau, new HashMap<String, Integer>(), 0);
						if (exitStates != null) {
							for (State v : newstates.getStates())
								if (exitStates.contains(v))
									exitStates.remove(v);
						}
						
						tau = (tau > 40) ? tau : tau * 2;
						Ct.addAll(newstates);
						Collection<State> expand = (exitStates == null) ? Ct.getStates() : newstates.getStates();
						if (exitStates == null)
							exitStates = new StateSet();
						for (State v : expand) {
							for (State successor : generateSuccessors(v, exitProbs.getPaths(v.key), exitStates, Ct)
									.getStates())
								if (!Ct.contains(successor))
									exitStates.add(successor);
						}
						if (parameters.verbose)
							output("  Cycle extended from #" + prev_cycle_size + " to #" + Ct.size() + "states (#"
									+ exitStates.size() + " exits)");
						exitRatio = ((double) exitStates.size()) / (double) Ct.size();

						if(Ct.size() < parameters.rewiringLimit || exitStatesRewiring.size() == 0) {
							cycleToRewire = new StateSet(Ct);
							exitStatesRewiring = new StateSet(exitStates);
						}

					} while (exitRatio > 0 && prev_cycle_size < Ct.size() && Ct.size() < parameters.expansionLimit);
					if (parameters.verbose)
						publish("Extended cycle with #" + Ct.size() + " states and exitRatio=" + exitRatio);

					/** D2: Rewire Graph **/
					
					if (exitStates.isEmpty()) {
						if (parameters.verbose)
							output("  Identified an attractor!");
						if (temporaryTransients.size() == 0)
							result.add(new StateSet(Ct));
						else
							result.add(calculateComplexAttractor(Ct, temporaryTransients, savedTransients));
						break;
					}
					
					avgSteps += Math.ceil((double) cycleToRewire.size() / 2.0);
					largestFoundTransient = Math.max(largestFoundTransient, Ct.size());
					Ct = cycleToRewire;
					F = new StateSet(exitStates);
					
					exitStates = exitStatesRewiring;
					if (Ct.size() > parameters.minStatesRewiring) {
						if (parameters.verbose)
							output("  Rewiring cycle  with #" + Ct.size() + " states");
						rewriteGraph(Ct, exitStates, exitProbs);
						if (parameters.verbose)
							output("  Cycle rewired");
						if (Ct.size() > parameters.minTransientSize) {
							StateSet transi = new StateSet(Ct);
							transi.setExitStates(new StateSet(exitStates));
							transi.setProbPaths(exitProbs);
							temporaryTransients.add(transi);
						}
					}
					for (State ds : Ct.getStates())
						discoveryTime.put(ds.key, time);
					time++;
					D.addAll(Ct);
					if (parameters.verbose)
						output("  Successors of " + s.toString() + " => " + F.toString());
					

				} else { /* D does not contain s: new state never seen before */

					/** E: Non-Cycle: Keep On **/

					D.add(s);
					discoveryTime.put(s.key, time++);
					F = generateSuccessors(s, exitProbs.getPaths(s.key), exitStates, new StateSet());

					if (F.isEmpty()) {
						result.add(s, avgSteps);
						for (int k = temporaryTransients.size() - 1; k >= 0; k--) {
							StateSet transi = temporaryTransients.remove(k);
							if (parameters.verbose)
								output("  Saving transient (#" + transi.size() + ")");
							// if (!parameters.strategy.equals(AvatarStrategy.strategy.RandomExit))
							// transi.setProbPaths(exitProbs);
							savedTransients.add(transi);
						}
						
					}
				}

				/** F: Finish Iteration **/

				avgSteps++;
				if (parameters.maxDepth > 0 && avgSteps >= parameters.maxDepth) {
					if (parameters.verbose)
						output("  Reached maximum depth: quitting current simulation");
					truncated++;
					break; // last;
				}
			}

			/** G: Out of Reincarnation **/

			if ((sn + 1) % space == 0) {
				Set<String> allkeys = new HashSet<String>();
				allkeys.addAll(result.complexAttractors.keySet());
				allkeys.addAll(result.pointAttractors.keySet());
				for (String key : allkeys) {
					if (!plotProbs.containsKey(key))
						plotProbs.put(key, new ArrayList<Double>());
					plotProbs.get(key).add((double) result.attractorsCount.get(key));
				}
			}
			if (parameters.verbose)
				output("  Out of iteration!");
			performed++;
		}

		/** H: plots **/

		if (plotProbs.size() > 0) {
			output("Plotting charts");
			int max = 0;
			for (String key : plotProbs.keySet()) {
				max = Math.max(max, plotProbs.get(key).size());
			}
			double[][] dataset = new double[plotProbs.size()][max];
			List<String> names = new ArrayList<String>();
			List<String> namesSSs = new ArrayList<String>(result.pointAttractors.keySet());
			Collections.sort(namesSSs);
			int i = 0;
			for (String key : plotProbs.keySet()) {
				for (int k = 0; k < plotProbs.get(key).size(); k++) {
					dataset[i][k] = plotProbs.get(key).get(k);
				}
				if (namesSSs.contains(key)) {
					names.add("SS" + (namesSSs.indexOf(key) + 1));
				} else {
					names.add(key);
				}
				i++;
			}

			String title = "Plot: convergence of probability estimates";
			BufferedImage img = ChartGNUPlot.getConvergence(MathFunctions.normalizeColumns(dataset), names, space, title,
					"#Iterations", "Probability").asImage();
			result.addPlot(title, img);

			List<String> depthRemovals = new ArrayList<String>();
			for (String key : result.attractorsDepths.keySet())
				if (result.attractorsDepths.get(key).size() == 0)
					depthRemovals.add(key);
			for (String key : depthRemovals)
				result.attractorsDepths.remove(key);
		}

		/** I: update results **/

		publish("Creating compact patterns of the found attractors");

		result.algorithm = EnumAlgorithm.AVATAR;
		result.transientMinSize = parameters.minTransientSize;
		result.maxTransientSize = largestFoundTransient;
		result.performed = performed;
		result.truncated = truncated;
		result.memory = memory;
		result.runs = parameters.runs;
		result.parameters = parameters.toString();
		result.iconditions = parameters.getInitialStates();
		if (parameters.verbose)
			output("Simulations asked: " + parameters.runs + ", performed: " + performed + ", truncated: " + truncated);
		result.log = "AVATAR\n" + getResultLog();
		return result;
	}

	/***********************/
	/** AUXILIARY METHODS **/
	/***********************/

	/**
	 * Method for generating the successor states of a given state (comprising
	 * knowledge of rewirings)
	 * 
	 * @param s          the state to be expanded
	 * @param exitProbs  knowledge of exit transitions
	 * @param exitStates knowledge of exit states
	 * @param intraNull  knowledge of cycle that the state is possibly in
	 * @return the correct successor states
	 */
	private StateSet generateSuccessors(State s, Map<String, Double> exitProbs, StateSet exitStates,
			StateSet intraNull) {
		Map<byte[], Double> successorsWitRates = SimulationUtils.applyRateToStates(s.state, avatarUpdater.getSuccessors(s.state), parameters.ratesUp, parameters.ratesDown);
		double totalRate = successorsWitRates.values().stream().mapToDouble(Double::doubleValue).sum();
		StateSet successorsSet = new StateSet();
		
		/*states not inside cycle*/
		if (exitProbs == null) { 
			for(byte[] state: successorsWitRates.keySet()) {
				double probability = successorsWitRates.get(state).doubleValue()/ totalRate;
				State successor = new State(state, probability);
				successorsSet.add(successor);
			}
			return successorsSet;
		}

		if (parameters.verbose)
			output("  Exits of " + s + " => " + exitProbs);

		/*adjust transition of states of cycle to the exit states */
		for(byte[] state: successorsWitRates.keySet()) {
			double probability = successorsWitRates.get(state).doubleValue()/ totalRate;
			State u = new State(state, probability);
			if (intraNull.contains(u))
				continue;
			else if (exitProbs.containsKey(u.key))
				u.probability = exitProbs.get(u.key);
			successorsSet.add(u);
		}
		
		/*adjust transition of exit states to their successors  */
		for (String sKey : exitProbs.keySet()) {
			if (!successorsSet.contains(sKey) && exitStates.contains(sKey)) {
				State u = exitStates.getState(sKey);
				u.probability = exitProbs.get(sKey);
				successorsSet.add(u);
			}
		}
		return successorsSet;
	}

	/**
	 * Calculates a complex attractor based on the knowledge of previous
	 * incarnations
	 * 
	 * @param savedTransients
	 * 
	 * @param Ct               terminal cycles from all incarnations
	 * @param temporaryTransients         all temporary transients discovered
	 * @return the revised complex attractor
	 */
	public StateSet calculateComplexAttractor(StateSet Ct, List<StateSet> temporaryTransients,
			PriorityQueue<StateSet> savedTransients) {
		StateSet Cstar = new StateSet();
		StateSet L = new StateSet(Ct);
		if (parameters.verbose)
			output("  Visiting all reincarnations to discover the master attractor ...");
		while (!L.isEmpty()) {
			State s = L.getFirstState();
			L.remove(s);
			Cstar.add(s);
			for (int k = temporaryTransients.size() - 1; k >= 0; k--) {
				StateSet transi = temporaryTransients.remove(k);
				if (transi.contains(s)) {
					for (State v : transi.getStates()) {
						if (Cstar.contains(v))
							continue;
						if (parameters.verbose)
							output("  Adding state " + v.toString() + " from reincarnation " + k);
						L.add(v);
					}
				} else
					savedTransients.add(transi);
			}
		}
		return Cstar;
	}

	/**
	 * Method for rewiring a cycle
	 * 
	 * @param cycle the cycle (state-set) to be rewired
	 * @param out   the exit states
	 * @param pi    the transitions between cycle and exit states whose probability
	 *              is to be adjusted
	 * @throws Exception
	 */
	public void rewriteGraph(StateSet cycle, StateSet out, ExhaustiveFinalPaths pi) throws Exception {

		List<String> cycleL = new ArrayList<String>(cycle.getKeys());
		List<String> outL = new ArrayList<String>(out.getKeys());
		if(cycleL.size() > 2000|| outL.size() > 2000) {
			output("Warning: the time and space required to rewire cycle might be too long/big.\nPlease lower rewiring limit " + 
					"or please consider another strategy of Avatar.");
		}


		/** I: MATRIX INVERSION **/

		if (parameters.strategy.equals(AvatarStrategy.MatrixInversion)) {
			
			

			final double[][] qMatrix, rMatrix;
			/** A: Computing q and r **/
			try {
				qMatrix = new double[cycle.size()][cycle.size()];
				rMatrix = new double[cycle.size()][out.size()];
			} catch (OutOfMemoryError e) {
				throw new AlgorithmException(
						"[error] out-of-memory exception since cycle is too large to rewrite: please either select 'Uniform Exits' or decrease the maximum number of states for rewriting operations!");
			}

			for (State s : cycle.getStates()) {
				StateSet set = generateSuccessors(s, pi.getPaths(s.key), out, cycle);
				int index = cycleL.indexOf(s.key);
				for (State v : set.getStates()) {
					if (cycle.contains(v.key))
						qMatrix[index][cycleL.indexOf(v.key)] = -v.probability; // pi(s,v)
					else if (out.contains(v.key))
						rMatrix[index][outL.indexOf(v.key)] = v.probability; // pi(s,v)
					else {
						if (parameters.verbose)
							output(v + " is not in cycle and not in cycle successors");
					}
				}
			}
			if (parameters.verbose)
				output("QMatrix\n" + AvatarUtils.toString(qMatrix));
			if (parameters.verbose)
				output("RMatrix\n" + AvatarUtils.toString(rMatrix));

			/** B: Computing (I-q)^-1 * r **/

			for (int i = 0, l = cycle.size(); i < l; i++)
				qMatrix[i][i] += 1;

			final double[][] rewrittenMatrix = new double[qMatrix.length][rMatrix[0].length];

			
			SimpleMatrix RMatrix = new SimpleMatrix(rMatrix);
			
			try {
				RMatrix = new SimpleMatrix(qMatrix).invert().mult(RMatrix);
			} catch (OutOfMemoryError e) {
				System.out.println("OutOfMemory");
			}
			
			for (int i = 0, l1 = RMatrix.numRows(); i < l1; i++)
				for (int j = 0, l2 = RMatrix.numCols(); j < l2; j++)
					rewrittenMatrix[i][j] = RMatrix.get(i, j);

			
			/** C: Adjusting Probabilities **/
			pi.addOutputPaths(cycleL, outL, rewrittenMatrix);

		} else if (parameters.strategy.equals(AvatarStrategy.RandomExit)) {
			/** III: APPROXIMATE SOLUTION **/
			double[][] rateMatrix = new double[cycleL.size()][outL.size()];
			double[] totalRatesToEachExits = new double[cycle.size()];			
			for (State stateOfCycle : cycle.getStates()) {
				Map<byte[], Double> successorsWithRate = SimulationUtils.applyRateToStates(stateOfCycle.state, avatarUpdater.getSuccessors(stateOfCycle.state), parameters.ratesUp, parameters.ratesDown);
				int index = cycleL.indexOf(stateOfCycle.key);
				for (State state : out.getStates()) {
					if(successorsWithRate.containsKey(state.state)) {
						rateMatrix[index][outL.indexOf(state.key)] = successorsWithRate.get(state.state);
						totalRatesToEachExits[outL.indexOf(state.key)] += successorsWithRate.get(state.state);
					} else {
						rateMatrix[index][outL.indexOf(state.key)] = 1;
						totalRatesToEachExits[outL.indexOf(state.key)] += 1;
					}
				}
			}

			double[][] finalMatrix = new double[cycleL.size()][outL.size()];
			for (int i = 0; i < finalMatrix.length; i++) {
				for (int j = 0; j < finalMatrix[i].length; j++) {
					finalMatrix[i][j] = rateMatrix[i][j] / totalRatesToEachExits[i]; /* divide rate by total rate*/
				}
			}
			pi.addOutputPaths(cycleL, outL, finalMatrix);
		}
		if (parameters.verbose)
			output("  Cycle pivot has " + out.size() + " exists");
	}

	/**
	 * Method for extending cycles before rewiring
	 * 
	 * @param v            the state being expanded (null at the start)
	 * @param cycle        the state-set representing the initial cycle
	 * @param newstates    new states added to SCC
	 * @param i            the current time
	 * @param tau          the expansion rate
	 * @param time         structure maintaining the time/depth of the included
	 *                     states
	 * @param originalTime the original time
	 */
	public void extendCycle(State v, StateSet cycle, StateSet exits, StateSet newstates, int i, int tau,
			Map<String, Integer> time, int originalTime) {
		if (parameters.verbose)
			output("    Extending tau=" + tau + " cycle=" + cycle.size());
		StateSet Q = new StateSet();
		if (v == null) {
			if (exits != null)
				Q = exits;
			for (State u : cycle.getStates()) {
				time.put(u.key, i);
				if (exits == null) {
					for (State s : new StateSet(avatarUpdater.getSuccessors(u.state)).getStates())
						if (!cycle.contains(s))
							Q.add(s);
				}
			}
		} else {
			if (parameters.verbose)
				output("(" + i + ")V=" + v.key);
			time.put(v.key, i);
			newstates.add(v);
			Q.addAll(new StateSet(avatarUpdater.getSuccessors(v.state)));
		}
		i++;
		StateSet additions = new StateSet();
		if (tau > 0) {
			if (parameters.verbose)
				output("Tau>0 and Q=" + Q.size());
			for (State w : Q.getStates()) {
				if (!time.containsKey(w.key)) {
					extendCycle(w, cycle, null, newstates, i, tau - 1, time, originalTime);
					if (v != null)
						time.put(v.key, Math.min(time.get(v.key), time.get(w.key)));
				} else if (v != null)
					time.put(v.key, Math.min(time.get(v.key), time.get(w.key)));

			}
		}
		if (v != null && time.get(v.key) > originalTime) {
			if (parameters.verbose)
				output("V:" + v.key + "(" + time.get(v.key) + ")=>remove at i=" + i);
			newstates.remove(v);
		} else
			newstates.addAll(additions);
	}
	
	public AvatarAlgorithmParameters getParameters() {
		return parameters;
	}

	@Override
	public void dynamicUpdateValues() {
		List<NodeInfo> components = model.getComponents();
		int allStates = 1;
		for (NodeInfo comp : components)
			allStates *= comp.getMax() + 1;

		parameters.runs = Math.max(10000, allStates);
		parameters.maxDepth = allStates;
		approxDepth = components.size();
		parameters.expansionLimit = allStates;

		int sumStates = 0;
		for (NodeInfo comp : components)
			sumStates += comp.getMax() + 1;
		parameters.minTransientSize = (int) Math.pow(sumStates, 1.5);

		// fixed
		parameters.verbose = false;
		parameters.tau = 3;
		parameters.strategy = AvatarStrategy.MatrixInversion;
		parameters.keepTransients = true;
		parameters.setKeepOracle(true);
		parameters.minStatesRewiring = 4;
	}


	@Override
	public String getName() {
		return "Avatar " + (parameters.strategy.toString().startsWith("Matrix") ? "(exact exit probs)" : "(uniform exit probs)");
	}
}