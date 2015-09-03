/*
 * Apr 3
 * 	- created
 */
package hist;

import grph.Grph;
import grph.VertexPair;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class GraphIntSet {

	IntSet[] e_list;
	
	////
	public GraphIntSet(Grph G){
		int n_nodes = G.getNumberOfVertices();
		e_list = new IntSet[n_nodes];
		for (int u = 0; u < n_nodes; u++)
			e_list[u] = new IntHashSet(); 

		//
		for (VertexPair p : G.getEdgePairs()){
	    	int u = p.first;
	       	int v = p.second;
	       	e_list[u].add(v);
	       	e_list[v].add(u);
		}
	}
	
	////
	public void addEdge(int u, int v){
		e_list[u].add(v);
		e_list[v].add(u);
	}
	
	////
	public void removeEdge(int u, int v){
		e_list[u].remove(v);
		e_list[v].remove(u);
	}
	
	////
	public boolean hasEdge(int u, int v){
		return e_list[u].contains(v);
	}
		
}
