package tuple;

public class TripleDoubleInt implements Comparable<TripleDoubleInt>{
	public double v0;
	public double v1;
	public int v2;		// cur_edge_id --> see MMBMap
	
	//
	public TripleDoubleInt(double v0, double v1, int v2) {
		super();
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public int compareTo(TripleDoubleInt arg0) {
		if (this.v0 < arg0.v0)
			return -1;
		else if (this.v0 > arg0.v0)
			return 1;
		
		if (this.v1 < arg0.v1)
			return -1;
		else if (this.v1 > arg0.v1)
			return 1;
		
		if (this.v2 < arg0.v2)
			return -1;
		else if (this.v2 > arg0.v2)
			return 1;
		
		return 0;
	}
	
	
}
