'''
Created on Jan 1, 2013

@author: Nguyen Huu Hiep

'''

from datetime import datetime
import time
import math
import sys
import option
from multiprocessing import Process, Manager
import cPickle
from subprocess import call

from query_loader import QueryLog
from map_loader import Map, Stack 
from geom_util import EdgeSegment, EdgeSegmentSet, Point, Query, CloakingSet, MBR, get_distance


def write_find_cloaking_sets(positive_mc_set):
    f = open(option.RESULT_PATH + "positive_mc_set.out", "w")
    cPickle.dump(positive_mc_set, f, protocol=0)

def parallelized_find_positive_negative_cliques(total_map_len, edge_segment_set, 
                                               clique_group, expanding_list, trajs, map_ratio):
    positive_mc_set = []
    negative_mc_set = []
    
    # WAY-1
#    (positive_mc_set, negative_mc_set) = edge_segment_set.find_positive_negative_cliques(timestamp, total_map_len, clique_group, 
#                                                           query_log, expanding_list, map_ratio)

    # WAY-2
#    for clique in clique_group:
#        clique_type = edge_segment_set.find_clique_type(clique, trajs, expanding_list, 
#                                                        total_map_len, map_ratio) 
#        if clique_type == 1:
#            positive_mc_set.append(clique)
#        if clique_type == 2:
#            negative_mc_set.append(clique)

    # WAY-3, need "math" in job submission
    for clique in clique_group:
        if len(clique) == 1:
            continue
        #
        max_min_area = 0
        max_k_anom = 0
        query_list = []
        for obj_id in clique:
            query = trajs[obj_id]
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
                clique_len >= math.sqrt(max_min_area) * total_map_len * map_ratio:
            positive_mc_set.append(clique) 
        elif len(clique) > 2:
            negative_mc_set.append(clique) 

    
    # RETURN
    return positive_mc_set, negative_mc_set


#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    
    start_time = time.clock()
        
#    map_data = Map()
#    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
#    print "Load Map : DONE"
#    query_log = QueryLog(map_data)
#    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, max_time_stamp = 10)    
#    print "Load Query : DONE"
#    
#    print "max_speed = ", query_log.max_speed
#    print "elapsed : ", (time.clock() - start_time)   
    
    
    ## LOAD map_data, query_log
    f = open(option.RESULT_PATH + "map_data.out", "r")
    map_data = cPickle.load(f)
    
    f = open(option.RESULT_PATH + "query_log.out", "r")
    query_log = cPickle.load(f)
    query_log.map_data = map_data
    
    print "LOAD map_data, query_log - elapsed : ", (time.clock() - start_time)   
    
    #
    timestamp = 0
    query_list = query_log.frames[timestamp]       # timestamp    
    
    trajs = {}
    for query in query_list:
        trajs[query.obj_id] = query_log.trajs[query.obj_id][timestamp]
    
    edge_segment_set = EdgeSegmentSet()
    
    # load expanding_list
    start_time = time.clock()    
    f = open(option.RESULT_PATH + "expanding_list.out", "r")
#    global expanding_list
    expanding_list = cPickle.load(f)
    
    print "Load expanding_list: DONE! - elapsed : ", (time.clock() - start_time) 
#    print "size(expanding_list) =", sys.getsizeof(expanding_list)
    
    # load mc_set
    f = open(option.RESULT_PATH + "mc_set.out", "r")
    mc_set = cPickle.load(f)
    print "Load mc_set: DONE, len(mc_set) =", len(mc_set)
    
    
    #1. PARALLEL
    start_time = time.clock()    
    print "Now: ", datetime.now()
    
    positive_mc_set_1 = []
    negative_mc_set_1 = []
    
    #1.1 find positive and negative cliques
    total_map_len = map_data.total_map_len
    
    clique_groups = []
    num_groups = 4
    num_cliques = len(mc_set)
    for i in range(num_groups-1):
        start = i*num_cliques/num_groups
        end = (i+1)*num_cliques/num_groups
        clique_groups.append(mc_set[start:end])
    clique_groups.append(mc_set[end:num_cliques]) 
    
    for clique_group in clique_groups:
        print "len(clique_groups) =", len(clique_group)   
    
    manager = Manager()
    dict_expanding_list = manager.dict(expanding_list)
    dict_trajs = manager.dict(trajs)
    
    for clique_group in clique_groups:
        p = Process(target=parallelized_find_positive_negative_cliques, args=(total_map_len, edge_segment_set, 
                    clique_group, dict_expanding_list, dict_trajs, option.MAP_RATIO))
        p.start()
        p.join()
    
    print "Submit jobs: DONE! - elapsed : ", (time.clock() - start_time) 
    
    print "find positive and negative cliques - elapsed : ", (time.clock() - start_time)  
    print "Now: ", datetime.now()

    ## USE NON-PARALLEL 
#    #2.convert negative cliques (heuristically)
#    new_negative_mc_set = []
#    
#    for clique in negative_mc_set_1:
#        query_list = []
#        for obj_id in clique:
#            query = query_log.trajs[obj_id][timestamp]
#            query_list.append(query)
#        #sort
#        query_list = sorted(query_list, key=lambda query: query.k_anom)
#            
#        while True:
#            query_list.pop()    #remove the last
#            if len(query_list) == 0:
#                break;
#            max_min_area = max(query_list, key=lambda query: query.min_area).min_area
#            #compute length of mesh
#            mesh = []
#            for query in query_list:
#                mesh = edge_segment_set.union(mesh, expanding_list[query.obj_id])
#            clique_len = edge_segment_set.length(mesh)
#            
#            #
#            if len(query_list) >= query_list[-1].k_anom and \
#                    clique_len >= math.sqrt(max_min_area) * map_data.total_map_len * option.MAP_RATIO:
#                break
#        #
#        if len(query_list) > 1:
#            clique = set([query.obj_id for query in query_list])
#            new_negative_mc_set.append(clique)    
#            
#    positive_mc_set_1.extend(new_negative_mc_set)
#    
#    
#    print "find_cloaking_sets (1) - elapsed : ", (time.clock() - start_time)        
     
        
    #2. NON-PARALLEL    
#    start_time = time.clock() 
#    
#    #1. find positive and negative cliques
#    positive_mc_set_2 = []
#    negative_mc_set_2 = []
#    for clique in mc_set:
#        if len(clique) == 1:
#            continue
#        #
#        max_min_area = 0
#        max_k_anom = 0
#        query_list = []
#        for obj_id in clique:
#            query = query_log.trajs[obj_id][timestamp]
#            query_list.append(query)
#            if max_min_area < query.min_area:
#                max_min_area = query.min_area
#            if max_k_anom < query.k_anom:
#                max_k_anom = query.k_anom
#                
#        #compute length of mesh
#        mesh = []
#        for obj_id in clique:
#            mesh = edge_segment_set.union(mesh, expanding_list[obj_id])
#        clique_len = edge_segment_set.length(mesh)
#        
#        #
#        if len(clique) >= max_k_anom and \
#                clique_len >= math.sqrt(max_min_area) * map_data.total_map_len * option.MAP_RATIO:
#            positive_mc_set_2.append(clique) 
#        elif len(clique) > 2:
#            negative_mc_set_2.append(clique)
#            
#    #2.convert negative cliques (heuristically)
#    new_negative_mc_set = []
#    
#    for clique in negative_mc_set_2:
#        query_list = []
#        for obj_id in clique:
#            query = query_log.trajs[obj_id][timestamp]
#            query_list.append(query)
#        #sort
#        query_list = sorted(query_list, key=lambda query: query.k_anom)
#            
#        while True:
#            query_list.pop()    #remove the last
#            if len(query_list) == 0:
#                break;
#            max_min_area = max(query_list, key=lambda query: query.min_area).min_area
#            #compute length of mesh
#            mesh = []
#            for query in query_list:
#                mesh = edge_segment_set.union(mesh, expanding_list[query.obj_id])
#            clique_len = edge_segment_set.length(mesh)
#            
#            #
#            if len(query_list) >= query_list[-1].k_anom and \
#                    clique_len >= math.sqrt(max_min_area) * map_data.total_map_len * option.MAP_RATIO:
#                break
#        #
#        if len(query_list) > 1:
#            clique = set([query.obj_id for query in query_list])
#            new_negative_mc_set.append(clique)    
#            
#    positive_mc_set_2.extend(new_negative_mc_set)
#        
#    print "find_cloaking_sets (2) - elapsed : ", (time.clock() - start_time)

    
    #3. CHECK


    #4. WRITE TO FILE
#    write_find_cloaking_sets(positive_mc_set_1)    
#
#    print "Serialize positive_mc_set to file: DONE"
    
    #X.
#    print "globals =", globals()
