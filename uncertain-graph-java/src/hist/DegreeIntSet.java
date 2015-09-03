/*
 * Apr 2
 * 	- converted from Python
 * Apr 3
 * 	- use GraphIntSet for edge switches
 */
package hist;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.mcmc.Dendrogram;
import dp.mcmc.Int4;
import grph.Grph;
import grph.Grph.DIRECTION;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import grph.io.GraphBuildException;
import grph.io.ParseException;
import toools.io.file.RegularFile;
import toools.set.IntSet;

public class DegreeIntSet {
	
	////
	// edit distance score (half)
	static int editScore(Grph G, Grph G0){
	    int score = 0;
	    for (VertexPair p : G.getEdgePairs()){
	    	int u = p.first;
	       	int v = p.second;
	       	IntSet s = G0.getNeighbours(u);
	        if (!s.contains(v))
	            score += 1;
	    }
	    //
	    return score;
	}
	    		
	////
	static int[] readNodeDeg(int n_nodes, String filename_deg) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(filename_deg)); 
	    
	    int[] deg_seq = new int[n_nodes];
	    
        while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split(" ");
        	int u_id = Integer.parseInt(items[0]);
        	int u_deg = Integer.parseInt(items[1]);
        	deg_seq[u_id] = u_deg;
        	
        }
        
        br.close();
	    //
	    return deg_seq;
	}
	
	////
	// reorder node ids in outfile_1k to match original degrees in filename_deg
	static Grph reorderNodes(int n_nodes, String outfile_1k, String filename_deg) throws Exception{

		Grph aG = new InMemoryGrph();
		EdgeListReader reader = new EdgeListReader();
		RegularFile f = new RegularFile(outfile_1k);
		
		aG = reader.readGraph(f);		// .gen file
		
//		for (int u = 0; u < n_nodes; u++)
//			if (!aG.containsVertex(u))
//				aG.addVertex(u);
		
		System.out.println("aG read from " + outfile_1k);
		System.out.println("#nodes = " + aG.getNumberOfVertices());
		System.out.println("#edges = " + aG.getNumberOfEdges());
		
	    // original degrees (.deg file)
	    int[] deg_list = readNodeDeg(n_nodes, filename_deg);
	    
	    List<Int2> org_pairs = new ArrayList<Int2>();
	    for (int u = 0; u < n_nodes; u++)
	    	org_pairs.add(new Int2(u, deg_list[u]));
	    
	    Collections.sort(org_pairs);					// sort by degree (see Int2.java)
	    
	    // degrees in .gen file
//	    List<Int2> gen_pairs = new ArrayList<Int2>();
//	    for (IntCursor c : aG.getVertices())
//	    	gen_pairs.add(new Int2(c.value, aG.getVertexDegree(c.value)));
//	    
//	    Collections.sort(gen_pairs);					// sort by degree (see Int2.java)
	    
	    
	    // map
	    HashMap<Integer, Integer> node_map = new HashMap<Integer, Integer>();
	    for (int u = 0; u < n_nodes; u++)
	        node_map.put(u, org_pairs.get(u).val0);
	    
	    //
	    Grph G0 = new InMemoryGrph("G0", true, DIRECTION.in_out);		//InMemoryGrph.storeEdges = false
	    for (VertexPair p : aG.getEdgePairs()){
	    	int u = p.first;
	       	int v = p.second;
	       	G0.addSimpleEdge(node_map.get(u), node_map.get(v), false);
	    }
	    
		for (int u = 0; u < n_nodes; u++)
			if (!G0.containsVertex(u))
				G0.addVertex(u);
	    return G0;
	}
	
	//// (u,w),(v,t) --> (u,t),(v,w)
	static void edgeSwitchSimple(GraphIntSet I0, int u, int v, int w, int t){
	    I0.addEdge(u, t);
	    I0.addEdge(v, w);
	    I0.removeEdge(u, w);
	    I0.removeEdge(v, t);
	}
	
	////
	static int edgeSwitch(GraphIntSet I0, GraphIntSet I, int cur_score, int u, int v, int w, int t){
		edgeSwitchSimple(I0, u,v,w,t);
		
	    // update score
	    int score = cur_score;
	    if (I.e_list[u].contains(t))
	        score -= 1;
	    if (I.e_list[v].contains(w))
	        score -= 1; 
	    if (I.e_list[u].contains(w))
	        score += 1;
	    if (I.e_list[v].contains(t))
	        score += 1; 
	    //
	    return score;
	}
	
	//// max_switch: 
	static List<Int4> randomSwitch(GraphIntSet I0, GraphIntSet I, int n_nodes, int[] node_list, int max_switch, Int2 score){
		
		Random random = new Random();
	    // find a random pair
	    int count = 0;
	    List<Int4> switch_list = new ArrayList<Int4>();        // list of (u,v,w,t)
	    int n_switch = 1 + random.nextInt(max_switch);
	    
	    while (count < n_switch){
	        int u = node_list[random.nextInt(n_nodes)];
	        int v = node_list[random.nextInt(n_nodes)];
	        if (u != v && I0.e_list[u].size() > 0 && I0.e_list[v].size() > 0){
	        	int[] u_neighbors = I0.e_list[u].toIntArray();
	            int[] v_neighbors = I0.e_list[v].toIntArray();
	            int u_num_nbrs = u_neighbors.length;
	            int v_num_nbrs = v_neighbors.length;
	            // select w,t
	            int w = u_neighbors[random.nextInt(u_num_nbrs)];
	            int t = v_neighbors[random.nextInt(v_num_nbrs)];
	            if (w != v && t != u && w != t && !I0.e_list[u].contains(t) && !I0.e_list[v].contains(w)){
	                // fast
	//                print u,v,w,t
	                score.val0 = edgeSwitch(I0, I, score.val0, u,v,w,t);
	                switch_list.add( new Int4(u,v,w,t));
	                
	                count += 1;
	            }
	        }
	    }
	    
	    //
	//    return score, u,v,w,t
//	    return score, switch_list;
	    return switch_list;
	}
	    		
	//// G0: 
	static void mcmcSampling(Grph G, Grph G0, double eps2, int n_steps, int n_samples, int sample_freq, int max_switch){
	    int n_nodes = G0.getNumberOfVertices();
	    int[] node_list = G0.getVertices().toIntArray();
	    // convert to GraphIntSet
	    GraphIntSet I = new GraphIntSet(G);
	    GraphIntSet I0 = new GraphIntSet(G0);
	    
	//    dU = n_nodes*(n_nodes-1)    // O(n^2)
	//    dU = math.log(n_nodes*(n_nodes-1))
	    double dU = 4.0;
	    int out_freq = (n_steps + n_samples*sample_freq)/10;
	    
	    int cur_score = editScore(G, G0);
	    List<Int4> switch_list;
	    Random random = new Random();
	    // MCMC
	    long start = System.currentTimeMillis();
	    int n_accept = 0;
	    int n_equal = 0;
	    
	    for (int i = 0; i < n_steps + n_samples*sample_freq; i++){
	        if (i % out_freq == 0)
	            System.out.println("i = " + i + " cur_score = " + cur_score + " n_accept = " + n_accept
	            		+ " time : " + (System.currentTimeMillis() - start));
	        
	        Int2 score = new Int2(cur_score, 0);	// init by cur_score
	        switch_list = randomSwitch(I0, I, n_nodes, node_list, max_switch, score);
	        
	        // debug
	        if (cur_score == score.val0)
	            n_equal += 1;
	        
	        double prob = Math.exp(eps2/(2*dU)*(cur_score-score.val0));
	        if (prob > 1.0)
	            prob = 1.0;
	        double prob_val = random.nextDouble();
	        if (prob_val < prob){
	            // accept
	            cur_score = score.val0;
	            n_accept += 1;
	        }else{  
	            // reverse
	        	ListIterator<Int4> it = switch_list.listIterator(switch_list.size());	// point to last
	        	while(it.hasPrevious()) {
	            	Int4 tuple = it.previous();
	            	int u = tuple.val0;
	            	int v = tuple.val1;
	            	int w = tuple.val2;
	            	int t = tuple.val3;
	                edgeSwitchSimple(I0, u,v,t,w);     // t,w
	            }
	        }
	    }
	    
	    //
	    System.out.println("n_accept = " + n_accept);
	    System.out.println("n_equal = " + n_equal);
	    System.out.println("final score = " + cur_score);
	}
	
	////////////////////////////
	public static void main(String[] args) throws Exception{	
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("DegreeIntSet");
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)		105k steps, max_switch = 105 (6.6s)
//		String dataname = "polblogs";		// (1224,16715) 	
		String dataname = "as20graph";		// (6474,12572)		65k steps, max_switch = 6474 (191s)
//		String dataname = "wiki-Vote";		// (7115,100762) 	
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		
		// COMMAND-LINE
		String prefix = "";
	    int n_samples = 5;
		int sample_freq = 100;
		int burn_factor = 100;
		double eps1 = 1.0;
		double eps2 = 4.0;		// for NON-PRIVATE
		int max_switch = 6474;
		
		if(args.length >= 7){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			sample_freq = Integer.parseInt(args[3]);
			burn_factor = Integer.parseInt(args[4]);
			eps1 = Double.parseDouble(args[5]);
			eps2 = Double.parseDouble(args[6]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("eps2 = " + eps2 + " max_switch = " + max_switch);
		System.out.println("burn_factor = " + burn_factor + " sample_freq = " + sample_freq + " n_samples = " + n_samples);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		String filename_1k = prefix + "_dk/" + dataname + "_noisy.1k";
		String filename_deg = filename_1k.substring(0,filename_1k.length()-3) + ".deg";
		String outfile_1k = filename_1k.substring(0,filename_1k.length()-3) + ".gen";

	    //
		EdgeListReader reader = new EdgeListReader();
		EdgeListWriter writer = new EdgeListWriter();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		// TEST reorderNodes(), editScore()
		int n_nodes = G.getNumberOfVertices();
		Grph G0 = reorderNodes(n_nodes, outfile_1k, filename_deg);
				
		int cur_score = editScore(G, G0);
		System.out.println("score = " + cur_score);
	
		
		// TEST mcmcSampling()
		long start = System.currentTimeMillis();
	    mcmcSampling(G, G0, eps2, burn_factor*G.getNumberOfVertices(), n_samples, sample_freq, max_switch);      
	    System.out.println("mcmcSampling - DONE, elapsed " + (System.currentTimeMillis() - start));
	}

}
