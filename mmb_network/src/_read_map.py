'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''

import struct
import time
from symbol import except_clause
from line_operator import LineOperator

# compute average edge length
# compute average speed


class Map:
    
    def __init__(self):
        self.nodes = {}
        self.edges = {}
        self.node_to_edges = {}
        self.xy_to_node_id = {}
        
        self.min_x = 0
        self.min_y = 0
        self.max_x = 0
        self.max_y = 0
        self.dx = 0
        self.dy = 0
        
    
    #######################################################
    def read_map(self, path, map_name):
        start = time.clock()
        
        # 1. NODES
        f = open(path + map_name + ".node", "rb")
        
        self.nodes = {}
        data = f.read()
        #print type(data[:20])
        
        cur = 0
        min_x, min_y = 1000000000, 1000000000
        max_x, max_y = -1000000000, -1000000000
        while cur < length(data):
            # node name
            node_name_len = ord(data[cur])
            #print node_name_len
            node_name = data[cur+1:cur+node_name_len+1]
            #print node_name
            
            # node id (big endian)
            #print repr(data[cur+node_name_len+2 : cur+node_name_len+10])
            node_id = struct.unpack('>Q', data[cur+node_name_len+1 : cur+node_name_len+9])[0]
            #print node_id
        
            # node x (big endian)
            node_x = struct.unpack('>i', data[cur+node_name_len+9 : cur+node_name_len+13])[0]
            #print node_x
            
            # node y (big endian)
            node_y = struct.unpack('>i', data[cur+node_name_len+13 : cur+node_name_len+17])[0]
            #print node_y
            
            cur = cur+node_name_len+17
            
            self.nodes[node_id] = (node_x, node_y)
            self.xy_to_node_id[(node_x, node_y)] = node_id
            
            print node_id, node_x, node_y
            #
            if min_x > node_x: 
                min_x = node_x        
            if min_y > node_y: 
                min_y = node_y
            if max_x < node_x: 
                max_x = node_x
            if max_y < node_y: 
                max_y = node_y
                
        f.close()
        
        print min_x, min_y, max_x, max_y
        dx = max_x - min_x
        dy = max_y - min_y
        
        self.min_x = min_x
        self.min_y = min_y
        self.max_x = max_x
        self.max_y = max_y
        self.dx = dx
        self.dy = dy
        
        
        # 2. EDGES
        f = open(path + map_name + ".edge", "rb")
        
        self.edges = {}
        data = f.read()
        
        cur = 0
        while cur < length(data):
            # start_node id (big endian)
            start_node_id = struct.unpack('>Q', data[cur : cur+8])[0]
            #print start_node_id
            
            # end_node id (big endian)
            end_node_id = struct.unpack('>Q', data[cur+8 : cur+16])[0]
            #print end_node_id
            
            # edge name
            edge_name_len = ord(data[cur+16])
            #print edge_name_len
            edge_name = data[cur+17:cur+17+edge_name_len]
            #print "%c" % edge_name
            
            # node x (big endian)
            edge_id = struct.unpack('>Q', data[cur+edge_name_len+17 : cur+edge_name_len+25])[0]
            #print edge_id
            
            # edge_class (big endian)
            edge_class = struct.unpack('>i', data[cur+edge_name_len+25 : cur+edge_name_len+29])[0]
            #print edge_class
            
            #print start_node_id, end_node_id, edge_name_len, edge_name, edge_id, edge_class
            #print start_node_id, end_node_id
            
            cur = cur + edge_name_len + 29
            
            self.edges[edge_id] = (start_node_id, end_node_id)
            #
            try:
                self.node_to_edges[start_node_id].append(edge_id)
            except:
                self.node_to_edges[start_node_id]= []
                self.node_to_edges[start_node_id].append(edge_id)
                
            try:
                self.node_to_edges[end_node_id].append(edge_id)
            except:
                self.node_to_edges[end_node_id]= []
                self.node_to_edges[end_node_id].append(edge_id)
        
        f.close()
        
        #timing
        elapsed = (time.clock() - start)
        print "Elapsed ", elapsed
        
        print length(self.nodes)
        print length(self.edges)
        
    
    #######################################################
    def get_nodes(self):
        return self.nodes
    
    #######################################################
    def get_edges(self):
        return self.edges
    
    #######################################################
    def get_node_to_edges(self):
        return self.get_node_to_edges()
    
    #######################################################
    def get_next_node_id(self, next_node_x, next_node_y):
        return self.xy_to_node_id.get((next_node_x, next_node_y), 0)
    
    #######################################################
    def get_nearest_edge_id(self, next_node_x, next_node_y, px, py):
        min_distance = 100000000.0
        nearest_edge_id = -1
        
        next_node_id = self.get_next_node_id(next_node_x, next_node_y)
        
        #print type(self.node_to_edges[next_node_id]) 
        
        for edge_id in self.node_to_edges[next_node_id]:
            start_node_id = self.edges[edge_id][0]
            end_node_id = self.edges[edge_id][1]
            #
            length = LineOperator.distance_to(self.nodes[start_node_id][0], self.nodes[start_node_id][1],
                self.nodes[end_node_id][0], self.nodes[end_node_id][1], px, py)
            if length < min_distance:
                min_distance = length
                nearest_edge_id = edge_id
                
        return nearest_edge_id
    
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    map_data = Map()
    map_data.read_map("D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/", 
                 "oldenburgGen")
        
    print "node_id found = ", map_data.get_next_node_id(13875, 24126)
    
    print "nearest_edge_id found", map_data.get_nearest_edge_id(13875, 24126, 13935.0, 23953.0)
    
    
    
    
    
    
    
    
    
    
