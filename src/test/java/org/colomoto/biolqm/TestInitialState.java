package org.colomoto.biolqm;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.internal.MDDStoreImpl;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.junit.jupiter.api.Test;

class TestInitialState {

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
	public void TestInitialStateWithARandomState () {
		LogicalModel model = getModel();
		model.setPattern("0*1*");
		model.parsePatternToStates();
		byte[] expectedIS = {0,-1,1,-1};
		assertTrue(model.getInitialStates().size() == 1);
		assertTrue(Arrays.equals(expectedIS, model.getInitialStates().get(0)));
	}
	
	@Test
	public void TestInitialStateWithStateSpace () {
		LogicalModel model = getModel();
		byte[] expectedIS = {-1,-1,-1,-1};
		model.parsePatternToStates();
		assertTrue(model.getInitialStates().size() == 1);
		assertTrue(Arrays.equals(expectedIS, model.getInitialStates().get(0)));
	}
	
}
