/*
 * Sep 18, 2015
 * 	- copied from NodeSetDiv, exponential mechanism with modularity Q
 * Sep 21
 * 	- faster constructors NodeSetMod(Grph G, IntSet A), NodeSetMod(Grph G)
 * 	- fix error "this.d_t = this.T.size()" in constructors 
 * 	- getRandomItem(): use fast InthashSet.pickRandomElement(random)
 * Sep 22
 * 	- add writePart(), readPart()
 * 	- fix recursiveMod(): correct order for checking limit_size, lower_size
 * 	- use epsilonByLevel() in recursiveMod()
 */

package dp.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.DPUtil;
import dp.mcmc.Dendrogram;
import dp.mcmc.Node;
import grph.Grph;
import grph.VertexPair;
import toools.set.BitVectorSet;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class NodeSetMod {

	//
	public IntSet S;
	public IntSet T;
	public int e_st;
	public int e_s;			// number of edges inside S
	public int e_t;			// number of edges inside T
	public int d_s = 0; 	// total degree of nodes in S
	public int d_t = 0;		// total degree of nodes in T
	
	public NodeSetMod parent, left, right;		// for recursive partitioning
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
	
	//// SLOW constructors ! complexity O(n^2)
	// n_nodes: 0->n_nodes-1 are node ids in graph
//	public NodeSetMod(Grph G, IntSet A){
//		if (A.size() == 1){
//			this.id = A.toIntArray()[0];
//			return;
//		}
//		
//		this.S = new IntHashSet();
//		this.T = new IntHashSet();
//
//		// call initSets
//		initSets(A, this.S, this.T);
//		
//		// d_s, d_t
//		this.d_s = 0;
//		for (IntCursor u : this.S)
//			this.d_s += G.getVertexDegree(u.value);
//		
//		this.d_t = 0;
//		for (IntCursor u : this.T)
//			this.d_t += G.getVertexDegree(u.value);
//		
//		// e_st
//		this.e_st = 0;
//		for (IntCursor s : this.S){
//			IntSet N = G.getNeighbours(s.value);
//			for (IntCursor t : this.T)
//				if (N.contains(t.value))
//					this.e_st ++;
//		}
//				
//		// e_s
//		this.e_s = 0;
//		int[] arrS = this.S.toIntArray();
//		for (int i = 0; i < arrS.length; i++){
//			IntSet N = G.getNeighbours(arrS[i]);
//			for (int j = i+1; j < arrS.length; j++)
//				if (N.contains(arrS[j]))
//					this.e_s ++;
//		}
//		// e_t
//		this.e_t = 0;
//		int[] arrT = this.T.toIntArray();
//		for (int i = 0; i < arrT.length; i++){
//			IntSet N = G.getNeighbours(arrT[i]);
//			for (int j = i+1; j < arrT.length; j++)
//				if (N.contains(arrT[j]))
//					this.e_t ++;
//		}
//		
//		
//	}
//	
//	////
//	public NodeSetMod(Grph G){
//		int n_nodes = G.getNumberOfVertices();
//		
//		this.S = new IntHashSet();
//		this.T = new IntHashSet();
//		//
//		for (int i = 0; i < n_nodes/2; i++)
//			this.S.add(i);
//		for (int i = n_nodes/2; i < n_nodes; i++)
//			this.T.add(i);
//		
//		// d_s, d_t
//		this.d_s = 0;
//		for (IntCursor u : this.S)
//			this.d_s += G.getVertexDegree(u.value);
//		
//		this.d_t = 0;
//		for (IntCursor u : this.T)
//			this.d_t += G.getVertexDegree(u.value);
//		
//		// e_st
//		this.e_st = 0;
//		for (IntCursor s : this.S){
//			IntSet N = G.getNeighbours(s.value);
//			for (IntCursor t : this.T)
//				if (N.contains(t.value))
//					this.e_st ++;
//		}
//		
//		// e_s
//		this.e_s = 0;
//		int[] arrS = this.S.toIntArray();
//		for (int i = 0; i < arrS.length; i++){
//			IntSet N = G.getNeighbours(arrS[i]);
//			for (int j = i+1; j < arrS.length; j++)
//				if (N.contains(arrS[j]))
//					this.e_s ++;
//		}
//		// e_t
//		this.e_t = 0;
//		int[] arrT = this.T.toIntArray();
//		for (int i = 0; i < arrT.length; i++){
//			IntSet N = G.getNeighbours(arrT[i]);
//			for (int j = i+1; j < arrT.length; j++)
//				if (N.contains(arrT[j]))
//					this.e_t ++;
//		}
//	}
	
	////FASTER constructors ! complexity O(m)
	// n_nodes: 0->n_nodes-1 are node ids in graph
	public NodeSetMod(Grph G, IntSet A){
		if (A.size() == 1){
			this.id = A.toIntArray()[0];
			return;
		}
		
		this.S = new IntHashSet();
		this.T = new IntHashSet();

		// call initSets
		initSets(A, this.S, this.T);
		
		//
		int n = G.getNumberOfVertices();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		// d_s, d_t
		this.d_s = 0;
		for (IntCursor u : this.S){
			this.d_s += G.getVertexDegree(u.value);
			node2Set[u.value] = 1;
		}
		
		this.d_t = 0;
		for (IntCursor u : this.T){
			this.d_t += G.getVertexDegree(u.value);
			node2Set[u.value] = 2;
		}
		
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
	public NodeSetMod(Grph G){
		int n_nodes = G.getNumberOfVertices();
		
		this.S = new IntHashSet();
		this.T = new IntHashSet();
		//
		for (int i = 0; i < n_nodes/2; i++)
			this.S.add(i);
		for (int i = n_nodes/2; i < n_nodes; i++)
			this.T.add(i);
		
		//
		int n = G.getNumberOfVertices();
		int[] node2Set = new int[n]; // 1:S, 2:T
		
		// d_s, d_t
		this.d_s = 0;
		for (IntCursor u : this.S){
			this.d_s += G.getVertexDegree(u.value);
			node2Set[u.value] = 1;
		}
		
		this.d_t = 0;
		for (IntCursor u : this.T){
			this.d_t += G.getVertexDegree(u.value);
			node2Set[u.value] = 2;
		}
		
		// e_st, e_s, e_t
		this.e_st = 0;
		this.e_s = 0;
		this.e_t = 0;
		
		int u; 
		int v;
		for (VertexPair p : G.getEdgePairs()){
			u = p.first;
			v = p.second;
			if (node2Set[u] + node2Set[v] == 3)	//  
				this.e_st += 1;
			if (node2Set[u] == 1 && node2Set[v] == 1)
				this.e_s += 1;
			if (node2Set[u] == 2 && node2Set[v] == 2)
				this.e_t += 1;
		}
		
		// debug
		System.out.println("NodeSetMod called: e_st = " + this.e_st + " e_s = " + this.e_s + " d_s = " + this.d_s + " e_t = " + this.e_t + " d_t = " + this.d_t);
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
		
		// 
		int deg_u = G.getVertexDegree(u);
		this.d_s += deg_u;
		this.d_t -= deg_u;
		
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
		
		// 
		int deg_u = G.getVertexDegree(u);
		this.d_s -= deg_u;
		this.d_t += deg_u;
		
		//
		
	}
	
	////move 1 item u from S back to T
	public void reverse_add(int u, Grph G, int old_st, int old_s, int old_t){
		int deg_u = G.getVertexDegree(u);
		this.e_st = old_st;
		this.e_s = old_s;
		this.e_t = old_t;
		
		this.S.remove(u);
		this.T.add(u);
		// 
		this.d_s -= deg_u;
		this.d_t += deg_u;
	}
	
	////move 1 item u from T back to S
	public void reverse_remove(int u, Grph G, int old_st, int old_s, int old_t){
		int deg_u = G.getVertexDegree(u);
		this.e_st = old_st;
		this.e_s = old_s;
		this.e_t = old_t;
		
		//
		this.S.add(u);
		this.T.remove(u);
		// 
		this.d_s += deg_u;
		this.d_t -= deg_u;
		
	}
	
	
	//// m : number of edges in G
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
		NodeSetMod root_set = this;
		while (root_set.parent != null)
			root_set = root_set.parent;
		
		double mod = 0.0;
		
		Queue<NodeSetMod> queue_set = new LinkedList<NodeSetMod>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetMod R = queue_set.remove();
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
	
	////
	public static int getRandomItem(IntSet T, Random random){
		int u = -1;
		// WAY-1
//		int id = random.nextInt(T.size());
//		for (IntCursor t: T){
//			if (id == 0){
//				u = t.value;
//				break;
//			}else
//				id = id - 1;
//		}
		// WAY-2
		u = T.pickRandomElement(random);
		//
		return u;
	}
	
	//// MODULARITY partition, using modularity()
	public static void partitionMod(NodeSetMod R, Grph G, double eps_p, int n_steps, int n_samples, int sample_freq, boolean print_out, int lower_size){
		if (print_out)
			System.out.println("NodeSetMod.partitionMod called");
		
//		int n_nodes = G.getNumberOfVertices();
		int n_nodes = R.S.size() + R.T.size();
		int n_edges = G.getNumberOfEdges();
		
		// compute dU
	    double dU = 8.0/n_edges;
		
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
		
		double modT = R.modularity(n_edges);
		double modT2;
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
				u = getRandomItem(R.T, random);
				R.add(u, G);
				
			}else{
				// randomly pick an item from S
				u = getRandomItem(R.S, random);
				R.remove(u, G);
			}
			
			// MCMC
			modT2 = R.modularity(n_edges);
			
			if (modT2 > modT){
				n_accept += 1;
				n_accept_positive += 1;
				modT = modT2;
				//
				old_st = R.e_st;
				old_s = R.e_s;
				old_t = R.e_t;
			}else{
				double prob = Math.exp(eps_p/(2*dU)*(modT2 - modT));			// prob ~ 1.0
				// System.out.println("prob = " + prob);
				double prob_val = random.nextDouble();
				if (prob_val > prob){
					// reverse
					if (is_add)
						R.reverse_add(u, G, old_st, old_s, old_t);
					else
						R.reverse_remove(u, G, old_st, old_s, old_t);
				}else {
					n_accept += 1;
					modT = modT2;
					//
					old_st = R.e_st;
					old_s = R.e_s;
					old_t = R.e_t;
				}
			}
			
			if (i % out_freq == 0 && print_out)
				System.out.println("i = " + i + " n_accept = " + n_accept + " mod = " + R.modularityAll(n_edges)
						+ " n_accept_positive = " + n_accept_positive
						+ " time : " + (System.currentTimeMillis() - start));
		}
		
	}
	
	
	//////////////////////////////
	// limit_size = 32: i.e. for NodeSet having size <= limit_size, call 
	public static NodeSetMod recursiveMod(Grph G, double eps1, int burn_factor, int limit_size, int lower_size, int max_level, double ratio){
		int n_nodes = G.getNumberOfVertices();
		int n_edges = G.getNumberOfEdges();
		int id = -1;
		
		//
		double[] epsArr = DPUtil.epsilonByLevel(eps1, max_level, ratio); 
		
		//
		IntSet A = new IntHashSet();
		for (int i = 0; i < n_nodes; i++)
			A.add(i);
		
		// root node
		NodeSetMod root = new NodeSetMod(G, A);
		root.id = id--;
		root.level = 0;
		// 
		Queue<NodeSetMod> queue = new LinkedList<NodeSetMod>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetMod R = queue.remove();
			System.out.println("R.level = " + R.level + " R.S.size() + R.T.size() = " + (R.S.size() + R.T.size()));
			
			// USE limit_size
			boolean check_mod = false;
			if (R.parent != null)
				if (R.parent.modularity(n_edges) > R.parent.left.modularity(n_edges) + R.parent.right.modularity(n_edges))
					check_mod = true;
			
//			if (R.level == max_level || check_mod){
			if (R.S.size() + R.T.size() <= limit_size || R.level == max_level){
//			if (R.S.size() + R.T.size() <= limit_size || R.level == max_level || R.modularity(n_edges) < R.modularitySelf(n_edges)){	
//			if (R.S.size() + R.T.size() <= limit_size || R.modularity(n_edges) < R.modularitySelf(n_edges)){
				// stop dividing R
				continue;
			}
			
			long start = System.currentTimeMillis();
			NodeSetMod.partitionMod(R, G, epsArr[R.level], burn_factor*(R.S.size() + R.T.size()), 0, 0, false, lower_size);
			System.out.println("elapsed " + (System.currentTimeMillis() - start));
			
			NodeSetMod RS = new NodeSetMod(G, R.S);
			RS.id = id--;
			R.left = RS;
			RS.parent = R;
			RS.level = R.level + 1;
			
			NodeSetMod RT = new NodeSetMod(G, R.T);
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
	public static void printSetIds(NodeSetMod root_set, int m){
		System.out.println("printSetIds");
		
		Queue<NodeSetMod> queue_set = new LinkedList<NodeSetMod>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetMod R = queue_set.remove();
			if (R.left != null){
//				System.out.println("R.id = " + R.id + " left.id = " + R.left.id + " right.id = " + R.right.id + 
//						" left.size = " + R.S.size() + " right.size = " + R.T.size() + " mod = " + R.modularity(m) + " modSelf = " + R.modularitySelf(m));
				System.out.println("R.id = " + R.id + " left.id = " + R.left.id + " right.id = " + R.right.id + 
					" left.size = " + R.S.size() + " right.size = " + R.T.size() + " (" + R.e_st + "," + R.e_s + "," + R.e_t + "," + R.d_s + "," + R.d_t + "," + m + ")" + 
					" mod = " + R.modularity(m) + " modSelf = " + R.modularitySelf(m));
			}else{
				System.out.println("LEAF R.id = " + R.id + " modSelf = " + R.modularitySelf(m)); // + " left.size = " + R.S.size() + " right.size = " + R.T.size());
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
	public static void writePart(NodeSetMod root_set, String part_file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));
		
		Queue<NodeSetMod> queue_set = new LinkedList<NodeSetMod>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetMod R = queue_set.remove();
			
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
