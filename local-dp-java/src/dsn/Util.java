package dsn;

import java.util.Arrays;

public class Util {

	//
	public static void quicksort(int[] main, int[] index) {
	    quicksort(main, index, 0, index.length - 1);
	}

	// quicksort a[left] to a[right]
	public static void quicksort(int[] a, int[] index, int left, int right) {
	    if (right <= left) return;
	    int i = partition(a, index, left, right);
	    quicksort(a, index, left, i-1);
	    quicksort(a, index, i+1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(int[] a, int[] index, 
	int left, int right) {
	    int i = left - 1;
	    int j = right;
	    while (true) {
	        while (less(a[++i], a[right]))      // find item on left to swap
	            ;                               // a[right] acts as sentinel
	        while (less(a[right], a[--j]))      // find item on right to swap
	            if (j == left) break;           // don't go out-of-bounds
	        if (i >= j) break;                  // check if pointers cross
	        exch(a, index, i, j);               // swap two elements into place
	    }
	    exch(a, index, i, right);               // swap with partition element
	    return i;
	}

	// is x < y ?
	private static boolean less(int x, int y) {
	    return (x < y);
	}

	// exchange a[i] and a[j]
	private static void exch(int[] a, int[] index, int i, int j) {
	    int swap = a[i];
	    a[i] = a[j];
	    a[j] = swap;
	    int b = index[i];
	    index[i] = index[j];
	    index[j] = b;
	}
	
	////
	public static int[] test0(){
		int[] ret = new int[]{5,4,3,2,1};
		
		return ret;
	}
	
	public static void test(int[] ret){
//		ret = test0();	// INCORRECT: temporary pointer to another array !
		
		System.arraycopy(test0(), 0, ret, 0, 5);	// CORRECT !
		
		ret[0] = 1000;
		for (int i : ret)
			System.out.print(i + " ");
		System.out.println();
	}
	
	////
	public static void main(String[] args) throws Exception{
//		int[] a = new int[]{5,7,3,10,18,21,6,11};
//		int[] idx = new int[]{0,1,2,3,4,5,6,7};
//		
//		quicksort(a, idx);
//		for (int i : a)
//			System.out.print(i + " ");
//		System.out.println();
//		
//		for (int i : idx)
//			System.out.print(i + " ");
//		System.out.println();
		
		// by-value !
		int[] idx = new int[]{0,1,2,3,4,5,6,7};
		test(idx);
		for (int i : idx)
			System.out.print(i + " ");
		System.out.println();
		
		
	}
}
