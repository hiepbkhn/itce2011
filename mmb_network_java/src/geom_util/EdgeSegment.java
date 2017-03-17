/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class EdgeSegment)
 */

package geom_util;

public class EdgeSegment implements Comparable<EdgeSegment>{

	public double start_x;
    public double start_y;
    public double end_x;
    public double end_y;
    public int cur_edge_id;
    
    //
	public EdgeSegment(double start_x, double start_y, double end_x, double end_y, int cur_edge_id) {
		super();
		this.start_x = start_x;
		this.start_y = start_y;
		this.end_x = end_x;
		this.end_y = end_y;
		this.cur_edge_id = cur_edge_id;
	}
    
    //
	public void normalize(){
        if (this.start_x > this.end_x || (this.start_x == this.end_x && this.start_y > this.end_y)){
            double temp = this.start_x;
            this.start_x = this.end_x;
            this.end_x = temp;
            temp = this.start_y;
            this.start_y = this.end_y;
            this.end_y = temp;
        }
	}
	
	//
    public static double square_length(EdgeSegment seg){
        return (seg.start_x - seg.end_x)*(seg.start_x - seg.end_x) + (seg.start_y - seg.end_y)*(seg.start_y - seg.end_y);
    }
     
    public static double length(EdgeSegment seg){
        return Math.sqrt((seg.start_x - seg.end_x)*(seg.start_x - seg.end_x) + (seg.start_y - seg.end_y)*(seg.start_y - seg.end_y));
    }
        
    public static boolean is_line_cover(Point point, EdgeSegment line){
        if ((point.x == line.start_x && point.y == line.start_y) ||
            (point.x == line.end_x && point.y == line.end_y))
            return true;
        if (line.start_x == line.end_x)
            return (point.y - line.start_y)*(point.y - line.end_y) < 0;
        if (line.start_y == line.end_y)
            return (point.x - line.start_x)*(point.x - line.end_x) < 0;
    
        return (point.y - line.start_y)*(point.y - line.end_y) < 0;
    }
    
    //
    public static PairBoolSeg union(EdgeSegment seg_1, EdgeSegment seg_2){
        if (seg_1.cur_edge_id != seg_2.cur_edge_id)
            return new PairBoolSeg(false, null);
        
        // swap (not needed, already sorted)
        
        // case 0: conincide <start> OR <end> --> return the longer segment
        if ((seg_1.start_x == seg_2.start_x && seg_1.start_y == seg_2.start_y) ||
            (seg_1.end_x == seg_2.end_x && seg_1.end_y == seg_2.end_y))
            if (EdgeSegment.square_length(seg_1) > EdgeSegment.square_length(seg_2))
                return new PairBoolSeg(true, seg_1);
            else
                return new PairBoolSeg(true, seg_2);
        
        // 
        if (seg_2.start_x < seg_1.end_x)
            if (seg_2.end_x >= seg_1.end_x)    //overlapped  
                return new PairBoolSeg(true, new EdgeSegment(seg_1.start_x, seg_1.start_y, 
                                            seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id ));
            else                               //covered  
                return new PairBoolSeg(true, seg_1);
            
        if (seg_1.start_x == seg_1.end_x)      //vertical !
            if (seg_1.end_y >= seg_2.start_y)  //not disjoint  
                if (seg_1.end_y < seg_2.end_y) //overlapped  
                    return new PairBoolSeg(true, new EdgeSegment(seg_1.start_x, seg_1.start_y, 
                                            seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id ));
                else    
                    return new PairBoolSeg(true, seg_1);        //covered  

        return new PairBoolSeg(false, null);
    }
    
    //
    public static PairBoolSeg intersect(EdgeSegment seg_1, EdgeSegment seg_2){     //Note: seg_1, seg_2 are already normalized
        // case 0: conincide <start> OR <end> --> return the shorter segment
        if ((seg_1.start_x == seg_2.start_x && seg_1.start_y == seg_2.start_y) ||
            (seg_1.end_x == seg_2.end_x && seg_1.end_y == seg_2.end_y))
            if (EdgeSegment.square_length(seg_1) < EdgeSegment.square_length(seg_2))
                return new PairBoolSeg(true, seg_1);
            else
                return new PairBoolSeg(true, seg_2);
        
        // 
        if (seg_2.start_x < seg_1.end_x)
            if (seg_2.end_x >= seg_1.end_x)    //overlapped  
                return new PairBoolSeg(true, new EdgeSegment(seg_2.start_x, seg_2.start_y, 
                                            seg_1.end_x, seg_1.end_y, seg_1.cur_edge_id ));
            else                               //covered  
                return new PairBoolSeg(true, seg_2);
            
        if (seg_1.start_x == seg_1.end_x)      //vertical !
            if (seg_1.end_y >= seg_2.start_y)  //not disjoint  
                if (seg_1.end_y < seg_2.end_y) //overlapped  
                    return new PairBoolSeg(true, new EdgeSegment(seg_1.start_x, seg_1.start_y, 
                                            seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id ));
                else    
                    return new PairBoolSeg(true, seg_2);        //covered  
        return new PairBoolSeg(false, null);
    }

	@Override
	public int compareTo(EdgeSegment arg0) {
		if (this.cur_edge_id < arg0.cur_edge_id)
			return -1;
		if (this.start_x < arg0.start_x)
			return -1;
		if (this.start_y < arg0.start_y)
			return -1;
		if (this.end_x < arg0.end_x)
			return -1;
		if (this.end_y < arg0.end_y)
			return -1;
		
		if (this.cur_edge_id > arg0.cur_edge_id)
			return 1;
		if (this.start_x > arg0.start_x)
			return 1;
		if (this.start_y > arg0.start_y)
			return 1;
		if (this.end_x > arg0.end_x)
			return 1;
		if (this.end_y > arg0.end_y)
			return 1;
		return 0;
	}
    
}
