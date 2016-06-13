/*
 * papers 
 *  - "Privacy via the Johnson-Lindenstrauss Transform" (2012)
 *  - "On Randomness Measures for Social Networks" (SDM'08)
 * May 19, 2016
 * 	- distCosine() for paper SDM'08
 * 	- 
 */

package dp.combined;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSparse;

import algs4.EdgeWeightedGraph;

public class RandomProjection {

	
	////
	public static double distL2(double[] a, double[] b){
		double sum = 0.0;
		for (int i = 0; i < a.length; i++)
			sum += (a[i] - b[i])*(a[i] - b[i]);
		//
		return sum;		// not sqrt()
	}
	
	////
	public static double distL1(double[] a, double[] b){
		double sum = 0.0;
		for (int i = 0; i < a.length; i++)
			sum += Math.abs(a[i] - b[i]);
		//
		return sum;	
	}
	
	////
	public static double distCosine(double[] a, double[] b){
		double sum = 0.0;
		double sum_a = 0.0;
		double sum_b = 0.0;
		for (int i = 0; i < a.length; i++){
			sum += a[i] * b[i];
			sum_a += a[i]*a[i];
			sum_b += b[i]*b[i];
		}
		
		//
		return -sum/Math.sqrt(sum_a * sum_b);		// negative
	}
	
	public static double dist(double[] a, double[] b, int dist_type){
		switch (dist_type){
			case 1:
				return distL2(a,b);
			case 2:
				return distL1(a,b);
			case 3:
				return distCosine(a,b);
			default:
				return -1;
		}
	}
	
	//// k-means to c clusters
	public static int[] kMeans(double[][] A2, int c, int steps, int dist_type){
		int n = A2.length;
		int k = A2[0].length;
		System.out.println("n = " + n + " k = " + k);
		System.out.println("c = " + c + " steps = " + steps);
		
		// 1 - init
		double[][] centers = new double[c][k];
		double[] min = new double[k];
		double[] max = new double[k];
		for (int j = 0; j < k; j++){
			min[j] = 10000000;
			max[j] = -10000000;
			for (int i = 0; i < n; i++){
				if (A2[i][j] < min[j])
					min[j] = A2[i][j];
				if (A2[i][j] > max[j])
					max[j] = A2[i][j];
			}
		}
		// debug
//		for (int j = 0; j < k; j++)
//			System.out.println(j + ": " + String.format("%.1f",min[j]) + "\t" + String.format("%.1f",max[j]));
		
		
		// equal-distance initial centers
		for (int i = 0; i < c; i++){
			for (int j = 0; j < k; j++){
				centers[i][j] = min[j] + (i+1)*(max[j] - min[j])/(c+1);
//				System.out.print(String.format("%.1f",centers[i][j]) + " ");
			}
//			System.out.println();
		}
		
		// 2 - loop
		int[] ret = new int[n];		// ret[i] = c -> node i in cluster c
		
		// find nearest center
		for (int i = 0; i < n; i++){
			int nearest = 0;
			double dist = dist(A2[i], centers[nearest], dist_type);
			
			for (int ic = 0; ic < c; ic++){
				double temp = dist(A2[i], centers[ic], dist_type);
				if (dist > temp){
					dist = temp;
					nearest = ic;
				}
			}
			ret[i] = nearest;
		}
		
		// update centers[]
		double[][] sum_c = new double[c][k];
		int[] n_c = new int[c];
		
		for (int i = 0; i < n; i++){
			n_c[ret[i]] += 1;
			for (int j = 0; j < k; j++)
				sum_c[ret[i]][j] += A2[i][j];
		}
		
		for (int i = 0; i < c; i++)
			for (int j = 0; j < k; j++){
				centers[i][j] = sum_c[i][j] / n_c[i];		// n_c[i] == 0 ?
		}
		
		//
		for (int t = 0; t < steps; t++){
			
			// find nearest center
			for (int i = 0; i < n; i++){
				int nearest = ret[i];
				double dist = dist(A2[i], centers[nearest], dist_type);
				
				for (int ic = 0; ic < c; ic++){
					double temp = dist(A2[i], centers[ic], dist_type);
					if (dist > temp){
						dist = temp;
						nearest = ic;
					}
				}
				ret[i] = nearest;
			}
			
			// debug
//			System.out.println(t + " : ret = ");
//			for (int i = 0; i < 50; i++)
//				System.out.println(ret[i]);
			
			// update centers[]
			sum_c = new double[c][k];
			n_c = new int[c];
			
			for (int i = 0; i < n; i++){
				n_c[ret[i]] += 1;
				for (int j = 0; j < k; j++)
					sum_c[ret[i]][j] += A2[i][j];
					
			}
			
			for (int i = 0; i < c; i++)
				for (int j = 0; j < k; j++){
					centers[i][j] = sum_c[i][j] / n_c[i];		// n_c[i] == 0 ?
			}
		}
		
		//
		return ret; 
	}
	
	
	////
	public static void privateProjection(EdgeWeightedGraph G, int k, double eps, double delta, int c, int steps, int dist_type){
		int n = G.V();
		
		// 0 - check privacy conditions
		double k_lower = 2*(Math.log(n) + Math.log(2/delta));
		System.out.println("k_lower = " + k_lower);
		
		double eps_upper = Math.log(1/delta);
		System.out.println("eps_upper = " + eps_upper);
		
		
		if (k < k_lower){
			System.err.println("Wrong k parameter !");
			return;
		}
				
		if (eps > eps_upper){
			System.err.println("Wrong eps parameter !");
			return;
		}
		
		double sigma = 4/eps* Math.sqrt(Math.log(1/delta)); 
		System.out.println("sigma = " + sigma);
		
		// 1 - output noisy projection A2
		double [][] AP = new double[n][k];		// AP = A*P where P ~ N(0,1/k)
		Random random = new Random();
		double sqrt_invk = Math.sqrt(1.0/k);
		System.out.println("sqrt_invk = " + sqrt_invk);
		for (int i = 0; i < n; i++)
			for (int j = 0; j < k; j++){
				for (int v : G.adj(i).keySet())
					AP[i][j] += random.nextGaussian()*sqrt_invk;
			}
		
		//
		int[] ret = kMeans(AP, c, steps, dist_type);
		
		int[] n_c = new int[c];
		for (int i = 0; i < n; i++)
			n_c[ret[i]] += 1;
		System.out.println("n_c : ");
		for (int i = 0; i < c; i++)
			System.out.println(n_c[i]);
		
		double true_mod = CommunityMeasure.modularity(G, ret); 
		System.out.println("AP: true_mod = " + true_mod);
		
		
		//
		double [][] A2 = new double[n][k];		// A2 = AP + Q where Q ~ N(0,sigma^2)
		for (int i = 0; i < n; i++)
			for (int j = 0; j < k; j++){
				A2[i][j] = AP[i][j] + random.nextGaussian()*sigma;
				
			}
		
		// 2 - k-means on A2 (c clusters)
		ret = kMeans(A2, c, steps, dist_type);
		
		n_c = new int[c];
		for (int i = 0; i < n; i++)
			n_c[ret[i]] += 1;
		System.out.println("n_c : ");
		for (int i = 0; i < c; i++)
			System.out.println(n_c[i]);
		
		true_mod = CommunityMeasure.modularity(G, ret); 
		System.out.println("A2: true_mod = " + true_mod);
		
//		System.out.println("ret = ");
//		for (int i = 0; i < 50; i++)
//			System.out.println(ret[i]);
		
	}
	
	
	//// write to MATLAB
	public static void writeSparseGraph(EdgeWeightedGraph G, String matlab_file) throws IOException{
		int n = G.V();
		int m = G.E();
		
		MLSparse mlSparse = new MLSparse("A", new int[] {n, n}, MLArray.mxDOUBLE_CLASS, 2*m);
		for (int u = 0; u < n; u++)
			for (int v : G.adj(u).keySet())
				mlSparse.set(1.0, u, v);
		
		ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(mlSparse); 
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
		
	}
	
	////
	public static void spectralAnalysis(EdgeWeightedGraph G, int d, int c, int steps, String eigen_file, int dist_type) throws IOException{
		int n = G.V();
		
		double [][] EV = new double[n][d];
		
		// read .10.ev file
		BufferedReader br = new BufferedReader(new FileReader(eigen_file));
		int i = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	if (str.length() == 0)
        		continue;
        	
        	String[] items = str.split(",");
        	
        	for (int j = 0; j < items.length; j++)
        		EV[i][j] = Double.parseDouble(items[j]);

        	i = i + 1;
		}
		br.close();

		// k-means
		int[] ret = kMeans(EV, c, steps, dist_type);
		
		int[] n_c = new int[c];
		for (i = 0; i < n; i++)
			n_c[ret[i]] += 1;
		System.out.println("n_c : ");
		for (i = 0; i < c; i++)
			System.out.println(n_c[i]);
		
		double true_mod = CommunityMeasure.modularity(G, ret); 
		System.out.println("true_mod = " + true_mod);
		
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		System.out.println("RandomProjection");
		
		// load graph
//		String dataname = "karate";			// (34, 78)
//		String dataname = "polbooks";		// (105, 441)		
//		String dataname = "polblogs";		// (1224,16715) 	
		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 		
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 	
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972)
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)		// privateProjection : 49s, writeSparseGraph: 52s
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)		// 			writeSparseGraph: 65s
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 

		////
		double eps = 4.0;	// 0.25, 0.5, 1.0, 2.0, 4.0
		double delta = 0.01;
		int k = 40;
		int c = 3;
		int steps = 20;
		System.out.println("eps = " + eps + " delta = " + delta + " k = " + k);

		
		String prefix = "";
		
		System.out.println("dataname = " + dataname);
		
		String filename = prefix + "_data/" + dataname + ".gr";
		
		//
		long start = System.currentTimeMillis();
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//// TEST privateProjection()
//		int dist_type = 1;
//		start = System.currentTimeMillis();
//		privateProjection(G, k, eps, delta, c, steps);
//		System.out.println("privateProjection - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		//// TEST writeSparseGraph() --> see uncertain-graph/dp/test.py for better implementation !
//		start = System.currentTimeMillis();
//		String matlab_file = prefix + "_data/" + dataname + ".mat";
//		writeSparseGraph(G, matlab_file);
//		System.out.println("writeSparseGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		//// TEST spectralAnalysis() --> call distCosine() in kMeans()
		int d = 2;
		String eigen_file = "D:/git/itce2011/uncertain-graph/_data/" + dataname + "." + d + ".ev";		// adj matrix A
//		String eigen_file = "D:/git/itce2011/uncertain-graph/_data/" + dataname + "." + d + ".lap";	// laplacian L

		System.out.println("eigen_file = " + eigen_file);
		c = 30;
		int dist_type = 3;
		spectralAnalysis(G, d, c, steps, eigen_file, dist_type);
		
	}

}
