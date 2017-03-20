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
    
    public double max_speed = 0;
    
    //
    public QueryLog(MMBMap map_data){
    	this.map_data = map_data;
    }
	
    //
    public void read_query(String path, String log_file_name, int max_time_stamp) throws IOException{	// max_time_stamp = -1
        
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
            
            ////// 1 - FOR Brinkhoff generator
//            obj_id = int(items[1])
//            maxNodeId = max( obj_id, maxNodeId)
//
//            x = float(items[5])
//            y = float(items[6])
//            timestamp = int(items[4])
//            speed = float(items[7])
//            next_node_x = int(items[8])
//            next_node_y = int(items[9])
//            k_anom = int(items[10])
//            min_length = float(items[11])
            
            ////// 2 - FOR TraceGenerator
            int obj_id = Integer.parseInt(items[0]);
            maxNodeId = obj_id > maxNodeId ? obj_id : maxNodeId;

            double x = Double.parseDouble(items[2]);
            double y = Double.parseDouble(items[3]);
            int timestamp = Integer.parseInt(items[1]);
            double speed = Double.parseDouble(items[4]);
            double next_node_x = Double.parseDouble(items[5]);
            double next_node_y = Double.parseDouble(items[6]);
            int k_anom = Integer.parseInt(items[7]);
            double min_length = Double.parseDouble(items[8]);     
            // (End) 2 - FOR TraceGenerator       
            
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
            		k_anom, min_length, Option.DISTANCE_CONSTRAINT));
            

            if (!this.frames.containsKey(obj_id))
            	this.frames.put(obj_id, new ArrayList<Query>());
            this.frames.get(obj_id).add(new Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, 
            		k_anom, min_length, Option.DISTANCE_CONSTRAINT));
            

            //
            if (this.max_speed < speed)
            	this.max_speed = speed;
        }
    }
}
