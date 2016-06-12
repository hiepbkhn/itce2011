/*
 * Jun 12, 2016
 * 	- copied from Int2.java
 * 	- add field c
 */

package dsn;

public class Int3 implements Comparable<Int3> {
	public int val0;
	public int val1;
	public int c;		// edge counter (see LinkExchange.linkExchangeNoDup())
	
	public Int3(int val0, int val1) {
		this.val0 = val0;
		this.val1 = val1;
	}
	
	////
	public int compareTo(Int3 other) {
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
		Int3 other = (Int3) obj;
		if (val0 != other.val0)
			return false;
		if (val1 != other.val1)
			return false;
		return true;
	}
	
	////
	
}
