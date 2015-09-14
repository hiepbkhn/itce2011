/*
 * Sep 11, 2015
 * 	- paper "AutoPart - Parameter-Free Graph Partitioning and Outlier Detection" (PKDD'04)
 */

package dp.combined;

import java.util.ArrayList;
import java.util.List;

import com.carrotsearch.hppc.cursors.IntCursor;

import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;
import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;

public class AutoPart {

	final static double EPSILON = 1e-30;
	
	////
	public static double log2(double k){
		return Math.log(k)/Math.log(2);
	}
	
	////
	public static double logStar(double k){
		double result = 0.0;
		double l = k;
		while (true){
			if (l <= 1)
				break;
			l = log2(l);
			result += l;
			
		}
		
		//
		return result;
	}
	
	//// T(D,k,G)
	// D[i][j]: number of 1 cells in partition (i,j)
	public static double totalCost(Grph G, int k, IntSet[] g, int[][] D){
		int n = G.getNumberOfVertices();
		
		double T = 0.0;
		
		// k*
		T += logStar(k) /n;
		
		int[] a = new int[k];
		for (int i = 0; i < k; i++)
			a[i] = g[i].size();
		
		// a_hat
		int[] a_hat = new int[k];
		for (int i = 0; i < k-1; i++){
			for (int t = i; t < k; t++)
				a_hat[i] += a[t];
			a_hat[i] += -k + (i+1);
			
			T += Math.ceil(log2(a_hat[i])) /n;
		}
				
		// a_j a_j, C(Dij)
		for (int i = 0; i < k; i++)
			for (int j = 0; j < k; j++){
				T += Math.ceil(log2(a[i]*a[j] + 1)) /n;
				
				if (D[i][j] > 0 && D[i][j] < a[i]*a[j]){
					double Pij = (double)D[i][j]/(a[i]*a[j]);
					if (Pij > EPSILON && Pij < 1 - EPSILON)
						T += -D[i][j]*log2(Pij) - (a[i]*a[j] - D[i][j])*log2(1-Pij) ;
				}
			}
		
		// debug
		for (int i = 0; i < k; i++)
			System.out.print(a[i] + " ");
		System.out.println();
		
		//
		return T;
	}
	
	////
	public static void printPartition(IntSet[] g, int k){
		for (int i = 0; i < k; i++){
			System.out.print("part " + i + " size = " + g[i].size() + " : ");
			if (g[i].size() > 0)
				for (IntCursor u:g[i])
					System.out.print(u.value + ",");
			System.out.println();
		}
		
		
		
	}
	
	////
	public static void printMatrixP(IntSet[] g, int k, double[][] P){
		double sumDiag = 0.0;
		for (int i = 0; i < k; i++){
			for(int j = 0; j < k; j++)
				System.out.print(String.format("%.3f",P[i][j]) + " ");
			System.out.println();
			
			sumDiag += P[i][i]*g[i].size()*g[i].size();
		}
		System.out.println("sumDiag = " + sumDiag);
	}
	
	////
	public static void checkG(IntSet[] g, int k, int n){
		int[] nodeToSet = new int[n];
		for (int x = 0; x < n; x++)
			nodeToSet[x] = -1;
		
		for (int i = 0; i < k; i++){
			if (g[i].size() > 0)
				for (IntCursor u:g[i])
					nodeToSet[u.value] = i;
		}
		
		int x = 0;
		for (; x < n; x++)
			if (nodeToSet[x] == -1){
				System.out.println("checkG : FAILED");
				break;
		}
		if (x == n)
			System.out.println("checkG : OK");
		
		//
		int sumGsize = 0;
		for (int i = 0; i < k; i++)
			sumGsize += g[i].size();
		System.out.println("sumGsize = " + sumGsize);
	}
	
	////
	public static double modularity(Grph G, IntSet[] g, int[][] D){
		int n = G.getNumberOfVertices();
		int m = G.getNumberOfEdges();
		int k = g.length;
		
		double Q = 0.0;
		
		for (int i = 0; i < k; i++){
			Q += (double)D[i][i]/ (2*m);	// 2 because D[i][i] is twice #edges in subgraph i
			
			int dc = 0;
			for (IntCursor u:g[i])
				dc += G.getVertexDegree(u.value);
			Q = Q - dc*dc/(4.0*m*m);
		}
		
		//
		return Q;
	}
	
	////
	public static void innerLoop(Grph G, int k, int nLoop){
		int n = G.getNumberOfVertices();
		
		int[][] D = new int[k][k];;
		double[][] P = new double[k][k];;
		int[][] w;
		
		// init partition
		IntSet[] g = new IntSet[k];
		for (int i = 0; i < k-1; i++){
			IntSet set =  new IntHashSet();
			for (int j = 0; j < n/k; j++)
				set.add(i*(n/k) + j);
			g[i] = set;
		}
		
		IntSet set =  new IntHashSet();
		for (int j = (k-1)*(n/k); j < n; j++)
			set.add(j);
		g[k-1] = set;

		
		for (int t = 0; t < nLoop; t++){
			// compute D_i,j(t), P_i,j(t)
			int[] nodeToSet = new int[n];	// point to containing nodelist
			for (int i = 0; i < k; i++){
				if (g[i].size() > 0)
					for (IntCursor u:g[i])
						nodeToSet[u.value] = i;
			}
			
			D = new int[k][k];
			for (VertexPair p : G.getEdgePairs()){
		    	int u = p.first;
		       	int v = p.second;
		       	D[nodeToSet[u]][nodeToSet[v]] += 1;
		       	D[nodeToSet[v]][nodeToSet[u]] += 1;
			}
			
			// debug
			int sD = 0;
			for (int i = 0; i < k; i++)
				for (int j = 0; j < k; j++)
					sD += D[i][j];
			System.out.println("sD = " + sD);

			
			P = new double[k][k];
			for(int i = 0; i < k; i++)
				for(int j = 0; j < k; j++)
					if (g[i].size() > 0 && g[j].size() > 0)
						P[i][j] = (double)D[i][j]/(g[i].size() * g[j].size());	
			
			//
			System.out.println("t = " + t + ", T = " + totalCost(G, k, g, D));
//			printPartition(g, k, P);
			
			
			// w(x_row,j) : w of size n*k
			w = new int[n][k];
			for (VertexPair p : G.getEdgePairs()){
				int u = p.first;
		       	int v = p.second;
		       	
		       	w[u][nodeToSet[v]] += 1;
		       	w[v][nodeToSet[u]] += 1;
			}

			// compute temp_g: g_x(t+1) = argmin ...
			IntSet[] temp_g = new IntSet[k];
			for (int i = 0; i < k; i++){
				temp_g[i] =  new IntHashSet();
			}
				
			for (int x = 0; x < n; x++){
				double min = 1000000000.0;
				int min_i = -1;
				
				
				for (int i = 0; i < k; i++){
					// compute RHS, formula (4)
					double RHS = 0.0;
					for (int j = 0; j < k; j++){
						if (P[i][j] > EPSILON && P[i][j] < 1 - EPSILON)
							RHS += -2*( w[x][j]*log2(P[i][j]) + (g[j].size()-w[x][j])*log2(1-P[i][j]) );		// 2 for undirected graphs
					}
					
					if (P[i][nodeToSet[x]] > EPSILON && P[i][nodeToSet[x]] < 1 - EPSILON)
						RHS += 2*log2(1 - P[i][nodeToSet[x]]);													// 2 for undirected graphs
						
					RHS += -log2(1 - P[i][i]);
					
					// debug
//					if (x == 0)
//						System.out.println("RHS = " + RHS);
					
					if (RHS > 0 && RHS < min){				// IMPORTANT : RHS > 0 (runnable with amazon)
						min = RHS;
						min_i = i; 
					}
				}
				
				temp_g[min_i].add(x);	
				
			}
			// update g
//			g = temp_g;
			
			for (int i = 0; i < k; i++){
				g[i].clear();
				if (temp_g[i].size() > 0)
					g[i].addAll(temp_g[i]);
			}
			
		}
		//
		
		System.out.println("T = " + totalCost(G, k, g, D));
//		printPartition(g, k);
		printMatrixP(g, k, P);
		checkG(g, k ,n);
		
		System.out.println("modularity Q = " + modularity(G, g, D));
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// load graph
//		String dataname = "polbooks";		// (105, 441)		k=3, nLoop=10
//		String dataname = "polblogs";		// (1224,16715) 	k=3,10
		String dataname = "as20graph";		// (6474,12572)		k=5
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	k=20, nLoop=5 (ok)  sumDiag = 2.082E7 ??
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	k=20, nLoop=5 (ok)	sumDiag = 2.856E7 ??
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624)k=20, nLoop=5 (mem=2.0GB): 1 cluster (failed at t=2)
													//						
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
//		String prefix = "";
//	    int n_samples = 1;
//	    
//	    System.out.println("dataname = " + dataname);
//	    
//		String filename = prefix + "_data/" + dataname + ".gr";
//		
//		EdgeListReader reader = new EdgeListReader();
//		Grph G;
//		RegularFile f = new RegularFile(filename);
//		
//		long start = System.currentTimeMillis();
//		G = reader.readGraph(f);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		// TEST 
//		int k = 28;
//		int nLoop = 20;
//		System.out.println("k = " + k + ", nLoop = " + nLoop);
//		innerLoop(G, k, nLoop);
		
		// TEST modularity
		Grph G = new InMemoryGrph();
		G.addNVertices(4);
		G.addSimpleEdge(0, 1, false);
		G.addSimpleEdge(1, 2, false);
		G.addSimpleEdge(1, 3, false);
		G.addSimpleEdge(2, 3, false);
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		IntSet[] g = new IntSet[2];
		g[0] = new IntHashSet(); g[0].add(0); g[0].add(3);
		g[1] = new IntHashSet(); g[1].add(1); g[1].add(2); 
		int[][] D = new int[2][2];
		D[0][0] = 0;
		D[1][1] = 2;
		
		
		System.out.println("modularity Q = " + modularity(G, g, D));
		
	}

}
