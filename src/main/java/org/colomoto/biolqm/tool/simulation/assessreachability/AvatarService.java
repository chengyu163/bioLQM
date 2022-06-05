package org.colomoto.biolqm.tool.simulation.assessreachability;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.service.MultivaluedSupport;
import org.colomoto.biolqm.tool.AbstractToolService;
import org.colomoto.biolqm.tool.ModelToolService;
import org.colomoto.biolqm.tool.simulation.assessreachability.domain.Result;
import org.colomoto.biolqm.tool.simulation.assessreachability.parameters.AvatarAlgorithmParameters;
import org.kohsuke.MetaInfServices;

@MetaInfServices(ModelToolService.class)
public class AvatarService extends AbstractToolService<Result, AvatarTask> {

	public static final String UID = "avatar";
	public static final String HELP_LINE = "Estimate attractor reachability using Avatar";

	public AvatarService() {
		super(UID, HELP_LINE, AvatarAlgorithmParameters.getAvailableParameters(), MultivaluedSupport.MULTIVALUED);
	}

	@Override
	public AvatarTask getTask(LogicalModel model) {
		return new AvatarTask(model);
	}
}
