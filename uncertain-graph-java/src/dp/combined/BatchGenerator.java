/*
 * Oct 11, 2015
 * 	- generate batch files (.cmd)
 * Oct 12
 * 	- generateLouvain() for "_ef_", "_tmf_", "_ldp_"
 * Oct 13
 * 	- measureABC,... (the same parameter list as generateABC functions)
 * Nov 9
 * 	- generateMCMCInference(), generateMCMCInferenceFixed()
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
			String cmd = "java naive.GreedyReconstruct " + prefix + " " + dataname + " " + n_samples + " " + String.format("%.2f", eps) + 
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
	
	////LouvainOpt
	public static void generateLouvainOpt(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int[] maxLevelArr, int[] kArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (int i = 0; i < kArr.length; i++){
			int k = kArr[i];
			int max_level = maxLevelArr[i];
			String cmd = "java dp.combined.LouvainOpt " + prefix + " " + dataname + " " + n_samples + " " + 
					burn_factor + " " + max_level + " " + k + 
					" > ../_console/" + dataname + "_nmd_" + burn_factor + "_" + max_level + "_" + k + "-CONSOLE.txt";
			bw.write(cmd + "\n");
		}
		
		bw.close();
	}
	
	////LouvainModDiv
	public static void generateLouvainModDiv(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int[] maxLevelArr, int[] kArr, double[] epsArr, double ratio) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			for (int i = 0; i < kArr.length; i++){
				int k = kArr[i];
				int max_level = maxLevelArr[i];
				String cmd = "java dp.combined.LouvainModDiv " + prefix + " " + dataname + " " + n_samples + " " + 
						burn_factor + " " + max_level + " " + k + " " + String.format("%.1f", eps) + " " + String.format("%.2f", ratio) + 
						" > ../_console/" + dataname + "_md_" + burn_factor + "_" + 
						max_level + "_" + k + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		
		bw.close();
	}
	
	////HRGDivisiveGreedy
	public static void generateHRGDiv(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int max_level, double[] epsArr, double[] ratioArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		
		for (double ratio : ratioArr){
			for (double eps : epsArr){
				String cmd = "java dp.combined.HRGDivisiveGreedy " + prefix + " " + dataname + " " + n_samples + " " + 
						burn_factor + " " + max_level + " " + String.format("%.1f", eps) + " " + String.format("%.2f", ratio) + 
						" > ../_console/" + dataname + "_hd_" + burn_factor + "_" + 
						max_level + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		
		bw.close();
	}
	
	/////////
	// algo = "_ef_", "_tmf_", "_ldp_"
	public static void generateLouvain(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr, String algo, int type) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			String sample_name = dataname + algo + String.format("%.1f", eps);
			
			String cmd = "java dp.combined.Louvain " + prefix + " " + n_samples + " " + type + " " + sample_name + 
					" > ../_console/" + dataname + "_ef_" + String.format("%.1f", eps) + "-LOUVAIN.txt";
			bw.write(cmd + "\n");
		}
    	bw.close();
	}
	
	//////to automate CommunityMeasure.computeAndExport()
	public static void measureEdgeFlip(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			String sample_file = dataname + "_ef_" + String.format("%.1f", eps);
			String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + sample_file;
					
			bw.write(cmd + "\n");
		}
 	bw.close();
		
	}
	
	public static void measureTmF(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			String sample_file = dataname + "_tmf_" + String.format("%.1f", eps);
			String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + sample_file;
			
			bw.write(cmd + "\n");
		}
		bw.close();
	}
	
	public static void measureLouvainDP(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr, int[] kArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			for (int k : kArr){
				String sample_file = dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k;
				String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + sample_file;
				
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		bw.close();
	}
	
	public static void measureLouvainOpt(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int[] maxLevelArr, int[] kArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (int i = 0; i < kArr.length; i++){
			int k = kArr[i];
			int max_level = maxLevelArr[i];
			
			String sample_file = dataname + "_nmd_" + burn_factor + "_" + max_level + "_" + k;
			String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + sample_file;
			
			bw.write(cmd + "\n");
		}
		
		bw.close();
	}
	
	public static void measureLouvainModDiv(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int[] maxLevelArr, int[] kArr, double[] epsArr, double ratio) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			for (int i = 0; i < kArr.length; i++){
				int k = kArr[i];
				int max_level = maxLevelArr[i];
				
				String sample_file = dataname + "_md_" + burn_factor + "_" + 
						max_level + "_" + k + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio);
				String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + sample_file;
				
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		
		bw.close();
	}
	
	public static void measureHRGDivisiveGreedy(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int max_level, double[] epsArr, double[] ratioArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (int i = 0; i < ratioArr.length; i++){
			double ratio = ratioArr[i];
			for (double eps : epsArr){
				String sample_file = dataname + "_hd_" + burn_factor + "_" + 
						max_level + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio);
				String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + sample_file;
				
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		
		bw.close();
	}
	
	// against LouvainOpt (not Louvain)
	public static void measureLouvainModDivAgainstOpt(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int[] maxLevelArr, int[] kArr, double[] epsArr, double ratio) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			for (int i = 0; i < kArr.length; i++){
				int k = kArr[i];
				int max_level = maxLevelArr[i];
				
				String louvainopt_file = dataname + "_nmd_" + burn_factor + "_" + 
						max_level + "_" + k;
				
				String sample_file = dataname + "_md_" + burn_factor + "_" + 
						max_level + "_" + k + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio);
				
				String cmd = "java dp.combined.CommunityMeasure " + prefix + " " + dataname + " " + n_samples + " " + 
						louvainopt_file + " " + sample_file;
				
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		
		bw.close();
	}
	
	//// Dendrogram (MCMCInference)
	public static void generateMCMCInference(String batch_file, String prefix, String dataname, int n_samples, 
			int sample_freq, int burn_factor, double[] epsArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps1 : epsArr){
			String cmd = "java dp.mcmc.MCMCInference " + prefix + " " + dataname + " " + n_samples + 
								" " + sample_freq + " " + burn_factor + " " + String.format("%.1f", eps1) + 
					" > ../_console/" + dataname + "_dendro_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + String.format("%.1f", eps1) + "-CONSOLE.txt";
			bw.write(cmd + "\n");
		}
		
   	bw.close();
		
	}
	
	//// DendrogramFixed (MCMCInferenceFixed)
	public static void generateMCMCInferenceFixed(String batch_file, String prefix, String dataname, int n_samples, 
			int sample_freq, int burn_factor, double[] epsArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps1 : epsArr){
			String cmd = "java dp.mcmc.MCMCInferenceFixed " + prefix + " " + dataname + " " + n_samples + 
								" " + sample_freq + " " + burn_factor + " " + String.format("%.1f", eps1) +
					" > ../_console/" + dataname + "_fixed_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + String.format("%.1f", eps1) + "-CONSOLE.txt";
			bw.write(cmd + "\n");
		}
		
    	bw.close();
		
	}
	
	//// NodeSetDivGreedy (HRGDivisiveFit)
	public static void generateHRGDivisiveFit(String batch_file, String prefix, String dataname, int n_samples, 
			int burn_factor, int max_level, int lower_size, double[] epsArr, double[] ratioArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double ratio : ratioArr){
			for (double eps1 : epsArr){
				String cmd = "java dp.comm.HRGDivisiveFit " + prefix + " " + dataname + " " + n_samples + " " + 
						burn_factor + " " + max_level + " " + lower_size + " " + String.format("%.1f", eps1) + " " + String.format("%.2f", ratio) + 
						" > ../_console/" + dataname + "_hrgdiv_" + burn_factor + "_" + 
						max_level + "_" + lower_size + "_" + String.format("%.1f", eps1) + "_" + String.format("%.2f", ratio) + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}
		
   	bw.close();
		
	}
	
	//// DER
	// ratio [][3]: ex. of a row: 4:1:2
	public static void generateDER(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr, double[][] ratio) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			for (int i = 0; i < ratio.length; i++){
				double sum_row = ratio[i][0] + ratio[i][1] + ratio[i][2];
				double eps_c =  ratio[i][0]/ sum_row * eps;
				double eps_p =  ratio[i][1]/ sum_row * eps;
				double epsA =  ratio[i][2]/ sum_row * eps;
				
				String java_cmd = "java";
				if (dataname.contains("ca-AstroPh"))
					java_cmd = "java -Xmx7000m";
				
				String cmd = java_cmd + " dp.der.DensityExploreReconstruct " + prefix + " " + dataname + " " + n_samples + " " + String.format("%.1f", eps_c) + 
						" " + String.format("%.1f", eps_p) + " " + String.format("%.1f", epsA) +
						" > ../_console/" + dataname + "_der_" + String.format("%.1f", eps_c) + 
						"_" + String.format("%.1f", eps_p) + "_" + String.format("%.1f", epsA) + "-CONSOLE.txt";
				bw.write(cmd + "\n");
				
			}
			bw.write("\n");
		}
   	bw.close();
	}
	
	//// 1k-Series
	public static void generate1kSeries(String batch_file, String prefix, String dataname, int n_samples, double[] epsArr) 
			throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double eps : epsArr){
			String cmd = "java dp.generator.Orbis " + prefix + " " + dataname + " " + n_samples + " " + String.format("%.2f", eps) + 
					" > ../_console/" + dataname + "_1k_" + String.format("%.1f", eps) + "-CONSOLE.txt";
			bw.write(cmd + "\n");
		}
		bw.close();
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		String prefix = "../";		// run in D:/git/itce2011/uncertain-graph-java/_cmd
		
		String[] dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc",
				"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
		int[] n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903, 334863, 317080, 1134890};
		// for TEST
//		String[] dataname_list = new String[]{"karate"};
//		int[] n_list = new int[]{34};
		
		int n_samples = 20;
	
		
		
		
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
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd/LouvainDP_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 1.5*log_n, 2*log_n, 3*log_n};
//			int[] kArr = new int[]{4,8,16,32,64};
//			
//			
//			
//			generateLouvainDP(batch_file, prefix, dataname, n_samples, epsArr, kArr);
//			System.out.println("DONE.");
//		}
		
		// LouvainOpt
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd/LouvainOpt_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			int burn_factor = 20;
//			
//			generateLouvainOpt(batch_file, prefix, dataname, n_samples, burn_factor, maxLevelArr, kArr);
//			System.out.println("DONE.");
//		}
		
		// LouvainModDiv
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd/LouvainModDiv_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			int burn_factor = 20;
//			double ratio = 2.0;
//			
//			generateLouvainModDiv(batch_file, prefix, dataname, n_samples, burn_factor, maxLevelArr, kArr, epsArr, ratio);
//			System.out.println("DONE.");
//		}
		
		// HRGDivisiveGreedy
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd/HRGDivisiveGreedy_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			int max_level = 10;
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			int burn_factor = 20;
//			double[] ratioArr = new double[]{2.0, 1.5, 1.0};
//			
//			generateHRGDiv(batch_file, prefix, dataname, n_samples, burn_factor, max_level, epsArr, ratioArr);
//			System.out.println("DONE.");
//		}
		
		
		// MCMCInference
//		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc"};
//		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903};
//		int burn_factor = 1000;
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd2/MCMCInference_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			int sample_freq = n;
//			
//			generateMCMCInference(batch_file, prefix, dataname, n_samples, sample_freq, burn_factor, epsArr);
//			System.out.println("DONE.");
//		}
		
		// MCMCInferenceFixed
//		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc", 
//				"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
//		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903,
//				334863, 317080, 1134890};
//		burn_factor = 1000;
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd2/MCMCInferenceFixed_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			int sample_freq = n;
//			
//			generateMCMCInferenceFixed(batch_file, prefix, dataname, n_samples, sample_freq, burn_factor, epsArr);
//			System.out.println("DONE.");
//		}
		
		// HRGDivisiveFit
//		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc", 
//				"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
//		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903,
//				334863, 317080, 1134890};
//		int[] max_level_list = new int[]{4, 6, 7, 7, 8, 8,
//				11, 11, 12};
//		int[] lower_size_list = new int[]{2, 2, 2, 2, 2, 2,
//				2, 2, 2};
//		int burn_factor = 20;
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			int max_level = max_level_list[i];
//			int lower_size = lower_size_list[i];
//			
//			//
//			String batch_file = "_cmd2/HRGDivisiveFit_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			double[] ratioArr = new double[]{2.0, 1.5, 1.0};
//			
//			generateHRGDivisiveFit(batch_file, prefix, dataname, n_samples, burn_factor, max_level, lower_size, epsArr, ratioArr);
//			System.out.println("DONE.");
//		}
		
		// DER
//		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc"};
//		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903};
//		double[][] ratio = new double[][]{{1,1,1}, {2,1,1}, {4,1,1}, {4,2,1}, {8,4,1}};
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			
//			//
//			String batch_file = "_cmd2/DER_" + dataname + ".cmd";
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			generateDER(batch_file, prefix, dataname, n_samples, epsArr, ratio);
//			System.out.println("DONE.");
//		}

		// 1k-Series
		for (int i = 0; i < n_list.length; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			
			//
			String batch_file = "_cmd2/1k_" + dataname + ".cmd";
			double log_n = Math.log(n);
			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
			
			generate1kSeries(batch_file, prefix, dataname, n_samples, epsArr);
			System.out.println("DONE.");
		}
		
		//////////////////////////////// LOUVAIN
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			String batch_file = "_cmd/Louvain_" + dataname + ".cmd";
//			generateLouvain(batch_file, prefix, dataname, n_samples, epsArr, "_ef_", 1);
//			
//			System.out.println("DONE.");
//		}
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			String batch_file = "_cmd/Louvain_" + dataname + ".cmd";
//			generateLouvain(batch_file, prefix, dataname, n_samples, epsArr, "_tmf_", 1);
//			
//			System.out.println("DONE.");
//		}
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{0.25*log_n, 0.5*log_n, log_n};
//			int[] kArr = new int[]{4,8,16,32,64};
//			
//			String batch_file = "_cmd/Louvain_" + dataname + ".cmd";
//			generateLouvain(batch_file, prefix, dataname, n_samples, epsArr, "_ldp_", 1);
//			
//			System.out.println("DONE.");
//		}
		
		
		//////////////////////////////// COMMUNITY METRICS
		for (int i = 0; i < n_list.length; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			
			//
			double log_n = Math.log(n);
			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};

//			//
//			String batch_file = "_cmd/Metric_EdgeFlip_" + dataname + ".cmd";
//			measureEdgeFlip(batch_file, prefix, dataname, n_samples, epsArr);
//			System.out.println("measureEdgeFlip - DONE.");
//			
//			//
//			batch_file = "_cmd/Metric_TmF_" + dataname + ".cmd";
//			measureTmF(batch_file, prefix, dataname, n_samples, epsArr);
//			System.out.println("measureTmF - DONE.");
//			
//			//
//			batch_file = "_cmd/Metric_LouvainDP_" + dataname + ".cmd";
//			int[] kArr = new int[]{2,4,8,16,32,64,128};
//			measureLouvainDP(batch_file, prefix, dataname, n_samples, epsArr, kArr);
//			System.out.println("measureLouvainDP - DONE.");
			
			
			//
//			String batch_file = "_cmd/Metric_LouvainOpt_" + dataname + ".cmd";
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			int burn_factor = 20;
//			measureLouvainOpt(batch_file, prefix, dataname, n_samples, burn_factor, maxLevelArr, kArr);
//			System.out.println("measureLouvainOpt - DONE.");
			
			//
//			String batch_file = "_cmd/Metric_LouvainModDiv_" + dataname + ".cmd";
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			int burn_factor = 20;
//			double ratio = 2.0;
//			measureLouvainModDiv(batch_file, prefix, dataname, n_samples, burn_factor, maxLevelArr, kArr, epsArr, ratio);
//			System.out.println("measureLouvainModDiv - DONE.");
			
			//
//			String batch_file = "_cmd/Metric_LouvainModDiv_Opt_" + dataname + ".cmd";
//			int[] kArr = new int[]{2,3,4,5,6,10};
//			int[] maxLevelArr = new int[]{10,7,5,4,4,3};
//			int burn_factor = 20;
//			double ratio = 2.0;
//			measureLouvainModDivAgainstOpt(batch_file, prefix, dataname, n_samples, burn_factor, maxLevelArr, kArr, epsArr, ratio);
//			System.out.println("measureLouvainModDivAgainstOpt - DONE.");
			
			//
//			String batch_file = "_cmd/Metric_HRGDivisiveGreedy_" + dataname + ".cmd";
//			int burn_factor = 20;
//			int max_level = 10;
//			double[] ratioArr = new double[]{2.0, 1.5, 1.0};
//			measureHRGDivisiveGreedy(batch_file, prefix, dataname, n_samples, burn_factor, max_level, epsArr, ratioArr);
//			System.out.println("measureHRGDivisiveGreedy - DONE.");
		}
		
	}

}
