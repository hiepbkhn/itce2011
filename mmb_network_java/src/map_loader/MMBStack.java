/*
 * Mar 17, 2017
 * 	- translated from mmb_network/map_loader.py (class Stack)
 */

package map_loader;

import java.util.List;
import java.util.Stack;

public class MMBStack {

	public Stack<SegItem> stack;
	public List<VisitedEdge> visited;	// list of visited edges 
	public int max_size;
	
	//
	public SegItem get(){
		return stack.pop();
	}
	
	//
	// 1: before, -1: after, 0: equal
    public int compare(SegItem seg_item_1, SegItem seg_item_2){
    	if (seg_item_1.x == seg_item_2.x && seg_item_1.y == seg_item_2.y)
            return 0;
        if ((seg_item_1.x < seg_item_2.x) || (seg_item_1.x == seg_item_2.x && seg_item_1.y < seg_item_2.y))
            return 1;
        else
            return -1; 
    }
    
    // 1: before, -1: after, 0: equal    
    public int compare_visited(VisitedEdge edge_1, VisitedEdge edge_2){
        if (edge_1.cur_edge_id < edge_2.cur_edge_id)
            return 1;
        if (edge_1.cur_edge_id > edge_2.cur_edge_id)
            return -1;
        
        if (edge_1.start_x == edge_2.start_x && edge_1.start_y == edge_2.start_y)
            return 0;
        if ((edge_1.start_x < edge_2.start_x) || (edge_1.start_x == edge_2.start_x && edge_1.start_y < edge_2.start_y))
            return 1;
        else
            return -1;
    }
    
    //
    public void push(SegItem seg_item){
        // check if seg_item is already in visited[]
        int insert_visited_loc = 0;
    	VisitedEdge edge = new VisitedEdge(seg_item.cur_edge_id, seg_item.x, seg_item.y);
        if (this.visited.size() > 0){
            boolean found_visited = false;
            int lo = 0;
            int hi = this.visited.size() - 1;
            int mid = (lo + hi) / 2;
            
            while (true){
                if (compare_visited(this.visited.get(mid), edge) == 0){
                    found_visited = true;
                    break;
                }
            
                if (compare_visited(this.visited.get(mid), edge) == -1){
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
            
            insert_visited_loc = lo;
            if (found_visited == true)
                return;
        }
        
        // check if seg_item.xy exist
        if (this.stack.size() == 0){
            this.stack.add(seg_item);
            this.visited.add(edge);
            return;
        }
        
        // insert to the head (queue !)
        this.stack.add(seg_item);
        this.visited.add(insert_visited_loc, edge);
    }
    
    //
    public int get_size(){
    	return this.stack.size();
    }
    
    //
    public void print_all(){
    	System.out.println("STACK");
        for (SegItem seg : this.stack)
        	System.out.println(seg.cur_edge_id + " " +  seg.x + " " +  seg.y + " " +  seg.end_x + " " +  seg.end_y + " " +  
        			seg.is_node + " " +  seg.length);
        System.out.println("VISITED");
        for (VisitedEdge edge : this.visited)
        	System.out.println(edge.cur_edge_id + " " +  edge.start_x + " " +  edge.start_y);
    }
    
}
