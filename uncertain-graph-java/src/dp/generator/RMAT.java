/*
 * Oct 26, 2015
 * 	- implement AutoMAT-fast (paper: R-MAT - A Recursive Model for Graph Mining - SDM'04)
 */

package dp.generator;

import java.util.List;

import algs4.EdgeWeightedGraph;


public class RMAT {

	
	////
	public static double nCk(int n, int k){
		double ret = 1.0;
		for (int i = 0; i < k; i++)
			ret = ret * (n-i) / (k-i);
		
		return ret;
	}
	
	
	
	//// grid search in [lower, upper, step]
	// degHist[d] = #nodes having degree d
	public static double searchProb(int[] degHist, int n, int E, double lower, double upper, double step){
		double best_p = 0.0;
		double min_dist = 1000000000.0;

//		double[] degDistr = new double[degHist.length];
//		int sum = 0;
//		for (int d : degHist)
//			sum += d;
//		for (int i = 0; i < degHist.length; i++)
//			degDistr[i] = (double)degHist[i]/sum;
		
		//
		double[] degEst = new double[degHist.length];	// estimated degree histogram
		for (double p = lower; p < upper; p += step){
			double[] Pi = new double[n+1];
			for (int i = 0; i<= n; i++)
				Pi[i] = Math.pow(p, n-i) * Math.pow(1-p, i);
			
			//
			for (int k = 0; k < degHist.length; k++){
				degEst[k] = 0.0;
				for (int i = 0; i<= n; i++){
					double EPi = E*Pi[i];
					double EPi2 = E*Pi[i]*(1-Pi[i]);
					degEst[k] += nCk(n,i) / Math.sqrt(EPi2*2*Math.PI) * Math.exp(-(k-EPi)*(k-EPi)/(2*EPi2));
					
				}
			}
			
			// L1-distance between degHist and degEst
			double dist = 0.0;
			for (int k = 0; k < degHist.length; k++)
				dist += Math.abs(degEst[k] - degHist[k]);
			System.out.println("p = " + p + " dist = " + dist);
			
			if (dist < min_dist){
				min_dist = dist;
				best_p = p;
			}
			
			
		}
		
		
		return best_p;
	}
	
	//// return (a+b, a+c)
	public static List<Double> fitRMAT(){
		
		return null;
	}
	
	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {

		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("D:/git/itce2011/uncertain-graph/_data/rmat1024.gr", 1024, " ");	// Unweighted G, rmat1024.gr Weighted !
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		int[] degHist = new int[G.V()];
		for (int u = 0; u < G.V(); u++)
			degHist[G.degree(u)] += 1;
		
		//
		double p = searchProb(degHist, 10, G.E(), 0.5, 1.0, 0.001);
		System.out.println("best p = " + p);	//	p = 0.681
	}

}
