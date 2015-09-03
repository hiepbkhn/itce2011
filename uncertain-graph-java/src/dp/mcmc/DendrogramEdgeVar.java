package dp.mcmc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import grph.Grph;

public class DendrogramEdgeVar extends DendrogramDeg{
	
	
    
	////
	public double edgeVar(){
        double EV = 0.0;
        Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.root_node);
		while (queue.size() > 0){
			Node r = queue.remove();
            EV += r.nL*r.nR* r.value * (1.0-r.value);
            
            if (r.left.id < 0)            // only internal nodes
                queue.add(r.left);
            if (r.right.id < 0)
                queue.add(r.right);    
		}
        //
        return EV;
	}
    
    ////
    public DendrogramEdgeVar copy() {
    	DendrogramEdgeVar T2 = new DendrogramEdgeVar();
		
		// copy node_dict first
        T2.node_dict = new HashMap<Integer, Node>();
        for (Map.Entry<Integer, Node> entry : this.node_dict.entrySet()){
        	int k = entry.getKey();
        	Node u = entry.getValue();
            Node u2 = u.copy();   // clone u
            T2.node_dict.put(k, u2);
        }
        
        // int_nodes
        T2.int_nodes = Arrays.copyOf(this.int_nodes, this.int_nodes.length);
        
        // node_list
        T2.node_list = new Node[this.node_list.length];
        for (Node u : T2.node_dict.values())
            if (u.id >= 0)
                T2.node_list[u.id] = u;
        
        // parent, left, right
        for (Node u : this.node_dict.values()){
            Node u2 = T2.node_dict.get(u.id);
            if (u.parent != null)
                u2.parent = T2.node_dict.get(u.parent.id);
            if (u.left != null)
                u2.left = T2.node_dict.get(u.left.id);
            if (u.right != null)
                u2.right = T2.node_dict.get(u.right.id);
        }        
        // root_node
        T2.root_node = T2.node_dict.get(this.root_node.id);        
        
        // 1. deg_list, org_deg_list
        T2.org_deg_list = Arrays.copyOf(this.org_deg_list, this.org_deg_list.length);
        
        // 2.
        
        
    	return T2;
    }
    
    ////
 // Exponential mechanism by MCMC
 	// n_samples number of sample T
 	static List<DendrogramEdgeVar> dendrogramFitting(DendrogramEdgeVar T, Grph G, double eps1, int n_steps, int n_samples, int sample_freq){
 		List<DendrogramEdgeVar> list_T = new ArrayList<DendrogramEdgeVar>(); 	// list of sample T
 	    
 	    // delta U
 	    int n_edges = G.getNumberOfEdges();
 	    double dU = 2.0;			// or 2.0
 	    System.out.println("dU = " + dU);
 	    System.out.println("#steps = " + (n_steps + n_samples*sample_freq));
 	    
 	    int out_freq = (n_steps + n_samples*sample_freq)/10;
 	    
 	    // MCMC
 	    long start = System.currentTimeMillis();
 	    int n_accept = 0;
 	    int n_accept_positive = 0;		// accept by the decrease of next_edgeVar
 	    Random random = new Random();
 	    double cur_edgeVar = T.edgeVar();
 	    for (int i = 0; i < n_steps + n_samples*sample_freq; i++){
 	        // randomly pick an internal node (not root)
 	    	Node r_node;
 	        while (true){
 	            int ind = T.int_nodes[random.nextInt(T.int_nodes.length)];
 	            r_node = T.node_dict.get(ind);
 	            if (r_node.parent != null)
 	                break;
 	        }
 	        // randomly use config_2(), config_3()
 	        int rand_val = random.nextInt(2);
 	        
 	        if (rand_val == 0){   // config_2()
 	            Node p_node = T.config_2(G, r_node);
 	            double next_edgeVar = T.edgeVar();
 	            
 	            double prob = Math.exp(eps1/(2*dU)*(cur_edgeVar-next_edgeVar));     // prefer smaller values of next_edgeVar
 	            if (prob > 1.0){
 	                prob = 1.0;
 	                n_accept_positive += 1;
 	            }
 	            double prob_val = random.nextDouble();
 	            if (prob_val > prob)      
 	                // reverse
 	                p_node = T.config_2(G, p_node);      // p_node
 	            else{
 	                n_accept += 1;
 	                cur_edgeVar = next_edgeVar;
 	            }
 	        }    
 	        else{               // config_3()
 	            r_node = T.config_3(G, r_node);
 	            double next_edgeVar = T.edgeVar();
 	            
 	            double prob = Math.exp(eps1/(2*dU)*(cur_edgeVar-next_edgeVar));     // prefer smaller values of next_edgeVar
 	            if (prob > 1.0){
	                prob = 1.0;
	                n_accept_positive += 1;
	            }
 	            double prob_val = random.nextDouble();
 	            if (prob_val > prob)      
 	                // reverse
 	                r_node = T.config_3(G, r_node);      // r_node
 	            else{
 	                n_accept += 1;
 	                cur_edgeVar = next_edgeVar;
 	            }
 	        }
 	        //
 	        if (i % out_freq == 0)
 	        	System.out.println("i = " + i + " logLK = " + T.logLK() + " degDiffL1 = " + T.degDiffL1() + " edgeVar = " + T.edgeVar()
						+ " n_accept = " + n_accept	+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
 	        if (i >= n_steps)
 	            if (i % sample_freq == 0){
 	                DendrogramEdgeVar T2 = T.copy();
 	                list_T.add(T2);
 	            }
 	    }
 	    //
 	    return list_T;
 	}
 	
 	
 	////
	static void writeInternalNodes(List<DendrogramEdgeVar> list_T, String node_file) throws Exception{
	    
		int i = 0;
	    for (DendrogramEdgeVar T : list_T){
	    	T.writeInternalNodes(node_file + "." + i);
	        i++;
	    }
	}
	
	////
	// list_T: list of new DendrogramEdgeVar()
	static void readInternalNodes(Grph G, List<DendrogramEdgeVar> list_T, String node_file, int n_samples) throws Exception{
		int i = 0;
	    for (DendrogramEdgeVar T : list_T){
	    	String filename = node_file + "." + i;
	        i++;
			T.readInternalNodes(G, filename);
	    }
	}
}
