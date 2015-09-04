package hist;

public class Int2 implements Comparable<Int2> {
	public int val0;
	public int val1;
	public Int2(int val0, int val1) {
		this.val0 = val0;
		this.val1 = val1;
	}
	
	////
	public int compareTo(Int2 other) {
		if (this.val1 < other.val1)
			return -1;
		if (this.val1 > other.val1)
			return 1;
		return 0;
	}
}
