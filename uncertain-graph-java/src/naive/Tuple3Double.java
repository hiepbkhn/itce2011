package naive;

public class Tuple3Double implements Comparable<Tuple3Double>{
	public int r;	// row
	public int c;	// column
	public double val;
	
	////
	public Tuple3Double(int r, int c, double val) {
		super();
		this.r = r;
		this.c = c;
		this.val = val;
	}

	////
	public int compareTo(Tuple3Double other) {
		if (this.val < other.val)
			return -1;
		if (this.val > other.val)
			return 1;
		return 0;
	}
	
	
}
