/*
 * Sep 17, 2015
 * 	- use HRG-Divisive (see dp.comm.CommunityFit) but at each node, we decide the further partition if doing so increases the modularity
 * 	- use NodeSetDivGreedy.java
 * Sep 22
 */

package dp.comm;

import java.util.List;

import dp.combined.NodeSetDivGreedy;
import dp.mcmc.Dendrogram;
import algs4.EdgeWeightedGraph;
import grph.Grph;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;

public class HRGDivisiveFit {

	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("HRGDivisiveGreedy");
		
		// load graph
//		String dataname = "karate";			// (34, 78)
		String dataname = "polbooks";		// (105, 441)		
//		String dataname = "polblogs";		// (1224,16715) 	
//		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762) 	
//		String dataname = "ca-HepPh";		// (12006,118489) 	 
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 	
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 	
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 	
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624) 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 20;
		int limit_size = 8;		// at least 4*lower_size
		int lower_size = 2;		// at least 2
		int max_level = 4;
		double eps = 20.0;
		double ratio = 2.0; // 1.26 = 2^(1/3)
		double eps_mod = 0.1;		// epsilon used in bestCut()
		
		if(args.length >= 8){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			burn_factor = Integer.parseInt(args[3]);
			max_level = Integer.parseInt(args[4]);
			lower_size = Integer.parseInt(args[5]);
			limit_size = lower_size * 4;				//
			eps = Double.parseDouble(args[6]);
			ratio = Double.parseDouble(args[7]);
		}
		
		System.out.println("dataname = " + dataname);
		System.out.println("burn_factor = " + burn_factor + " n_samples = " + n_samples);
		System.out.println("limit_size = " + limit_size);
		System.out.println("lower_size = " + lower_size);
		System.out.println("max_level = " + max_level);
		System.out.println("eps = " + eps);
		System.out.println("ratio = " + ratio);
		
		double eps1 = eps - eps_mod*max_level;
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
//		String part_file = prefix + "_out/" + dataname +"_hrgdivgreedy_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" 
//				+ max_level + "_" + String.format("%.2f", ratio) + "_" + String.format("%.1f", eps1) + ".part";
		String node_file = prefix + "_out/" + dataname +"_hrgdiv_" + burn_factor + "_"
				+ max_level + "_" + lower_size + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree";
		
		//
		/*
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		NodeSetModOpt R = new NodeSetModOpt(G);
		System.out.println("mod = " + R.modularity(G.getNumberOfEdges()));
		*/
		
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		NodeSetDivGreedy R = new NodeSetDivGreedy(G);
		System.out.println("logLK = " + R.logLK());
		
		// TEST recursiveLK()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			long start = System.currentTimeMillis();
			NodeSetDivGreedy root_set = NodeSetDivGreedy.recursiveLK(G, eps1, burn_factor, limit_size, lower_size, max_level, ratio);	
			System.out.println("recursiveLK - DONE, elapsed " + (System.currentTimeMillis() - start));
			
//			NodeSetDivGreedy.printSetIds(root_set, G.E());
			
			start = System.currentTimeMillis();
			Dendrogram D = NodeSetDivGreedy.convertToHRG(G, root_set, max_level);
			System.out.println("convertToHRG - DONE, elapsed " + (System.currentTimeMillis() - start));
			
//			System.out.println("AFTER convertToHRG");
//			NodeSetDivGreedy.printSetIds(root_set, G.E());
			
			D.writeInternalNodes(node_file + "." + i);
			System.out.println("D.logLK = " + D.logLK());
		}

	}

}
