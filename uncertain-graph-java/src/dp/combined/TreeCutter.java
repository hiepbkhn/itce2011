/*
 * Oct 14, 2015
 * 	- 
 * Oct 18
 * 	- add logLK(): compute log-likelihood
 */

package dp.combined;

import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import algs4.Edge;
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
			if (R.children.length == 0) // leaf
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
			if (R.children.length == 0){ // leaf
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
		String[] dataname_list = new String[]{ "com_amazon_ungraph"}; //"com_amazon_ungraph", "com_dblp_ungraph", com_youtube_ungraph
		int[] n_list = new int[]{334863};	//334863, 317080, 1134890
		int n_samples = 20;
		
		
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
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			double[] epsArr = new double[]{log_n};	//2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			int burn_factor = 100;
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
		
		
		//////////
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("_data/karate.gr");
////		NodeSetLouvain root = NodeSetLouvain.readTree("_sample/karate_nmd_20_3_2_tree.1");
//		NodeSetLouvain root = NodeSetLouvain.readTree("_sample/karate_hd_20_3_2.0_1.00_tree.1");
		
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("_data/com_amazon_ungraph.gr");
		NodeSetLouvain root = NodeSetLouvain.readTree("_sample/com_amazon_ungraph_md_20_10_2_38.2_2.00_tree.0");
		
		long start = System.currentTimeMillis();
		double ret = logLK(G, root);
		System.out.println("Done, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("logLK = " + ret);
		
	}

}
