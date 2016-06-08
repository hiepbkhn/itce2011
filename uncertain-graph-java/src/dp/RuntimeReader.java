/*
 * Nov 30, 2015
 * 	- see dp.combined.RuntimeParser.java
 */

package dp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLInt32;

public class RuntimeReader {

	
	////
	public static void readTmF(String prefix, String dataname, int n_samples, double eps) 
			throws IOException{
		String timeStr = "filterLaplace - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		
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
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_tmf_" + String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
		
	////
	public static void readEdgeFlip(String prefix, String dataname, int n_samples, double eps) 
			throws IOException{
		String timeStr = "perturbGraph - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_ef_" + String.format("%.1f", eps) + "-CONSOLE.txt";
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
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_ef_" + String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void read1k(String prefix, String dataname, int n_samples, double eps) 
			throws IOException{
		String adjustStr = "adjustDegreeSequence - DONE, elapsed";
		String timeStr = "dkTopoGen1k - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_1k_" + String.format("%.1f", eps) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > adjustStr.length())
	        	if (str.substring(0, adjustStr.length()).equals(adjustStr))
	        		timeArr[count] = Integer.parseInt(str.substring(adjustStr.length()+1));
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeArr[count++] += Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_1k_" + String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readDER(String prefix, String dataname, int n_samples, double eps_c, double eps_p, double epsA) 
			throws IOException{
		String aStr = "compute_A - DONE, elapsed";
		String cStr = "compute_C - DONE, elapsed";
		
		String exploreStr = "exploreDenseRegion - DONE, elapsed";
		String arrangeStr = "arrangeEdge - DONE, elapsed";
		int[] timeArr = new int[n_samples];
		int aTime = 0;
		int cTime = 0;
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_der_" + String.format("%.1f", eps_c) + 
				"_" + String.format("%.1f", eps_p) + "_" + String.format("%.1f", epsA) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > aStr.length())
	        	if (str.substring(0, aStr.length()).equals(aStr))
	        		aTime = Integer.parseInt(str.substring(aStr.length()+1));
        	if (str.length() > cStr.length())
	        	if (str.substring(0, cStr.length()).equals(cStr))
	        		cTime = Integer.parseInt(str.substring(cStr.length()+1));
        	
        	
        	if (str.length() > exploreStr.length())
	        	if (str.substring(0, exploreStr.length()).equals(exploreStr))
	        		timeArr[count] = Integer.parseInt(str.substring(exploreStr.length()+1));
        	if (str.length() > arrangeStr.length())
	        	if (str.substring(0, arrangeStr.length()).equals(arrangeStr))
	        		timeArr[count++] += Integer.parseInt(str.substring(arrangeStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_der_" + String.format("%.1f", eps_c) + 
				"_" + String.format("%.1f", eps_p) + "_" + String.format("%.1f", epsA) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeArr", timeArr, 1);
    	MLInt32 timeAux = new MLInt32("timeAux", new int[]{aTime, cTime}, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA);
        towrite.add(timeAux); 
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readHRGMCMC(String prefix, String dataname, int n_samples, int sample_freq, int burn_factor, double eps) 
			throws IOException{
		String timeStr = "dendrogramFitting - DONE, elapsed";
		String sampleStr = "generateSanitizedSample - DONE, elapsed";
		int timeFit = 0;
		int[] sampleArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_dendro_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeFit = Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// -SAMPLE.txt
    	console_file = prefix + "_console/" + dataname + "_dendro_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "_tree_1.0" + "-SAMPLE.txt";
		br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > sampleStr.length())
	        	if (str.substring(0, sampleStr.length()).equals(sampleStr))
	        		sampleArr[count++] = Integer.parseInt(str.substring(sampleStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_dendro_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeFit", new int[]{timeFit}, 1);
    	MLInt32 sampleA = new MLInt32("sampleArr", sampleArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        towrite.add(sampleA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readHRGDivisive(String prefix, String dataname, int n_samples, int burn_factor, int max_level, int lower_size, double eps, double ratio) 
			throws IOException{
		String timeStr1 = "recursiveLK - DONE, elapsed";
		String timeStr2 = "buildDendrogram - DONE, elapsed";
		String timeStr3 = "convertToHRG - DONE, elapsed";
		String sampleStr = "generateSanitizedSample - DONE, elapsed";
		int[] timeFit1 = new int[n_samples];
		int[] timeFit2 = new int[n_samples];
		int[] timeFit3 = new int[n_samples];
		int[] sampleArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_hrgdiv_" + burn_factor + "_" + 
				max_level + "_" + lower_size + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr1.length())
	        	if (str.substring(0, timeStr1.length()).equals(timeStr1))
	        		timeFit1[count] = Integer.parseInt(str.substring(timeStr1.length()+1));
        	if (str.length() > timeStr2.length())
	        	if (str.substring(0, timeStr2.length()).equals(timeStr2))
	        		timeFit2[count] = Integer.parseInt(str.substring(timeStr2.length()+1));
        	if (str.length() > timeStr3.length())
	        	if (str.substring(0, timeStr3.length()).equals(timeStr3))
	        		timeFit3[count++] = Integer.parseInt(str.substring(timeStr3.length()+1));
        	
		}
    	br.close();
    	
    	// -SAMPLE.txt
    	console_file = prefix + "_console/" + dataname + "_hrgdiv_" + burn_factor + "_" + 
				max_level + "_" + lower_size + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_tree_1.0" + "-SAMPLE.txt";
		br = new BufferedReader(new FileReader(console_file));
		count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > sampleStr.length())
	        	if (str.substring(0, sampleStr.length()).equals(sampleStr))
	        		sampleArr[count++] = Integer.parseInt(str.substring(sampleStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_hrgdiv_" + burn_factor + "_" + 
				max_level + "_" + lower_size + "_" + String.format("%.1f", eps) + "_" + String.format("%.2f", ratio) + "_runtime.mat";
		
    	MLInt32 time1 = new MLInt32("timeFit1", timeFit1, 1);
    	MLInt32 time2 = new MLInt32("timeFit2", timeFit2, 1);
    	MLInt32 time3 = new MLInt32("timeFit3", timeFit3, 1);
    	MLInt32 sampleA = new MLInt32("sampleArr", sampleArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(time1);
        towrite.add(time2);
        towrite.add(time3);
        towrite.add(sampleA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////
	public static void readHRGFixed(String prefix, String dataname, int n_samples, int sample_freq, int burn_factor, double eps) 
			throws IOException{
		String timeStr = "dendrogramFitting - DONE, elapsed";
		String sampleStr = "generateSanitizedSample - DONE, elapsed";
		int timeFit = 0;
		int[] sampleArr = new int[n_samples];
		
		// -CONSOLE.txt
		String console_file = prefix + "_console/" + dataname + "_fixed_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "-CONSOLE.txt";
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > timeStr.length())
	        	if (str.substring(0, timeStr.length()).equals(timeStr))
	        		timeFit = Integer.parseInt(str.substring(timeStr.length()+1));
        	
		}
    	br.close();
    	
    	// -SAMPLE.txt
    	console_file = prefix + "_console/" + dataname + "_fixed_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "_tree_1.0" + "-SAMPLE.txt";
		br = new BufferedReader(new FileReader(console_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > sampleStr.length())
	        	if (str.substring(0, sampleStr.length()).equals(sampleStr))
	        		sampleArr[count++] = Integer.parseInt(str.substring(sampleStr.length()+1));
        	
		}
    	br.close();
    	
    	// write to MATLAB
    	String matlab_file = prefix + "_runtime/" + dataname + "_fixed_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_" + 
				String.format("%.1f", eps) + "_runtime.mat";
		
    	MLInt32 timeA = new MLInt32("timeFit", new int[]{timeFit}, 1);
    	MLInt32 sampleA = new MLInt32("sampleArr", sampleArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(timeA); 
        towrite.add(sampleA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{

		String prefix = "";		// run in D:/git/itce2011/uncertain-graph-java/_cmd2
		
		String[] dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc",
				"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
		int[] n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903, 334863, 317080, 1134890};
		
		int n_samples = 20;
		
		// TmF, EdgeFlip, 1k-series
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
////			double[] epsArr = new double[]{log_n, 1.5*log_n, 2*log_n, 3*log_n};									// EdgeFlip
//			
//			for (double eps : epsArr){
//				readTmF(prefix, dataname, n_samples, eps);
////				readEdgeFlip(prefix, dataname, n_samples, eps);
////				read1k(prefix, dataname, n_samples, eps);
//				
//			}
//			
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
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			for (double eps : epsArr){
//				for (int j = 0; j < ratio.length; j++){
//					double sum_row = ratio[j][0] + ratio[j][1] + ratio[j][2];
//					double eps_c =  ratio[j][0]/ sum_row * eps;
//					double eps_p =  ratio[j][1]/ sum_row * eps;
//					double epsA =  ratio[j][2]/ sum_row * eps;
//					
//					readDER(prefix, dataname, n_samples, eps_c, eps_p, epsA);
//				}
//			}
//			System.out.println("DONE.");
//		}
		
		
		// HRG-MCMC (MCMCInference)
//		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc"};
//		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903};
//		int burn_factor = 1000;
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			int sample_freq = n;
//			for (double eps : epsArr){
//				readHRGMCMC(prefix, dataname, n_samples, sample_freq, burn_factor, eps);
//				
//			}
//			
//			System.out.println("DONE.");
//		}
		
		// HRG-Divisive
//		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc", 
//				"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
//		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903,
//				334863, 317080, 1134890};
//		int[] max_level_list = new int[]{4, 6, 7, 7, 8, 8,
//				11, 11, 12};
//		int[] lower_size_list = new int[]{2, 2, 2, 2, 2, 2,
//				2, 2, 2};
//		int burn_factor = 20;
//		double[] ratioArr = new double[]{2.0, 1.5, 1.0};
//		
//		for (int i = 0; i < n_list.length; i++){
//			String dataname = dataname_list[i];
//			int n = n_list[i];
//			int max_level = max_level_list[i];
//			int lower_size = lower_size_list[i];
//			
//			double log_n = Math.log(n);
//			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
//			
//			for (double eps : epsArr){
//				for (double ratio : ratioArr){
//					readHRGDivisive(prefix, dataname, n_samples, burn_factor, max_level, lower_size, eps, ratio);
//				}
//				
//			}
//			
//			System.out.println("DONE.");
//		}
		
		// HRG-Fixed
		dataname_list = new String[]{"polbooks", "polblogs-wcc", "as20graph", "wiki-Vote-wcc", "ca-HepPh-wcc", "ca-AstroPh-wcc", 
				"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
		n_list = new int[]{105, 1222, 6474, 7066, 11204, 17903,
				334863, 317080, 1134890};
		int burn_factor = 1000;
		
		for (int i = 0; i < n_list.length; i++){
			String dataname = dataname_list[i];
			int n = n_list[i];
			double log_n = Math.log(n);
			double[] epsArr = new double[]{2.0, 0.25*log_n, 0.5*log_n, log_n, 1.5*log_n, 2*log_n, 3*log_n};
			
			int sample_freq = n;
			for (double eps : epsArr){
				readHRGFixed(prefix, dataname, n_samples, sample_freq, burn_factor, eps);
				
			}
			
			System.out.println("DONE.");
		}
		
	}

}
