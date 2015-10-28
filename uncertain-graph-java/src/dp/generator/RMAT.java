/*
 * Oct 26, 2015
 * 	- implement AutoMAT-fast (paper: R-MAT - A Recursive Model for Graph Mining - SDM'04)
 */

package dp.generator;

import java.util.List;

import algs4.EdgeDirWeightedGraph;
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
			// Pi[i] = p^(n-i)*(1-p)^i
			double[] Pi = new double[n+1];
			double maxPi = -1.0;
			double minPi = 2.0;
			for (int i = 0; i<= n; i++){
				Pi[i] = Math.pow(p, n-i) * Math.pow(1-p, i);
				if (maxPi < Pi[i])
					maxPi = Pi[i];
				if (minPi > Pi[i])
					minPi = Pi[i];
			}
			System.out.println("minPi = " + minPi + " maxPi = " + maxPi);
			
			// WAY-1
			for (int k = 0; k < degHist.length; k++){
				degEst[k] = 0.0;
				for (int i = 0; i<= n; i++){
					double EPi = E*Pi[i];
					double EPi2 = E*Pi[i]*(1-Pi[i]);
					degEst[k] += nCk(n,i) / Math.sqrt(2*Math.PI*EPi2) * Math.exp(-(k-EPi)*(k-EPi)/(2*EPi2) );	// normal distr to approximate binomial distr
					
				}
			}
			
			// WAY-2 (WRONG !)
//			for (int k = 0; k < degHist.length; k++){
//				
//				double sumMean = 0.0;
//				double sumVar = 0.0;
//				for (int i = 0; i<= n; i++){
//					double nCi = nCk(n,i);
//					double EPi = E*Pi[i];
//					double EPi2 = E*Pi[i]*(1-Pi[i]);
//					sumMean += nCi * EPi;
//					sumVar += nCi * nCi * EPi2;
//				}
//				
//				degEst[k] = Math.exp(-(k-sumMean)*(k-sumMean)/(2*sumVar)) / Math.sqrt(2*Math.PI * sumVar);
//			}
			
			// L1-distance between degHist and degEst
			double dist = 0.0;
			for (int k = 0; k < degHist.length; k++)
				dist += Math.abs(degEst[k] - degHist[k]);
			// debug
//			System.out.println("p = " + p + " dist = " + dist);
			
			if (dist < min_dist){
				min_dist = dist;
				best_p = p;
			}
			
			
		}
		System.out.println("min_dist = " + min_dist);
		
		
		return best_p;
	}
	
	//// return (a+b, a+c)
	public static List<Double> fitRMAT(){
		
		return null;
	}
	
	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {

//		EdgeDirWeightedGraph G = EdgeDirWeightedGraph.readEdgeList("D:/git/itce2011/uncertain-graph/_data/rmat1024_06_01_015.gr", 1024, " ");	// directed (p=0.728, 0.703)
//		EdgeDirWeightedGraph G = EdgeDirWeightedGraph.readEdgeList("D:/git/itce2011/uncertain-graph/_data/rmat1024_045_015_015.gr", 1024, " ");	// directed (p=0.661, 0.649)
		EdgeDirWeightedGraph G = EdgeDirWeightedGraph.readEdgeList("D:/git/itce2011/uncertain-graph/_data/rmat1024_045_01_01.gr", 1024, " ");	// directed (p=0.63, 0.63)
//		EdgeDirWeightedGraph G = EdgeDirWeightedGraph.readEdgeList("D:/git/itce2011/uncertain-graph/_data/rmat65536_045_01_01.gr", 65536, " ");	// directed (p=0.639, 0.639)
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		int n = (int)Math.round(Math.log(G.V()) / Math.log(2.0));
		System.out.println("n = " + n);
		double lower = 0.5;
		double upper = 0.9;
		double step = 0.001;
		
		// out-degree
		int[] degHist = new int[G.V()];
		for (int u = 0; u < G.V(); u++)
			degHist[G.degreeOut(u)] += 1;
		
		long start = System.currentTimeMillis();
		double p = searchProb(degHist, n, G.E(), lower, upper, step);
		System.out.println("out-deg: best p = " + p);	//
		System.out.println("elapsed " + (System.currentTimeMillis() - start));
		
		
		// in-degree
		degHist = new int[G.V()];
		for (int u = 0; u < G.V(); u++)
			degHist[G.degreeIn(u)] += 1;
		
		start = System.currentTimeMillis();
		p = searchProb(degHist, n, G.E(), lower, upper, step);
		System.out.println("in-deg: best p = " + p);	//	
		System.out.println("elapsed " + (System.currentTimeMillis() - start));
	}

}
