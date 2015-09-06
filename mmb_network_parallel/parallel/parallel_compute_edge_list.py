'''
Created on Dec 30, 2012

@author: Nguyen Huu Hiep

compute_expanding_list --> OK 
        by converting all "staticmethod" to non-static, e.g. EdgeSegmentSet.clean_fixed_expanding
        and setting number of jobs small value, num_groups = 2 (=no.cores) is the best
'''

import time
import sys
import math
import option
import pp
import cPickle
from subprocess import call

from query_loader import QueryLog
from map_loader import Map, Stack 
from geom_util import EdgeSegment, EdgeSegmentSet, Point, Query, CloakingSet, MBR, get_distance
from mesh_cloak_parallel import Graph


def write_mc_set(mc_set):
    f = open(option.RESULT_PATH + "mc_set.out", "w")
    cPickle.dump(mc_set, f, protocol=0)

def write_compute_edge_list(list_edges):
    f = open(option.RESULT_PATH + "edge_list.out", "w")
    cPickle.dump(list_edges, f, protocol=0)

def parallelized_compute_partial_edges(map_data, expanding_group):
    full_edge_meshes = {}
    part_edges = []
    #
    for (obj_id, mesh) in expanding_group.iteritems():
        for e in mesh:
            if map_data.is_full_edge(e) == True:   #FULL edge
                if not full_edges.has_key(e.cur_edge_id):
                    full_edge_meshes[e.cur_edge_id] = []
                full_edge_meshes[e.cur_edge_id].append(obj_id)
                    
            else:                                       #PARTIAL edge
                part_edges.append((e, obj_id))          # "list" of only one point mesh(obj_id) covers e

    return full_edge_meshes, part_edges

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
    
    graph = Graph(0, map_data, query_log, None)
    
    #
    timestamp = 0
    if len(sys.argv) > 1:
        timestamp = int(sys.argv[1])
    print "timestamp =", timestamp
    
    query_list = query_log.frames[timestamp]       # timestamp    
    
    # load expanding_list
    f = open(option.RESULT_PATH + "expanding_list.out", "r")
    expanding_list = cPickle.load(f)
    print "Load expanding_list: DONE"
    #
    num_edges_1 = 0 
    list_edges_1 = []
    
    directed_edges = [] #(u,v) in directed_edges: mesh(v) contains u
    
    full_edges = {} # dict of full edges (edge_id)
    part_edges = [] # list of partial edges (edge segment)
    full_edge_meshes = {}   # full_edge_meshes[e] = list of meshes containing e
    
    #1. PARALLEL
#    ppservers = ()
#    job_server = pp.Server(ppservers=ppservers)
#    print "Starting pp with", job_server.get_ncpus(), "workers"
#    
#    start_time = time.clock()    
#    
#    #1.1
#    expanding_groups = []
#    num_groups = 2
#    num_queries = len(expanding_list)
#    for i in range(num_groups):
#        start = i*num_queries/num_groups
#        end = (i+1)*num_queries/num_groups
#        if i == num_groups-1:
#            end = num_queries
#        
#        keys = list(query.obj_id for query in query_list[start:end])
#        expanding_group = dict((key, expanding_list[key]) for key in keys if key in expanding_list)
#        
#        expanding_groups.append(expanding_group)
#    
#    jobs = [(expanding_group, job_server.submit(parallelized_compute_partial_edges, 
#                (map_data, expanding_group,), 
#                (map_data.is_full_edge,), 
#                ("map_loader", "geom_util"))) for expanding_group in expanding_groups]
#    print "Submit jobs: DONE!"
#    
#    results = []
#    for expanding_group, job in jobs:
#        res = job()
#        print type(res)
#        results.append(res)
#        print "DONE one query group !"
#    
#    for expanding_group, res in zip(expanding_groups,results):    
#        for query, seg_list in zip(expanding_group, res):
#             
#    
#    job_server.print_stats()     
#    
#    #### TEMPORARY ############
#    #sort part_edges by e.cur_edge_id
#    sorted_part_edges = sorted(part_edges, key=lambda pair: pair[0].cur_edge_id)
#                
#    #1.2.
#    for query in query_list:
#        u = query.obj_id
#        #
#        if full_edge_meshes.has_key(query.cur_edge_id):
#            list_full = full_edge_meshes[query.cur_edge_id]
#            for v in list_full:
#                directed_edges.append((u,v))    #NOTE: maybe v = u
#                        
#        #
#        list_part = graph.find_partial_coverage(sorted_part_edges, query)
#        for v in list_part:
#            directed_edges.append((u,v))    #NOTE: maybe v = u    
#
#    print "directed_edges.len =", len(directed_edges)                
#    #sort
#    directed_edges.sort()
#    
#    #1.3. find undirected edge: both (u,v) and (v,u) from directed_edges
#    for pair in directed_edges:
#        if pair[0] < pair[1]:
#            u = pair[0]
#            v = pair[1]
#            if graph.is_reverse_existed(u, v, directed_edges):
#                num_edges_1 += 1
#                list_edges_1.append((u,v))     
#    
#    print "expanding_list_1 - elapsed : ", (time.clock() - start_time)        
     
        
    #2. NON-PARALLEL 
    num_edges_2 = 0 
    list_edges_2 = []
       
    start_time = time.clock()       
    #2.1.
    for (obj_id, mesh) in expanding_list.iteritems():
        for e in mesh:
            if map_data.is_full_edge(e) == True:   #FULL edge
                if not full_edges.has_key(e.cur_edge_id):
                    full_edge_meshes[e.cur_edge_id] = []
                full_edge_meshes[e.cur_edge_id].append(obj_id)
                    
            else:                                       #PARTIAL edge
                part_edges.append((e, obj_id))          # "list" of only one point mesh(obj_id) covers e
                
    #sort part_edges by e.cur_edge_id
    sorted_part_edges = sorted(part_edges, key=lambda pair: pair[0].cur_edge_id)
                
    #2.2.
    for query in query_list:
        u = query.obj_id
        #
        if full_edge_meshes.has_key(query.cur_edge_id):
            list_full = full_edge_meshes[query.cur_edge_id]
            for v in list_full:
                directed_edges.append((u,v))    #NOTE: maybe v = u
                        
        #
        list_part = graph.find_partial_coverage(sorted_part_edges, query)
        for v in list_part:
            directed_edges.append((u,v))    #NOTE: maybe v = u    

    print "directed_edges.len =", len(directed_edges)                
    #sort
    directed_edges.sort()
    
    #2.3. find undirected edge: both (u,v) and (v,u) from directed_edges
    for pair in directed_edges:
        if pair[0] < pair[1]:
            u = pair[0]
            v = pair[1]
            if graph.is_reverse_existed(u, v, directed_edges):
                num_edges_2 += 1
                list_edges_2.append((u,v))  

    print "expanding_list_2 - elapsed : ", (time.clock() - start_time)                    

    #3. CHECK
    print "Compare expanding_list_1 & expanding_list_2"
    print "num_edges_1 =", num_edges_1
    print "num_edges_2 =", num_edges_2
    
    
    print "Compare expanding_list(s): DONE"    

    #4. WRITE TO FILE
    write_compute_edge_list(list_edges_2)    

    print "Serialize edge_lists to file: DONE"
    
    #5. BUILD 'CONSTRAINT GRAPH'
    # write list_edges[] to file
    graph.write_list_edges(list_edges_2)
    #

    start_time = time.clock()        
#    # (OLD)    
#    graph.add_to_mc_set(list_edges)
     
    # (NEW)
    call([option.MACE_EXECUTABLE, "M", option.MAXIMAL_CLIQUE_FILE_IN, option.MAXIMAL_CLIQUE_FILE_OUT], 
         shell=False)

    f = open(option.MAXIMAL_CLIQUE_FILE_OUT, "r")
    fstr = f.read()
    f.close()
    
    mc_set = []
    for line in fstr.split("\n"):
        node_list = line.split(" ")
        if len(node_list) < 2:
            continue
        mc_set.append(set([int(node) for node in node_list]))
        
    print len(mc_set) 
    
    print "add_to_mc_set - elapsed : ", (time.clock() - start_time)   
    
    
    #6. WRITE mc_set TO FILE
    write_mc_set(mc_set)  
    
    print "Serialize mc_set to file: DONE"
    
    
    
    
    
    #X.
#    print "globals =", globals()
