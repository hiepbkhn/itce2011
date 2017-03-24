package tuple;

import java.util.ArrayList;
import java.util.List;

public class PairIntList {

	public int num_edges;
	public List<PairInt> list_edges = new ArrayList<PairInt>();
	//
	public PairIntList(int num_edges, List<PairInt> list_edges) {
		super();
		this.num_edges = num_edges;
		this.list_edges = list_edges;
	}
	
	
}
