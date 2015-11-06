/*
 * Nov 6, 2015
 * 	- implement the new problem: given a fixed dendrogram (balance tree), find the best permutation of leaf nodes (highest likelihood)
 */

package dp.mcmc;

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

import com.carrotsearch.hppc.cursors.IntCursor;

import toools.set.IntHashSet;
import toools.set.IntSet;
import grph.Grph;

public class DendrogramFixed extends Dendrogram{
	
	
	////
	public void initByGraph(Grph G){
		super.initByGraph(G);
		
		// (copied from generateSanitizedSample)
		// build sets (bottom-up), use node.level
	    // array of level -> nodes (see compute_nL_nR)
  		ArrayList<ArrayList<Node>> level_array = new ArrayList<ArrayList<Node>>();
  		for (int i = 0; i < root_node.level+1; i++)
  			level_array.add(new ArrayList<Node>());
  		for (Node u: node_dict.values())
			level_array.get(u.level).add(u);
  		
  		for (Node u : level_array.get(1)){	// parent of leaf nodes
  			u.LS = new IntHashSet();
  			u.LS.add(u.left.id);
  			u.RS = new IntHashSet();
  			u.RS.add(u.right.id);
  		}
  		
	    for (int i = 2; i < this.root_node.level + 1; i++){
	    	for (Node u : level_array.get(i)){
	    		u.LS = new IntHashSet();
	    		u.RS = new IntHashSet();
	    		
	    		if (u.left.id >= 0)
	      			u.LS.add(u.left.id);
	    		else{
	    			u.LS.addAll(u.left.LS);
	    			u.LS.addAll(u.left.RS);
	    		}
	    		
	    		if (u.right.id >= 0)
	      			u.RS.add(u.right.id);
	    		else{
	    			u.RS.addAll(u.right.LS);
	    			u.RS.addAll(u.right.RS);
	    		}
	    	}
	    }
		
	}
 	
	////
	public DendrogramFixed copy(){
		DendrogramFixed T2 = new DendrogramFixed();
		
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
	public int countEdges(Grph G, int u, IntSet S){
		int ret = 0;
		int[] N = G.getNeighbours(u).toIntArray();
		for (int v : N)
			if (S.contains(v))
				ret += 1;
		
		//
		return ret;
	}
	
	//// determine left or right set then remove u_id, add v_id
	//	update nEdge
	public void updateChildSet(Grph G, Node u_parent, Node u, int u_id, int v_id){
//		System.out.println(u_parent.id + " " + u.id + ":" + u_id + " " + v_id);
		
		
		if (u.id == u_parent.left.id){
			u_parent.LS.remove(u_id);
			u_parent.LS.add(v_id);
			
			u_parent.nEdge += countEdges(G, v_id, u_parent.RS);
			u_parent.nEdge -= countEdges(G, u_id, u_parent.RS);
			
		}else{
			u_parent.RS.remove(u_id);
			u_parent.RS.add(v_id);
			u_parent.nEdge += countEdges(G, v_id, u_parent.LS);
			u_parent.nEdge -= countEdges(G, u_id, u_parent.LS);
			
		}
		
		u_parent.value = (double)u_parent.nEdge /u_parent.nL / u_parent.nR;
	}
	
	//// swap two leaf nodes (u,v), update affected parents
	public void swap(Grph G, Node u, Node v){
		Node u_parent = u.parent;
		Node v_parent = v.parent;
		
//		System.out.print(u.id + "(" + u.toplevel + ") " + v.id + "(" + v.toplevel + ") : ");
		
		// find affected internal nodes
		List<Node> listParent = new ArrayList<Node>();
		Node temp_u = u;
		Node temp_v = v;
		if (u.toplevel > v.toplevel){
			Node temp = u;
			for (int i = 0; i < u.toplevel - v.toplevel; i++){
				listParent.add(temp.parent);
				updateChildSet(G, temp.parent, temp, u.id, v.id);
				
				temp = temp.parent;
			}
			temp_u = temp;
		}else{
			Node temp = v;
			for (int i = 0; i < v.toplevel - u.toplevel; i++){
				listParent.add(temp.parent);
				updateChildSet(G, temp.parent, temp, v.id, u.id);
				
				temp = temp.parent;
			}
			temp_v = temp;
		}
		
//		System.out.println(temp_u.id + "(" + temp_u.toplevel + ") " + temp_v.id + "(" + temp_v.toplevel + ")");
		
		while (temp_u.parent.id != temp_v.parent.id){
			listParent.add(temp_u.parent);
			updateChildSet(G, temp_u.parent, temp_u, u.id, v.id);
			
			listParent.add(temp_v.parent);
			updateChildSet(G, temp_v.parent, temp_v, v.id, u.id);
			
			temp_u = temp_u.parent;
			temp_v = temp_v.parent;
		}
		
		listParent.add(temp_u.parent);		// lowest common ancestor
		updateChildSet(G, temp_u.parent, temp_u, u.id, v.id);
		updateChildSet(G, temp_u.parent, temp_v, v.id, u.id);	// because temp_u.parent == temp_v.parent	
		
		// update pointers
		u.parent = v_parent;
		u.toplevel = v_parent.toplevel + 1;
		v.parent = u_parent;
		v.toplevel = u_parent.toplevel + 1;
		// determine left/right child
		if (u.id == u_parent.left.id)
			u_parent.left = v;
		else
			u_parent.right = v;
		
		if (v.id == v_parent.left.id)
			v_parent.left = u;
		else
			v_parent.right = u;
		
		
	}
	
	
	////
	// Exponential mechanism by MCMC
	// n_samples number of sample T
	static List<DendrogramFixed> dendrogramFitting(DendrogramFixed T, Grph G, double eps1, int n_steps, int n_samples, int sample_freq){
		List<DendrogramFixed> list_T = new ArrayList<DendrogramFixed>(); 	// list of sample T
	    
	    // delta U
	    int n_nodes = G.getNumberOfVertices();
	    long nMax = 0;
	    if (n_nodes % 2 == 0) 
	        nMax = n_nodes*n_nodes/4;
	    else 
	        nMax = (n_nodes*n_nodes-1)/4; 
	    double dU = Math.log(nMax) + (nMax-1)*Math.log(1+1.0/(nMax-1));
	    System.out.println("dU = " + dU);
	    System.out.println("#steps = " + (n_steps + n_samples*sample_freq));
	    
	    int out_freq = (n_steps + n_samples*sample_freq)/10;
	    
	    // MCMC
	    long start = System.currentTimeMillis();
	    int n_accept = 0;
	    int n_accept_positive = 0;
	    Random random = new Random();
	    double logLT = T.logLK();
	    double logLT2;
	    
	    for (int i = 0; i < n_steps + n_samples*sample_freq; i++){
	        // randomly pick a pair of leaf nodes
	    	Node u = T.node_list[random.nextInt(T.node_list.length)];
	    	Node v = T.node_list[random.nextInt(T.node_list.length)];
	        while (v.parent.id == u.parent.id)		//v.id == u.id : redundant
	        	v = T.node_list[random.nextInt(T.node_list.length)];
	        
	        // swap (u,v)
	        T.swap(G, u, v);
	        
	        logLT2 = T.logLK();
            
            if (logLT2 > logLT){
				n_accept += 1;
				n_accept_positive += 1;
				logLT = logLT2;
			}else{
				double prob = Math.exp(eps1/(2*dU)*(logLT2 - logLT));			// prob < 1.0
				double prob_val = random.nextDouble();
				if (prob_val > prob)
					// reverse
					T.swap(G, u, v);
				else {
					n_accept += 1;
					logLT = logLT2;
				}
			}
	        
	        //
	        if (i % out_freq == 0)
	        	System.out.println("i = " + i + " n_accept = " + n_accept + " logLK = " + T.logLK()
						+ " n_accept_positive = " + n_accept_positive 
						+ " time : " + (System.currentTimeMillis() - start));
	        if (i >= n_steps)
	            if (i % sample_freq == 0){
	                DendrogramFixed T2 = T.copy();
	                list_T.add(T2);
	            }
	    }
	    //
	    return list_T;
	}
	
	
 	
 	////
	static void writeInternalNodes(List<DendrogramFixed> list_T, String node_file) throws Exception{
	    
		int i = 0;
	    for (DendrogramFixed T : list_T){
	    	T.writeInternalNodes(node_file + "." + i);
	        i++;
	    }
	}
	
	////
	// list_T: list of new DendrogramDeg()
	static void readInternalNodes(Grph G, List<DendrogramFixed> list_T, String node_file, int n_samples) throws Exception{
		int i = 0;
	    for (DendrogramFixed T : list_T){
	    	String filename = node_file + "." + i;
	        i++;
			T.readInternalNodes(G, filename);
	    }
	}
	
	
}
