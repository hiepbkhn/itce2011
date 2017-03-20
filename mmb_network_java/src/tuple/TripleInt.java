package tuple;

public class TripleInt implements Comparable<TripleInt>{
	public int v0;
	public int v1;
	public int v2;
	
	//
	public TripleInt(int v0, int v1, int v2) {
		super();
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public int compareTo(TripleInt arg0) {
		if (this.v0 < arg0.v0)
			return -1;
		if (this.v1 < arg0.v1)
			return -1;
		if (this.v2 < arg0.v2)
			return -1;
		if (this.v0 > arg0.v0)
			return 1;
		if (this.v1 > arg0.v1)
			return 1;
		if (this.v2 > arg0.v2)
			return 1;
		
		return 0;
	}
	
}
