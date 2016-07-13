/*
 * Jun 22, 2016
 * 	- exportCountFileMATLAB()
 * Jun 24
 * 	- readBaseline(), readBloomFilter()
 */


package dsn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

public class RuntimeParser {

	//// read .cnt files
	public static void readCount(String count_file, String matlab_file, int nRun, int round) throws IOException{
		System.out.println("count_file = " + count_file);
		
		int n = 10000;
		double[] trueLinks = new double[round*n];
		double[] falseLinks = new double[round*n];
		
		for (int t = 1; t < round+1; t++){
			for (int i = 0; i < nRun; i++){
				BufferedReader br = new BufferedReader(new FileReader(count_file + "-" + t + ".cnt." + i));
				
				for (int u = 0; u < n; u++){
					String str = br.readLine();
					String[] items = str.split("\t");
					trueLinks[t-1 + u*round] += Integer.parseInt(items[0]);
					falseLinks[t-1 + u*round] += Integer.parseInt(items[1]);
				}
				
				br.close();
			}
			
			for (int u = 0; u < n; u++){
				trueLinks[t-1 + u*round] = trueLinks[t-1 + u*round] / nRun;
				falseLinks[t-1 + u*round] = falseLinks[t-1 + u*round] / nRun;
			}
			
		}
		
		// write to MATLAB
		MLDouble trueArr = new MLDouble("trueLinks", trueLinks, round);
		MLDouble falseArr = new MLDouble("falseLinks", falseLinks, round);
		
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(trueArr); 
        towrite.add(falseArr);
        
        new MatFileWriter(matlab_file + "-count.mat", towrite );
        System.out.println("Written to MATLAB file.");		
		
	}
	
	////
	public static void exportCountFileMATLAB() throws IOException{
		
		String[] dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"}; //{"pl_10000_3_01", "pl_10000_5_01", "pl_10000_10_01", "er_10000_00006", "er_10000_0001", "er_10000_0002"};
		int[] diam_list = new int[]{6,7};	//diameter 7,6,5,10,7,5
		double[] alphaArr = new double[]{0.5, 1.0}; // 0.25, 0.5, 0.75, 1
		double[] betaArr = new double[]{0.5, 1};
		double[] falsePositiveArr = new double[]{0.01, 0.001};		// 0.25, 0.1, 0.01
		
		int nRun = 10;
		int nSample = 100;

		//
		for (int i = 0; i < dataname_list.length; i++){
			String dataname = dataname_list[i];
			int round = diam_list[i];
			for (double alpha : alphaArr)
				for (double beta : betaArr){
					// Baseline
//					String name = dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
//							String.format("%.2f",beta) + "_" + nSample;
//					String count_file = "_out/" + name;
//					String matlab_file = "_stat/" + name;
//					
//					readCount(count_file, matlab_file, nRun, round);
					
					// BloomFilter
					for (double falsePositive : falsePositiveArr){
						String name = dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
								String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive);
						
						String count_file = "_out/" + name;
						String matlab_file = "_stat/" + name;
						
						readCount(count_file, matlab_file, nRun, round);
					}
					
				}
			
		}
	}
	
	////
	public static void readBaseline(String dataname, int round, int nRun, int nSample, double alpha, double beta) 
			throws IOException{
		String exchangeStr = "exchangeNoDup - DONE, elapsed";
		String countStr = "countTrueFalseDupLinks - DONE, elapsed";
		
		int[] exchangeArr = new int[nRun * round];
		int[] countArr = new int[nRun * round];
		
		// -CONSOLE.txt
		String console_file = "_console/" + dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
				String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "-CONSOLE.txt";
		System.out.println("round = " + round);
		System.out.println("console_file = " + console_file);
		
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int iE = 0;
		int iC = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > exchangeStr.length())
	        	if (str.substring(0, exchangeStr.length()).equals(exchangeStr))
	        		exchangeArr[iE++] = Integer.parseInt(str.substring(exchangeStr.length()+1));
        	
        	if (str.length() > countStr.length())
	        	if (str.substring(0, countStr.length()).equals(countStr))
	        		countArr[iC++] = Integer.parseInt(str.substring(countStr.length()+1));
		}
    	br.close();
    	
    	
    	// write to MATLAB
    	String matlab_file = "_runtime/" + dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
				String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "_runtime.mat";
		
    	MLInt32 exchangeA = new MLInt32("exchangeArr", exchangeArr, round);		// nRun columns
    	MLInt32 countA = new MLInt32("countArr", countArr, round);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(exchangeA);
        towrite.add(countA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}	
	
	////
	public static void readBloomFilter(String dataname, int round, int nRun, int nSample, double alpha, double beta, double falsePositive) 
			throws IOException{
		String exchangeStr = "exchangeNoDup - DONE, elapsed";
		String countStr = "countTrueFalseDupLinks - DONE, elapsed";
		
		int[] exchangeArr = new int[nRun * round];
		int[] countArr = new int[nRun * round];
		
		// -CONSOLE.txt
		String console_file = "_console/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
				String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive) + "_" + nRun + "_" + nSample + "-CONSOLE.txt";
		System.out.println("round = " + round);
		System.out.println("console_file = " + console_file);
		
		BufferedReader br = new BufferedReader(new FileReader(console_file));
		int iE = 0;
		int iC = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	if (str.length() > exchangeStr.length())
	        	if (str.substring(0, exchangeStr.length()).equals(exchangeStr))
	        		exchangeArr[iE++] = Integer.parseInt(str.substring(exchangeStr.length()+1));
        	
        	if (str.length() > countStr.length())
	        	if (str.substring(0, countStr.length()).equals(countStr))
	        		countArr[iC++] = Integer.parseInt(str.substring(countStr.length()+1));
		}
    	br.close();
    	
    	
    	// write to MATLAB
    	String matlab_file = "_runtime/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
				String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive) + "_" + nRun + "_" + nSample + "_runtime.mat";
		
    	MLInt32 exchangeA = new MLInt32("exchangeArr", exchangeArr, round);		// nRun columns
    	MLInt32 countA = new MLInt32("countArr", countArr, round);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(exchangeA);
        towrite.add(countA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{

		//
		exportCountFileMATLAB();
		
		//
//		String[] dataname_list = new String[]{"pl_10000_3_01", "pl_10000_5_01", "pl_10000_10_01", "er_10000_00006", "er_10000_0001", "er_10000_0002"};
//		int[] diam_list = new int[]{7,6,5,10,7,5};	//diameter
//		double[] alphaArr = new double[]{0.25, 0.5, 0.75, 1};
//		double[] betaArr = new double[]{0.5, 1};
//		double[] falsePositiveArr = new double[]{0.1};		// 0.25, 0.1, 0.01
//		
//		int nRun = 10;
//		int nSample = 100;
//		
//		// readBaseline()
//		for (int i = 0; i < 6; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			for (double alpha : alphaArr)
//				for (double beta : betaArr){
//					readBaseline(dataname, round, nRun, nSample, alpha, beta);
//					readBloomFilter(dataname, round, nRun, nSample, alpha, beta, 0.1);
//				}
//					
//		}
//		System.out.println("DONE.");
		
		
		
		
	}

}
