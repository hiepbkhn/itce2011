package naive;

public class Tuple3Int implements Comparable<Tuple3Int>{
	public int r;	// row
	public int c;	// column
	public int val;
	
	////
	public Tuple3Int(int r, int c, int val) {
		super();
		this.r = r;
		this.c = c;
		this.val = val;
	}

	////
	public int compareTo(Tuple3Int other) {
		if (this.val < other.val)
			return -1;
		if (this.val > other.val)
			return 1;
		return 0;
	}
	
	
}
