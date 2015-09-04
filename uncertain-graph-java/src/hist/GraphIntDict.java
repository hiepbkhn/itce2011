/*
 * Sep 4
 * 	- created
 */
package hist;

import java.util.HashMap;
import java.util.Map;

import grph.Grph;
import grph.VertexPair;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class GraphIntDict {

	Map<Integer, IntSet> e_dict;
	
	////int[2][m]
	public GraphIntDict(int[] nodelist, int[][] edgelist){
		int n = nodelist.length;
		int m = edgelist[0].length;
		
		e_dict = new HashMap<Integer, IntSet>();
		for (int u = 0; u < n; u++)
			e_dict.put(nodelist[u], new IntHashSet()); 

		//
		for (int i = 0; i < m; i++){
	    	int u = edgelist[0][i];
	       	int v = edgelist[1][i];
	       	e_dict.get(u).add(v);
	       	e_dict.get(v).add(u);
		}
	}
	
	////
	public void addEdge(int u, int v){
		e_dict.get(u).add(v);
		e_dict.get(v).add(u);
	}
	
	////
	public void removeEdge(int u, int v){
		e_dict.get(u).remove(v);
		e_dict.get(v).remove(u);
	}
	
	////
	public boolean hasEdge(int u, int v){
		return e_dict.get(u).contains(v);
	}
		
}
