package org.colomoto.biolqm;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.internal.MDDStoreImpl;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.junit.jupiter.api.Test;

public class TestNamedState {

	private LogicalModel getModel() {
		// build a list of variables and functions for a model
		List<NodeInfo> vars = new ArrayList<NodeInfo>();
		vars.add(new NodeInfo("G0"));
		vars.add(new NodeInfo("G1"));
		vars.add(new NodeInfo("G2"));
		vars.add(new NodeInfo("G3"));
		
		MDDManager manager = new MDDStoreImpl(vars, 2);
		int[] functions = new int[vars.size()];
		MDDVariable v0 = manager.getVariableForKey(vars.get(0));
		MDDVariable v1 = manager.getVariableForKey(vars.get(1));
		MDDVariable v2 = manager.getVariableForKey(vars.get(2));
		MDDVariable v3 = manager.getVariableForKey(vars.get(3));
		int f0 = v0.getNode(0, 1);
		int f1 = v1.getNode(0, 1);
		int f2 = v2.getNode(0, 1);
		int fn3 = v3.getNode(1, 0);
		
		functions[0] = f1;
		functions[1] = f0;
		functions[2] = fn3;
		functions[3] = MDDBaseOperators.OR.combine(manager, f1, f2);
		
		return new LogicalModelImpl(vars, manager, functions);
	}

	@Test
	public void TestInitialStateWithNamedState1 () {
		LogicalModel model = getModel();
		NamedState ns = new NamedState();
		ns.setModelSize(model.getComponents().size());
		ns.parseNamedStateFile("src/test/java/org/colomoto/biolqm/named_states.txt");
		InitialStates state = new InitialStates();
		state.setPattern("state1");
		state.parsePatternToStates(model, ns);
		byte[] expectedIS = {0,0,0,1};
		assertTrue(Arrays.equals(expectedIS, state.getRandomState()));
	}
	
	@Test
	public void TestInitialStateWithNamedState2 () {
		LogicalModel model = getModel();
		NamedState ns = new NamedState();
		ns.setModelSize(model.getComponents().size());
		ns.parseNamedStateFile("src/test/java/org/colomoto/biolqm/named_states.txt");
		InitialStates state = new InitialStates();
		state.setPattern("state2");
		state.parsePatternToStates(model, ns);
		byte[] expectedIS = {0,1,1,1};
		assertTrue(Arrays.equals(expectedIS, state.getRandomState()));
	}
}
