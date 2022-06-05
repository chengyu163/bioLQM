package org.colomoto.biolqm.tool.simulation.assessreachability;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.service.MultivaluedSupport;
import org.colomoto.biolqm.tool.AbstractToolService;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.FirefrontParameters;

public class FirefrontService extends AbstractToolService<Result, FirefrontTask> {

	public static final String UID = "firefront";
	public static final String HELP_LINE = "Estimate attractor reachability using Firefront";

	public FirefrontService() {
		super(UID, HELP_LINE, FirefrontParameters.getAvailableParameters(), MultivaluedSupport.MULTIVALUED);
	}

	@Override
	public FirefrontTask getTask(LogicalModel model) {
		return new FirefrontTask(model);
	}

}
