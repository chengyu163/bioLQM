package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

/**
 * Statistical facilities to compute confidence intervals
 * @author Nuno Mendes, Rui Henriques
 */
public final class ConfidenceInterval {

	/**
	 * Computes the confidence interval "lowerboud,upperbound"
	 * @param n number of observation
	 * @param p expected probability
	 * @return string representing the confidence interval: "lowerboud,upperbound"
	 */
	public static String getConfidenceInterval(int n, double p) {
		return getConfidenceInterval(n,p*n,p);
	}
	
	private static String getConfidenceInterval(int n /*runs*/, double x, double p) {
		double k = 1.96;
		double mid = (x+Math.pow(k,2)/2)/(n+Math.pow(k,2));

		double diff = (n < 40) ?  
			((k*Math.sqrt(n))/(n+Math.pow(k,2)))*Math.sqrt((x/n)*(1-(x/n)) + (Math.pow(k,2)/(4*n)))
			: (k/(Math.sqrt(n+Math.pow(k,2)))* Math.sqrt(((x+(Math.pow(k,2)/2))/(n+Math.pow(k,2)))*(1-(x+(Math.pow(k,2)/2))/(n + Math.pow(k,2))) ));

		double min = mid - diff, max = mid + diff;
		if(min<0) min = 0;
		if(max>1) max = 1;

		return min+","+max;
	}
}
