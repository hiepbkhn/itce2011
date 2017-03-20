package tuple;

import geom_util.Edge;
import geom_util.EdgeSegment;

public class PairEdgeSegInt implements Comparable<PairEdgeSegInt> {

	public EdgeSegment e;
	public int obj_id;
	
	//
	public PairEdgeSegInt(EdgeSegment e, int obj_id) {
		super();
		this.e = e;
		this.obj_id = obj_id;
	}

	@Override
	public int compareTo(PairEdgeSegInt arg0) {
		if (this.e.cur_edge_id < arg0.e.cur_edge_id)
			return -1;
		if (this.e.cur_edge_id > arg0.e.cur_edge_id)
			return 1;
		return 0;
	}
	
	
}
