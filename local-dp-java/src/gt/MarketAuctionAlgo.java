/**
 * Jul 31
 * papers:
 * - Auction Algorithms for Market Equilibrium (STOC'04)
 */

package gt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


////
// NOTE: pick one value or all values in D_i for j_selected --> similar results
//
public class MarketAuctionAlgo {

	static final int n = 3;
	static final int m = 3;
	
	static double eps = 0.02;	// 0.1 (49 rounds), 0.02 (266->10k rounds), 0.01 (>10k rounds)
	
	static double[][] a = {{1,2,3}, {4,5,6}, {7,8,9}};	// (n x m)
	static double[][] v = {{1,2,3}, {3,2,1}, {1,2,3}};
	
	static double[] p = new double[m];
	static double[] sum_a = new double[m];
	static double a_min = 10000000.0;
	
	static double[][] x = new double[n][m];
	static double[][] y = new double[n][m];
	static double[][] h = new double[n][m];
	
	static double[] r = new double[n];		// money surplus
	static double[] alpha = new double[n];		//
	
	////
	static void init(){
		// p
		for (int j = 0; j < m; j++)
			p[j] = 1.0;
				
		// r
		for (int i = 0; i < n; i++){
			r[i] = 0.0;
			for (int j = 0; j < m; j++)
				r[i] += a[i][j]*p[j];
		}
		
		// alpha
		
		// sum_a
		for (int j = 0; j < m; j++){
			sum_a[j] = 0.0;
			for (int i = 0; i < n; i++)
				sum_a[j] += a[i][j];
		}
			
		// a_min
		for (int j = 0; j < m; j++)
			if (a_min > sum_a[j])
				a_min = sum_a[j];
	}
	
	////
	static void outbid(int i, int j, int k){
		System.out.println("outbid : i = " + i + " j = " + j + " k = " + k);
		
		double t = y[k][j];
		if (t > r[i]/p[j])
			t = r[i]/p[j];
		
		h[i][j] += t;
		y[k][j] -= t;
		
		r[i] -= t*p[j];
		r[k] += t*p[j]/(1+eps);
	}
	
	////
	static void assign(double sum_x, int i, int j){
		System.out.println("assigned : i = " + i + " j = " +j);
		
		double t = sum_a[j] - sum_x;
		if (t > r[i]/p[j])
			t = r[i]/p[j];
		
		h[i][j] += t;
		r[i] -= t*p[j];
	}
	
	////
	static void raisePrice(int j){
		System.out.println("raisePrice : j = " +j);
		
		for (int k = 0; k < n; k++){
			y[k][j] = h[k][j];
			h[k][j] = 0.0;
		}
		
		for (int i = 0; i < n; i++)
			r[i] += eps*a[i][j]*p[j];
		
		p[j] = (1+eps)*p[j];
	}		
	
	//// 
	static void print_x(){
		System.out.println("x[i][j] :");	
		for (int i = 0; i < n; i++){
			for (int j = 0; j < m; j++)
				System.out.print(x[i][j] + " ");
			System.out.println();
		}
	}
	
	////
	public static void main(String[] args) {
		
		init();
		System.out.println("Init - DONE");
		
		Random random = new Random();
		//
		int round = 0;
		while (true){
			System.out.println("round " + (round++));
			
			
			// pick i (random)
			List<Integer> list_i = new ArrayList<Integer>();
			for(int i = 0; i < n; i++)
				if(r[i] > 0)
					list_i.add(i);
			int i = list_i.get(random.nextInt(list_i.size()) );
			
			// alpha[i] and demand set D_i
			alpha[i] = 0.0;
			for(int j = 0; j < m; j++)
				if (alpha[i] < v[i][j]/p[j])
					alpha[i] = v[i][j]/p[j];
			
			List<Integer> D_i = new ArrayList<Integer>();
			for(int j = 0; j < m; j++)
				if (v[i][j]/p[j] == alpha[i])
					D_i.add(j);
			
			//
			for (int j_selected : D_i){
//			int j_selected = D_i.get(random.nextInt(D_i.size()) );
			
				double sum_x = 0.0;
				for (int l = 0; l < m; l++)
					sum_x += x[l][j_selected];
				
				if (sum_x < sum_a[j_selected])
					assign(sum_x, i, j_selected);
				else{
					boolean found = false;
					int k = -1;
					for (k = 0; k < n; k++)
						if (y[k][j_selected] > 0){
							found = true;
							break;
						}
					
					if (found)
						outbid(i, j_selected, k);
					else{
						raisePrice(j_selected);
					}
				}
				
				for (int ii = 0; ii < n; ii++)
					for (int jj = 0; jj < m; jj++)
						x[ii][jj] = y[ii][jj] + h[ii][jj];
						
				print_x();
			}	// end for j_selected
			
			// check 2 stop conditions
			boolean stop1 = true;
			for (i = 0; i < n ; i++)
				if (r[i] >= eps/(n*(1+eps))*a_min){
					stop1 = false;
					break;
				}
			
			boolean stop2 = true;
			for (int j = 0; j < m ; j++){
				double sum_x = 0.0;
//				sum_x = 0.0;
				for (i = 0; i < n; i++)
					sum_x += x[i][j];
					
				if (sum_x != sum_a[j]){
					stop2 = false;
					break;
				}
			}
			
			if (stop1 || stop2)
				break;
			
			if (round == 10000)
				break;
		}
		
		System.out.println("Loop terminated");
		
//		System.out.println("sum_a ");
//		for (int j = 0; j < m; j++)
//			System.out.print(sum_a[j] + " ");
//		System.out.println();
//		
//		System.out.println("a_min = " + a_min);
		
		
		
		
		System.out.println("Prices :");
		for (int j = 0; j < m; j++)
			System.out.print(p[j] + " ");
		System.out.println();
		
		
		
	}

}
