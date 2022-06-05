package org.colomoto.biolqm.tool.simulation.assessreachability.algorithm;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.EnumAlgorithm;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.State;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.StateSet;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.FirefrontParameters;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AvatarUtils;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.ChartGNUPlot;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.StateProbComparator;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AsynchronousUpdater;

/**
 * Firefront simulation for the quasi-exact analysis of point attractors
 * 
 * @author Rui Henriques
 * @author Pedro T. Monteiro
 * @author Nuno Mendes
 * @version 1.0
 */
public class FirefrontSimulation extends Simulation {
	
	public FirefrontParameters parameters;

	protected AsynchronousUpdater updater;

	/**
	 * Instantiates a Firefront simulation
	 */
	public FirefrontSimulation(FirefrontParameters parameters, LogicalModel model) {
		super.addModel(model, parameters);
		this.parameters = parameters;
		this.updater = new AsynchronousUpdater(model);
	}

	@Override
	public Result runSim() throws IOException {

		/** A: parameterize/initialize firefront */

		Result result = new Result();
		if (parameters.beta == -1) parameters.beta = parameters.alpha;

		List<double[]> pStates = new ArrayList<double[]>(), pProbs = new ArrayList<double[]>();

		StateSet F = new StateSet(new State(parameters.getInitialStates().get(0), 1));
		StateSet N = new StateSet(), A = new StateSet();

		/** B: firefront converging behavior */

		int k = 0, na = 0;
		Map<String, Integer> oscillatorCount = new HashMap<String, Integer>();
		Map<String, Integer> oscillatorDepth = new HashMap<String, Integer>();
		StateSet complexA = new StateSet();
		String revisit = null;

		for (; k <= parameters.depth && F.totalProbability() > parameters.beta; k++) {
			String fid = F.getKeys() + "";
			if (revisit == null) {
				if (oscillatorCount.containsKey(fid)) {
					if (oscillatorCount.get(fid) == 2) {
						revisit = fid;
						complexA.addAll(F);
					} else
						oscillatorCount.put(fid, oscillatorCount.get(fid) + 1);
				} else {
					oscillatorCount.put(fid, 0);
					oscillatorDepth.put(fid, k);
				}
			} else {
				if (revisit.equals(fid)) {
					List<StateSet> complexAttractors = getAttractorsFromSet(complexA);
					for (StateSet c : complexAttractors) {
						result.complexAttractors.put("att_" + na, c);
						result.attractorsDepths.put("att_" + na, Arrays.asList(oscillatorDepth.get(fid)));
						double prob = 0;
						for (State s : F.getStates())
							if (c.contains(s))
								prob += s.probability;
						// result.attractorsUpperBound.put("att_"+na,prob+N.totalProbability());
						result.attractorsLowerBound.put("att_" + (na++), prob);
					}
					F = new StateSet();
					break;
				}
				complexA.addAll(F);
			}

			output(" Iteration:" + k + " set cardinals:[F=" + F.size() + ",N=" + N.size() + ",A=" + A.size() + "]"
						+ " set probs:[F=" + AvatarUtils.round(F.totalProbability()) + ",N="
						+ AvatarUtils.round(N.totalProbability()) + ",A=" + AvatarUtils.round(A.totalProbability())
						+ "]");
			

			pStates.add(new double[] { F.size(), N.size(), A.size() });
			pProbs.add(new double[] { F.totalProbability(), N.totalProbability(), A.totalProbability() });


			/** B1: states to expand and pass */

			StateSet toExpand = new StateSet(), toPass = new StateSet();
			if (parameters.maxExpand >= 0 && F.size() > parameters.maxExpand) {
				List<State> states = new ArrayList<State>();
				states.addAll(F.getStates());
				Collections.sort(states, new StateProbComparator(false /* desc */)); 
				
				for (int i = 0; i < parameters.maxExpand; i++)
					toExpand.add(states.get(i));
				for (int i = parameters.maxExpand, l = states.size(); i < l; i++)
					toPass.add(states.get(i));
			} else
				toExpand = F;
			if (parameters.verbose)
				output("  [F=" + F.size() + ",EXPAND=" + toExpand.size() + ",PASS=" + toPass.size() + "]");

			/** B2: for each expanding state generate succ to find attractors */


			for (State s : toExpand.getStates()) {
				List<byte[]> successors = new ArrayList<byte[]>(updater.getSuccessors(s.state));
				double prob = s.probability * (1.0 / (double) successors.size());
				StateSet Q = new StateSet(successors, prob); // add successors with correct prob
				if (Q.isEmpty()) {
					// discovery = true;
					A.addCumulative(s);
					if (result.contains(s)) {
						result.increment(s);
					} else {
						result.add(s);
						result.attractorsDepths.get(s.key).add(k);
					}
					if (parameters.verbose)
						output("  Found an attractor:" + s.toString());
				} else {
					boolean complex = false;
					for (StateSet trans : result.complexAttractors.values()) {
						if (trans.contains(s)) {
							A.addCumulative(s);
							// System.out.println(result.attractorsLowerBound.get(trans.getKey()));
							result.attractorsLowerBound.put(trans.getKey(),
									result.attractorsLowerBound.get(trans.getKey()) + s.probability);
							result.attractorsDepths.get(trans.getKey()).add(k);
							complex = true;
							break;
						}
					}
					if (!complex) {
						for (StateSet trans : oracle) {
							if (trans.contains(s)) {
								A.addCumulative(s);
								result.complexAttractors.put("att_" + na, trans);
								trans.setKey("att_" + na);
								ArrayList<Integer> depths = new ArrayList<Integer>();
								depths.add(k);
								result.attractorsDepths.put("att_" + na, depths);
								result.attractorsLowerBound.put("att_" + na, s.probability);
								na++;
								complex = true;
								if (parameters.verbose)
									output("  Incrementing attractor!");
								break;
							}
						}
					}
					if (!complex) {
						if (parameters.verbose)
							output("  " + Q.size() + " successors\n  Parent state has probability " + s.probability);
						for (State v : Q.getStates()) {
							if (toPass.contains(v))
								toPass.addCumulative(v);
							else {
								if (N.contains(v)) {
									v.probability += N.getProbability(v);
									N.remove(v);
								}
								if (parameters.verbose)
									output("  v => " + v.toString());
								if (v.probability >= parameters.alpha)
									toPass.addCumulative(v); // if(!A.contains(v) && !toExpand.contains(v))
								else
									N.add(v);
							}
						}
					}
				}
			}
			F = toPass;
		}

		if (parameters.verbose)
			output("Final results:\n  states=[F=" + F.size() + ",N=" + N.size() + ",A=" + A.size() + "]"
					+ "\n  probs=[F=" + F.totalProbability() + ",N=" + N.totalProbability() + ",A="
					+ A.totalProbability() + ",residual=" + (N.totalProbability() + F.totalProbability()) + "]"
					+ "\n  total prob=" + (F.totalProbability() + N.totalProbability() + A.totalProbability()));
		result.residual = N.totalProbability() + F.totalProbability();


		
		String title = "Plot: F, N and A cardinal evolutions";
		BufferedImage img = ChartGNUPlot.getProgression(pStates, title, "#Iterations", "#states").asImage();
		result.addPlot(title, img);

		String title2 = "Plot: F, N and A cumulative probability evolutions";

		BufferedImage img2 = ChartGNUPlot.getProgression(pProbs, title2, "#Iterations", "probability").asImage();
		result.addPlot(title2, img2);


		if(F.totalProbability() > parameters.beta) result.convergence = false;
		for (State a : result.pointAttractors.values())
			result.setBounds(a.key, a.probability, a.probability + F.totalProbability() + N.totalProbability());
		for (String key : result.complexAttractors.keySet())
			result.attractorsUpperBound.put(key,
					result.attractorsLowerBound.get(key) + F.totalProbability() + N.totalProbability());


		result.algorithm = EnumAlgorithm.FIREFRONT;
		result.log = getResultLog();
		result.parameters = parameters.toString();
		result.iconditions = parameters.getInitialStates();
		return result;
	}


	private List<StateSet> getAttractorsFromSet(StateSet complexA) {
		List<StateSet> result = new ArrayList<StateSet>();
		Collection<String> keys = complexA.getKeys();
		while (!keys.isEmpty()) {
			StateSet att = new StateSet();
			String key = keys.iterator().next();
			State s = complexA.getState(key);
			keys.remove(key);

			List<byte[]> successors = updater.getSuccessors(s.state);
			while (successors.size() > 0) {
				State v = new State(successors.remove(0));
				if (!att.contains(v)) {
					att.add(v);
					keys.remove(v.key);
					successors.addAll(updater.getSuccessors(v.state));
				}
			}
			result.add(att);
		}
		return result;
	}

	@Override
	public void dynamicUpdateValues() {
		List<NodeInfo> components = model.getComponents();
		int allStates = 1;
		for (NodeInfo comp : components)
			allStates *= comp.getMax() + 1;
		parameters.alpha = 1.0 / (double) (10 * allStates);
		parameters.beta = parameters.alpha;
		parameters.depth = allStates;
		parameters.maxExpand = allStates;
		quiet = true;
	}


	@Override
	public String getName() {
		return "FireFront";
	}

}
