'''
Created on Nov 21, 2012

@author: Nguyen Huu Hiep

use [sqrt(min_area) * total_street_len] as privacy constraint 
Dec-26:    integrate 'weighted_set_cover.py'
Jan-10:  
Jan-15:    replace [sqrt(min_area) * total_street_len] by [min_length * total_street_len]
Apr-10:    write_results_to_files(): write 
Nov-29:    BUG: need to reset self.user_mesh to {} in each timestep
'''


import sys
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
        self.cover_mesh = []    # list of meshes, 
                                # checked agaist option.S_GLOBAL at the end of solve_new_queries() 
        self.cover_mesh_mmb = []
        #
        self.new_cover_set = []     # list of sets
        self.new_cover_mesh = []    # list of meshes, 
        self.new_cover_mesh_mmb = []
        
    #######################################################        
    def reset(self):
        self.num_user = 0
        self.user_mc_set = {}   #dict of sets
        self.mc_set = []        #maximal clique set, list of sets 
        #
        self.old_user_mc_set = self.user_mc_set
        self.old_user_mesh = self.user_mesh  
        #
        self.user_mesh = {}

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
                    mesh.extend(expanding_list[query.obj_id])
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
    def compute_cover_mesh_mmb(self, cover_mesh, expanding_list):
        
        start_time = time.clock()
        # prepare MMB
        cover_mesh_mmb = []
        for cover_id in range(len(cover_mesh)):
            mesh = self.map_data.compute_mesh_expanding(cover_mesh[cover_id], option.MAX_SPEED)
            cover_mesh_mmb.append(mesh)
            if cover_id % 100 == 0:
                print "cover_id =", cover_id
        print "compute_cover_mesh_mmb MMB - elapsed : ", (time.clock() - start_time)  
        
        return cover_mesh_mmb 
    
    #######################################################
    def check_MMB_MAB(self, checking_pairs, cover_mesh, cover_mesh_mmb, new_cover_mesh, new_cover_mesh_mmb,):
        
#        start_time = time.clock()
#        # prepare MAB
#        positive_mesh = []
#        for (pos_id, check_list) in checking_pairs.iteritems():
#            if len(check_list) == 0:
#                positive_mesh.append(0)
#                continue
#            mesh = []
#            for obj_id in positive_mc_set[pos_id]:   
#                mesh.extend(expanding_list[obj_id])
#            mesh = EdgeSegmentSet.clean_fixed_expanding(mesh)
#            positive_mesh.append(mesh)
#        print "prepare MAB - Step 1 - elapsed : ", (time.clock() - start_time)
#            
#        start_time = time.clock()    
#        positive_mab = []     
#        count_id = 0
#        for (pos_id, check_list) in checking_pairs.iteritems():
#            if len(check_list) == 0:
#                continue     
#            # compute MAB
#            mesh = self.map_data.compute_mesh_expanding(positive_mesh[pos_id], option.MAX_SPEED)
#            #
#            positive_mab.append(mesh)
#            
#            count_id += 1
#            if count_id % 100 == 0:
#                print "count_id =", count_id
#            
#        print "prepare MAB - Step 2 - elapsed : ", (time.clock() - start_time) 
           
        #
        start_time = time.clock()
        count_pair = 0
        for (pos_id, check_list) in enumerate(checking_pairs):
            if len(check_list) == 0:
                print "ERROR in checking_pairs at pos_id =", pos_id # CASE inter_len = 0 in find_next_cover()
                continue
            
            for old_cover_id in check_list:
                # 1. MMB
                new_mesh = new_cover_mesh[pos_id]
                old_mesh_mmb = cover_mesh_mmb[old_cover_id]
                #
                inter_set = EdgeSegmentSet.intersect(old_mesh_mmb, new_mesh)
                if len(inter_set) > 0 and len(inter_set) < option.S_GLOBAL:
                    print "MMB FAULT at (pos_id, old_cover_id):", pos_id, old_cover_id
                    
                # 2. MAB
                new_mesh_mab = new_cover_mesh_mmb[pos_id]
                old_mesh = cover_mesh[old_cover_id]
                #
                inter_set = EdgeSegmentSet.intersect(old_mesh, new_mesh_mab)
                
                if len(inter_set) > 0 and len(inter_set) < option.S_GLOBAL:
                    print "MAB FAULT at (pos_id, cover_id):", pos_id, old_cover_id
            #
            count_pair += 1
            if count_pair % 100 == 0:
                print "count_pair =", count_pair
                
        print "check MMB/MAB - elapsed : ", (time.clock() - start_time)         
              
                
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
                                            query.cur_edge_id, query.dist)  #old: option.DISTANCE_CONSTRAINT
            seg_list = EdgeSegmentSet.clean_fixed_expanding(seg_list)
            
            expanding_list[query.obj_id] = seg_list
        print "expanding_list - elapsed : ", (time.clock() - start_time)       
        
        #2. compute mc_set
#        start_time = time.clock()    
#        num_edges = 0 
#        list_edges = []
#        for i in range(len(query_list)):
#            for j in range(i+1,len(query_list)):
#                if get_distance(query_list[i].x, query_list[i].y, query_list[j].x, query_list[j].y) > \
#                    option.INIT_GRAPH_DISTANCE:
#                    continue
#                
#                if EdgeSegmentSet.is_set_cover(Point(query_list[i]), expanding_list[query_list[j].obj_id]) and \
#                    EdgeSegmentSet.is_set_cover(Point(query_list[j]), expanding_list[query_list[i].obj_id]):
#                    num_edges += 1
#                    list_edges.append((query_list[i].obj_id, query_list[j].obj_id))
#                    
#        print "num_edges=", num_edges
#        print "list_edges OLD - elapsed : ", (time.clock() - start_time)  
        
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
            self.new_cover_set = self.cover_set     # for compute CLOAKING MESH
        else:
            self.new_cover_set, num_cloaked_users, checking_pairs = find_next_cover(self.positive_mc_set, num_element, self.cover_set, option.K_GLOBAL)
        
        print "Success rate =", float(num_cloaked_users)/len(query_list)    
        print "compute cover_set - elapsed : ", (time.clock() - start_time)
        
        
        #5. compute CLOAKING MESH
        start_time = time.clock()   
        
        total_mesh_length = 0
        total_query = 0
        self.new_cover_mesh = []    # NEW
        for clique_id in range(len(self.new_cover_set)):
            clique = self.new_cover_set[clique_id]
            #compute length of mesh
            mesh = []
            for obj_id in clique:
                mesh = EdgeSegmentSet.union(mesh, expanding_list[obj_id])
            
            self.new_cover_mesh.append(mesh)    #NEW           
            
            total_mesh_length += EdgeSegmentSet.length(mesh)
            total_query += len(clique)    
            
        
        average_mesh_query = total_mesh_length/total_query
        
        print "total_mesh_length =", total_mesh_length    
        print "average_mesh_query =", average_mesh_query    
        print "Compute CLOAKING MBR - elapsed : ", (time.clock() - start_time)  
#        print "user_mesh = ", self.user_mesh
                
                
        #5.2 Check MMB/MAB
#        self.new_cover_mesh_mmb = self.compute_cover_mesh_mmb(self.new_cover_mesh, expanding_list)
#            
#        if timestamp > 0:
#            start_time = time.clock() 
#            
#            self.check_MMB_MAB(checking_pairs, self.cover_mesh, self.cover_mesh_mmb, self.new_cover_mesh, self.new_cover_mesh_mmb)
#            
#            print "check_MMB_MAB() - elapsed : ", (time.clock() - start_time)
        
        # UPDATE
        self.cover_set = self.new_cover_set
        self.cover_mesh = self.new_cover_mesh
#        self.cover_mesh_mmb = self.new_cover_mesh_mmb        
        
        
        #6. compute user_mc_set (max clique for each obj_id), replace self.positive_mc_set by self.cover_set
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
            #
            for obj_id in clique:
                if self.user_mc_set[obj_id] == clique_id:  #clique id comparison
                    self.user_mesh[obj_id] = self.cover_mesh[clique_id]        
        
        print "Compute user_mc_set - elapsed : ", (time.clock() - start_time)   
#        print "user_mc_set = ", self.user_mc_set
        
                
        #7. publish MBRs (write to file)   
        start_time = time.clock()   
        self.write_results_to_files(timestamp)
        
        print "write_results_to_files - elapsed : ", (time.clock() - start_time)  
        
        
    #######################################################
    def write_results_to_files(self, timestamp):
        
        config_name = option.QUERY_FILE[0:-4] + "-" + str(option.DISTANCE_CONSTRAINT) + \
            "-" + str(option.MAX_SPEED)
        
#        #0. self.positive_mc_set
#        f = open(option.RESULT_PATH + "/" + config_name + "_positive_mc_set" + "_" + str(timestamp) + ".out", "w")
#        for clique in self.positive_mc_set:
#            for obj_id in clique:
#                f.write("%d,"%obj_id)
#            f.write("\n")
#        f.close()    
#        
        #1. self.cover_set
        f = open(option.RESULT_PATH + "/" + config_name + "_" + str(option.K_GLOBAL) + "_" +
                  str(option.INIT_COVER_KEEP_RATIO) + "_cover_set" + "_" + str(timestamp) + ".out", "w")
        for clique in self.cover_set:
            for obj_id in clique:
                f.write("%d,"%obj_id)
            f.write("---")
            for obj_id in clique:
                f.write("%d,"%self.query_log.trajs[obj_id][timestamp].k_anom)    
            f.write("\n")
        f.close()    
            
        #2. self.user_mc_set
#        f = open(option.RESULT_PATH + "/" + config_name + "_user_mc_set" + "_" + str(timestamp) + ".out", "w")
#        for (obj_id, clique_id) in self.user_mc_set.iteritems():
#            f.write("%d %d\n"%(obj_id,clique_id))
#        f.close()     
        
        #3. self.user_mesh
#        f = open(option.RESULT_PATH + "/" + config_name + "_user_mesh" + "_" + str(timestamp) + ".out", "w")
#        for (obj_id, mesh) in self.user_mesh.iteritems():
#            f.write("%d\n"%obj_id)
#            for seg in mesh:
#                f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
#            f.write("\n")            
#        f.close()     

        #4. self.cover_mesh
#        f = open(option.RESULT_PATH + "/" + config_name + "_cover_mesh" + "_" + str(timestamp) + ".out", "w")
#        for mesh in self.cover_mesh:
#            mbr = EdgeSegmentSet.compute_mbr(mesh)
#            f.write("%.2f,%.2f,%.2f,%.2f,%.2f\n"%(mbr.area, mbr.min_x, mbr.min_y, mbr.max_x, mbr.max_y))
#            for seg in mesh:
#                f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
#            f.write("\n")            
#        f.close()         
        
        #5. self.user_mesh (only print edge_id) for attacks (in trace_generator)
        f = open(option.RESULT_PATH + "/" + config_name + "_edge_cloaking_" + str(timestamp) + ".out", "w")
        for (obj_id, mesh) in self.user_mesh.iteritems():
            cur_edge_id = self.query_log.trajs[obj_id][timestamp].cur_edge_id
            f.write("%d-%d\n"%(obj_id, cur_edge_id))
            for seg in mesh:
                f.write("%d,"%seg.cur_edge_id)
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
        
        part_edges = [] # list of partial edges (edge segment)
        full_edge_meshes = {}   # full_edge_meshes[e] = list of meshes containing e
        
        #1.
        for (obj_id, mesh) in expanding_list.iteritems():
            for e in mesh:
                if self.map_data.is_full_edge(e) == True:   #FULL edge
                    if not full_edge_meshes.has_key(e.cur_edge_id):
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
            start_time = time.clock()
            print "--------->>"
            print "TIMESTAMP : ", timestamp
            print "self.num_user = ", len(self.query_log.frames[timestamp])
            
            self.solve_new_queries(timestamp)
            
            print "Total time elapsed :", time.clock() - start_time
#            print "check_published_mcset_mbr", \
#                self.check_published_mcset_mesh(self.user_mc_set, self.user_mesh, timestamp)
            
            #print "len(graph.mc_set) = ", len(graph.mc_set)
            #print graph.mc_set
        
        
            
   
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    
    
#    mc_set_1 = [set([1,2,3]), set([1,3,4])]
#    mc_set_2 = [set([1,5,6]), set([1,4,5]), set([1,3,4])]
#    
#    Graph.intersect_mc_set(mc_set_1, mc_set_2)

    # Command-line param for automation (bash)
    timestep = 3
#    timestep = 40       # for lbs_attack
    if len(sys.argv) > 3:
        option.QUERY_FILE = sys.argv[1]
        timestep = int(sys.argv[2])
        option.DISTANCE_CONSTRAINT = int(sys.argv[3])

    if len(sys.argv) > 4:
        option.K_GLOBAL = int(sys.argv[4])    
        
    if len(sys.argv) > 5:
        option.INIT_COVER_KEEP_RATIO = float(sys.argv[5])     
    
    if len(sys.argv) > 6:
        option.NEXT_COVER_KEEP_RATIO = float(sys.argv[6])       

    #    
    start_time = time.clock()
        
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    query_log = QueryLog(map_data)
    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, max_time_stamp = 40)   # default: max_time_stamp = 10 (40: only for attack) 
    print "Load Query : DONE"
    
    print "max_speed = ", query_log.max_speed
    print "elapsed : ", (time.clock() - start_time)   
    
        
    graph = Graph(0, map_data, query_log, None)
    
    #TEST
    graph.run_timestamps(0, timestep)
    print "graph.run_timestamps - DONE"
        
        
    
#    master = Tk()
#    
#    
#    width = 700
#    map_visualizer = MapVisualizer(map_data, query_log, width)
#    
#    w = Canvas(master, width=map_visualizer.width, height=map_visualizer.height)
#    w.pack()
#    
#    map_visualizer.draw_map(w)
#    
#    #
#    graph = Graph(0, map_data, query_log, map_visualizer)
#    
#    graph.run_timestamps(0,0)
#    
#    mainloop()
    
    

#    graph.solve_new_queries(0)
#    print graph.mc_set


#    graph = Graph(0, None, None, None)
#    graph.init_nodes([1,2,3,4,5,6])
#    print graph.mc_set
#    graph.add_to_mc_set([(1,2),(1,3),(1,4),(1,5),(1,6)])
##    graph.add_to_mc_set(1, [(1,2)])
##    graph.add_to_mc_set(1, [(1,2),(1,3)])
#    print graph.mc_set
#
#    graph.add_to_mc_set([(2,3),(2,6)])
#    print graph.mc_set
#    
#    graph.add_to_mc_set([(3,4)])
#    print graph.mc_set
#
#    graph.add_to_mc_set([(4,5)])
#    print graph.mc_set
#    
#    graph.add_to_mc_set([(5,6)])
#    print graph.mc_set
#    
#    graph.add_to_mc_set([(3,5)])
#    print graph.mc_set
#    
#    #TEST
#    print "Test remove nodes"
#    graph.remove_from_mc_set([1])
#    print graph.mc_set
    
    
    
    