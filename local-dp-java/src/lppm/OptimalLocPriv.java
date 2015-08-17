/**
 * papers:
 *  - Protecting Location Privacy: Optimal Strategy against Localization Attacks (CCS'12)
 * Jul 1
 * 	- create file
 * Aug 16
 * 	- userProgram, attackerProgram: up to N=300
 * 	-   
 */

package lppm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class OptimalLocPriv {

	static int W = 2;	// width	20	
	static int H = 2;	// height	15
	static int N = 300;	// N = W * H
	
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
	
	
	//// generate .MPS file (MAX)
	public static void userProgram(double maxQloss, double[] phi, double[][] dP, double[][] dQ, String mpsFilename) throws IOException{
		
		File file = new File(mpsFilename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("NAME          TESTPROB\n");
		bw.write("ROWS\n");
		bw.write(" N  COST\n");
		
		// C1: constraints (r^,r') == (r3,r2)
		for (int r3 = 0; r3 < N; r3++)
			for (int r2 = 0; r2 < N; r2++){
				bw.write(" L  CXF" + (r3*N+r2) + "\n");
			}
		// C2: constraint with Q_loss^max
		bw.write(" L  QLOSS\n");
		
		// C3: constraints f(r',r)
		for (int r1 = 0; r1 < N; r1++)
			bw.write(" E  CF" + r1 + "\n");
		
		// COLUMNS
		bw.write("COLUMNS\n");
		
		// variables x_r'
		for (int r2 = 0; r2 < N; r2++){
			bw.write("    " + String.format("%-10s", "X"+r2) + String.format("%-10s", "COST") + String.format("%12s",1) + "\n");
			
			for (int r3 = 0; r3 < N; r3++)
				bw.write("    " + String.format("%-10s", "X"+r2) + String.format("%-10s", "CXF"+(r3*N+r2)) + String.format("%12s",1) + "\n");
		}
		
		// variables f_r'_r (each variable MUST BE CONSECUTIVE in .mps)
		for (int r2 = 0; r2 < N; r2++){
			for (int r1 = 0; r1 < N; r1++){
				// for C1
				for (int r3 = 0; r3 < N; r3++)		
					bw.write("    " + String.format("%-10s", "F"+(r2*N+r1)) + String.format("%-10s", "CXF"+(r3*N+r2)) + 
						String.format("%12s", String.format("%.2f", -phi[r1] * dP[r3][r1])) + "\n");
				
				// for C2
				bw.write("    " + String.format("%-10s", "F"+(r2*N+r1)) + String.format("%-10s", "QLOSS") + 
						String.format("%12s", String.format("%.2f", phi[r1] * dQ[r2][r1])) + "\n");
				
				// for C3
				bw.write("    " + String.format("%-10s", "F"+(r2*N+r1)) + String.format("%-10s", "CF"+r1) + 
						String.format("%12s", "1") + "\n");
			}
		}
		
		
		// RHS
		bw.write("RHS\n");
		for (int r3 = 0; r3 < N; r3++)		// for C1
			for (int r2 = 0; r2 < N; r2++){
				bw.write("    " + String.format("%-10s", "RHS1") + String.format("%-10s", "CXF"+(r3*N+r2)) + String.format("%12s", "0") + "\n");
			}
		
											// for C2
		bw.write("    " + String.format("%-10s", "RHS1") + String.format("%-10s", "QLOSS") + 
				String.format("%12s", String.format("%.2f", maxQloss)) + "\n");
		
		for (int r1 = 0; r1 < N; r1++)		// for C3
			bw.write("    " + String.format("%-10s", "RHS1") + String.format("%-10s", "CF"+r1) + String.format("%12s", "1") + "\n");
		
		
		// BOUNDS
		bw.write("BOUNDS\n");
		for (int r1 = 0; r1 < N; r1++)		// for f_r'_r
			for (int r2 = 0; r2 < N; r2++)
				bw.write(" LO " + String.format("%-10s", "BND1") + String.format("%-10s", "F"+(r2*N+r1)) + String.format("%12s", "0") + "\n");
		
		bw.write("ENDATA\n");
		
		bw.close();
	}
	
	//// generate .MPS file (MIN)
	public static void attackerProgram(double maxQloss, double[] phi, double[][] dP, double[][] dQ, String mpsFilename) throws IOException{
		
		File file = new File(mpsFilename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("NAME          TESTPROB\n");
		bw.write("ROWS\n");
		bw.write(" N  COST\n");
		
		// C1: constraints (r,r') == (r1,r2)
		for (int r1 = 0; r1 < N; r1++)
			for (int r2 = 0; r2 < N; r2++){
				bw.write(" L  CYH" + (r1*N+r2) + "\n");
			}

		// C2: constraints h(r^,r')
		for (int r2 = 0; r2 < N; r2++)
			bw.write(" E  CH" + r2 + "\n");
		
		// COLUMNS
		bw.write("COLUMNS\n");
		
		// variables y_r
		for (int r1 = 0; r1 < N; r1++){
			bw.write("    " + String.format("%-10s", "Y"+r1) + String.format("%-10s", "COST") + String.format("%12s", phi[r1]) + "\n");
			
			for (int r2 = 0; r2 < N; r2++)
				bw.write("    " + String.format("%-10s", "Y"+r1) + String.format("%-10s", "CYH"+(r1*N+r2)) + String.format("%12s", -1) + "\n");
		}
		
		// variables h_r^_r' (each variable MUST BE CONSECUTIVE in .mps)
		for (int r3 = 0; r3 < N; r3++){
			for (int r2 = 0; r2 < N; r2++){
				// for C1
				for (int r1 = 0; r1 < N; r1++)		
					bw.write("    " + String.format("%-10s", "H"+(r3*N+r2)) + String.format("%-10s", "CYH"+(r1*N+r2)) + 
						String.format("%12s", String.format("%.2f", dP[r3][r1])) + "\n");
				
				// for C2
				bw.write("    " + String.format("%-10s", "H"+(r3*N+r2)) + String.format("%-10s", "CH"+r2) + String.format("%12s", "1") + "\n");
			}
		}
		
		// variable z
		bw.write("    " + String.format("%-10s", "Z") + String.format("%-10s", "COST") + 
				String.format("%12s", String.format("%.2f", maxQloss)) + "\n");
		
		for (int r1 = 0; r1 < N; r1++)
			for (int r2 = 0; r2 < N; r2++)
				bw.write("    " + String.format("%-10s", "Z") + String.format("%-10s", "CYH"+(r1*N+r2)) + String.format("%12s", dQ[r2][r1]) + "\n");
		
		// RHS
		bw.write("RHS\n");
		for (int r1 = 0; r1 < N; r1++)		// for C1
			for (int r2 = 0; r2 < N; r2++){
				bw.write("    " + String.format("%-10s", "RHS1") + String.format("%-10s", "CYH"+(r1*N+r2)) + String.format("%12s", "0") + "\n");
			}
		
		for (int r2 = 0; r2 < N; r2++)		// for C2
			bw.write("    " + String.format("%-10s", "RHS1") + String.format("%-10s", "CH"+r2) + String.format("%12s", "1") + "\n");
		
		
		// BOUNDS
		bw.write("BOUNDS\n");
		for (int r3 = 0; r3 < N; r3++)		// for h_r^_r'
			for (int r2 = 0; r2 < N; r2++)
				bw.write(" LO " + String.format("%-10s", "BND1") + String.format("%-10s", "H"+(r3*N+r2)) + String.format("%12s", "0") + "\n");
		
		bw.write(" LO " + String.format("%-10s", "BND1") + String.format("%-10s", "Z") + String.format("%12s", "0") + "\n");
		
		bw.write("ENDATA\n");
		
		bw.close();
		
		
	}
	
	
	////
	public static void randomConstants(double[] phi, double[][] dP, double[][] dQ){
		int nonZeros = (int)Math.round(0.2*N);
		
		Random random = new Random();
		
		// phi
		for (int i = 0; i < N; i++)
			phi[i] = 0.0;
		
		double s = 0.0;
		for (int i = 0; i < nonZeros; i++){
			phi[i] = random.nextDouble();
			s += phi[i];
		}
		for (int i = 0; i < nonZeros; i++)
			phi[i] = phi[i]/s;
		
		//
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++){
				dP[i][j] = random.nextDouble();
				dQ[i][j] = random.nextDouble();
			}
		
		// maxQloss
//		maxQloss[0] = 1.0;
//		return 1.0;
		
	}
	
	////
	public static void main(String[] args) throws Exception{
		
		
//		generateProfile(20);

		// test 
//		double maxQloss = 80; // 0.8 --> infeasible;
//		double[] phi = {0.2, 0.2, 0.3, 0.3};
//		double[][] dP = {{1,2,3,4}, {4,1,2,3}, {4,3,1,2}, {2,3,4,1}};
//		double[][] dQ = {{1,2,3,4}, {4,1,2,3}, {4,3,1,2}, {2,3,4,1}};
		
		// random constants (maxQloss, phi, dP, dQ)
		double maxQloss = 1.0;
		double[] phi = new double[N];
		double[][] dP = new double[N][N];;
		double[][] dQ = new double[N][N];
		randomConstants(phi, dP, dQ);
		
		
		System.out.println("N = " + N);
		System.out.println("maxQloss = " + maxQloss);
//		System.out.println("phi = ");
//		for (int i = 0; i < N; i++)
//			System.out.print(phi[i] + " ");
		System.out.println();
		
		long start = System.currentTimeMillis();
		userProgram(maxQloss, phi, dP, dQ, "sample-user.mps");
		System.out.println("userProgram - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		attackerProgram(maxQloss, phi, dP, dQ, "sample-attacker.mps");
		System.out.println("attackerProgram - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		
//		System.out.println(String.format("%10s%-30s%10s", "x153", "xy", "-4"));
//		System.out.println(String.format("%10s%-30s%10s", "x15", "x", "-4"));
	}

}
