/*************************************************************************
 *  Compilation:  javac Edge.java
 *  Execution:    java Edge
 *
 *  Immutable weighted edge.
 *
 *************************************************************************/

package graph;

/**
 *  The <tt>Edge</tt> class represents a weighted edge in an 
 *  {@link EdgeWeightedGraph}. Each edge consists of two integers
 *  (naming the two vertices) and a real-value weight. The data type
 *  provides methods for accessing the two endpoints of the edge and
 *  the weight. The natural order for this data type is by
 *  ascending order of weight.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/43mst">Section 4.3</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class WeightedEdge implements Comparable<WeightedEdge> { 

    private final int v;
    private final int w;
//    private final double weight;
    private double weight;	// hiepnh Sep 14, 2015

    /**
     * Initializes an edge between vertices <tt>v/tt> and <tt>w</tt> of
     * the given <tt>weight</tt>.
     * param v one vertex
     * param w the other vertex
     * param weight the weight of the edge
     * @throws IndexOutOfBoundsException if either <tt>v</tt> or <tt>w</tt> 
     *    is a negative integer
     * @throws IllegalArgumentException if <tt>weight</tt> is <tt>NaN</tt>
     */
    public WeightedEdge(int v, int w, double weight) {
        if (v < 0) throw new IndexOutOfBoundsException("Vertex name must be a nonnegative integer");
        if (w < 0) throw new IndexOutOfBoundsException("Vertex name must be a nonnegative integer");
        if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
        this.v = v;
        this.w = w;
        this.weight = weight;
    }

    /**
     * Returns the weight of the edge.
     * @return the weight of the edge
     */
    public double weight() {
        return weight;
    }

    /**
     * Returns either endpoint of the edge.
     * @return either endpoint of the edge
     */
    public int either() {
        return v;
    }

    public int from() {
        return v;
    }
    
    public int to() {
        return w;
    }
    
    /**
     * Returns the endpoint of the edge that is different from the given vertex
     * (unless the edge represents a self-loop in which case it returns the same vertex).
     * @param vertex one endpoint of the edge
     * @return the endpoint of the edge that is different from the given vertex
     *   (unless the edge represents a self-loop in which case it returns the same vertex)
     * @throws java.lang.IllegalArgumentException if the vertex is not one of the endpoints
     *   of the edge
     */
    public int other(int vertex) {
        if      (vertex == v) return w;
        else if (vertex == w) return v;
        else throw new IllegalArgumentException("Illegal endpoint");
    }

    /**
     * Compares two edges by weight.
     * @param that the other edge
     * @return a negative integer, zero, or positive integer depending on whether
     *    this edge is less than, equal to, or greater than that edge
     */
    public int compareTo(WeightedEdge that) {
        if      (this.weight() < that.weight()) return -1;
        else if (this.weight() > that.weight()) return +1;
        else                                    return  0;
    }

    /**
     * Returns a string representation of the edge.
     * @return a string representation of the edge
     */
    public String toString() {
        return String.format("%d-%d %.5f", v, w, weight);
    }

	////////////////////////////////////////
	// hiepnh - Sep 14, 2015
    public void setWeight(double new_weight){
    	this.weight = new_weight;
    }
    
    // hiepnh - Aug 12, 2016
    public void incWeight(double value){
    	this.weight += value;
    }
    public void decWeight(double value){
    	this.weight -= value;
    }
    
    /**
     * Unit tests the <tt>Edge</tt> data type.
     */
    public static void main(String[] args) {
    	WeightedEdge e = new WeightedEdge(12, 23, 3.14);
        StdOut.println(e);
    }
}