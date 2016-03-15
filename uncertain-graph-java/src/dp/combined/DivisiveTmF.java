/*
 * Sep 3, 2015
 * 	- combine HRG-Divisive (dp.comm.NodeSet2) with TmF (naive.GreedyConstruct)
 * 	- use NodeSetDiv.java
 * 
 */

package dp.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;
import dp.mcmc.Node;

public class DivisiveTmF {

	////
	public static Grph generateSample(String nodeset_file, Grph G, double epsTmF, double eps1, int max_level) throws IOException{
		Grph aG = new InMemoryGrph();
	    aG.addNVertices(G.getNumberOfVertices());
	    
	    // read cluster_file
	    BufferedReader br = new BufferedReader(new FileReader(nodeset_file));
	    
	    int in_cluster_edges = 0;
	    while (true){
	    	// level
        	String str = br.readLine();
        	if (str == null)
        		break;
        	int level = Integer.parseInt(str);
        	
        	// node ids
        	str = br.readLine();
        	String[] items = str.split(",");
        	IntSet nodeset = new IntHashSet();
        	for (int i = 0; i < items.length; i++)
        		nodeset.add(Integer.parseInt(items[i]));
        	
        	Grph sG = G.getSubgraphInducedByVertices(nodeset);
        	System.out.println("sG: #nodes = " + sG.getNumberOfVertices() + " #edges = " + sG.getNumberOfEdges());
        	
        	in_cluster_edges += sG.getNumberOfEdges();
	    }
	    br.close();
	    
	    System.out.println("in_cluster_edges = " + in_cluster_edges);
	    //
	    return aG;
	}
	
	
	////
	public static void writeNodeSets(NodeSetDiv root_set, String filename) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

		Queue<NodeSetDiv> queue_set = new LinkedList<NodeSetDiv>();
		queue_set.add(root_set);
		
		int num_leaf_nodes = 0;
		int between_cluster_edges = 0;
		
		while (queue_set.size() > 0){
			NodeSetDiv R = queue_set.remove();
			if (R.left != null){
				int parent_id = -100000000;
				if (R.parent != null)
					parent_id = R.parent.id;
				System.out.println("R.id = " + R.id + " parent.id = " + parent_id + " (left+right).size = " + (R.S.size() + R.T.size()) + " R.e_st = " + R.e_st);
				between_cluster_edges += R.e_st;
				
				queue_set.add(R.left);
				queue_set.add(R.right);
			}else{
				System.out.println("LEAF R.id = " + R.id + " parent.id = " + R.parent.id + " (left+right).size = " + (R.S.size() + R.T.size()));
				num_leaf_nodes += (R.S.size() + R.T.size());
//				for (IntCursor t : R.S)
//					System.out.print(t.value + " ");
//				for (IntCursor t : R.T)
//					System.out.print(t.value + " ");
//				System.out.println();
				
				//
				bw.write(R.level + "\n");
				for (IntCursor t : R.S)
					bw.write(t.value + ",");
				for (IntCursor t : R.T)
					bw.write(t.value + ",");
				bw.write("\n");
			}
			
		}
		
		System.out.println("between_cluster_edges = " + between_cluster_edges);
		System.out.println("num_leaf_nodes = " + num_leaf_nodes);
		
		bw.close();
	}
	
	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("DivisiveTmF");
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)		
											// recursiveLK		
//		String dataname = "polblogs";		// (1224,16715) 	
											// recursiveLK		
//		String dataname = "as20graph";		// (6474,12572)		(100,20): 12s
											// recursiveLK		
//		String dataname = "wiki-Vote";		// (7115,100762) 	
											// recursiveLK		
//		String dataname = "ca-HepPh";		// (12006,118489) 	 
															
//		String dataname = "ca-AstroPh";		// (18771,198050) 	(200,30): 60s
		
		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624) mem (4.7GB) 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 20;
		int limit_size = 100;
		int lower_size = 20;		// at least 2
		int max_level = 4;
		double eps1 = 50.0;
		double epsTmF = 2.0; 
		
		if(args.length >= 4){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			burn_factor = Integer.parseInt(args[3]);
		}
		if(args.length >= 5)
			limit_size = Integer.parseInt(args[4]);
		
		System.out.println("dataname = " + dataname);
		System.out.println("burn_factor = " + burn_factor + " n_samples = " + n_samples);
		System.out.println("limit_size = " + limit_size);
		System.out.println("lower_size = " + lower_size);
		System.out.println("max_level = " + max_level);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		String nodeset_file = prefix + "_out/" + dataname + "_divtmf_cluster_" + burn_factor + "_" + limit_size + "_" 
															+ lower_size + "_" + max_level + "_" + String.format("%.2f",eps1);		// 
		String sample_file = prefix + "_sample/" + dataname + "_divtmf_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" + max_level;
		
		//
////		GrphTextReader reader = new GrphTextReader();
//		EdgeListReader reader = new EdgeListReader();
//		
//		Grph G;
//		RegularFile f = new RegularFile(filename);
//		
//		G = reader.readGraph(f);
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		//
//		NodeSetDiv R = new NodeSetDiv(G);
//		System.out.println("logLK = " + R.logLK() + " mincut = " + R.mincut() + " edgeVar = " + R.edgeVar());
//		
//		// TEST recursiveLK()
//		for (int i = 0; i < n_samples; i++){
//			System.out.println("sample i = " + i);
//			
//			long start = System.currentTimeMillis();
//			NodeSetDiv root_set = NodeSetDiv.recursiveLK(G, eps1, burn_factor, limit_size, lower_size, max_level, false);	// false: stop partitioning at limit_size
//			System.out.println("recursiveLK - DONE, elapsed " + (System.currentTimeMillis() - start));
//			
//			//debug
////			NodeSet2.printSetIds(root_set);
//			
//			writeNodeSets(root_set, nodeset_file + "." + i);
//			
//			Grph aG = generateSample(nodeset_file + "." + i, G, epsTmF, eps1, max_level);
//			
//////			start = System.currentTimeMillis();
//////			System.out.println("edit distance (aG, G) = " + DegreeSeqHist.editScore(aG, G));
//////			System.out.println("editScore - DONE, elapsed " + (System.currentTimeMillis() - start));
////			
////			f = new RegularFile(sample_file + "." + i);
////			EdgeListWriter writer = new EdgeListWriter();
////	    	writer.writeGraph(aG, f);
//		}

	}

}
