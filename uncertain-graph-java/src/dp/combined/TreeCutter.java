/*
 * Oct 14, 2015
 * 	- 
 * Oct 18
 * 	- add logLK(): compute log-likelihood
 * Dec 2
 * 	- add cutTreeHRGFixed()
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import dp.mcmc.Dendrogram;
import dp.mcmc.Node;
import algs4.Edge;
import algs4.EdgeIntGraph;
import algs4.EdgeWeightedGraph;

public class TreeCutter {

	////
	public static void fixTreeFile(String prefix, String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(prefix + "_sample2/" + filename));
		BufferedWriter bw = new BufferedWriter(new FileWriter(prefix + "_sample/" + filename));
		
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	ArrayList<Integer> pos = new ArrayList<Integer>();
    		for(int i = 0; i < str.length(); i++){
    		    if(str.charAt(i) == ':'){
    		       pos.add(i);
    		    }
    		}
    		
        	if (pos.size() > 1){
        		int last_minus = 0;
        		for (int i = 1; i < pos.size(); i++){
        			int minus_pos = str.substring(0, pos.get(i)).lastIndexOf("-");
        			
        			bw.write(str.substring(last_minus, minus_pos) + "\n");
        			last_minus = minus_pos;
        		}
        		bw.write(str.substring(last_minus) + "\n");
        		
        		//
        		count += 1;
        	}else{
        		bw.write(str + "\n");
        	}
		}
		
		br.close();
		bw.close();
	}
	
	////
	public static void fixHRGDivisiveTreeFiles() throws IOException{
		
		String[] dataname_list = new String[]{"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
		int[] n_list = new int[]{334863, 317080, 1134890};
		int n_samples = 20;
		
		for (int i = 0; i < n_list.length; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			
			System.out.println("dataname = " + dataname);
			//
			double log_n = Math.log(n);
			int max_level = 10;
			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
			int burn_factor = 20;
			double[] ratioArr = new double[]{2.0, 1.5, 1.0};
			
			for (double ratio : ratioArr){
				System.out.println("ratio = " + ratio);
				for (double eps : epsArr){
					System.out.println("eps = " + eps);
					String filename = dataname + "_hd_" + burn_factor + "_" + max_level + "_" + 
							String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree";
					
					for (int s = 0; s < n_samples; s++){
						System.out.println("sample " + s);
						fixTreeFile("", filename + "." + s);
					}
					
				}
			}
		}
		
		
		
	}
	
	
	////
	public static void cutTreeNMD(String tree_file, int n_samples) throws IOException{
		String part_file = tree_file.substring(0, tree_file.length()-5);
		
		for (int i = 0; i < n_samples; i++){
			NodeSetLouvainOpt root_set = NodeSetLouvainOpt.readTree("_sample/" + tree_file + "." + i);
			
			
			List<NodeSetLouvainOpt> best_cut = NodeSetLouvainOpt.bestCutOffline(root_set);
			System.out.println("best_cut.size = " + best_cut.size());
			NodeSetLouvainOpt.writeBestCut(best_cut, "_louvain/" + part_file + "." + i + ".best");
			
			
			List<NodeSetLouvainOpt> part2_cut = NodeSetLouvainOpt.cutLevel(root_set, 2);
			System.out.println("part2_cut.size = " + part2_cut.size());
			NodeSetLouvainOpt.writeBestCut(part2_cut, "_louvain/" + part_file + "." + i + ".part2");
		}
		
	}
	
	////
	public static void cutTreeMD(String tree_file, int n_samples) throws IOException{
		String part_file = tree_file.substring(0, tree_file.length()-5);
		
		for (int i = 0; i < n_samples; i++){
			NodeSetLouvain root_set = NodeSetLouvain.readTree("_sample/" + tree_file + "." + i);
			
			
			List<NodeSetLouvain> best_cut = NodeSetLouvain.bestCutOffline(root_set);
			System.out.println("best_cut.size = " + best_cut.size());
			NodeSetLouvain.writeBestCut(best_cut, "_louvain/" + part_file + "." + i + ".best");
			
			
			List<NodeSetLouvain> part2_cut = NodeSetLouvain.cutLevel(root_set, 2);
			System.out.println("part2_cut.size = " + part2_cut.size());
			NodeSetLouvain.writeBestCut(part2_cut, "_louvain/" + part_file + "." + i + ".part2");
		}
		
	}
	
	////
	public static void cutTreeHD(String tree_file, int n_samples) throws IOException{
		String part_file = tree_file.substring(0, tree_file.length()-5);
		
		for (int i = 0; i < n_samples; i++){
			NodeSetLouvain root_set = NodeSetLouvain.readTree("_sample/" + tree_file + "." + i);
			
			
			List<NodeSetLouvain> best_cut = NodeSetLouvain.bestCutOffline(root_set);
			System.out.println("best_cut.size = " + best_cut.size());
			NodeSetLouvain.writeBestCut(best_cut, "_louvain/" + part_file + "." + i + ".best");
			
			
			List<NodeSetLouvain> part2_cut = NodeSetLouvain.cutLevel(root_set, 2);
			System.out.println("part2_cut.size = " + part2_cut.size());
			NodeSetLouvain.writeBestCut(part2_cut, "_louvain/" + part_file + "." + i + ".part2");
		}
		
	}
	
	////
	public static void cutTreeHRGFixed(EdgeIntGraph G, String tree_file, int n_samples, int level) throws IOException{
		String part_file = tree_file.substring(0, tree_file.length()-5);
		
		for (int i = 0; i < n_samples; i++){
			NodeSetLouvain root_set = Dendrogram.readTree(G, "_out/" + tree_file + "." + i);
			
			List<NodeSetLouvain> best_cut = NodeSetLouvain.bestCutHRGFixed(root_set, G.E(), level, 0.0);
			System.out.println("best_cut.size = " + best_cut.size());
			NodeSetLouvain.writeBestCutHRG(best_cut, "_louvain/" + part_file + "." + i + ".best");
		}
		
	}
	
	////
	public static void cutTreeHRGMCMC(EdgeIntGraph G, String tree_file, int n_samples) throws IOException{
		String part_file = tree_file.substring(0, tree_file.length()-5);
		
		for (int i = 0; i < n_samples; i++){
			NodeSetLouvain root_set = Dendrogram.readTree(G, "_out/" + tree_file + "." + i);
			System.out.println("max_level = " + root_set.max_level);
			
			System.out.println("root_set.ind2node = " + root_set.ind2node.length);
			System.out.println(root_set.children[0].ind2node.length + " " + root_set.children[1].ind2node.length);
			
			List<NodeSetLouvain> best_cut = NodeSetLouvain.bestCutHRGMCMC(root_set, G.E(), 0.0);
			System.out.println("best_cut.size = " + best_cut.size());
			NodeSetLouvain.writeBestCutHRG(best_cut, "_louvain/" + part_file + "." + i + ".best");
		}
		
	}
	
	//// return 2 children of ancestor
	public static Int2 getLowestAncestor(NodeSetLouvain uLeaf, NodeSetLouvain vLeaf, NodeSetLouvain ans){
		
		NodeSetLouvain uR = uLeaf;
		NodeSetLouvain vR = vLeaf;
		
		if (uR.level > vR.level){
			for (int i = 0; i < uR.level - vR.level; i++)
				uR = uR.parent;
		}else{
			for (int i = 0; i < vR.level - uR.level; i++)
				vR = vR.parent;
		}
		
		NodeSetLouvain prev_u = uR;;
		NodeSetLouvain prev_v = vR;
		while (uR.id != vR.id){
			prev_u = uR;
			prev_v = vR;
			uR = uR.parent;
			vR = vR.parent;
		}
		ans = uR;
		int u_child = -1;
		int v_child = -1;
		
		for (int i = 0; i < ans.children.length; i++){
			if (ans.children[i].id == prev_u.id)
				u_child = i;
			if (ans.children[i].id == prev_v.id)
				v_child = i;
		}
		
		return new Int2(u_child, v_child);
	}
	
	//// read all _nmd_, _md_, _hd_ files
	public static double logLK(EdgeWeightedGraph G, NodeSetLouvain root_set){
		
		// find leaf nodes
		List<NodeSetLouvain> leaves = new ArrayList<NodeSetLouvain>();
		Queue<NodeSetLouvain> queue_set = new LinkedList<NodeSetLouvain>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetLouvain R = queue_set.remove();
			if (R.children.length == 0) 	// leaf, for readTree()
//			if (R.children[0] == null) 		// leaf, for randomBinaryTree()
				leaves.add(R);
			else{
				for (int i = 0; i < R.children.length; i++)
					queue_set.add(R.children[i]);
			}
		}
		
		// debug
//		for (NodeSetLouvain R : leaves){
//			System.out.print("id = " + R.id + " level = " + R.level + " : ");
//			for (int u : R.ind2node)
//				System.out.print(u + " ");
//			System.out.println();
//		}
		
		
		// map nodes of G to leaves
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < leaves.size(); i++){
			NodeSetLouvain R = leaves.get(i);
			for (int u : R.ind2node)
				map.put(u, i);
		}
	
		// debug
//		for (Map.Entry<Integer, Integer> entry : map.entrySet())
//			System.out.print("(" + entry.getKey() + "," + entry.getValue() + ") ");
//		System.out.println();
		
		//
		for (Edge e : G.edges()){
			int u = e.either();
			int v = e.other(u);
//			System.out.println(u + " " + v);
			NodeSetLouvain uR = leaves.get(map.get(u));
			NodeSetLouvain vR = leaves.get(map.get(v));
			
			if (uR.id == vR.id){
				uR.e_self += 1;
			}else{
				// find lowest common ancestor
				NodeSetLouvain ans = root_set;
				Int2 pair = getLowestAncestor(uR, vR, ans);
				ans.eArr[pair.val0][pair.val1] += 1;
			}
		}
		
		//
		double ret = 0.0;
		queue_set = new LinkedList<NodeSetLouvain>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetLouvain R = queue_set.remove();
			if (R.children.length == 0){ 	// leaf, for readTree()
//			if (R.children[0] == null){ 	// leaf, for randomBinaryTree()
				double n_e = (double)R.ind2node.length* (R.ind2node.length-1)/2;
				double p = (double)R.e_self/ n_e;
				if (p > 0.0 && p < 1.0)
					ret += R.e_self* Math.log(p) + (n_e - R.e_self)*Math.log(1-p);
				
			}else{
				for (int i = 0; i < R.children.length; i++)
					for (int j = 0; j < R.children.length; j++)
					if (R.eArr[i][j] > 0){
						double n_e = (double)R.children[i].ind2node.length * R.children[j].ind2node.length ;
						double p = (double)R.eArr[i][j]/ n_e;
						
						ret += R.eArr[i][j] * Math.log(p) + (n_e - R.eArr[i][j])*Math.log(1-p);
					}	
				
				for (int i = 0; i < R.children.length; i++)
					queue_set.add(R.children[i]);
			}
		}
		
		
		//
		return ret;
	}
	
	////
	public static void randomBinaryTree(EdgeWeightedGraph G, int max_level){
		int n = G.V();
		// random permutation
		List<Integer> id = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			id.add(i);
		Collections.shuffle(id);
		
		// build k-ary tree
		int k = 2;
		int id_count = -1;
		
		NodeSetLouvain root_set = new NodeSetLouvain(k);
		root_set.id = id_count--;
		root_set.ind2node = new int[n];
		for (int i = 0; i < n; i++)
			root_set.ind2node[i] = id.get(i);
		
		Queue<NodeSetLouvain> queue_set = new LinkedList<NodeSetLouvain>();
		queue_set.add(root_set);
		while (queue_set.size() > 0){
			NodeSetLouvain R = queue_set.remove();
			
			if (R.level < max_level){
				n = R.ind2node.length;		// NOTE
				
				for (int j = 0; j < k-1; j++){
					NodeSetLouvain child = new NodeSetLouvain(k);
					child.ind2node = new int[n/k];
					for (int i = 0; i < n/k; i++)
						child.ind2node[i] = R.ind2node[j*(n/k) + i];
					child.id = id_count--;
					child.level = R.level + 1;
					child.parent = R;
					R.children[j] = child;
					
					queue_set.add(child);
				}
				
				NodeSetLouvain child = new NodeSetLouvain(k);
				child.ind2node = new int[n - (k-1)*(n/k)];
				for (int i = (k-1)*(n/k); i < n; i++)
					child.ind2node[i - (k-1)*(n/k)] = R.ind2node[i];
				child.id = id_count--;
				child.level = R.level + 1;
				child.parent = R;
				R.children[k-1] = child;
				
				queue_set.add(child);
			}
		}
		
		// debug
//		queue_set = new LinkedList<NodeSetLouvain>();
//		queue_set.add(root_set);
//		while (queue_set.size() > 0){
//			NodeSetLouvain R = queue_set.remove();
//			
//			System.out.print("id = " + R.id + ": ");
//			if (R.children[0] != null)
//				for (NodeSetLouvain child : R.children){
//					System.out.print(child.id + ",");
//					queue_set.add(child);
//				}
//			System.out.println();
//		}
		
		
		//
		System.out.println("logLK = " + logLK(G, root_set) );
		
	}
	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
//		fixHRGDivisiveTreeFiles();
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
//		String prefix = "";
//		String tree_file = "";
//		int type = 0;	// 1 : NMD, 2:MD, 3:HD
//		
//		if(args.length >= 3){
//			prefix = args[0];
//			tree_file = args[1];
//			type = Integer.parseInt(args[2]);
//		}
//
//		System.out.println("tree_file = " + tree_file);
//		System.out.println("type = " + type);
		
		////
		String[] dataname_list = new String[]{"as20graph", "ca-AstroPh-wcc"}; //"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
		int[] n_list = new int[]{6474, 17903};//334863, 317080, 1134890};
		int n_samples = 20;
		
		
		////////// TODO: TREE-CUTTER
		// 1 - NMD (LouvainOpt)
//		int[] kArr = new int[]{2}; //,3,4,5,6,10};
//		int[] maxLevelArr = new int[]{10}; //,7,5,4,4,3};
//		int burn_factor = 20;
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			for (int j = 0; j < kArr.length; j++){
//				int k = kArr[j];
//				int max_level = maxLevelArr[j];
//				
//				String tree_file = dataname + "_nmd_" + burn_factor + "_" + max_level + "_" + k + "_tree";
//				
//				cutTreeNMD(tree_file, n_samples);
//			}
//		}
		
		// 2 - MD (LouvainModDiv)
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			double log_n = Math.log(n);
//			int[] kArr = new int[]{2,3,4}; // {2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{7,5,4}; // {10,7,5,4,4,3};
//			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n};
//			int burn_factor = 20;
//			double ratio = 2.0;
//			
//			for (double eps : epsArr){
//				for (int j = 0; j < kArr.length; j++){
//					int k = kArr[j];
//					int max_level = maxLevelArr[j];
//					
//					String tree_file = dataname + "_md_" + burn_factor + "_" + max_level + "_" + k + 
//							"_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree";
//					
//					cutTreeMD(tree_file, n_samples);
//				}
//			}
//		}
		
		// 3 - HD (HRGDivisiveGreedy)
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			int burn_factor = 20;
//			double[] ratioArr = new double[]{2.0, 1.5, 1.0};
//			int max_level = 10;
//			
//			for (double ratio : ratioArr){
//				for (double eps : epsArr){
//						
//					String tree_file = dataname + "_hd_" + burn_factor + "_" + max_level + 
//							"_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree";
//					
//					cutTreeHD(tree_file, n_samples);
//				}
//			}
//		}
		
		// 4 - fixed tree (HRGFixedTree)
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			String filename = "_data/" + dataname + ".gr";
//			EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");
//			System.out.println("#nodes = " + G.V());
//			System.out.println("#edges = " + G.E());
//			
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n};
//			int burn_factor = 1000;
//			int sample_freq = n;
//			int level = 10;
//			
//			for (double eps : epsArr){
//					
//				String tree_file = dataname + "_fixed_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + String.format("%.1f", eps) + "_tree";
//				
//				cutTreeHRGFixed(G, tree_file, n_samples, level);
//			}
//		}
		
		// 5 - HRG MCMC
		for (int i = 0; i < n_list.length; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			
			String filename = "_data/" + dataname + ".gr";
			EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");
			System.out.println("#nodes = " + G.V());
			System.out.println("#edges = " + G.E());
			
			double log_n = Math.log(n);
			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n};
			int burn_factor = 1000;
			int sample_freq = n;
			
			for (double eps : epsArr){
					
				String tree_file = dataname + "_dendro_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + String.format("%.1f", eps) + "_tree";
				
				cutTreeHRGMCMC(G, tree_file, n_samples);
			}
		}
		
		// TEST
//		String dataname = "as20graph";
//		n_samples = 1;
//		String filename = "_data/" + dataname + ".gr";
//		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//		String tree_file = "as20graph_dendro_20_6474_1000_4.4_tree";
//		
//		cutTreeHRGMCMC(G, tree_file, n_samples);
				
		
		
		////////// TODO: LOGLK
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("_data/karate.gr");
////		NodeSetLouvain root = NodeSetLouvain.readTree("_sample/karate_nmd_20_3_2_tree.1");
//		NodeSetLouvain root = NodeSetLouvain.readTree("_sample/karate_hd_20_3_2.0_1.00_tree.1");
		
		// TEST randomBinaryTree
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("_data/com_youtube_ungraph.gr");
//		for (int i = 0; i < 20; i++)
//			randomBinaryTree(G, 10);
		
		
		//
//		int[] kArr = new int[]{2,3,4,5,6,10};
//		int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//		int burn_factor = 20;
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			System.out.println("dataname = " + dataname);
//			EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("_data/" + dataname + ".gr");
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			double ratio = 2.0;
//			
//			// NMD
////			System.out.println("NMD");
////			for (int j = 0; j < kArr.length; j++){
////				int k = kArr[j];
////				int max_level = maxLevelArr[j];
////				System.out.println("(k,maxL) = " + k + "," + max_level);
////				
////				String tree_file = dataname + "_nmd_" + burn_factor + "_" + max_level + "_" + k + "_tree";
////				double sum_lk = 0.0;
////				for (int s = 0; s < n_samples; s++){
////					NodeSetLouvain root = NodeSetLouvain.readTree("_sample/" + tree_file + "." + s);
////					sum_lk += logLK(G, root);
////				}
////				System.out.println("logLK = " + (sum_lk/n_samples));
////			}
////			
////			// MD
////			System.out.println("MD");
////			
////			for (int j = 0; j < kArr.length; j++){
////				int k = kArr[j];
////				int max_level = maxLevelArr[j];
////				System.out.println("(k,maxL) = " + k + "," + max_level);
////				
////				for (double eps : epsArr){
////					System.out.println("eps = " + String.format("%.1f", eps) );
////					
////					String tree_file = dataname + "_md_" + burn_factor + "_" + max_level + "_" + k +
////							"_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree";
////					double sum_lk = 0.0;
////					for (int s = 0; s < n_samples; s++){
////						NodeSetLouvain root = NodeSetLouvain.readTree("_sample/" + tree_file + "." + s);
////						sum_lk += logLK(G, root);
////					}
////					System.out.println("logLK = " + (sum_lk/n_samples));
////				}
////			}
//			
//			// HD
////			System.out.println("HD");
////			ratio = 1.0;
////			int k = 2;
////			int max_level = 10;
////			System.out.println("(k,maxL) = " + k + "," + max_level + " ratio = " + ratio);
////			
////			for (double eps : epsArr){
////				System.out.println("eps = " + String.format("%.1f", eps));
////				
////				String tree_file = dataname + "_hd_" + burn_factor + "_" + max_level + 
////						"_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree";
////				double sum_lk = 0.0;
////				for (int s = 0; s < n_samples; s++){
////					NodeSetLouvain root = NodeSetLouvain.readTree("_sample/" + tree_file + "." + s);
////					sum_lk += logLK(G, root);
////				}
////				System.out.println("logLK = " + (sum_lk/n_samples));
////			}
//		}
		
		
		
	}

}
