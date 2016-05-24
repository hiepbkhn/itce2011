package coding;

public class Link {

	public int r;		// size 
	public int[] c;		// coefficients
	
	////
	public Link(int r){
		this.r = r;
		this.c = new int[this.r];
	}
	
	////
	public Link copy(){
		Link ret = new Link(this.r);
		
		for (int i = 0; i < ret.r; i++)
			ret.c[i] = this.c[i]; 
		
		//
		return ret;
	}
	
	//// linear combination
	public static Link combine(Link link1, Link link2, int val1, int val2){
		Link ret = new Link(link1.r);
		
		for (int i = 0; i < ret.r; i++)
			ret.c[i] = (link1.c[i] * val1 + link2.c[i] * val2) % ret.r; 
		
		
		//
		return ret;
	}
	
	//// determinant of matrix
	public static int det(Link[] links, int r){
		int ret = 0;
		
		if (r == 2){
			ret = (links[0].c[0] * links[1].c[1] - links[0].c[1] * links[1].c[0]) % r;
		}
		
		//
		return ret;
	}

	////
	@Override
	public String toString() {
		return "[" + this.c[0] + ":" + this.c[1] + "]";
	}
	
	
	
}
