/*
 * 
 * Mar 24, 2015
 * 	- add computeTopLevels() (top-down Node.toplevel) to speed up compute_nL_nR()
 *  - lowestCommonAncestor() runs faster using Node.level 
 * Mar 25
 * 	- logLK() missed minus (-) in the formula of L 
 * Apr 2
 *  - addlaplaceNoise(): use Geometric Mechanism
 *  - generateSanitizedSample(): run much faster O(m)
 * Sep 17
 * 	- add partitionTopDown() using queue (see dynamic programming in NodeSetMod.bestCut())
 * Sep 24
 * 	- add buildDendrogram() using EdgeWeightedGraph
 * Nov 6
 * 	- add printDendrogram()
 * 	!! fix Overflow in config_2(), config_3(), buildDendrogram() (2 overloads)
 * 	- add buildDendrogram(UnweightedGraph)
 * Nov 8
 * 	!! fix Overflow in logLK()
 * Nov 9
 * 	- dendrogramFitting(): write to node_file instead of store to list_T + force recomputations in computeTopLevels(), computeNodeLevels()
 * Nov 12
 * 	- field logLK faster computed in config_2(), config_3() instead of calling logLK()
 * 	- EdgeIntGraph in place of Grph
 * Dec 2
 * 	- replace Grph with EdgeIntGraph
 * 	- compute_LS_RS() extracted from generateSanitizedSample()
 * 	- readTree() used in
 */

package dp.mcmc;

import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import algs4.Edge;
import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.EdgeWeightedGraph;
import algs4.UnweightedGraph;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.DPUtil;
import dp.combined.NodeSetLouvain;
import dp.mcmc.Int4;
import dp.mcmc.Node;
import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class Dendrogram {
	public static final int SAMPLE_FREQ = 1000;
	
	public Node root_node;
	public Node[] node_list;		// list of leaf nodes
	public HashMap<Integer, Node> node_dict;
	public int[] int_nodes;		//list of ids of internal nodes
	
	public double logLK;		// log-likelihood
	
	////
	public Dendrogram(){
		this.root_node = null;
		
	}
	
	////
	public Dendrogram copy(){
		Dendrogram T2 = new Dendrogram();
		
		// copy node_dict first
        T2.node_dict = new HashMap<Integer, Node>();
        for (Map.Entry<Integer, Node> entry : this.node_dict.entrySet()){
        	int k = entry.getKey();
        	Node u = entry.getValue();
            Node u2 = u.copy();   // clone u
            T2.node_dict.put(k, u2);
        }
        
        // int_nodes
        T2.int_nodes = Arrays.copyOf(this.int_nodes, this.int_nodes.length);
        
        // node_list
        T2.node_list = new Node[this.node_list.length];
        for (Node u : T2.node_dict.values())
            if (u.id >= 0)
                T2.node_list[u.id] = u;
        
        // parent, left, right
        for (Node u : this.node_dict.values()){
            Node u2 = T2.node_dict.get(u.id);
            if (u.parent != null)
                u2.parent = T2.node_dict.get(u.parent.id);
            if (u.left != null)
                u2.left = T2.node_dict.get(u.left.id);
            if (u.right != null)
                u2.right = T2.node_dict.get(u.right.id);
        }        
        // root_node
        T2.root_node = T2.node_dict.get(this.root_node.id);
            
		
		//
		return T2;
	}
	
	////
	public void initByGraph(EdgeIntGraph G){
		// compute node_list[]
		this.node_list = new Node[G.V()];
        for (int u = 0; u < G.V(); u++){
            Node node = new Node(u,null,0.0);      // leaf node
            node.nL = 1;                     // IMPORTANT in config_2(), config_3()
            this.node_list[u] = node;
        }
        // init binary
        this.root_node = initBinary(this.node_list,1);
        
        // compute node_dict{}
        this.int_nodes = new int[this.node_list.length-1];
        this.node_dict = buildNodeDict(this.root_node, this.int_nodes);
        
        //
        long start = System.currentTimeMillis();
        this.computeNodeLevels();		// for compute_nL_nR()
//        this.computeTopLevels();		// already called in computeNodeLevels
        
        buildDendrogram(this.node_list, this.node_dict, this.root_node, G);
        System.out.println("build_dendrogram - DONE, elapsed " + (System.currentTimeMillis() - start));
        
        //
	    this.logLK = logLK();
	    System.out.println("this.logLK = " + this.logLK);
	}
	
	////
	public void initByInternalNodes(EdgeIntGraph G, Int4[] int_nodes){
		// compute node_list[]
		this.node_list = new Node[G.V()];
        for (int u = 0; u < G.V(); u++){
            Node node = new Node(u,null,0.0);      // leaf node
            node.nL = 1;                     // IMPORTANT in config_2(), config_3()
            this.node_list[u] = node;
        }
        // init binary
        this.root_node = initBottomUp(this.node_list, int_nodes);
        
        // compute node_dict{}
        this.int_nodes = new int[this.node_list.length-1];
        this.node_dict = buildNodeDict(this.root_node, this.int_nodes);
        
        //
        long start = System.currentTimeMillis();
        this.computeNodeLevels();		// for compute_nL_nR()
//      this.computeTopLevels();		// already called in computeNodeLevels
        
        buildDendrogram(this.node_list, this.node_dict, this.root_node, G);
        System.out.println("build_dendrogram - DONE, elapsed " + (System.currentTimeMillis() - start));
	}
	
	////
    // r_node: internal node, not root
    public Node config_2(EdgeIntGraph G, Node r_node){
        
        Node p_node = r_node.parent;
        
        // CASE 1: t right
        if (r_node.id == p_node.left.id){     
            Node t_node = r_node.right;
            Node u_node = p_node.right;
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK -= r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK -= p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));  
            
            // update nL, nR, nEdge, value
            r_node.nR = (t_node.nL + t_node.nR) + (u_node.nL + u_node.nR);
            p_node.nL = (t_node.nL + t_node.nR);
            
            int n_st_u = p_node.nEdge;
            int n_st = r_node.nEdge;
            int n_tu = countBetweenEdges(G, t_node, u_node);
            int n_su = n_st_u - n_tu;
            assert (n_su >= 0);
            int n_s_tu = n_st + n_su;
            
            r_node.nEdge = n_s_tu;
            r_node.value = (double)n_s_tu/r_node.nL/r_node.nR;
            p_node.nEdge = n_tu;
            p_node.value = (double)n_tu/p_node.nL/p_node.nR; 
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK += r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK += p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            //
            t_node.parent = p_node;
            p_node.left = t_node;
            if (p_node.parent == null){
                r_node.parent = null;        // change root
                this.root_node = r_node;
            }else{
                r_node.parent = p_node.parent;
                if (p_node.id == p_node.parent.left.id)  // update
                    p_node.parent.left = r_node;
                else
                    p_node.parent.right = r_node;
            }
            p_node.parent = r_node;
            r_node.right = p_node;
        }    
            
            
        // CASE 2: t left    
        if (r_node.id == p_node.right.id){   
            Node t_node = r_node.left;
            Node u_node = p_node.left;
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK -= r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK -= p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            // update nL, nR, nEdge, value
            r_node.nL = (t_node.nL + t_node.nR) + (u_node.nL + u_node.nR);
            p_node.nR = (t_node.nL + t_node.nR);
            
            int n_st_u = p_node.nEdge;
            int n_st = r_node.nEdge;
            int n_tu = countBetweenEdges(G, t_node, u_node);
            int n_su = n_st_u - n_tu;
            assert (n_su >= 0);
            int n_s_tu = n_st + n_su;
            
            r_node.nEdge = n_s_tu;
            r_node.value = (double)n_s_tu/r_node.nL/r_node.nR;
            p_node.nEdge = n_tu;
            p_node.value = (double)n_tu/p_node.nL/p_node.nR; 
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK += r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK += p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            //
            t_node.parent = p_node;
            p_node.right = t_node;
            if (p_node.parent == null){   
                r_node.parent = null;        // change root
                this.root_node = r_node;
        	}else{
                r_node.parent = p_node.parent;
                if (p_node.id == p_node.parent.left.id)  // update
                    p_node.parent.left = r_node;
                else
                    p_node.parent.right = r_node;
        	}
            p_node.parent = r_node;
            r_node.left = p_node;
        }
        //
        return p_node;
    }
	
    ////
    // r_node: internal node, not root
    // RETURN r_node (whereas config_2() return p_node)
    public Node config_3(EdgeIntGraph G, Node r_node){
        
        Node p_node = r_node.parent;
        
        // CASE 1: t right
        if (r_node.id == p_node.left.id){ 
            Node t_node = r_node.right;
            Node u_node = p_node.right;
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK -= r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK -= p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            // update nL, nR, nEdge, value
            r_node.nR = (u_node.nL + u_node.nR);
            p_node.nL = (r_node.nL + r_node.nR);
            p_node.nR = (t_node.nL + t_node.nR);
            
            int n_st_u = p_node.nEdge;
            int n_st = r_node.nEdge;
            int n_tu = countBetweenEdges(G, t_node, u_node);
            int n_su = n_st_u - n_tu;
            assert (n_su >= 0);
            int n_su_t = n_st + n_tu;
            
            r_node.nEdge = n_su;
            r_node.value = (double)n_su/r_node.nL/r_node.nR;
            p_node.nEdge = n_su_t;
            p_node.value = (double)n_su_t/p_node.nL/p_node.nR; 
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK += r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK += p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            //
            t_node.parent = p_node;
            p_node.right = t_node;
            
            u_node.parent = r_node;
            r_node.right = u_node;
        }
        // CASE 2: t left    
        if (r_node.id == p_node.right.id){    
            Node t_node = r_node.left;
            Node u_node = p_node.left;
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK -= r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK -= p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            // update nL, nR, nEdge, value
            r_node.nL = (u_node.nL + u_node.nR);
            p_node.nL = (t_node.nL + t_node.nR);
            p_node.nR = (r_node.nL + r_node.nR);
            
            int n_st_u = p_node.nEdge;
            int n_st = r_node.nEdge;
            int n_tu = countBetweenEdges(G, t_node, u_node);
            int n_su = n_st_u - n_tu;
            assert (n_su >= 0);
            int n_su_t = n_st + n_tu;
            
            r_node.nEdge = n_su;
            r_node.value = (double)n_su/r_node.nL/r_node.nR;
            p_node.nEdge = n_su_t;
            p_node.value = (double)n_su_t/p_node.nL/p_node.nR; 
            
            if (r_node.value > 0.0 && r_node.value < 1.0)
            	this.logLK += r_node.nL* (r_node.nR * (r_node.value * Math.log(r_node.value) + (1-r_node.value)*Math.log(1-r_node.value))); 
            if (p_node.value > 0.0 && p_node.value < 1.0)
            	this.logLK += p_node.nL* (p_node.nR * (p_node.value * Math.log(p_node.value) + (1-p_node.value)*Math.log(1-p_node.value)));
            
            //
            t_node.parent = p_node;
            p_node.left = t_node;
            
            u_node.parent = r_node;
            r_node.left = u_node;
        }
        //
        return r_node;
    }
    
	////
	public double logLK(){
        double L = 0.0;
        Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.root_node);
		while (queue.size() > 0){
			Node r = queue.remove();
            if (r.value > 0.0 && r.value < 1.0)
                L += -r.nL* (r.nR * (-r.value * Math.log(r.value) - (1-r.value)*Math.log(1-r.value))); // (r.nR: to avoid overflow !
            
            if (r.left.id < 0)            // only internal nodes
                queue.add(r.left);
            if (r.right.id < 0)
                queue.add(r.right);    
		}
        //
        return L;
	}
	
	//// Bottom-Up (using 'level')
	public void writeInternalNodes(String filename) throws IOException{
		
		this.computeNodeLevels(); // compute node levels (level and toplevel)
		
		// debug
//		Queue<Node> queue = new LinkedList<Node>();
//		queue.add(this.root_node);
//		while (queue.size() > 0){
//			Node r = queue.remove();
//			int parent_id = 100000000;
//			if (r.parent != null)
//				parent_id = r.parent.id;
//			System.out.println(r.id + "(" + parent_id + "):" + r.left.id + "," + r.right.id + ": level=" + r.level + ", toplevel=" + r.toplevel);
//			
//            if (r.left.id < 0)            // only internal nodes
//                queue.add(r.left);
//            if (r.right.id < 0)
//                queue.add(r.right);    
//		}
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

		for (int level = 1; level < this.root_node.level + 1; level++)
			for (Node node : this.node_dict.values()) {
				if (node.id >= 0 || node.level != level) // print by level (ascending)
					continue;
				int parent_id = 0;
				if (node.parent != null)
					parent_id = node.parent.id;
				else
					parent_id = Node.ROOT_NODE;
				bw.write(node.id + " " + parent_id + " " + node.left.id + " " + node.right.id + "\n");
			}
		bw.close();
	}
	
	public void readInternalNodes(EdgeIntGraph G, String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
        
        Int4[] int_nodes = new Int4[G.V()-1];  // list of tuples (id, parent.id, left.id, right.id)
        int u = 0;
        while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split(" ");
        	int_nodes[u++] = new Int4(Integer.parseInt(items[0]), Integer.parseInt(items[1]),
        			Integer.parseInt(items[2]), Integer.parseInt(items[3]));
        	
        }
        
        br.close();
        //
        this.initByInternalNodes(G, int_nodes);
	}
	
	////
	/* 	recursive, negative ids for internal nodes
		node_list: list of leaf nodes
	*/
	static Node initBinary(Node[] node_list, int count){
		if (node_list.length == 0) return null;
		if (node_list.length == 1) return node_list[0];
		
		int mid = node_list.length / 2;
		Node root_node = new Node(-count,null,0.0);   // internal node, id=-count (negative)
		Node left_node = initBinary(Arrays.copyOfRange(node_list, 0, mid), count*2);    
	    Node right_node = initBinary(Arrays.copyOfRange(node_list, mid, node_list.length), count*2+1);
	    if (left_node != null){
	        root_node.left = left_node;
	        left_node.parent = root_node;
	    }
	    if (right_node != null){
	        root_node.right = right_node;   
	        right_node.parent = root_node;
	    }
		
		return root_node;
	}
	
	////
//	static Node initBottomUp(Node[] node_list, Quartet<Integer,Integer,Integer,Integer>[] int_nodes){
//		HashMap<Integer, Node> int_dict = new HashMap<Integer, Node>();
//		for (Quartet<Integer,Integer,Integer,Integer> u: int_nodes)
//			int_dict.put(u.getValue0(), new Node(u.getValue0(), null, 0.0));
//		for (Quartet<Integer,Integer,Integer,Integer> u: int_nodes)
//			
//		
//		return null;
//	}
	
	////
	static Node initBottomUp(Node[] node_list, Int4[] int_nodes){
		HashMap<Integer, Node> int_dict = new HashMap<Integer, Node>();
		Node root_node = null;
		
		for (Int4 u: int_nodes)
			int_dict.put(u.val0, new Node(u.val0, null, 0.0));
		
		for (Int4 u: int_nodes){
//			System.out.println(u.val0 + "," + u.val1 + "," + u.val2 + "," + u.val3);
			
			if (u.val1 == Node.ROOT_NODE){						// root node
				int_dict.get(u.val0).parent = null;
				root_node = int_dict.get(u.val0); 
			}else
				int_dict.get(u.val0).parent = int_dict.get(u.val1);
			
			if (u.val2 >= 0){
	            int_dict.get(u.val0).left = node_list[u.val2];   // point to leaf node
	            node_list[u.val2].parent = int_dict.get(u.val0);
			}else        
	            int_dict.get(u.val0).left = int_dict.get(u.val2);
			
			if (u.val3 >= 0){
	            int_dict.get(u.val0).right = node_list[u.val3];   // point to leaf node
	            node_list[u.val3].parent = int_dict.get(u.val0);
			}else        
	            int_dict.get(u.val0).right = int_dict.get(u.val3);
		}
		
		return root_node;
	}
	
	////
	static List<Integer> findChildren(Node u){
		if (u.id >= 0)
			return Arrays.asList(u.id);
		
		ArrayList<Integer> u_list = new ArrayList<Integer>();
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(u);
		while (queue.size() > 0){
			Node cur_node = queue.remove();
			
			if (cur_node.left.id >= 0)
	            u_list.add(cur_node.left.id);
	        else
	            queue.add(cur_node.left);
			
			if (cur_node.right.id >= 0)
	            u_list.add(cur_node.right.id);
	        else
	            queue.add(cur_node.right);
		}
		
		return u_list;
	}
	
	////
	static int countBetweenEdges(EdgeIntGraph G, Node u, Node t){
		int count = 0;
		List<Integer> u_list = findChildren(u);
		List<Integer> t_list = findChildren(t);
		
		// 1-Grph
//		for (int u1: u_list){
//			IntSet s = G.getNeighbours(u1);
//			for (int t1: t_list)
//				if (s.contains(t1))
//					count++;
		
		// slow
//		for (int u1: u_list){
//			for (int t1: t_list)
//				if (G.areVerticesAdjacent(u1, t1))
//					count++;
			
//				if (A.get(u1, t1) == 1)
//					count++;
		
		// 2-EdgeIntGraph
		for (int u1: u_list){
			for (int t1: t_list)
				if (G.areEdgesAdjacent(u1, t1))
					count++;
		
		
		
		}
		return count;
	}
	
	////
	public static HashMap<Integer, Node> buildNodeDict(Node root_node, int[] int_nodes){
		HashMap<Integer, Node> node_dict =  new HashMap<Integer, Node>();
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root_node);
		while (queue.size() > 0){
			Node cur_node = queue.remove();
			node_dict.put(cur_node.id, cur_node);
			if (cur_node.left != null)
				queue.add(cur_node.left);
			if (cur_node.right != null)
				queue.add(cur_node.right);
			
		}
		
		// int_nodes
		int i = 0; 
		for (int k : node_dict.keySet())
			if (k < 0){
				int_nodes[i] = k;
				i++;
			}
		return node_dict;
	}
	
	////
	// find root from any node
	static Node findRoot(Node any_node){
		Node a_node = any_node;
		while (a_node.parent != null)
			a_node = a_node.parent;
		return a_node;
	}
	
	//// return id of 
	static int lowestCommonAncestor(Node u, Node v){
		// computeTopLevels already called
	    Node t1 = u;
	    Node t2 = v;
//	    System.out.println("lowestCommonAncestor called");
//	    System.out.println("BEFORE: toplevel = " + t1.toplevel + " " + t2.toplevel);
	    
	    if (t1.toplevel > t2.toplevel){
	    	int diff = t1.toplevel-t2.toplevel;
	    	for (int i = 0; i < diff; i++)
	    		t1 = t1.parent;
	    }else{
	    	int diff = t2.toplevel-t1.toplevel;
	    	for (int i = 0; i < diff; i++)
	    		t2 = t2.parent;
	    }
	    
//	    System.out.println("AFTER: toplevel = " + t1.toplevel + " " + t2.toplevel);
	    while (t1.id != t2.id){
//	    	System.out.println(t1.id + " " + t2.id + " :  toplevel = " + t1.toplevel + " " + t2.toplevel);
	        t1 = t1.parent;
	        t2 = t2.parent;
	        
	    }
	    
	    return t1.id;
	}
	
	////
//	static void compute_nL_nR(HashMap<Integer, Node> node_dict){
//		Queue<Node> queue = new LinkedList<Node>();
//		
//		for (Node u: node_dict.values()){
//			if (u.id < 0)
//				if (u.left.id >= 0 && u.right.id >= 0){
//	                u.nL = 1;
//	                u.nR = 1;
//				}else{
//	                if (u.left.id >= 0)
//	                    u.nL = 1;
//	                if (u.right.id >= 0)
//	                    u.nR = 1;
//	                queue.add(u);     // wait for processing
//				}
//		}
//		//
//		while (queue.size() > 0){
//			Node cur_node = queue.remove();
//	        if (cur_node.nL == 0 && cur_node.nR > 0){
//	            if (cur_node.left.nL > 0 && cur_node.left.nR > 0)
//	                cur_node.nL = cur_node.left.nL + cur_node.left.nR;
//	            else
//	                queue.add(cur_node);     // push back to queue
//	        }else if (cur_node.nR == 0 && cur_node.nL > 0){
//	            if (cur_node.right.nL > 0 && cur_node.right.nR > 0)
//	                cur_node.nR = cur_node.right.nL + cur_node.right.nR;
//	            else
//	                queue.add(cur_node);     // push back to queue
//	        }else{
//	        	queue.add(cur_node);     // push back to queue
//	        }
//		}
//		
//	}
	
	//// use Node.level
	static void compute_nL_nR(Node root_node, HashMap<Integer, Node> node_dict){
		//array of level -> nodes
		ArrayList<ArrayList<Node>> level_array = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < root_node.level+1; i++)
			level_array.add(new ArrayList<Node>());
		
		for (Node u: node_dict.values())
			level_array.get(u.level).add(u);
		
		// bottom-up
		for (Node u : level_array.get(0)){
			u.nL = 1;
			u.nR = 0;
		}
		for (int i = 1; i < root_node.level+1; i++)
			for (Node u : level_array.get(i)){
				u.nL = u.left.nL + u.left.nR;
				u.nR = u.right.nL + u.right.nR;
			}
			
		
	}
	
	
	////
	public static void buildDendrogram(Node[] node_list, HashMap<Integer, Node> node_dict, Node root_node, EdgeIntGraph G){
		compute_nL_nR(root_node, node_dict);
		
	    // compute nEdge
		for (EdgeInt p : G.edges()){
	        int u = p.either();
	        int v = p.other(u);
	//        print u, v
	        // find lowest common ancestor
	        int a_id = lowestCommonAncestor(node_dict.get(u), node_dict.get(v));
	        node_dict.get(a_id).nEdge += 1;
	    }
		
	    // compute value
	    for (Node u : node_dict.values())
	        if (u.id < 0)    // internal nodes
	            u.value = (double)u.nEdge/u.nL/u.nR;
	}
	
	////
	public static void buildDendrogram(Node[] node_list, HashMap<Integer, Node> node_dict, Node root_node, EdgeWeightedGraph G){
		compute_nL_nR(root_node, node_dict);
		
	    // compute nEdge
		for (Edge e : G.edges()){
			int u = e.either();
			int v = e.other(u);
	//        print u, v
	        // find lowest common ancestor
	        int a_id = lowestCommonAncestor(node_dict.get(u), node_dict.get(v));
	        node_dict.get(a_id).nEdge += 1;
	    }
		
	    // compute value
	    for (Node u : node_dict.values())
	        if (u.id < 0)    // internal nodes
	            u.value = (double)u.nEdge/u.nL/u.nR;
	}
	
	////
	public static void buildDendrogram(Node[] node_list, HashMap<Integer, Node> node_dict, Node root_node, UnweightedGraph G){
		compute_nL_nR(root_node, node_dict);
		
	    // compute nEdge
		for (EdgeInt e : G.edges()){
			int u = e.either();
			int v = e.other(u);
	//        print u, v
	        // find lowest common ancestor
	        int a_id = lowestCommonAncestor(node_dict.get(u), node_dict.get(v));
	        node_dict.get(a_id).nEdge += 1;
	    }
		
	    // compute value
	    for (Node u : node_dict.values())
	        if (u.id < 0)    // internal nodes
	            u.value = (double)u.nEdge/u.nL/u.nR;
	}
	
	////
	// Exponential mechanism by MCMC
	// n_samples number of sample T
	static List<Dendrogram> dendrogramFitting(Dendrogram T, EdgeIntGraph G, double eps1, int n_steps, int n_samples, int sample_freq, String node_file) throws IOException{
		List<Dendrogram> list_T = new ArrayList<Dendrogram>(); 	// list of sample T
	    
	    // delta U
	    int n_nodes = G.V();
	    long nMax = 0;
	    if (n_nodes % 2 == 0) 
	        nMax = n_nodes*n_nodes/4;
	    else 
	        nMax = (n_nodes*n_nodes-1)/4; 
	    double dU = Math.log(nMax) + (nMax-1)*Math.log(1+1.0/(nMax-1));
	    System.out.println("dU = " + dU);
	    System.out.println("#steps = " + (n_steps + n_samples*sample_freq));
	    
	    int out_freq = (n_steps + n_samples*sample_freq)/10;
	    
	    // MCMC
	    long start = System.currentTimeMillis();
	    int n_accept = 0;
	    int n_accept_positive = 0;
	    int n_config2 = 0;
	    Random random = new Random();
	    double logLT = T.logLK;
	    double logLT2;
	    int sample = 0;
	    
	    for (int i = 0; i < n_steps + n_samples*sample_freq; i++){
	        // randomly pick an internal node (not root)
	    	Node r_node;
	        while (true){
	            int ind = T.int_nodes[random.nextInt(T.int_nodes.length)];
	            r_node = T.node_dict.get(ind);
	            if (r_node.parent != null)
	                break;
	        }
	        // randomly use config_2(), config_3()
	        int rand_val = random.nextInt(2);
	        if (rand_val == 0){   // config_2()
	            Node p_node = T.config_2(G, r_node);
	            logLT2 = T.logLK;
	            
	            if (logLT2 > logLT){
					n_accept += 1;
					n_accept_positive += 1;
					logLT = logLT2;
				}else{
					double prob = Math.exp(eps1/(2*dU)*(logLT2 - logLT));			// prob < 1.0
					double prob_val = random.nextDouble();
					if (prob_val > prob)
						// reverse
						p_node = T.config_2(G, p_node); // p_node
					else {
						n_accept += 1;
						logLT = logLT2;
					}
				}
				n_config2 += 1;
			} else { // config_3()
				r_node = T.config_3(G, r_node);
				logLT2 = T.logLK;
				
				if (logLT2 > logLT){
					n_accept += 1;
					n_accept_positive += 1;
					logLT = logLT2;
				}else{
					double prob = Math.exp(eps1/(2*dU)*(logLT2 - logLT));			// prob < 1.0
					double prob_val = random.nextDouble();
					if (prob_val > prob)
						// reverse
						r_node = T.config_3(G, r_node); // p_node
					else {
						n_accept += 1;
						logLT = logLT2;
					}
				}
	        }
	        
	        //
	        if (i >= n_steps)
	        	if (i % sample_freq == 0){
	        		T.writeInternalNodes(node_file + "." + sample);
	        		sample ++;
	        	}
	        
	        //
	        if (i % out_freq == 0)
	        	System.out.println("i = " + i + " n_accept = " + n_accept + " logLK = " + T.logLK + " logLK(2) = " + T.logLK()
						+ " n_accept_positive = " + n_accept_positive + " n_config2 = " + n_config2
						+ " time : " + (System.currentTimeMillis() - start));
	        
//	        if (i >= n_steps)
//	            if (i % sample_freq == 0){
//	                Dendrogram T2 = T.copy();
//	                list_T.add(T2);
//	            }
	    }
	    //
	    return list_T;
	}
	
	//// top-down cuts: use queue
	public static List<Map<Integer, Integer>> partitionTopDown(Dendrogram T){
		T.computeTopLevels();
		
		List<Map<Integer, Integer>> part_list = new ArrayList<Map<Integer,Integer>>();
		
		//
		System.out.println("root_node.toplevel = " + T.root_node.toplevel);
		
		int maxToplevel = -1;
		for (Node r : T.node_dict.values())
			if (maxToplevel < r.toplevel)
				maxToplevel = r.toplevel;
		System.out.println("maxToplevel = " + maxToplevel);
		
		int minNodeParentToplevel = maxToplevel;
		for (Node r : T.node_list)
			if (minNodeParentToplevel > r.parent.toplevel)
				minNodeParentToplevel = r.parent.toplevel;
		System.out.println("minNodeParentToplevel = " + minNodeParentToplevel);
		
		// try double the number of clusters per iteration
		int n = T.node_list.length;
		
		for (int i = 1; i < Math.log(n)/Math.log(2); i++){
			
			List<Node> cut_list = new ArrayList<Node>();
			Queue<Node> queue = new LinkedList<Node>();
			queue.add(T.root_node);
			
			int numChildren = 0;		// number of covered children
			List<List<Integer>> children_list = new ArrayList<List<Integer>>();
			
			while (numChildren < n){
				Node cur_node = queue.remove();
				
				// WAY-1
				if (cur_node.left.id >= 0 || cur_node.right.id >= 0){
					cut_list.add(cur_node);
					
					List<Integer> children = findChildren(cur_node);
					children_list.add(children);
					numChildren += children.size();
					
					if (cur_node.left.id < 0)
						queue.add(cur_node.left);
					if (cur_node.right.id < 0)
						queue.add(cur_node.right);
				}
				else if (cur_node.left.toplevel >= i){		
					List<Integer> children = findChildren(cur_node.left);
					children_list.add(children);
					numChildren += children.size();
					
					children = findChildren(cur_node.right);
					children_list.add(children);
					numChildren += children.size();
				}
				else{
					queue.add(cur_node.right);
					queue.add(cur_node.left);
				}
				
				
			}
			//
			Map<Integer, Integer> part = new HashMap<Integer, Integer>();
			int count = 0;
			for (List<Integer> children : children_list){
				for (int u : children)
					part.put(u, count);
				
				count += 1;
			}
			
			//
			part_list.add(part);
		}
		
		//
		return part_list;
	}
	
	////
	public void addLaplaceNoise(double eps2){
    	Queue<Node> queue = new LinkedList<Node>();
        queue.add(this.root_node);
        
        double alpha = Math.exp(-eps2);
        while (queue.size() > 0){
            Node cur_node = queue.remove();
//            cur_node.noisy_nEdge = cur_node.nEdge + DPUtil.laplaceMechanism(eps2);	// LAPLACE
            cur_node.noisy_nEdge = cur_node.nEdge + DPUtil.geometricMechanism(alpha);	// GEOMETRIC
            
            double prob = cur_node.noisy_nEdge/(cur_node.nL*cur_node.nR);
            if (prob < 0.0)
                prob = 0.0;
            else if (prob > 1.0)
                prob = 1.0;
            cur_node.noisy_value = prob;
            
            //
            if (cur_node.left.id < 0)            // only internal nodes
                    queue.add(cur_node.left);
            if (cur_node.right.id < 0)
                queue.add(cur_node.right);
        }
}
	
	//// NOT add noise !! (to test non-noisy HRG)
	public void computeNodeValues(){
	    
    	Queue<Node> queue = new LinkedList<Node>();
        queue.add(this.root_node);
        
        while (queue.size() > 0){
            Node cur_node = queue.remove();
            cur_node.noisy_nEdge = cur_node.nEdge;		// NOT add noise
            cur_node.noisy_value = cur_node.noisy_nEdge/(cur_node.nL*cur_node.nR);
            
            //
            if (cur_node.left.id < 0)            // only internal nodes
                    queue.add(cur_node.left);
            if (cur_node.right.id < 0)
                queue.add(cur_node.right);
        }
	}
	
	////
	// is_noisy = True (MCMCInference), false (MCMCFit)
//	public Grph generateSanitizedSample(int n_nodes, boolean is_noisy) throws Exception{
//	    
//	    Random random = new Random();
//    	this.computeTopLevels();				// computeTopLevels, called in initByInternalNodes()
//    	
//    	Grph aG = new InMemoryGrph();
//	    aG.addNVertices(n_nodes);
//        
//        //
//        for (int u_id = 0; u_id < n_nodes; u_id++){
//            Node u = this.node_list[u_id];
//            for (int v_id = u_id+1; v_id < n_nodes; v_id++){
//                Node v = this.node_list[v_id];
//                int a_id = lowestCommonAncestor(u, v);     // lowest common ancestor
//                Node a_node = this.node_dict.get(a_id);
//                //
//                double rand_val = random.nextDouble();
//                if (is_noisy){
//                	if (rand_val < a_node.noisy_value)		// MCMCInference
//                		aG.addSimpleEdge(u_id, v_id, false);
//                }else{
//                	if (rand_val < a_node.value)			// MCMCFit
//                		aG.addSimpleEdge(u_id, v_id, false);
//                }
//            }
//        }	 
//        return aG;
//	}
	
	//// FASTER !
	// is_noisy = True (MCMCInference), false (MCMCFit)
	public EdgeIntGraph generateSanitizedSample(int n_nodes, boolean is_noisy) throws Exception{
	    
	    Random random = new Random();
//	    this.computeNodeLevels();				// already called in initByInternalNodes()
	    
	    EdgeIntGraph aG = new EdgeIntGraph(n_nodes);
        
	    compute_LS_RS();
	    
	    // scan internal nodes
	    Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.root_node);
		while (queue.size() > 0){
			Node r = queue.remove();
			
			int num_edges = r.nEdge; 
			if (is_noisy)
				num_edges = (int)Math.round(r.noisy_nEdge);
			
			int[] left_nodes = r.LS.toIntArray();
			int[] right_nodes = r.RS.toIntArray();
			int u = 0;
			int v = 0;
			for (int i = 0; i < num_edges; i++){
				u = left_nodes[random.nextInt(left_nodes.length)];
				v = right_nodes[random.nextInt(right_nodes.length)];
				aG.addEdge(new EdgeInt(u, v, 1));
			}
			
			
			//
			if (r.left.id < 0)            // only internal nodes
                queue.add(r.left);
            if (r.right.id < 0)
                queue.add(r.right);    
		}
	    
	    
	    //
        return aG;
	}
	
	////
	public void compute_LS_RS(){
		// 1. build sets (bottom-up), use node.level
	    // array of level -> nodes (see compute_nL_nR)
  		ArrayList<ArrayList<Node>> level_array = new ArrayList<ArrayList<Node>>();
  		for (int i = 0; i < root_node.level+1; i++)
  			level_array.add(new ArrayList<Node>());
  		for (Node u: node_dict.values())
			level_array.get(u.level).add(u);
  		
  		for (Node u : level_array.get(1)){	// parent of leaf nodes
  			u.LS = new IntHashSet();
  			u.LS.add(u.left.id);
  			u.RS = new IntHashSet();
  			u.RS.add(u.right.id);
  			
  			u.e_self = u.nEdge + (u.left.e_self + u.right.e_self);
  		}
  		
	    for (int i = 2; i < this.root_node.level + 1; i++){
	    	for (Node u : level_array.get(i)){
	    		u.LS = new IntHashSet();
	    		u.RS = new IntHashSet();
	    		
	    		if (u.left.id >= 0)
	      			u.LS.add(u.left.id);
	    		else{
	    			u.LS.addAll(u.left.LS);
	    			u.LS.addAll(u.left.RS);
	    		}
	    		
	    		if (u.right.id >= 0)
	      			u.RS.add(u.right.id);
	    		else{
	    			u.RS.addAll(u.right.LS);
	    			u.RS.addAll(u.right.RS);
	    		}
	    		
	    		u.e_self = u.nEdge + (u.left.e_self + u.right.e_self);
	    	}
	    }
	}
	
	////
	static int inOrderPrint(Node root_node, boolean nLR_on, boolean value_on){
		int nLeft = 0;
		int nRight = 0;
		if (root_node.left != null)
			nLeft = inOrderPrint(root_node.left, nLR_on, value_on); 
		
		System.out.print(root_node.id);
		
		if (nLR_on)
	        System.out.print("\tnL = " + root_node.nL + " nR = " + root_node.nR + " nEdge = " + root_node.nEdge);
	    if (value_on)
	    	System.out.print(" value = " + root_node.value);
	    System.out.println();
	    
		if (root_node.right != null)
			nRight = inOrderPrint(root_node.right, nLR_on, value_on);
		//
		return (nLeft + nRight + 1);
	}
	
	////
	// leaf nodes' level = 0, int_node = max(left,right) + 1
	public void computeNodeLevels(){
		// WAY-1
//	    for (Node u : node_list)
//	        u.level = 0;
//	    int count = 0; 
//	    while (true){
//	        // find nodes having leveled children
//	        boolean found = false;
//	        for (int u_id : int_nodes){
//	            Node u = node_dict.get(u_id);
//	            if (u.level == -1)
//	                if (u.left.level != -1 && u.right.level != -1){
//	                    u.level = (u.left.level > u.right.level? u.left.level : u.right.level) + 1;
//	                    found = true;
//	                    break;
//	                }
//	        }
//	        if (!found)
//	            break;
//	    }
	    
		// WAY-2
//		if  (this.node_list[0].toplevel == 0)	// commented to force recomputation !
		this.computeTopLevels();
		
		int max_toplevel = 0;
		HashMap<Integer, ArrayList<Node>> toplevel_nodes = new HashMap<Integer, ArrayList<Node>>();
		for (Node u : this.node_dict.values()){
			if (u.id >= 0)
				continue;
			if (!toplevel_nodes.containsKey(u.toplevel)){
				toplevel_nodes.put(u.toplevel, new ArrayList<Node>());
				toplevel_nodes.get(u.toplevel).add(u);
			}else
				toplevel_nodes.get(u.toplevel).add(u);
			if (max_toplevel < u.toplevel)
                max_toplevel = u.toplevel;
		}
		
		// init leaf nodes
		for (Node u : node_list)
	        u.level = 0;
		for (int i = max_toplevel; i >= 0; i--){
			for (Node u : toplevel_nodes.get(i)){
//                if (u.level != -1)	// commented to force recomputation !
//                    continue;
                if (u.left.level == -1 && u.right.level == -1)
                    System.out.println("level ERROR at u.id = " + u.id + " u.toplevel = " + u.toplevel);
                
                u.level = (u.left.level > u.right.level ? u.left.level : u.right.level) + 1;
			}
		}
					
		
	}
	
	// root_node's level = 0, int_node = parent + 1
	public void computeTopLevels(){
		this.root_node.toplevel = 0;
		
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(this.root_node);
	    while (queue.size() > 0){
	    	Node cur_node = queue.remove();
	    	if (cur_node.left != null){
	    		cur_node.left.toplevel = cur_node.toplevel + 1;
	    		cur_node.right.toplevel = cur_node.toplevel + 1;
	    		queue.add(cur_node.left);
	    		queue.add(cur_node.right);
	    	}
	    }
	}
	
	////
	static void writeInternalNodes(List<Dendrogram> list_T, String node_file) throws Exception{
	    
		int i = 0;
	    for (Dendrogram T : list_T){
	    	T.writeInternalNodes(node_file + "." + i);
	        i++;
	    }
	}
	
	////
	// list_T: list of new Dendrogram()
	static void readInternalNodes(EdgeIntGraph G, List<Dendrogram> list_T, String node_file, int n_samples) throws Exception{
		int i = 0;
	    for (Dendrogram T : list_T){
	    	String filename = node_file + "." + i;
	        i++;
			T.readInternalNodes(G, filename);
	    }
	}
	
	////
	static void printDendrogram(Node root ){
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(root);
		while (queue.size() > 0){
			Node R = queue.remove();
			System.out.print(R.id + "(" + R.nEdge + "," + String.format("%.4f", R.value) + "):" + R.left.id + "," + R.right.id + " :: ");
			System.out.print(R.LS);
			System.out.print(R.RS);
			System.out.println();
			
			if (R.left.id < 0)
				queue.add(R.left);
			if (R.right.id < 0)
				queue.add(R.right);
			
		}
	}
	
	
	////used in TreeCutter.cutTreeHRGFixed()
	// used with NodeSetLouvain.bestCutHRG()
	public static NodeSetLouvain readTree(EdgeIntGraph G, String filename) throws IOException{
		
		// read dendrogram
		Dendrogram T = new Dendrogram();
		T.readInternalNodes(G, filename);
		
		T.compute_LS_RS();
		// debug
		System.out.println("T.root_node.id = " + T.root_node.id);
		
		
		// create the corresponding NodeSetLouvain
		int k = 2;
		NodeSetLouvain root = new NodeSetLouvain(k);
		
		Queue<Node> queue = new LinkedList<Node>();
		Queue<NodeSetLouvain> queue_set = new LinkedList<NodeSetLouvain>();
		queue.add(T.root_node);
		queue_set.add(root);
		while (queue.size() > 0){
			Node R = queue.remove();
			NodeSetLouvain NS = queue_set.remove();
			NS.id = R.id;
			
//			int n = R.nL + R.nR;
//			NS.part = new int[n];
			NS.size = new int[k];
			NS.dc = new int[k];
			NS.size[0] = R.nL; NS.size[1] = R.nR;
			// store child leaves, used in NodeSetLouvain.writeBestCutHRG()
			NS.ind2node = new int[R.nL + R.nR];
			int i = 0;
			for (IntCursor s : R.LS)
				NS.ind2node[i++] = s.value;
			for (IntCursor t : R.RS)
				NS.ind2node[i++] = t.value;
			
			//
			for (IntCursor s : R.LS)
				NS.dc[0] += G.degree(s.value);
			for (IntCursor t : R.RS)
				NS.dc[1] += G.degree(t.value);
			
			// R.nEdge computed in compute_LS_RS()
			NS.e_self = R.e_self;
			NS.nEdge = R.nEdge;
			
			//
			if (R.left.id < 0){
				queue.add(R.left);
				
				NodeSetLouvain child = new NodeSetLouvain(k);
				child.level = NS.level + 1;			// top-down
				NS.children[0] = child;
				queue_set.add(child);
				if (child.level > root.max_level)
					root.max_level = child.level;
			}
			
			if (R.right.id < 0){
				queue.add(R.right);
				
				NodeSetLouvain child = new NodeSetLouvain(k);
				child.level = NS.level + 1;			// top-down
				NS.children[1] = child;
				queue_set.add(child);
				if (child.level > root.max_level)
					root.max_level = child.level;
			}
			
		}
		
		
		//
		return root;
	}
}

