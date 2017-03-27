/*
 * Mar 17, 2017
 * 	- translated from mmb_network/map_loader.py (class Stack)
 */

package map_loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geom_util.Edge;
import geom_util.EdgeSegment;
import geom_util.EdgeSegmentSet;
import geom_util.GeomUtil;
import geom_util.LineOperator;
import geom_util.Node;
import geom_util.Point;
import mmb.Option;
import tuple.PairDouble;
import tuple.PairInt;
import tuple.TripleDouble;
import tuple.TripleDoubleInt;

public class MMBMap {

	public Map<Integer, Node> nodes = new HashMap<Integer, Node>();
    public Map<Integer, Edge> edges = new HashMap<Integer, Edge>();
    
    public Map<Integer, List<Integer>>adj = new HashMap<Integer, List<Integer>>();          //adjacent lists (list of lists)
    public Map<Integer, List<Integer>> node_to_edges = new HashMap<Integer, List<Integer>>();
    
    public Map<PairInt, Integer> xy_to_node_id = new HashMap<PairInt, Integer>();
    public Map<PairInt, Integer> node_pair_to_edge = new HashMap<PairInt, Integer>();
    //
//    public interval_tree_x = None
//    public interval_tree_y = None
    
    public double min_x = 0;
    public double min_y = 0;
    public double max_x = 0;
    public double max_y = 0;
    public double dx = 0;
    public double dy = 0;
    public double area = 0;   // = dx*dy
    public double total_map_len = 0;
    
    // big endian
    private long longFromBytes(byte[] by){
    	long value = 0;
    	for (int i = 0; i < by.length; i++)
    	{
    	   value = (value << 8) + (by[i] & 0xff);
    	}
    	return value;
    }
    
    // big endian
    private int intFromBytes(byte[] by){
    	int value = 0;
    	for (int i = 0; i < by.length; i++)
    	{
    	   value = (value << 8) + (by[i] & 0xff);
    	}
    	return value;
    }
    
    //
    public void read_map(String path, String map_name) throws IOException{
    	// 1. NODES
    	String inputFile = path + map_name + ".node";
    	
    	InputStream f = new FileInputStream(inputFile);
    	long fileSize = new File(inputFile).length();

    	byte[] data = new byte[(int) fileSize];
        f.read(data);
        
        int cur = 0;
        double min_x = 1000000000; 
        double min_y = 1000000000;
        double max_x = -1000000000;
        double max_y = -1000000000;
        while (cur < data.length){
            // node name
            int node_name_len = (char)data[cur];
            //print node_name_len
            String node_name = new String(Arrays.copyOfRange(data, cur+1, cur+node_name_len+1));
            //print node_name
            
            // node id (big endian)
            //print repr(data[cur+node_name_len+2 : cur+node_name_len+10])
            int node_id = intFromBytes(Arrays.copyOfRange(data, cur+node_name_len+1, cur+node_name_len+9));
            //print node_id
        
            // node x (big endian)
            int node_x = intFromBytes(Arrays.copyOfRange(data, cur+node_name_len+9, cur+node_name_len+13));
            //print node_x
            
            // node y (big endian)
            int node_y = intFromBytes(Arrays.copyOfRange(data, cur+node_name_len+13, cur+node_name_len+17));
            //print node_y
            
            cur = cur+node_name_len + 17;
            
            this.nodes.put(node_id, new Node(node_id, node_x, node_y));
            this.xy_to_node_id.put(new PairInt(node_x, node_y), node_id);
            
            // DEBUG
//            System.out.println(node_id + " " + node_x + " " + node_y);
            
            //
            if (min_x > node_x) 
                min_x = node_x;        
            if (min_y > node_y)
                min_y = node_y;
            if (max_x < node_x)
                max_x = node_x;
            if (max_y < node_y) 
                max_y = node_y;
        }
        
        f.close();
        
        System.out.println("min,max (X,Y) " + min_x + " " + min_y + " " + max_x + " " + max_y);
        double dx = max_x - min_x;
        double dy = max_y - min_y;
        System.out.println("dx, dy : " + " " + dx + " " + dy);
        
        this.min_x = min_x;
        this.min_y = min_y;
        this.max_x = max_x;
        this.max_y = max_y;
        this.dx = dx;
        this.dy = dy;
        this.area = this.dx * this.dy;
        System.out.println("map.area " + " " + this.area);
    	
    	// 2. EDGES
        inputFile = path + map_name + ".edge";
    	
    	f = new FileInputStream(inputFile);
    	fileSize = new File(inputFile).length();

    	data = new byte[(int) fileSize];
        f.read(data);
        
        cur = 0;
        while (cur < data.length){
            // start_node id (big endian)
            int start_node_id = intFromBytes(Arrays.copyOfRange(data, cur, cur+8));
            //print start_node_id
            
            // end_node id (big endian)
            int end_node_id = intFromBytes(Arrays.copyOfRange(data, cur+8, cur+16));
            //print end_node_id
            
            // edge name
            int edge_name_len = (char)data[cur+16];
            //print edge_name_len
            String edge_name = new String(Arrays.copyOfRange(data, cur+17, cur+17+edge_name_len));
            //print "%c" % edge_name
            
            // node x (big endian)
            int edge_id = intFromBytes(Arrays.copyOfRange(data, cur+edge_name_len+17, cur+edge_name_len+25));
            //print edge_id
            
            // edge_class (big endian)
            int edge_class = intFromBytes(Arrays.copyOfRange(data, cur+edge_name_len+25, cur+edge_name_len+29));
            //print edge_class
            
            //print start_node_id, end_node_id, edge_name_len, edge_name, edge_id, edge_class
            // DEBUG
//            System.out.println(start_node_id + " " + end_node_id);
            
            cur = cur + edge_name_len + 29;
            
            // CHECK
            if (start_node_id == end_node_id)
//                print "ERROR (start_node_id == end_node_id) at edge_id =", edge_id
                continue;
            if (this.node_pair_to_edge.containsKey(new PairInt(start_node_id, end_node_id)) )
//                print "ERROR (duplicate edge) at edge_id =", edge_id    
                continue;
            
            Edge edge = new Edge(edge_id, start_node_id, end_node_id, edge_class);
            edge.edge_length = Edge.length(this.nodes.get(start_node_id), this.nodes.get(end_node_id));
            
            this.edges.put(edge_id, edge);
            
            this.total_map_len += GeomUtil.get_edge_length(this.nodes.get(start_node_id), this.nodes.get(end_node_id));
            //
            if (!this.node_to_edges.containsKey(start_node_id)) 
            	this.node_to_edges.put(start_node_id, new ArrayList<Integer>());
            this.node_to_edges.get(start_node_id).add(edge_id);
            
            if (!this.node_to_edges.containsKey(end_node_id)) 
            	this.node_to_edges.put(end_node_id, new ArrayList<Integer>());
            this.node_to_edges.get(end_node_id).add(edge_id);
            
            //    
            if (!this.adj.containsKey(start_node_id)) 
            	this.adj.put(start_node_id, new ArrayList<Integer>());
            this.adj.get(start_node_id).add(end_node_id);
            
            if (!this.adj.containsKey(end_node_id)) 
            	this.adj.put(end_node_id, new ArrayList<Integer>());
            this.adj.get(end_node_id).add(start_node_id);   
            
            //
            this.node_pair_to_edge.put(new PairInt(start_node_id, end_node_id), edge_id);
            this.node_pair_to_edge.put(new PairInt(end_node_id, start_node_id), edge_id);
        }
        
        f.close();
        
        System.out.println("total_map_len = " + this.total_map_len);
    	
    }
    
    //
    public Map<Integer, Node> get_nodes(){
    	return this.nodes;
    }
    
    //
    public Map<Integer, Edge> get_edges(){
    	return this.edges;
    }
    
    //
    public Map<Integer, List<Integer>> get_node_to_edges(){
    	return this.node_to_edges;
    }
    
    //
    public int get_next_node_id(int next_node_x, int next_node_y){
    	if (this.xy_to_node_id.containsKey(new PairInt(next_node_x, next_node_y)) )
    		return this.xy_to_node_id.get(new PairInt(next_node_x, next_node_y));
    	else
    		return -1;   //we may have node 0
    }
    
    //
    public boolean is_full_edge(EdgeSegment seg){
        Edge edge = this.edges.get(seg.cur_edge_id);
        Node start_node = this.nodes.get(edge.start_node_id);
        Node end_node = this.nodes.get(edge.end_node_id);
        if (seg.start_x == start_node.x && seg.start_y == start_node.y &&
            seg.end_x == end_node.y && seg.end_y == end_node.y)
            return true;
        if (seg.start_x == end_node.x && seg.start_y == end_node.y &&
            seg.end_x == start_node.y && seg.end_y == start_node.y)
            return true;
        return false;
    }
    
    //
    public int get_nearest_edge_id(int next_node_x, int next_node_y, double px, double py){
        double min_distance = 100000000.0;
        int nearest_edge_id = -1;
        
        int next_node_id = this.get_next_node_id(next_node_x, next_node_y);
        
        //print type(this.node_to_edges.get(next_node_id)) 
        
        for (int edge_id : this.node_to_edges.get(next_node_id)){
            int start_node_id = this.edges.get(edge_id).start_node_id;
            int end_node_id = this.edges.get(edge_id).end_node_id;
            //
            double length = LineOperator.distance_to(this.nodes.get(start_node_id).x, this.nodes.get(start_node_id).y,
                this.nodes.get(end_node_id).x, this.nodes.get(end_node_id).y, px, py);
            if (length < min_distance){
                min_distance = length;
                nearest_edge_id = edge_id;
            }
        }
                
//            print edge_id, length, px, py, this.nodes[start_node_id].x, this.nodes[start_node_id].y, \
//                this.nodes[end_node_id].x, this.nodes[end_node_id].y
                
        return nearest_edge_id;
    }
    
    //
    public List<EdgeSegment> compute_fixed_expanding(double x, double y, int cur_edge_id, double length){
        
    	List<EdgeSegment>result = new ArrayList<EdgeSegment>();
        
    	MMBStack stack = new MMBStack();
        //
        boolean is_node = this.get_next_node_id((int)x, (int)y) > -1;
        
        stack.push(new SegItem(x, y, -1, -1, length, cur_edge_id, is_node));
        
        while (stack.get_size() > 0){
            // DEBUG
//            stack.print_all()
//            print "END OF LIST"
            //
        	SegItem item = stack.get();
//            if item.length == 0:
//                result.append(item)
//                continue
            
            // case 1, is_node == True
            if (item.is_node == true){
                int node_id = this.get_next_node_id((int)item.x, (int)item.y);
                
                for (int end_node_id : this.adj.get(node_id)){   //traverse adjacent edges...
                	double edge_len = GeomUtil.get_edge_length(this.nodes.get(node_id), this.nodes.get(end_node_id));
                    if (edge_len < item.length){
                        double remaining_len = item.length - edge_len;
                        //
                        result.add(new EdgeSegment(this.nodes.get(node_id).x, this.nodes.get(node_id).y, 
                                              this.nodes.get(end_node_id).x, this.nodes.get(end_node_id).y,
                                              this.node_pair_to_edge.get(new PairInt(node_id, end_node_id)) ));
                        //
                        for (int edge_id : this.node_to_edges.get(end_node_id)) //one choice for each adjacent edge
                            stack.push(new SegItem(this.nodes.get(end_node_id).x, this.nodes.get(end_node_id).y, 
                                                 -1, -1,
                                                 remaining_len, edge_id, true));
                        
                    }else{
                        double end_x = item.x + item.length * (this.nodes.get(end_node_id).x - item.x) / edge_len;
                        double end_y = item.y + item.length * (this.nodes.get(end_node_id).y - item.y) / edge_len;
                        result.add(new EdgeSegment(this.nodes.get(node_id).x, this.nodes.get(node_id).y, 
                                              end_x, end_y, 
                                              this.node_pair_to_edge.get(new PairInt(node_id, end_node_id)) ));
                    }
                }
            }
            // case 2, is_node == False
            else{
            	int[] id_list = new int[]{this.edges.get(item.cur_edge_id).start_node_id, this.edges.get(item.cur_edge_id).end_node_id};
            	for (int end_node_id : id_list){
                    double segment_len = GeomUtil.get_segment_length(this.nodes.get(end_node_id), item.x, item.y);
                    if (segment_len < item.length){
                        double remaining_len = item.length - segment_len;
                        // end_node_id.xy go first to comply with convention: first point always graph node !!
                        result.add(new EdgeSegment(this.nodes.get(end_node_id).x, this.nodes.get(end_node_id).y,  
                                              item.x, item.y,
                                              item.cur_edge_id));
                        //
                        for (int edge_id : this.node_to_edges.get(end_node_id))
                            stack.push(new SegItem(this.nodes.get(end_node_id).x, this.nodes.get(end_node_id).y, 
                                             -1, -1,
                                             remaining_len, edge_id, true));
                    }    
                    else{
                    	double end_x = item.x + item.length * (this.nodes.get(end_node_id).x - item.x) / segment_len;
                    	double end_y = item.y + item.length * (this.nodes.get(end_node_id).y - item.y) / segment_len;
                        result.add(new EdgeSegment(item.x, item.y,
                                              end_x, end_y, 
                                              item.cur_edge_id));
                    }
                }
            }
        }
        
        // DEBUG
//        print "stack.max_size", stack.max_size
//        
//        print "length(result) = ", length(result)
//        for item in result:
//            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
//                item.start_x, item.start_y, item.end_x, item.end_y)
         
        return result;
	}
    
    //
    public List<EdgeSegment> compute_mesh_expanding(List<EdgeSegment> item_list, double length){   
        
    	List<EdgeSegment> result = item_list;
        //1. call find_boundary_points() 
    	List<TripleDoubleInt> boundary_points = MMBMap.find_boundary_points(item_list);
        
        //2. 
        for (TripleDoubleInt point : boundary_points){
            List<EdgeSegment> new_seg_set = this.compute_fixed_expanding(point.v0, point.v1, point.v2, Option.MAX_SPEED);
            // OLD
//            new_seg_set = EdgeSegmentSet.clean_fixed_expanding(new_seg_set)
//            result = EdgeSegmentSet.union(result, new_seg_set)
            // NEW
            result.addAll(new_seg_set);
        }
        
        result = EdgeSegmentSet.clean_fixed_expanding(result);
            
        return result;
    }
    
    //
    public boolean is_node_in_rec(double min_x, double min_y, double max_x, double max_y, Node node){
        int p1_x = node.x;
        int p1_y = node.y;
        return (min_x <= p1_x) && (p1_x <= max_x) && (min_y <= p1_y) && (p1_y <= max_y);
    }
    
    //
    public boolean is_edge_in_rec(double min_x, double min_y, double max_x, double max_y, Edge edge){
        return this.is_node_in_rec(min_x, min_y, max_x, max_y, this.nodes.get(edge.start_node_id)) && 
            this.is_node_in_rec(min_x, min_y, max_x, max_y, this.nodes.get(edge.end_node_id));
    }


    //
    public TripleDouble get_line_equation(double x1, double y1, double x2, double y2){
        return new TripleDouble(y2-y1, x1-x2, y1*x2-y2*x1);
    }
    
    //
    public boolean is_edge_cut_rec(double min_x, double min_y, double max_x, double max_y, Edge edge){
        int x1 = this.nodes.get(edge.start_node_id).x;
        int y1 = this.nodes.get(edge.start_node_id).y;
        int x2 = this.nodes.get(edge.end_node_id).x;
        int y2 = this.nodes.get(edge.end_node_id).y;
        TripleDouble coeff = this.get_line_equation(x1, y1, x2, y2);
        double a1 = coeff.v0*min_x + coeff.v1*min_y + coeff.v2;
        double a2 = coeff.v0*min_x + coeff.v1*max_y + coeff.v2;
        double a3 = coeff.v0*max_x + coeff.v1*max_y + coeff.v2;
        double a4 = coeff.v0*max_x + coeff.v1*min_y + coeff.v2;
        
        if  (this.is_node_in_rec(min_x, min_y, max_x, max_y, this.nodes.get(edge.start_node_id)) ||
            this.is_node_in_rec(min_x, min_y, max_x, max_y, this.nodes.get(edge.end_node_id)))
            return true;       
        else
        	if ((a1*a2 < 0 && (x1-min_x)*(x2-min_x) < 0) || (a2*a3 < 0 && (y1-max_y)*(y2-max_y) < 0) 
            	|| (a3*a4 < 0 && (x1-max_x)*(x2-max_x) < 0) || (a4*a1 < 0 && (y1-min_y)*(y2-min_y) < 0))
            return true;
        
        return false;
    }
            
    //
    //MBR: maximum boundary rectangle
    //MUST use R-Tree !!!
    //
    public List<EdgeSegment> compute_mesh_mbr(List<Point> locations){
    	List<EdgeSegment> result = new ArrayList<EdgeSegment>();
        //
    	double min_x = 100000000;
    	double min_y = 100000000;
    	double max_x = -100000000;
        double max_y = -100000000;
        for (Point point : locations){
            if (min_x > point.x)
                min_x = point.x;
            if (min_y > point.y)
                min_y = point.y;
            if (max_x < point.x)
                max_x = point.x;
            if (max_y < point.y)
                max_y = point.y;
        }
        
//        print "compute_mesh_mbr - min,max (X,Y)", min_x, min_y, max_x, max_y
                  
        //Solution 1: linear scan
//        start = time.clock()
        for (Edge edge : this.edges.values())
            if (this.is_edge_cut_rec(min_x, min_y, max_x, max_y, edge)){
                double p1_x = this.nodes.get(edge.start_node_id).x;
                double p1_y = this.nodes.get(edge.start_node_id).y;
                double p2_x = this.nodes.get(edge.end_node_id).x;
                double p2_y = this.nodes.get(edge.end_node_id).y;
                result.add(new EdgeSegment(p1_x, p1_y, p2_x, p2_y, edge.edge_id));
            }
                
//        print "Elapsed ", (time.clock() - start)        
//        print "Solution 1: len=", len(result)
//        for edge_seg in result:
//            print edge_seg.cur_edge_id
        
                
        //Solution 2: Interval Tree
//        start = time.clock()
//        set_x = set(this.interval_tree_x.findRange([min_x, max_x]))
//        set_y = set(this.interval_tree_y.findRange([min_y, max_y]))
//        print "set_x", set_x
//        print "set_y", set_y
//        
//        cutting_edges = set_x & set_y
//        print "cutting_edges", cutting_edges
//        result = []
//        for edge_id in cutting_edges:
//            if this.is_edge_cut_rec(min_x, min_y, max_x, max_y, this.edges[edge_id]):
//                p1_x = this.nodes[this.edges[edge_id].start_node_id].x
//                p1_y = this.nodes[this.edges[edge_id].start_node_id].y
//                p2_x = this.nodes[this.edges[edge_id].end_node_id].x
//                p2_y = this.nodes[this.edges[edge_id].end_node_id].y
//                result.append(EdgeSegment(p1_x, p1_y, p2_x, p2_y, edge_id))  
//        print "Elapsed ", (time.clock() - start)         
//        print "Solution 2: len=", len(result)
//        for edge_seg in result:
//            print edge_seg.cur_edge_id
                  
        return result;
    }
    
    //
    public static List<TripleDoubleInt> find_boundary_points(List<EdgeSegment> item_list){
        
//        print "length(item_list) = ", len(item_list)
//        for item in item_list:
//            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
//                item.start_x, item.start_y, item.end_x, item.end_y)
            
        // WAY-1    
//        nodes = {}
//        result = []
//        
//        for item in item_list:
//            if not nodes.has_key((item.start_x, item.start_y, item.cur_edge_id)):
//                nodes[(item.start_x, item.start_y, item.cur_edge_id)] = 1
//            else:
//                nodes[(item.start_x, item.start_y, item.cur_edge_id)] += 1
//                
//            if not nodes.has_key((item.end_x, item.end_y, item.cur_edge_id)):
//                nodes[(item.end_x, item.end_y, item.cur_edge_id)] = 1   
//            else:
//                nodes[(item.end_x, item.end_y, item.cur_edge_id)] += 1   
//        
//        for (point, count) in nodes.iteritems():
//            if count == 1:
//                result.append(point)

        // WAY-2
        List<TripleDoubleInt> nodes = new ArrayList<TripleDoubleInt>();
        List<TripleDoubleInt> result = new ArrayList<TripleDoubleInt>();
        for (EdgeSegment item : item_list){
            nodes.add(new TripleDoubleInt(item.start_x, item.start_y, item.cur_edge_id));   
            nodes.add(new TripleDoubleInt(item.end_x, item.end_y, item.cur_edge_id));
        }
        
        Collections.sort(nodes);
        
        for (int i = 0; i < nodes.size()-1; i++)
            if ((nodes.get(i+1).v0 != nodes.get(i).v0) || (nodes.get(i+1).v1 != nodes.get(i).v1))
                result.add(nodes.get(i));
        if ((nodes.get(nodes.size()-1).v0 != nodes.get(nodes.size()-2).v0) || (nodes.get(nodes.size()-1).v1 != nodes.get(nodes.size()-2).v1))
                result.add(nodes.get(nodes.size()-1));
        
                
        return result;
    }
    
	//////////
	public static void main(String[] args) throws IOException {
		
		MMBMap map_data = new MMBMap();
		map_data.read_map("data/", "oldenburgGen");
		
		
		
	}
}
