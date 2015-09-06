'''
Created on Nov 21, 2012

@author: Nguyen Huu Hiep

use [sqrt(min_area) * total_street_len] as privacy constraint 
Dec-26: integrate 'weighted_set_cover.py'
'''

import sys
from datetime import datetime
import pp
import copy
import time
import math
import option
from subprocess import call


from query_loader import QueryLog
from map_loader import Map 
from geom_util import EdgeSegment, EdgeSegmentSet, Point, Query, CloakingSet, MBR, get_distance
from map_visualizer import MapVisualizer
from weighted_set_cover import find_init_cover, find_next_cover

class Graph:
    def __init__(self, num_user, map_data, query_log, map_visualizer):
        self.num_user = num_user
        self.map_data = map_data
        self.query_log = query_log
        self.map_visualizer = map_visualizer
        
        self.user_mc_set = {}   #dict of clique_id, e.g. user_mc_set[1] = 2 (clique_id = 2)
        self.mc_set = []        #maximal clique set, list of sets
        self.graph_edges = {}   #dict of pair (u,w) 
        self.last_query = {}    #dict of (user, last query)
        self.user_mesh = {}      #last cloaked (published) region
        #
        self.positive_mc_set = []   #list of sets
        self.old_user_mc_set = {}
        self.old_user_mesh = {}  
        #
        self.cover_set = []     # list of sets
        
    #######################################################        
    def reset(self):
        self.num_user = 0
        self.user_mc_set = {}   #dict of sets
        self.mc_set = []        #maximal clique set, list of sets 
        self.positive_mc_set = [] 
        #
        self.old_user_mc_set = self.user_mc_set
        self.old_user_mesh = self.user_mesh  


    #######################################################
    def print_mc_set(self):
        print self.mc_set
    
    
    #######################################################
    def init_nodes(self, user_list):
        for user in user_list:
            self.mc_set.append(set([user]))
            self.user_mc_set[user] = []
        
        
    #######################################################
    @staticmethod
    def intersect_mc_set(mc_set_1, mc_set_2):
        i_list = []
        for set_1 in mc_set_1:
            for set_2 in mc_set_2:
                interset = set_1 & set_2
                if len(interset) > 0:
                    i_list.append(interset)
        #
        cur = 0
        while cur < len(i_list):
            found = False
            for i in range(0,len(i_list)):
                if (i != cur and len(i_list[cur] - i_list[i]) == 0):    #[cur] is subset of [i]
                    i_list.pop(cur)
                    found = True
                    break
            if found == True:
                continue
            else:
                cur += 1
        
        #
#        print i_list        
            
        return i_list    
        
    #######################################################
    @staticmethod
    def check_mc_set(mc_set):
        for i in range(len(mc_set)):
            for j in range(i+1, len(mc_set)):
                if mc_set[i] == mc_set[j]:
                    print i,j,mc_set[i] 
                    return False
        return True
    
    
    #######################################################
    # u_edges: list of tuples (pairs) (u,xx)
    def add_to_mc_set(self, u_edges):
        
        
        for pair in u_edges:
            #add two edges
            self.graph_edges[(pair[0], pair[1])] = 1
            self.graph_edges[(pair[1], pair[0])] = 1
            #
            new_mc_set = []
            
            u = pair[0]
            w = pair[1]
            # 1.
            clique_u = []
            for set_u in self.mc_set:
                if u in set_u:
                    clique_u.append(set_u)
            
            clique_w = []
            for set_w in self.mc_set:
                if w in set_w:
                    clique_w.append(set_w)        

            
            #
            clique_inter = Graph.intersect_mc_set(clique_u, clique_w)
            
            # 2.
            if len(clique_inter) == 0:
                new_mc_set.append(set([u,w]))
                
            else:
                for set_inter in clique_inter:
                    new_clique = set_inter | set([u,w])
                    new_mc_set.append(new_clique)

            # 3.
            clique_union = clique_u
            for set_w in clique_w:
                found = False
                for set_u in clique_u:
                    if set_w == set_u:
                        found = True
                        break
                if found == False:
                    clique_union.append(set_w)
                
            #temp_mc_set = copy.deepcopy(new_mc_set)          #DEEP COPY !
            temp_mc_set = []
            
            for set_union in clique_union:
                is_subset = False
                for set_temp in new_mc_set:                 # changed: temp_mc_set -> new_mc_set   
                    if len(set_union - set_temp) == 0:      #set_union is NOT subset of set_temp
                        is_subset = True
                        break
                    
                if is_subset == False:
                    temp_mc_set.append(set_union)
            
            # NOT use DEEP COPY
            new_mc_set.extend(temp_mc_set)
            
            
            # 4.
            for set_neither in self.mc_set:
                if (u in set_neither) or (w in set_neither):
                    continue
                else:
                    new_mc_set.append(set_neither)
                    
                    
            #
            self.mc_set = new_mc_set    
                
        #end FOR
    
    
    
    #######################################################
    # u_nodes: list of nodes
    def remove_from_mc_set(self, u_nodes):
        
        
        for node in u_nodes:
            #remove edges
            for node_2 in range(option.MAX_USER):
                if self.graph_edges.has_key((node, node_2)):
                    del self.graph_edges[(node, node_2)]
                if self.graph_edges.has_key((node_2, node)):
                    del self.graph_edges[(node_2, node)]    
            #
            new_mc_set = []
            keep = []
            
            #1.
            for set_u in self.mc_set:
                set_u = set_u - set([node])
                new_mc_set.append(set_u)
                keep.append(True)
                
            #2.
            for u in range(0,len(new_mc_set)): 
                for w in range(0,len(new_mc_set)): 
                    if keep[w] == True and w != u:
                        if len(new_mc_set[u] - new_mc_set[w]) == 0:
                            keep[u] = False
                            break
            
            #3.
            self.mc_set = []     
            for u in range(0,len(new_mc_set)): 
                if keep[u] == True:
                    self.mc_set.append(new_mc_set[u])
                    
        #4. compute user_mc_set, max clique for each obj_id
        self.user_mc_set = {}
        
        for clique in self.mc_set:
            for obj_id in clique:
                if not self.user_mc_set.has_key(obj_id):
                    self.user_mc_set[obj_id] = clique
                elif len(self.user_mc_set[obj_id]) < len(clique):
                    self.user_mc_set[obj_id] = clique
        
    
    #######################################################
    def find_cloaking_sets(self, timestamp, expanding_list):
        
        #1. find positive and negative cliques
        self.positive_mc_set = []
        negative_mc_set = []
        for clique in self.mc_set:
            if len(clique) == 1:
                continue
            #
            max_min_length = 0
            max_k_anom = 0
            query_list = []
            for obj_id in clique:
                query = self.query_log.trajs[obj_id][timestamp]
                query_list.append(query)
                if max_min_length < query.min_length:
                    max_min_length = query.min_length
                if max_k_anom < query.k_anom:
                    max_k_anom = query.k_anom
                    
            #compute length of mesh
            mesh = []
            for obj_id in clique:
#                mesh = EdgeSegmentSet.union(mesh, expanding_list[obj_id])
                # NEW (trial)
                mesh.extend(expanding_list[obj_id])
            # NEW (trial)
            mesh = EdgeSegmentSet.clean_fixed_expanding(mesh)    


            clique_len = EdgeSegmentSet.length(mesh)
            
            #
            if len(clique) >= max_k_anom and \
                    clique_len >= max_min_length * self.map_data.total_map_len:
                self.positive_mc_set.append(clique) 
            elif len(clique) > 2:
                negative_mc_set.append(clique)
                
        #2.convert negative cliques (heuristically)
        new_negative_mc_set = []
        
        for clique in negative_mc_set:
            query_list = []
            for obj_id in clique:
                query = self.query_log.trajs[obj_id][timestamp]
                query_list.append(query)
            #sort
            query_list = sorted(query_list, key=lambda query: query.k_anom)
                
            while True:
                query_list.pop()    #remove the last
                if len(query_list) == 0:
                    break;
                max_min_length = max(query_list, key=lambda query: query.min_length).min_length
                #compute length of mesh
                mesh = []
                for query in query_list:
#                    mesh = EdgeSegmentSet.union(mesh, expanding_list[query.obj_id])
                    # NEW (trial)
                    mesh.extend(expanding_list[obj_id])
                # NEW (trial)
                mesh = EdgeSegmentSet.clean_fixed_expanding(mesh)    


                clique_len = EdgeSegmentSet.length(mesh)
                
                #
                if len(query_list) >= query_list[-1].k_anom and \
                        clique_len >= max_min_length * self.map_data.total_map_len:
                    break
            #
            if len(query_list) > 1:
                clique = set([query.obj_id for query in query_list])
                new_negative_mc_set.append(clique)    
                
        #3.
#        print "positive_mc_set =", self.positive_mc_set
#        print "new_negative_mc_set =", new_negative_mc_set 
        
        self.positive_mc_set.extend(new_negative_mc_set)
          
                
    #######################################################
    def solve_new_queries(self, timestamp):
        
        expanding_list = {}                                 #dict of lists
        query_list = self.query_log.frames[timestamp]       # timestamp
        
        
        #0. reset
        self.reset()
        
        #1. compute expanding_list
        start_time = time.clock()    
        for query in query_list:
            seg_list = self.map_data.compute_fixed_expanding(query.x, query.y, 
                                            query.cur_edge_id, option.DISTANCE_CONSTRAINT)
            seg_list = EdgeSegmentSet.clean_fixed_expanding(seg_list)
            
            expanding_list[query.obj_id] = seg_list
        print "expanding_list - elapsed : ", (time.clock() - start_time)       
        
        #2. compute mc_set
        start_time = time.clock()
        (num_edges, list_edges) = self.compute_edge_list(expanding_list, query_list)
        print "num_edges=", num_edges
        print "list_edges NEW - elapsed : ", (time.clock() - start_time)       
        
        # write list_edges[] to file
        self.write_list_edges(list_edges)
        #
    
        start_time = time.clock()        
#        # (OLD)    
#        graph.add_to_mc_set(list_edges)
         
        # (NEW)
        call([option.MACE_EXECUTABLE, "M", option.MAXIMAL_CLIQUE_FILE_IN, option.MAXIMAL_CLIQUE_FILE_OUT], 
             shell=False)

        f = open(option.MAXIMAL_CLIQUE_FILE_OUT, "r")
        fstr = f.read()
        f.close()
        for line in fstr.split("\n"):
            node_list = line.split(" ")
            if len(node_list) < 2:
                continue
            self.mc_set.append(set([int(node) for node in node_list]))
            
        print len(self.mc_set) 
        
        print "add_to_mc_set - elapsed : ", (time.clock() - start_time)   
#        print "mc_set =", self.mc_set
        
        #3.
        start_time = time.clock() 
        self.find_cloaking_sets(timestamp, expanding_list)
        
        print "find_cloaking_sets - elapsed : ", (time.clock() - start_time)
        
        
        #4. 'Set Cover Problem' (from weighted_set_cover.py)
        start_time = time.clock() 
        
        num_element = max(query_list, key=lambda query: query.obj_id).obj_id + 1  # avoid out of range  
        if timestamp == 0:
            self.cover_set, num_cloaked_users = find_init_cover(self.positive_mc_set, num_element)
        else:
            self.cover_set, num_cloaked_users = find_next_cover(self.positive_mc_set, num_element, self.cover_set)
            
        print "Success rate =", float(num_cloaked_users)/len(query_list)   
        print "compute cover_set - elapsed : ", (time.clock() - start_time)
        
        #5. compute user_mc_set (max clique for each obj_id), replace self.positive_mc_set by self.cover_set
        start_time = time.clock()   
        self.user_mc_set = {}
        for clique_id in range(len(self.cover_set)):
            clique = self.cover_set[clique_id]
            for obj_id in clique:
                #
                if not self.user_mc_set.has_key(obj_id):
                    self.user_mc_set[obj_id] = clique_id            #use id
                elif len(self.cover_set[self.user_mc_set[obj_id]]) < len(clique):
                    self.user_mc_set[obj_id] = clique_id               #store the maximum
        
        print "Compute user_mc_set - elapsed : ", (time.clock() - start_time)   
#        print "user_mc_set = ", self.user_mc_set
                    
        #6. compute CLOAKING MESH
        start_time = time.clock()   
        total_mesh_length = 0
        total_query = 0
        for clique_id in range(len(self.cover_set)):
            clique = self.cover_set[clique_id]
            #compute length of mesh
            mesh = []
            for obj_id in clique:
                mesh = EdgeSegmentSet.union(mesh, expanding_list[obj_id])
            
            total_mesh_length += EdgeSegmentSet.length(mesh)
            total_query += len(clique)    
            #
            for obj_id in clique:
                if self.user_mc_set[obj_id] == clique_id:  #clique id comparison
                    self.user_mesh[obj_id] = mesh
        
        average_mesh_query = total_mesh_length/total_query
        
        print "total_mesh_length =", total_mesh_length    
        print "average_mesh_query =", average_mesh_query    
        print "Compute CLOAKING MBR - elapsed : ", (time.clock() - start_time)  
#        print "user_mesh = ", self.user_mesh
                
        #6. publish MBRs (write to file)   
        start_time = time.clock()   
        self.write_results_to_files(timestamp)
        
        print "write_results_to_files - elapsed : ", (time.clock() - start_time)  
        
        
    #######################################################
    def write_results_to_files(self, timestamp):
        
        config_name = option.QUERY_FILE[0:-4] + "-" + str(option.DISTANCE_CONSTRAINT) + \
            "-" + str(option.MAX_SPEED)
        
#        #1. self.positive_mc_set
#        f = open(option.RESULT_PATH + "/" + config_name + "_positive_mc_set" + "_" + str(timestamp) + ".out", "w")
#        for clique in self.positive_mc_set:
#            for obj_id in clique:
#                f.write("%d,"%obj_id)
#            f.write("\n")
#        f.close()    
        
        #1. self.cover_set
        f = open(option.RESULT_PATH + "/" + config_name + "_cover_set" + "_" + str(timestamp) + ".out", "w")
        for clique in self.cover_set:
            for obj_id in clique:
                f.write("%d,"%obj_id)
            f.write("\n")
        f.close()    
            
        #2. self.user_mc_set
        f = open(option.RESULT_PATH + "/" + config_name + "_user_mc_set" + "_" + str(timestamp) + ".out", "w")
        for (obj_id, clique_id) in self.user_mc_set.iteritems():
            f.write("%d %d\n"%(obj_id,clique_id))
        f.close()     
        
        #3. self.user_mesh
        f = open(option.RESULT_PATH + "/" + config_name + "_user_mesh" + "_" + str(timestamp) + ".out", "w")
        for (obj_id, mesh) in self.user_mesh.iteritems():
            f.write("%d\n"%obj_id)
            for seg in mesh:
                f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
            f.write("\n")            
        f.close()     
        
    
    #######################################################  
    # copied from EdgeSegmentSet.is_set_cover()  
    def find_partial_coverage(self, part_edges, query):
        if len(part_edges) == 0:
            return []
        
        #binary search
        lo = 0
        hi = len(part_edges) - 1
        mid = (lo + hi) / 2
        found = False
        while True:
            if part_edges[mid][0].cur_edge_id == query.cur_edge_id:
                found = True
                break
            if part_edges[mid][0].cur_edge_id > query.cur_edge_id:
                hi = mid - 1
                if hi < lo: 
                    break
            else:
                lo = mid + 1
                if lo > hi:
                    break
            mid = (lo + hi) / 2    
        
        if found == False:
            return []
        
        #
        lo = mid
        while lo-1 > 0 and part_edges[lo-1][0].cur_edge_id == query.cur_edge_id:
            lo = lo - 1
        hi = mid
        while hi+1 < len(part_edges) and part_edges[hi+1][0].cur_edge_id == query.cur_edge_id:
            hi = hi + 1
        
        result = []
        for pair in part_edges[lo:hi+1]:
            if EdgeSegment.is_line_cover(Point(query), pair[0]) == True:
                result.append(pair[1])
        
        return result
    
        
    #######################################################    
    def is_reverse_existed(self, u, v, directed_edges):     #check (v,u) in directed_edges
        #binary search
        lo = 0
        hi = len(directed_edges) - 1
        mid = (lo + hi) / 2
        found = False
        while True:
            if directed_edges[mid][0] == v:
                found = True
                break
            if directed_edges[mid][0] > v:
                hi = mid - 1
                if hi < lo: 
                    break
            else:
                lo = mid + 1
                if lo > hi:
                    break
            mid = (lo + hi) / 2    
        
        if found == False:
            return False
        
        #
        lo = mid
        while lo-1 > 0 and directed_edges[lo-1][0] == v:
            lo = lo - 1
        hi = mid
        while hi+1 < len(directed_edges) and directed_edges[hi+1][0] == v:
            hi = hi + 1
        
        for pair in directed_edges[lo:hi+1]:
            if pair[1] == u: return True
        
        return False    
    
    #######################################################    
    def compute_edge_list(self, expanding_list, query_list):
        num_edges = 0 
        list_edges = []
        
        directed_edges = [] #(u,v) in directed_edges: mesh(v) contains u
        
        full_edges = {} # dict of full edges (edge_id)
        part_edges = [] # list of partial edges (edge segment)
        full_edge_meshes = {}   # full_edge_meshes[e] = list of meshes containing e
        
        #1.
        for (obj_id, mesh) in expanding_list.iteritems():
            for e in mesh:
                if self.map_data.is_full_edge(e) == True:   #FULL edge
                    if not full_edges.has_key(e.cur_edge_id):
                        full_edge_meshes[e.cur_edge_id] = []
                    full_edge_meshes[e.cur_edge_id].append(obj_id)
                        
                else:                                       #PARTIAL edge
                    part_edges.append((e, obj_id))          # "list" of only one point mesh(obj_id) covers e
                    
        #sort part_edges by e.cur_edge_id
        sorted_part_edges = sorted(part_edges, key=lambda pair: pair[0].cur_edge_id)
                    
        #2.
        for query in query_list:
            u = query.obj_id
            #
            if full_edge_meshes.has_key(query.cur_edge_id):
                list_full = full_edge_meshes[query.cur_edge_id]
                for v in list_full:
                    directed_edges.append((u,v))    #NOTE: maybe v = u
                            
            #
            list_part = self.find_partial_coverage(sorted_part_edges, query)
            for v in list_part:
                directed_edges.append((u,v))    #NOTE: maybe v = u    

        print "directed_edges.len =", len(directed_edges)                
        #sort
        directed_edges.sort()
        
        #3. find undirected edge: both (u,v) and (v,u) from directed_edges
        for pair in directed_edges:
            if pair[0] < pair[1]:
                u = pair[0]
                v = pair[1]
                if self.is_reverse_existed(u, v, directed_edges):
                    num_edges += 1
                    list_edges.append((u,v))
                    
        return (num_edges, list_edges)    
        
        
        
    #######################################################    
    def write_list_edges(self, list_edges):
        #
        max_edge_id = max(list_edges, key=lambda e: e[0])[0]
        max_edge_id = max(max_edge_id, max(list_edges, key=lambda e: e[1])[1])
        
        list_adj = [[] for i in range(max_edge_id+1)]   #list of lists
        for e in list_edges:
            list_adj[e[0]].append(e[1])
         
        #    
        f = open(option.MAXIMAL_CLIQUE_FILE_IN, 'w')
        for u in range(max_edge_id+1):
            if len(list_adj[u]) > 0:
                for v in list_adj[u]:
                    f.write("%d,"%v)
            f.write("\n")    
            
        f.close()
    
    #######################################################
    def check_published_mcset_mesh(self, user_mc_set, user_mesh, timestamp):
        
        query_list = self.query_log.frames[timestamp] 
        query_dict = {}
        for query in query_list:
            query_dict[query.obj_id] = query
            
        #
        processed = {}
        for user_u in user_mc_set.iterkeys():
            if not processed.has_key(user_u): #unprocessed clique
                max_min_length = 0
                max_k_anom = 0
                for obj_id in user_mc_set[user_u]:
                    processed[obj_id] = 1
                    if max_min_length < query_dict[obj_id].min_length:
                        max_min_length = query_dict[obj_id].min_length
                    if max_k_anom < query_dict[obj_id].k_anom:
                        max_k_anom = query_dict[obj_id].k_anom
                if max_k_anom > len(user_mc_set[user_u]) or \
                    max_min_length*self.map_data.area > user_mesh[user_u]:
                    return False
        #
        return True
        
        
        
    
    #######################################################
    def run_timestamps(self, start_timestamp, end_timestamp):
        for timestamp in range (start_timestamp, end_timestamp+1):
            print "--------->>"
            print "TIMESTAMP : ", timestamp
            print "self.num_user = ", len(self.query_log.frames[timestamp])
            
            self.solve_new_queries(timestamp)
            
#            print "check_published_mcset_mbr", \
#                self.check_published_mcset_mesh(self.user_mc_set, self.user_mesh, timestamp)
            
            #print "len(graph.mc_set) = ", len(graph.mc_set)
            #print graph.mc_set
        


#######################################################
# PARALLEL ROUTINES
#######################################################
def parallelized_compute_expanding_list(map_data, edge_segment_set, query_group, init_graph_distance):
    result = []
    for query in query_group:
        seg_list = map_data.compute_fixed_expanding(query.x, query.y, 
                                            query.cur_edge_id, init_graph_distance)
        seg_list = edge_segment_set.clean_fixed_expanding(seg_list)
        result.append(seg_list)
    
    return result   


def mapped_mem_find_positive_negative_cliques(timestamp, total_map_len, edge_segment_set, 
                                               clique_group, query_file_name):
    positive_mc_set = []
    negative_mc_set = []
    
    edge_segment_set.show_sys_time("START mapped_mem_find - Now: ")  
    # use mmap to map trajs and expanding_list
    #1.
    trajs = edge_segment_set.parse_query_log(timestamp, query_file_name)
    
    expanding_list = edge_segment_set.parse_expanding_list()
    

    edge_segment_set.show_sys_time("START find_positive_negative - Now: ")
    #2. 
    for clique in clique_group:
        clique_type = edge_segment_set.find_clique_type(clique, trajs, expanding_list, 
                                                        total_map_len) 
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
            max_min_length = max(query_list, key=lambda query: query.min_length).min_length
            #compute length of mesh
            mesh = []
            for query in query_list:
#                mesh = edge_segment_set.union(mesh, expanding_list[query.obj_id])
                # NEW (trial)
                mesh.extend(expanding_list[obj_id])
            # NEW (trial)
            mesh = edge_segment_set.clean_fixed_expanding(mesh)    


            clique_len = edge_segment_set.length(mesh)
            
            #
            if len(query_list) >= query_list[-1].k_anom and \
                    clique_len >= max_min_length * total_map_len:
                break
        #
        if len(query_list) > 1:
            clique = set([query.obj_id for query in query_list])
            new_negative_mc_set.append(clique)  
    
    positive_mc_set.extend(new_negative_mc_set)
    
    edge_segment_set.show_sys_time("END find_positive_negative - Now: ")
    
    # RETURN
    return positive_mc_set, len_positive, len_negative             
            
            
def write_compute_expanding_list(expanding_list):
    f = open(option.RESULT_PATH + "expanding_list.txt", "w")
    for (obj_id, seg_list) in expanding_list.iteritems():
        f.write("%d\n"%obj_id)
        for seg in seg_list:
            # original --> imprecise find_cloaking_sets
#            f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
            
            # better  
            f.write("%f,%f,%f,%f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))

            # (worse than the second)   
#            f.write(str(seg.start_x)+",")
#            f.write(str(seg.start_y)+",")
#            f.write(str(seg.end_x)+",")
#            f.write(str(seg.end_y)+",")
#            f.write("%d:"%seg.cur_edge_id)
 
        f.write("\n")            
    f.close()               
            
   
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    
    # Command-line param for automation (bash)
    timestep = 3
    num_groups = 2  # = ncpus
    if len(sys.argv) > 4:
        option.QUERY_FILE = sys.argv[1]
        timestep = int(sys.argv[2])
        option.DISTANCE_CONSTRAINT = int(sys.argv[3])
        num_groups = int(sys.argv[4])
    #   
    
        
    start_time = time.clock()
        
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    query_log = QueryLog(map_data)
    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, max_time_stamp = 5)    
    print "Load Query : DONE"
    
    print "max_speed = ", query_log.max_speed
    print "elapsed : ", (time.clock() - start_time)   
    
    #TEST    
    graph = Graph(0, map_data, query_log, None)
#    graph.run_timestamps(0,2)
#    print "graph.run_timestamps - DONE"

    # PREPARE
    edge_segment_set = EdgeSegmentSet()
    
    
    ppservers = ()
    job_server = pp.Server(ncpus = num_groups, ppservers=ppservers)
    print "Starting pp with", job_server.get_ncpus(), "workers"

    #LOOP
    for timestamp in range(0,timestep+1):
        print "--------->>"
        print "TIMESTAMP : ", timestamp
        
        print "START - Now: ", datetime.now()
        
        #0. reset
        graph.reset()
        
        expanding_list = {}                                 #dict of lists
        query_list = query_log.frames[timestamp]       # timestamp    
        
        # 1. COMPUTE_EXPANDING_LIST
        start_time = time.clock()    
            
        query_groups = []
        num_queries = len(query_list)
        end = 0     # for special case num_groups = 1
        for i in range(num_groups-1):
            start = i*num_queries/num_groups
            end = (i+1)*num_queries/num_groups
            query_groups.append(query_list[start:end])
        query_groups.append(query_list[end:num_queries])
        
        for query_group in query_groups:
            print "len(query_group) =", len(query_group)
        
            
        jobs = [(query_group, job_server.submit(parallelized_compute_expanding_list, 
                    (map_data, edge_segment_set, query_group, option.DISTANCE_CONSTRAINT,), 
                    (map_data.compute_fixed_expanding,edge_segment_set.clean_fixed_expanding,), 
                    ("map_loader", "geom_util"))) for query_group in query_groups]
        print "Submit jobs: DONE! - elapsed : ", (time.clock() - start_time)
        
        results = []
        for query_group, job in jobs:
            res = job()
            results.append(res)
            print "DONE one query group !"
        
        for query_group, res in zip(query_groups,results):    
            for query, seg_list in zip(query_group, res):
                expanding_list[query.obj_id] = seg_list 
        
        job_server.print_stats()        
        
        print "expanding_list - elapsed : ", (time.clock() - start_time)
        print "expanding_list - Now: ", datetime.now()  
    
        # READ in by EdgeSegmentSet.parse_expanding_list() in mapped_mem_find_positive_negative_cliques()
        write_compute_expanding_list(expanding_list)
        
        
        # 2. COMPUTE EDGE LIST
        start_time = time.clock()
        (num_edges, list_edges) = graph.compute_edge_list(expanding_list, query_list)
        print "num_edges=", num_edges
        print "list_edges NEW - elapsed : ", (time.clock() - start_time)       
        
        # write list_edges[] to file
        graph.write_list_edges(list_edges)
        #
        print "list_edges - Now: ", datetime.now() 
    
        # call MACE_GO (Tomita et al.)
        start_time = time.clock()        
        call([option.MACE_EXECUTABLE, "M", option.MAXIMAL_CLIQUE_FILE_IN, option.MAXIMAL_CLIQUE_FILE_OUT], 
             shell=False)

        f = open(option.MAXIMAL_CLIQUE_FILE_OUT, "r")
        fstr = f.read()
        f.close()
        for line in fstr.split("\n"):
            node_list = line.split(" ")
            if len(node_list) < 2:
                continue
            graph.mc_set.append(set([int(node) for node in node_list]))
            
        print len(graph.mc_set) 
        
        print "add_to_mc_set - elapsed : ", (time.clock() - start_time) 
    
        print "add_to_mc_set - Now: ", datetime.now() 
    
        # 3. FIND_CLOAKING_SETS
        positive_mc_set = []
    
        #1.1 find positive and negative cliques
        total_map_len = map_data.total_map_len
        
        clique_groups = []
        num_cliques = len(graph.mc_set)
        
        # WAY-1
#        for i in range(num_groups-1):
#            start = i*num_cliques/num_groups
#            end = (i+1)*num_cliques/num_groups
#            clique_groups.append(graph.mc_set[start:end])
#        clique_groups.append(graph.mc_set[end:num_cliques]) 
        
        # WAY-2, interleave mc_set[] to clique_groups
        for i in range(num_groups):
            clique_groups.append([])
        for i in range(num_cliques):
            clique_groups[i % num_groups].append(graph.mc_set[i]) 
            
        for clique_group in clique_groups:
            print "len(clique_groups) =", len(clique_group)   
        
        # 
        jobs = [job_server.submit(mapped_mem_find_positive_negative_cliques, 
                    (timestamp, total_map_len, edge_segment_set, clique_group, option.QUERY_FILE,), 
                    (edge_segment_set.parse_query_log, edge_segment_set.parse_expanding_list, 
                     edge_segment_set.find_clique_type,edge_segment_set.show_sys_time,), 
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
                graph.positive_mc_set.append(clique)
            print "sub-positive =", res[1], "sub-negative =", res[2]     
            num_positive += res[1]
            num_negative += res[2]    
        
        print "num_positive =", num_positive
        print "num_negative =", num_negative
        
        job_server.print_stats()      
        
        print "find positive/negative cliques - elapsed : ", (time.clock() - start_time)  
        print "len(positive_mc_set_1) =", len(graph.positive_mc_set)
        print "find positive/negative cliques - Now: ", datetime.now() 
        
        # 4. 'SET COVER PROBLEM' (from weighted_set_cover.py)
        start_time = time.clock() 
        
        num_element = max(query_list, key=lambda query: query.obj_id).obj_id + 1  # avoid out of range  
        if timestamp == 0:
            graph.cover_set, num_cloaked_users = find_init_cover(graph.positive_mc_set, num_element)
        else:
            graph.cover_set, num_cloaked_users = find_next_cover(graph.positive_mc_set, num_element, graph.cover_set)
            
        print "Success rate =", float(num_cloaked_users)/len(query_list)       
        print "compute cover_set - elapsed : ", (time.clock() - start_time)
        print "cover_set - Now: ", datetime.now() 
        
        #5. COMPUTE USER_MC_SET (max clique for each obj_id), replace self.positive_mc_set by self.cover_set
        start_time = time.clock()   
        graph.user_mc_set = {}
        for clique_id in range(len(graph.cover_set)):
            clique = graph.cover_set[clique_id]
            for obj_id in clique:
                #
                if not graph.user_mc_set.has_key(obj_id):
                    graph.user_mc_set[obj_id] = clique_id            #use id
                elif len(graph.cover_set[graph.user_mc_set[obj_id]]) < len(clique):
                    graph.user_mc_set[obj_id] = clique_id               #store the maximum
        
        print "Compute user_mc_set - elapsed : ", (time.clock() - start_time)   
        print "user_mc_set - Now: ", datetime.now() 
        
#        print "user_mc_set = ", self.user_mc_set
                    
        # 6. COMPUTE CLOAKING MESH
        start_time = time.clock()   
        total_mesh_length = 0
        total_query = 0
        for clique_id in range(len(graph.cover_set)):
            clique = graph.cover_set[clique_id]
            #compute length of mesh
            mesh = []
            for obj_id in clique:
                mesh = edge_segment_set.union(mesh, expanding_list[obj_id])
            
            total_mesh_length += edge_segment_set.length(mesh)
            total_query += len(clique)    
            #
            for obj_id in clique:
                if graph.user_mc_set[obj_id] == clique_id:  #clique id comparison
                    graph.user_mesh[obj_id] = mesh
        
        average_mesh_query = total_mesh_length/total_query
        
        print "total_mesh_length =", total_mesh_length    
        print "average_mesh_query =", average_mesh_query    
        print "Compute CLOAKING MBR - elapsed : ", (time.clock() - start_time)  
        print "CLOAKING MBR - Now: ", datetime.now() 
#        print "user_mesh = ", self.user_mesh
                
        #6. publish MBRs (write to file)   
#        start_time = time.clock()   
#        graph.write_results_to_files(timestamp)
#        
#        print "write_results_to_files - elapsed : ", (time.clock() - start_time)  
        
        
        print "END - Now: ", datetime.now()
    
    # END OF LOOP
    
    
    
    
    
    
    
    
    
    
        
        
    

    
    
    
    