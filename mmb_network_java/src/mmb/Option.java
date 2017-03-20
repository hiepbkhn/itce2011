package mmb;

public class Option {

	public static double MAX_SPEED = 600;
	public static double INIT_DISTANCE = 1000;
	public static double MAX_USER = 10000;
	
//	public static double DISTANCE_CONSTRAINT = 500; // oldenburgGen
	public static double DISTANCE_CONSTRAINT = 15000; // cal
	
	
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
}
