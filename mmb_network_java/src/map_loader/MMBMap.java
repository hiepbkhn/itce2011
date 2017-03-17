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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geom_util.Edge;
import geom_util.GeomUtil;
import geom_util.Node;

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
        
        System.out.println("min,max (X,Y)" + min_x + " " + min_y + " " + max_x + " " + max_y);
        double dx = max_x - min_x;
        double dy = max_y - min_y;
        System.out.println("dx, dy :" + " " + dx + " " + dy);
        
        this.min_x = min_x;
        this.min_y = min_y;
        this.max_x = max_x;
        this.max_y = max_y;
        this.dx = dx;
        this.dy = dy;
        this.area = this.dx * this.dy;
        System.out.println("map.area" + " " + this.area);
    	
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
            System.out.println(start_node_id + " " + end_node_id);
            
            cur = cur + edge_name_len + 29;
            
            // CHECK
            if (start_node_id == end_node_id)
//                print "ERROR (start_node_id == end_node_id) at edge_id =", edge_id
                continue;
            if (this.node_pair_to_edge.containsKey(new PairInt(start_node_id, end_node_id)) )
//                print "ERROR (duplicate edge) at edge_id =", edge_id    
                continue;
            
            
            this.edges.put(edge_id, new Edge(edge_id, start_node_id, end_node_id, edge_class));
            
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
            this.node_pair_to_edge.put(new PairInt(start_node_id, end_node_id), edge_id);
        }
        
        f.close();
        
        System.out.println("total_map_len =" + this.total_map_len);
    	
    }
    
	//////////
	public static void main(String[] args) throws IOException {
		
		MMBMap map = new MMBMap();
		map.read_map("data/", "oldenburgGen");
		
	}
}
