'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''

import option
from geom_util import Node, Edge, Query



class QueryLog:
    
    def __init__(self, map_data):
        self.map_data = map_data
        
        self.nodes = map_data.get_nodes()
        self.edges = map_data.get_edges()
        #
        self.trajs = {}     #dict of dicts [node][timestamp]
        self.frames = {}    #dict of lists
        #
        self.max_speed = 0
    
    #######################################################
    def read_query(self, path, log_file_name, max_time_stamp=-1, query_type=1):
        
        f = open(path + log_file_name, "r")
        fstr = f.read()
        f.close()
        
        maxNodeId = -1
        for line in fstr.split("\n"):
            if line == "": # EOF
                break
            items = line.split("\t")
            
            ### 1 - FOR Brinkhoff generator
            if query_type == 0:
                obj_id = int(items[1])
                maxNodeId = max( obj_id, maxNodeId)
     
                x = float(items[5])
                y = float(items[6])
                timestamp = int(items[4])
                speed = float(items[7])
                next_node_x = int(items[8])
                next_node_y = int(items[9])
                k_anom = int(items[10])
                min_length = float(items[11])
            else:
            ### 2 - FOR TraceGenerator
                obj_id = int(items[0])
                maxNodeId = max( obj_id, maxNodeId)
     
                x = float(items[2])
                y = float(items[3])
                timestamp = int(items[1])
                speed = float(items[4])
                next_node_x = int(items[5])
                next_node_y = int(items[6])
                k_anom = int(items[7])
                min_length = float(items[8])     
            # (End) 2 - FOR TraceGenerator       
            
            #
            if max_time_stamp != -1 and timestamp > max_time_stamp:
                break

            # TODO: find next_node_id and cur_edge_id
            next_node_id = self.map_data.get_next_node_id(next_node_x, next_node_y)
            cur_edge_id = self.map_data.get_nearest_edge_id(next_node_x, next_node_y, x, y)
            
            # Trajectories
            try:
                self.trajs[obj_id][timestamp] = Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, k_anom, min_length, option.DISTANCE_CONSTRAINT)
            except:
                self.trajs[obj_id] = {}
                self.trajs[obj_id][timestamp] = Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, k_anom, min_length, option.DISTANCE_CONSTRAINT)

            # Time frames
            try:
                self.frames[timestamp].append( Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, k_anom, min_length, option.DISTANCE_CONSTRAINT))
            except:
                self.frames[timestamp] = []
                self.frames[timestamp].append( Query(obj_id, x, y, timestamp, next_node_x, next_node_y, next_node_id, cur_edge_id, k_anom, min_length, option.DISTANCE_CONSTRAINT))

            #
            if self.max_speed < speed:
                self.max_speed = speed
                    
                    
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    pass

    












