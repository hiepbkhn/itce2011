/*
 * 23 Mar, 2015
 * conversion from Python, run 30 times faster ! 
 */

package dp.der;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import dp.DPUtil;
import grph.Grph;
import grph.VertexPair;
import grph.algo.AdjacencyMatrix;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import grph.io.GrphTextReader;
import hist.DegreeSeqHist;
import toools.io.file.RegularFile;

// partition tuple (y,x,score)
class PTuple{
	int y;
	int x;
	double score;
	//
	public PTuple(int y, int x, double score) {
		super();
		this.y = y;
		this.x = x;
		this.score = score;
	}
	
}

// arrange tuple (s, exp, GS)
class ATuple{
	int s;
	double e;
	double Gs;
	//
	public ATuple(int s, double e, double gs) {
		super();
		this.s = s;
		this.e = e;
		this.Gs = gs;
	}
	
	
}

////
public class DensityExploreReconstruct {
	
	static final double cbr2 = Math.pow(2.0, 1.0/3);		// cube root of 2
	static int[] step_size = new int[] {16,8,4,2,1,1,1,1,1,1,1,1,1,1,1};
	static final int LARGE_AREA = 40000;

	////
	// combination
	public static double nCr_log(int n, int r){
	    if (n == 0)
	        return 0.0;
	    double s = 0.0;
	    for (int i = 0; i < n-r; i++)
	        s += Math.log((double)(n-i)/(n-r-i));
	    return s;
	}
	
	////
	public static int compute_h(int n_nodes, double mu, double eps){
		double RHS = (cbr2 - 1)*n_nodes*n_nodes*eps / (mu*Math.sqrt(2)*2);
		
		int h = 1;
	    while (true){
	    	// print cbr2* 2**(h*2) - (2**(h*5/3.0))
	        if (cbr2* Math.pow(2,(h*2)) - Math.pow(2,h*5/3.0) > RHS)
	            break;
	        h ++;
	    }
	    return h;
	}
	
	////
	public static void print2dMat(int[][] A){
		for (int i = 0; i < A.length; i++){
			for (int j = 0; j < A[0].length; j++)
				System.out.print(A[i][j] + "\t");
			System.out.println();
		}
	}
	
	////
	public static int[][] compute_A(Grph G){
		int n_nodes = G.getNumberOfVertices();
		int[][] A = new int[n_nodes][n_nodes];
		for (VertexPair p : G.getEdgePairs()){
			int u = p.first;
			int v = p.second;
			A[u][v] = 1;
			A[v][u] = 1;
		}
		
		//
		return A;
	}
	
	////
	public static int[][] compute_C(int[][] A){
		int n_nodes = A.length;
		int[][] C = new int[n_nodes+1][n_nodes+1];
		
		for (int i = 0; i < n_nodes; i++)
			for (int j = 0; j < n_nodes; j++)
	            C[i+1][j+1] = C[i][j+1] + C[i+1][j] - C[i][j] + A[i][j];      // C: 1-based indexing
		
		//
		return C;
	}
	
	////
	// y1,y2,x1,x2: all are inclusive
	public static double density(int[][]C, int y1, int y2, int x1, int x2){
	    return (C[y2+1][x2+1] - C[y2+1][x1] - C[y1][x2+1] + C[y1][x1])/((y2-y1+1)*(x2-x1+1));    // C: 1-based indexing
	}
	
	////
	// y1,y2,x1,x2: all are inclusive
	public static int count_1(int[][]C, int y1, int y2, int x1, int x2, double eps_cu){
	    // GEOMETRIC
	    double alpha = Math.exp(-eps_cu);
	    int value = (C[y2+1][x2+1] - C[y2+1][x1] - C[y1][x2+1] + C[y1][x1]) + DPUtil.geometricMechanism(alpha);      // integer, C: 1-based indexing
	    // truncated
	    if (value < 0)
	        value = 0;
	    if (value > (y2-y1+1)*(x2-x1+1))
	        value = (y2-y1+1)*(x2-x1+1);
	    //
	    return value;
	}
	
	////
	// partition non-leaf node u
	public static Int2 partition(Node u, double eps_p, int[][] C, int n_nodes, double h, boolean use_step){   
	    ArrayList<PTuple> score_list = new ArrayList<PTuple>(); // score for each splitting point p
	//  double  GSq = 2.0* 4**(u.level+1)/(n_nodes*n_nodes);     // max sensitivity, 2
	    double GSq = 1.0* Math.pow(4,(u.level+1)/(n_nodes*n_nodes));     // max sensitivity, 1 
	    double min_area =  (double)(n_nodes*n_nodes)/(Math.pow(4, u.level+2));     // u.level+1
	//    print "GSq =", GSq
	//    print "min_area =", min_area
	    
	    int step = 1;
	    if (use_step)
	        step = step_size[u.level];
	    for (int y = u.y1+1; y < u.y2; y += step)
	        for (int x = u.x1+1; x < u.x2; x += step){
	            // constraint of region area
	            if ((y+1-u.y1)*(x+1-u.x1) < min_area || (y+1-u.y1)*(u.x2-x) < min_area ||
	                (u.y2-y)*(x+1-u.x1) < min_area || (u.y2-y)*(u.x2-x) < min_area)
	                continue;
	            
	            double[] density_list = new double[]{density(C, u.y1, y, u.x1, x), density(C, u.y1, y, x+1, u.x2),
	                            density(C, y+1, u.y2, u.x1, x), density(C, y+1, u.y2, x+1, u.x2)};
	            double q = DPUtil.max(density_list) - DPUtil.min(density_list);
	            score_list.add(new PTuple(y,x, Math.exp(eps_p/(2*h*GSq)*q)) );     // (y,x) for exponential mechanism
	        }

	    if (score_list.size() == 0)
	        return new Int2(0,0); 

	    // normalize score_list
	    double sum_q = 0.0;
	    for (PTuple item : score_list)
	    	sum_q += item.score;
	    	
	//    print "sum_q =", sum_q
	    for (PTuple item : score_list)
	        item.score = item.score/sum_q;
	        
	    // exponential mechanism
	    Random random = new Random();
	    double rand_val = random.nextDouble();
	    double cur_val = 0.0;
	    int i = 0;
	    while (true){
	        if ((i == score_list.size()-1) || (cur_val + score_list.get(i).score > rand_val))
	            break;
	        cur_val += score_list.get(i).score;
	        i += 1;
	    }
	    // return 
	    int y = score_list.get(i).y;
	    int x = score_list.get(i).x;
	    
	    return new Int2(y,x);
	}
	
	////
	// quadtree
	// eps_c (count), eps_p (partition)
	public static Node exploreDenseRegion(Grph G, int[][] A, int[][] C, double mu, double eps_c, double eps_p, boolean use_step){
	    int n_nodes = G.getNumberOfVertices();
	    
	    // quad-tree
	    Node QT = new Node(0,n_nodes-1,0,n_nodes-1, 0);  // QT.level = 0
	    QT.noisy_count = C[n_nodes][n_nodes];                    // C: 1-based indexing
	    
	//    h = math.log(n_nodes)/math.log(2) # log2 of n_nodes
	    int h = compute_h(n_nodes, mu, eps_c);
	    System.out.println("h = " + h);
	    
	    
	    Queue<Node> queue = new LinkedList<Node>(); 
	    queue.add(QT);
	    int count = 0;
	    System.out.println("0.8*n_nodes*n_nodes/(4^h) = " + (0.8*n_nodes*n_nodes/Math.pow(4,h)));
	    
	    while (queue.size() > 0){
	        Node u = queue.remove();
	        
	        count += 1;
	        if (count % 10 == 0)
	            System.out.println("# areas processed : " + count);
	            
	        //
	        if (u.level == h){
	            u.is_leaf = true;
	            continue;
	        }
	            
	        // check 2 stop conditions
	        double density_u = (double)u.noisy_count/((u.y2-u.y1+1)*(u.x2-u.x1+1));     // density(C, u.y1, u.y2, u.x1, u.x2)
	        if (density_u >= 0.8){
	            u.is_leaf = true;
	            u.stop_cond = 1;
	            System.out.println("1st cond meet at u");
	            continue;
	        }
	        
	        if (u.noisy_count < 0.8*n_nodes*n_nodes/Math.pow(4,h)){
	            u.is_leaf = true;
	            System.out.println("2nd cond meet at u");
	            u.stop_cond = 2;
	            continue;
	        }
	        
	        // calculate eps_cu, eps_pu
	        int next_level = u.level + 1;
	        double eps_cu = Math.pow(cbr2,next_level) * (cbr2-1) * eps_c/(Math.pow(cbr2,h+1) - 1);      // after Cormode's paper
	        double eps_pu = eps_p/h;
	        Int2 par = partition(u, eps_pu, C, n_nodes, h, use_step);                 // call partition()
	        int y = par.val0;
	        int x = par.val1;
	        
	        if (y == 0 && x == 0){   // len(score_list) == 0
	            u.is_leaf = true;
	            System.out.println("3rd cond meet at u");
	            u.stop_cond = 3;
	            continue;
	        }
	        
	        // four subregions
	        Node s1 = new Node(u.y1, y, u.x1, x, u.level+1);
	        s1.noisy_count = count_1(C, u.y1, y, u.x1, x, eps_cu);
	        Node s2 = new Node(u.y1, y, x+1, u.x2, u.level+1);
	        s2.noisy_count = count_1(C, u.y1, y, x+1, u.x2, eps_cu);
	        Node s3 = new Node(y+1, u.y2, u.x1, x, u.level+1);
	        s3.noisy_count = count_1(C, y+1, u.y2, u.x1, x, eps_cu);
	        Node s4 = new Node(y+1, u.y2, x+1, u.x2, u.level+1);
	        s4.noisy_count = count_1(C, y+1, u.y2, x+1, u.x2, eps_cu);
	        
	        u.children = new Node[]{s1, s2, s3, s4};
	        
	        // add to queue
	        queue.add(s1);
	        queue.add(s2);
	        queue.add(s3);
	        queue.add(s4);
	    }
	    //
	    return QT;
	}
	
	////
	public static ArrayList<Int2> randomArrange(Node u, int m, int l, int c1){
		ArrayList<Int2> result = new ArrayList<Int2>();
	    List<Integer> list_1 = new ArrayList<Integer>();
	    for (int i = 0; i < m*l; i++)
	    	list_1.add(i);
	    Collections.shuffle(list_1);	// for permutation
	    
	    for (int i = 0; i < c1; i++)
	        result.add (new Int2(u.y1 + i / l, u.x1 + i % l));
	    //
	    return result; 
	}
	
	////
	// arrange the area covered by u
	// c: true count (computed from C), c1: noisy count
	// return: list of 1-locs
	public static ArrayList<Int2> arrangeArea(int[][] A, int c, Node u, double epsA, int large_area){
//		System.out.println(u.level + " " + u.stop_cond + " [" + u.y1 + " " + u.y2 + " " + u.x1 + " " + u.x2 + "] " + u.noisy_count);
	    
	    // random if edge density is low
	    int m = u.y2-u.y1+1;
	    int l = u.x2-u.x1+1;
	    int c1 = u.noisy_count;
	    
	    if (c1 == 0){
//	        System.out.println();
	        return new ArrayList<Int2>();
	    }
	    
	//    if float(u.noisy_count)/(m*l) < LOW_DENSITY:
	//        print float(u.noisy_count)/(m*l)
	//        return random_arrange(u, m, l, c1) 
	//    else:
	//        print
	    
	    // otherwise
	    double GSq = 1.0;   // 2.0
	    int lower_s = Math.max(c1+c-m*l, m*l-c-c1);
	    int upper_s = Math.max(m*l+c-c1, m*l+c1-c);
	    // DEBUG
	//    print "c =", c
	//    print "c1 =", c1
	//    print "m,l =", m, l
	//    print lower_s, upper_s
	    
	    if (m*l > large_area){
	        System.out.println("large " + m*l);
	        return randomArrange(u, m, l, c1); 
	    }
//	    else
//	        System.out.println();
	    
	    ArrayList<ATuple> score_list = new ArrayList<ATuple>();
	    double max_logGs = 0.0;
	    for (int s = lower_s; s < upper_s+1; s++)
	        if ((s+c+c1-m*l) % 2 == 0)      // must be even
	            if ((s+c+c1-m*l)/2 >= 0 && (m*l+c1-s-c)/2 >= 0 && (s+c+c1-m*l)/2 <= c && (m*l+c1-s-c)/2 <= m*l-c){
	//                Gs = math.log(nCr(c,(s+c+c1-m*l)/2) * nCr(m*l-c,(m*l+c1-s-c)/2))        // store log(Gs)
	                double Gs = nCr_log(c,(s+c+c1-m*l)/2) + nCr_log(m*l-c,(m*l+c1-s-c)/2);
	                if (max_logGs < Gs)
	                    max_logGs = Gs;
	//                score_list.append([s, math.exp(s*epsA/(2*GSq)) * Gs])                   // may encounter OverflowError
	                score_list.add(new ATuple(s, Math.exp((s-upper_s)*epsA/(2*GSq)), Gs));  			// Gs up to 1760 digits !   
	                
	            }
	//    print "score_list =", score_list
	    // normalize score_list
	    double sum_q = 0.0;
	    for (ATuple item : score_list){
	        item.Gs = Math.exp(item.Gs-max_logGs);   
	    	sum_q += item.e*item.Gs;					// item[1] * exp(log(Gs)-max_logGs), sum_q ~ 0.0 (Underflow !)
	    }
	    
	    for (ATuple item : score_list)
	        item.e = item.e/sum_q;
	    
	//    print "after NORM, score_list =", score_list
	        
	    // exponential mechanism
	    Random random = new Random();
	    double rand_val = random.nextDouble();
	    double cur_val = 0.0;
	    int i = 0;
	    while (true){
	        if ((i == score_list.size()-1) || (cur_val + score_list.get(i).e > rand_val))
	            break;
	        cur_val += score_list.get(i).e;
	        i += 1;
	    }
	     
	    int s = score_list.get(i).s;
	        
	    // list of 1 locs, 0 locs
	    ArrayList<Int2> loc_1 = new ArrayList<Int2>();
	    ArrayList<Int2> loc_0 = new ArrayList<Int2>();
	    for (i = u.y1; i < u.y2+1; i++)
	        for (int j = u.x1; j < u.x2+1; j++)
	            if (A[i][j] == 1)
	                loc_1.add(new Int2(i,j));
	            else
	            	loc_0.add(new Int2(i,j));

	    assert (loc_1.size() == c);
	    //
	    int n1T = (s+c+c1-m*l)/2;    // for Aij=1
	    int n1F = (m*l+c1-c-s)/2;    // for Aij=0
	    ArrayList<Int2> result = new ArrayList<Int2>();
	    if (c > 0){
	        ArrayList<Integer> list_1T = new ArrayList<Integer>();
    		for (i = 0; i < c; i++)
    			list_1T.add(i);
    	    Collections.shuffle(list_1T);	// for permutation
	        for (i = 0; i < n1T; i++)
	            result.add(loc_1.get(list_1T.get(i)));
	    }
	    
	    if (c < m*l){
	        ArrayList<Integer> list_1F = new ArrayList<Integer>();
    		for (i = 0; i < m*l-c; i++)
    			list_1F.add(i);
    	    Collections.shuffle(list_1F);	// for permutation
	        for (i = 0; i < n1F; i++)
	            result.add(loc_0.get(list_1F.get(i)));
	    }
	    // 
	    return result;
	}
	
	///
	public static Grph arrangeEdge(int n_nodes, int[][] A, int[][] C, Node QT, double epsA, int large_area){
	    ArrayList<Int2> loc1 = new ArrayList<Int2>();
	    Queue<Node> queue = new LinkedList<Node>();
	    queue.add(QT);
	    int count = 0;
	    while (queue.size() > 0){
	        Node u = queue.remove();
	        int c = C[u.y2+1][u.x2+1] - C[u.y2+1][u.x1] - C[u.y1][u.x2+1] + C[u.y1][u.x1];      // C: 1-based indexing
	        
	        if (u.is_leaf){
	            count += 1;
//	            if (count % 10 == 0)
//	                System.out.println("# areas arranged :" + count);
	            ArrayList<Int2>result = arrangeArea(A, c, u, epsA, large_area);                // call arrange_area()
	            loc1.addAll(result);    // edges
	        //
	        }else{
	            queue.add(u.children[0]);
	            queue.add(u.children[1]);
	            queue.add(u.children[2]);
	            queue.add(u.children[3]);
	        }
	    }
	    // post-process
	    Grph aG = new InMemoryGrph();
	    aG.addNVertices(n_nodes);
	    Random random = new Random();
	    for (Int2 item : loc1){
	    	int u = item.val0;
	    	int v = item.val1;
	        if (u != v)                      // no self-loops
	            if (random.nextDouble() < 0.5)	// for symmetry
	                aG.addSimpleEdge(u, v, false);
	    }
	    //
	    return aG;
	}
	        
	////
	public static void print_QT(Node QT){
	    System.out.println(QT.level + " " + QT.stop_cond + " [" + QT.y1 + " " + QT.y2 + " " + QT.x1 + " " + QT.x2 + "] " + QT.noisy_count + " " + QT.is_leaf);
	    if (QT.children != null){
	        print_QT(QT.children[0]);
	        print_QT(QT.children[1]);
	        print_QT(QT.children[2]);
	        print_QT(QT.children[3]);
	    }
	}
	        
	////
	public static void print_QT_leaves(Node QT){
	    if (QT.is_leaf)
	    	System.out.println(QT.level + " " + QT.stop_cond + " [" + QT.y1 + " " + QT.y2 + " " + QT.x1 + " " + QT.x2 + "] " + QT.noisy_count);
	        
	    if (QT.children != null){
	        print_QT_leaves(QT.children[0]);
	        print_QT_leaves(QT.children[1]);
	        print_QT_leaves(QT.children[2]);
	        print_QT_leaves(QT.children[3]);
	    }
	}
	
	///////////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// TOY GRAPH
//		Grph G = new InMemoryGrph();
//		G.addNVertices(8);
//		G.addSimpleEdge(0, 5, false); G.addSimpleEdge(0, 6, false); G.addSimpleEdge(0, 7, false);
//		G.addSimpleEdge(1, 5, false); G.addSimpleEdge(1, 6, false); G.addSimpleEdge(1, 7, false);
//		G.addSimpleEdge(2, 6, false); G.addSimpleEdge(2, 7, false); G.addSimpleEdge(2, 4, false);
//		G.addSimpleEdge(3, 4, false); // example in the paper
		
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 
		String dataname = "as20graph";		// (6474,12572)		com_C: 0.2s,  step=1:~3360 nodes, expl (20s), arr(8s), edit.dist=12715 
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	com_C: 0.6s, use_step:~35.3k nodes, expl (39s), arr(57s), h=11	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	com_C: 1.6s
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps_c> <eps_p> <epsA>
		String prefix = "";
	    int n_samples = 10;
	    double eps_c = 1.0;
		double eps_p = 1.0;
		double epsA = 1.0;
		double mu = 5;
		
		if(args.length >= 6){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			eps_c = Double.parseDouble(args[3]);
			eps_p = Double.parseDouble(args[4]);
			epsA = Double.parseDouble(args[5]);
		}
		System.out.println("dataname = " + dataname);
		
		System.out.println("n_samples = " + n_samples);
		System.out.println("eps_c = " + eps_c);
		System.out.println("eps_p = " + eps_p);
		System.out.println("epsA = " + epsA);
		
		
		String filename = prefix + "_data/" + dataname + ".gr";
		String sample_file = prefix + "_sample/" + dataname + "_der_" + String.format("%.1f", eps_c) + "_" + 
									String.format("%.1f", eps_p) + "_" + String.format("%.1f", epsA);
		System.out.println("sample_file = " + sample_file);
		
		//
		Grph G;
		EdgeListReader reader = new EdgeListReader();
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
			
//		AdjacencyMatrix A = G.getAdjacencyMatrix();
//		System.out.println(A.toString());
		
		long start = System.currentTimeMillis();
		int[][] A = compute_A(G);
		System.out.println("compute_A - DONE, elapsed " + (System.currentTimeMillis() - start));
		
//		System.out.println("A =");
//		print2dMat(A);
		
		start = System.currentTimeMillis();
		int[][] C = compute_C(A);
		System.out.println("compute_C - DONE, elapsed " + (System.currentTimeMillis() - start));
//		System.out.println("C =");
//		print2dMat(C);
		
		//
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			start = System.currentTimeMillis();
			
			Node QT;
			if (G.getNumberOfVertices() < 10000)
				QT = exploreDenseRegion(G, A, C, mu, eps_c, eps_p, false);   // for as20graph and smaller graphs
			else
				QT = exploreDenseRegion(G, A, C, mu, eps_c, eps_p, true);   // for ca-HepPh and larger..
	        System.out.println("exploreDenseRegion - DONE, elapsed " + (System.currentTimeMillis() - start));
			
	//        print_QT(QT);
	//        print_QT_leaves(QT);
	        
	        start = System.currentTimeMillis();
	        int large_area = 40000;
	        Grph aG = arrangeEdge(G.getNumberOfVertices(), A, C, QT, epsA, large_area);
	        System.out.println("arrangeEdge - DONE, elapsed " + (System.currentTimeMillis() - start));
	        System.out.println("#nodes = " + aG.getNumberOfVertices());
			System.out.println("#edges = " + aG.getNumberOfEdges());
			
//			start = System.currentTimeMillis();
//			System.out.println("edit distance (aG, G) = " + DegreeSeqHist.editScore(aG, G));
//			System.out.println("editScore - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(aG, f);
		}
	}

}
