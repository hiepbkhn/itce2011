/*
 * Sep 22, 2015
 * 	- add epsilonByLevel()
 */

package dp;

import java.util.Random;

public class DPUtil {
	//
	public static int geometricMechanism(double alpha){
		Random random = new Random();
		double u = random.nextDouble();
        if (u <= (1-alpha)/(1+alpha))
            return 0;
        else{
            double s = random.nextDouble();
            Double value = Math.floor(Math.log((1-u)*(1+alpha)/2) / Math.log(alpha));
            if (s < 0.5)
                return -value.intValue();
            else
                return value.intValue();
        }
	}
	
	////
	public static double laplaceMechanism(double eps){
		Random random = new Random();
		double u = random.nextDouble();
		double x = -Math.log(1-u)/eps;
		double s = random.nextDouble();
		if (s < 0.5)
            return -x;
        else
            return x;
	}
	
	////
	public static double max(double[] a){
		double maxVal = a[0];
		for (double d:a)
			if (d > maxVal)
				maxVal = d;
		return maxVal;
	}
	
	////
	public static double min(double[] a){
		double minVal = a[0];
		for (double d:a)
			if (d < minVal)
				minVal = d;
		return minVal;
	}
	
	////
	public static double[] epsilonByLevel(double eps, int max_level, double ratio){
		double[] ret = new double[max_level];
		if (ratio == 1){
			for (int i = 0; i < max_level; i++){
				ret[i] = eps / max_level;
//				System.out.println(ret[i]);
			}
			return ret;
		}
			
		
		double alpha = eps*(ratio-1)/(Math.pow(ratio, max_level) - 1);
		
		double s = 0.0;
		for (int i = 1; i < max_level+1; i++){
			ret[i-1] = alpha*Math.pow(ratio,max_level-i);
//			s += ret[i-1];
//			System.out.println(ret[i-1]);
		}
//		System.out.println("s = " + s);
		
		//
		return ret;
	}
	
	
	////
	public static void main(String[] args) throws Exception{
		// GEOMETRIC
//		double alpha = Math.exp(-1.0);
//		int[] a = new int[1000];
//		for(int i = 0; i < a.length; i++){
//			a[i] = geometricMechanism(alpha);
//			System.out.print(a[i] + " ");
//		}
		
		// LAPLACE
//		double eps = 1.0;
//		double[] a = new double[1000];
//		for(int i = 0; i < a.length; i++){
//			a[i] = laplaceMechanism(eps);
//			System.out.print(a[i] + " ");
//		}
		
		
		// TEST epsilonByLevel()
		double[] ret = epsilonByLevel(10.0, 5, 2);
		ret = epsilonByLevel(10.0, 5, 1);
	}
}
