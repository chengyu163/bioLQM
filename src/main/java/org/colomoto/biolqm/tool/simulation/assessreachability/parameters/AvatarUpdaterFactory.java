package org.colomoto.biolqm.tool.simulation.assessreachability.parameters;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AbstractMultipleSuccessorUpdater;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AsynchronousUpdater;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.PriorityUpdater;

public class AvatarUpdaterFactory {

	public static AbstractMultipleSuccessorUpdater getUpdater(LogicalModel model, String priorityClass) {
		AbstractMultipleSuccessorUpdater updater;
		if (priorityClass == null) 
			updater = new AsynchronousUpdater(model);
		else
			updater = new PriorityUpdater(model, priorityClass);
		return updater;
	}

}
