/*
 * Mar 25, 2015
 * Apr 22
 * 	- add normalizeGraph()
 * Nov 17
 * 	- add testCallPython()
 * 	- add bfsSamples(): shortest paths from 1000 nodes (converted from Python)
 * 	- globalClusteringCoeff()
 * 	- hyperANF(): use loadGraph()
 * Nov 18
 * 	- use EdgeIntGraph in place of Grph
 * May 30, 2016
 * 	- faster globalClusteringCoeff(): see http://theory.stanford.edu/~tim/s14/l/l1.pdf
 * Jun 6
 * 	- fix getDegreeDistr()
 */
package dp;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.HyperBall;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

//import org.python.core.PyInteger;
//import org.python.core.PyObject;
//import org.python.core.PyString;
//import org.python.util.PythonInterpreter;

import nl.peterbloem.powerlaws.Continuous;
import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;

import com.carrotsearch.hppc.cursors.IntCursor;
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
	
	public static int INF = 100000000;
	
	////
	public static double[] getDegreeDistr(EdgeIntGraph G, DegreeMetric deg){
		int n_nodes = G.V();
		int n_edges = G.E();
		
		double[] dist = new double[n_nodes];
		
		deg.s_AD = 2.0*n_edges/n_nodes;
		
		double s = 0.0;
		int max_deg = 0;
		Collection<Double> degrees = new ArrayList<Double>();
		for (int u = 0; u < n_nodes; u++){
			int d = G.degree(u);
			if (max_deg < d)
				max_deg = d;
			
			dist[d] += 1;
			degrees.add((double)d);
			s += (d - deg.s_AD)*(d - deg.s_AD);
		}
		deg.s_DV = s/n_nodes;
		deg.s_MD = max_deg;
		
		// normalize
		for (int d = 0; d < n_nodes; d++){
//			dist[d] = dist[d]/(2*n_edges);		// WRONG
			dist[d] = dist[d]/n_nodes;
		}
		
//		deg.s_CC = G.getClusteringCoefficient();		// Error: compile external sources
		deg.s_CC = globalClusteringCoeff(G);
		deg.s_PL = Continuous.fit(degrees).fit().exponent();
		
		
		//
		return dist;
	}

		
	
	//// Kullback-Leibler distance
	public static double degreeDistributionKL(EdgeIntGraph G, EdgeIntGraph G0){
		int n_nodes = G.V();
		
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
	public static int cutQuery(EdgeIntGraph G, int[] S, int[] T){
		int c = 0;
		for (int u : S){
			for (int v : T)
				if (G.areEdgesAdjacent(u, v))
					c += 1;
		}
		return c;
	}
	
	////
	public static double[] getDistanceDistr(EdgeIntGraph G, PathMetric path) throws IOException{
		int n_nodes = G.V();
//		double[] dist = new double[n_nodes+1];
		
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

		// WAY-2: BFSAlgorithm
//		BFSAlgorithm algo = new BFSAlgorithm();
//		
//		for (int u = 0; u < n_nodes; u++){
//			SearchResult res = algo.compute(G, u);
//			for (int d : res.distances)
//				if (d == -1)
//					dist[n_nodes] += 1;
//				else if (d != 0)
//					dist[d] += 1;
//		}
//		
//		double sum = n_nodes*(n_nodes-1);	// not divided by 2
//		for (int d = 0; d < n_nodes+1; d++)
//			dist[d] = dist[d] / sum;		
		
		
		// WAY-3: call hyperANF()
		double[] dist = hyperANF(G);					// NOTE: dist[i] num of path lengths <= i
		
		double[] dist_list = new double[dist.length];	// dist_list[i] num of path lengths == i
		for (int i = dist.length-1; i > 0; i--)
			dist_list[i] = dist[i] - dist[i-1];
		dist_list[0] = dist[0];
		
		double sum_APD = 0.0;
		for (int i = 0; i < dist_list.length; i++)
			sum_APD += (i+1) * dist_list[i];			// NOTE: i+1

		double num_APD = dist[dist.length - 1];
		
		for (int i = 0; i < dist.length; i++)
	        if (dist[i] >= 0.9 * num_APD){
	            path.s_EDiam = i;
	            break;
	        }
		path.s_APD = sum_APD/num_APD;
		path.s_Diam = dist.length;
		
		double sum_CL = 0.0;
		for (int i = 0; i < dist_list.length; i++)
            sum_CL += dist_list[i]/(i+1);
	    path.s_CL = num_APD/sum_CL; 
	    
	    return dist_list;
		    
		
		
	}
	
	//// n_nodes < 20000
	public static double[] getDistanceDistr(UnweightedGraph G, PathMetric path) throws IOException{
		int n_nodes = G.V();
		    
		double[] dist_list = bfsSamples(G, n_nodes);	// full BFS
		
		path.s_Diam = dist_list[0];		// see the end of bfsSamples()
		
		double sum_APD = 0.0;
		double num_APD = 0.0;
		
		for (int i = 1; i <= path.s_Diam ; i++){
			sum_APD += i * dist_list[i];
			num_APD += dist_list[i];
		}
		
		double sum_dist = 0.0;
		for (int i = 1; i <= path.s_Diam; i++){
			sum_dist += dist_list[i];
	        if (sum_dist >= 0.9 * num_APD){
	            path.s_EDiam = i;
	            break;
	        }
		}
		path.s_APD = sum_APD/num_APD;
		
		double sum_CL = 0.0;
		for (int i = 1; i <= path.s_Diam ; i++)
            sum_CL += dist_list[i]/i;
	    path.s_CL = num_APD/sum_CL; 
		
		return dist_list;
		
		
	}
	
	//// converted from utility_measure.bfs_sample()
	public static double[] bfsSamples(UnweightedGraph G, int n_samples){

		double[] dist_list = new double[50];
		
		int n_nodes = G.V();
		int[] node_list = new int[n_nodes];
		
		// get a random permutation
		for (int u = 0; u < n_nodes; u++)
			node_list[u] = u;
		Random random = new Random();
		for (int i = 0; i < n_nodes; i++) {
			int randIdx = random.nextInt(n_nodes);
			
			int tmp = node_list[randIdx];
			node_list[randIdx] = node_list[i];
			node_list[i] = tmp;
		}
		
		int[] v_list = Arrays.copyOfRange(node_list, 0, n_samples);
		System.out.println("v_list.length = " + v_list.length);
		
		// BFS
		boolean[] marked = new boolean[n_nodes];
		int[] dist = new int[n_nodes];
		int max_dist = -1;
		
		int[] queue = new int[n_nodes];
		int head = 0;
		int tail = 0;
		
		for (int v : v_list){
			for (int u = 0; u < n_nodes; u++){
				dist[u] = INF;	// init
				marked[u] = false;
			}
			queue[0] = v;
			head = 0;
			tail = 0;
			dist[v] = 0;
			marked[v] = true;
			
			// queue: int array (a bit faster, 1/3 memory consumption)
			while (head <= tail){
				int u = queue[head];
				head++;
				
				for (int t : G.adj(u))
					if (! marked[t]){
						dist[t] = dist[u] + 1;
						marked[t] = true;
						tail++;
						queue[tail] = t;
					}
			}
			
			for (int u = 0; u < n_nodes; u++){
				if (dist[u] < INF)
					dist_list[dist[u]] += 1;
						
				if (max_dist < dist[u] && dist[u] != INF)
					max_dist = dist[u];
			}
		}
		
		dist_list[0] = max_dist;			// IMPORTANT !
		
		System.out.println("max_dist = " + max_dist);
		
		//
		return dist_list;
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
	public static double globalClusteringCoeff(EdgeIntGraph G){
		double ret = 0.0;
		
		int n_nodes = G.V();
		double triples = 0.0;
		double triangles = 0.0;
		
		//
//		for (int u = 0; u < n_nodes; u++){
//			Set<Integer> nb = G.adj(u).keySet();
//			
//			triples += nb.size() * (nb.size()-1)/2;
//			
//			for (int v : nb)
//				for (int t : nb)
//					if (v > u && t > v && G.areEdgesAdjacent(v, t))
//						triangles += 1;
//		}
//		ret = 3*triangles/triples;
		
		// FASTER (youtute: 60s -> 47s)
//		long start = System.currentTimeMillis();
		int[] deg = new int[n_nodes];
		for (int u = 0; u < n_nodes; u++)
			deg[u] = G.degree(u);
		for (int u = 0; u < n_nodes; u++){
			Set<Integer> nb = G.adj(u).keySet();
			
			triples += nb.size() * (nb.size()-1)/2;
			
			for (int v : nb)
				for (int t : nb)
					if (t > v){
						if (deg[v] > deg[u] && deg[t] > deg[u] && G.areEdgesAdjacent(v, t))
							triangles += 1;
						else if (deg[v] == deg[u] && deg[t] > deg[u] && v > u && G.areEdgesAdjacent(v, t))
							triangles += 1;
						else if (deg[v] > deg[u] && deg[t] == deg[u] && t > u && G.areEdgesAdjacent(v, t))
							triangles += 1;
						else if (deg[v] == deg[u] && deg[t] == deg[u] && v > u && t > v && G.areEdgesAdjacent(v, t))
							triangles += 1;
				}
		}
		ret = 3*triangles/triples;
//		System.out.println("globalClusteringCoeff, elapsed " + (System.currentTimeMillis() - start));
				
		//
		return ret;
	}
	
	//// used in hyperANF
	public static byte[] loadGraph(EdgeIntGraph G) throws IOException{
		
		StringBuilder ret = new StringBuilder();
		for (int u = 0; u < G.V(); u++)
			for (int v : G.adj(u).keySet()){
			ret.append(u + " " + v +"\n");
			
		}
		
		//
		return ret.toString().getBytes("ASCII");
	}
	
	////
	public static double[] hyperANF(EdgeIntGraph G) throws IOException{
		int log2m = 8;
		
		ArcListASCIIGraph ag = ArcListASCIIGraph.loadOnce(new FastByteArrayInputStream(loadGraph(G)) );	
		ImmutableGraph g = new ArrayListMutableGraph( ag ).immutableView();
		
		
		long start = System.currentTimeMillis();
		int numberOfThreads = 1;		// run 1 core
		HyperBall hyperBall = new HyperBall( g, null, log2m, null, numberOfThreads, 10, 10, false, false, false, null, 1);
		hyperBall.init();
		do {
			hyperBall.iterate();
			final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
		} while( hyperBall.modified() != 0 );
		System.out.println("hyperBall - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		
		double[] ret = hyperBall.neighbourhoodFunction.toDoubleArray();
		
		hyperBall.close();
		
		//
		return ret;
		
	}
	
	////
	// read graph, query sets then compute utilities and write to MATLAB file
	public static void computeUtility(String graph_file, String cut_query_file, String matlab_file, int n_queries, int n_nodes) throws Exception{

		EdgeIntGraph G = EdgeIntGraph.readEdgeListWithNodes(graph_file, "\t", n_nodes);
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		// 1. degree distribution
		DegreeMetric deg = new DegreeMetric();
		double[] deg_dist = getDegreeDistr(G, deg);
		
		System.out.println("s_AD = " + deg.s_AD);
		System.out.println("s_MD = " + deg.s_MD);
		System.out.println("s_DV = " + deg.s_DV);
		System.out.println("s_CC = " + deg.s_CC);
		System.out.println("s_PL = " + deg.s_PL);
		
		// 2. distance distribution
		PathMetric path = new PathMetric();
		double[] distance_dist;
		if (G.V() > 20000){
			System.out.println("HyperANF");
			distance_dist = getDistanceDistr(G, path);
		}else{
			System.out.println("full BFS");
			UnweightedGraph aG = UnweightedGraph.readEdgeListWithNodes(graph_file, "\t", n_nodes);
			System.out.println("#nodes = " + aG.V());
			System.out.println("#edges = " + aG.E());
			distance_dist = getDistanceDistr(aG, path);
		}
		
		System.out.println("s_APD = " + path.s_APD);
		System.out.println("s_CL = " + path.s_CL);
		System.out.println("s_EDiam = " + path.s_EDiam);
		System.out.println("s_Diam = " + path.s_Diam);
		
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
		
		MLDouble s_AD = new MLDouble("s_AD", new double[]{deg.s_AD}, 1);
		MLDouble s_MD = new MLDouble("s_MD", new double[]{deg.s_MD}, 1);
		MLDouble s_DV = new MLDouble("s_DV", new double[]{deg.s_DV}, 1);
		MLDouble s_CC = new MLDouble("s_CC", new double[]{deg.s_CC}, 1);
		MLDouble s_PL = new MLDouble("s_PL", new double[]{deg.s_PL}, 1);
		
		MLDouble s_APD = new MLDouble("s_APD", new double[]{path.s_APD}, 1);
		MLDouble s_CL = new MLDouble("s_CL", new double[]{path.s_CL}, 1);
		MLDouble s_EDiam = new MLDouble("s_EDiam", new double[]{path.s_EDiam}, 1);
		MLDouble s_Diam = new MLDouble("s_Diam", new double[]{path.s_Diam}, 1);
		
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(degArr); 
        towrite.add(distArr);
        towrite.add(cutArr);
        towrite.add(s_AD);
        towrite.add(s_MD);
        towrite.add(s_DV);
        towrite.add(s_CC);
        towrite.add(s_PL);
        towrite.add(s_APD);
        towrite.add(s_CL);
        towrite.add(s_EDiam);
        towrite.add(s_Diam);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
		
	}
	
	
	////
	private static void test(){
		// TOY GRAPH
//		Grph G = new InMemoryGrph();
//		G.addNVertices(7);
//		for (int v = 0; v < 6; v++)
//			G.addSimpleEdge(v, v+1, false);
//		
//		Grph G0 = new InMemoryGrph();
//		G0.addNVertices(7);
//		for (int v = 0; v < 6; v++)
//			G0.addSimpleEdge(v, v+1, false);
//		G0.addSimpleEdge(0, 6, false);
//		
//		// TEST degreeDistributionKL
//		double KL1 = degreeDistributionKL(G, G0);
//		double KL2 = degreeDistributionKL(G0, G);
//		System.out.println("KL = " + KL1 + " " + KL2);
//		
//		// TEST cutQuery
//		int[] S = new int[]{0,1,2};		// NOTE (u,u) has 1 edge
//		int[] T = new int[]{3,4};
//		System.out.println("cutQuery = " + cutQuery(G, S, T));
//		
//		// TEST getDistanceDistr
//		double[] dist = getDistanceDistr(G, new PathMetric());
//		for (double d : dist)
//			System.out.print(d + " ");
	}
	
	//// normalize to 0-based and consecutive ids
	public static void normalizeGraph(String filename, int nEdges, String out_file) throws Exception{
		
		int[][] edges = new int[nEdges][2];
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		int count = 0;
		int nodeId = 0;
		HashMap<Integer, Integer> node_dict = new HashMap<Integer, Integer>();
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	if (str.charAt(0) == '#')
        		continue;
        	String[] items = str.split("\t");
        	edges[count][0] = Integer.parseInt(items[0]);
        	edges[count][1] = Integer.parseInt(items[1]);
        	
        	if (!node_dict.containsKey(edges[count][0])){
        		node_dict.put(edges[count][0], nodeId);
        		nodeId ++;
        	}
        	if (!node_dict.containsKey(edges[count][1])){
        		node_dict.put(edges[count][1], nodeId);
        		nodeId ++;
        	}
        	
        	count++;
        	
        }
		br.close();
		
		// update node ids
		for (int i = 0; i < nEdges; i++){
			edges[i][0] = node_dict.get(edges[i][0]);
			edges[i][1] = node_dict.get(edges[i][1]);
		}
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(out_file));
		for (int i = 0; i < nEdges; i++)
			bw.write(edges[i][0] + " " + edges[i][1] + "\n");
		
		bw.close();
		
	}
	
	////
//	public static void testCallPython(){
//		PythonInterpreter pi = new PythonInterpreter();
//		pi.set("integer", new PyInteger(42));
//		pi.exec("square = integer*integer");
//		PyInteger square = (PyInteger) pi.get("square");
//		System.out.println("square: " + square.asInt());
//		pi.close();
//		
//	}
	
	////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
//		test();
		
		// load graph
//		String dataname = "polbooks";			// (105, 441)		
//		String dataname = "polblogs";			// (1224,16715) 	
//		String dataname = "as20graph";			// (6474,12572)		getDistanceDistr: 2.5s
//		String dataname = "wiki-Vote";			// (7115,100762) 	getDistanceDistr: 2.8s
//		String dataname = "ca-HepPh";			// (12006,118489) 	WAY-1: OutOfMemory,		WAY-2: 9.4s
//		String dataname = "ca-AstroPh";			// (18771,198050) 	WAY-1: OutOfMemory, 	WAY-2: 25.6s
//		String dataname = "sm_50000_005_11";	// (50000,250000) 	WAY-1: OutOfMemory, 	WAY-2: 149s
//		String dataname = "sm_100000_005_11";	// (100000,500000) 	WAY-1: OutOfMemory, 	WAY-2: 805s
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 
		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624)	bfsSamples 1000 nodes (UnweightedGraph: 210s, 1.7GB) (int queue: 200s, 0.6GB)
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 	
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 	bfsSamples 1000 nodes (Grph: 131s), (UnweightedGraph: 5.6s)
		
		String filename = "_data/" + dataname + ".gr";
		String louvain_file = "_sample/" + dataname + "_louvain";	// by community detection algo
		String fit_file = "_sample/" + dataname + "_fit";    		// HRG
	    String mcmc_file = "_sample/" + dataname + "_mcmc_10_10";    		// MCMCInference
	    
	    
	    
	    int n_queries = 1000;
	    
	    // COMMAND-LINE
 		String prefix = "";
 	    int n_samples = 20;
 	    String graph_name = "";
 	    int n_nodes = 0;
 	   
 		if(args.length >= 5){
 			prefix = args[0];
 			dataname = args[1];
 			n_samples = Integer.parseInt(args[2]);
 			graph_name = args[3];
 			n_nodes = Integer.parseInt(args[4]);
 		
	 		System.out.println("dataname = " + dataname);
	 		System.out.println("graph_name = " + graph_name);
	 		
	 		String cut_query_file = prefix + "_data/" + dataname + ".cut";
	 		String graph_file = prefix + "_sample/" + graph_name;
	 		String matlab_file = prefix + "_matlab/" + graph_name;
		    
	 		for (int i = 0; i < n_samples; i++){
		    	System.out.println("sample i = " + i);
		    	
				
				long start = System.currentTimeMillis();
			    computeUtility(graph_file + "." + i, cut_query_file, matlab_file + "." + i + ".mat", n_queries, n_nodes);
			    System.out.println("computeUtility - DONE, elapsed " + (System.currentTimeMillis() - start));
	 		}
 		}else{	// run on true graph (3 arguments)
 			prefix = args[0];
 			dataname = args[1];
 			n_nodes = Integer.parseInt(args[2]);
 			
 			System.out.println("dataname = " + dataname);
 			String graph_file = prefix + "_data/" + dataname + ".gr";
	 		String matlab_file = prefix + "_matlab/" + dataname + ".mat";
 			String cut_query_file = prefix + "_data/" + dataname + ".cut";
 			
 			long start = System.currentTimeMillis();
		    computeUtility(graph_file, cut_query_file, matlab_file, n_queries, n_nodes);
		    System.out.println("computeUtility - DONE, elapsed " + (System.currentTimeMillis() - start));
 		}
 		
	    
	    //
//	    testCallPython();
	    
	    //
//		EdgeListReader reader = new EdgeListReader();
//		Grph G;
//		RegularFile f = new RegularFile(filename);
//		
//		G = reader.readGraph(f);
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
		
	    
		//
//		long start = System.currentTimeMillis();
//		double[] dist = getDistanceDistr(G);
//	    System.out.println("getDistanceDistr - DONE, elapsed " + (System.currentTimeMillis() - start));

		// TEST bfsSamples
//	    UnweightedGraph G = UnweightedGraph.readEdgeListWithNodes(filename, "\t", 17903);
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//		long start = System.currentTimeMillis();
//		bfsSamples(G, 17903);
//	    System.out.println("bfsSamples - DONE, elapsed " + (System.currentTimeMillis() - start));

		// TEST generateCutQueries()
//		generateCutQueries(G, n_queries, cut_query_file, 500);
//		System.out.println("generateCutQueries - DONE");
		
	    // TEST 
//	    String graph_file = "_data/" + dataname + ".gr";
//	    EdgeIntGraph G = EdgeIntGraph.readEdgeListWithNodes(graph_file, "\t", 317080);
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//	    DegreeMetric deg = new DegreeMetric();
//	    double[] dist = getDegreeDistr(G, deg);
//	    System.out.println("s_AD = " + deg.s_AD);
//	    System.out.println("s_MD = " + deg.s_MD);
//	    System.out.println("s_DV = " + deg.s_DV);
//	    System.out.println("s_PL = " + deg.s_PL);
//	    System.out.println("s_CC = " + deg.s_CC);
	    
	    
		// TEST computeUtility()
//	    long start = System.currentTimeMillis();
//	    computeUtility(filename, cut_query_file, "_matlab/as20graph_mcmc_10_10.0.mat", n_queries);
//	    System.out.println("computeUtility - DONE, elapsed " + (System.currentTimeMillis() - start));
	    
	    // TEST normalizeGraph()
//	    String file_name = "E:/Tailieu/Paper-code/DATA-SET/SNAP/Networks with ground-truth communities/com-lj.ungraph.txt";		// mem 2.4GB, 47s
//	    String out_file = "E:/Tailieu/Paper-code/DATA-SET/SNAP/Networks with ground-truth communities/com_lj_ungraph.gr";
//	    long start = System.currentTimeMillis();
//	    normalizeGraph(file_name, 34681189, out_file);
//	    System.out.println("normalizeGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
	}

}
