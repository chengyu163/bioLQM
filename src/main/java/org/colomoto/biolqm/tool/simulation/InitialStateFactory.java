package org.colomoto.biolqm.tool.simulation;

import org.colomoto.biolqm.LogicalModel;

public class InitialStateFactory {

    public static byte[] parseInitialState(int size, String s) {
        int n = s.length();
        if (n != size) {
            throw new RuntimeException("Length of initial state mismatch: "+n + " (expected: "+size+")");
        }

        byte[] state = new byte[n];
        for (int i=0 ; i<n ; i++) {
            state[i] = (byte)Character.getNumericValue(s.charAt(i));
        }

        return state;
    }


}
