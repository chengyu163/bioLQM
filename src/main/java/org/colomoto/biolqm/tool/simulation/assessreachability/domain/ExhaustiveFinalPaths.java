package org.colomoto.biolqm.tool.simulation.assessreachability.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exhaustive representation of the probabilities of the transitions within and going out of a cycle
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class ExhaustiveFinalPaths {

	public Map<String,Integer> cycle, out;
	public List<List<Double>> exitProbs; 

	/**
	 * Creates an empty cycle
	 */
	public ExhaustiveFinalPaths(){
		cycle = new HashMap<String,Integer>();
		out = new HashMap<String,Integer>();
		exitProbs = new ArrayList<List<Double>>();
	}

	
	/* (non-Javadoc)
	 * @see org.ginsim.service.tool.avatar.domain.FinalPaths#addOutputPaths(java.util.Collection, java.util.Collection, double)
	 */
	public void addOutputPaths(Collection<String> states, Collection<String> exits, double prob){
		//System.out.println("Befor:("+cycle.size()+","+out.size()+")");
		for(String s : states) if(!cycle.containsKey(s)) cycle.put(s, cycle.size());
		for(String s : exits) if(!out.containsKey(s)) out.put(s, out.size());
		//System.out.println("After:("+cycle.size()+","+out.size()+")");
		
		for(int i=exitProbs.size(), l1=cycle.size(); i<l1; i++) exitProbs.add(new ArrayList<Double>());
		for(String s1 : states){
			int i=cycle.get(s1);
			for(String s2 : exits){
				int j=out.get(s2); 
				if(j<exitProbs.get(i).size()) exitProbs.get(i).set(j,prob);
				else exitProbs.get(i).add(prob);
			}
		}
		for(int i=0, l1=cycle.size(); i<l1; i++) 
			while(exitProbs.get(i).size()<out.size()) exitProbs.get(i).add(-1.0);  
	}
	
	/* (non-Javadoc)
	 * @see org.ginsim.service.tool.avatar.domain.FinalPaths#addOutputPaths(java.util.Collection, java.util.Collection, double[][])
	 */
	public void addOutputPaths(Collection<String> states, Collection<String> exits, double[][] probs){
		for(String s : states) if(!cycle.containsKey(s)) cycle.put(s, cycle.size());
		for(String s : exits) if(!out.containsKey(s)) out.put(s, out.size());
		
		for(int i=exitProbs.size(), l1=cycle.size(); i<l1; i++) exitProbs.add(new ArrayList<Double>());
		int index1=0; 
		for(String s1 : states){
			int i=cycle.get(s1);
			int index2=0;
			for(String s2 : exits){
				int j=out.get(s2); 
				if(j<exitProbs.get(i).size()) exitProbs.get(i).set(j,probs[index1][index2]);
				else exitProbs.get(i).add(probs[index1][index2]);
				index2++;
			}
			index1++;
		}
		for(int i=0, l1=cycle.size(); i<l1; i++) 
			while(exitProbs.get(i).size()<out.size()) exitProbs.get(i).add(-1.0);  
	}
	
	/* (non-Javadoc)
	 * @see org.ginsim.service.tool.avatar.domain.FinalPaths#getPaths(java.lang.String)
	 */
	public Map<String, Double> getPaths(String state) {
		Map<String,Double> result = new HashMap<String,Double>();
		Integer index = cycle.get(state);
		if(index==null) return null;
		for(String key : out.keySet()){
			try{
				double v = exitProbs.get(index).get(out.get(key));
				if(v>0) result.put(key, v); 
			} catch(Exception e){
				System.out.println("Exception occurred!");
				System.out.println(">"+key);
				System.out.println(">"+exitProbs.get(index));
				System.out.println(">"+out.get(key));
				System.out.println("="+exitProbs.get(index).get(out.get(key)));
			}
		}
		return result;
	}
	
	public ExhaustiveFinalPaths clone() {
		ExhaustiveFinalPaths res = new ExhaustiveFinalPaths();
		res.cycle = new HashMap<String,Integer>(cycle);
		res.out = new HashMap<String,Integer>(out);
		res.exitProbs = new ArrayList<List<Double>>(exitProbs);
		return res;
	}
}
