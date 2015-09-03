package dp.comm;

import java.util.List;

import dp.mcmc.Dendrogram;
import dp.mcmc.Node;
import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import hist.DegreeSeqHist;
import hrg.HRG;

public class CommunityFit {

	////
	static void test(){
		// TOY GRAPH
		Grph G = new InMemoryGrph();
		G.addNVertices(6);
//				for (int v = 0; v < 6; v++)
//					G.addSimpleEdge(v, v+1, false);
		G.addSimpleEdge(0, 1, false);
		G.addSimpleEdge(0, 2, false);
		G.addSimpleEdge(1, 2, false);
		G.addSimpleEdge(2, 3, false);
		G.addSimpleEdge(3, 4, false);
		G.addSimpleEdge(3, 5, false);
		G.addSimpleEdge(4, 5, false);
		
		//
		NodeSet R = new NodeSet(G);
		
		System.out.println(R.e_s);
		System.out.println(R.n_s);
		System.out.println(R.e_t);
		System.out.println(R.n_t);
		System.out.println(R.e_st);
		//
		System.out.println("logLK = " + R.logLK());
	}
	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("CommunityFit");
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)						 converge at 1250 (-1284.67 > -1397.01)
											// recursiveLK		0.3s,	edit.dist=296
//		String dataname = "polblogs";		// (1224,16715) 	124k(15.5s),     converge at 24k (-63293.5 > -74986.3)
											// recursiveLK		7.8s	edit.dist=13279
//		String dataname = "as20graph";		// (6474,12572)		66k steps (27s), converge at 66k (-80965.7 > -102679.0 start)
											// recursiveLK		110s,	edit.dist=12195
//		String dataname = "wiki-Vote";		// (7115,100762) 	144k steps (61s) converge at 72k (-493504.7 > -637501.2 start)
											// recursiveLK		120s,	edit.dist=91893
//		String dataname = "ca-HepPh";		// (12006,118489) 	122k steps(76s), converge at 120k (-639774.8 > -877945.1 start) 
															
//		String dataname = "ca-AstroPh";		// (18771,198050) 	188k steps(216s),converge at 188k (-1342741.6 > -1542791.1 start)
		
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624) mem (4.7GB) (
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 10;
		int burn_factor = 20;
		int limit_size = 1;
		
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
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		String node_file = prefix + "_out/" + dataname + "_div_hrg_" + burn_factor + "_" + limit_size;		// _hrg : NON-PRIVATE
		String sample_file = prefix + "_sample/" + dataname + "_divisive_" + burn_factor + "_" + limit_size;
		
		//
//		GrphTextReader reader = new GrphTextReader();
		EdgeListReader reader = new EdgeListReader();
		
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		NodeSet R = new NodeSet(G);
		System.out.println("logLK = " + R.logLK() + " mincut = " + R.mincut() + " edgeVar = " + R.edgeVar());
		
		// TEST partitionLK()
//	    long start = System.currentTimeMillis();
//	    
//	    NodeSet.partitionLK(R, G, burn_factor*G.getNumberOfVertices(), n_samples, sample_freq, true);
////	    NodeSet.partitionMC(R, G, burn_factor*G.getNumberOfVertices(), n_samples, sample_freq, true);
////	    NodeSet.partitionEV(R, G, burn_factor*G.getNumberOfVertices(), n_samples, sample_freq, true);
//	    
//	    System.out.println("Node.partition - DONE, elapsed " + (System.currentTimeMillis() - start));
//	    
//	    System.out.println("logLK = " + R.logLK() + " mincut = " + R.mincut());
////	    R.print();
//	    // check -> correctly
//	    System.out.println("S.size + T.size = " + (R.S.size() + R.T.size()));
		
		
		// TEST recursiveLK()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			long start = System.currentTimeMillis();
			NodeSet root_set = NodeSet.recursiveLK(G, burn_factor, limit_size);
			System.out.println("recursiveLK - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			//debug
//			NodeSet.printSetIds(root_set);
			
			Dendrogram D = NodeSet.convertToHRG(G, root_set);
			
			D.writeInternalNodes(node_file + "." + i);
			System.out.println("D.logLK = " + D.logLK());
			
			Grph aG = D.generateSanitizedSample(G.getNumberOfVertices(), false);
			
//			start = System.currentTimeMillis();
//			System.out.println("edit distance (aG, G) = " + DegreeSeqHist.editScore(aG, G));
//			System.out.println("editScore - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(aG, f);
		}
	    
	}

}
