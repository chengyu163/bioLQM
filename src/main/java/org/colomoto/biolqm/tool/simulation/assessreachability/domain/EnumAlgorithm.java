package org.colomoto.biolqm.tool.simulation.assessreachability.domain;

public enum EnumAlgorithm {
	AVATAR("Avatar"), FIREFRONT("FireFront"), MONTE_CARLO("MonteCarlo");
	private String desc;

	private EnumAlgorithm(String desc) {
		this.desc = desc;
	}

	public String toString() {
		return this.desc;
	}
}