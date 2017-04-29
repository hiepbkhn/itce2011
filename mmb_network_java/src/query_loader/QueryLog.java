/*
 * Mar 17, 2017
 * 	- translated from mmb_network/query_loader.py (class QueryLog)
 */

package query_loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geom_util.Query;
import map_loader.MMBMap;
import mmb.Option;

public class QueryLog {

	public MMBMap map_data;
	
	public Map<Integer, Map<Integer, Query>> trajs = new HashMap<Integer, Map<Integer, Query>>();     //dict of dicts [node][timestamp]
    public Map<Integer, List<Query>> frames = new HashMap<Integer, List<Query>>();  //dict of lists
    
    public double min_speed = 1e10;
    public double max_speed = 0;
    
    //
    public QueryLog(MMBMap map_data){
    	this.map_data = map_data;
    }
	
    // query_type = 0 (Brinkhoff), 1 (TraceGenerator)
    public void read_query(String path, String log_file_name, int max_time_stamp, int query_type) throws IOException{	// max_time_stamp = -1
        
//        f = open(path + log_file_name, "r")
//        fstr = f.read()
//        f.close()
        
        BufferedReader f = new BufferedReader(new FileReader(path + log_file_name));
        List<String> fstr = new ArrayList<String>();
        while (true){
        	String str = f.readLine();
        	if (str == null)
        		break;
        	fstr.add(str);
        }
        f.close();
        
        int maxNodeId = -1;
        for (String line : fstr){
            String[] items = line.split("\t");
            
            int obj_id;
            double x;
            double y;
            int timestamp;
            double speed;
            int next_node_x;
            int next_node_y;
            int k_anom;
            double min_length = 0.0;
            ////// 1 - FOR Brinkhoff generator
            if (query_type == 0){
            	obj_id = Integer.parseInt(items[1]);
            	maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;

            	x = Double.parseDouble(items[5]);
	            y = Double.parseDouble(items[6]);
	            timestamp = Integer.parseInt(items[4]);
	            speed = Double.parseDouble(items[7]);
	            next_node_x = Integer.parseInt(items[8]);
	            next_node_y = Integer.parseInt(items[9]);
	            k_anom = Integer.parseInt(items[10]);
//	            min_length = Double.parseDouble(items[11]);
            }else{
            ////// 2 - FOR TraceGenerator
	            obj_id = Integer.parseInt(items[0]);
	            maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;
	
	            x = Double.parseDouble(items[2]);
	            y = Double.parseDouble(items[3]);
	            timestamp = Integer.parseInt(items[1]);
	            speed = Double.parseDouble(items[4]);
	            next_node_x = (int)Double.parseDouble(items[5]);
	            next_node_y = (int)Double.parseDouble(items[6]);
	            k_anom = Integer.parseInt(items[7]);
//	            min_length = Double.parseDouble(items[8]);     
	            // (End) 2 - FOR TraceGenerator       
            }
            
            //
            if (max_time_stamp != -1 && timestamp > max_time_stamp)
                break;

            // TODO: find next_node_id and cur_edge_id
            int next_node_id = this.map_data.get_next_node_id(next_node_x, next_node_y);
            int cur_edge_id = this.map_data.get_nearest_edge_id(next_node_x, next_node_y, x, y);
            
            // Trajectories
            if (!this.trajs.containsKey(obj_id))
            	this.trajs.put(obj_id, new HashMap<Integer, Query>());
            this.trajs.get(obj_id).put(timestamp, new Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, 
            		k_anom, min_length, Option.DISTANCE_CONSTRAINT, speed));
            

            if (!this.frames.containsKey(timestamp))
            	this.frames.put(timestamp, new ArrayList<Query>());
            this.frames.get(timestamp).add(new Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, 
            		k_anom, min_length, Option.DISTANCE_CONSTRAINT, speed));
            

            //
            if (speed > 0 && this.min_speed > speed)
            	this.min_speed = speed;
            if (this.max_speed < speed)
            	this.max_speed = speed;
        }
    }
}
