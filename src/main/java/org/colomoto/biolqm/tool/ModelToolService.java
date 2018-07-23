package org.colomoto.biolqm.tool;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.Service;
import org.colomoto.common.task.Task;

/**
 * Simple tool description interface.
 * Implement this interface to integrate a tool in the command line interface.
 * 
 * @author Aurelien Naldi
 */
public interface ModelToolService<R, T extends ToolTask<R>> extends Service {

	/**
	 * Does this tool handle multivalued models?
	 * 
	 * @return true if it supports multivalued models, false for Boolean only
	 */
	boolean supportsMultivalued();
	
	/**
	 * Construct a default setting object.
	 *
	 * @param model the source model
	 * @return the custom setting object
	 */
	T getTask(LogicalModel model);
	
	/**
	 * Construct a parsed setting object.
	 *
	 * @param model the source model
	 * @param parameters optional command line settings
	 * @return the custom setting object
	 */
	T getTask(LogicalModel model, String parameters);
	
	/**
	 * Construct a default setting object.
	 *
	 * @param model the source model
	 * @param parameters optional command line settings
	 * @return the custom setting object
	 */
	T getTask(LogicalModel model, String ... parameters);

	/**
	 * Run the tool on a logical model.
	 *
	 * @param model the model to use
	 * @param parameters the raw command line parameters
	 */
	void run(LogicalModel model, String ... parameters);
}
