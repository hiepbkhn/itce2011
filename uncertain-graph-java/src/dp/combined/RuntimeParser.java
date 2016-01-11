/*
 * Oct 15, 2015
 * 	- read -CONSOLE.txt, -LOUVAIN.txt to extract runtime and export to MATLAB
 */

package dp.combined;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

public class RuntimeParser {

	
	////
	public static void read1k(String prefix, String dataname, int n_samples, double eps) 
			throws IOException{
		String timeStr = "perturbGraph - DONE, elapsed";
		String louvainStr = "best_partition - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		int[] louvainArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_1k_" + String.format("%.1f", eps) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeArr[count++] = Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// -LOUVAIN.txt
    	console_file = prefix + "_console/" + dataname + "_1k_" + String.format("%.1f", eps) + "-LOUVAIN.txt";
		br = new BufferedReader(new FileReader(console_file));
		count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_1k_" + String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readEdgeFlip(String prefix, String dataname, int n_samples, double eps) 
			throws IOException{
		String timeStr = "perturbGraph - DONE, elapsed";
		String louvainStr = "best_partition - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		int[] louvainArr = new int[n_samples];
		
		// -CONSOLE.txt
//		String console_file = prefix + "_console/" + dataname + "_ef_" + String.format("%.1f", eps) + "-CONSOLE.txt";
		String console_file = prefix + "_console/" + dataname + "_ef_shrink_" + String.format("%.1f", eps) + "_1.0-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeArr[count++] = Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// -LOUVAIN.txt
//    	console_file = prefix + "_console/" + dataname + "_ef_" + String.format("%.1f", eps) + "-LOUVAIN.txt";
    	console_file = prefix + "_console/" + dataname + "_ef_shrink_" + String.format("%.1f", eps) + "_1.0-LOUVAIN.txt";
		br = new BufferedReader(new FileReader(console_file));
		count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
//    	String matlab_file = prefix + "_runtime/" + dataname + "_ef_" + String.format("%.1f", eps) + "_runtime.mat";
    	String matlab_file = prefix + "_runtime/" + dataname + "_ef_shrink_" + String.format("%.1f", eps) + "_1.0_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readTmF(String prefix, String dataname, int n_samples, double eps) 
			throws IOException{
		String timeStr = "filterLaplace - DONE, elapsed";
		String louvainStr = "best_partition - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		int[] louvainArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_tmf_" + String.format("%.1f", eps) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeArr[count++] = Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// -LOUVAIN.txt
    	console_file = prefix + "_console/" + dataname + "_tmf_" + String.format("%.1f", eps) + "-LOUVAIN.txt";
		br = new BufferedReader(new FileReader(console_file));
		count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_tmf_" + String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readLouvainDP(String prefix, String dataname, int n_samples, double eps, int k) 
			throws IOException{
		String timeStr = "genEqualPrivate - DONE, elapsed";
		String louvainStr = "best_partition - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		int[] louvainArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeArr[count++] = Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// -LOUVAIN.txt
    	console_file = prefix + "_console/" + dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k + "-LOUVAIN.txt";
		br = new BufferedReader(new FileReader(console_file));
		count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readLouvainOpt(String prefix, String dataname, int n_samples, int burn_factor, int max_level, int k) 
			throws IOException{
		String louvainStr = "recursiveLouvain - DONE, elapsed";
		int[] louvainArr = new int[n_samples];
		
    	// -CONSOLE.txt
    	String console_file = prefix + "_console/" + dataname + "_nmd_" + burn_factor + "_" + max_level + "_" + k + "-CONSOLE.txt";
    	BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_nmd_" + burn_factor + "_" + max_level + "_" + k + "_runtime.mat";
		
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readLouvainModDiv(String prefix, String dataname, int n_samples, int burn_factor, int max_level, int k, double eps, double ratio) 
			throws IOException{
		String louvainStr = "recursiveMod - DONE, elapsed";
		int[] louvainArr = new int[n_samples];
		
    	// -CONSOLE.txt
    	String console_file = prefix + "_console/" + dataname + "_md_" + burn_factor + "_" + 
				max_level + "_" + k + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "-CONSOLE.txt";
    	BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_md_" + burn_factor + "_" + 
				max_level + "_" + k + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_runtime.mat";
		
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readHRGFixed(String prefix, String dataname, int n_samples, int burn_factor, int n_nodes, double eps) 
			throws IOException{
		String louvainStr = "dendrogramFitting - DONE, elapsed";
		int[] louvainArr = new int[n_samples];
		
    	// -CONSOLE.txt
    	String console_file = prefix + "_console/" + dataname + "_fixed_" + n_samples + "_" + n_nodes + "_" + burn_factor + "_" + 
    					String.format("%.1f", eps) + "-CONSOLE.txt";
    	BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > louvainStr.length())
	        	if (str.substring(0, louvainStr.length()).equals(louvainStr))
	        		louvainArr[count++] = Integer.parseInt(str.substring(louvainStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_fixed_" + n_samples + "_" + n_nodes + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 louvainA = new MLInt32("louvainArr", louvainArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(louvainA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		String prefix = "";		// run in D:/git/itce2011/uncertain-graph-java/_cmd
		
		String[] dataname_list = new String[]{"ca-AstroPh-wcc", "com_dblp_ungraph", "com_youtube_ungraph", "com_amazon_ungraph"}; // com_amazon_ungraph
		int[] n_list = new int[]{17903, 317080, 1134890, 334863}; //334863
		// for TEST
//		String[] dataname_list = new String[]{"karate"};
//		int[] n_list = new int[]{34};
		
		int n_samples = 20;
		
		// 1K, EdgeFlip, TmF
//		for (int i = 0; i < 3; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n}; //
//			
//			for (double eps : epsArr){
//				read1k(prefix, dataname, n_samples, eps);
////				readEdgeFlip(prefix, dataname, n_samples, eps);
//				readTmF(prefix, dataname, n_samples, eps);
//			}
//			
//			System.out.println("DONE.");
//		}

		// LouvainDP
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n};
//			int[] kArr = new int[]{4,8,16,32,64};
//			
//			for (double eps : epsArr)
//				for (int k : kArr){
//				readLouvainDP(prefix, dataname, n_samples, eps, k);
//			}
//			
//			System.out.println("DONE.");
//		}
		
		// LouvainOpt
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			int burn_factor = 20;
//			
//			for (int j = 0; j < kArr.length; j++){
//				int k = kArr[j];
//				int max_level = maxLevelArr[j];
//				readLouvainOpt(prefix, dataname, n_samples, burn_factor, max_level, k);
//			}
//			
//			System.out.println("DONE.");
//		}

		// LouvainModDiv
//		for (int i = 3; i < 4; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			double log_n = Math.log(n);
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n};
//			int burn_factor = 50;
//			double ratio = 2.0;
//			for (double eps : epsArr){
//				for (int j = 0; j < kArr.length; j++){
//					int k = kArr[j];
//					int max_level = maxLevelArr[j];
//					readLouvainModDiv(prefix, dataname, n_samples, burn_factor, max_level, k, eps, ratio);
//				}
//			}
//			System.out.println("DONE.");
//		}
		
		// HRGFixed
		for (int i = 0; i < 3; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			
			double log_n = Math.log(n);
			double[] epsArr = new double[]{0.1*log_n, 0.2*log_n, 0.3*log_n, 0.4*log_n, 0.5*log_n};
			int burn_factor = 1000;
			for (double eps : epsArr){
				readHRGFixed(prefix, dataname, n_samples, burn_factor, n, eps);
			}
			System.out.println("DONE.");
		}
	}

}
