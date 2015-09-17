package hrg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dp.mcmc.Dendrogram;
import dp.mcmc.Int4;
import dp.mcmc.Node;
import grph.Grph;

public class HRG extends Dendrogram {

	////
	public HRG copy(){
		HRG T2 = new HRG();
		
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
            
		
		//
		return T2;
	}
	
	////
	// MCMC
	// n_samples number of sample T
	public static List<HRG> dendrogramFitting(HRG T, Grph G, int n_steps, int n_samples, int sample_freq) {
		List<HRG> list_T = new ArrayList<HRG>(); // list of sample T
		HRG best_T;
		double best_logLT = -Double.MAX_VALUE;

		System.out.println("#steps = " + (n_steps + n_samples * sample_freq));

		int out_freq = (n_steps + n_samples * sample_freq) / 10;

		// MCMC
		long start = System.currentTimeMillis();
		int n_accept = 0;
		int n_accept_positive = 0;
		int n_config2 = 0;
		Random random = new Random();
		double logLT = T.logLK();
		double logLT2;
		
		for (int i = 0; i < n_steps + n_samples * sample_freq; i++) {
			// randomly pick an internal node (not root)
			Node r_node;
			while (true) {
				int ind = T.int_nodes[random.nextInt(T.int_nodes.length)];
				r_node = T.node_dict.get(ind);
				if (r_node.parent != null)
					break;
			}
			// randomly use config_2(), config_3()
			int rand_val = random.nextInt(2);

			if (rand_val == 0) { // config_2()
				Node p_node = T.config_2(G, r_node);
				logLT2 = T.logLK();

				if (logLT2 > logLT){
					n_accept += 1;
					n_accept_positive += 1;
					logLT = logLT2;
				}else{
					double prob = Math.exp(logLT2 - logLT);			// prob << 1.0
					double prob_val = random.nextDouble();
					if (prob_val > prob)
						// reverse
						p_node = T.config_2(G, p_node); // p_node
					else {
						n_accept += 1;
						logLT = logLT2;
					}
				}
				n_config2 += 1;
			} else { // config_3()
				r_node = T.config_3(G, r_node);
				logLT2 = T.logLK();
				
				if (logLT2 > logLT){
					n_accept += 1;
					n_accept_positive += 1;
					logLT = logLT2;
				}else{
					double prob = Math.exp(logLT2 - logLT);			// prob << 1.0
					double prob_val = random.nextDouble();
					if (prob_val > prob)
						// reverse
						r_node = T.config_3(G, r_node); // p_node
					else {
						n_accept += 1;
						logLT = logLT2;
					}
				}
			}
			//
			if (best_logLT < logLT){
				best_logLT = logLT;
//				best_T = T.copy();		// make it run slower than Dendrogram
			}
			
			//
			if (i % out_freq == 0)
				System.out.println("i = " + i + " n_accept = " + n_accept + " logLK = " + T.logLK()
						+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
			if (i >= n_steps)
				if (i % sample_freq == 0) {
					HRG T2 = T.copy();
					list_T.add(T2);
				}
		}
		//
		System.out.println("best_logLT = " + best_logLT);
		
		
		//
		return list_T;
	}

	////
	public static void writeInternalNodes(List<HRG> list_T, String node_file) throws Exception{
	    
		int i = 0;
	    for (HRG T : list_T){
	    	T.writeInternalNodes(node_file + "." + i);
	        i++;
	    }
	}
	
	////
	// list_T: list of new HRG()
	public static void readInternalNodes(Grph G, List<HRG> list_T, String node_file, int n_samples) throws Exception{
		int i = 0;
	    for (HRG T : list_T){
	    	String filename = node_file + "." + i;
	        i++;
			T.readInternalNodes(G, filename);
	    }
	}
}
