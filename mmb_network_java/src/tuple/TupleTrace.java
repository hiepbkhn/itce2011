package tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TupleTrace {
	public Map<Integer, Map<Integer, Double>> trans_prob; 
    public Map<Integer, Double> access_prob;
    
    public Map<Integer, Map<Integer, Double>> trans_prob_e; 
    public Map<Integer, Double> access_prob_e;
    
    public Map<Integer, Map<Integer, List<Double>>> move_cdf;

    //
	public TupleTrace(Map<Integer, Map<Integer, Double>> trans_prob, Map<Integer, Double> access_prob,
			Map<Integer, Map<Integer, Double>> trans_prob_e, Map<Integer, Double> access_prob_e,
			Map<Integer, Map<Integer, List<Double>>> move_cdf) {
		super();
		this.trans_prob = trans_prob;
		this.access_prob = access_prob;
		this.trans_prob_e = trans_prob_e;
		this.access_prob_e = access_prob_e;
		this.move_cdf = move_cdf;
	}
	
}
