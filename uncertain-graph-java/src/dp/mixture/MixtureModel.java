/*
 * Apr 29
 * 	- conversion from C source (Mixture models and exploratory analysis in networks (PNAS, 2007))
 * May 4
 * 	- try to fix underflow errors for large graphs (n_nodes >= 1000)
 */

package dp.mixture;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import toools.io.file.RegularFile;

public class MixtureModel {

	public static final int MAXITER = 100000;
	public static final int MAXITER_DIRECTED = 10000;
	public static final int CHECKINT = 10;
	public static final double EPS = 0.0000001;
	public static final double DELTA_EPS = 0.001;
	
	////
	public static void init(double[][] theta, double[] pi){
		int n_groups = theta.length; 
		int n_nodes = theta[0].length;
		
//		System.out.println("n_groups = " + n_groups);
//		System.out.println("n_nodes = " + n_nodes);
		
		int i, r;
		double norm;
		// Seed the RNG
		Random random = new Random(System.currentTimeMillis());
		// Initialize the pi parameters
		norm = 0.0;
		for (r = 0; r < n_groups; r++) {
			pi[r] = 1.0 + 0.01 * random.nextDouble();
			norm += pi[r];
		}
		for (r = 0; r < n_groups; r++)
			pi[r] /= norm;

		// Initialize the thetas
		for (r = 0; r < n_groups; r++) {
			norm = 0.0;
			for (i = 0; i < n_nodes; i++) {
				theta[r][i] = 1.0 + 0.01 * random.nextDouble();
				norm += theta[r][i];
			}
			for (i = 0; i < n_nodes; i++)
				theta[r][i] /= norm;
		}
	}
	
	////
	public static double logLK(Grph G, double[][] theta, double[][] q, double[] pi){
		int n_groups = theta.length; 
		int n_nodes = theta[0].length;
		
		double res, sum;
		res = 0.0;
		
		for (int i = 0; i < n_nodes; i++) {
			for (int r = 0; r < n_groups; r++) {
				sum = 0.0;
				for (IntCursor c : G.getNeighbours(i))
					sum += Math.log(theta[r][c.value]);
				if (q[i][r] > 0.0)
					res += q[i][r] * (Math.log(pi[r]) + sum);
			}
		}
		return res;
	}
	
	////
	static double sqr(double x){
		return x * x;
	}
	
	//// UNDIRECTED
	public static void expectationMaximization(Grph G, int n_groups, int n_nodes, String out_file) throws IOException{
		double[] pi = new double[n_groups];
		double[][] theta = new double[n_groups][n_nodes];
		double[][] q = new double[n_nodes][n_groups];
		double[][] qold = new double[n_nodes][n_groups];
		
		// init
		init(theta, pi);
		
		
		//
		int i, r, s, t;
		double norm;
		double sum1, sum2, sum3, sum;
		double deltasq = 0; 
		double delta = 0;
		double[] pp = new double[n_groups];
		// Main loop
		for (t = 0; t < MAXITER; t++) {
			if (t == 1)
				System.out.println("INIT - log likelihood = " + logLK(G, theta, q, pi));
			
			// Save the group memberships
			for (i = 0; i < n_nodes; i++) {
				for (r = 0; r < n_groups; r++) {
					qold[i][r] = q[i][r];
				}
			}
	
			// WAY-1: Calculate the conditional group memberships (complexity O(n.r.d))
//			for (i = 0; i < n_nodes; i++) {
//				norm = 0.0;
//				for (r = 0; r < n_groups; r++) {
//					pp[r] = pi[r];
//					for (IntCursor c : G.getNeighbours(i))
//						pp[r] *= theta[r][c.value];
//					norm += pp[r];
//				}
//				for (r = 0; r < n_groups; r++)
//					q[i][r] = pp[r] / norm;
//			}
			
			// WAY-2 (complexity O(n.r^2.d))
			boolean computed;
			for (i = 0; i < n_nodes; i++) {
				for (r = 0; r < n_groups; r++) {
					computed = false;
					// check pi[]
					if (pi[r] < EPS){
						q[i][r] = 0.0;
						computed = true;
						continue;
					}
					// check theta[][]
					for (IntCursor c : G.getNeighbours(i))
						if (theta[r][c.value] < EPS){
							q[i][r] = 0.0;
							computed = true;
							break;
						}
					// otherwise
					if (! computed){
						sum = 0.0;
						for (s = 0; s < n_groups; s++) {
							pp[s] = pi[s] / pi[r];
							for (IntCursor c : G.getNeighbours(i))
								pp[s] *= theta[s][c.value] / theta[r][c.value];
							sum += pp[s];
						}
						q[i][r] = 1 / sum;
					}
				}
				
			}
			
			// Calculate the updated model parameters
			for (r = 0; r < n_groups; r++) {
				// pi
				sum1 = sum2 = 0.0;
				for (i = 0; i < n_nodes; i++) {
					sum1 += q[i][r];
					sum2 += G.getVertexDegree(i) * q[i][r];
				}
				pi[r] = sum1 / n_nodes;
				
				// theta
				for (i = 0; i < n_nodes; i++) {
					sum3 = 0;
					for (IntCursor c : G.getNeighbours(i))
						sum3 += q[c.value][r];
					theta[r][i] = sum3 / sum2;
				}
			}
	
			// Calculate the total deltasq for the q's
			deltasq = 0.0;
			for (i = 0; i < n_nodes; i++) {
				for (r = 0; r < n_groups; r++)
					deltasq += sqr(qold[i][r] - q[i][r]);
			}
			delta = Math.sqrt(deltasq);
	
			// Write out a progress report
			if (t % CHECKINT == 0)
				System.out.println("deltasq = " + deltasq + " delta = " + delta);
			if (delta < DELTA_EPS)
				break;
		}
		System.out.println("delta = " + delta + ", " + t + " iterations to converge");
		
		// log likelihood
		System.out.println("log likelihood = " + logLK(G, theta, q, pi));
		
		// output
		output(out_file, theta, qold, pi);
	}
	
	
	//// DIRECTED
	public static void expectationMaximizationDirected(Grph G, int n_groups, int n_nodes, String out_file) throws IOException{
		double[] pi = new double[n_groups];
		double[][] theta = new double[n_groups][n_nodes];
		double[][] q = new double[n_nodes][n_groups];
		double[][] qold = new double[n_nodes][n_groups];
		
		// init
		init(theta, pi);
		
		
		//
		int i, r, s,t;
		double norm;
		double sum1, sum2, sum3, sum;
		double deltasq = 0; 
		double delta = 0;
		double[] pp = new double[n_groups];
		// Main loop
		for (t = 0; t < MAXITER_DIRECTED; t++) {
			if (t == 1)
				System.out.println("INIT - log likelihood = " + logLK(G, theta, q, pi));
			
			// Save the group memberships
			for (i = 0; i < n_nodes; i++) {
				for (r = 0; r < n_groups; r++) {
					qold[i][r] = q[i][r];
				}
			}
	
			// WAY-1: Calculate the conditional group memberships
//			for (i = 0; i < n_nodes; i++) {
//				norm = 0.0;
//				for (r = 0; r < n_groups; r++) {
//					pp[r] = pi[r];
//					for (IntCursor c : G.getNeighbours(i))
//						pp[r] *= theta[r][c.value];
//					norm += pp[r];
//				}
//				for (r = 0; r < n_groups; r++)
//					q[i][r] = pp[r] / norm;
//			}
			
			// WAY-2
			boolean computed;
			for (i = 0; i < n_nodes; i++) {
				for (r = 0; r < n_groups; r++) {
					computed = false;
					// check pi[]
					if (pi[r] < EPS){
						q[i][r] = 0.0;
						computed = true;
						continue;
					}
					// check theta[][]
					for (IntCursor c : G.getNeighbours(i))
						if (theta[r][c.value] < EPS){
							q[i][r] = 0.0;
							computed = true;
							break;
						}
					// otherwise
					if (! computed){
						sum = 0.0;
						for (s = 0; s < n_groups; s++) {
							pp[s] = pi[s] / pi[r];
							for (IntCursor c : G.getNeighbours(i))
								pp[s] *= theta[s][c.value] / theta[r][c.value];
							sum += pp[s];
						}
						q[i][r] = 1 / sum;
					}
				}
				
			}
			
			// Calculate the updated model parameters
			for (r = 0; r < n_groups; r++) {
				// pi
				sum1 = sum2 = 0.0;
				for (i = 0; i < n_nodes; i++) {
					sum1 += q[i][r];
					sum2 += G.getVertexDegree(i) * q[i][r];
				}
				pi[r] = sum1 / n_nodes;
				
				// theta
				for (i = 0; i < n_nodes; i++) {
					sum3 = 0;
					for (IntCursor c : G.getInNeighbors(i))			// DIFFERENCE here
						sum3 += q[c.value][r];
					theta[r][i] = sum3 / sum2;
				}
			}
	
			// Calculate the total deltasq for the q's
			deltasq = 0.0;
			for (i = 0; i < n_nodes; i++) {
				for (r = 0; r < n_groups; r++) {
					deltasq += sqr(qold[i][r] - q[i][r]);
				}
			}
			delta = Math.sqrt(deltasq);
	
			// Write out a progress report
			if (t % CHECKINT == 0)
				System.out.println("deltasq = " + deltasq + " delta = " + delta);
			if (delta < DELTA_EPS)
				break;
		}
		System.out.println("delta = " + delta + ", " + t + " iterations to converge");
		
		// log likelihood
		System.out.println("log likelihood = " + logLK(G, theta, q, pi));
		
		// output
		output(out_file, theta, qold, pi);
	}
	
	
	////
	public static void output(String out_file, double[][] theta, double[][] q, double[] pi) throws IOException{
		int n_groups = theta.length; 
		int n_nodes = theta[0].length;
		
		int i, r;
		BufferedWriter bw = new BufferedWriter(new FileWriter(out_file));
		// Print out the pi's
		bw.write("pi:\n");
		for (r = 0; r < n_groups; r++)
			bw.write(String.format("%.6f", pi[r]) + " ");
		bw.write("\n");
		
		bw.write("\n\n");
		
		// Print the theta's
		bw.write("theta:\n");
		for (i = 0; i < n_nodes; i++) {
			for (r = 0; r < n_groups; r++) {
				bw.write(String.format("%.6f", theta[r][i]) + " ");
			}
			bw.write("\n");
		}
		bw.write("\n");
		
		// Print the q's
		bw.write("q:\n");
		for (i = 0; i < n_nodes; i++) {
			for (r = 0; r < n_groups; r++) {
				bw.write(String.format("%.6f", q[i][r]) + " ");
			}
			bw.write("\n");
		}
		bw.write("\n");
		
		bw.close();
	}
	
	////////////////////////////
	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("MixtureModel");
		
		// load graph
//		String dataname = "karate";			// (34, 78)		2 groups
//		String dataname = "adjnoun";		// (112, 425)	bipartite
//		String dataname = "keystone";		// (108, 1388)	4 groups (8 keystones), directed
		
//		String dataname = "polbooks";		// (105, 441)	3 groups	, 0.25s
//		String dataname = "polblogs";		// (1224,16715) 	
//		String dataname = "as20graph";		// (6474,12572)		
		String dataname = "wiki-Vote";		// (7115,100762) 	(r=3: mem 1GB, 97s, logLK = -1570985. -> -1478659.)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	(r=3: mem 1GB, 190s, logLK = -3651141. -> -3424748.)
		
		// COMMAND-LINE
		String prefix = "";
		int n_groups = 3;
		
		if(args.length >= 3){
			prefix = args[0];
			dataname = args[1];
			n_groups = Integer.parseInt(args[2]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("n_groups = " + n_groups);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		String out_file = prefix + "_data/" + dataname + ".out";

	    //
		EdgeListReader reader = new EdgeListReader();
//		EdgeListWriter writer = new EdgeListWriter();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());

		// TEST expectationMaximization
		long start = System.currentTimeMillis();
		expectationMaximization(G, n_groups, G.getNumberOfVertices(), out_file);
		System.out.println("expectationMaximization - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// TEST expectationMaximizationDirected
//		expectationMaximizationDirected(G, n_groups, G.getNumberOfVertices(), out_file);
		
	}

}
