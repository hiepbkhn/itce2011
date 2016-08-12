/*
 * Mar 27, 2016
 * 	- commented hashCode()
 * 	- fix compareTo()
 */

package hist;

import algs4.Edge;

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

	// overflow for returned int !
	// BUT required for HashMap (e.g. in LouvainDP)
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
	
	////////////////////////////////////////////////////
	public static void main(String[] args){
		Int2 a = new Int2(1, 2);
		Int2 b = new Int2(1, 2);
		Int2 c = new Int2(2, 2);
		System.out.println(a.equals(a));
		System.out.println(a.equals(b));
		System.out.println(a.equals(c));
		
		int v1 = 1000000;
		int v2 = 2000000;
		long BIG_VAL = 1000000000;
//		long k = (long)v1 * 1000000000 + v2;	// ok
		long k = v1 * BIG_VAL + v2;	// ok
		System.out.println("k = " + k); // -1528494976
		
		Edge e = new Edge((int)(k/BIG_VAL), (int)(k % BIG_VAL), 1);
		v1 = e.either();
		v2 = e.other(v1);
		System.out.println(v1 + " " + v2);
	}
}
