package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geom_util.EdgeSegment;
import geom_util.EdgeSegmentSet;
import geom_util.Query;
import map_loader.MMBMap;
import mmb.Option;
import query_loader.QueryLog;
import tuple.PairInt;

public class Test {

	public static void main(String[] args) throws IOException {
		// TEST HashMap with PairInt key
//		Map<PairInt, Integer> map = new HashMap<PairInt, Integer>();
//		
//		map.put(new PairInt(1,2), 3);
//		
//		System.out.println(map.get(new PairInt(1,2)));
		
		// TEST String.split()
//		String a = "171-1944:0.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,|;";
//		System.out.println(a.split(";").length);
		
		
		// TEST MMBMap.compute_fixed_expanding(), EdgeSegmentSet.clean_fixed_expanding()
		int timestep = 3;
	    System.out.println("MAP_FILE = " + Option.MAP_NAME);
		System.out.println("timestep = " + timestep);
		System.out.println("QUERY_FILE = " + Option.QUERY_FILE);
		System.out.println("DISTANCE_CONSTRAINT = " + Option.DISTANCE_CONSTRAINT);
	    
	    //    
	    long start_time = System.currentTimeMillis();
	        
	    MMBMap map_data = new MMBMap();
	    map_data.read_map(Option.MAP_PATH, Option.MAP_NAME);
	    System.out.println("Load Map : DONE");
	    
	    QueryLog query_log = new QueryLog(map_data);
	    query_log.read_query(Option.QUERY_PATH, Option.QUERY_FILE, timestep, Option.QUERY_TYPE);   // default: max_time_stamp = 10 (40: only for attack) 
	    System.out.println("Load Query : DONE");
	    
	    System.out.println("max_speed = " + query_log.max_speed);
	    System.out.println("elapsed : " + (System.currentTimeMillis() - start_time));  
		
	    //
	    int timestamp = 0;
	    Map<Integer, List<EdgeSegment>> expanding_list = new HashMap<Integer, List<EdgeSegment>>();       //dict of lists
        List<Query> query_list = query_log.frames.get(timestamp);       // timestamp
        
        
        //1. compute expanding_list
        start_time = System.currentTimeMillis(); 
        System.out.println("#users = " + query_list.size());
        int count= 0;
        for (Query query : query_list){
        	List<EdgeSegment> seg_list = map_data.compute_fixed_expanding(query.x, query.y, 
                                            query.cur_edge_id, query.dist);  //old: Option.DISTANCE_CONSTRAINT
        	
        	double seg_list_length = 0.0;
	        for (EdgeSegment seg : seg_list)
	            seg_list_length += EdgeSegment.length(seg);
        	System.out.println("seg_list.size = " + seg_list.size() + " - " +  seg_list_length);
        	        
            seg_list = EdgeSegmentSet.clean_fixed_expanding(seg_list);
	        System.out.println("AFTER seg_list.size =" + seg_list.size());
            
            expanding_list.put(query.obj_id, seg_list);
            
            count += 1;
            if (count == 10)
            	break;
        }
        System.out.println("expanding_list - elapsed : " + (System.currentTimeMillis() - start_time));    
		
	}
}
