/*
 * Sep 9, 2016
 * 	- copied from NodeSetCut
 */

package dp.combined;

import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import algs4.Edge;
import algs4.EdgeWeightedGraph;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.DPUtil;
import dp.mcmc.Dendrogram;
import dp.mcmc.Node;


public class NodeSetCut {

	//
	public boolean[] ind;	// ind[i] = true -> i in S, false -> i in T
	public int[] ind2node;	// ind2node[i] = node id at index i
	public Map<Integer, Integer> node2ind;	// reverse of ind2node
	public int n_s;
	public int n_t;
	//
	public int e_st;
	public List<Int2> e_list;	
	
	public NodeSetCut parent, left, right;		// for recursive partitioning
	public int id;
	public int level = 0;
	
	
	////return e_listS, e_listT (they MUST be initialized outside !)
	public static void getSubEgdeLists(NodeSetCut R, List<Int2> e_listS, List<Int2> e_listT){
		
		int u = 0;
		int v = 0;
		for (Int2 e : R.e_list){
			u = R.node2ind.get(e.val0);
			v = R.node2ind.get(e.val1);
			if (R.ind[u] == true && R.ind[v] == true)
				e_listS.add(e);
			if (R.ind[u] == false && R.ind[v] == false)
				e_listT.add(e);
		}
	}
	
	////
	public static void getSubSet(EdgeWeightedGraph G, NodeSetCut R, NodeSetCut ret, boolean val, List<Int2> e_list){
//		NodeSetCut ret = new NodeSetCut();
		
		ret.node2ind = new HashMap<Integer, Integer>();
		int count = 0;
		for (int i = 0; i < R.ind.length; i++)
			if (R.ind[i] == val){
				ret.node2ind.put(R.ind2node[i], count);
				count ++;
			}
		
		ret.ind = new boolean[count];
		ret.ind2node = new int[count];
		count = 0;
		for (int i = 0; i < R.ind.length; i++)
			if (R.ind[i] == val)
				ret.ind2node[count++] = R.ind2node[i];
		
		//
		int n_nodes = ret.ind.length;
		
		//
		for (int i = 0; i < n_nodes/2; i++)
			ret.ind[i] = true;
		for (int i = n_nodes/2; i < n_nodes; i++)
			ret.ind[i] = false;
		
		ret.n_s = n_nodes/2;
		ret.n_t = n_nodes - n_nodes/2;
		
		// shuffle nodes !
		List<Integer> id = new ArrayList<Integer>();
		for (int i = 0; i < n_nodes; i++)
			id.add(i);
		Collections.shuffle(id);	

		//
		int[] node2Set = new int[n_nodes]; // 1:S, 2:T
		
		for (int i = 0; i < n_nodes; i++)
			if (ret.ind[id.get(i)] == true){
				node2Set[i] = 1;
			}else{
				node2Set[i] = 2;
			}
	
		// e_st
		ret.e_st = 0;
		
		int u_id; 
		int v_id;
		for (Int2 e : e_list){
			u_id = ret.node2ind.get(e.val0);
			v_id = ret.node2ind.get(e.val1);
			if (node2Set[u_id] + node2Set[v_id] == 3)	//  
				ret.e_st += 1;
		}
		
		//
//		return ret;
	}
	
	////
	public NodeSetCut(){
		this.e_list = new ArrayList<Int2>();
	}
	
	////
	public NodeSetCut(EdgeWeightedGraph G){
		int n_nodes = G.V();
		
		this.ind = new boolean[n_nodes];
		this.ind2node = new int[n_nodes];
		this.node2ind = new HashMap<Integer, Integer>();
		
		//
		for (int i = 0; i < n_nodes; i++){
			this.ind2node[i] = i;
			this.node2ind.put(i, i);
		}
		
		// shuffle nodes !
		List<Integer> id = new ArrayList<Integer>();
		for (int i = 0; i < n_nodes; i++)
			id.add(i);
		Collections.shuffle(id);	
		
		//
		for (int i = 0; i < n_nodes/2; i++)
			this.ind[id.get(i)] = true;
		for (int i = n_nodes/2; i < n_nodes; i++)
			this.ind[id.get(i)] = false;
		
		this.n_s = n_nodes/2;
		this.n_t = n_nodes - n_nodes/2;
		//
		int n = G.V();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		for (int u = 0; u < n; u++)
			if (this.ind[this.node2ind.get(u)] == true){
				node2Set[u] = 1;
			}else{
				node2Set[u] = 2;
			}
		
		// e_st, e_s, e_t, e_list
		this.e_st = 0;
		this.e_list = new ArrayList<Int2>();
		
		int u; 
		int v;
		for (Edge e : G.edges()){
			u = e.either();
			v = e.other(u);
			this.e_list.add(new Int2(u, v));
			if (node2Set[u] + node2Set[v] == 3)	//  
				this.e_st += 1;
		}
		
		// debug
		System.out.println("NodeSetCut called: e_st = " + this.e_st);
	}
	
	//// move 1 node u from T to S
	public void add(int u, EdgeWeightedGraph G){
		
		//
		int count_add = 0;
		int count_remove = 0;
		for (int v : G.adj(u).keySet()){
			if (this.node2ind.containsKey(v)){
				if (this.ind[this.node2ind.get(v)] == true)
					count_remove += 1;
				else
					count_add += 1;
			}
		}
		this.n_s += 1;
		this.n_t -= 1;
		
		this.e_st = this.e_st - count_remove + count_add;
		
		//
		this.ind[this.node2ind.get(u)] = true;
		
	}
	
	//// move 1 node u from S to T
	public void remove(int u, EdgeWeightedGraph G){
		
		//
		int count_add = 0;
		int count_remove = 0;
		for (int v : G.adj(u).keySet()){
			if (this.node2ind.containsKey(v)){
				if (this.ind[this.node2ind.get(v)] == true)
					count_add += 1;
				else
					count_remove += 1;
			}
		}
		this.n_s -= 1;
		this.n_t += 1;
		
		this.e_st = this.e_st - count_remove + count_add;
		
		this.ind[this.node2ind.get(u)] = false;
		
		
	}
	
	////move 1 item u from S back to T
	public void reverse_add(int u, EdgeWeightedGraph G, int old_st, int old_s, int old_t){
		this.e_st = old_st;
		
		this.ind[this.node2ind.get(u)] = false;
		
		this.n_s -= 1;
		this.n_t += 1;
		// 
	}
	
	////move 1 item u from T back to S
	public void reverse_remove(int u, EdgeWeightedGraph G, int old_st, int old_s, int old_t){
		this.e_st = old_st;
		
		this.ind[this.node2ind.get(u)] = true;
		
		this.n_s += 1;
		this.n_t -= 1;
		
	}
	
	
	////
	public void print(){
		System.out.print("S : ");
		for (int s = 0; s < this.ind.length; s++)
			if (this.ind[s] == true)
			System.out.print(this.ind2node[s] + " ");
		System.out.println();
		
		System.out.print("T : ");
		for (int t = 0; t < this.ind.length; t++)
			if (this.ind[t] == false)
			System.out.print(this.ind2node[t] + " ");
		System.out.println();
	}
	
	////
	public int pickRandomFromS(Random random){
		int loc = random.nextInt(this.ind.length);
		while (true){
			if (this.ind[loc] == true)
				return this.ind2node[loc];
			loc = random.nextInt(this.ind.length);
		}
	}
	
	////
	public int pickRandomFromT(Random random){
		int loc = random.nextInt(this.ind.length);
		while (true){
			if (this.ind[loc] == false)
				return this.ind2node[loc];
			loc = random.nextInt(this.ind.length);
		}
	}
	
	//// 
	public static void queryCut(NodeSetCut R, EdgeWeightedGraph G, double eps, double ratio_eps, double threshold, int min_frac, int out_freq, boolean print_out){
		if (print_out)
			System.out.println("NodeSetCut.queryCut called");
		
		int n_nodes = R.ind.length;
		
		int lower_size = n_nodes/min_frac;		
		
		//
		double eps_t = ratio_eps*eps;
		double eps_c = (1-ratio_eps)*eps;
		System.out.println("eps_t = " + String.format("%.3f",eps_t) + " eps_c = " + String.format("%.3f",eps_c));
		
		double noisy_threshold = threshold + DPUtil.laplaceMechanism(1/eps_t);
		System.out.println("noisy_threshold = " + noisy_threshold);
		
		//
		long start = System.currentTimeMillis();
		boolean is_add = true;			// add or remove
		Random random = new Random();
		int u = -1;
		
		int i = 0;
		while (true) {		// INFINITE LOOP !
			// decide add or remove
			if (R.n_s < n_nodes/2 && R.n_s > lower_size){	// add or remove
				int rand_val = random.nextInt(2);
				if (rand_val == 0)
					is_add = true;
				else
					is_add = false;	
			}else if (R.n_s <= lower_size){			// only add
				is_add = true;
			}else{								// only remove (R.S.size() >= n_nodes/2)
				is_add = false;
			}
			
			i = i + 1;
			
			// perform add or remove
			if (is_add){
				// randomly pick an item from T
				u = R.pickRandomFromT(random);
				R.add(u, G);
				
			}else{
				// randomly pick an item from S
				u = R.pickRandomFromS(random);
				R.remove(u, G);
			}
			
			// cut query
			double noisy_cut = R.e_st + DPUtil.laplaceMechanism(1/eps_c);
			
			if (noisy_cut < noisy_threshold)
				break;
			
			if (i % out_freq == 0 && print_out)
				System.out.println("i = " + i + " R.e_st = " + R.e_st + " time : " + (System.currentTimeMillis() - start));
		}
		
	}
	
	
	//////////////////////////////
	// limit_size = 32: i.e. for NodeSet having size <= limit_size, call 
	public static NodeSetCut recursiveCut(EdgeWeightedGraph G, double eps, double ratio_eps, int min_frac, int max_level, double ratio_level){
		int id = -1;
		
		//
		double[] epsArr = DPUtil.epsilonByLevel(eps, max_level, ratio_level); 
		
		// root node
		NodeSetCut root = new NodeSetCut(G);
		root.id = id--;
		root.level = 0;
		// 
		Queue<NodeSetCut> queue = new LinkedList<NodeSetCut>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetCut R = queue.remove();
			System.out.println("R.level = " + R.level + " R.S.size() + R.T.size() = " + R.ind.length + " R.e_st = " + R.e_st);
			
			if (R.level == max_level){
				continue;
			}
			
			// THRESHOLD
//			double threshold = Math.sqrt(R.ind.length);
//			double threshold = R.ind.length;		// OK,
			double threshold = R.ind.length*0.4;	// 0.8->0.6 OK, 0.5 failed (150s)
			System.out.println("threshold = " + threshold);
			
			// call queryCut()
			long start = System.currentTimeMillis();
			NodeSetCut.queryCut(R, G, epsArr[R.level], ratio_eps, threshold, min_frac, 1000*R.ind.length, true);
			System.out.println("elapsed " + (System.currentTimeMillis() - start));
			
			// prepare two children RS, RT
			NodeSetCut RS = new NodeSetCut();
			NodeSetCut RT = new NodeSetCut();
			
			getSubEgdeLists(R, RS.e_list, RT.e_list);
			
			getSubSet(G, R, RS, true, RS.e_list);
			getSubSet(G, R, RT, false, RT.e_list);
					
			RS.id = id--;
			R.left = RS;
			RS.parent = R;
			RS.level = R.level + 1;
			
			RT.id = id--;
			R.right = RT;
			RT.parent = R;
			RT.level = R.level + 1;
			
			//
//			if (R.S.size() > lower_size)
				queue.add(RS);
			
//			if (R.T.size() > lower_size)
				queue.add(RT);
			
		}
		
		//
		return root;
	}
	
	////
	public static void printSetIds(NodeSetCut root_set, int m){
		System.out.println("printSetIds");
		
		Queue<NodeSetCut> queue_set = new LinkedList<NodeSetCut>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetCut R = queue_set.remove();
			if (R.left != null){
				
				System.out.print("{");
				for (int i = 0; i < R.ind.length; i++)
					System.out.print(R.ind2node[i] + " ");
				System.out.println("}");
				
				// " (" + R.e_st + "," + R.e_s + "," + R.e_t + "," + R.d_s + "," + R.d_t + "," + m + ")" + 
				System.out.println("R.id = " + R.id + " left.id = " + R.left.id + " right.id = " + R.right.id + 
						" left.size = " + R.n_s + " right.size = " + R.n_t);
			}else{
				System.out.println("LEAF R.id = " + R.id); // + " left.size = " + R.S.size() + " right.size = " + R.T.size());
				//
//				System.out.print("LEAF R.id = " + R.id + " : {");
//				for (IntCursor t : R.S)
//					System.out.print(t.value + " ");
//				System.out.print(" ** ");
//				for (IntCursor t : R.T)
//					System.out.print(t.value + " ");
//				System.out.println("}");
			}
			if (R.left != null){
				queue_set.add(R.left);
				queue_set.add(R.right);
			}
		}
	}
	
	////
	public static void writePart(NodeSetCut root_set, String part_file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));
		
		Queue<NodeSetCut> queue_set = new LinkedList<NodeSetCut>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetCut R = queue_set.remove();
			
			if (R.left != null){
				queue_set.add(R.left);
				queue_set.add(R.right);
			}else{	// leaf
				for (int s = 0; s < R.ind.length; s++)
					if (R.ind[s] == true)
						bw.write(R.ind2node[s] + ",");
				for (int s = 0; s < R.ind.length; s++)
					if (R.ind[s] == false)
						bw.write(R.ind2node[s] + ",");
				bw.write("\n");
			}
		}
		
		bw.close();
	}
	
	
	////
	public static int readPart(String part_file, Map<Integer, Integer> part_init) throws IOException{
		
		
		BufferedReader br = new BufferedReader(new FileReader(part_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	if (str.length() ==0)
        		continue;
        	
        	String[] items = str.split(",");
        	
        	for (int i = 0; i < items.length; i++){
        		int u = Integer.parseInt(items[i]);
        		part_init.put(u, count);
        	}
        		
        	//
        	count += 1;
		}
		
		br.close();
		//
		return count;
	}
	
}
