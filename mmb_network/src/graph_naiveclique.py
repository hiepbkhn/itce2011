'''
Created on Nov 21, 2012

@author: Nguyen Huu Hiep

'''



import copy
import time
import option


from query_loader import QueryLog
from map_loader import Map 
from geom_util import EdgeSegmentSet, Point, Query, CloakingSet, MBR, get_distance
from map_visualizer import MapVisualizer

class Graph:
    def __init__(self, num_user, map_data, query_log, map_visualizer):
        self.num_user = num_user
        self.map_data = map_data
        self.query_log = query_log
        self.map_visualizer = map_visualizer
        
        self.user_mc_set = {}   #dict of sets, e.g. user_mc_set[1] = set([1,2,3,5])
        self.mc_set = []        #maximal clique set, list of sets
        self.graph_edges = {}   #dict of pair (u,w) 
        self.last_query = {}    #dict of (user, last query)
        self.user_mbr = {}      #last cloaked (published) region
        #
        self.positive_mc_set = []
        self.old_user_mc_set = {}
        self.old_user_mbr = {}  
        
    #######################################################        
    def reset(self):
        self.num_user = 0
        self.user_mc_set = {}   #dict of sets
        self.mc_set = []        #maximal clique set, list of sets 
        #
        self.old_user_mc_set = self.user_mc_set
        self.old_user_mbr = self.user_mbr  


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
    def find_cloaking_sets(self, timestamp):
        
        #1. find positive and negative cliques
        self.positive_mc_set = []
        negative_mc_set = []
        for clique in self.mc_set:
            if len(clique) == 1:
                continue
            #
            max_min_area = 0
            max_k_anom = 0
            query_list = []
            for obj_id in clique:
                query = self.query_log.trajs[obj_id][timestamp]
                query_list.append(query)
                if max_min_area < query.min_area:
                    max_min_area = query.min_area
                if max_k_anom < query.k_anom:
                    max_k_anom = query.k_anom
                    
            mbr = CloakingSet.compute_mbr(query_list)
            #
            if len(clique) >= max_k_anom and mbr.area >= max_min_area * self.map_data.area:
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
                query_list.pop()
                if len(query_list) == 0:
                    break;
                max_min_area = max(query_list, key=lambda query: query.min_area).min_area
                mbr = CloakingSet.compute_mbr(query_list)
                if len(query_list) >= query_list[-1].k_anom and mbr.area >= max_min_area * self.map_data.area:
                    break
            #
            if len(query_list) > 1:
                clique = set([query.obj_id for query in query_list])
                new_negative_mc_set.append(clique)    
                
        #3.
        print "positive_mc_set =", self.positive_mc_set
        print "new_negative_mc_set =", new_negative_mc_set 
        
        self.positive_mc_set.extend(new_negative_mc_set)
          
                
    #######################################################
    def solve_new_queries(self, timestamp):
        
        query_list = self.query_log.frames[timestamp]       # timestamp
        
        #0. reset
        self.reset()
        
        #1. compute mc_set
        start_time = time.clock()    
        num_edges = 0 
        list_edges = []
        for i in range(len(query_list)):
            for j in range(i+1,len(query_list)):
                if get_distance(query_list[i].x, query_list[i].y, query_list[j].x, query_list[j].y) <= \
                        option.INIT_DISTANCE:
                    num_edges += 1
                    list_edges.append((query_list[i].obj_id, query_list[j].obj_id))
                    
        print "num_edges=", num_edges
        print "list_edges - elapsed : ", (time.clock() - start_time)       
    
        start_time = time.clock()            
        graph.add_to_mc_set(list_edges) 
        
        print "add_to_mc_set - elapsed : ", (time.clock() - start_time)   
        print "mc_set =", self.mc_set
        
        #2.
        self.find_cloaking_sets(timestamp)
        
        #3. compute user_mc_set (max clique for each obj_id)
        start_time = time.clock()   
        self.user_mc_set = {}
        for clique in self.positive_mc_set:
            for obj_id in clique:
                #
                if not self.user_mc_set.has_key(obj_id):
                    self.user_mc_set[obj_id] = clique
                elif len(self.user_mc_set[obj_id]) < len(clique):
                    self.user_mc_set[obj_id] = clique               #store the maximum
        
        print "Compute user_mc_set - elapsed : ", (time.clock() - start_time)   
        print "user_mc_set = ", self.user_mc_set
                    
        #4. compute MBRs (CLOAKING MBR)
        for clique in self.positive_mc_set:
            query_list = []
            for obj_id in clique:
                query = self.query_log.trajs[obj_id][timestamp]
                query_list.append(query)
            
            mbr = CloakingSet.compute_mbr(query_list)
            for obj_id in clique:
                if self.user_mc_set[obj_id] == clique:
                    self.user_mbr[obj_id] = mbr
                
        print "Compute CLOAKING MBR - elapsed : ", (time.clock() - start_time)  
        print "user_mbr = ", self.user_mbr
                
        #5. publish MBRs (write to file)            
        
        
        
        
                
    
    #######################################################
    def check_published_mcset_mbr(self, user_mc_set, user_mbr, timestamp):
        
        query_list = self.query_log.frames[timestamp] 
        query_dict = {}
        for query in query_list:
            query_dict[query.obj_id] = query
            
        #
        processed = {}
        for user_u in user_mc_set.iterkeys():
            if not processed.has_key(user_u): #unprocessed clique
                max_min_area = 0
                max_k_anom = 0
                for obj_id in user_mc_set[user_u]:
                    processed[obj_id] = 1
                    if max_min_area < query_dict[obj_id].min_area:
                        max_min_area = query_dict[obj_id].min_area
                    if max_k_anom < query_dict[obj_id].k_anom:
                        max_k_anom = query_dict[obj_id].k_anom
                if max_k_anom > len(user_mc_set[user_u]) or \
                    max_min_area*self.map_data.area > user_mbr[user_u]:
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
            print "check_published_mcset_mbr", \
                self.check_published_mcset_mbr(self.user_mc_set, self.user_mbr, timestamp)
            
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
        
    start_time = time.clock()
        
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    query_log = QueryLog(map_data)
    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, max_time_stamp = 20)    
    print "Load Query : DONE"
    
    print "max_speed = ", query_log.max_speed
    print "elapsed : ", (time.clock() - start_time)   
    
        
    graph = Graph(0, map_data, query_log, None)
    
    #TEST
    graph.run_timestamps(0,10)
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
    
    
    
    