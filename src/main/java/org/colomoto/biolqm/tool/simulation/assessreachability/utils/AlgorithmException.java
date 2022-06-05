package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

/** 
 * Exception associated with the behavior of Avatar simulations
 * @author Rui Henriques
 * @version 1.0
 */
public class AlgorithmException extends RuntimeException {

	private static final long serialVersionUID = 1;

	/**
	 * Creates a new Avatar exception with a dedicated message
     * @param message the detail message
     */
    public AlgorithmException(String message) {
        super(message);
        System.out.println(message);
    }

    /**
     * Creates a new Avatar exception with a detail message and nested exception
     * @param message the detail message
     * @param cause the nested exception
     */
    public AlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }
}
