package map_loader;

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
		if (this.y < arg0.y)
			return -1;
		if (this.x > arg0.x)
			return 1;
		if (this.y > arg0.y)
			return 1;
		return 0;
	}
	
	
}
