/*
 * paper "The Benefits of Coding over Routing in a Randomized Setting" (ISIT'03)
 * May 24, 2016
 * 	- randomizedRouting(), randomizedCoding()
 */	


package coding;

import java.util.Random;

public class RandomizedCoding {

	//// over a rectangular grid
	public static int[][] randomizedRouting(int n){
		int r = 2;
		
		Link [][] H = new Link[n][n];
		Link [][] V = new Link[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++){
				H[i][j] = new Link(r);
				V[i][j] = new Link(r);
			}
				
		
		// step 0, node (0,0) sends X1 along the link H[0,0], X2 along V[0,0]
		H[0][0].c[0] = 1;
		V[0][0].c[1] = 1;
		
		// step 1 -> n-1
		Random random = new Random();
		for (int k = 1; k < n; k++){
			// 2 boundary nodes (0,k), (k,0)
			H[0][k] = H[0][k-1].copy();
			V[0][k] = H[0][k-1].copy();
			
			V[k][0] = V[k-1][0].copy();
			H[k][0] = V[k-1][0].copy();
			
			// internal nodes (i,j)
			for (int j = 1; j < k; j++){
				int i = k-j;
				// randomized routing two input links H[i,j-1], V[j,i-1]
				if (random.nextDouble() > 0.5){
					H[i][j] = H[i][j-1].copy();
					V[i][j] = V[i-1][j].copy();
				}else{
					H[i][j] = V[i-1][j].copy();
					V[i][j] = H[i][j-1].copy();
				}
				
			}
		}
		
		// count the number of decodable nodes at distance k
		int[][] count = new int[n][n];
		count[0][0] = 1;
		for (int k = 1; k < n; k++){
			for (int j = 1; j < k; j++){
				int i = k - j;
				
				Link[] links = new Link[] {H[i][j-1], V[i-1][j]};
				if (Link.det(links, r) != 0)
					count[i][j] = 1;
			}
		}
			
		// debug
//		for (int i = 0; i < n; i++){
//			for (int j = 0; j < n; j++)
//				System.out.print(H[i][j] + " ");	// row-based
//			System.out.println();
//		}
//		System.out.println("-----------");
//		for (int i = 0; i < n; i++){
//			for (int j = 0; j < n; j++)
//				System.out.print(V[i][j] + " ");	// row-based
//			System.out.println();
//		}
		
		//
//		for (int k = 0; k < n; k++)
//			System.out.print(count[k] + "/" + k + ", ");
//		System.out.println();
		
		//
		return count;
	}
	
	//// over a rectangular grid
	public static int[][] randomizedCoding(int n){
		int r = 2;
		
		Link [][] H = new Link[n][n];
		Link [][] V = new Link[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++){
				H[i][j] = new Link(r);
				V[i][j] = new Link(r);
			}
				
		
		// step 0, node (0,0) sends X1 along the link H[0,0], X2 along V[0,0]
		H[0][0].c[0] = 1;
		V[0][0].c[1] = 1;
		
		// step 1 -> n-1
		Random random = new Random();
		int[][] coeff = new int[][]{{1,1},{1,0},{0,1}};
		for (int k = 1; k < n; k++){
			// 2 boundary nodes (0,k), (k,0)
			H[0][k] = H[0][k-1].copy();
			V[0][k] = H[0][k-1].copy();
			
			V[k][0] = V[k-1][0].copy();
			H[k][0] = V[k-1][0].copy();
			
			// internal nodes (i,j)
			for (int j = 1; j < k; j++){
				int i = k-j;
				// randomized CODING two input links H[i,j-1], V[j,i-1]
				int ind = random.nextInt(3);
				int val1 = coeff[ind][0];
				int val2 = coeff[ind][1];
				H[i][j] = Link.combine(H[i][j-1], V[i-1][j], val1, val2);
				
				ind = random.nextInt(3);
				val1 = coeff[ind][0];
				val2 = coeff[ind][1];
				V[i][j] = Link.combine(H[i][j-1], V[i-1][j], val1, val2);
				
			}
		}
		
		// count the number of decodable nodes at distance k
		int[][] count = new int[n][n];
		count[0][0] = 1;
		for (int k = 1; k < n; k++){
			for (int j = 1; j < k; j++){
				int i = k - j;
				
				Link[] links = new Link[] {H[i][j-1], V[i-1][j]};
				if (Link.det(links, r) != 0)
					count[i][j] = 1;
			}
		}	
		
		//
		return count;
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) {
		
		//// TEST randomizedRouting()
//		System.out.println("TEST randomizedRouting");
//		int n = 20;
//		int trials = 1000;
//		int[][] sum = new int[n][n];
//		for (int t = 0; t < trials; t++){
//			int[][] count = randomizedRouting(n);
//			for (int i = 0; i < n; i++)
//				for (int j = 0; j < n; j++)
//					sum[i][j] += count[i][j];
//		}
//
//		for (int i = 0; i < n; i++){
//			for (int j = 0; j < n; j++)
//				System.out.print(String.format("%.2f", sum[i][j]/(double)trials) + " ");	
//			System.out.println();
//		}
		
		
		//// TEST randomizedCoding()
		System.out.println("TEST randomizedCoding");
		int n = 20;
		int trials = 1000;
		int[][] sum = new int[n][n];
		for (int t = 0; t < trials; t++){
			int[][] count = randomizedCoding(n);
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++)
					sum[i][j] += count[i][j];
		}

		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++)
				System.out.print(String.format("%.2f", sum[i][j]/(double)trials) + " ");	
			System.out.println();
		}
	}

}
