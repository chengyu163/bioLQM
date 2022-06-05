package org.colomoto.biolqm.tool.simulation.assessreachability;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.service.MultivaluedSupport;
import org.colomoto.biolqm.tool.AbstractToolService;
import org.colomoto.biolqm.tool.ModelToolService;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.MonteCarloParameters;
import org.kohsuke.MetaInfServices;

@MetaInfServices(ModelToolService.class)
public class MonteCarloService extends AbstractToolService<Result, MonteCarloTask> {

	public static final String UID = "montecarlo";
	public static final String HELP_LINE = "Estimate attractor reachability using Monte Carlo";

	public MonteCarloService() {
		super(UID, HELP_LINE, MonteCarloParameters.getAvailableParameters(), MultivaluedSupport.MULTIVALUED);
	}

	@Override
	public MonteCarloTask getTask(LogicalModel model) {
		return new MonteCarloTask(model);
	}

}
