'''
Created on Dec 30, 2012

@author: Nguyen Huu Hiep

'''

from datetime import datetime
import copy
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


def write_find_cloaking_sets(positive_mc_set):
    f = open(option.RESULT_PATH + "positive_mc_set.out", "w")
    cPickle.dump(positive_mc_set, f, protocol=0)



def mapped_mem_find_positive_negative_cliques(timestamp, total_map_len, edge_segment_set, 
                                               clique_group, map_ratio):
    positive_mc_set = []
    negative_mc_set = []
    
    # use mmap to map trajs and expanding_list
    #1.
    trajs = edge_segment_set.parse_query_log(timestamp)
    
    expanding_list = edge_segment_set.parse_expanding_list()
    

    #2. 
    for clique in clique_group:
        clique_type = edge_segment_set.find_clique_type(clique, trajs, expanding_list, 
                                                        total_map_len, map_ratio) 
        if clique_type == 1:
            positive_mc_set.append(clique)
        if clique_type == 2:
            negative_mc_set.append(clique)
    
    len_positive = len(positive_mc_set)
    len_negative = len(negative_mc_set)
    
    #3. convert negative cliques
    new_negative_mc_set = []

    for clique in negative_mc_set:
        query_list = []
        for obj_id in clique:
            query = trajs[obj_id]
            query_list.append(query)
        #sort
        query_list = sorted(query_list, key=lambda query: query.k_anom)
            
        while True:
            query_list.pop()    #remove the last
            if len(query_list) == 0:
                break;
            max_min_area = max(query_list, key=lambda query: query.min_area).min_area
            #compute length of mesh
            mesh = []
            for query in query_list:
                mesh = edge_segment_set.union(mesh, expanding_list[query.obj_id])
            clique_len = edge_segment_set.length(mesh)
            
            #
            if len(query_list) >= query_list[-1].k_anom and \
                    clique_len >= math.sqrt(max_min_area) * total_map_len * map_ratio:
                break
        #
        if len(query_list) > 1:
            clique = set([query.obj_id for query in query_list])
            new_negative_mc_set.append(clique)  
    
    positive_mc_set.extend(new_negative_mc_set)
    
    # RETURN
    return positive_mc_set, len_positive, len_negative        


### NOT USED
def parallelized_find_positive_negative_cliques(total_map_len, edge_segment_set, 
                                               clique_group, expanding_list, trajs, map_ratio):
    positive_mc_set = []
    negative_mc_set = []
    
    # WAY-1
#    (positive_mc_set, negative_mc_set) = edge_segment_set.find_positive_negative_cliques(timestamp, total_map_len, clique_group, 
#                                                           query_log, expanding_list, map_ratio)

    # WAY-2
    #1.
    for clique in clique_group:
        clique_type = edge_segment_set.find_clique_type(clique, trajs, expanding_list, 
                                                        total_map_len, map_ratio) 
        if clique_type == 1:
            positive_mc_set.append(clique)
        if clique_type == 2:
            negative_mc_set.append(clique)

    
    # RETURN
    return positive_mc_set, negative_mc_set




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
    
#    ##
#    f = open(option.RESULT_PATH + "map_data.out", "w")
#    cPickle.dump(map_data, f, protocol=0)
#    
#    f = open(option.RESULT_PATH + "query_log.out", "w")
#    cPickle.dump(query_log, f, protocol=0)


    ## LOAD map_data, query_log
#    f = open(option.RESULT_PATH + "map_data.out", "r")
#    map_data = cPickle.load(f)
#    
#    f = open(option.RESULT_PATH + "query_log.out", "r")
#    query_log = cPickle.load(f)
#    query_log.map_data = map_data
    
    
    print "LOAD map_data, query_log - elapsed : ", (time.clock() - start_time)   
    
    ####
    start_time = time.clock()
    
    timestamp = 0
    if len(sys.argv) > 1:
        timestamp = int(sys.argv[1])
    print "timestamp =", timestamp
    
#    query_list = query_log.frames[timestamp]       # timestamp    
#    
#    trajs = {}
#    for query in query_list:
#        trajs[query.obj_id] = query_log.trajs[query.obj_id][timestamp]   #Deep-Copy or NOT
#    print "prepare trajs - elapsed : ", (time.clock() - start_time)  
#    print "size(trajs) =", sys.getsizeof(trajs)
    
    edge_segment_set = EdgeSegmentSet()
    
    # load expanding_list
#    start_time = time.clock()    
#    f = open(option.RESULT_PATH + "expanding_list.out", "r")
#    expanding_list = cPickle.load(f)
#    
#    print "Load expanding_list: DONE! - elapsed : ", (time.clock() - start_time) 
#    print "size(expanding_list) =", sys.getsizeof(expanding_list)
    
    # mem-file mapping
    trajs = edge_segment_set.parse_query_log(timestamp)
    expanding_list = edge_segment_set.parse_expanding_list()
    
    
    # load mc_set
    f = open(option.RESULT_PATH + "mc_set.out", "r")
    mc_set = cPickle.load(f)
    print "Load mc_set: DONE, len(mc_set) =", len(mc_set)
    
    
    #1. PARALLEL
    num_groups = 8
    
    ppservers = ()
    job_server = pp.Server(ncpus = num_groups, ppservers=ppservers)
    print "Starting pp with", job_server.get_ncpus(), "workers"
    
    start_time = time.clock()    
    print "Now: ", datetime.now()
    
    positive_mc_set_1 = []
    
    #1.1 find positive and negative cliques
    total_map_len = map_data.total_map_len
    
    clique_groups = []
    num_cliques = len(mc_set)
    for i in range(num_groups-1):
        start = i*num_cliques/num_groups
        end = (i+1)*num_cliques/num_groups
        clique_groups.append(mc_set[start:end])
    clique_groups.append(mc_set[end:num_cliques]) 
    
    for clique_group in clique_groups:
        print "len(clique_groups) =", len(clique_group)   
    
    jobs = [job_server.submit(mapped_mem_find_positive_negative_cliques, 
                (timestamp, total_map_len, edge_segment_set, clique_group, option.MAP_RATIO,), 
                (edge_segment_set.parse_query_log, edge_segment_set.parse_expanding_list, 
                 edge_segment_set.find_clique_type,), 
                ("geom_util","math")) for clique_group in clique_groups]
    print "Submit jobs: DONE! - elapsed : ", (time.clock() - start_time) 
    
    results = []
    for job in jobs:
        res = job()
        print type(res)
        results.append(res)
        print "DONE one clique_group !"
    
    num_positive = 0
    num_negative = 0
    for res in results:   
        for clique in res[0]:
            positive_mc_set_1.append(clique)
        num_positive += res[1]
        num_negative += res[2]    
    
    print "num_positive =", num_positive
    print "num_negative =", num_negative
    
    job_server.print_stats()      
    
    print "find positive/negative cliques - elapsed : ", (time.clock() - start_time)  
    print "len(positive_mc_set_1) =", len(positive_mc_set_1)
    print "Now: ", datetime.now()
    
     
    


          
     
    ##############################################    
    #2. NON-PARALLEL    
    start_time = time.clock() 
    
    #1. find positive and negative cliques
    positive_mc_set_2 = []
    negative_mc_set_2 = []
    for clique in mc_set:
        if len(clique) == 1:
            continue
        #
        max_min_area = 0
        max_k_anom = 0
        query_list = []
        for obj_id in clique:
            query = query_log.trajs[obj_id][timestamp]
            query_list.append(query)
            if max_min_area < query.min_area:
                max_min_area = query.min_area
            if max_k_anom < query.k_anom:
                max_k_anom = query.k_anom
                
        #compute length of mesh
        mesh = []
        for obj_id in clique:
            mesh = edge_segment_set.union(mesh, expanding_list[obj_id])
        clique_len = edge_segment_set.length(mesh)
        
        #
        if len(clique) >= max_k_anom and \
                clique_len >= math.sqrt(max_min_area) * map_data.total_map_len * option.MAP_RATIO:
            positive_mc_set_2.append(clique) 
        elif len(clique) > 2:
            negative_mc_set_2.append(clique)
    
    print "len(positive_mc_set_2)", len(positive_mc_set_2)
    print "len(negative_mc_set_2)", len(negative_mc_set_2)
            
    #2.convert negative cliques (heuristically)
    new_negative_mc_set = []
    
    for clique in negative_mc_set_2:
        query_list = []
        for obj_id in clique:
            query = trajs[obj_id]           #query_log.trajs[obj_id][timestamp]
            query_list.append(query)
        #sort
        query_list = sorted(query_list, key=lambda query: query.k_anom)
            
        while True:
            query_list.pop()    #remove the last
            if len(query_list) == 0:
                break;
            max_min_area = max(query_list, key=lambda query: query.min_area).min_area
            #compute length of mesh
            mesh = []
            for query in query_list:
                mesh = edge_segment_set.union(mesh, expanding_list[query.obj_id])
            clique_len = edge_segment_set.length(mesh)
            
            #
            if len(query_list) >= query_list[-1].k_anom and \
                    clique_len >= math.sqrt(max_min_area) * map_data.total_map_len * option.MAP_RATIO:
                break
        #
        if len(query_list) > 1:
            clique = set([query.obj_id for query in query_list])
            new_negative_mc_set.append(clique)    
            
    positive_mc_set_2.extend(new_negative_mc_set)
        
    print "find_cloaking_sets (2) - elapsed : ", (time.clock() - start_time)

    
    #3. CHECK
    print "Compare positive_mc_set_1 & positive_mc_set_2"
    print "len(positive_mc_set_1) =", len(positive_mc_set_1)
    print "len(positive_mc_set_2) =", len(positive_mc_set_2)
    

    #4. WRITE TO FILE
    write_find_cloaking_sets(positive_mc_set_1)    

    print "Serialize positive_mc_set to file: DONE"
    #X.
#    print "globals =", globals()
