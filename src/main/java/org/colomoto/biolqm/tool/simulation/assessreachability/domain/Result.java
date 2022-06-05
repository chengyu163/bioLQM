package org.colomoto.biolqm.tool.simulation.assessreachability.domain;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.AvatarUtils;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.MathFunctions;
import org.colomoto.biolqm.tool.simulation.assessreachability.utils.NaturalOrderComparator;

public class Result {

	/** Associated type of simulation (e.g. Avatar, FF, MonteCarlo) **/
	public EnumAlgorithm algorithm;
	/** Complex attractors (terminal cycles) **/
	public Map<String, StateSet> complexAttractors;
	/** Stable states **/
	public Map<String, State> pointAttractors;
	/** Pattern representation of complex attractors **/
	public Map<String, List<byte[]>> complexAttractorPatterns;
	/** Number of occurrences per attractor **/
	public Map<String, Integer> attractorsCount;
	/** Lower probability bound of attractors **/
	public Map<String, Double> attractorsLowerBound;
	/** Upper probability bound of attractors **/
	public Map<String, Double> attractorsUpperBound;
	/**
	 * Transient cycles * public List<StateSet> transients;
	 */
	public int maxTransientSize = -1;
	/** Depth of attractors (from a well-defined portion of the state space) **/
	public Map<String, List<Integer>> attractorsDepths;
	/** Charts to be plotted **/
	public Map<String, BufferedImage> charts;
	/** Simulation log **/
	public String log;
	/** Maximum number of iterations **/
	public int transientMinSize = -1;
	/** Maximum number of iterations **/
	public int runs = -1;
	/** Number of truncated iterations **/
	public int truncated = -1;
	/** Number of performed iterations **/
	public int performed = -1;
	/** Simulation time (miliseconds) **/
	public long time;
	/** Simulation memory (Mbytes) **/
	public int memory;
	/** FireFront residual probability **/
	public double residual;
	/** Simulation name **/
	public String name;
	/** Parameters description **/
	public String parameters;
	/** Names of components **/
	public String nodes;
	/** Initial states associated with these results **/
	public List<byte[]> iconditions;
	/** Applied perturbations **/
	public String perturbation = null;
	/** Applied reductions **/
	public String reduction = null;
	/** Convergence **/
	public boolean convergence = true;

	private int complexAttID = 0;
	
	public LogicalModel model;

	public Result() {
		attractorsDepths = new HashMap<String, List<Integer>>();
		complexAttractors = new HashMap<String, StateSet>();
		complexAttractorPatterns = new HashMap<String, List<byte[]>>();
		pointAttractors = new HashMap<String, State>();
		attractorsCount = new HashMap<String, Integer>();
		attractorsLowerBound = new HashMap<String, Double>();
		attractorsUpperBound = new HashMap<String, Double>();
		charts = new HashMap<String, BufferedImage>();
	}

	/**
	 * Adds a stable state to results
	 * 
	 * @param s
	 *            stable state to be added
	 */
	public void add(State s) {
		pointAttractors.put(s.key, s);
		attractorsCount.put(s.key, 1);
		attractorsDepths.put(s.key, new ArrayList<Integer>());
	}

	/**
	 * Adds a complex attractor (terminal cycle) to results
	 * 
	 * @param s
	 *            complex attractor to be added
	 */
	public void add(StateSet s) {
		s.setKey("CA" + (++complexAttID));
		complexAttractors.put(s.getKey(), s);
		attractorsCount.put(s.getKey(), 1);
		attractorsDepths.put(s.getKey(), new ArrayList<Integer>());
		// System.out.println("ADDED to:"+complexAttractors.keySet());
	}

	/**
	 * Adds a stable state to results
	 * 
	 * @param s
	 *            stable state to be added
	 * @param steps
	 *            depth of the stable state from a portion of the state space
	 */
	public void add(State s, int steps) {
		if (contains(s)) {
			attractorsCount.put(s.key, attractorsCount.get(s.key) + 1);
		} else {
			pointAttractors.put(s.key, s);
			attractorsCount.put(s.key, 1);
			attractorsDepths.put(s.key, new ArrayList<Integer>());
		}
		attractorsDepths.get(s.key).add(steps);
	}

	/**
	 * Adds a complex attractor (terminal cycle) to results
	 * 
	 * @param s
	 *            complex attractor to be added
	 * @param steps
	 *            depth of the complex attractor from a portion of the state space
	 */
	public void add(StateSet s, int steps) {
		add(s);
		attractorsDepths.get(s.getKey()).add(steps);
	}

	public void incrementComplexAttractor(String key, int steps) {
		attractorsCount.put(key, attractorsCount.get(key) + 1);
		attractorsDepths.get(key).add(steps);
	}

	/**
	 * Checks whether a stable state is stored
	 * 
	 * @param s
	 *            stable state to be checked
	 * @return true if the attractor is stored
	 */
	public boolean contains(State s) {
		return pointAttractors.containsKey(s.key);
	}

	/**
	 * Checks whether a complex attractor is stored
	 * 
	 * @param s
	 *            complex attractor to be checked
	 * @return true if the attractor is stored public boolean
	 *         contains(StateSet s) { return
	 *         complexAttractors.containsKey(s.getKey()); }
	 */

	/**
	 * Increments the number of occurrences for a stable state (assumes the results
	 * contain the attractor)
	 * 
	 * @param s
	 *            stable state whose occurrence is to be accounted
	 */
	public void increment(State s) {
		attractorsCount.put(s.key, attractorsCount.get(s.key) + 1);
	}

	/**
	 * Increments the number of occurrences for a complex attractor (assumes the
	 * results contain the attractor)
	 * 
	 * @param s
	 *            complex attractor whose occurrence is to be accounted
	 */
	public void increment(StateSet s) {
		attractorsCount.put(s.getKey(), attractorsCount.get(s.getKey()) + 1);
	}

	/**
	 * Bounds the probability of a given attractor
	 * 
	 * @param attractor
	 *            the attractor whose probability is to be bounded
	 * @param lower
	 *            lower bound
	 * @param upper
	 *            upper bound
	 */
	public void setBounds(String attractor, double lower, double upper) {
		attractorsLowerBound.put(attractor, Math.max(0, lower));
		attractorsUpperBound.put(attractor, Math.min(1.0, upper));
	}

	/**
	 * Adds a plotted chart to results
	 * 
	 * @param title
	 *            the name of the chart
	 * @param img
	 *            the chart to be stored
	 */
	public void addPlot(String title, BufferedImage img) {
		charts.put(title, img);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {	
		return "\n" + toHTMLString().replace("<br>", "\n").replace("<b>", "").replace("</b>", "").replace("<br/>", "")
				.replace("&nbsp;", "  ").replace("&plusmn;", "+/-").replace("</br>", "" );
	}
	
	
	

	/**
	 * Serializes the results into HTML
	 * 
	 * @return HTML text describing the gathered results
	 */
	public String toHTMLString() {
		
		StringBuffer result = new StringBuffer( name + "</b><br><br><b>Parameters</b><br>"
				+ parameters.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;") + "<br>");
		
		result.append("<br><b>Nodes</b>=[" + nodes + "]<br>");
		if (iconditions != null) {
			result.append("<br><b>Initial conditions</b><br>");
			for (byte[] s : iconditions)
				result.append("&nbsp;&nbsp;&nbsp;" + AvatarUtils.toString(s).replace("-1", "*") + "<br>");
		}

		int sum = MathFunctions.sumCollection(attractorsCount.values());
		double probleft = 0;
		result.append("<br><b>Time</b>=" + (((double) time) / 1000.0) + "s");
		if (algorithm.equals(EnumAlgorithm.AVATAR)) {
			probleft = ((double)(runs-sum))/(double)runs;
			result.append("<br><b>Support</b>: " + sum + " successful runs (below max depth) out of " + runs
					+ "</br>");
		} else if (algorithm.equals(EnumAlgorithm.FIREFRONT)) {
			if (!convergence) {
				result.append("<br><b>Warning</b>: firefront could not converge before reaching the maximum specified depth "
						+ "(probabilities in F higher than beta).<br>");
			} else {
				result.append("<br>Success: the simulation converged before reaching the maximum depth.<br>");
			}
			result.append("<br/><br/><b>Probability of state sets</b>:\n");
			result.append("<br/>&nbsp;&nbsp;&nbsp;Neglected set: " + AvatarUtils.round(this.residual));
			double attSum = 0;
			for (Double val : attractorsLowerBound.values()) {
				attSum += val;
			}
			result.append("<br/>\n&nbsp;&nbsp;&nbsp;Attractor set: " + AvatarUtils.round(attSum) + "<br/>");
			
		} else {
			result.append("<br><b>Support</b>: " + performed + " successful runs (below max depth) out of " + runs
					+ "</br>");
		}

		/** A: print the discovered attractors */
		if (pointAttractors.size() > 0) {
			result.append("<br><b>Stable states</b>:<br>");
			List<String> lTmp = new ArrayList<String>(pointAttractors.keySet());
			Collections.sort(lTmp);
			for (int i = 0; i < lTmp.size(); i++) {
				String key = lTmp.get(i);
				result.append("&nbsp;&nbsp;&nbsp;SS" + (i+1) + "&nbsp;=>&nbsp;" +
						model.stateToNamedState(pointAttractors.get(key).state));
				if (!algorithm.equals(EnumAlgorithm.FIREFRONT)) {
					if (!algorithm.equals(EnumAlgorithm.AVATAR)) {
						result.append("&nbsp;prob=[" + AvatarUtils.round(attractorsLowerBound.get(key)) + "," 
									+ AvatarUtils.round(attractorsUpperBound.get(key)) + "]");
						result.append("&nbsp;depth=" + AvatarUtils.round(MathFunctions.mean(attractorsDepths.get(key)), 1) + "&plusmn;"
								+ AvatarUtils.round(MathFunctions.std(attractorsDepths.get(key)), 1) + "<br>");
					} else {
						double prob = AvatarUtils.round((double)(attractorsCount.get(key)/(double)runs));
						result.append("&nbsp;prob=[" + prob + "," + (prob+probleft) + "]");
						//result.append("&nbsp;prob=" + AvatarUtils.round((double) attractorsCount.get(key) / sum);
						// result+="&nbsp;counts="+attractorsCount.get(key);
						result.append("<br>");
					}
				} else {
					result.append("&nbsp;&nbsp;prob=[" + AvatarUtils.round(attractorsLowerBound.get(key)) + ","
							+ AvatarUtils.round(attractorsUpperBound.get(key)) + "]");
					result.append("&nbsp;depth=" + ((int) MathFunctions.mean(attractorsDepths.get(key))) + "<br>");
				}
			}
		}
		if (complexAttractors.size() > 0) {
			result.append("<br><b>Complex attractors</b>:<br>");
			// int i=0;
			List<String> lsCAs = new ArrayList<String>(complexAttractors.keySet());
			Collections.sort(lsCAs, new NaturalOrderComparator());
			for (String key : lsCAs) {
				if (complexAttractorPatterns.size() == 0)
					result.append("&nbsp;&nbsp;&nbsp;" + key + "&nbsp;=>&nbsp;" 
						//todo +
							);
				else
					result.append("&nbsp;&nbsp;&nbsp;" + key + "&nbsp;=>&nbsp;"
							+ AvatarUtils.toString(complexAttractorPatterns.get(key)).replace("-1", "*"));
				if (algorithm.equals(EnumAlgorithm.FIREFRONT)) {
					result.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prob=["
							+ AvatarUtils.round(attractorsLowerBound.get(key)) + ","
							+ AvatarUtils.round(attractorsUpperBound.get(key)) + "]");
					result.append("&nbsp;depth=" + (int) MathFunctions.mean(attractorsDepths.get(key)) + "<br>");
				} else {
					double prob = AvatarUtils.round((double)(attractorsCount.get(key)/(double)runs));
					result.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prob=[" + prob + "," + (prob+probleft) + "]");
					result.append("&nbsp;size=" + complexAttractors.get(key).size());
					if (!algorithm.equals(EnumAlgorithm.AVATAR) && attractorsDepths.containsKey(key))
						result.append("&nbsp;depth=" + AvatarUtils.round(MathFunctions.mean(attractorsDepths.get(key)),1) + "&plusmn;"
								+ AvatarUtils.round(MathFunctions.std(attractorsDepths.get(key)),1) + "<br>");
					else
						result.append("<br>");
				}
			}
		}

		/** B: prints statistics */
		if (algorithm.equals(EnumAlgorithm.AVATAR)) {
			/*
			 * result+="<br><b>Countings</b>:&nbsp;{"; for(String k :
			 * attractorsCount.keySet()) result+=k+"="+attractorsCount.get(k)+",";
			 * if(attractorsCount.size()>0) result=result.substring(0,result.length()-1);
			 */
			if (maxTransientSize > 0)
				result.append("<br><b>Transient found</b>: #" + maxTransientSize + " states");
			/*
			 * if(transients.size()>0){ result+=" with sizes {"; for(StateSet s :
			 * transients) result+=s.size()+",";
			 * result=result.substring(0,result.length()-1)+"}"; }
			 */
		}
		// result+="<br>Runs:"+runs+" truncated:"+truncated+"
		// performed:"+performed+"</br>";
		// result+="<br><b>Probability bounds</b> per attractor:<br>";
		return result.toString();
	}
	
	/**
	 * Serializes the log into HTML
	 * 
	 * @return HTML text describing the stored log
	 */
	public String logToHTMLString() {
		return log.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
	}

	public String toCSVString() {
		
		StringBuffer result = new StringBuffer(name + "\n\nParameters\n" + parameters.replace("\t", ",").replace("=", ",") + "\n\n");

		result.append("Time," + (((double) time) / 1000.0) + ",secs\n\n");

		/** A: print the discovered attractors */

		int sum = MathFunctions.sumCollection(attractorsCount.values());
		if (pointAttractors.size() > 0) {
			result.append("Stable states\n," + nodes);
			if (algorithm.equals(EnumAlgorithm.AVATAR)) {
				result.append(",prob\n");
			} else if (algorithm.equals(EnumAlgorithm.FIREFRONT)) {
				result.append(",lowerbound,upperbound,depth\n");
			} else {
				result.append(",prob,depth\n");
			}
			List<String> lTmp = new ArrayList<String>(pointAttractors.keySet());
			Collections.sort(lTmp);
			for (int i = 0; i < lTmp.size(); i++) {
				String key = lTmp.get(i);
				result.append("SS" + (i+1) + "," + AvatarUtils.toOpenString(pointAttractors.get(key).state));
				if (!algorithm.equals(EnumAlgorithm.FIREFRONT)) {
					result.append("," + format("%.5f", ((double) attractorsCount.get(key)) / (double) sum));
					// result+=","+attractorsCount.get(key);
					if (!algorithm.equals(EnumAlgorithm.AVATAR)) {
						result.append("," + format("%.1f", MathFunctions.mean(attractorsDepths.get(key))) + "+-"
								+ format("%.1f", MathFunctions.std(attractorsDepths.get(key))) + "\n");
					} else {
						result.append("\n");
					}
				} else {
					result.append("," + format("%.5f", attractorsLowerBound.get(key)) + ","
							+ format("%.5f", attractorsUpperBound.get(key)));
					result.append("," + ((int) MathFunctions.mean(attractorsDepths.get(key))) + "\n");
				}
			}
		}
		if (complexAttractors.size() > 0) {
			result.append("\nComplex attractors\n," + nodes);
			if (algorithm.equals(EnumAlgorithm.AVATAR)) {
				result.append(",prob,size\n");
			} else if (algorithm.equals(EnumAlgorithm.AVATAR)) {
				result.append(",lowerbound,upperbound,depth\n");
			} else {
				result.append(",prob,depth\n");
			}
			for (String key : complexAttractors.keySet()) {
				if (complexAttractorPatterns.size() == 0) 
					result.append("," + key + "," + complexAttractors.get(key).toString());
				else 
					result.append(key + ",");
				boolean first = true;
				for (byte[] s : complexAttractorPatterns.get(key)) {
					result.append(AvatarUtils.toOpenString(s).replace("-1", "*"));
					if (first) {
						first = false;
						if (algorithm.equals(EnumAlgorithm.FIREFRONT)) {
							result.append("," + format("%.5f", attractorsLowerBound.get(key)) + ","
									+ format("%.5f", attractorsUpperBound.get(key)) + ","
									+ (int) MathFunctions.mean(attractorsDepths.get(key)));
						} else {
							result.append("," + format("%.5f", ((double) attractorsCount.get(key)) / (double) sum));// +","+attractorsCount.get(key);
							if (!algorithm.equals(EnumAlgorithm.AVATAR) && attractorsDepths.containsKey(key)) {
								result.append("," + format("%.1f", MathFunctions.mean(attractorsDepths.get(key))) + "+-"
										+ format("%.1f", MathFunctions.std(attractorsDepths.get(key))) + "\n");
							} else {
								result.append("," + complexAttractors.get(key).size());
							}
						}
					}
					result.append("\n,");
				}
			}
		}

		/** B: prints statistics */
		if (algorithm.equals(EnumAlgorithm.AVATAR)) {
			if (maxTransientSize > 0)
				result.append("\n,Max transient size," + maxTransientSize);
			/*
			 * if(transients.size()>0){ for(StateSet s : transients)
			 * result+=s.size()+";"; result=result.substring(0,result.length()-1)+";"; }
			 */
			result.append("\n,Successful runs," + sum + "\n");
		} else {
			if (algorithm.equals(EnumAlgorithm.FIREFRONT)) {
				if (performed == 0) {
					result.append("\n,WARNING:;firefront could not converge before reaching the maximum specified depth. "
							+ "Please increase the maximum depth for a more precise analysis of stable states.\n");
				}

			} else {
				result.append("\n,Support:,Successful runs," + performed + ",Total runs," + runs + "\n");
			}
		}
		
		return result.toString();
	}

	private String format(String pattern, double value) {
		return String.format(pattern, value).replace(",", ".");
	}
}
