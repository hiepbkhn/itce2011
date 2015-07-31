/**
 * Jul 31
 * papers:
 * - Auction Algorithms for Market Equilibrium (STOC'04)
 */

package gt;


public class MarketAuctionAlgo {

	static final int n = 3;
	static final int m = 3;
	
	static double eps = 0.1;
	
	static double[][] a = {{1,2,3}, {4,5,6},{7,8,9}};	// (n x m)
	static double[][] v = {{1,2,3}, {1,2,3},{1,2,3}};
	
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
		double t = sum_a[j] - sum_x;
		if (t > r[i]/p[j])
			t = r[i]/p[j];
		
		h[i][j] += t;
		r[i] -= t*p[j];
	}
	
	////
	static void raisePrice(int j){
		for (int k = 0; k < n; k++){
			y[k][j] = h[k][j];
			h[k][j] = 0.0;
		}
		
		for (int i = 0; i < n; i++){
			r[i] += eps*a[i][j]*p[j];
		}
		
		p[j] = (1+eps)*p[j];
	}		
	
	////
	public static void main(String[] args) {
		
		init();
		System.out.println("Init - DONE");
		
		//
		int round = 0;
		while (true){
			System.out.println("round " + (round++));
			
			
			// pick i
			int i = 0;
			for(i = 0; i < n; i++)
				if(r[i] > 0)
					break;
			
			//
			alpha[i] = 0.0;
			int j_selected = -1;
			for(int j = 0; j < m; j++)
				if (alpha[i] < v[i][j]/p[j]){
					alpha[i] = v[i][j]/p[j];
					j_selected = j;
				}
			
			// demand set D_i
			
			//
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
			
			// check 2 stop conditions
			boolean stop1 = true;
			for (i = 0; i < n ; i++)
				if (r[i] >= eps/(n*(1+eps))*a_min){
					stop1 = false;
					break;
				}
			
			boolean stop2 = true;
			for (int j = 0; j < m ; j++){
				sum_x = 0.0;
				for (i = 0; i < n; i++)
					sum_x += x[i][j];
					
				if (sum_x != sum_a[j]){
					stop2 = false;
					break;
				}
			}
			
			if (stop1 || stop2)
				break;
		}
		
		System.out.println("Loop terminated");
		System.out.println("Prices :");
		for (int j = 0; j < m; j++)
			System.out.print(p[j] + " ");
		System.out.println();
		
		System.out.println("x[i][j] :");
		for (int i = 0; i < n; i++){
			for (int j = 0; j < m; j++)
				System.out.print(p[j] + " ");
			System.out.println();
		}
		
	}

}
