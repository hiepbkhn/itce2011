package dp.comm;

import java.util.List;

import algs4.EdgeIntGraph;
import toools.io.file.RegularFile;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;

public class CommunityPriv {

	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("CommunityPriv");
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)						 converge at 1250 (-1284.67 > -1397.01)
		String dataname = "polblogs";		// (1224,16715) 	124k(15.5s),     converge at 24k (-63293.5 > -74986.3)
//		String dataname = "as20graph";		// (6474,12572)		66k steps (27s), converge at 66k (-80965.7 > -102679.0 start)
//		String dataname = "wiki-Vote";		// (7115,100762) 	144k steps (61s) converge at 72k (-493504.7 > -637501.2 start)
//		String dataname = "ca-HepPh";		// (12006,118489) 	122k steps(76s), converge at 120k (-639774.8 > -877945.1 start) 
//		String dataname = "ca-AstroPh";		// (18771,198050) 	188k steps(216s),converge at 188k (-1342741.6 > -1542791.1 start)
		
		// COMMAND-LINE
		String prefix = "";
		int n_samples = 20;
		int sample_freq = 100;
		int burn_factor = 20;
		double eps1 = 1.0;
		System.out.println("dataname = " + dataname);
		System.out.println("burn_factor = " + burn_factor + " sample_freq = " + sample_freq + " n_samples = " + n_samples);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		
		//
	    long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		NodePriv R = new NodePriv(G);
		System.out.println("logLK = " + R.logLK() + " mincut = " + R.mincut() + " edgeVar = " + R.edgeVar());
		
		// TEST 
	    start = System.currentTimeMillis();
	    
//	    NodePriv.partitionLK(R, G, eps1, burn_factor*G.V(), n_samples, sample_freq);
	    NodePriv.partitionEV(R, G, eps1, burn_factor*G.V(), n_samples, sample_freq);
	    
	    System.out.println("NodePriv.partition - DONE, elapsed " + (System.currentTimeMillis() - start));
	    
	    System.out.println("logLK = " + R.logLK() + " mincut = " + R.mincut());
//	    R.print();
	    // check -> correctly
	    System.out.println("S.size + T.size = " + (R.S.size() + R.T.size()));
	    
	}

}
