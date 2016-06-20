package dsn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BatchGenerator {

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
	
	//// LinkExchangeBloomFilter
	public static void generateBloomFilter(String batch_file, String prefix, String dataname, int round, int nRun, int nSample, 
			double[] alphaArr, double[] betaArr, double[] falsePositiveArr) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(batch_file));
		for (double alpha : alphaArr){
			for (double beta : betaArr)
				for (double falsePositive : falsePositiveArr){
				String cmd = "java dsn.LinkExchangeBloomFilter " + prefix + " " + dataname + " " + round + " " + String.format("%.2f",alpha) + " " + 
							String.format("%.2f",beta) + " " + String.format("%.2f",falsePositive) + " " + nRun + " " + nSample + 
						" > ../_console/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
							String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive) + "_" + nRun + "_" + nSample + "-CONSOLE.txt";
				bw.write(cmd + "\n");
			}
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
		
		// LinkExchange
		for (int i = 0; i < 6; i++){
			String dataname = dataname_list[i];
			int round = diam_list[i];
			
			String batch_file = "_cmd/LinkExchange_" + dataname + ".cmd";
			
			generateBaseline(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr);
		}
		System.out.println("DONE.");
		
		// LinkExchangeBloomFilter
		for (int i = 0; i < 6; i++){
			String dataname = dataname_list[i];
			int round = diam_list[i];
			
			String batch_file = "_cmd/LinkExchangeBloomFilter_" + dataname + ".cmd";
			
			generateBloomFilter(batch_file, prefix, dataname, round, nRun, nSample, alphaArr, betaArr, falsePositiveArr);
		}
		System.out.println("DONE.");
		
	}

}
