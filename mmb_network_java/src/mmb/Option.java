package mmb;

import java.util.HashMap;
import java.util.Map;

public class Option {

	public static String QUERY_PATH = "query/";
	
//	public static String QUERY_FILE = "cal_mod_10k_0.5_2_5_0005_0002_2_10_20_80_10_20_events.txt";      	//k-anom: 2-5, slow
	
	public static String QUERY_FILE = "oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt";    		//k-anom: 2-5, slow	
//	public static String QUERY_FILE = "oldenburgGen_mod_10k_0.5_2_5_0005_0020_2_10_20_80_10_20_events.txt";
	
	//
	public static String RESULT_PATH = "out/";
	
	public static String MAP_PATH = "data/";
	
	//public static String MAP_NAME = "synthetic_24_30_30_40";
	//public static String MAP_NAME = "synthetic_12_15_20_20";
	//public static String MAP_NAME = "delaunay_12_15_396_1150";
	public static String MAP_NAME = "oldenburgGen";
//	public static String MAP_NAME = "oldenburgGen_mod";
	//public static String MAP_NAME = "cal";     
	//public static String MAP_NAME = "cal_mod";
			
			
	//
	public static double MAX_SPEED = 600;
	public static double INIT_DISTANCE = 1000;
	public static double MAX_USER = 10000;
	
	public static double DISTANCE_CONSTRAINT = 500; // oldenburgGen
//	public static double DISTANCE_CONSTRAINT = 15000; // cal
	
	
	public static double MAP_RATIO = 0.2;

	public static int K_ANONYMITY = 2;
	public static int DELAY_MAX = 3;   //report prob. = 0.2 --> 5 timestamps

	public static int K_GLOBAL = 2;
	public static int S_GLOBAL = 2;

	public static double INIT_COVER_KEEP_RATIO = 1.0;     //0.8, 0.85, 0.9, 0.95, 1.0 (for K_GLOBAL = 2)
	public static double NEXT_COVER_KEEP_RATIO = 1.0;

	public static double MAX_MESH_LENGTH = 5*INIT_DISTANCE;
	public static double MIN_MESH_LENGTH = 1.5*INIT_DISTANCE;
	
	public static String MACE_EXECUTABLE = "mace_go.exe";
	
	public static String MAXIMAL_CLIQUE_FILE_IN = "mesh.grh";
	public static String MAXIMAL_CLIQUE_FILE_OUT = "mesh.out";
	
	
	/////////////////////// from trace_generator/lbs_option.py
	//MAP_WIDTH = 24000.0
	//MAP_HEIGHT = 30000.0
	//NUM_NODE_WIDTH = 30
	//NUM_NODE_HEIGHT = 40
	//
	//MAP_NAME = "synthetic_24_30_30_40"        //k-anom: 2-10

	// smaller map
	public static double MAP_WIDTH = 12000.0;
	public static double MAP_HEIGHT = 15000.0;
	public static int NUM_NODE_WIDTH = 20;
	public static int NUM_NODE_HEIGHT = 20;

	public static String PROFILE_PATH = "../out/deviation/";

	//PROFILE_NAME = "synthetic_12_15_20_20_5k_2_10"     //k-anom: 2-10
	//PROFILE_NAME = "synthetic_12_15_20_20_10k_2_5"     //k-anom: 2-5
	//PROFILE_NAME = "delaunay_12_15_396_1150_10k_2_5"     //k-anom: 2-5

	//PROFILE_NAME = "oldenburgGen_2k_1_2_5_0005_002_3_7_20_80_10_20"     //k-anom: 2-5, speed_profile: 1.0,
	//PROFILE_NAME = "oldenburgGen_1k_1_2_5_0005_002_5_15_20_80_10_20"     //k-anom: 2-5, speed_profile: 1.0,
	//PROFILE_NAME = "oldenburgGen_5k_1_2_5_0005_002_5_15_20_80_10_20"     //k-anom: 2-5, speed_profile: 1.0,
	//PROFILE_NAME = "oldenburgGen_mod_1k_1_2_5_0005_002_5_15_20_80_10_20"     //k-anom: 2-5, speed_profile: 1.0,

	//PROFILE_NAME = "cal_2k_1_2_5_0005_002_3_7_20_80_10_20"     //k-anom: 2-5, speed_profile: 1.0,
	//PROFILE_NAME = "cal_mod_1k_1_2_5_0005_002_3_7_20_80_10_20"     //k-anom: 2-5, speed_profile: 1.0, 

	// for lbs_attack.py
	//CLOAKING_PATH = "../../mmb_network/out/deviation/"       
	public static String CLOAKING_PATH = "../../mmb_network/out/";              // for ICliqueCloak

	//CLOAKING_FILE_NAME = "delaunay_12_15_396_1150_10k_2_5_events-400-300_edge_cloaking_"
	//CLOAKING_FILE_NAME = "oldenburgGen_2k_1_2_5_0005_002_3_7_20_80_10_20_events-400-300_edge_cloaking_"
	//CLOAKING_FILE_NAME = "oldenburgGen_mod_5k_0.5_2_5_0005_002_2_10_20_80_10_20_events-500-300_edge_cloaking_"
	//CLOAKING_FILE_NAME = "cal_2k_1_2_5_0005_002_3_7_20_80_10_20_events-20000-6000_edge_cloaking_"
	//CLOAKING_FILE_NAME = "cal_2k_1_2_5_0005_002_3_7_20_80_10_20_events-20000-6000_edge_cloaking_"
	//CLOAKING_FILE_NAME = "cal_mod_10k_0.5_2_5_0005_002_2_10_20_80_10_20_events-15000-300_edge_cloaking_"
	public static String CLOAKING_FILE_NAME = "i7_iclique_out_cal_mod_10k_0.5_2_5_0005_002_2_10_90s_10_1.0_edge_cloaking_";  // converted output of ICliqueCloak

	public static double[] EDGE_CLASSES = new double[]{0.5, 0.7, 0.8, 0.9, 1.0};    // CDF

	//SPEED_CLASSES = [1.0, 0.8, 1.2, 1.5, 2.0]
	public static double[] SPEED_CLASSES = new double[]{1.0, 0.8, 1.2, 1.4, 1.6, 1.8, 2.0};   // oldenburgGen, 7 classes
//	public static double[] SPEED_CLASSES = new double[]{1.0};   // cal, 1 class

	public static String[] EDGE_COLORS = new String[]{"black", "blue", "green", "cyan", "red", "yellow", "white"};

	//USER_NOMINAL_SPEEDS = [50.0, 60.0, 80.0, 120.0, 150.0]
	//USER_NOMINAL_SPEEDS = [50.0, 60.0, 80.0, 120.0, 150.0, 200.0, 250.0]    // oldenburgGen
	public static double[] USER_NOMINAL_SPEEDS = new double[]{5000.0, 10000.0, 15000.0, 17000.0, 20000.0};    // cal
	public static int NUM_NOMINAL_SPEEDS = USER_NOMINAL_SPEEDS.length;

	public static double SPEED_PROFILE = 0.5; 	// 0.5: slow, 1.0: medium 3.0: fast

	public static int N_USERS = 10000;     // 2000, 5000, 10000, 20000
	//N_TRAIN_TRACES = 100

	public static int MIN_TRAIN_GROUP = 2;     // 3,2,5
	public static int MAX_TRAIN_GROUP = 10;    // 7,10,15

	public static int MIN_N_TRAIN_PATH = 20;
	public static int MAX_N_TRAIN_PATH = 80;

	public static int MIN_N_RANDOM_TRACE = 10;
	public static int MAX_N_RANDOM_TRACE = 20;

	public static int N_TIMESTEPS = 10;    // for prediction S_ij(k)

	public static int MAX_TRACE_LEN = 30;

	public static int MIN_SELECTED_TRACE_LEN = 5;

	//
	public static int K_MIN = 2;
	public static int K_MAX = 5;
	public static double MIN_LENGTH_LOW = 0.0005;
	public static double MIN_LENGTH_HIGH = 0.002;

	// for attack
	public static int MAX_OUTPUT_TIMESTEP = 50;    // 20, 50
	public static int ATTACK_TIME_STEPS = 40;

	public static int NUM_USERS_NO_DEV = 500;
	public static int NUM_USERS_WITH_DEV = 300;

	public static int N_LOOP_MARKOV = 20;

	//
	public static String getProfileName(){
		Map<Integer, String> num_user_dict = new HashMap<Integer, String>();
		num_user_dict.put(1000, "1k");
		num_user_dict.put(2000, "2k");
		num_user_dict.put(5000, "5k"); 
		num_user_dict.put(10000, "10k"); 
		num_user_dict.put(20000, "20k");
		
		String PROFILE_NAME = MAP_NAME + "_" + num_user_dict.get(N_USERS) + "_" + SPEED_PROFILE + "_" + K_MIN + "_" + K_MAX + "_" +
	                String.format("%.4f", MIN_LENGTH_LOW).substring(2) + "_" + String.format("%.4f", MIN_LENGTH_HIGH).substring(2) + "_" + 
	                MIN_TRAIN_GROUP + "_" + MAX_TRAIN_GROUP + "_" +
	                MIN_N_TRAIN_PATH + "_" + MAX_N_TRAIN_PATH + "_" + MIN_N_RANDOM_TRACE + "_" + MAX_N_RANDOM_TRACE;
	return PROFILE_NAME;
	}
	
}
