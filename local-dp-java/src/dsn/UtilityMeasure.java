package dsn;

public class UtilityMeasure {


	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		String prefix = "";
		String sample_name = "";
		int nRun = 10;
		int round = 0;
//		int nSample = 100;
		int n_nodes = 10000;
		
		// COMMAND-LINE <prefix> <dataname> <round> <alpha> <beta> <nSample>
		if(args.length >= 3){
			prefix = args[0];
			sample_name = args[1];
			round = Integer.parseInt(args[2]);
		}

		System.out.println("sample_name = " + sample_name);
		System.out.println("round = " + round);
		
		

		//
		for (int t= 1; t < round+1; t++){
			System.out.println("round = " + t);
		
			for (int i = 0; i < nRun; i++){
				System.out.println("run i = " + i);
				
				String sample_file = prefix + "_sample/" + sample_name + "-" + t + ".out" + i;
				String matlab_file = prefix + "_matlab/" + sample_name + "-" + t + "." + i + ".mat";
				System.out.println("sample_file = " + sample_file);
				System.out.println("matlab_file = " + matlab_file);
				
				LinkExchangeInt2.computeLocalGraph(sample_file, matlab_file, n_nodes);
			}
		}
	}

}
