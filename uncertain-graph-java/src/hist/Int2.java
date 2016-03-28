/*
 * Mar 27, 2016
 * 	- commented hashCode()
 * 	- fix compareTo()
 */

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
		if (this.val0 < other.val0)
			return -1;
		if (this.val0 == other.val0 && this.val1 < other.val1)
			return -1;
		if (this.val0 > other.val0)
			return 1;
		if (this.val0 == other.val0 && this.val1 > other.val1)
			return 1;
		return 0;
	}

//	@Override
//	public int hashCode() {
//		return val0*10000000 + val1;
//	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Int2 other = (Int2) obj;
		if (val0 != other.val0)
			return false;
		if (val1 != other.val1)
			return false;
		return true;
	}
	
	////
	
}
