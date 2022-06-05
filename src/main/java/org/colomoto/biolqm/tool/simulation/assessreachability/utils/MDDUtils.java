package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.State;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.StateSet;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.PathSearcher;

/**
 * Facilities associated with simulation services. Including:<br>
 * 1) Random generation of an initial state from a given pattern or
 * state-set.<br>
 * 2) Inference of state patterns from a given set of states.<br>
 * 3) Generating a drawable graph from a complex attractor.<br>
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public final class MDDUtils {

	/**
	 * Maps an oracle (set of state patterns) into a set of specific states
	 * 
	 * @param oracle
	 *            the oracle (set of state patterns) to be unfolded
	 * @param nodes
	 *            knowledge of the states of the components within a state pattern
	 * @return the set of specific states associated with the given state patterns
	 */
	public static StateSet toStateSet(List<byte[]> oracle, List<NodeInfo> nodes) {
		List<byte[]> all = new ArrayList<byte[]>();
		while (!oracle.isEmpty()) {
			byte[] state = oracle.remove(0);
			int index = firstMultiple(state);
			if (index == -1)
				all.add(state);
			else {
				for (int i = 0, l = nodes.get(index).getMax(); i <= l; i++) {
					byte[] newstate = Arrays.copyOf(state, state.length);
					newstate[index] = (byte) i;
					oracle.add(newstate);
				}
			}
		}
		return new StateSet(all);
	}

	private static int firstMultiple(byte[] state) {
		for (int i = 0, l = state.length; i < l; i++)
			if (state[i] == -1)
				return i;
		return -1;
	}

	/**
	 * Infers a set of state patterns from a given set of states
	 * 
	 * @param vars
	 *            list of information regarding each node of the model
	 * @param stateset
	 *            the set of states to be compacted
	 * @return the set of state patterns associated with the given states
	 */
	public static List<byte[]> getStatePatterns(List<NodeInfo> vars, StateSet stateset) {
		MDDVariableFactory mvf = new MDDVariableFactory();
		for (NodeInfo ni : vars)
			mvf.add(ni, (byte) (ni.getMax() + 1));
		List<Set<Integer>> statesPerComponent = new ArrayList<Set<Integer>>();
		for (int i = 0, l = vars.size(); i < l; i++)
			statesPerComponent.add(new HashSet<Integer>());
		MDDManager ddmanager = MDDManagerFactory.getManager(mvf, 10);

		List<byte[]> states = new ArrayList<byte[]>();
		for (State s : ((StateSet) stateset).getStates())
			states.add(s.state);

		int mdd = ddmanager.nodeFromStates(states, 1);

		PathSearcher searcher = new PathSearcher(ddmanager, 1);
		int[] path = searcher.setNode(mdd);
		List<byte[]> result = new ArrayList<byte[]>();
		for (int p : searcher)
			result.add(AvatarUtils.toByteArray(path));
		// for(byte[] s : result) System.out.println(">>"+AvatarUtils.toString(s));
		return result;
	}

}
