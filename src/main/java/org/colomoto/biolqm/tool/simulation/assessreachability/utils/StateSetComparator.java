package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

import java.util.Comparator;

import org.colomoto.biolqm.tool.simulation.assessreachability.domain.StateSet;

public class StateSetComparator implements Comparator<StateSet>{

	@Override
    public int compare(StateSet x, StateSet y){
        if (x.size() < y.size()) return 1;
        if (x.size() > y.size()) return -1;
        return 0;
    }
}
