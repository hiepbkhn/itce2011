package tuple;

public class PairInt implements Comparable<PairInt>{

	public int x;
	public int y;
	
	//
	public PairInt(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(PairInt arg0) {
		if (this.x < arg0.x)
			return -1;
		else if (this.x > arg0.x)
			return 1;
		
		if (this.y < arg0.y)
			return -1;
		else if (this.y > arg0.y)
			return 1;
		return 0;
	}

	@Override
	public int hashCode() {
		
		return x * 100000 + y;
	}

	@Override
	public boolean equals(Object obj) {

		PairInt temp = (PairInt)obj;
		return (this.x == temp.x && this.y == temp.y);
	}
	
	
	
}
