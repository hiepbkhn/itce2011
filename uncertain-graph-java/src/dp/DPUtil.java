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
	public static void main(String[] args) throws Exception{
		// GEOMETRIC
//		double alpha = Math.exp(-1.0);
//		int[] a = new int[1000];
//		for(int i = 0; i < a.length; i++){
//			a[i] = geometricMechanism(alpha);
//			System.out.print(a[i] + " ");
//		}
		
		// LAPLACE
		double eps = 1.0;
		double[] a = new double[1000];
		for(int i = 0; i < a.length; i++){
			a[i] = laplaceMechanism(eps);
			System.out.print(a[i] + " ");
		}
	}
}
