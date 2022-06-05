package org.colomoto.biolqm.tool.simulation.assessreachability.domain;

import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AvatarUtils;

public class State {

	public String key = "";
	public boolean btrans;
	public byte[] state;
	public double probability;

	public State(byte[] s) {
		this(s, 1);
	}

	public State(byte[] s, double prob) {
		key = Dictionary.toKey(s);
		// for(byte i : s) key += i;
		state = s;
		probability = prob;
	}

	public String toString() {
		return AvatarUtils.toString(state) + "(prob=" + AvatarUtils.round(probability) + ")";
	}

	public String toShortString() {
		return AvatarUtils.toString(state);
	}
}
