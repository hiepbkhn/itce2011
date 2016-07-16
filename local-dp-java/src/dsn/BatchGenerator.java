package dsn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BatchGenerator {

	//// Ground Truth
	public static void generateGroundTruth(String batch_file, String prefix, String[] dataname_list, int[] diam_list,  int nRun, int nSample) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		double alpha = 1.0;
		double beta = 0.0;
		for (int i = 0; i < dataname_list.length; i++){
			String dataname = dataname_list[i];
			int round = diam_list[i];
			
			String cmd = "java dsn.LinkExchange " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
						String.format("%.2f",beta) + " " + nRun + " " + nSample + 
					" > ../_console/" + dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
						String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "-CONSOLE.txt";
			bw.write(cmd + "\n");
			bw.write("\n");
		}

		bw.close();
		
	}
	
	//// LinkExchange
	public static void generateBaseline(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr){
				String cmd = "java dsn.LinkExchange " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
							String.format("%.2f",beta) + " " + nRun + " " + nSample + 
						" > ../_console/" + dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	//// LinkExchange (D2)
	public static void generateBaselineD2(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr, double[] gammaArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr)
				for (double gamma : gammaArr){
					String cmd = "java dsn.LinkExchange " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
								String.format("%.2f",beta) + " " + nRun + " " + nSample + " " + String.format("%.2f",gamma) + 
							" > ../_console/" + dataname + "-nodup-d2-" + round + "_" + String.format("%.2f",alpha) + "_" + 
								String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "_" + String.format("%.2f",gamma) + "-CONSOLE.txt";
					bw.write(cmd + "\n");
				}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	//// LinkExchangeBloomFilter
	public static void generateBloomFilter(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr, double[] falsePositiveArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr)
				for (double falsePositive : falsePositiveArr){
				String cmd = "java dsn.LinkExchangeBloomFilter " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
							String.format("%.2f",beta) + " " + falsePositive + " " + nRun + " " + nSample + 
						" > ../_console/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + falsePositive + "_" + nRun + "_" + nSample + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	///////////////////// UTILITY
	////Ground Truth
	public static void generateUtilGroundTruth(String batch_file, String prefix, String[] dataname_list, int[] diam_list,  int nRun, int nSample) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		double alpha = 1.0;
		double beta = 0.0;
		for (int i = 0; i < dataname_list.length; i++){
			String dataname = dataname_list[i];
			int round = diam_list[i];
			
			String sample_name = dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
					String.format("%.2f",beta);
			
			String cmd = "java dsn.UtilityMeasure " + prefix + " " + sample_name + " " + round + " " + nRun + 
					" > ../_console/" + dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
					String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "-UTIL.txt";
			bw.write(cmd + "\n");
			bw.write("\n");
		}

		bw.close();
		
	}
	
	////
	public static void generateUtilBaseline(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr){
				String sample_name = dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
						String.format("%.2f",beta);
				
				String cmd = "java dsn.UtilityMeasure " + prefix + " " + sample_name + " " + round + " " + nRun + 
						" > ../_console/" + dataname + "-nodup-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "-UTIL.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	//// D2
	public static void generateUtilBaselineD2(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr, double[] gammaArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr)
				for (double gamma : gammaArr){
					String sample_name = dataname + "-nodup-d2-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + String.format("%.2f",gamma);
					
					String cmd = "java dsn.UtilityMeasure " + prefix + " " + sample_name + " " + round + " " + nRun + 
							" > ../_console/" + dataname + "-nodup-d2-" + round + "_" + String.format("%.2f",alpha) + "_" + 
								String.format("%.2f",beta) + "_" + String.format("%.2f",gamma) + "_" + nRun + "_" + nSample + "-UTIL.txt";
					bw.write(cmd + "\n");
				}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	//// LinkExchangeBloomFilter
	public static void generateUtilBloomFilter(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr, double[] falsePositiveArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr)
				for (double falsePositive : falsePositiveArr){
				String sample_name = dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
						String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive);	
					
				String cmd = "java dsn.UtilityMeasure2 " + prefix + " " + sample_name + " " + round + " " + nRun + 
						" > ../_console/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive) + "_" + nRun + "_" + nSample + "-UTIL.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	////
	public static void generateCompression(String batch_file, String prefix, String dataname, int round, int nRun,
			double[] alphaArr, double[] betaArr, double[] falsePositiveArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr)
				for (double falsePositive : falsePositiveArr){
					
				String cmd = "java dsn.LinkExchangeBloomFilter " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
						String.format("%.2f",beta) + " " + falsePositive + " " + nRun +
						" > ../_console/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive) + "_" + nRun + "-COMPRESS.txt";
				bw.write(cmd + "\n");
			}
			bw.write("\n");
		}

		bw.close();
		
	}
	
	////
	public static void generateAttack(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr)
			for (double beta : betaArr){
					
				String cmd = "java -Xmx9000m dsn.LinkExchangeAttack " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
						String.format("%.2f",beta) + " " + nRun + " " + nSample + 
						" > ../_console/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + nRun + "_" + nSample + "-ATTACK.txt";
				bw.write(cmd + "\n");
			bw.write("\n");
		}

		bw.close();
		
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "../";		// run in D:/git/itce2011/local-dp-java/_cmd
		
		String[] dataname_list = new String[]{"pl_10000_3_01", "pl_10000_5_01", "pl_10000_10_01", "er_10000_00006", "er_10000_0001", "er_10000_0002"};
		int[] diam_list = new int[]{7,6,5,10,7,5};	//diameter
		double[] alphaArr = new double[]{0.25, 0.5, 0.75, 1};
		double[] betaArr = new double[]{0.5, 1};
		double[] falsePositiveArr = new double[]{0.1};		// 0.25, 0.1, 0.01
		
		int nRun = 10;
		int nSample = 100;
		
		////////////////
		// LinkExchange - GROUND TRUTH
//		String batch_file = "_cmd/GroundTruth.cmd";
//		
//		generateGroundTruth(batch_file, prefix, dataname_list, diam_list, nRun, nSample);
//		System.out.println("DONE.");
		
		// LinkExchange
//		for (int i = 0; i < 6; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/LinkExchange_" + dataname + ".cmd";
//			
//			generateBaseline(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr);
//		}
//		System.out.println("DONE.");
		
		
		// LinkExchange (D2)
//		dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
//		diam_list = new int[]{6,7};	//diameter
//		alphaArr = new double[]{0.5, 1};
//		betaArr = new double[]{0.5};
//		double[] gammaArr = new double[]{0.0, 0.5};
//		for (int i = 0; i < dataname_list.length; i++){
//		String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/LinkExchangeD2_" + dataname + ".cmd";
//			
//			generateBaselineD2(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, gammaArr);
//		}
//		System.out.println("DONE.");
		
		
//		// LinkExchangeBloomFilter
//		for (int i = 0; i < 6; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/LinkExchangeBloomFilter_" + dataname + ".cmd";
//			
//			generateBloomFilter(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, falsePositiveArr);
//		}
//		System.out.println("DONE.");
		
		
		
		// VARY falsePositive
//		dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
//		diam_list = new int[]{6,7};	//diameter
//		alphaArr = new double[]{0.5, 1};
//		betaArr = new double[]{0.5, 1};
//		falsePositiveArr = new double[]{0.01, 0.001};		// 0.25, 0.1, 0.01
//		// LinkExchangeBloomFilter
//		for (int i = 0; i < dataname_list.length; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/FalsePositive_" + dataname + ".cmd";
//			
//			generateBloomFilter(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, falsePositiveArr);
//		}
//		System.out.println("DONE.");
		
		
		
		//////////////// UTILITY
//		String batch_file = "_cmd/UtilGroundTruth.cmd";
//		
//		generateUtilGroundTruth(batch_file, prefix, dataname_list, diam_list, nRun, nSample);
//		System.out.println("DONE.");
		
		// LinkExchange
//		for (int i = 0; i < 6; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/UtilBaseline_" + dataname + ".cmd";
//			
//			generateUtilBaseline(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr);
//		}
//		System.out.println("DONE.");
		
		
		// LinkExchange (D2)
//		dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
//		diam_list = new int[]{6,7};	//diameter
//		alphaArr = new double[]{0.5, 1};
//		betaArr = new double[]{0.5};
//		double[] gammaArr = new double[]{0.0, 0.5};
//		for (int i = 0; i < dataname_list.length; i++){
//		String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/UtilBaselineD2_" + dataname + ".cmd";
//			
//			generateUtilBaselineD2(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, gammaArr);
//		}
//		System.out.println("DONE.");
		
		
//		// LinkExchangeBloomFilter
//		for (int i = 0; i < 6; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/UtilBloomFilter_" + dataname + ".cmd";
//			
//			generateUtilBloomFilter(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, falsePositiveArr);
//		}
//		System.out.println("DONE.");
		
		
		// VARY falsePositive
//		dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
//		diam_list = new int[]{6,7};	//diameter
//		alphaArr = new double[]{0.5, 1};
//		betaArr = new double[]{0.5, 1};
//		falsePositiveArr = new double[]{0.01, 0.001};		// 0.25, 0.1, 0.01
//		// LinkExchangeBloomFilter
//		for (int i = 0; i < dataname_list.length; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/UtilFalsePositive_" + dataname + ".cmd";
//			
//			generateUtilBloomFilter(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, falsePositiveArr);
//		}
//		System.out.println("DONE.");
		
		
		
		//////////////// COMPRESSION
//		dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
//		diam_list = new int[]{6,7};	//diameter
//		alphaArr = new double[]{0.25, 0.5, 0.75, 1};
//		betaArr = new double[]{0.5, 1};
//		falsePositiveArr = new double[]{0.01, 0.001}; //, 0.01, 0.001};		// 0.25, 0.1, 0.01
//
//		for (int i = 0; i < dataname_list.length; i++){
//			String dataname = dataname_list[i];
//			int round = diam_list[i];
//			
//			String batch_file = "_cmd/CompressionBloomFilter_" + dataname + ".cmd";
//			
//			generateCompression(batch_file, prefix, dataname, round, nRun, alphaArr, betaArr, falsePositiveArr);
//		}
//		System.out.println("DONE.");
		
		
		//////////////// ATTACK
		dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
		diam_list = new int[]{6,7};	//diameter
		alphaArr = new double[]{0.5, 1};
		betaArr = new double[]{0.5, 1};
		nRun = 2;
		
		for (int i = 0; i < dataname_list.length; i++){
			String dataname = dataname_list[i];
			int round = diam_list[i];
			
			String batch_file = "_cmd/Attack_" + dataname + ".cmd";
			
			generateAttack(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr);
		}
		System.out.println("DONE.");
		
	}

}
