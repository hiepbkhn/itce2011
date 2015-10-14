/*
 * Apr 13, 2015
 * 	- copied from NodeSet, try to speedup add(), remove()
 *  - add reverse_add(), reverse_remove()
 * Sep 3
 * 	- add param binaryPart to recursiveLK() (used in dp.combined.*) 
 * 	- add param lower_size to partitionLK() (used in dp.combined.*) 
 * Sep 17
 * 	- copied from NodeSetDiv
 * Sep 22
 * 	- update to NodeSetDivGreedy
 * Sep 24
 * 	- not use IntHashSet.pickRandomElement, copied from NodeSetDivGreedy.java
 * 	- use EdgeWeightedGraph in place of EdgeWeightedGraph
 * Oct 11
 * 	- copy writeTree, readTree, bestCutOffline from NodeSetLouvainOpt
 * 	- copy getSubEgdeLists() from NodeSetMod
 */

package dp.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.DPUtil;
import dp.mcmc.Dendrogram;
import dp.mcmc.Node;
import algs4.Edge;
import algs4.EdgeWeightedGraph;
import grph.VertexPair;
import hist.Int2;
import toools.set.BitVectorSet;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class NodeSetDivGreedy {

	public static final int TYPE_LOG_LK = 0;
	public static final int TYPE_MINCUT = 1;
	//
	//
	public boolean[] ind;	// ind[i] = true -> i in S, false -> i in T
	public int[] ind2node;	// 
	public Map<Integer, Integer> node2ind;
	//
	public int e_st;		//
	public int e_s;
	public int e_t;
	public int n_s = 0;
	public int n_t = 0;
	public int d_s = 0; 	// total degree of nodes in S
	public int d_t = 0;		// total degree of nodes in T
	public List<Int2> e_list;	
	
	public NodeSetDivGreedy parent, left, right;		// for recursive partitioning
	public int id;
	public int level = 0;
	public double modSelf = 0.0;	// see writeTree, readTree
	
	
	////return e_listS, e_listT (they MUST be initialized outside !)
	public static void getSubEgdeLists(NodeSetDivGreedy R, List<Int2> e_listS, List<Int2> e_listT){
		
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
	public static void getSubSet(EdgeWeightedGraph G, NodeSetDivGreedy R, NodeSetDivGreedy ret, boolean val, List<Int2> e_list){
//		NodeSetDivGreedy ret = new NodeSetDivGreedy();
		
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
		//
		int n = G.V();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		// d_s, d_t
		ret.d_s = 0;
		ret.d_t = 0;
		for (int u = 0; u < n; u++)
			if (ret.node2ind.containsKey(u)){
				if (ret.ind[ret.node2ind.get(u)] == true){
					ret.d_s += G.degree(u);
					node2Set[u] = 1;
				}else{
					ret.d_t += G.degree(u);
					node2Set[u] = 2;
				}
			}
		
		// e_st, e_s, e_t
		ret.e_st = 0;
		ret.e_s = 0;
		ret.e_t = 0;
		
		int u_id; 
		int v_id;
		for (Int2 e : e_list){
			u_id = ret.node2ind.get(e.val0);
			v_id = ret.node2ind.get(e.val1);
			if (node2Set[u_id] + node2Set[v_id] == 3)	//  
				ret.e_st += 1;
			if (node2Set[u_id] == 1 && node2Set[v_id] == 1)
				ret.e_s += 1;
			if (node2Set[u_id] == 2 && node2Set[v_id] == 2)
				ret.e_t += 1;
		}
		
		//
//		return ret;
	}
	
	////
	public NodeSetDivGreedy(){
		this.e_list = new ArrayList<Int2>();
	}
	
	////
	public NodeSetDivGreedy(EdgeWeightedGraph G){
		int n_nodes = G.V();
		
		this.ind = new boolean[n_nodes];
		this.ind2node = new int[n_nodes];
		this.node2ind = new HashMap<Integer, Integer>();
		
		//
		for (int i = 0; i < n_nodes; i++){
			this.ind2node[i] = i;
			this.node2ind.put(i, i);
		}
		
		//
		for (int i = 0; i < n_nodes/2; i++)
			this.ind[i] = true;
		for (int i = n_nodes/2; i < n_nodes; i++)
			this.ind[i] = false;
		
		this.n_s = n_nodes/2;
		this.n_t = n_nodes - n_nodes/2;
		//
		int n = G.V();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		// d_s, d_t
		this.d_s = 0;
		this.d_t = 0;
		for (int u = 0; u < n; u++)
			if (this.ind[this.node2ind.get(u)] == true){
				this.d_s += G.degree(u);
				node2Set[u] = 1;
			}else{
				this.d_t += G.degree(u);
				node2Set[u] = 2;
			}
		
		// e_st, e_s, e_t
		this.e_st = 0;
		this.e_s = 0;
		this.e_t = 0;
		this.e_list = new ArrayList<Int2>();
		
		int u; 
		int v;
		for (Edge e : G.edges()){
			u = e.either();
			v = e.other(u);
			this.e_list.add(new Int2(u, v));
			if (node2Set[u] + node2Set[v] == 3)	//  
				this.e_st += 1;
			if (node2Set[u] == 1 && node2Set[v] == 1)
				this.e_s += 1;
			if (node2Set[u] == 2 && node2Set[v] == 2)
				this.e_t += 1;
		}
		
	}
	
	//// move 1 item u from T to S
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
		this.e_s = this.e_s + count_remove;
		this.e_t = this.e_t - count_add;
		
		//
		this.ind[this.node2ind.get(u)] = true;
		
		// 
		int deg_u = G.degree(u);
		this.d_s += deg_u;
		this.d_t -= deg_u;
	}
	
	//// move 1 item u from S to T
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
		this.e_s = this.e_s - count_add;
		this.e_t = this.e_t + count_remove;
		
		this.ind[this.node2ind.get(u)] = false;
		
		// 
		int deg_u = G.degree(u);
		this.d_s -= deg_u;
		this.d_t += deg_u;
	}
	
	////move 1 item u from S back to T
	public void reverse_add(int u, EdgeWeightedGraph G, int old_st, int old_s, int old_t){
		int deg_u = G.degree(u);
		this.e_st = old_st;
		this.e_s = old_s;
		this.e_t = old_t;
		
		this.ind[this.node2ind.get(u)] = false;
		
		this.n_s -= 1;
		this.n_t += 1;
		
		// 
		this.d_s -= deg_u;
		this.d_t += deg_u;
	}
	
	////move 1 item u from T back to S
	public void reverse_remove(int u, EdgeWeightedGraph G, int old_st, int old_s, int old_t){
		int deg_u = G.degree(u);
		this.e_st = old_st;
		this.e_s = old_s;
		this.e_t = old_t;
		
		this.ind[this.node2ind.get(u)] = true;
		
		this.n_s += 1;
		this.n_t -= 1;
		//
		this.d_s += deg_u;
		this.d_t -= deg_u;
	}
	
	////m : number of edges in G
	public double modularity(int m){
		double mod = 0.0;
		
//		System.out.println(" e_st = " + this.e_st + " e_s = " + this.e_s + " e_t = " + this.e_t + " d_s = " + this.d_s + " d_t = " + this.d_t);
		
		mod = (double)this.e_s/m - (this.d_s/(2.0*m)) *(this.d_s/(2.0*m))  +  (double)this.e_t/m - (this.d_t/(2.0*m))*(this.d_t/(2.0*m));	// avoid over flow of int*int
		
		//
		return mod;
	}
	
	////m : number of edges in G
	public double modularitySelf(int m){
		double mod = 0.0;
		
		double lc = this.e_s + this.e_t + this.e_st;
		double dc = this.d_s + this.d_t;
		
		mod = lc/m - (dc/(2.0*m))*(dc/(2.0*m));
		
		//
		return mod;
	}
	
	////m : number of edges in G
	public double modularityAll(int m){
		NodeSetDivGreedy root_set = this;
		while (root_set.parent != null)
			root_set = root_set.parent;
		
		double mod = 0.0;
		
		Queue<NodeSetDivGreedy> queue_set = new LinkedList<NodeSetDivGreedy>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetDivGreedy R = queue_set.remove();
			if (R.left == null) // leaf
				mod += R.modularitySelf(m);
			else{
				queue_set.add(R.left);
				queue_set.add(R.right);
			}
				
		}
		
		//
		return mod;
	}
	
	////
	public double logLK(){
		double L = 0.0;
		//
		long nsC2 = this.n_s*(this.n_s-1)/2; 		// int: overflow on amazon, youtube
		double p_s = (double)this.e_s/nsC2;
		if (p_s > 0.0 && p_s < 1.0) 
			L += this.e_s * Math.log(p_s) + (nsC2-this.e_s)*Math.log(1-p_s);
		
		//
		long ntC2 = this.n_t*(this.n_t-1)/2; 		// int: overflow on amazon, youtube
		double p_t = (double)this.e_t/ntC2;
		if (p_t > 0.0 && p_t < 1.0) 
			L += this.e_t * Math.log(p_t) + (ntC2-this.e_t)*Math.log(1-p_t);
		
		//
		long nst = this.n_s * this.n_t;				// int: overflow on amazon, youtube
		double p_st = (double)this.e_st/nst;
		if (p_st > 0.0 && p_st < 1.0) 
			L += this.e_st * Math.log(p_st) + (nst-this.e_st)*Math.log(1-p_st);
		
		//
		return L;
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
	
	//// LOG-LIKELIHOOD partition, using logLK()
	public static void partitionLK(NodeSetDivGreedy R, EdgeWeightedGraph G, double eps_p, int n_steps, int n_samples, int sample_freq, boolean print_out, int lower_size){
		if (print_out)
			System.out.println("Node.partitionLK called");
		
//		int n_nodes = G.getNumberOfVertices();
		int n_nodes = R.ind.length;
		
		// compute dU
		long nMax = 0;
	    if (n_nodes % 2 == 0) 
	        nMax = n_nodes*n_nodes/4;
	    else 
	        nMax = (n_nodes*n_nodes-1)/4; 
	    double dU = Math.log(nMax) + (nMax-1)*Math.log(1+1.0/(nMax-1));
		
//		if (print_out)
//			System.out.println("#steps = " + (n_steps + n_samples * sample_freq));

		int out_freq = (n_steps + n_samples * sample_freq) / 10;
		//
		long start = System.currentTimeMillis();
		boolean is_add = true;			// add or remove
		Random random = new Random();
		int n_accept = 0;
		int n_accept_positive = 0;
		int u = -1;
		
		double logLT = R.logLK();
		double logLT2;
		int old_st = R.e_st;
		int old_s = R.e_s;
		int old_t = R.e_t;
		
		for (int i = 0; i < n_steps + n_samples * sample_freq; i++) {
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
			
			// MCMC
			logLT2 = R.logLK();
			
			if (logLT2 > logLT){
				n_accept += 1;
				n_accept_positive += 1;
				logLT = logLT2;
				//
				old_st = R.e_st;
				old_s = R.e_s;
				old_t = R.e_t;
			}else{
				double prob = Math.exp(eps_p/(2*dU)*(logLT2 - logLT));			// prob << 1.0
				double prob_val = random.nextDouble();
				if (prob_val > prob){
					// reverse
					if (is_add)
						R.reverse_add(u, G, old_st, old_s, old_t);
					else
						R.reverse_remove(u, G, old_st, old_s, old_t);
				}else {
					n_accept += 1;
					logLT = logLT2;
					//
					old_st = R.e_st;
					old_s = R.e_s;
					old_t = R.e_t;
				}
			}
			
			if (i % out_freq == 0 && print_out)
				System.out.println("i = " + i + " n_accept = " + n_accept + " logLK = " + R.logLK()
						+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
		}
		
	}
	
	
	////
//	public static int binaryPartition(EdgeWeightedGraph G, NodeSetDivGreedy root, int id){
//		
//		int cur_id = id;
//		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
//		queue.add(root);
//		while(queue.size() > 0){
//			NodeSetDivGreedy R = queue.remove();
//			
//			if (R.n_s >= 2){
//				NodeSetDivGreedy RS = getSubSet(G, R, true);;
//				RS.id = cur_id--;
//				R.left = RS;
//				RS.parent = R;
//				queue.add(RS);
//			}else{
//				NodeSetDivGreedy RS = getSubSet(G, R, true);;		// RS.id is the remaining item in S
//				R.left = RS;
//				RS.parent = R;
//			}
//			
//			if (R.n_t >= 2){
//				NodeSetDivGreedy RT = getSubSet(G, R, false);
//				RT.id = cur_id--;
//				R.right = RT;
//				RT.parent = R;
//				queue.add(RT);
//			}else{
//				NodeSetDivGreedy RT = getSubSet(G, R, false);		// RT.id is the remaining item in T
//				R.right = RT;
//				RT.parent = R;
//			}
//				
//		}
//		
//		//
//		return cur_id;
//	}
	
	//////////////////////////////
	// limit_size = 32: i.e. for NodeSet having size <= limit_size, call 
	public static NodeSetDivGreedy recursiveLK(EdgeWeightedGraph G, double eps1, int burn_factor, int limit_size, int lower_size, int max_level, double ratio){
		int n_nodes = G.V();
		int id = -1;
		
		double[] epsArr = DPUtil.epsilonByLevel(eps1, max_level, ratio); 
		
		// root node
		NodeSetDivGreedy root = new NodeSetDivGreedy(G);
		root.id = id--;
		root.level = 0;
		// 
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
//			System.out.println("R.level = " + R.level + " R.S.size() + R.T.size() = " + R.ind.length);
			
			// USE limit_size
			if (R.ind.length <= limit_size || R.level == max_level)		// changed: < to <=
				continue;
			
			long start = System.currentTimeMillis();
			NodeSetDivGreedy.partitionLK(R, G, epsArr[R.level], burn_factor*R.ind.length, 0, 0, false, lower_size);
//			System.out.println("elapsed " + (System.currentTimeMillis() - start));
			
			
			NodeSetDivGreedy RS = new NodeSetDivGreedy();
			NodeSetDivGreedy RT = new NodeSetDivGreedy();
			
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
			queue.add(RS);
		
			queue.add(RT);
			
		}
		
		//
		return root;
	}
	
	////
	public static void printSetIds(NodeSetDivGreedy root_set, int m){
		System.out.println("printSetIds");
		
		Queue<NodeSetDivGreedy> queue_set = new LinkedList<NodeSetDivGreedy>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetDivGreedy R = queue_set.remove();
			if (R.left != null)
				System.out.println("R.id = " + R.id + " left.id = " + R.left.id + " right.id = " + R.right.id + 
						" left.size = " + R.n_s + " right.size = " + R.n_t + " mod = " + String.format("%.4f", R.modularity(m)) + 
						" modSelf = " + String.format("%.4f", R.modularitySelf(m)));
			else
				System.out.println("LEAF R.id = " + R.id);
			if (R.left != null){
				queue_set.add(R.left);
				queue_set.add(R.right);
			}
		}
	}
	
	////
	public static Dendrogram convertToHRG(EdgeWeightedGraph G, NodeSetDivGreedy root_set){
		int n_nodes = G.V();
		Dendrogram D = new Dendrogram(); 
		D.node_list = new Node[n_nodes];
		
		// D.root_node and D.node_list
		Node root_node = new Node(root_set.id, null, 0.0);
		
		Queue<NodeSetDivGreedy> queue_set = new LinkedList<NodeSetDivGreedy>();
		Queue<Node> queue = new LinkedList<Node>();
		
		queue_set.add(root_set);	// parallel queues
		queue.add(root_node);
		
		while(queue.size() > 0){
			NodeSetDivGreedy R = queue_set.remove();
			Node r = queue.remove();
			
			if (R.left != null){	//internal node
				Node left_node = new Node(R.left.id, r, 0.0);
				r.left = left_node;
				queue_set.add(R.left);
				queue.add(left_node);

				Node right_node = new Node(R.right.id, r, 0.0);
				r.right = right_node;
				queue_set.add(R.right);
				queue.add(right_node);
			}else{					// leaf node
				D.node_list[R.id] = r;
			}
			
		}
		
		D.root_node = root_node;
		// COPIED from Dendrogram.initByInternalNodes()
		// compute node_dict{}
        D.int_nodes = new int[D.node_list.length-1];
        D.node_dict = Dendrogram.buildNodeDict(D.root_node, D.int_nodes);
        
        //
        long start = System.currentTimeMillis();
        D.computeNodeLevels();		// for compute_nL_nR() in buildDendrogram()
//        D.computeTopLevels();
        
        //
        Dendrogram.buildDendrogram(D.node_list, D.node_dict, D.root_node, G);
        System.out.println("buildDendrogram - DONE, elapsed " + (System.currentTimeMillis() - start));
		//
		return D;
	}
	
	
	////
	public static void writePart(NodeSetDivGreedy root_set, String part_file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));
		
		Queue<NodeSetDivGreedy> queue_set = new LinkedList<NodeSetDivGreedy>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetDivGreedy R = queue_set.remove();
			
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
	
	
	////dynamic programming: opt(R) = max{mod(R), opt(R.left) + opt(R.right)}
	public static List<NodeSetDivGreedy> bestCut(NodeSetDivGreedy root_set, int m){
		
		List<NodeSetDivGreedy> ret = new ArrayList<NodeSetDivGreedy>();
		Map<Integer, CutNode> sol = new HashMap<Integer, CutNode>();	// best solution node.id --> CutNode info
		
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		Stack<NodeSetDivGreedy> stack = new Stack<NodeSetDivGreedy>();
		
		// fill stack using queue
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			stack.push(R);
			if (R.left != null){
				queue.add(R.left);
				queue.add(R.right);
			}
		}
		
		// 
		while (stack.size() > 0){
			NodeSetDivGreedy R = stack.pop();
			
			double mod = R.modularitySelf(m);			// non-private, need modularitySelfDP() !
			boolean self = true;
			if (R.left == null){	// leaf nodes
				sol.put(R.id, new CutNode(mod, true));
			}else{
				//
				double mod_opt = sol.get(R.left.id).mod + sol.get(R.right.id).mod;
				if (mod < mod_opt){
					mod = mod_opt;
					self = false;
				}
					
				sol.put(R.id, new CutNode(mod, self));
			}
		}
		
		System.out.println("sol.size = " + sol.size());
		System.out.println("best modularity = " + sol.get(-1).mod);
		
		// compute ret
		queue = new LinkedList<NodeSetDivGreedy>();
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			
			if (sol.get(R.id).self == true){
				ret.add(R);
				System.out.print(R.id + " ");
			}else if (R.left != null){
				queue.add(R.left);
				queue.add(R.right);
			}
		}
		
		//
		return ret;
	}
	
	////
	public static void writeBestCut(List<NodeSetDivGreedy> best_cut, String part_file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));

		for (NodeSetDivGreedy R : best_cut){
//				for (int i = 0; i < R.k; i++){
//					for (int s = 0; s < R.part.length; s++)
//						if (R.part[s] == i)
//							bw.write(R.ind2node[s] + ",");
//					bw.write("\n");
//				}
			
			for (int s = 0; s < R.ind2node.length; s++)
				bw.write(R.ind2node[s] + ",");
			bw.write("\n");
		}
		
		bw.close();
	}

	////
	public static void writeTree(NodeSetDivGreedy root_set, String tree_file, int m) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(tree_file));
		
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			bw.write(R.id + ":" + R.modularitySelf(m) + ";");
			
			if (R.left != null){
				bw.write(R.left.id + "," + R.right.id);
				bw.write("\n");
				
				queue.add(R.left);
				queue.add(R.right);
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
	public static NodeSetDivGreedy readTree(String tree_file) throws IOException{
		Map<Integer, NodeSetDivGreedy> map = new HashMap<Integer, NodeSetDivGreedy>();
		
		BufferedReader br = new BufferedReader(new FileReader(tree_file));
		//
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	int id = Integer.parseInt(str.substring(0, str.indexOf(":")));
        	double modSelf = Double.parseDouble(str.substring(str.indexOf(":") + 1,str.indexOf(";")));
        	
        	NodeSetDivGreedy node = new NodeSetDivGreedy();	
        	node.id = id;
        	node.modSelf = modSelf;
        	
        	map.put(node.id, node);
        	//
        	String val = str.substring(str.indexOf(";") + 1);
        	String[] items = val.split(",");
        	int[] values = new int[items.length];
        	for (int i = 0; i < items.length; i++)
        		values[i] = Integer.parseInt(items[i]);

        	if (values[0] >= 0){ // leaf node sets
//	        		System.out.println("LEAF node.id = " + node.id);
        		
        		node.ind2node = new int[values.length];
//	        		System.out.println("node.ind2node.length = " + node.ind2node.length);
        		System.arraycopy(values, 0, node.ind2node, 0, values.length);
        	}
		}
		br.close();
		
		// read again to build tree (compute node.children)
		br = new BufferedReader(new FileReader(tree_file));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	int id = Integer.parseInt(str.substring(0, str.indexOf(":")));
        	
        	String val = str.substring(str.indexOf(";") + 1);
        	String[] items = val.split(",");
        	int[] values = new int[items.length];
        	for (int i = 0; i < items.length; i++)
        		values[i] = Integer.parseInt(items[i]);
        	
        	
        	if (values[0] < 0){ // child node ids
        		NodeSetDivGreedy cur_node = map.get(id);
        		
    			cur_node.left = map.get(values[0]);
    			cur_node.right = map.get(values[0]);
        		cur_node.left.level = cur_node.level + 1;
        		cur_node.right.level = cur_node.level + 1;
        	}
		}
		
		br.close();
		
		// compute node.ind2node
		NodeSetDivGreedy root_set = map.get(-1);
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		Stack<NodeSetDivGreedy> stack = new Stack<NodeSetDivGreedy>();
		
		// fill stack using queue
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			stack.push(R);
			if (R.left != null){
				queue.add(R.left);
				queue.add(R.right);
			}
		}
		
		// 
		while (stack.size() > 0){
			NodeSetDivGreedy R = stack.pop();
			
			if (R.ind2node == null){	// not leaf
				int len = R.left.ind2node.length + R.right.ind2node.length;
				
				R.ind2node = new int[len];
				int count = 0;
				for (int u : R.left.ind2node)
					R.ind2node[count++] = u;
				for (int u : R.right.ind2node)
					R.ind2node[count++] = u;
			}
				
		}
		
		//
		return root_set;
	}
	
	
	////dynamic programming: opt(R) = max{mod(R), opt(R.left) + opt(R.right)}
	public static List<NodeSetDivGreedy> bestCutOffline(NodeSetDivGreedy root_set){
		
		List<NodeSetDivGreedy> ret = new ArrayList<NodeSetDivGreedy>();
		Map<Integer, CutNode> sol = new HashMap<Integer, CutNode>();	// best solution node.id --> CutNode info
		
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		Stack<NodeSetDivGreedy> stack = new Stack<NodeSetDivGreedy>();
		
		// fill stack using queue
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			stack.push(R);
			if (R.left != null){
				queue.add(R.left);
				queue.add(R.right);
			}
		}
		
		// 
		while (stack.size() > 0){
			NodeSetDivGreedy R = stack.pop();
			
			double mod = R.modSelf;			// non-private, need modularitySelfDP() !
			boolean self = true;
			if (R.left == null){	// leaf nodes
				sol.put(R.id, new CutNode(mod, true));
			}else{
				//
				double mod_opt = sol.get(R.left.id).mod + sol.get(R.right.id).mod;
				if (mod < mod_opt){
					mod = mod_opt;
					self = false;
				}
					
				sol.put(R.id, new CutNode(mod, self));
			}
		}
		
		System.out.println("sol.size = " + sol.size());
		System.out.println("best modularity = " + sol.get(-1).mod);
		
		// compute ret
		queue = new LinkedList<NodeSetDivGreedy>();
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			
			if (sol.get(R.id).self == true){
				ret.add(R);
				System.out.print(R.id + " ");
			}else if (R.left != null){
				queue.add(R.left);
				queue.add(R.right);
			}
		}
		System.out.println();
		
		//
		return ret;
	}
}
