package tuple;

public class PairDouble {

	public double x;
	public double y;
	
	//
	public PairDouble(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		PairDouble temp = (PairDouble)obj;
		return (this.x == temp.x && this.y == temp.y);
	}

	@Override
	public int hashCode() {
		
		return (int)(x * 100000 + y);
	}
	
	
	
}
