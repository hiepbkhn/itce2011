'''
Created on Dec 30, 2012

@author: Nguyen Huu Hiep

compute_expanding_list --> OK 
        by converting all "staticmethod" to non-static, e.g. EdgeSegmentSet.clean_fixed_expanding
        and setting number of jobs small value, num_groups = 2 (=no.cores) is the best
'''

import time
import math
import sys
import option
import pp
import cPickle
from subprocess import call

from query_loader import QueryLog
from map_loader import Map, Stack 
from geom_util import EdgeSegment, EdgeSegmentSet, Point, Query, CloakingSet, MBR, get_distance



def serialize_compute_expanding_list(expanding_list):
    f = open(option.RESULT_PATH + "expanding_list.out", "w")
    cPickle.dump(expanding_list, f, protocol=0)
    
    
def write_compute_expanding_list(expanding_list):
    f = open(option.RESULT_PATH + "expanding_list.txt", "w")
    for (obj_id, seg_list) in expanding_list.iteritems():
        f.write("%d\n"%obj_id)
        for seg in seg_list:
            f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
        f.write("\n")            
    f.close()   
    
     

def parallelized_compute_expanding_list(map_data, edge_segment_set, query_group, init_graph_distance):
    result = []
    for query in query_group:
        seg_list = map_data.compute_fixed_expanding(query.x, query.y, 
                                            query.cur_edge_id, init_graph_distance)
        seg_list = edge_segment_set.clean_fixed_expanding(seg_list)
        result.append(seg_list)
    
    return result   




#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    
    start_time = time.clock()
        
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    query_log = QueryLog(map_data)
    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, max_time_stamp = 5)    
    print "Load Query : DONE"
    
    print "max_speed = ", query_log.max_speed
    print "elapsed : ", (time.clock() - start_time)   
    
    print "size(map_data) =", sys.getsizeof(map_data)
    
    #
    expanding_list_1 = {}                                 #dict of lists
    
    timestamp = 0
    if len(sys.argv) > 1:
        timestamp = int(sys.argv[1])
    print "timestamp =", timestamp
    
    query_list = query_log.frames[timestamp]       # timestamp    
    
    edge_segment_set = EdgeSegmentSet()
    
    #1. PARALLEL
    num_groups = 8
    ppservers = ()
    job_server = pp.Server(ncpus = num_groups, ppservers=ppservers)
    print "Starting pp with", job_server.get_ncpus(), "workers"
    
    start_time = time.clock()    
        
    query_groups = []
    num_queries = len(query_list)
    for i in range(num_groups-1):
        start = i*num_queries/num_groups
        end = (i+1)*num_queries/num_groups
        query_groups.append(query_list[start:end])
    query_groups.append(query_list[end:num_queries])
    
    for query_group in query_groups:
        print "len(query_group) =", len(query_group)
    
        
    jobs = [(query_group, job_server.submit(parallelized_compute_expanding_list, 
                (map_data, edge_segment_set, query_group, option.INIT_GRAPH_DISTANCE,), 
                (map_data.compute_fixed_expanding,edge_segment_set.clean_fixed_expanding,), 
                ("map_loader", "geom_util"))) for query_group in query_groups]
    print "Submit jobs: DONE! - elapsed : ", (time.clock() - start_time)
    
    results = []
    for query_group, job in jobs:
        res = job()
        print type(res)
        results.append(res)
        print "DONE one query group !"
    
    for query_group, res in zip(query_groups,results):    
        for query, seg_list in zip(query_group, res):
            expanding_list_1[query.obj_id] = seg_list 
    
    job_server.print_stats()        
    
    print "expanding_list_1 - elapsed : ", (time.clock() - start_time)        
     
        
    #2. NON-PARALLEL    
    expanding_list_2 = {}                                 #dict of lists
    
    start_time = time.clock()    
    for query in query_list:
        seg_list = map_data.compute_fixed_expanding(query.x, query.y, 
                                        query.cur_edge_id, option.INIT_GRAPH_DISTANCE)
        seg_list = edge_segment_set.clean_fixed_expanding(seg_list)
        
        expanding_list_2[query.obj_id] = seg_list
    print "expanding_list_2 - elapsed : ", (time.clock() - start_time)       

    #3. CHECK
    print "Compare expanding_list_1 & expanding_list_2"
    print "len(expanding_list_1) =", len(expanding_list_1)
    print "len(expanding_list_2) =", len(expanding_list_2)
    
    for query in query_list:
        seg_list_1 = expanding_list_1[query.obj_id]
        seg_list_2 = expanding_list_2[query.obj_id]
        if len(seg_list_1) != len(seg_list_2):
            print "Error at query.obj_id = ", query.obj_id
            break
    print "Compare expanding_list(s): DONE"    

    #4. SERIALIZE TO FILE
    serialize_compute_expanding_list(expanding_list_1)    

    print "Serialize expanding_list to file: DONE"
    
    #5. WRITE TO FILE (for MAPPED I/O)
    start_time = time.clock()    
    write_compute_expanding_list(expanding_list_1)
    print "Write expanding_list to file - elapsed : ", (time.clock() - start_time) 
    
    #X.
#    print "globals =", globals()
