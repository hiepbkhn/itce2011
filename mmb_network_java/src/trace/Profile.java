/*
 * Mar 20, 2017
 * 	- translated from trace_generator/lbs_class.py (class Profile)
 */

package trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {

	public int user_id = 0;
    public int n_train_paths = 0;
    public List<Path> train_paths = new ArrayList<Path>();       // list of Path(s)
    public Map<Integer, Map<Integer, Double>> trans_prob = new HashMap<Integer, Map<Integer, Double>>(); // dict of dicts ( n1 -> {(n2,f2),(n3,f3),...} ) 
    public Map<Integer, Double> access_prob = new HashMap<Integer, Double>();      // dict of nodes (map access profile)
    
    // for attacks (e.g. Maximum Likelihood Tracking attack)
    public Map<Integer, Map<Integer, Double>> trans_prob_e = new HashMap<Integer, Map<Integer, Double>>();  // dict of dicts ( e1 -> {(e2,f2),(e3,f3),...} ) 
    public Map<Integer, Double> access_prob_e = new HashMap<Integer, Double>();       // dict of edges (map access profile)
    public Map<Integer, Integer> edge_to_id = new HashMap<Integer, Integer>();          // edge_i -> i
    public Map<Integer, Integer> id_to_edge = new HashMap<Integer, Integer>();          // i -> edge_i
    
    // for prediction
    public Map<Integer, Map<Integer, List<Double>>> move_cdf = new HashMap<Integer, Map<Integer, List<Double>>>(); // dict of dicts of lists (CDF) (i,j)[k]
    
    public double user_speed = 0.0;
    public List<Trace> random_traces = new ArrayList<Trace>();     // list of Trace(s)
    public List<Event> exposed_events = new ArrayList<Event>();    // list of Events    
    public double deviation = 0.0;       // 0.0, 0.1, 0.2, 0.3, 0.4, 0.5
}
