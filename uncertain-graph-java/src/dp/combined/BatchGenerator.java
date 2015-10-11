/*
 * Oct 11, 2015
 * 	- generate batch files (.cmd)
 */

package dp.combined;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class BatchGenerator {
	
	//// EdgeFlip
	public static void generateEdgeFlip(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			String cmd = "java dp.combined.EdgeFlip " + prefix + " " + dataname + " " + n_samples + " " + String.format("%.2f", eps) + 
					" > ../_console/" + dataname + "_ef_" + String.format("%.1f", eps) + "-CONSOLE.txt";
			bw.write(cmd + "\n");
		}
    	bw.close();
	}
	
	//// TmF
	public static void generateTmF(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			String cmd = "java dp.naive.GreedyReconstruct " + prefix + " " + dataname + " " + n_samples + " " + String.format("%.2f", eps) + 
					" > ../_console/" + dataname + "_tmf_" + String.format("%.1f", eps) + "-CONSOLE.txt";
			bw.write(cmd + "\n");
		}
		bw.close();
	}
	
	//// LouvainDP
	public static void generateLouvainDP(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr, int[] kArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			for (int k : kArr){
				String cmd = "java dp.combined.LouvainDP " + prefix + " " + dataname + " " + n_samples + " " + 
						String.format("%.2f", eps) + " " + k + 
						" > ../_console/" + dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		bw.close();
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		String prefix = "../";		// run in D:/git/itce2011/uncertain-graph-java/_cmd
		String[] dataname_list = new String[]{"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
		int n_samples = 20;
		int[] n_list = new int[]{334863, 317080, 1134890};
		
		
		
		
		
		// EdgeFlip
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd/EdgeFlip_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			
//			generateEdgeFlip(batch_file, prefix, dataname, n_samples, epsArr);
//			System.out.println("DONE.");
//		}
		
		// TmF
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd/TmF_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			
//			generateTmF(batch_file, prefix, dataname, n_samples, epsArr);
//			System.out.println("DONE.");
//		}
		
		// LouvainDP
		for (int i = 0; i < n_list.length; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			
			//
			String batch_file = "_cmd/LouvainDP_" + dataname + ".cmd";
			double log_n = Math.log(n);
			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
			int[] kArr = new int[]{2,4,8,16,32,64,128};
			
			
			
			generateLouvainDP(batch_file, prefix, dataname, n_samples, epsArr, kArr);
			System.out.println("DONE.");
		}
	}

}
