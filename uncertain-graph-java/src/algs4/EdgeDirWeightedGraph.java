/*
 * Oct 27, 2015
 * 	- directed, weighted
 * 	- copied from EdgeWeightedGraph
 */

package algs4;

import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EdgeDirWeightedGraph {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final int V;
    private int E;
    private List<Map<Integer, Edge>> adjOut;
    private List<Map<Integer, Edge>> adjIn;
    
    /**
     * Initializes an empty edge-weighted graph with <tt>V</tt> vertices and 0 edges.
     *
     * @param  V the number of vertices
     * @throws IllegalArgumentException if <tt>V</tt> < 0
     */
    public EdgeDirWeightedGraph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adjOut = new ArrayList<Map<Integer,Edge>>();
        adjIn = new ArrayList<Map<Integer,Edge>>();
        for (int v = 0; v < V; v++) {
            adjOut.add(new HashMap<Integer, Edge>());
            adjIn.add(new HashMap<Integer, Edge>());
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
    public EdgeDirWeightedGraph(int V, int E) {
        this(V);
        if (E < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
        for (int i = 0; i < E; i++) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            double weight = Math.round(100 * StdRandom.uniform()) / 100.0;
            Edge e = new Edge(v, w, weight);
            addEdge(e);
        }
    }

    /**  
     * Initializes an edge-weighted graph from an input stream.
     * The format is the number of vertices <em>V</em>,
     * followed by the number of edges <em>E</em>,
     * followed by <em>E</em> pairs of vertices and edge weights,
     * with each entry separated by whitespace.
     *
     * @param  in the input stream
     * @throws IndexOutOfBoundsException if the endpoints of any edge are not in prescribed range
     * @throws IllegalArgumentException if the number of vertices or edges is negative
     */
    public EdgeDirWeightedGraph(In in) {
        this(in.readInt());
        int E = in.readInt();
        if (E < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
        for (int i = 0; i < E; i++) {
            int v = in.readInt();
            int w = in.readInt();
            double weight = in.readDouble();
            Edge e = new Edge(v, w, weight);
            addEdge(e);
        }
    }

//    /**
//     * Initializes a new edge-weighted graph that is a deep copy of <tt>G</tt>.
//     *
//     * @param  G the edge-weighted graph to copy
//     */
//    public EdgeDirWeightedGraph(EdgeDirWeightedGraph G) {
//        this(G.V());
//        this.E = G.E();
//        for (int v = 0; v < G.V(); v++) {
//            // reverse so that adjacency list is in same order as original
//            Stack<Edge> reverse = new Stack<Edge>();
//            for (Edge e : G.adjOut.get(v).values()) {
//                reverse.push(e);
//            }
//            for (Edge e : reverse) {
//                adjOut.get(v).put(e.other(v), e);
//            }
//        }
//    }


    /**
     * Returns the number of vertices in this edge-weighted graph.
     *
     * @return the number of vertices in this edge-weighted graph
     */
    public int V() {
        return V;
    }

    /**
     * Returns the number of edges in this edge-weighted graph.
     *
     * @return the number of edges in this edge-weighted graph
     */
    public int E() {
        return E;
    }

    // throw an IndexOutOfBoundsException unless 0 <= v < V
    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (V-1));
    }

    /**
     * Adds the undirected edge <tt>e</tt> to this edge-weighted graph.
     *
     * @param  e the edge
     * @throws IndexOutOfBoundsException unless both endpoints are between 0 and V-1
     */
    public void addEdge(Edge e) {
        int v = e.either();
        int w = e.other(v);
        validateVertex(v);
        validateVertex(w);
        adjOut.get(v).put(v, e);
        adjIn.get(w).put(w, e);
        E++;
    }

    /**
     * Returns the edges incident on vertex <tt>v</tt>.
     *
     * @param  v the vertex
     * @return the edges incident on vertex <tt>v</tt> as an Iterable
     * @throws IndexOutOfBoundsException unless 0 <= v < V
     */
    public Map<Integer, Edge> adj(int v) {
        validateVertex(v);
        return adjOut.get(v);
    }

    /**
     * Returns the degree of vertex <tt>v</tt>.
     *
     * @param  v the vertex
     * @return the degree of vertex <tt>v</tt>               
     * @throws IndexOutOfBoundsException unless 0 <= v < V
     */
    public int degreeOut(int v) {
        validateVertex(v);
        return adjOut.get(v).size();
    }

    public int degreeIn(int v) {
        validateVertex(v);
        return adjIn.get(v).size();
    }
    
    /**
     * Returns all edges in this edge-weighted graph.
     * To iterate over the edges in this edge-weighted graph, use foreach notation:
     * <tt>for (Edge e : G.edges())</tt>.
     *
     * @return all edges in this edge-weighted graph, as an iterable
     */
    public Iterable<Edge> edges() {
        Bag<Edge> list = new Bag<Edge>();
        for (int v = 0; v < V; v++) {
            int selfLoops = 0;
            for (Edge e : adjOut.get(v).values()) {
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
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (Edge e : adjOut.get(v).values()) {
                s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    ////////////////////////////////////////
    // hiepnh - Sep 14, 2015
    public double totalWeight(){
    	double ret = 0.0;
    	for (Edge e : edges())
    		ret += e.weight();
    	//
    	return ret;
    }
    
    public double adjWeight(int v){
    	double ret = 0.0;
    	for (Edge e : adjOut.get(v).values()) {
    		if (e.other(v) != v)
    			ret += e.weight();
    		else
    			ret += e.weight() * 2;		// IMPORTANT
    	}
    	//
    	return ret;
    }
    
    public boolean areEdgesAdjacent(int u, int v){
    	return adjOut.get(u).containsKey(v);
    }
    
    public Edge getEdge(int u, int v){
    	if (adjOut.get(u).containsKey(v))
    		return adjOut.get(u).get(v); 
    	return null;
    }
    
    public EdgeDirWeightedGraph clone(){
    	EdgeDirWeightedGraph ret = new EdgeDirWeightedGraph(this.V());
    	ret.E = 0;
    	for (Edge e : this.edges())
    		ret.addEdge(e);
    	//
    	return ret;
    }
    
    // Oct 26, 2015
    public static EdgeDirWeightedGraph readEdgeList(String filename, int n_nodes, String delimiter) throws IOException{

    	int maxNodeId = -1;
    	List<Int2> edgeList = new ArrayList<Int2>();
    	
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(delimiter);
        	int u = Integer.parseInt(items[0]);
        	int v = Integer.parseInt(items[1]);
        	if (maxNodeId < u)
        		maxNodeId = u;
        	if (maxNodeId < v)
        		maxNodeId = v;
        	edgeList.add(new Int2(u, v));
    	}
    	
    	br.close();
    	
    	EdgeDirWeightedGraph ret = new EdgeDirWeightedGraph(n_nodes);
    	for (Int2 e : edgeList)
    		ret.addEdge(new Edge(e.val0, e.val1, 1.0));
    	
    	return ret;
    }
    
    // Oct 16, 2015
    public static EdgeDirWeightedGraph readEdgeListAndWeight(String filename, int n_nodes, String delimiter) throws IOException{

    	EdgeDirWeightedGraph ret = new EdgeDirWeightedGraph(n_nodes);
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(delimiter);
        	int u = Integer.parseInt(items[0]);
        	int v = Integer.parseInt(items[1]);
        	double weight = Double.parseDouble(items[2]);
        	ret.addEdge(new Edge(u, v, weight));
    	}
    	
    	br.close();
    	
    	return ret;
    }
    
    public static void writeGraph(EdgeDirWeightedGraph G, String filename, String delimiter) throws IOException{
    	
    	BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    	for (Edge e : G.edges()){
    		int u = e.either();
    		int v = e.other(u);
    		bw.write(u + delimiter + v + delimiter + e.weight() + "\n");
    	}
    	
    	bw.close();
    }
    
    /**
     * Unit tests the <tt>EdgeWeightedGraph</tt> data type.
     */
    public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeDirWeightedGraph G = new EdgeDirWeightedGraph(in);
        StdOut.println(G);
        
        System.out.println("areEdgesAdjacent = " + G.areEdgesAdjacent(0, 5));
    }

}
