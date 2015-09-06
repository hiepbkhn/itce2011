'''
Created on Nov 20, 2012

@author: Nguyen Huu Hiep
'''

import copy
import time
import option


from query_loader import QueryLog
from map_loader import Map 
from geom_util import EdgeSegmentSet, Point, Query, get_distance
from map_visualizer import MapVisualizer

class Graph:
    def __init__(self, num_user, map_data, query_log, map_visualizer):
        self.num_user = num_user
        self.map_data = map_data
        self.query_log = query_log
        self.map_visualizer = map_visualizer
        
        self.adj = {}           #adjacent lists, dict of lists
        self.user_mc_set = {}   #dict of sets, e.g. user_mc_set[1] = set([1,2,3,5])
        self.mc_set = []        #maximal clique set, list of sets 
        self.user_mesh = {}      #dict of EdgeSegment lists, e.g. user_mesh[1] = [seg1, seg2, ...]
        self.mmb_mesh = {}      #Map.compute_mesh_expanding()
        self.user_set = set([]) #set of users satisfied and cloaked !  
        
        
    #######################################################        
    def reset(self):
        self.num_user = 0
        self.user_mc_set = {}   #dict of sets
        self.mc_set = []        #maximal clique set, list of sets 


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
                
#                #DISABLED
#                new_user_mc_set[u].append(set([u,w]))
#                new_user_mc_set[w].append(set([u,w]))
            else:
                for set_inter in clique_inter:
                    new_clique = set_inter | set([u,w])
                    new_mc_set.append(new_clique)
#                    #DEBUG
#                    if new_clique == set([538, 684, 655]):
#                        print "in step 2"

#                    #DISABLED
#                    for user in new_clique:
#                        new_user_mc_set[user].append(new_clique)
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
#                    #DEBUG
#                    if set_union == set([538, 684, 655]):
#                        print "in step 3", clique_union
                    
                    #DISABLED
#                    for user in set_union:
#                        new_user_mc_set[user].append(set_union)
            
            # NOT use DEEP COPY
            new_mc_set.extend(temp_mc_set)
            
            
            # 4.
            for set_neither in self.mc_set:
                if (u in set_neither) or (w in set_neither):
                    continue
                else:
                    new_mc_set.append(set_neither)
#                    #DEBUG
#                    if set_neither == set([538, 684, 655]):
#                        print "in step 4"
                    
                    #DISABLED
#                    for user in set_neither:
#                        new_user_mc_set[user].append(set_neither)
                    
                    
            #
            self.mc_set = new_mc_set    
                
            # DEBUG    
#            if Graph.check_mc_set(self.mc_set) == False:
#                return False
#            else:
#                return True
        #end FOR
    
    
    
    #######################################################
    # u_nodes: list of nodes
    def remove_from_mc_set(self, u_nodes):
        
        
        for node in u_nodes:
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
    def find_cloaking_set(self, user):
        can_cr = self.user_mc_set[user]
        can_cr.sort(key=len, reverse = True)
        
        
    #######################################################
    def init_cloaking_sets(self):
        pass    
    
             
    #######################################################
    def solve_new_queries(self, timestamp):         
        start_time = time.clock()
        
        expanding_list = {}                                 #dict of lists
        query_list = self.query_log.frames[timestamp]       # timestamp
        min_obj_id = option.MAX_USER
        max_obj_id = 0
        
        #1. compute expanding_list
        for query in query_list:
            if max_obj_id < query.obj_id:
                max_obj_id = query.obj_id
            if min_obj_id > query.obj_id:
                min_obj_id = query.obj_id
            #        
            seg_list = self.map_data.compute_fixed_expanding(query.x, query.y, 
                                            query.cur_edge_id, option.INIT_DISTANCE)
            seg_list = EdgeSegmentSet.clean_fixed_expanding(seg_list)
            
            expanding_list[query.obj_id] = seg_list
            
            
        #2. init self.mc_set
        self.reset()
        for query in query_list:
            self.mc_set.append(set([query.obj_id]))
            
        #3. compute mc_set
        start_time = time.clock()    
        num_edges = 0 
        list_edges = []
        for i in range(len(query_list)):
            for j in range(i+1,len(query_list)):
                if get_distance(query_list[i].x, query_list[i].y, query_list[j].x, query_list[j].y) > \
                    option.INIT_DISTANCE:
                    continue
                
                if EdgeSegmentSet.is_set_cover(Point(query_list[i]), expanding_list[query_list[j].obj_id]) and \
                    EdgeSegmentSet.is_set_cover(Point(query_list[j]), expanding_list[query_list[i].obj_id]):
                    num_edges += 1
                    list_edges.append((query_list[i].obj_id, query_list[j].obj_id))
        print "num_edges=", num_edges
        print "list_edges - elapsed : ", (time.clock() - start_time)               
                 
        start_time = time.clock()            
        graph.add_to_mc_set(list_edges) 
        
        print "add_to_mc_set - elapsed : ", (time.clock() - start_time)   
        print "mc_set =", self.mc_set
        
        #4. compute user_mc_set, max clique for each obj_id
        start_time = time.clock()   
        self.user_mc_set = {}
        for clique in self.mc_set:
            if len(clique) >= option.K_ANONYMITY:
                for obj_id in clique:
                    if not self.user_mc_set.has_key(obj_id):
                        self.user_mc_set[obj_id] = clique
                    elif len(self.user_mc_set[obj_id]) < len(clique):
                        self.user_mc_set[obj_id] = clique
        print "Compute user_mc_set - elapsed : ", (time.clock() - start_time)   
        print "user_mc_set = ", self.user_mc_set
                    
        #5. compute MMBs (CLOAKING MESHES) and self.user_set
        start_time = time.clock()   
        max_mesh_len = 0
        min_mesh_len = 1000000
        for clique in self.mc_set:
            if len(clique) >= option.K_ANONYMITY:
                mesh = []
                for obj_id in clique:
                    mesh = EdgeSegmentSet.union(mesh, expanding_list[obj_id])
                
                #    
                temp_len = EdgeSegmentSet.length(mesh)
                if max_mesh_len < temp_len:
                    max_mesh_len = temp_len
                if min_mesh_len > temp_len:
                    min_mesh_len = temp_len    
                     
                # assign mesh to all obj_id in 'clique'
                for obj_id in clique:
                    self.user_mesh[obj_id] = mesh
                    
                # 
                for obj_id in clique:
                    self.user_set = self.user_set | set([obj_id])
        
        #print "self.user_set = ", self.user_set
        print "Compute CLOAKING MESH - elapsed : ", (time.clock() - start_time)  
                
        print "max_mesh_len =", max_mesh_len
        print "min_mesh_len =", min_mesh_len
        
        
            
    #######################################################
    def run_timestamps(self, start_timestamp, end_timestamp):
        for timestamp in range (start_timestamp, end_timestamp+1):
            print "--------->>"
            print "TIMESTAMP : ", timestamp
            print "self.num_user = ", len(self.query_log.frames[timestamp])
            
            self.solve_new_queries(timestamp)
            
            print "len(graph.mc_set) = ", len(graph.mc_set)
            print graph.mc_set
        
        
            
   
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    
    
#    mc_set_1 = [set([1,2,3]), set([1,3,4])]
#    mc_set_2 = [set([1,5,6]), set([1,4,5]), set([1,3,4])]
#    
#    Graph.intersect_mc_set(mc_set_1, mc_set_2)
        
    start_time = time.clock()
        
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    query_log = QueryLog(map_data)
    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE)    
    print "Load Query : DONE"
    
    print "max_speed = ", query_log.max_speed
    print "elapsed : ", (time.clock() - start_time)   
    
        
    graph = Graph(0, map_data, query_log, None)
    
    #TEST
    graph.run_timestamps(0,5)
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
    
    
    
    