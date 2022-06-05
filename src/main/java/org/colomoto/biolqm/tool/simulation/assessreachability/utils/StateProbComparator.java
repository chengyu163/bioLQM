package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

import java.util.Comparator;

import org.colomoto.biolqm.tool.simulation.assessreachability.domain.State;

/**
 * Comparator of states according to their probability
 * @author Rui Henriques
 * @version 1.0
 */
public class StateProbComparator implements Comparator<State> {
	
	private int asc = 1;
	
	/**
	 * @param ascendent true if sorting order is ascendent (false otherwise)
	 */
	public StateProbComparator(boolean ascendent){
		asc = ascendent ? 1 : -1;
	}

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(State s1, State s2) {
        if(s1.probability == s2.probability) return 0;
        else if(s1.probability > s2.probability) return 1*asc;
        else return -1*asc;
    }
}
