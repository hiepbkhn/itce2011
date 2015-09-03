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

import dp.mcmc.DendrogramDeg;
import dp.mcmc.Int4;
import dp.mcmc.Node;
import grph.Grph;

public class HRGDeg extends DendrogramDeg {

	////
	public HRGDeg copy(){
		HRGDeg T2 = new HRGDeg();
		
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
            
        // deg_list, org_deg_list
        T2.org_deg_list = Arrays.copyOf(this.org_deg_list, this.org_deg_list.length);
		
		//
		return T2;
	}
	
	////
	// MCMC
	// n_samples number of sample T
	static List<HRGDeg> dendrogramFitting(HRGDeg T, Grph G, int n_steps, int n_samples, int sample_freq) {
		List<HRGDeg> list_T = new ArrayList<HRGDeg>(); // list of sample T
		HRGDeg best_T;
		double best_degDiffL1 = Double.MAX_VALUE;

		System.out.println("#steps = " + (n_steps + n_samples * sample_freq));

		int out_freq = (n_steps + n_samples * sample_freq) / 10;

		// MCMC
		long start = System.currentTimeMillis();
		int n_accept = 0;
		int n_accept_positive = 0;
		Random random = new Random();
		double cur_degDiffL1 = T.degDiffL1();
		double next_degDiffL1;
		
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
				next_degDiffL1 = T.degDiffL1();

				if (next_degDiffL1 < cur_degDiffL1){
					n_accept += 1;
					n_accept_positive += 1;
					cur_degDiffL1 = next_degDiffL1;
				}else{
					double prob = Math.exp(cur_degDiffL1-next_degDiffL1);		// NOT cur_degDiffL1/next_degDiffL1;
					double prob_val = random.nextDouble();
					if (prob_val > prob)
						// reverse
						p_node = T.config_2(G, p_node); // p_node
					else {
						n_accept += 1;
						cur_degDiffL1 = next_degDiffL1;
					}
				}
			} else { // config_3()
				r_node = T.config_3(G, r_node);
				next_degDiffL1 = T.degDiffL1();
				
				if (next_degDiffL1 < cur_degDiffL1){
					n_accept += 1;
					n_accept_positive += 1;
					cur_degDiffL1 = next_degDiffL1;
				}else{
					double prob = Math.exp(cur_degDiffL1-next_degDiffL1);		// NOT cur_degDiffL1/next_degDiffL1;
					double prob_val = random.nextDouble();
					if (prob_val > prob)
						// reverse
						r_node = T.config_3(G, r_node); // p_node
					else {
						n_accept += 1;
						cur_degDiffL1 = next_degDiffL1;
					}
				}
			}
			//
			if (best_degDiffL1 > cur_degDiffL1){
				best_degDiffL1 = cur_degDiffL1;
				best_T = T.copy();
			}
			
			//
			if (i % out_freq == 0)
				System.out.println("i = " + i + " n_accept = " + n_accept
						+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
			if (i >= n_steps)
				if (i % sample_freq == 0) {
					HRGDeg T2 = T.copy();
					list_T.add(T2);
				}
		}
		//
		System.out.println("best_degDiffL1 = " + best_degDiffL1);
		
		
		//
		return list_T;
	}

	////
	static void writeInternalNodes(List<HRGDeg> list_T, String node_file) throws Exception{
	    
		int i = 0;
	    for (HRGDeg T : list_T){
	    	T.writeInternalNodes(node_file + "." + i);
	        i++;
	    }
	}
	
	////
	// list_T: list of new HRG()
	static void readInternalNodes(Grph G, List<HRGDeg> list_T, String node_file, int n_samples) throws Exception{
		int i = 0;
	    for (HRGDeg T : list_T){
	    	String filename = node_file + "." + i;
	        i++;
			T.readInternalNodes(G, filename);
	    }
	}
}
