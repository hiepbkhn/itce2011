package dsn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import dp.DegreeMetric;
import dp.PathMetric;
import dp.UtilityMeasure;

public class Test {

	////
	public static void computeUtility(EdgeIntGraph aG, DegreeMetric deg, double[] deg_dist, PathMetric path, double[] distance_dist) throws IOException{
		if (aG.E() == 0)
			return;
		
		// 1. degree distribution
		System.arraycopy(UtilityMeasure.getDegreeDistr(aG, deg), 0, deg_dist, 0, aG.V());
		
		// 2. distance distribution
//			distance_dist = UtilityMeasure.getDistanceDistr(G, path);	// hyperANF
		
		long start = System.currentTimeMillis();
//		UnweightedGraph bG = new UnweightedGraph(aG);
//		System.arraycopy(UtilityMeasure.getDistanceDistr(bG, path), 0, distance_dist, 0, 50);	// BFS
//		System.out.println("BFS - DONE, elapsed " + (System.currentTimeMillis() - start));
		//
		double[] ret = UtilityMeasure.getDistanceDistr(aG, path);		// hyperANF
		if (ret.length <= 50)
			System.arraycopy(ret, 0, distance_dist, 0, ret.length);
		else
			System.arraycopy(ret, 0, distance_dist, 0, 50);
		System.out.println("max_dist = " + ret.length);	
		System.out.println("hyperANF - DONE, elapsed " + (System.currentTimeMillis() - start));
		
	}
	
	////
	public static void computeTrueGraph(EdgeIntGraph G, String matlab_file) throws IOException{
		int n_nodes = G.V();
		
		// compute utility
		DegreeMetric deg = new DegreeMetric();
		double[] deg_dist = new double[n_nodes]; 
		PathMetric path = new PathMetric();
		double[] distance_dist = new double[50];
		
		computeUtility(G, deg, deg_dist, path, distance_dist);
			
		
		// write to MATLAB
//		MLDouble degArr = new MLDouble("degArr", deg_dist, 1);
//		MLDouble distArr = new MLDouble("distArr", distance_dist, 1);
//		
//		MLDouble s_AD = new MLDouble("s_AD", new double[]{deg.s_AD}, 1);
//		MLDouble s_MD = new MLDouble("s_MD", new double[]{deg.s_MD}, 1);
//		MLDouble s_DV = new MLDouble("s_DV", new double[]{deg.s_DV}, 1);
//		MLDouble s_CC = new MLDouble("s_CC", new double[]{deg.s_CC}, 1);
//		MLDouble s_PL = new MLDouble("s_PL", new double[]{deg.s_PL}, 1);
//		
//		MLDouble s_APD = new MLDouble("s_APD", new double[]{path.s_APD}, 1);
//		MLDouble s_CL = new MLDouble("s_CL", new double[]{path.s_CL}, 1);
//		MLDouble s_EDiam = new MLDouble("s_EDiam", new double[]{path.s_EDiam}, 1);
//		MLDouble s_Diam = new MLDouble("s_Diam", new double[]{path.s_Diam}, 1);
//		
//        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
//        towrite.add(degArr); 
//        towrite.add(distArr);
//        towrite.add(s_AD);
//        towrite.add(s_MD);
//        towrite.add(s_DV);
//        towrite.add(s_CC);
//        towrite.add(s_PL);
//        towrite.add(s_APD);
//        towrite.add(s_CL);
//        towrite.add(s_EDiam);
//        towrite.add(s_Diam);
//        
//        new MatFileWriter(matlab_file, towrite );
//        System.out.println("Written to MATLAB file.");
		
	}
	
	////
	public static void computeLocalGraph(String sample_file, String matlab_file, int n_nodes) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(sample_file));
		
		String str = br.readLine();
		int k = Integer.parseInt(str);
		System.out.println("#selected nodes = " + k);
		
		// for MATLAB
		double[] a_AD = new double[k];
		double[] a_DV = new double[k];
		double[] a_MD = new double[k];
		double[] a_PL = new double[k];
		double[] a_CC = new double[k];
		
		double[] a_APD = new double[k];
		double[] a_CL = new double[k];
		double[] a_EDiam = new double[k];
		double[] a_Diam = new double[k];
		
		double[] a_degree = new double[k*n_nodes];
		double[] a_distance = new double[k*50];
		
		for(int i = 0; i < k; i++){
			str = br.readLine();
			String[] items = str.split(",");
			int u = Integer.parseInt(items[0]);
			int size = Integer.parseInt(items[1]);
			System.out.println("u = " + u + ", size = " + size);
			
			// build local graph
			EdgeIntGraph aG = new EdgeIntGraph(n_nodes); 
			for (int j = 0; j < size; j++){
				str = br.readLine();
				items = str.split("\t");
				int v = Integer.parseInt(items[0]);
				int w = Integer.parseInt(items[1]);
				
				aG.addEdge(new EdgeInt(v,w,1));
			}
			System.out.println("#nodes = " + aG.V());
			System.out.println("#edges = " + aG.E());
				
			// compute utility
			DegreeMetric deg = new DegreeMetric();
			double[] deg_dist = new double[n_nodes]; 
			PathMetric path = new PathMetric();
			double[] distance_dist = new double[50];
			
			computeUtility(aG, deg, deg_dist, path, distance_dist);
			
			a_AD[i] = deg.s_AD;
			a_DV[i] = deg.s_DV;
			a_MD[i] = deg.s_MD;
			a_PL[i] = deg.s_PL;
			a_CC[i] = deg.s_CC;
			
			a_APD[i] = path.s_APD;
			a_CL[i] = path.s_CL;
			a_EDiam[i] = path.s_EDiam;
			a_Diam[i] = path.s_Diam;
			
			for (int j = 0; j < n_nodes; j++)
				a_degree[i + j*k] = deg_dist[j];		// packed by column
			for (int j = 0; j < 50; j++)
				a_distance[i + j*k] = distance_dist[j];	// packed by column
		}
		br.close();
		
		// write to MATLAB
//		MLDouble degArr = new MLDouble("degArr", a_degree, k);
//		MLDouble distArr = new MLDouble("distArr", a_distance, k);
//		
//		MLDouble s_AD = new MLDouble("a_AD", a_AD, 1);
//		MLDouble s_MD = new MLDouble("a_MD", a_MD, 1);
//		MLDouble s_DV = new MLDouble("a_DV", a_DV, 1);
//		MLDouble s_CC = new MLDouble("a_CC", a_CC, 1);
//		MLDouble s_PL = new MLDouble("a_PL", a_PL, 1);
//		
//		MLDouble s_APD = new MLDouble("a_APD", a_APD, 1);
//		MLDouble s_CL = new MLDouble("a_CL", a_CL, 1);
//		MLDouble s_EDiam = new MLDouble("a_EDiam", a_EDiam, 1);
//		MLDouble s_Diam = new MLDouble("a_Diam", a_Diam, 1);
//		
//        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
//        towrite.add(degArr); 
//        towrite.add(distArr);
//        towrite.add(s_AD);
//        towrite.add(s_MD);
//        towrite.add(s_DV);
//        towrite.add(s_CC);
//        towrite.add(s_PL);
//        towrite.add(s_APD);
//        towrite.add(s_CL);
//        towrite.add(s_EDiam);
//        towrite.add(s_Diam);
//        
//        new MatFileWriter(matlab_file, towrite );
//        System.out.println("Written to MATLAB file.");
		
	}
	
	////
	public static void exportDegSeq(EdgeIntGraph G, String matlab_file) throws IOException{
		int n = G.V();
		double[] degSeq = new double[n];
		
		for (int u = 0; u < n; u++)
			degSeq[u] = G.degree(u);
		
		// save to MATLAB
		MLDouble degArr = new MLDouble("degSeq", degSeq, 1);
		
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(degArr); 
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		String prefix = "";
		
//		String dataname = "pl_1000_5_01";		// diameter = 5
//		String dataname = "pl_10000_5_01";		// diameter = 6,  Dup: round=3 (OutOfMem, 7GB ok), 98s (Acer)
												//				NoDup: round=3 (a=0.5, b=1.0, 4.5GB), 376s (Acer)
												//				NoDup: roudn=3 (a=1.0, b=1.0, 13GB), not run
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, NoDup: round=3 (5.1GB), 430s (Acer), 350s (PC), totalLink = 255633393
		
//		String dataname = "er_1000_001";		// diameter = 5
		String dataname = "er_10000_0001";		// diameter = 7, NoDup: round=3 (2.5GB), 23s (PC)
		
//		String dataname = "sm_1000_005_11";		// diameter = 9
//		String dataname = "sm_10000_005_11";	// diameter = 12, NoDup: round=3 (1.2GB), 5s (PC), round=4 (1.7GB), 12s (PC)
												// 						round=5 (3.0GB), 29s (PC), round=6 (3.3GB), 74s (PC)
		//
//		String dataname = "example";			// 	diameter = 5, 
//		String dataname = "karate";				// (34, 78)	diameter = 5
//		String dataname = "polbooks";			// (105, 441)			
//		String dataname = "polblogs";			// (1224,16715) 		
//		String dataname = "as20graph";			// (6474,12572)			
//		String dataname = "wiki-Vote";			// (7115,100762)		
//		String dataname = "ca-HepPh";			// (12006,118489) 		
//		String dataname = "ca-AstroPh";			// (18771,198050) 			
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	round=1 (11s), totalLink = 19354729
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
		
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		
		//
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());

//		computeTrueGraph(G, "_matlab/" + dataname + ".mat");
		
////		String sample_file = "_sample/er_10000_00006-nodup-10_0.50_1.00_100-7.out.0";
//		String sample_file = "_sample/er_10000_00006-nodup-10_0.25_0.50_100-9.out.0";
//		computeLocalGraph(sample_file, "", 10000);
		
		
		// TEST BitSet
//		BitSet a = new BitSet(10);
//		System.out.println("a.length = " + a.length());
//		System.out.println("a.size = " + a.size());
//		System.out.println("a.cardinality = " + a.cardinality());
//		a.set(1);	
//		a.set(5);
//		System.out.println("a.length = " + a.length());
//		System.out.println("a.size = " + a.size());
//		System.out.println("a.cardinality = " + a.cardinality());
		
		// TEST exportDegSeq()
		exportDegSeq(G, "_data/" + dataname + "_deg.mat");
	}

}
