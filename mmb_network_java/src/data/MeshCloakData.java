/*
 * Apr 18, 2017
 * 	- generate MeshCloak data from \Thomas Brinkhoff\RunTime21\query\
 */

package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MeshCloakData {

	////
	public static void convertBrinkhoff(String inputFile, String outputFile, int max_k_anom) throws IOException{
		BufferedReader fIn = new BufferedReader(new FileReader(inputFile));
		BufferedWriter fOut = new BufferedWriter(new FileWriter(outputFile));
		
		Random random = new Random();
		while (true){
        	String str = fIn.readLine();
        	if (str == null)
        		break;
        	//
        	String[] items = str.split("\t");
        	
            String point_type = items[0];
            int obj_id = Integer.parseInt(items[1]);
            int obj_class = Integer.parseInt(items[3]);
            int event_ts = Integer.parseInt(items[2]);
            
            double x = Double.parseDouble(items[5]);
            double y = Double.parseDouble(items[6]);
            int timestamp = Integer.parseInt(items[4]);
            double speed = Double.parseDouble(items[7]);
            int next_node_x = Integer.parseInt(items[8]);
            int next_node_y = Integer.parseInt(items[9]);
            int k_anom = 2 + random.nextInt(max_k_anom - 1);	// nextInt: upper bound is exclusive
            
            //
//            fOut.write(point_type.charAt(0) + "\t" + obj_id + "\t" + event_ts + "\t" + obj_class + "\t" + timestamp + "\t" + 
//            		String.format("%.2f", x) + "\t" + String.format("%.2f", y) + "\t" + String.format("%.2f", speed) + "\t" + 
//            		next_node_x + "\t" + next_node_y + "\t" + k_anom + '\n');
            // do not format (x, y, speed)
            fOut.write(point_type.charAt(0) + "\t" + obj_id + "\t" + event_ts + "\t" + obj_class + "\t" + timestamp + "\t" + 
            		x + "\t" + y + "\t" + speed + "\t" + 
            		next_node_x + "\t" + next_node_y + "\t" + k_anom + '\n');
            
        }
        fIn.close();
		fOut.close();
	}
	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
//		String inName = "SanJoaquin_100000_0_0_0_20_20_1_1000_50";
		String inName = "SanJoaquin_100000_0_0_0_20_20_1_1000_250";
		int k_anom = 5;
		String outName = inName + "_2_" + k_anom;
		String inputFile = "F:\\Tailieu\\Paper-code\\Location Privacy\\Thomas Brinkhoff\\RunTime21\\query\\" + inName + ".txt"; 
		String outputFile = "D:\\github\\itce2011\\mmb_network_cpp\\query\\" + outName + ".txt";
		convertBrinkhoff(inputFile, outputFile, k_anom);
		System.out.println(inName + " - DONE.");
		
//		String[] mapList = new String[]{"oldenburgGen", "cal"};
//		int[] nUserList = new int[]{5000, 10000, 20000, 50000, 100000};
//		int[] speedList = new int[]{10, 50, 250};
//		int[] k_anom_list = new int[]{5, 10, 15};
//		
//		for (String map : mapList)
//			for (int nUser : nUserList)
//				for (int speed : speedList)
//					for (int k_anom : k_anom_list){
//						String inName = map + "_" + nUser + "_0_0_0_50_20_1_1000_" + speed;
//						
//						String outName = inName + "_2_" + k_anom;
//						String inputFile = "F:\\Tailieu\\Paper-code\\Location Privacy\\Thomas Brinkhoff\\RunTime21\\query\\" + inName + ".txt"; 
//						String outputFile = "D:\\github\\itce2011\\mmb_network_cpp\\query\\" + outName + ".txt";
//						
//						convertBrinkhoff(inputFile, outputFile, k_anom);
//						System.out.println(inName + "DONE.");
//				}
		
	}

}
