/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class CloakingSet)
 */

package geom_util;

import java.util.List;

public class CloakingSet{

	public clique = clique
    public List<Query> query_list;
    public MBR mbr;
	
	//
	public CloakingSet(clique, query_list){
        self.clique = clique
        self.query_list = query_list
        self.mbr = CloakingSet.compute_mbr(query_list)
	}
    
    
    public static MBR compute_mbr(List<Query> query_list){
        double min_x = 100000000;
        double min_y = 100000000;
        double max_x = -100000000;
        double max_y = -100000000;   
        for (Query query : query_list){
            if (min_x > query.x)
                min_x = query.x;
            if (min_y > query.y)
                min_y = query.y;
            if (max_x < query.x)
                max_x = query.x;
            if (max_y < query.y)
                max_y = query.y;
        }
        
        return new MBR((max_x-min_x)*(max_y-min_y), min_x, max_x, min_y, max_y);
    }
}
