/*
 * "Graphical Models for Game Theory" (UAI'01)
 * Sep 06, 2016
 * 	- createFullPayOff, readFullPayOff
 */

package gt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GraphicalGame {

	//// k: degree bound, r: grid size, eps: equilibrium tolerance
	public static void treeNash(int k, double r, double eps){
		
		
		
	}
	
	////
	public static GNode[] initTree(){
		GNode[] ret = new GNode[8];
		
		ret[0] = new GNode(1, 0);
		ret[1] = new GNode(0, 0);
		ret[2] = new GNode(1, 0);
		ret[3] = new GNode(0, 0);
		ret[4] = new GNode(1, 2);
		ret[5] = new GNode(0, 2);
		ret[6] = new GNode(1, 2);
		ret[7] = new GNode(1, 1);
		
		//
		ret[4].parents = new GNode[]{ret[0], ret[1]};
		ret[4].child = ret[6];
		
		ret[5].parents = new GNode[]{ret[2], ret[3]};
		ret[5].child = ret[6];
		
		ret[6].parents = new GNode[]{ret[4], ret[5]};
		ret[6].child = ret[7];
		
		ret[7].parents = new GNode[]{ret[6]};
		ret[7].child = null;
		//
		return ret;
	}
	
	
	//// two-action games
	public static void createFullPayOff(int n, String file_name) throws IOException {
		
		int nP = 1 << n; // number of profiles (2^n)
		
		System.out.println("nP = " + nP);
		
		//
		BufferedWriter bw = new BufferedWriter(new FileWriter(file_name));
		Random random = new Random();
		
		for (int i = 0; i < nP; i++){
			String profile = "";
			for (int k = 0; k < n; k++){	// check bits
				if (((i >> k) & 1) == 1)
					profile = "1" + profile;
				else
					profile = "0" + profile;
			}
//			System.out.println(profile);
			
			// payoff vector
			String payoff = "";
			for (int k = 0; k < n; k++)
				payoff += random.nextInt(2) + ",";
			
			//
			bw.write(profile + ":" + payoff + "\n");
			
		}
		bw.close();
		
	}
	
	////
	public static int[][] readFullPayOff(int n, String file_name) throws IOException {
		int nP = 1 << n; 
		int[][] ret = new int[n][nP];
		
		BufferedReader br = new BufferedReader(new FileReader(file_name));
		
		int profileId = 0;
		while (true){
			String str = br.readLine();
			if (str == null)
				break;
			
			str = str.substring(str.indexOf(":") + 1, str.length()-1);
			String[] values = str.split(",");
			
			for(int k = 0; k < n; k++)
				ret[k][profileId] = Integer.parseInt(values[k]);
			
			profileId += 1;
		}
		
		br.close();
		
		//
		return ret;
	}
	
	////
	public static void findEquilibria(int[][] M){
		int n = M.length;
		int nP = 1 << n;
		for (int i = 0; i < nP; i++){
			// check best response
			boolean found = true;
			for (int k = 0; k < n; k++){	// check bits
				int kPay = M[k][i];			// payoff of player k
				
				int iFlip = i ^ (1 << k);	// the index profile, flipped at k
				int oPay = M[k][iFlip];		// payoff of player k
				
				if (oPay > kPay){			// if i is not the best response of k
					found = false;
					break;
				}
			}
			
			if (found){
				System.out.println("equilibrium at profile " + i);
			}
			
		}
		
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		
		int  n = 8;
//		createFullPayOff(n, "_game/graph-game.po");

		//
		int[][] M = readFullPayOff(n, "_game/graph-game.po");
		
		findEquilibria(M);
		
		//
//		System.out.println(5 ^ (1 << 2));
	}

}
