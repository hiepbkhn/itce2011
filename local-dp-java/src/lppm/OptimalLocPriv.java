/**
 * Jul 1
 * papers:
 * - Protecting Location Privacy: Optimal Strategy against Localization Attacks (CCS'12)
 * 
 */

package lppm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class OptimalLocPriv {

	static int W = 20;	// width
	static int H = 15;	// height
	
	////
	public static void generateTraces(int width, int height, int numTraces){
		
		
	}
	
	//// 
	public static void computeProfile(){
		
	}
	
	////
	public static void generateProfile(int numNonZeros){
		int [][] nonZeros = new int[numNonZeros][2];
		double[] freq = new double[numNonZeros];
		double sumFreq = 0.0;
		
		boolean [][] marked = new boolean[W][H];
		
//		for (int i = 0; i < W; i++){
//			for (int j = 0; j < H; j++)
//				System.out.print(marked[i][j] + " ");
//			System.out.println();
//		}
		
		
		int x = -1;
		int y = -1;
		Random random = new Random();
		for (int i = 0; i < numNonZeros; i++){
			while (true){
				x = random.nextInt(W);
				y = random.nextInt(H);
				if (!marked[x][y]){
					nonZeros[i][0] = x;
					nonZeros[i][1] = y;
					marked[x][y] = true;
					
					freq[i] = random.nextDouble();
					sumFreq += freq[i];
					break;
				}
			}
		}
		
		//
		for (int i = 0; i < numNonZeros; i++)
			freq[i] = freq[i]/sumFreq;
		
		//
		for (int i = 0; i < numNonZeros; i++)
			System.out.println(nonZeros[i][0] + " " + nonZeros[i][1] + ": " + freq[i]);
		
	}
	
	//// generate .MPS file
	public static void userProgram(double maxQLoss, String mpsFilename) throws IOException{
		
		File file = new File(mpsFilename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("NAME          TESTPROB\n");
		bw.write("ROWS\n");
		bw.write(" N  COST\n");
		
		// variables x_r'
		
		
		
		// variables f_r'r
		
		
		
		
		bw.close();
	}
	
	////
	public static void attackerProgram(){
		
		
	}
	
	
	
	////
	public static void main(String[] args) throws Exception{
		
		
		generateProfile(20);

		userProgram(0.8, "sample.mps");
	}

}
