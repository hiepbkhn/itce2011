/*
 * Sep 21, 2015
 * 	- copied from EdgeWeightedGraph, use EdgeShort
 * Nov 18
 * 	- readEdgeListWithNodes()
 * Dec 2
 * 	- writeGraph(), writeGraphWithWeights()
 * May 26, 2016
 * 	- allEdges()
 * Jun 8
 * 	- subGraph()
 * Jun 9
 * 	- components(), subGraphD2()
 */

package algs4;

import hist.Short2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/******************************************************************************
 *  Compilation:  javac EdgeWeightedGraph.java
 *  Execution:    java EdgeWeightedGraph filename.txt
 *  Dependencies: Bag.java Edge.java In.java StdOut.java
 *  Data files:   http://algs4.cs.princeton.edu/43mst/tinyEWG.txt
 *
 *  An edge-weighted undirected graph, implemented using adjacency lists.
 *  Parallel edges and self-loops are permitted.
 *
 *  % java EdgeWeightedGraph tinyEWG.txt 
 *  8 16
 *  0: 6-0 0.58000  0-2 0.26000  0-4 0.38000  0-7 0.16000  
 *  1: 1-3 0.29000  1-2 0.36000  1-7 0.19000  1-5 0.32000  
 *  2: 6-2 0.40000  2-7 0.34000  1-2 0.36000  0-2 0.26000  2-3 0.17000  
 *  3: 3-6 0.52000  1-3 0.29000  2-3 0.17000  
 *  4: 6-4 0.93000  0-4 0.38000  4-7 0.37000  4-5 0.35000  
 *  5: 1-5 0.32000  5-7 0.28000  4-5 0.35000  
 *  6: 6-4 0.93000  6-0 0.58000  3-6 0.52000  6-2 0.40000
 *  7: 2-7 0.34000  1-7 0.19000  0-7 0.16000  5-7 0.28000  4-7 0.37000
 *
 ******************************************************************************/
import java.util.Queue;

/**
 *  The <tt>EdgeWeightedGraph</tt> class represents an edge-weighted
 *  graph of vertices named 0 through <em>V</em> - 1, where each
 *  undirected edge is of type {@link Edge} and has a real-valued weight.
 *  It supports the following two primary operations: add an edge to the graph,
 *  iterate over all of the edges incident to a vertex. It also provides
 *  methods for returning the number of vertices <em>V</em> and the number
 *  of edges <em>E</em>. Parallel edges and self-loops are permitted.
 *  <p>
 *  This implementation uses an adjacency-lists representation, which 
 *  is a vertex-indexed array of @link{Bag} objects.
 *  All operations take constant time (in the worst case) except
 *  iterating over the edges incident to a given vertex, which takes
 *  time proportional to the number of such edges.
 *  <p>
 *  For additional documentation,
 *  see <a href="http://algs4.cs.princeton.edu/43mst">Section 4.3</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class EdgeShortGraph {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final short V;
    private short E;
    private List<Map<Short, EdgeShort>> adj;
    
    /**
     * Initializes an empty edge-weighted graph with <tt>V</tt> vertices and 0 edges.
     *
     * @param  V the number of vertices
     * @throws IllegalArgumentException if <tt>V</tt> < 0
     */
    public EdgeShortGraph(short V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = new ArrayList<Map<Short,EdgeShort>>();
        for (short v = 0; v < V; v++) {
            adj.add(new HashMap<Short, EdgeShort>());
        }
    }

    /**
     * Initializes a random edge-weighted graph with <tt>V</tt> vertices and <em>E</em> edges.
     *
     * @param  V the number of vertices
     * @param  E the number of edges
     * @throws IllegalArgumentException if <tt>V</tt> < 0
     * @throws IllegalArgumentException if <tt>E</tt> < 0
     */
//    public EdgeShortGraph(short V, short E) {
//        this(V);
//        if (E < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
//        for (short i = 0; i < E; i++) {
//            short v = StdRandom.uniform(V);
//            short w = StdRandom.uniform(V);
//            short weight = (short)Math.round(100 * StdRandom.uniform()) / 100.0;
//            EdgeShort e = new EdgeShort(v, w, weight);
//            addEdge(e);
//        }
//    }

    /**  
     * Initializes an edge-weighted graph from an input stream.
     * The format is the number of vertices <em>V</em>,
     * followed by the number of edges <em>E</em>,
     * followed by <em>E</em> pairs of vertices and edge weights,
     * with each entry separated by whitespace.
     *
     * @param  in the input stream
     * @throws IndexOutOfBoundsException if the endposhorts of any edge are not in prescribed range
     * @throws IllegalArgumentException if the number of vertices or edges is negative
     */
    public EdgeShortGraph(In in) {
        this(in.readShort());
        short E = in.readShort();
        if (E < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
        for (short i = 0; i < E; i++) {
            short v = in.readShort();
            short w = in.readShort();
            short weight = in.readShort();
            EdgeShort e = new EdgeShort(v, w, weight);
            addEdge(e);
        }
    }

    /**
     * Initializes a new edge-weighted graph that is a deep copy of <tt>G</tt>.
     *
     * @param  G the edge-weighted graph to copy
     */
    public EdgeShortGraph(EdgeShortGraph G) {
        this(G.V());
        this.E = G.E();
        for (short v = 0; v < G.V(); v++) {
            // reverse so that adjacency list is in same order as original
            Stack<EdgeShort> reverse = new Stack<EdgeShort>();
            for (EdgeShort e : G.adj.get(v).values()) {
                reverse.push(e);
            }
            for (EdgeShort e : reverse) {
                adj.get(v).put(e.other(v), e);
            }
        }
    }


    /**
     * Returns the number of vertices in this edge-weighted graph.
     *
     * @return the number of vertices in this edge-weighted graph
     */
    public short V() {
        return V;
    }

    /**
     * Returns the number of edges in this edge-weighted graph.
     *
     * @return the number of edges in this edge-weighted graph
     */
    public short E() {
        return E;
    }

    // throw an IndexOutOfBoundsException unless 0 <= v < V
    private void validateVertex(short v) {
        if (v < 0 || v >= V)
            throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (V-1));
    }

    /**
     * Adds the undirected edge <tt>e</tt> to this edge-weighted graph.
     *
     * @param  e the edge
     * @throws IndexOutOfBoundsException unless both endposhorts are between 0 and V-1
     */
    public void addEdge(EdgeShort e) {
        short v = e.either();
        short w = e.other(v);
        validateVertex(v);
        validateVertex(w);
        if (v != w){		// hiepnh fixed
        	adj.get(v).put(w, e);
        	adj.get(w).put(v, e);
        }else
        	adj.get(v).put(v, e);
        E++;
    }

    /**
     * Returns the edges incident on vertex <tt>v</tt>.
     *
     * @param  v the vertex
     * @return the edges incident on vertex <tt>v</tt> as an Iterable
     * @throws IndexOutOfBoundsException unless 0 <= v < V
     */
    public Map<Short, EdgeShort> adj(short v) {
        validateVertex(v);
        return adj.get(v);
    }

    /**
     * Returns the degree of vertex <tt>v</tt>.
     *
     * @param  v the vertex
     * @return the degree of vertex <tt>v</tt>               
     * @throws IndexOutOfBoundsException unless 0 <= v < V
     */
    public short degree(short v) {
        validateVertex(v);
        return (short)adj.get(v).size();
    }

    /**
     * Returns all edges in this edge-weighted graph.
     * To iterate over the edges in this edge-weighted graph, use foreach notation:
     * <tt>for (Edge e : G.edges())</tt>.
     *
     * @return all edges in this edge-weighted graph, as an iterable
     */
    public Iterable<EdgeShort> edges() {
        Bag<EdgeShort> list = new Bag<EdgeShort>();
        for (short v = 0; v < V; v++) {
            short selfLoops = 0;
            for (EdgeShort e : adj.get(v).values()) {
                if (e.other(v) > v) {
                    list.add(e);
                }
                // only add one copy of each self loop (self loops will be consecutive)
                else if (e.other(v) == v) {
                    if (selfLoops % 2 == 0) list.add(e);
                    selfLoops++;
                }
            }
        }
        return list;
    }
    
    ////
    public List<Short2> allEdges() {
    	List<Short2> ret = new ArrayList<Short2>();
    	for (short v = 0; v < V; v++) {
            short selfLoops = 0;
            for (EdgeShort e : adj.get(v).values()) {
            	short u = e.other(v);
                if (u > v) 
                    ret.add(new Short2(v, e.other(v)));
            }
    	}
    	return ret;
    }

    /**
     * Returns a string representation of the edge-weighted graph.
     * This method takes time proportional to <em>E</em> + <em>V</em>.
     *
     * @return the number of vertices <em>V</em>, followed by the number of edges <em>E</em>,
     *         followed by the <em>V</em> adjacency lists of edges
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + " " + totalWeight() + NEWLINE);
        for (short v = 0; v < V; v++) {
            s.append(v + ": ");
            for (EdgeShort e : adj.get(v).values()) {
                s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    ////////////////////////////////////////
    // hiepnh - Sep 14, 2015
    public short totalWeight(){
    	short ret = 0;
    	for (EdgeShort e : edges())
    		ret += e.weight();
    	//
    	return ret;
    }
    
    public short adjWeight(short v){
    	short ret = 0;
    	for (EdgeShort e : adj.get(v).values()) {
    		if (e.other(v) != v)
    			ret += e.weight();
    		else
    			ret += e.weight() * 2;		// IMPORTANT
    	}
    	//
    	return ret;
    }
    
    public boolean areEdgesAdjacent(short u, short v){
    	return adj.get(u).containsKey(v);
    }
    
    public EdgeShort getEdge(short u, short v){
    	if (adj.get(u).containsKey(v))
    		return adj.get(u).get(v); 
    	return null;
    }
    
    public EdgeShortGraph clone(){
    	EdgeShortGraph ret = new EdgeShortGraph(this.V());
    	ret.E = this.E;
    	for (EdgeShort e : this.edges())
    		ret.addEdge(e);
    	//
    	return ret;
    }
    
    public static EdgeShortGraph readEdgeList(String filename, String split_char) throws IOException{

    	short maxNodeId = -1;
    	List<Short2> edgeList = new ArrayList<Short2>();
    	
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(split_char);
        	short u = Short.parseShort(items[0]);
        	short v = Short.parseShort(items[1]);
        	if (maxNodeId < u)
        		maxNodeId = u;
        	if (maxNodeId < v)
        		maxNodeId = v;
        	edgeList.add(new Short2(u, v));
    	}
    	
    	br.close();
    	
    	EdgeShortGraph ret = new EdgeShortGraph((short)(maxNodeId + 1));
    	for (Short2 e : edgeList)
    		ret.addEdge(new EdgeShort(e.val0, e.val1, (short)1));
    	
    	return ret;
    }
    
    //// 
    public static EdgeShortGraph readEdgeListWithNodes(String filename, String split_char, short n_nodes) throws IOException{

    	List<Short2> edgeList = new ArrayList<Short2>();
    	
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(split_char);
        	short u = Short.parseShort(items[0]);
        	short v = Short.parseShort(items[1]);
        	edgeList.add(new Short2(u, v));
    	}
    	
    	br.close();
    	
    	EdgeShortGraph ret = new EdgeShortGraph(n_nodes);
    	for (Short2 e : edgeList)
    		ret.addEdge(new EdgeShort(e.val0, e.val1, (short)1));
    	
    	return ret;
    }
    
    ////
    public static void writeGraph(EdgeShortGraph G, String filename) throws IOException{
    	
    	BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    	for (EdgeShort e : G.edges()){
    		short u = e.either();
    		short v = e.other(u);
    		bw.write(u + "\t" + v + "\n");
    	}
    	
    	bw.close();
    }
    
    ////
    public static void writeGraphWithWeights(EdgeShortGraph G, String filename) throws IOException{
    	
    	BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    	for (EdgeShort e : G.edges()){
    		short u = e.either();
    		short v = e.other(u);
    		bw.write(u + "\t" + v + "\t" + e.weight() + "\n");
    	}
    	
    	bw.close();
    }
    
    //// return aG and nodeMap
    public static EdgeShortGraph subGraph(EdgeShortGraph G, List<Short> nodes, Map<Short, Short> nodeMap){
    	short n = (short)nodes.size();

    	short i = 0;
    	for (short u : nodes){
    		nodeMap.put(u, i);
    		i++;
    	}
    	
    	EdgeShortGraph aG = new EdgeShortGraph(n);
    	for (Short2 e : G.allEdges()){
    		short u = e.val0;
    		short v = e.val1;
    		if (nodeMap.containsKey(u) && nodeMap.containsKey(v)){
    			aG.addEdge(new EdgeShort(nodeMap.get(u), nodeMap.get(v), (short)1));
    		}
    	}
    	
    	//
    	return aG;
    }
    
    //// subgraph at distance-2: (u,v) connected if d(u,v) = 2
    // 	 return aG and nodeMap
    public static EdgeShortGraph subGraphD2(EdgeShortGraph G, List<Short> nodes, Map<Short, Short> nodeMap){
	   	short n = (short)nodes.size();
	
	   	short i = 0;
	   	for (short u : nodes){
	   		nodeMap.put(u, i);
	   		i++;
	   	}
	   	
	   	EdgeShortGraph aG = new EdgeShortGraph(n);
	   	for (Short2 e : G.allEdges()){
	   		short u = e.val0;
	   		short v = e.val1;
	   		if (nodeMap.containsKey(u)){
	   			for (short w : G.adj(v).keySet())
	   				if (w > u && nodeMap.containsKey(w))
	   					aG.addEdge(new EdgeShort(nodeMap.get(u), nodeMap.get(w), (short)1));
	   		}
	   		
	   		if (nodeMap.containsKey(v)){
	   			for (short w : G.adj(u).keySet())
	   				if (w > v && nodeMap.containsKey(w))
	   					aG.addEdge(new EdgeShort(nodeMap.get(v), nodeMap.get(w), (short)1));
	   		}
	   	}
	   	
	   	//
	   	return aG;
    }
    
    //// connected components
    public static short[] components(EdgeShortGraph G){
    	short n = G.V();
    	short[] ret = new short[n];

    	for (short u = 0; u < n; u++)
    		ret[u] = -1;
    	// BFS
    	short comp = 0;
    	short cur = 0;
    	while (true){
    		// find next cur
    		while (cur < n && ret[cur] != -1)
    			cur += 1;
    		if (cur == n)
    			break;
    		
    		//
    		ret[cur] = comp;
    		Queue<Short> queue = new LinkedList<Short>();
    		queue.add(cur);
    		while(queue.size() > 0){
    			short top = queue.remove();
    			for (short u : G.adj(top).keySet())
    				if (ret[u] == -1){
    					ret[u] = comp;
    					queue.add(u);
    				}
    		}
    		
    		comp += 1;
    			
    	}
    	
    	//
    	return ret;
    }
    
    
    ////////////////////////////////////////////////
    public static void main(String[] args) {

    	EdgeShortGraph G = new EdgeShortGraph((short)5);
    	
    	G.addEdge(new EdgeShort((short)0,(short)1,(short)1));
    	G.addEdge(new EdgeShort((short)2,(short)3,(short)1));
    	G.addEdge(new EdgeShort((short)2,(short)4,(short)1));
    	System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
    	
    	short[] comp = EdgeShortGraph.components(G);
    	for (short u = 0; u < G.V(); u++)
    		System.out.print(comp[u] + " ");
    }

}
