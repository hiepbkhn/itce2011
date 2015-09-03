/*
 * Mar 25, 2015
 */
package dp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import nl.peterbloem.powerlaws.Continuous;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

import dp.mcmc.Dendrogram;
import dp.mcmc.Int4;
import toools.io.file.RegularFile;
import toools.math.Distribution;
import toools.set.IntSet;
import grph.Grph;
import grph.Grph.DIRECTION;
import grph.Grph.TYPE;
import grph.algo.distance.DistanceMatrix;
import grph.algo.search.BFSAlgorithm;
import grph.algo.search.SearchResult;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.GrphTextReader;

public class UtilityMeasure {
	
	////
	public static double[] getDegreeDistr(Grph G, DegreeMetric deg){
		int n_nodes = G.getNumberOfVertices();
		int n_edges = G.getNumberOfEdges();
		
		double[] dist = new double[n_nodes];
		
		deg.s_AD = 2.0*n_edges/n_nodes;
		deg.s_MD = G.getMaxInVertexDegrees();
		
		double s = 0.0;
		Collection<Double> degrees = new ArrayList<Double>();
		for (int u = 0; u < n_nodes; u++){
			int d = G.getVertexDegree(u);
			dist[d] += 1;
			degrees.add((double)d);
			s += (d - deg.s_AD)*(d - deg.s_AD);
		}
		deg.s_DV = s/n_nodes;
		
		// normalize
		for (int d = 0; d < n_nodes; d++){
			dist[d] = dist[d]/(2*n_edges);
		}
		
//		deg.s_CC = G.getClusteringCoefficient();		// Error: compile external sources
		deg.s_PL = Continuous.fit(degrees).fit().exponent();
		
		
		//
		return dist;
	}

		
	
	//// Kullback-Leibler distance
	public static double degreeDistributionKL(Grph G, Grph G0){
		int n_nodes = G.getNumberOfVertices();
		
		double[] dist = getDegreeDistr(G, new DegreeMetric());
		double[] dist0 = getDegreeDistr(G0, new DegreeMetric());
	
		//
		double KL = 0.0;
		for (int d = 0; d < n_nodes; d++){
			if (dist[d] != 0.0 && dist0[d] != 0)
				KL += dist[d]*Math.log(dist[d]/dist0[d]);
			
		}
		
		return KL;
	}
	
	////
	public static int cutQuery(Grph G, int[] S, int[] T){
		int c = 0;
		for (int u : S){
			IntSet s = G.getNeighbours(u);
			for (int v : T)
				if (s.contains(v))
					c += 1;
		}
		return c;
	}
	
	////
	public static double[] getDistanceDistr(Grph G, PathMetric path){
		int n_nodes = G.getNumberOfVertices();
		double[] dist = new double[n_nodes+1];
		
		// WAY-1: OutOfMemory on ca-HepPh and up
//		DistanceMatrix mat = G.getDistanceMatrix(null);
//		for (int u = 0; u < n_nodes; u++){
//			for (int v = u+1; v < n_nodes; v++){
//				int d = mat.get(u, v);
//				if (d == -1)
//					dist[n_nodes] += 1;
//				else
//					dist[d] += 1;
//			}
//		}
//		
//		double sum = n_nodes*(n_nodes-1)/2;
//		for (int d = 0; d < n_nodes+1; d++)
//			dist[d] = dist[d] / sum;

		// WAY-2
		BFSAlgorithm algo = new BFSAlgorithm();
		
		for (int u = 0; u < n_nodes; u++){
			SearchResult res = algo.compute(G, u);
			for (int d : res.distances)
				if (d == -1)
					dist[n_nodes] += 1;
				else if (d != 0)
					dist[d] += 1;
		}
		
		double sum = n_nodes*(n_nodes-1);	// not devided by 2
		for (int d = 0; d < n_nodes+1; d++)
			dist[d] = dist[d] / sum;		
		
		return dist;
	}
	
	////
	public static void generateCutQueries(Grph G, int n_queries, String cut_query_file, int size_limit) throws IOException{
		int n_nodes = G.getNumberOfVertices();
		
		int max_size = n_nodes/2-10 < size_limit ? n_nodes/2-10 : size_limit;
		
		Random random = new Random();
		List<Integer> perm = new ArrayList<Integer>();		// node permutation
		for (int i = 0; i < n_nodes; i++)
			perm.add(i);
			
		BufferedWriter bw = new BufferedWriter(new FileWriter(cut_query_file));
		for (int i = 0; i < n_queries; i++){
			int S_size = 10 + random.nextInt(max_size);
			int T_size = 10 + random.nextInt(max_size);
			int[] S = new int[S_size];
			int[] T = new int[T_size];
			
			Collections.shuffle(perm);		// random permutation
			for (int k = 0; k < S_size; k++)
				S[k] = perm.get(k);
			for (int k = 0; k < T_size; k++)
				T[k] = perm.get(k + S_size);
			
			//
			for (int s:S)
				bw.write(s + ",");
			bw.write("\n");
			for (int t:T)
				bw.write(t + ",");
			bw.write("\n");
			
		}
		bw.close();
	}
	
	////
	// read graph, query sets then compute utilities and write to MATLAB file
	public static void computeUtility(String graph_file, String cut_query_file, String matlab_file, int n_queries, int n_nodes) throws Exception{
//		GrphTextReader reader = new GrphTextReader();
		EdgeListReader reader = new EdgeListReader();
		
		Grph G;
		RegularFile f = new RegularFile(graph_file);
		
		G = reader.readGraph(f);
		for (int u = 0; u < n_nodes; u++)
			if (!G.containsVertex(u))
				G.addVertex(u);
		
		
		// 1. degree distribution
		DegreeMetric deg = new DegreeMetric();
		double[] deg_dist = getDegreeDistr(G, deg);
		
		// 2. distance distribution
		PathMetric path = new PathMetric();
		double[] distance_dist = getDistanceDistr(G, path);
		
		// 3. cut queries
		int[] cut_queries = new int[n_queries];
		BufferedReader br = new BufferedReader(new FileReader(cut_query_file));
		int count = 0;
		while (true){
			// S
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(",");
        	int[] S = new int[items.length];
        	for (int i = 0; i < items.length; i++)
        		S[i] = Integer.parseInt(items[i]);
        	
        	// T
        	str = br.readLine();
        	items = str.split(",");
        	int[] T = new int[items.length];
        	for (int i = 0; i < items.length; i++)
        		T[i] = Integer.parseInt(items[i]);
        	//
        	cut_queries[count++] = cutQuery(G, S, T);
        	
        }
		br.close();
		
		// write to MATLAB file
		MLDouble degArr = new MLDouble("degArr", deg_dist, 1);
		MLDouble distArr = new MLDouble("distArr", distance_dist, 1);
		MLInt32 cutArr = new MLInt32("cutArr", cut_queries, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(degArr); 
        towrite.add(distArr);
        towrite.add(cutArr);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
		
	}
	
	
	////
	private static void test(){
		// TOY GRAPH
		Grph G = new InMemoryGrph();
		G.addNVertices(7);
		for (int v = 0; v < 6; v++)
			G.addSimpleEdge(v, v+1, false);
		
		Grph G0 = new InMemoryGrph();
		G0.addNVertices(7);
		for (int v = 0; v < 6; v++)
			G0.addSimpleEdge(v, v+1, false);
		G0.addSimpleEdge(0, 6, false);
		
		// TEST degreeDistributionKL
		double KL1 = degreeDistributionKL(G, G0);
		double KL2 = degreeDistributionKL(G0, G);
		System.out.println("KL = " + KL1 + " " + KL2);
		
		// TEST cutQuery
		int[] S = new int[]{0,1,2};		// NOTE (u,u) has 1 edge
		int[] T = new int[]{3,4};
		System.out.println("cutQuery = " + cutQuery(G, S, T));
		
		// TEST getDistanceDistr
		double[] dist = getDistanceDistr(G, new PathMetric());
		for (double d : dist)
			System.out.print(d + " ");
	}
	
	////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
//		test();
		
		// load graph
//		String dataname = "polbooks";			// (105, 441)		
//		String dataname = "polblogs";			// (1224,16715) 	
//		String dataname = "as20graph";			// (6474,12572)		getDistanceDistr: 2.5s
//		String dataname = "wiki-Vote";			// (7115,100762) 	getDistanceDistr: 2.8s
//		String dataname = "ca-HepPh";			// (12006,118489) 	WAY-1: OutOfMemory,		WAY-2: 9.4s
		String dataname = "ca-AstroPh";			// (18771,198050) 	WAY-1: OutOfMemory, 	WAY-2: 25.6s
//		String dataname = "sm_50000_005_11";	// (50000,250000) 	WAY-1: OutOfMemory, 	WAY-2: 149s
//		String dataname = "sm_100000_005_11";	// (100000,500000) 	WAY-1: OutOfMemory, 	WAY-2: 805s
		
		
		String filename = "_data/" + dataname + ".grph";
		String edgelist_name = "_data/" + dataname + ".gr";
		String louvain_file = "_sample/" + dataname + "_louvain";	// by community detection algo
		String fit_file = "_sample/" + dataname + "_fit";    		// HRG
	    String mcmc_file = "_sample/" + dataname + "_mcmc_10_10";    		// MCMCInference
	    String cut_query_file = "_data/" + dataname + ".cut";
	    String matlab_file = "_matlab/" + dataname + ".mat";
	    
	    int n_queries = 1000;
	    
	    
	    //
		GrphTextReader reader = new GrphTextReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
//		long start = System.currentTimeMillis();
//		double[] dist = getDistanceDistr(G);
//	    System.out.println("getDistanceDistr - DONE, elapsed " + (System.currentTimeMillis() - start));
	 
		// TEST generateCutQueries()
		generateCutQueries(G, n_queries, cut_query_file, 500);
		System.out.println("generateCutQueries - DONE");
		
	    // TEST 
//	    DegreeMetric deg = new DegreeMetric();
//	    double[] dist = getDegreeDistr(G, deg);
//	    System.out.println("s_AD = " + deg.s_AD);
//	    System.out.println("s_MD = " + deg.s_MD);
//	    System.out.println("s_DV = " + deg.s_DV);
//	    System.out.println("s_PL = " + deg.s_PL);
//	    System.out.println("s_CC = " + deg.s_CC);
	    
	    
		// TEST computeUtility()
//	    int n_nodes = 6474;
//	    computeUtility(edgelist_name, cut_query_file, matlab_file, n_queries, n_nodes);
////	    computeUtility("_sample/as20graph_mcmc_10_10.0", cut_query_file, "_matlab/as20graph_mcmc_10_10.0.mat", n_queries, n_nodes);
	    
	}

}
