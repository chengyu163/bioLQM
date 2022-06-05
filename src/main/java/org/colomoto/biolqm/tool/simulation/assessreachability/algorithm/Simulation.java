package org.colomoto.biolqm.tool.simulation.assessreachability.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Dictionary;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.StateSet;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.SimulationParameters;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.MDDUtils;

/**
 * Class defining an abstract simulation and providing facilities for their
 * management.<br>
 * Specialized simulations (e.g. Avatar, Firefront, MonteCarlo) can be added.
 * 
 * @author Rui Henriques
 * @author Pedro T. Monteiro
 * @version 1.0
 */
public abstract class Simulation {

	/** output directory to save the outputs */
	public String outputDir;

	/**
	 * whether detailed logs of the behavior of the simulation are to be printed
	 * (true is suggested to not hamper efficiency)
	 */
	public boolean quiet = true;

	protected LogicalModel model;
	protected List<StateSet> oracle;
	protected StringBuffer resultLog = new StringBuffer();
	protected int memory;
	
	
	public void addModel(LogicalModel _model, SimulationParameters parameters) {
		model = _model;
		List<NodeInfo> components = model.getComponents();
		oracle = new ArrayList<StateSet>();
		int nstates = -1;
		for (NodeInfo comp : components)
			nstates = Math.max(nstates, comp.getMax() + 1);
		if (Math.pow(nstates, components.size()) >= Long.MAX_VALUE) {
			BigInteger[] hugeFactors = new BigInteger[components.size()];
			for (int i = 0, l = components.size(); i < l; i++)
				hugeFactors[i] = new BigInteger(nstates + "").pow(i);
			Dictionary.codingLongStates(hugeFactors);
		} else {
			long[] factors = new long[components.size()];
			for (int i = 0, l = components.size(); i < l; i++)
				factors[i] = (long) Math.pow(nstates, i);
			Dictionary.codingShortStates(factors);
		}
		List<List<byte[]>> os = parameters.getOracles();
		if (os != null) {
			for (List<byte[]> o : os) {
				oracle.add(new StateSet(o));
			}

		}
	}

	/**
	 * Performs the simulation
	 * 
	 * @return the discovered attractors, their reachability, and remaining
	 *         contextual information
	 * @throws Exception
	 */
	public Result runSimulation() throws Exception {
		long time = System.currentTimeMillis();
		Result res = runSim();
		for (String key : res.complexAttractors.keySet()) 
			res.complexAttractorPatterns.put(key,
						MDDUtils.getStatePatterns(model.getComponents(), (StateSet) res.complexAttractors.get(key)));
		

		res.time = (System.currentTimeMillis() - time);
		res.name = getName();
		res.nodes = getNodes();
		res.model = model;
		return res;
	}

	/**
	 * Performs the simulation
	 * 
	 * @return the discovered attractors, their reachability, and remaining
	 *         contextual information
	 * @throws Exception
	 */
	public abstract Result runSim() throws Exception;


	public abstract String getName();

	private String getNodes() {
		StringBuffer result = new StringBuffer();
		for (NodeInfo node : model.getComponents())
			result.append(node.getNodeID() + ",");
		return result.substring(0, result.length() - 1);
	}

	/**
	 * Updates a simulation with parameterizations dynamically fixed based on the
	 * properties of the input model
	 * 
	 */
	public abstract void dynamicUpdateValues();

	protected void output(String s) {
		resultLog.append(s + "\n");
		System.out.println(s);
		this.publish(s);
	}

	public String getResultLog() {
		return resultLog.toString();
	}

	/********* used for dynamic updating progress ********************/
	protected boolean exit = false;
	protected Thread t1; // used for heavy tasks from external libraries
	private JTextArea progress;

	@SuppressWarnings("deprecation")
	public void exit() {
		if (t1 != null && t1.isAlive())
			t1.stop();
	}

	public Result run() throws Exception {
		final Result[] res = new Result[1];
		final Exception[] es = new Exception[1];
		final boolean[] ok = new boolean[] { true };
		t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					res[0] = runSimulation();
				} catch (Exception e) {
					// e.printStackTrace();
					es[0] = e;
					ok[0] = false;
				}
			}
		});
		t1.start();
		t1.join();
		t1 = null;
		if (!ok[0])
			throw es[0];
		return res[0];
	}

	public void setComponents(JTextArea _progress) {
		progress = _progress;
	}

	protected void publish(String note) {
		if (progress == null)
			progress = new JTextArea();
		progress.append(note + "\n");
	}
}
