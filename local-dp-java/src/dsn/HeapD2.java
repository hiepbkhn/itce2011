/*
 * Jun 8, 2016
 * 	- copied from Heap.java, adjusted for distance-2 Vertex Cover
 */

package dsn;

import hist.Int2;

public class HeapD2 {
	public Int2[] a;	// a[i].val0: node id, a[i].val1: node value (degree at distance 2)
	public int[] loc;	// loc[u] = i <--> a[i].val0 = u OR a[loc[u]].val0 = u
	private int n;
	private int left;
	private int right;
	private int largest;

	////
	public HeapD2(Int2[] a){
		this.a = a;
		this.loc = new int[a.length];
		for (int i = 0; i < a.length; i++)
			loc[i] = i;
		this.n = a.length-1;
	}
	
	////
	public void buildheap(int n) {
		for (int i = n / 2; i >= 0; i--) {
			maxheap(i);
		}
	}

	////
	public void maxheap(int i) {
		left = 2 * i;
		right = 2 * i + 1;
		if (left <= n && a[left].val1 > a[i].val1) {
			largest = left;
		} else {
			largest = i;
		}

		if (right <= n && a[right].val1 > a[largest].val1) {
			largest = right;
		}
		if (largest != i) {
			exchange(i, largest);
			maxheap(largest);
		}
	}

	////
	public void update(int v, int d){
		a[loc[v]].val1 -= d;
		maxheap(loc[v]);
	}
	
	////
	public void exchange(int i, int j) {
		int t = a[i].val0;
		a[i].val0 = a[j].val0;
		a[j].val0 = t;
		
		t = a[i].val1;
		a[i].val1 = a[j].val1;
		a[j].val1 = t;
		
		//
		t = loc[a[i].val0];
		loc[a[i].val0] = loc[a[j].val0];;
		loc[a[j].val0] = t;
	}

	////
	public void sort() {
		buildheap(n);

		for (int i = n; i > 0; i--) {
			exchange(0, i);
			n = n - 1;
			maxheap(0);
		}
	}

	////
	public void print(){
		System.out.println("HEAP");
		for (int i = 0; i < a.length; i++)
			System.out.print(loc[i] + " ");
		System.out.println();
		for (int i = 0; i < a.length; i++)
			System.out.print(a[i].val0 + " " + a[i].val1 + ", ");
		System.out.println();
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) {
		Int2[] a1 = { new Int2(0,4), new Int2(1,1), new Int2(2,3), new Int2(3,2), new Int2(4,16), 
				new Int2(5,9), new Int2(6,10), new Int2(7,14), new Int2(8,8), new Int2(9,7) };
		
		HeapD2 heap = new HeapD2(a1);
		heap.sort();
		
		for (int i = 0; i < a1.length; i++)
			System.out.print(heap.loc[i] + " ");
		System.out.println();
		
		for (int i = 0; i < a1.length; i++)
			System.out.print(a1[i].val0 + " ");
		System.out.println();
		
		for (int i = 0; i < a1.length; i++)
			System.out.print(a1[i].val1 + " ");
	}
}
