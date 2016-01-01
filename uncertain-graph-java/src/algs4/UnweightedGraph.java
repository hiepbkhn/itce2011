/*
 * Sep 21, 2015
 * 	- copied from EdgeWeightedGraph, use EdgeInt
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

public class UnweightedGraph {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final int V;
    private int E;
    private List<List<Integer>> adj;
    
    /**
     * Initializes an empty edge-weighted graph with <tt>V</tt> vertices and 0 edges.
     *
     * @param  V the number of vertices
     * @throws IllegalArgumentException if <tt>V</tt> < 0
     */
    public UnweightedGraph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = new ArrayList<List<Integer>>();
        for (int v = 0; v < V; v++) {
            adj.add(new ArrayList<Integer>());
        }
    }


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
    public void addEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        if (v != w){		// hiepnh fixed
        	adj.get(v).add(w);
        	adj.get(w).add(v);
        }else
        	adj.get(v).add(v);
        E++;
    }

    /**
     * Returns the edges incident on vertex <tt>v</tt>.
     *
     * @param  v the vertex
     * @return the edges incident on vertex <tt>v</tt> as an Iterable
     * @throws IndexOutOfBoundsException unless 0 <= v < V
     */
    public List<Integer> adj(int v) {
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
    public int degree(int v) {
        validateVertex(v);
        return adj.get(v).size();
    }

    /**
     * Returns all edges in this edge-weighted graph.
     * To iterate over the edges in this edge-weighted graph, use foreach notation:
     * <tt>for (Edge e : G.edges())</tt>.
     *
     * @return all edges in this edge-weighted graph, as an iterable
     */
    public Iterable<EdgeInt> edges() {
        Bag<EdgeInt> list = new Bag<EdgeInt>();
        for (int v = 0; v < V; v++) {
            for (int w : adj.get(v)) {
            	if (w > v)
            		list.add(new EdgeInt(v, w, 1));
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
            for (int w : adj.get(v)) {
                s.append(w + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    ////////////////////////////////////////
    // hiepnh - Sep 14, 2015
    public int totalWeight(){
    	int ret = 0;
    	for (EdgeInt e : edges())
    		ret += e.weight();
    	//
    	return ret;
    }
    
    
    public static UnweightedGraph readEdgeList(String filename, String split_char) throws IOException{

    	int maxNodeId = -1;
    	List<Int2> edgeList = new ArrayList<Int2>();
    	
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(split_char);
        	int u = Integer.parseInt(items[0]);
        	int v = Integer.parseInt(items[1]);
        	if (maxNodeId < u)
        		maxNodeId = u;
        	if (maxNodeId < v)
        		maxNodeId = v;
        	edgeList.add(new Int2(u, v));
    	}
    	
    	br.close();
    	
    	UnweightedGraph ret = new UnweightedGraph(maxNodeId + 1);
    	for (Int2 e : edgeList)
    		ret.addEdge(e.val0, e.val1);
    	
    	return ret;
    }
    
    public static UnweightedGraph readEdgeListWithNodes(String filename, String split_char, int n_nodes) throws IOException{

    	List<Int2> edgeList = new ArrayList<Int2>();
    	
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(split_char);
        	int u = Integer.parseInt(items[0]);
        	int v = Integer.parseInt(items[1]);
        	edgeList.add(new Int2(u, v));
    	}
    	
    	br.close();
    	
    	UnweightedGraph ret = new UnweightedGraph(n_nodes);
    	for (Int2 e : edgeList)
    		ret.addEdge(e.val0, e.val1);
    	
    	return ret;
    }
    
    ////
    public static void writeGraph(UnweightedGraph G, String filename) throws IOException{
    	
    	BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    	for (int v = 0; v < G.V(); v++)
            for (int w : G.adj.get(v)){
            	if (w > v)
            		bw.write(v + "\t" + w + "\n");
            }
    	
    	bw.close();
    }
    
    /**
     * Unit tests the <tt>EdgeWeightedGraph</tt> data type.
     */
    public static void main(String[] args) {
    	
    	
    }

}
