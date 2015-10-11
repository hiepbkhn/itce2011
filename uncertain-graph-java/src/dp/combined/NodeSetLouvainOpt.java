/*
 * Sep 30, 2015
 * 	- non-private, k-ary tree, copied from NodeSetLouvain.java
 * 	- maximizeLouvain(): move node to the best group 
 * Oct 11
 * 	- add writeTree(), readTree(), bestCutOffline()
 */

package dp.combined;

import hist.Int2;
import hist.Int2;

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


public class NodeSetLouvainOpt {

	public int k = 5;	// 	fan-out
	//
	public int[] part;	//	part[i] = 0 -> K-1 is the partition of the node i 
	public int[] size;	//	number of nodes in partition k
	public int[] lc;	// 	number of intra-edges
	public int[] dc;	// 	
	//
	public List<Int2> e_list;	
	public int[] ind2node;	// 
	public Map<Integer, Integer> node2ind;
	//
	public NodeSetLouvainOpt[] children;
	public NodeSetLouvainOpt parent;
	public int id;
	public int level = 0;
	public double modSelf = 0.0;	// see writeTree, readTree
	
	//// for the case number of parts < k
	public int normalizePart(){
		boolean[] mark = new boolean[this.k];
		for (int i = 0; i < this.k; i++)
			mark[i] = false;
		
		for (int p : this.part)
			mark[p] = true;
		
		int[] map = new int[this.k];
		int count = 0;
		for (int i = 0; i < this.k; i++){
			map[i] = count;
			if (mark[i] == true)
				count += 1;
		}
		
		//
		for (int i = 0; i < this.part.length; i++)
			this.part[i] = map[this.part[i]];
		
		//
		return count;
	}
	
	////
	public static void getSubEgdeLists(NodeSetLouvainOpt R, int k, List<List<Int2>> e_list){
		for (int i = 0; i < k; i++)
			e_list.add(new ArrayList<Int2>());
		
		int u = 0;
		int v = 0;
		for (Int2 e : R.e_list){
			u = R.node2ind.get(e.val0);
			v = R.node2ind.get(e.val1);
			if (R.part[u] == R.part[v])
				e_list.get(R.part[u]).add(e);
		}
	}
	
	//// build child node RC (part = val)
	public static void getSubSet(EdgeWeightedGraph G, NodeSetLouvainOpt R, NodeSetLouvainOpt RC, int val, int k){
		int count = 0;
		RC.node2ind = new HashMap<Integer, Integer>();
		for (int i = 0; i < R.part.length; i++)
			if (R.part[i] == val){
				RC.node2ind.put(R.ind2node[i], count);
				count ++;
			}
		
		RC.part = new int[count];
		RC.ind2node = new int[count];
		count = 0;
		for (int i = 0; i < R.part.length; i++)
			if (R.part[i] == val)
				RC.ind2node[count++] = R.ind2node[i];
		
		//
		int n = RC.part.length;
		
		RC.part = new int[n];
		RC.size = new int[k];
		RC.lc = new int[k];
		RC.dc = new int[k];
		//
		for (int i = 0; i < k-1; i++){
			for (int j = 0; j < n/k; j++)
				RC.part[i*(n/k) + j] = i;
			RC.size[i] = n/k;
		}

		int m = n - (k-1)*(n/k);
		for (int j = 0; j < m; j++)
			RC.part[(k-1)*(n/k) + j] = k-1;
		RC.size[k-1] = m;
		
		// lc, dc
		int u_id; 
		int v_id;
		for (Int2 e : RC.e_list){
			u_id = RC.node2ind.get(e.val0);
			v_id = RC.node2ind.get(e.val1);
			if (RC.part[u_id] == RC.part[v_id])
				RC.lc[RC.part[u_id]] += 1;
		}
		
		for (int u : RC.ind2node){
			u_id = RC.node2ind.get(u);
			
			RC.dc[RC.part[u_id]] += G.degree(u);
			
		}
		
	}
	
	////
	public NodeSetLouvainOpt(int k){
		this.k = k;
		this.children = new NodeSetLouvainOpt[this.k];
	}
	
	////
	public NodeSetLouvainOpt(EdgeWeightedGraph G, int k){
		int n = G.V();
		
		// ind2node, node2ind
		this.ind2node = new int[n];
		this.node2ind = new HashMap<Integer, Integer>();
		
		//
		for (int i = 0; i < n; i++){
			this.ind2node[i] = i;
			this.node2ind.put(i, i);
		}
		
		//
		this.k = k;
		this.part = new int[n];
		this.size = new int[k];
		this.lc = new int[k];
		this.dc = new int[k];
		//
		for (int i = 0; i < k-1; i++){
			for (int j = 0; j < n/k; j++)
				this.part[i*(n/k) + j] = i;
			this.size[i] = n/k;
		}

		int m = n - (k-1)*(n/k);
		for (int j = 0; j < m; j++)
			this.part[(k-1)*(n/k) + j] = k-1;
		this.size[k-1] = m;
		
		// lc, dc, e_list
		this.e_list = new ArrayList<Int2>();
		int u; 
		int v;
		for (Edge e : G.edges()){
			u = e.either();
			v = e.other(u);
			this.e_list.add(new Int2(u, v));
			
			if (this.part[u] == this.part[v])
				this.lc[this.part[u]] += 1;
			this.dc[this.part[u]] += 1;
			this.dc[this.part[v]] += 1;
		}
		
		// check sum(lc), sum(dc)
		int sum_dc = 0;
		for (int val : this.dc)
			sum_dc += val;
		System.out.println("sum_dc = " + sum_dc);
		
		
	}
	
	////m : number of edges in G
	public double modularity(int m){
		double mod = 0.0;
		
		for (int i = 0; i < lc.length; i++)
			mod += (double)this.lc[i]/m - (this.dc[i]/(2.0*m)) *(this.dc[i]/(2.0*m));
		//
		return mod;
	}
	
	////m : number of edges in G
	public double modularitySelf(int m){
		double mod = 0.0;
		double lc = this.e_list.size();
		double dc = 0;
		for (int val : this.dc)
			dc += val;
		
		mod = lc/m - (dc/(2.0*m))*(dc/(2.0*m));
		
		//
		return mod;
	}
	
	////m : number of edges in G
	public double modularityAll(int m){
		NodeSetLouvainOpt root_set = this;
		while (root_set.parent != null)
			root_set = root_set.parent;
		
		double mod = 0.0;
		
		Queue<NodeSetLouvainOpt> queue_set = new LinkedList<NodeSetLouvainOpt>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetLouvainOpt R = queue_set.remove();
			if (R.children[0] == null) // leaf
				mod += R.modularitySelf(m);
			else{
				for (int i = 0; i < R.children.length; i++)
					queue_set.add(R.children[i]);
			}
				
		}
		
		//
		return mod;
	}
	
	//// move node u (id) to group 'dest'
	public void move(int u_id, int dest, EdgeWeightedGraph G){
		
		int u = this.ind2node[u_id];
		
		int deg_u = G.degree(u);
		this.dc[part[u_id]] -= deg_u;
		this.dc[dest] += deg_u;
		
		for (int v : G.adj(u).keySet()){
			if (this.node2ind.containsKey(v)){
				int v_id = this.node2ind.get(v);
				if (this.part[v_id] == this.part[u_id])
					this.lc[part[u_id]] -= 1;
				if (this.part[v_id] == dest)
					this.lc[dest] += 1;
			}
		}
		
		//
		this.size[part[u_id]] -= 1;
		this.size[dest] += 1;
		
		this.part[u_id] = dest;
		
	}
	
	//// change of modularity if u_id is moved to dest
	public double modIncrement(int u_id, int dest, EdgeWeightedGraph G){
		double incr = 0.0;
		int u = this.ind2node[u_id];
		
		int deg_u = G.degree(u);	// k_i
		
		int lc_dest = 0;		// k_i,in
		
		for (int v : G.adj(u).keySet())
			if (this.node2ind.containsKey(v)){
				int v_id = this.node2ind.get(v);
				if (this.part[v_id] == dest)
					lc_dest += 1;
			}
		
		incr = lc_dest - this.dc[dest]*(double)deg_u/G.E();
		
		//
		return incr;
	}
	
	
	////
	public static void partitionLouvain(NodeSetLouvainOpt R, EdgeWeightedGraph G, int burn_factor){
		
		int n_nodes = R.part.length;
		int n_edges = G.E();
		int k = R.lc.length;
		
		//
	    Random random = new Random();
		int n_accept = 0;
		int n_accept_positive = 0;
		int u = -1;
		int dest = -1;
		int old_dest = -1;
		
		double modT = R.modularity(n_edges);
		double modT2;
		
		boolean modif = true;
		int nb_pass_done = 0;
		
		while (modif && nb_pass_done != burn_factor){
	        modif = false;
	        nb_pass_done += 1;
		
	        for (int i = 0; i < R.part.length; i++){
		        u = random.nextInt(n_nodes);
				dest = random.nextInt(k);
				while (dest == R.part[u])
					dest = random.nextInt(k);
				
				old_dest = R.part[u];
				R.move(u, dest, G);
				
				modT2 = R.modularity(n_edges);
				
				if (modT2 > modT){
					modif = true;
					modT = modT2;
					
					n_accept += 1;
					n_accept_positive += 1;
				}else{
					// reverse
					R.move(u, old_dest, G);
						
				}
	        }
			
		}
		//
		System.out.println("nb_pass_done < burn_factor : " + (nb_pass_done < burn_factor));
		
	}
	
	//// move each node to the best group (see Louvain.one_level())
	public static void maximizeLouvain(NodeSetLouvainOpt R, EdgeWeightedGraph G, int burn_factor){
		
		int n_nodes = R.part.length;
		int n_edges = G.E();
		int k = R.lc.length;
		
		//
		int u = -1;
		int dest = -1;
		
		boolean modif = true;
		int nb_pass_done = 0;
		
		while (modif && nb_pass_done != burn_factor){
	        modif = false;
	        nb_pass_done += 1;
		
	        for (int u_id = 0; u_id < R.part.length; u_id++){
	        	u = R.ind2node[u_id];
	        	
	        	int best_dest = R.part[u_id];
	        	double best_incr = 0.0;
	        	for (dest = 0; dest < k; dest++){
	        		double incr = R.modIncrement(u_id, dest, G);
	        		if (best_incr < incr){
	        			best_incr = incr;
	        			best_dest = dest;
	        		}
	        			
	        	}
	        	if (best_dest != R.part[u_id]){
	        		modif = true;
	        		R.move(u_id, best_dest, G);
	        	}
	        	
	        }
			
		}
		//
		System.out.println("nb_pass_done < burn_factor : " + (nb_pass_done < burn_factor));
		
	}
	
	////
	public static NodeSetLouvainOpt recursiveLouvain(EdgeWeightedGraph G, int burn_factor, int limit_size, int max_level, int k, int type){
		int n_nodes = G.V();
		int n_edges = G.E();
		int id = -1;
		
		// root node
		NodeSetLouvainOpt root = new NodeSetLouvainOpt(G, k);
		root.id = id--;
		root.level = 0;
		
		// 
		Queue<NodeSetLouvainOpt> queue = new LinkedList<NodeSetLouvainOpt>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
//			System.out.println("R.level = " + R.level + " R.size = " + R.part.length + " mod = " + String.format("%.4f", R.modularity(n_edges)) + 
//					" modSelf = " + String.format("%.4f", R.modularitySelf(n_edges)) ); 
////					" check mod = " + String.format("%.4f", CommunityMeasure.modularitySet(G, R.ind2node)) );
			
			if (R.part.length <= limit_size || R.level == max_level){
				continue;
			}
			
			long start = System.currentTimeMillis();
			if (type == 1)
				NodeSetLouvainOpt.partitionLouvain(R, G, burn_factor);
			if (type == 2)
				NodeSetLouvainOpt.maximizeLouvain(R, G, burn_factor);
			System.out.println("elapsed " + (System.currentTimeMillis() - start));
			
			int count = R.normalizePart();
			
			List<List<Int2>> e_list = new ArrayList<List<Int2>>();
			getSubEgdeLists(R, count, e_list);
			
			R.children = new NodeSetLouvainOpt[count];
			for (int i = 0; i < count; i++){
				NodeSetLouvainOpt RC = new NodeSetLouvainOpt(k);
				
				RC.e_list = e_list.get(i);
				getSubSet(G, R, RC, i, k);
				
				RC.id = id--;
				RC.level = R.level + 1;
				RC.parent = R;
				R.children[i] = RC;
				
				
				queue.add(RC);
			}
			
		}
		
		//
		return root;
	}
	
	////
	public static void printSetIds(NodeSetLouvainOpt root_set, int m){
		System.out.println("printSetIds");
		
		Queue<NodeSetLouvainOpt> queue_set = new LinkedList<NodeSetLouvainOpt>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetLouvainOpt R = queue_set.remove();
			if (R.children[0] != null){
//				System.out.print("{");
//				for (int i = 0; i < R.part.length; i++)
//					System.out.print(R.ind2node[i] + " ");
//				System.out.println("}");
				
				System.out.print("R.id = " + R.id + " mod = " + String.format("%.4f", R.modularity(m)) + 
						" modSelf = " + String.format("%.4f", R.modularitySelf(m)) + " children (id, size) = ");
				for (int i = 0; i < R.children.length; i++)
					System.out.print(" (" + R.children[i].id + "," + R.size[i] + ") mod = " + 
							String.format("%.4f", R.children[i].modularity(m)) );
				System.out.println();
			}else{
				System.out.println("LEAF R.id = " + R.id + " modSelf = " + String.format("%.4f", R.modularitySelf(m)) );
			}
			
			if (R.children[0] != null){
				for (int i = 0; i < R.children.length; i++)
					queue_set.add(R.children[i]);
			}
		}
	}
	
	////
	public void writePart(String part_file) throws IOException{

		List<List<Integer>> com = new ArrayList<List<Integer>>();
		int k = this.lc.length;
		for (int i = 0; i < k; i++)
			com.add(new ArrayList<Integer>());
		
		for (int i = 0; i < this.part.length; i++)
			com.get(this.part[i]).add(i);
		
		//
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));
		for (List<Integer> list:com){
			for (int u : list)
				bw.write(u + ",");
			bw.write("\n");
		}
		
		bw.close();
	}
	
	////dynamic programming: opt(R) = max{mod(R), opt(R.left) + opt(R.right)}
	public static List<NodeSetLouvainOpt> bestCut(NodeSetLouvainOpt root_set, int m){
		
		List<NodeSetLouvainOpt> ret = new ArrayList<NodeSetLouvainOpt>();
		Map<Integer, CutNode> sol = new HashMap<Integer, CutNode>();	// best solution node.id --> CutNode info
		
		Queue<NodeSetLouvainOpt> queue = new LinkedList<NodeSetLouvainOpt>();
		Stack<NodeSetLouvainOpt> stack = new Stack<NodeSetLouvainOpt>();
		
		// fill stack using queue
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
			stack.push(R);
			if (R.children[0] != null)
				for (int i = 0; i < R.children.length; i++)
					queue.add(R.children[i]);
		}
		
		// 
		while (stack.size() > 0){
			NodeSetLouvainOpt R = stack.pop();
			
			double mod = R.modularitySelf(m);			// non-private, need modularitySelfDP() !
			boolean self = true;
			if (R.children[0] == null){	// leaf nodes
				sol.put(R.id, new CutNode(mod, true));
				
			}else{
				//
				double mod_opt = 0.0;
				for (int i = 0; i < R.children.length; i++)
					mod_opt += sol.get(R.children[i].id).mod;
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
		queue = new LinkedList<NodeSetLouvainOpt>();
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
			
			if (sol.get(R.id).self == true){
				ret.add(R);
				System.out.print(R.id + " ");
			}else if (R.children[0] != null)
				for (int i = 0; i < R.children.length; i++)
					queue.add(R.children[i]);
		}
		System.out.println();
		
		//
		return ret;
	}
	
	////
	public static void writeBestCut(List<NodeSetLouvainOpt> best_cut, String part_file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));

		for (NodeSetLouvainOpt R : best_cut){
//			for (int i = 0; i < R.k; i++){
//				for (int s = 0; s < R.part.length; s++)
//					if (R.part[s] == i)
//						bw.write(R.ind2node[s] + ",");
//				bw.write("\n");
//			}
			
			for (int s = 0; s < R.part.length; s++)
				bw.write(R.ind2node[s] + ",");
			bw.write("\n");
		}
		
		bw.close();
	}
	
	////
	public static void writeTree(NodeSetLouvainOpt root_set, String tree_file, int m) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(tree_file));
		
		Queue<NodeSetLouvainOpt> queue = new LinkedList<NodeSetLouvainOpt>();
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
			bw.write(R.id + ":" + R.modularitySelf(m) + ";");
			
			if (R.children[0] != null){
				for (int i = 0; i < R.children.length; i++){
					bw.write(R.children[i].id + ",");
					queue.add(R.children[i]);
				}
			}else{	// leaf
				for (int s = 0; s < R.part.length; s++)
					bw.write(R.ind2node[s] + ",");
			}
			
			bw.write("\n");
			
		}
		
		bw.close();
	}
	
	////
	public static NodeSetLouvainOpt readTree(String tree_file) throws IOException{
		Map<Integer, NodeSetLouvainOpt> map = new HashMap<Integer, NodeSetLouvainOpt>();
		
		BufferedReader br = new BufferedReader(new FileReader(tree_file));
		//
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	int id = Integer.parseInt(str.substring(0, str.indexOf(":")));
        	double modSelf = Double.parseDouble(str.substring(str.indexOf(":") + 1,str.indexOf(";")));
        	
        	NodeSetLouvainOpt node = new NodeSetLouvainOpt(0);	// temporarily no child
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
//        		System.out.println("LEAF node.id = " + node.id);
        		
        		node.ind2node = new int[values.length];
//        		System.out.println("node.ind2node.length = " + node.ind2node.length);
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
        		NodeSetLouvainOpt cur_node = map.get(id);
        		
        		cur_node.k = items.length;
        		cur_node.children = new NodeSetLouvainOpt[items.length];
        		
        		for (int i = 0; i < values.length; i++){
        			cur_node.children[i] = map.get(values[i]);
        			cur_node.children[i].level = cur_node.level + 1;
        		}
        	}
		}
		
		br.close();
		
		// compute node.ind2node
		NodeSetLouvainOpt root_set = map.get(-1);
		Queue<NodeSetLouvainOpt> queue = new LinkedList<NodeSetLouvainOpt>();
		Stack<NodeSetLouvainOpt> stack = new Stack<NodeSetLouvainOpt>();
		
		// fill stack using queue
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
			stack.push(R);
			if (R.children != null)
				for (int i = 0; i < R.children.length; i++)
					queue.add(R.children[i]);
		}
		
		// 
		while (stack.size() > 0){
			NodeSetLouvainOpt R = stack.pop();
			
			if (R.ind2node == null){	// not leaf
				int len = 0;
				for (NodeSetLouvainOpt child : R.children)
					len += child.ind2node.length;
				
				R.ind2node = new int[len];
				int count = 0;
				for (NodeSetLouvainOpt child : R.children)
					for (int u : child.ind2node)
						R.ind2node[count++] = u;
			}
				
		}
		
		// debug, the same as _tree file
//		queue = new LinkedList<NodeSetLouvainOpt>();
//		queue.add(root_set);
//		while (queue.size() > 0){
//			NodeSetLouvainOpt R = queue.remove();
//			System.out.print(R.id + ":" + R.modSelf + ";");
//			
//			if (R.children.length > 0){
//				for (int i = 0; i < R.children.length; i++){
//					System.out.print(R.children[i].id + ",");
//					queue.add(R.children[i]);
//				}
//			}else{	// leaf
//				for (int s = 0; s < R.ind2node.length; s++)
//					System.out.print(R.ind2node[s] + ",");
//			}
//			
//			System.out.println();
//			
//		}
		
		//
		return root_set;
	}
	
	
	////dynamic programming: opt(R) = max{mod(R), opt(R.left) + opt(R.right)}
	public static List<NodeSetLouvainOpt> bestCutOffline(NodeSetLouvainOpt root_set, int m){
		
		List<NodeSetLouvainOpt> ret = new ArrayList<NodeSetLouvainOpt>();
		Map<Integer, CutNode> sol = new HashMap<Integer, CutNode>();	// best solution node.id --> CutNode info
		
		Queue<NodeSetLouvainOpt> queue = new LinkedList<NodeSetLouvainOpt>();
		Stack<NodeSetLouvainOpt> stack = new Stack<NodeSetLouvainOpt>();
		
		// fill stack using queue
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
			stack.push(R);
			if (R.children.length > 0)
				for (int i = 0; i < R.children.length; i++)
					queue.add(R.children[i]);
		}
		
		// 
		while (stack.size() > 0){
			NodeSetLouvainOpt R = stack.pop();
			
			double mod = R.modSelf;			// non-private, need modularitySelfDP() !
			boolean self = true;
			if (R.children.length == 0){	// leaf nodes
				sol.put(R.id, new CutNode(mod, true));
				
			}else{
				//
				double mod_opt = 0.0;
				for (int i = 0; i < R.children.length; i++)
					mod_opt += sol.get(R.children[i].id).mod;
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
		queue = new LinkedList<NodeSetLouvainOpt>();
		queue.add(root_set);
		while (queue.size() > 0){
			NodeSetLouvainOpt R = queue.remove();
			
			if (sol.get(R.id).self == true){
				ret.add(R);
				System.out.print(R.id + " ");
			}else if (R.children.length > 0)
				for (int i = 0; i < R.children.length; i++)
					queue.add(R.children[i]);
		}
		System.out.println();
		
		//
		return ret;
	}
	
}
