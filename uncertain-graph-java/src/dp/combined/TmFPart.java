/* 
 * Sep 4
 * 	- copy TmF algo from naive.GreedyConstruct
 */

package dp.combined;

import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import hist.GraphIntDict;
import hist.GraphIntSet;
import hist.Int2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import naive.GreedyReconstruct;
import toools.io.file.RegularFile;
import dp.DPUtil;

public class TmFPart {

	
	//// nodelist[n], edgelist[2][m]
	public static int[][] tmfSubgraph(int[] nodelist, int[][] edgelist, double eps){
		int n = nodelist.length;
		int m = edgelist[0].length;
		int m_noisy = m + DPUtil.geometricMechanism(Math.exp(-1));

		int[][] result = new int[2][m_noisy];
		
		//
		double eps_threshold = Math.log((double)n*(n-1)/(2.0*m) - 1);			// 2.0 to avoid rounding, (double)
		double theta = 0;
		long n1 = 0;		// number of passed 1-cells
		long n2 = 0;		// number of passed 0-cells
		if (eps > eps_threshold){
			theta = 1/(2*eps)*Math.log((double)n*(n-1)/(2.0*m) - 1) + 0.5;		// 2.0 to avoid rounding
			n1 = Math.round(m/2 * (2-Math.exp(-eps*(1-theta))));	
		}else{
			theta = 1/eps*Math.log( ((double)n*(n-1)/2 + m*(Math.exp(eps) - 1)) / (2*m));
			n1 = Math.round(m/2 * Math.exp(-eps*(theta-1)));
		}
		n2 = m_noisy-n1;
		
//		// gamma = (m-1)/m
//		double upper_eps1 = Math.log((double)n*(n-1)*m/8 - (double)m*m/4);
//		// gamma = 0.9
//		double upper_eps2 = Math.log(((double)n*(n-1)/2 - m)/(0.04*m) );
//		System.out.println("upper_eps1 = " + upper_eps1);
//		System.out.println("upper_eps2 = " + upper_eps2);
		
		System.out.println("eps = " + eps);
		System.out.println("eps_threshold = " + eps_threshold);
		System.out.println("theta = " + theta);
		System.out.println("n1 = " + n1);
		
		// 1. for 1-cells
		int n1cells = 0;
		for (int i = 0; i < m; i++){
	    	int u = edgelist[0][i];
	       	int v = edgelist[1][i];
	       	if (1 + DPUtil.laplaceMechanism(eps) > theta){
	       		result[0][n1cells] = u;
	       		result[1][n1cells] = v;
	       		n1cells ++;
	       	}
		}
		System.out.println("n1cells = " + n1cells);
		
		// 2. for 0-cells
		GraphIntDict I = new GraphIntDict(nodelist, edgelist);
		
		int count = 0;
		Random random = new Random();
		while (count < m_noisy-n1cells){
			int u = nodelist[random.nextInt(n)];
			int v = nodelist[random.nextInt(n)];
			if (!I.hasEdge(u, v)){		// 0-cell
				result[0][n1cells+count] = u;
				result[1][n1cells+count] = v;
				count++;
			}
		}
		
		//
		return result;
	}
	
	//// bipartite graph (nodelist1, nodelist2)
	public static int[][] tmfBigraph(int[] nodelist1, int[] nodelist2, int[][] edgelist, double eps){
		int N1 = nodelist1.length;
		int N2 = nodelist2.length;
		int m = edgelist[0].length;
		int m_noisy = m + DPUtil.geometricMechanism(Math.exp(-1));

		int[][] result = new int[2][m_noisy];
		
		//
		double eps_threshold = Math.log((double)N1*N2/m - 1);			// 2.0 to avoid rounding, (double)
		double theta = 0;
		long n1 = 0;		// number of passed 1-cells
		long n2 = 0;		// number of passed 0-cells
		if (eps > eps_threshold){
			theta = 1/(2*eps)*Math.log((double)N1*N2/m - 1) + 0.5;		// 2.0 to avoid rounding
			n1 = Math.round(m/2 * (2-Math.exp(-eps*(1-theta))));	
		}else{
			theta = 1/eps*Math.log( ((double)N1*N2 + m*(Math.exp(eps) - 1)) / m);
			n1 = Math.round(m/2 * Math.exp(-eps*(theta-1)));
		}
		n2 = m_noisy-n1;
		
		// 1. for 1-cells
		int n1cells = 0;
		for (int i = 0; i < m; i++){
	    	int u = edgelist[0][i];
	       	int v = edgelist[1][i];
	       	if (1 + DPUtil.laplaceMechanism(eps) > theta){
	       		result[0][n1cells] = u;
	       		result[1][n1cells] = v;
	       		n1cells ++;
	       	}
		}
		System.out.println("n1cells = " + n1cells);
		
		// 2. for 0-cells
		int N = N1 + N2;
		int[] nodelist = new int[N];
		for (int i = 0; i < N1; i++)
			nodelist[i] = nodelist1[i];
		for (int i = 0; i < N2; i++)
			nodelist[N1+i] = nodelist2[i];
		
		GraphIntDict I = new GraphIntDict(nodelist, edgelist);
		
		int count = 0;
		Random random = new Random();
		while (count < m_noisy-n1cells){
			int u = nodelist1[random.nextInt(N1)];
			int v = nodelist2[random.nextInt(N2)];
			if (!I.hasEdge(u, v)){		// 0-cell
				result[0][n1cells+count] = u;
				result[1][n1cells+count] = v;
				count++;
			}
		}
				
		//
		return result;
	}
	
	
	////
	public static List<int[]> partitionNodeEqual(int n, int k){
		List<int[]> nodelists = new ArrayList<int[]>();
		
		for (int i = 0; i < k-1; i++){
			nodelists.add(new int[n/k]);
			for (int j = 0; j < n/k; j++)
				nodelists.get(i)[j] = i*(n/k) + j;
		}
		// last partition
		int m = n-(k-1)*(n/k);
		nodelists.add(new int[m]);
		for (int j = 0; j < m; j++)
			nodelists.get(k-1)[j] = (k-1)*(n/k) + j;
		
		//
		return nodelists;
	}
	
	//// 
	public static int[][] getEdgeSubgraph(Grph G, int[] nodelist){
		List<Int2> edges = new ArrayList<Int2>();
		int n = nodelist.length;
		for (int i = 0; i < n; i++)
			for (int j = i+1; j < n; j++)
				if (G.areVerticesAdjacent(nodelist[i], nodelist[j]))
					edges.add(new Int2(nodelist[i], nodelist[j]));
		
		
		int[][] result = new int[2][edges.size()];
		int i = 0;
		for (Int2 p:edges){
			result[0][i] = p.val0;
			result[1][i] = p.val1;
			i ++;
		}
		
		//
		return result;
		
	}
	
	////
	public static int[][] getEdgeBigraph(Grph G, int[] nodelist1, int[] nodelist2){
		List<Int2> edges = new ArrayList<Int2>();
		int n1 = nodelist1.length;
		int n2 = nodelist2.length;
		for (int i = 0; i < n1; i++)
			for (int j = 0; j < n2; j++)
				if (G.areVerticesAdjacent(nodelist1[i], nodelist2[j]))
					edges.add(new Int2(nodelist1[i], nodelist2[j]));
		
		
		int[][] result = new int[2][edges.size()];
		int i = 0;
		for (Int2 p : edges){
			result[0][i] = p.val0;
			result[1][i] = p.val1;
			i++;
		}
		
		//
		return result;
		
	}
	
	//// k: number of nodesets
	public static Grph filterEqual(Grph G, int k, double eps){
		int n = G.getNumberOfVertices();
		
		Grph aG = new InMemoryGrph();
		aG.addNVertices((int)n);
		
		List<int[]> nodelists = partitionNodeEqual(n, k);
		
		for (int i = 0; i < k; i++){
			// subgraph (i,i)
			int[] nodelist = nodelists.get(i);
			int[][] edgelist = getEdgeSubgraph(G, nodelist);
			
			int[][] result = tmfSubgraph(nodelist, edgelist, eps); 
			System.out.println("result[0].length = " + result[0].length);
			
			for (int t = 0; t < result[0].length; t++)
				aG.addSimpleEdge(result[0][t], result[1][t], false);
			
			// bipartite graphs (i,j)
			for (int j = i+1; j < k; j++){
				int[] nodelist1 = nodelists.get(i);
				int[] nodelist2 = nodelists.get(j);
				edgelist = getEdgeBigraph(G, nodelist1, nodelist2);
			
				result = tmfBigraph(nodelist1, nodelist2, edgelist, eps); 
				System.out.println("result[0].length = " + result[0].length);
				
				for (int t = 0; t < result[0].length; t++)
					aG.addSimpleEdge(result[0][t], result[1][t], false);
			}
				
		}
		
		//
		return aG;
	}
	
	
	////
	public static void main(String[] args) throws Exception{
		// load graph
		String dataname = "polbooks";		// (105, 441)		
																
		
//		String dataname = "polblogs";		// (1224,16715) 	
																
		
//		String dataname = "as20graph";		// (6474,12572)		
																		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872) 
//		String dataname = "com_dblp_ungraph";		// (,) 
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
													//						
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix = "";
	    int n_samples = 1;
	    double eps = 1.0;
	    int numpart = 5;
		
		if(args.length >= 4){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			eps = Double.parseDouble(args[3]);
		}
		System.out.println("dataname = " + dataname);
		
		System.out.println("n_samples = " + n_samples);
		System.out.println("eps_c = " + eps);
		
		String filename = prefix + "_data/" + dataname + ".gr";
		String sample_file = prefix + "_sample/" + dataname + "_tmfpart_" + String.format("%.1f", eps);
		System.out.println("sample_file = " + sample_file);

		
//		GrphTextReader reader = new GrphTextReader();
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		long start = System.currentTimeMillis();
		G = reader.readGraph(f);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());

		// TEST filterEqual()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
		
			start = System.currentTimeMillis();
			Grph aG = filterEqual(G, numpart, eps);
			System.out.println("filterEqual - DONE, elapsed " + (System.currentTimeMillis() - start));
			System.out.println("#nodes = " + aG.getNumberOfVertices());
			System.out.println("#edges = " + aG.getNumberOfEdges());
			
			start = System.currentTimeMillis();
			System.out.println("edit distance (aG, G) = " + GreedyReconstruct.editScore(aG, G));
			System.out.println("editScore - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(aG, f);
		
		}

	}

}
