/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class EdgeSegmentSet)
 */

package geom_util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EdgeSegmentSet {
	
	public List<EdgeSegment> set = new ArrayList<EdgeSegment>();
	
	//
    public static List<EdgeSegment> clean_fixed_expanding(List<EdgeSegment> result){
    	List<EdgeSegment> new_result = result;
        
        // 1. NORMALIZE each edge (left_low_x first)
        for (EdgeSegment item : new_result)
            item.normalize();
        
        // 2. SORT by cur_edge_id
        Collections.sort(new_result);
        
        // DEBUG
//        print "length(new_result) = ", length(new_result)
//        for item in new_result:
//            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
//                item.start_x, item.start_y, item.end_x, item.end_y)
         
        // 3. REMOVE duplicates
        int cur = 0;
        while (cur < new_result.size()-1){
            if (new_result.get(cur+1).cur_edge_id == new_result.get(cur).cur_edge_id &&
                new_result.get(cur+1).start_x == new_result.get(cur).start_x &&
                new_result.get(cur+1).start_y == new_result.get(cur).start_y &&
                new_result.get(cur+1).end_x == new_result.get(cur).end_x &&
                new_result.get(cur+1).end_y == new_result.get(cur).end_y){
                new_result.remove(cur+1);
                continue;
            }
            else
                cur += 1;
        }
                
//        print "length(new_result) = ", length(new_result)
//        for item in new_result:
//            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
//                item.start_x, item.start_y, item.end_x, item.end_y)
        
        // 4. UNION
        cur = 0;
        while (cur < new_result.size()-1){
            PairBoolSeg test_union = EdgeSegment.union(new_result.get(cur), new_result.get(cur+1));
            if (test_union.result == true){
                new_result.remove(cur);
                new_result.remove(cur);
                new_result.add(cur, test_union.seg);
                continue;
            }
            else
                cur += 1;
        }
        
        // DEBUG                
//        print "length(new_result) = ", length(new_result)
//        for item in new_result:
//            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
//                item.start_x, item.start_y, item.end_x, item.end_y)    
        
         
        
        //
        return new_result;

    }
    
    
    //        
    public static List<EdgeSegment> union(List<EdgeSegment> set_1, List<EdgeSegment> set_2){
    	List<EdgeSegment> result = set_1;
        result.addAll(set_2);
        
        return EdgeSegmentSet.clean_fixed_expanding(result);
    }
        
    public static List<EdgeSegment> intersect(List<EdgeSegment> set_1, List<EdgeSegment> set_2){        //Note: set_1, set_2 are already sorted by (cur_edge_id, start_x,...) 
    	List<EdgeSegment> result = new ArrayList<EdgeSegment>();
        for (EdgeSegment item_1 : set_1)
            for (EdgeSegment item_2 : set_2)
                if (item_1.cur_edge_id == item_2.cur_edge_id){
                	PairBoolSeg test_intersect = EdgeSegment.intersect(item_1, item_2);
                    if (test_intersect.result == true)
                        result.add(test_intersect.seg);
                }
//        result = sorted(result, key=lambda edge_segment: (edge_segment.cur_edge_id, 
//            edge_segment.start_x, edge_segment.start_y, edge_segment.end_x, edge_segment.end_y))
        
        return result;
    }
    
    public static double length(EdgeSegmentSet set_1){
    	double total_len = 0;
        for (EdgeSegment item : set_1.set)
            total_len += EdgeSegment.length(item);
        
        return total_len;
    }
        
    //
    public static boolean is_set_cover(Point point, List<EdgeSegment> line_set){
        if (line_set.size() == 0)
            return false;
    
        
        
        //binary search
        int lo = 0;
        int hi = line_set.size() - 1;
        int mid = (lo + hi) / 2;
        boolean found = false;
        while (true){
            if (line_set.get(mid).cur_edge_id == point.cur_edge_id){
                found = true;
                break;
            }
            
            if (line_set.get(mid).cur_edge_id > point.cur_edge_id){
                hi = mid - 1;
                if (hi < lo) 
                    break;
            }
            else{
                lo = mid + 1;
                if (lo > hi)
                    break;
            }
            mid = (lo + hi) / 2;    
        }
        
        if (found == false)
            return false;
        
//        print found, mid
        
        //
        lo = mid;
        while (lo-1 > 0 && line_set.get(lo-1).cur_edge_id == point.cur_edge_id)
            lo = lo - 1;
        hi = mid;
        while (hi+1 < line_set.size() && line_set.get(hi+1).cur_edge_id == point.cur_edge_id)
            hi = hi + 1;
        
        for (int i = lo; i < hi+1; i++){
        	EdgeSegment item = line_set.get(i);
            if (EdgeSegment.is_line_cover(point, item) == true)
                return true;
        }
        
        return false;
    }
    
    public static MBR compute_mbr(EdgeSegmentSet mesh){
        double min_x = 100000000;
        double min_y = 100000000;
        double max_x = -100000000;
        double max_y = -100000000;  
        for (EdgeSegment seg : mesh.set){
            if (min_x > seg.start_x)
                min_x = seg.start_x;
            if (min_y > seg.start_y)
                min_y = seg.start_y;
            if (max_x < seg.start_x)
                max_x = seg.start_x;
            if (max_y < seg.start_y)
                max_y = seg.start_y;
            if (min_x > seg.end_x)
                min_x = seg.end_x;
            if (min_y > seg.end_y)
                min_y = seg.end_y;
            if (max_x < seg.end_x)
                max_x = seg.end_x;
            if (max_y < seg.end_y)
                max_y = seg.end_y;   
        }
        return new MBR((max_x-min_x)*(max_y-min_y), min_x, max_x, min_y, max_y);
    }
}
