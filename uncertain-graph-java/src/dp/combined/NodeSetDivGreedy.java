/*
 * Apr 13, 2015
 * 	- copied from NodeSet, try to speedup add(), remove()
 *  - add reverse_add(), reverse_remove()
 * Sep 3
 * 	- add param binaryPart to recursiveLK() (used in dp.combined.*) 
 * 	- add param lower_size to partitionLK() (used in dp.combined.*) 
 * Sep 17
 * 	- copied from NodeSetDiv
 */

package dp.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.mcmc.Dendrogram;
import dp.mcmc.Node;
import grph.Grph;
import grph.VertexPair;
import toools.set.BitVectorSet;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class NodeSetDivGreedy {

	public static final int TYPE_LOG_LK = 0;
	public static final int TYPE_MINCUT = 1;
	//
	public IntSet S;
	public IntSet T;
	public int e_st;		//
	public int e_s;
	public int e_t;
	public int n_s = 0;
	public int n_t = 0;
	
	public NodeSetDivGreedy parent, left, right;		// for recursive partitioning
	public int id;
	public int level = 0;
	
	//// init two sets S and T from N
	public static void initSets(IntSet N, IntSet S, IntSet T){
		int[] arrN = N.toIntArray();
		for (int i = 0; i < arrN.length/2; i++)
			S.add(arrN[i]);
		for (int i = arrN.length/2; i < arrN.length; i++)
			T.add(arrN[i]); 
	}
	
	////
	// n_nodes: 0->n_nodes-1 are node ids in graph
	public NodeSetDivGreedy(Grph G, IntSet A){
		if (A.size() == 1){
			this.id = A.toIntArray()[0];
			return;
		}
		
//		int n_nodes = G.getNumberOfVertices();
		
		this.S = new IntHashSet();
		this.T = new IntHashSet();
		//
//		for (int i = 0; i < n_nodes/2; i++)
//			this.S.add(i);
//		for (int i = n_nodes/2; i < n_nodes; i++)
//			this.T.add(i);
		
		// call initSets
		initSets(A, this.S, this.T);
		
		//
		this.n_s = this.S.size();
		this.n_t = this.T.size();
		
		//
		int n = G.getNumberOfVertices();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		for (IntCursor u : this.S)
			node2Set[u.value] = 1;
		
		for (IntCursor u : this.T)
			node2Set[u.value] = 2;
		
		// e_st, e_s, e_t
		this.e_st = 0;
		this.e_s = 0;
		this.e_t = 0;
		
		int u; 
		int v;
		for (VertexPair p : G.getEdgePairs()){
			u = p.first;
			v = p.second;
			if (node2Set[u] + node2Set[v] == 3)	// avoid node2Set[u] == 0
				this.e_st += 1;
			if (node2Set[u] == 1 && node2Set[v] == 1)
				this.e_s += 1;
			if (node2Set[u] == 2 && node2Set[v] == 2)
				this.e_t += 1;
		}
	}
	
	////
	public NodeSetDivGreedy(Grph G){
		int n_nodes = G.getNumberOfVertices();
		
		this.S = new IntHashSet();
		this.T = new IntHashSet();
		//
		for (int i = 0; i < n_nodes/2; i++)
			this.S.add(i);
		for (int i = n_nodes/2; i < n_nodes; i++)
			this.T.add(i);
		
		//
		this.n_s = this.S.size();
		this.n_t = this.T.size();
		
		//
		int n = G.getNumberOfVertices();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		for (IntCursor u : this.S)
			node2Set[u.value] = 1;
		
		for (IntCursor u : this.T)
			node2Set[u.value] = 2;
		
		// e_st, e_s, e_t
		this.e_st = 0;
		this.e_s = 0;
		this.e_t = 0;
		
		int u; 
		int v;
		for (VertexPair p : G.getEdgePairs()){
			u = p.first;
			v = p.second;
			if (node2Set[u] + node2Set[v] == 3)	// avoid node2Set[u] == 0
				this.e_st += 1;
			if (node2Set[u] == 1 && node2Set[v] == 1)
				this.e_s += 1;
			if (node2Set[u] == 2 && node2Set[v] == 2)
				this.e_t += 1;
		}
	}
	
	//// move 1 item u from T to S
	public void add(int u, Grph G){
		
		//
		int count_add = 0;
		int count_remove = 0;
		int[] N = G.getNeighbours(u).toIntArray();
		for (int v : N){
			if (S.contains(v))
				count_remove += 1;
			if (T.contains(v))
				count_add += 1;
		}
		
		this.e_st = this.e_st - count_remove + count_add;
		this.e_s = this.e_s + count_remove;
		this.e_t = this.e_t - count_add;
		
		//
		this.S.add(u);
		this.T.remove(u);
		// update cut, size_S, size_T
		this.n_s += 1;
		this.n_t -= 1;
		
		//
		
	}
	
	//// move 1 item u from S to T
	public void remove(int u, Grph G){
		
		//
		int count_add = 0;
		int count_remove = 0;
		int[] N = G.getNeighbours(u).toIntArray();
		for (int v : N){
			if (S.contains(v))
				count_add += 1;
			if (T.contains(v))
				count_remove += 1;
		}
		
		this.e_st = this.e_st - count_remove + count_add;
		this.e_s = this.e_s - count_add;
		this.e_t = this.e_t + count_remove;
		
		//
		this.S.remove(u);
		this.T.add(u);
		// update cut, size_S, size_T
		this.n_s -= 1;
		this.n_t += 1;
		
		//
		
	}
	
	////move 1 item u from S back to T
	public void reverse_add(int u, Grph G, int old_st, int old_s, int old_t){
		this.e_st = old_st;
		this.e_s = old_s;
		this.e_t = old_t;
		
		this.S.remove(u);
		this.T.add(u);
		// update cut, size_S, size_T
		this.n_s -= 1;
		this.n_t += 1;
	}
	
	////move 1 item u from T back to S
	public void reverse_remove(int u, Grph G, int old_st, int old_s, int old_t){
		this.e_st = old_st;
		this.e_s = old_s;
		this.e_t = old_t;
		
		//
		this.S.add(u);
		this.T.remove(u);
		// update cut, size_S, size_T
		this.n_s += 1;
		this.n_t -= 1;
		
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
		for (IntCursor s : this.S)
			System.out.print(s.value + " ");
		System.out.println();
		
		System.out.print("T : ");
		for (IntCursor t : this.T)
			System.out.print(t.value + " ");
		System.out.println();
	}
	
	//// LOG-LIKELIHOOD partition, using logLK()
	public static void partitionLK(NodeSetDivGreedy R, Grph G, double eps_p, int n_steps, int n_samples, int sample_freq, boolean print_out, int lower_size){
		if (print_out)
			System.out.println("Node.partitionLK called");
		
//		int n_nodes = G.getNumberOfVertices();
		int n_nodes = R.S.size() + R.T.size();
		
		// compute dU
		long nMax = 0;
	    if (n_nodes % 2 == 0) 
	        nMax = n_nodes*n_nodes/4;
	    else 
	        nMax = (n_nodes*n_nodes-1)/4; 
	    double dU = Math.log(nMax) + (nMax-1)*Math.log(1+1.0/(nMax-1));
		
//		if (print_out)
			System.out.println("#steps = " + (n_steps + n_samples * sample_freq));

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
			if (R.S.size() < n_nodes/2 && R.S.size() > lower_size){	// add or remove
				int rand_val = random.nextInt(2);
				if (rand_val == 0)
					is_add = true;
				else
					is_add = false;	
			}else if (R.S.size() <= lower_size){			// only add
				is_add = true;
			}else{								// only remove (R.S.size() >= n_nodes/2)
				is_add = false;
			}
			
			// perform add or remove
			if (is_add){
				// randomly pick an item from T
				u = R.T.pickRandomElement(random);
				R.add(u, G);
				
			}else{
				// randomly pick an item from S
				u = R.S.pickRandomElement(random);
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
	public static int binaryPartition(Grph G, NodeSetDivGreedy root, int id){
		
		int cur_id = id;
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			
			if (R.S.size() >= 2){
				NodeSetDivGreedy RS = new NodeSetDivGreedy(G, R.S);
				RS.id = cur_id--;
				R.left = RS;
				RS.parent = R;
				queue.add(RS);
			}else{
				NodeSetDivGreedy RS = new NodeSetDivGreedy(G, R.S);		// RS.id is the remaining item in S
				R.left = RS;
				RS.parent = R;
			}
			
			if (R.T.size() >= 2){
				NodeSetDivGreedy RT = new NodeSetDivGreedy(G, R.T);
				RT.id = cur_id--;
				R.right = RT;
				RT.parent = R;
				queue.add(RT);
			}else{
				NodeSetDivGreedy RT = new NodeSetDivGreedy(G, R.T);		// RT.id is the remaining item in T
				R.right = RT;
				RT.parent = R;
			}
				
		}
		
		//
		return cur_id;
	}
	
	//////////////////////////////
	// limit_size = 32: i.e. for NodeSet having size <= limit_size, call 
	public static NodeSetDivGreedy recursiveLK(Grph G, double eps1, int burn_factor, int limit_size, int lower_size, int max_level){
		int n_nodes = G.getNumberOfVertices();
		int id = -1;
		
		IntSet A = new IntHashSet();
		for (int i = 0; i < n_nodes; i++)
			A.add(i);
		
		// root node
		NodeSetDivGreedy root = new NodeSetDivGreedy(G, A);
		root.id = id--;
		root.level = 0;
		// 
		Queue<NodeSetDivGreedy> queue = new LinkedList<NodeSetDivGreedy>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetDivGreedy R = queue.remove();
			System.out.println("R.level = " + R.level + " R.S.size() + R.T.size() = " + (R.S.size() + R.T.size()));
			
			// USE limit_size
			if (R.S.size() + R.T.size() <= limit_size || R.level == max_level)		// changed: < to <=
				continue;
			
			long start = System.currentTimeMillis();
			NodeSetDivGreedy.partitionLK(R, G, eps1/max_level, burn_factor*(R.S.size() + R.T.size()), 0, 0, false, lower_size);
			System.out.println("elapsed " + (System.currentTimeMillis() - start));
			
			
			NodeSetDivGreedy RS = new NodeSetDivGreedy(G, R.S);
			RS.id = id--;
			R.left = RS;
			RS.parent = R;
			RS.level = R.level + 1;
			
			NodeSetDivGreedy RT = new NodeSetDivGreedy(G, R.T);
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
	public static void printSetIds(NodeSetDivGreedy root_set){
		System.out.println("printSetIds");
		
		Queue<NodeSetDivGreedy> queue_set = new LinkedList<NodeSetDivGreedy>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetDivGreedy R = queue_set.remove();
			if (R.left != null)
				System.out.println("R.id = " + R.id + " left.id = " + R.left.id + " right.id = " + R.right.id + 
						" left.size = " + R.S.size() + " right.size = " + R.T.size());
			else
				System.out.println("LEAF R.id = " + R.id);
			if (R.left != null){
				queue_set.add(R.left);
				queue_set.add(R.right);
			}
		}
	}
	
	////
	public static Dendrogram convertToHRG(Grph G, NodeSetDivGreedy root_set){
		int n_nodes = G.getNumberOfVertices();
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
				if (R.S != null)
					for (IntCursor t : R.S)
						bw.write(t.value + ",");
				if (R.T != null)
					for (IntCursor t : R.T)
						bw.write(t.value + ",");
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
