/*
 * Jun 13, 2016
 * 	- copied from Int2, use short
 */

package hist;

public class Short2 implements Comparable<Short2> {
	public short val0;
	public short val1;
	public Short2(short val0, short val1) {
		this.val0 = val0;
		this.val1 = val1;
	}
	
	////
	public int compareTo(Short2 other) {
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
//	public short hashCode() {
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
		Short2 other = (Short2) obj;
		if (val0 != other.val0)
			return false;
		if (val1 != other.val1)
			return false;
		return true;
	}
	
	////
	
}
