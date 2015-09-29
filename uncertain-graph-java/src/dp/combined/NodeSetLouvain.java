/*
 * Sep 28, 2015
 * 	- exponential mechanism via MCMC on K groups of nodes (first pass of Louvain method)
 * Sep 29, 2015
 * 	- apply tree structure (not binary)
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


public class NodeSetLouvain {

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
	public NodeSetLouvain[] children;
	public int id;
	public int level = 0;
	
	//// for the case number of parts < k
	public void normalizePart(){
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
		
	}
	
	////
	public static void getSubEgdeLists(NodeSetLouvain R, List<List<Int2>> e_list){
		int u = 0;
		int v = 0;
		for (Int2 e : R.e_list){
			u = R.node2ind.get(e.val0);
			v = R.node2ind.get(e.val1);
			if (R.part[u] == R.part[v])
				e_list.get(R.part[u]).add(e);
		}
	}
	
	////
	public NodeSetLouvain(EdgeWeightedGraph G, int k){
		int n = G.V();
		
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
		
		// lc, dc
		int u; 
		int v;
		for (Edge e : G.edges()){
			u = e.either();
			v = e.other(u);
			if (this.part[u] == this.part[v])
				this.lc[this.part[u]] += 1;
			this.dc[this.part[u]] += 1;
			this.dc[this.part[v]] += 1;
		}
		
	}
	
	////
	public double modularity(int m){
		double mod = 0.0;
		
		for (int i = 0; i < lc.length; i++)
			mod += (double)this.lc[i]/m - (this.dc[i]/(2.0*m)) *(this.dc[i]/(2.0*m));
		//
		return mod;
	}
	
	//// move node u to group 'dest'
	public void move(int u, int dest, EdgeWeightedGraph G){
		
		
		int deg_u = G.degree(u);
		this.dc[part[u]] -= deg_u;
		this.dc[dest] += deg_u;
		
		for (int v : G.adj(u).keySet()){
			if (this.part[v] == this.part[u])
				this.lc[part[u]] -= 1;
			if (this.part[v] == dest)
				this.lc[dest] += 1;
		}
		
		//
		this.part[u] = dest;
	}
	
	
	////
	public static void partitionMod(NodeSetLouvain R, EdgeWeightedGraph G, double eps_p, int n_steps, int n_samples, int sample_freq){
		
		int n_nodes = G.V();
		int n_edges = G.E();
		int k = R.lc.length;
		
		// compute dU
	    double dU = 8.0/n_edges;
	    
	    System.out.println("#steps = " + (n_steps + n_samples * sample_freq));
		//
	    Random random = new Random();
		int n_accept = 0;
		int n_accept_positive = 0;
		int u = -1;
		int dest = -1;
		int old_dest = -1;
		
		double modT = R.modularity(n_edges);
		double modT2;
		
		for (int i = 0; i < n_steps + n_samples * sample_freq; i++) {
			
			// decide (u, dest)
			u = random.nextInt(n_nodes);
			dest = random.nextInt(k);
			while (dest == R.part[u])
				dest = random.nextInt(k);
			
			old_dest = R.part[u];
			R.move(u, dest, G);
			
			// MCMC
			modT2 = R.modularity(n_edges);
			
			if (modT2 > modT){
				n_accept += 1;
				n_accept_positive += 1;
				modT = modT2;
				
			}else{
				double prob = Math.exp(eps_p/(2*dU)*(modT2 - modT));			// prob ~ 1.0
				// System.out.println("prob = " + prob);
				double prob_val = random.nextDouble();
				
				if (prob_val > prob){
					// reverse
					R.move(u, old_dest, G);
					
				}else {
					n_accept += 1;
					modT = modT2;
					//
				}
			}
			
			
		}
	}
	
	////
	public static NodeSetLouvain recursiveMod(EdgeWeightedGraph G, double eps1, int burn_factor, int limit_size, int lower_size, int max_level, double ratio, int k){
		int n_nodes = G.V();
		int n_edges = G.E();
		int id = -1;
		
		//
		double[] epsArr = DPUtil.epsilonByLevel(eps1, max_level, ratio); 
		
		// root node
		NodeSetLouvain root = new NodeSetLouvain(G, k);
		root.id = id--;
		root.level = 0;
		
		// 
		Queue<NodeSetLouvain> queue = new LinkedList<NodeSetLouvain>();
		queue.add(root);
		while(queue.size() > 0){
			NodeSetLouvain R = queue.remove();
			System.out.println("R.level = " + R.level + " R.size = " + R.part.length);
			
			if (R.part.length <= limit_size || R.level == max_level){
				continue;
			}
			
			long start = System.currentTimeMillis();
			NodeSetLouvain.partitionMod(R, G, epsArr[R.level], burn_factor*R.part.length, 0, 0);
			System.out.println("elapsed " + (System.currentTimeMillis() - start));
			
			
		}
		
		//
		return root;
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
	
}
