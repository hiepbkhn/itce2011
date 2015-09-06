'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''


#import map_visualizer
#from query_loader import QueryLog
from pyautocad_draw import draw_map

import struct
import time
import option
from line_operator import LineOperator
from geom_util import Node, Edge, EdgeSegment, EdgeSegmentSet, Query, Point, IntervalTree, \
    get_edge_length, get_segment_length

# compute average edge length
# compute average speed

class TreeNode: pass
 
def kdtree(point_list, depth=0):
 
    if not point_list:
        return None
 
    # Select axis based on depth so that axis cycles through all valid values
    k = len(point_list[0]) # assumes all points have the same dimension
    axis = depth % k
 
    # Sort point list and choose median as pivot element
    point_list.sort(key=lambda point: point[axis])
    median = len(point_list) // 2 # choose median
 
    # Create node and construct subtrees
    node = TreeNode()
    node.location = point_list[median]
    node.left_child = kdtree(point_list[:median], depth + 1)
    node.right_child = kdtree(point_list[median + 1:], depth + 1)
    return node

def count_tree(tree):
    left_count = 0
    if tree.left_child:
        left_count = count_tree(tree.left_child)  
    right_count = 0     
    if tree.right_child:
        right_count = count_tree(tree.right_child)
    return (1 + left_count + right_count)      
        


class SegItem:
    def __init__(self, x, y, end_x, end_y, length, cur_edge_id, is_node):
        self.x = x
        self.y = y
        self.end_x = end_x
        self.end_y = end_y
        self.length = length
        self.cur_edge_id = cur_edge_id  # = -1 if is_node = true
        self.is_node = is_node

class Stack:
    def __init__(self):
        self.stack = []
        self.max_size = 0
    
    def pop(self):
        return self.stack.pop(0)   
    
    def compare(self, seg_item_1, seg_item_2):
        if (seg_item_1.x == seg_item_2.x and seg_item_1.y == seg_item_2.y):
            return 0 
        if (seg_item_1.x < seg_item_2.x) or (seg_item_1.x == seg_item_2.x and seg_item_1.y < seg_item_2.y):
            return 1
        else:
            return -1 
    
    def push(self, seg_item):
        # check if seg_item.xy exist
        if len(self.stack) == 0:
            self.stack.append(seg_item)
        
        # binary search
        lo = 0
        hi = len(self.stack) - 1
        mid = (lo + hi) / 2
        found = False
        while True:
            if self.compare(self.stack[mid], seg_item) == 0:
                found = True
                break
            if self.compare(self.stack[mid], seg_item) == -1:
                hi = mid - 1
                if hi < lo: 
                    break
            else:
                lo = mid + 1
                if lo > hi:
                    break
            mid = (lo + hi) / 2    
        
        # insert
        if found == False:
            mid = lo
            self.stack.insert(mid, seg_item)
            
        # replace            
        else:
            if self.stack[mid].length < seg_item.length:
                self.stack[mid] = seg_item

        #DEBUG
#        self.max_size = max(self.max_size, len(self.stack))        

    def get_size(self):    
        return len(self.stack)            


class Map:
    
    def __init__(self):
        self.nodes = {}
        self.edges = {}
        self.adj = {}           #adjacent lists
        self.node_to_edges = {}
        self.xy_to_node_id = {}
        self.node_pair_to_edge = {}
        #
        self.interval_tree_x = None
        self.interval_tree_y = None
        
        self.min_x = 0
        self.min_y = 0
        self.max_x = 0
        self.max_y = 0
        self.dx = 0
        self.dy = 0
        self.area = 0   # = dx*dy
        self.total_map_len = 0
        
    
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
        while cur < len(data):
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
            
            self.nodes[node_id] = Node(node_id, node_x, node_y)
            self.xy_to_node_id[(node_x, node_y)] = node_id
            
            #print node_id, node_x, node_y
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
        
        print "min,max (X,Y)", min_x, min_y, max_x, max_y
        dx = max_x - min_x
        dy = max_y - min_y
        
        self.min_x = min_x
        self.min_y = min_y
        self.max_x = max_x
        self.max_y = max_y
        self.dx = dx
        self.dy = dy
        self.area = self.dx * self.dy
        print "map.area", self.area
        
        # 2. EDGES
        f = open(path + map_name + ".edge", "rb")
        
        self.edges = {}
        data = f.read()
        
        cur = 0
        while cur < len(data):
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
            
            self.edges[edge_id] = Edge(edge_id, start_node_id, end_node_id, edge_class)
            
            self.total_map_len += get_edge_length(self.nodes[start_node_id], self.nodes[end_node_id])
            #
            try:
                self.node_to_edges[start_node_id].append(edge_id)
            except:
                self.node_to_edges[start_node_id]= []
                self.node_to_edges[start_node_id].append(edge_id)
            #    
            try:
                self.node_to_edges[end_node_id].append(edge_id)
            except:
                self.node_to_edges[end_node_id]= []
                self.node_to_edges[end_node_id].append(edge_id)
            #    
            try:
                self.adj[start_node_id].append(end_node_id)    
            except:
                self.adj[start_node_id] = []
                self.adj[start_node_id].append(end_node_id)   
                 
            try:
                self.adj[end_node_id].append(start_node_id)    
            except:
                self.adj[end_node_id] = []
                self.adj[end_node_id].append(start_node_id)       
            #
            self.node_pair_to_edge[(start_node_id, end_node_id)] = edge_id
            self.node_pair_to_edge[(end_node_id, start_node_id)] = edge_id
        
        f.close()
        
        print "total_map_len =", self.total_map_len
        
        #timing
        elapsed = (time.clock() - start)
        print "Elapsed ", elapsed
        
        print len(self.nodes)
        print len(self.edges)
        
        # 3. Interval Trees (X,Y)
        features_x = []
        features_y = []
        for edge in self.edges.itervalues():
            x1 = min(self.nodes[edge.start_node_id].x, self.nodes[edge.end_node_id].x)
            x2 = max(self.nodes[edge.start_node_id].x, self.nodes[edge.end_node_id].x)
            
            y1 = min(self.nodes[edge.start_node_id].y, self.nodes[edge.end_node_id].y)
            y2 = max(self.nodes[edge.start_node_id].y, self.nodes[edge.end_node_id].y)
        
            features_x.append([x1, x2, edge.edge_id])
            features_y.append([y1, y2, edge.edge_id])
        
        self.interval_tree_x = IntervalTree(features_x, 0, 1, self.min_x, self.max_x)
        self.interval_tree_y = IntervalTree(features_y, 0, 1, self.min_y, self.max_y)
        print "compute Interval Trees (X,Y): DONE"
        
         
            
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
        return self.xy_to_node_id.get((next_node_x, next_node_y), -1)   #we may have node 0
    
    #######################################################
    def get_nearest_edge_id(self, next_node_x, next_node_y, px, py):
        min_distance = 100000000.0
        nearest_edge_id = -1
        
        next_node_id = self.get_next_node_id(next_node_x, next_node_y)
        
        #print type(self.node_to_edges[next_node_id]) 
        
        for edge_id in self.node_to_edges[next_node_id]:
            start_node_id = self.edges[edge_id].start_node_id
            end_node_id = self.edges[edge_id].end_node_id
            #
            length = LineOperator.distance_to(self.nodes[start_node_id].x, self.nodes[start_node_id].y,
                self.nodes[end_node_id].x, self.nodes[end_node_id].y, px, py)
            if length < min_distance:
                min_distance = length
                nearest_edge_id = edge_id
                
#            print edge_id, length, px, py, self.nodes[start_node_id].x, self.nodes[start_node_id].y, \
#                self.nodes[end_node_id].x, self.nodes[end_node_id].y
                
        return nearest_edge_id
    
    
    #######################################################
    def compute_fixed_expanding(self, x, y, cur_edge_id, length):
        
        result = []
        
        stack = Stack()
        #
        is_node = self.get_next_node_id(x, y) > -1
        
        stack.push( SegItem(x, y, -1, -1, length, cur_edge_id, is_node))
        
        while stack.get_size() > 0:
            item = stack.pop()
#            if item.length == 0:
#                result.append(item)
#                continue
            
            # case 1, is_node == True
            if item.is_node == True:
                node_id = self.get_next_node_id(item.x, item.y)
                
                for end_node_id in self.adj[node_id]:   #traverse adjacent edges...
                    edge_len = get_edge_length(self.nodes[node_id], self.nodes[end_node_id])
                    
                    if edge_len < item.length:
                        remaining_len = item.length - edge_len
                        #
                        result.append(EdgeSegment(self.nodes[node_id].x, self.nodes[node_id].y, 
                                              self.nodes[end_node_id].x, self.nodes[end_node_id].y,
                                              self.node_pair_to_edge[(node_id, end_node_id)]))
                        #
                        for edge_id in self.node_to_edges[end_node_id]: #one choice for each adjacent edge
                            stack.push(SegItem(self.nodes[end_node_id].x, self.nodes[end_node_id].y, 
                                                 -1, -1,
                                                 remaining_len, edge_id, True))
                        
                    else:
                        end_x = item.x + item.length * (self.nodes[end_node_id].x - item.x) / edge_len
                        end_y = item.y + item.length * (self.nodes[end_node_id].y - item.y) / edge_len
                        result.append(EdgeSegment(self.nodes[node_id].x, self.nodes[node_id].y, 
                                              end_x, end_y, 
                                              self.node_pair_to_edge[(node_id, end_node_id)]))
                        
            
            # case 2, is_node == False
            else:
                for end_node_id in (self.edges[item.cur_edge_id].start_node_id, self.edges[item.cur_edge_id].end_node_id):
                    segment_len = get_segment_length(self.nodes[end_node_id], item.x, item.y)
                    
                    if segment_len < item.length:
                        remaining_len = item.length - segment_len
                        # end_node_id.xy go first to comply with convention: first point always graph node !!
                        result.append(EdgeSegment(self.nodes[end_node_id].x, self.nodes[end_node_id].y,  
                                              item.x, item.y,
                                              item.cur_edge_id))
                        #
                        for edge_id in self.node_to_edges[end_node_id]:
                            stack.push(SegItem(self.nodes[end_node_id].x, self.nodes[end_node_id].y, 
                                             -1, -1,
                                             remaining_len, edge_id, True))
                        
                    else:
                        end_x = item.x + item.length * (self.nodes[end_node_id].x - item.x) / segment_len
                        end_y = item.y + item.length * (self.nodes[end_node_id].y - item.y) / segment_len
                        result.append(EdgeSegment(item.x, item.y,
                                              end_x, end_y, 
                                              item.cur_edge_id))    
         
        
        # DEBUG
#        print "stack.max_size", stack.max_size
#        
#        print "length(result) = ", length(result)
#        for item in result:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)
         
        return result
         
         
    #######################################################
    def compute_mesh_expanding(self, item_list, length):   
        
        result = item_list
        #1. call find_boundary_points() 
        boundary_points = Map.find_boundary_points(item_list)
        
        #2. 
        for point in boundary_points:
            new_seg_set = self.compute_fixed_expanding(point[0], point[1], point[2], option.MAX_SPEED)
            new_seg_set = EdgeSegmentSet.clean_fixed_expanding(new_seg_set)
            
            result = EdgeSegmentSet.union(result, new_seg_set)
            
        return result
    
    
    #######################################################
    def is_node_in_rec(self, min_x, min_y, max_x, max_y, node):
        p1_x = node.x
        p1_y = node.y
        return (min_x <= p1_x) and (p1_x <= max_x) and (min_y <= p1_y) and (p1_y <= max_y)
    
    
    #######################################################
    def is_edge_in_rec(self, min_x, min_y, max_x, max_y, edge):
        return self.is_node_in_rec(min_x, min_y, max_x, max_y, self.nodes[edge.start_node_id]) and \
            self.is_node_in_rec(min_x, min_y, max_x, max_y, self.nodes[edge.end_node_id])


    #######################################################
    def get_line_equation(self, x1, y1, x2, y2):
        return (y2-y1, x1-x2, y1*x2-y2*x1)
    
    
    
    #######################################################
    def is_edge_cut_rec(self, min_x, min_y, max_x, max_y, edge):
        x1 = self.nodes[edge.start_node_id].x
        y1 = self.nodes[edge.start_node_id].y
        x2 = self.nodes[edge.end_node_id].x
        y2 = self.nodes[edge.end_node_id].y
        coeff = self.get_line_equation(x1, y1, x2, y2)
        a1 = coeff[0]*min_x + coeff[1]*min_y + coeff[2]
        a2 = coeff[0]*min_x + coeff[1]*max_y + coeff[2]
        a3 = coeff[0]*max_x + coeff[1]*max_y + coeff[2]
        a4 = coeff[0]*max_x + coeff[1]*min_y + coeff[2]
        
        if  self.is_node_in_rec(min_x, min_y, max_x, max_y, self.nodes[edge.start_node_id]) or \
            self.is_node_in_rec(min_x, min_y, max_x, max_y, self.nodes[edge.end_node_id]):
            return True       
        elif (a1*a2 < 0 and (x1-min_x)*(x2-min_x) < 0) or (a2*a3 < 0 and (y1-max_y)*(y2-max_y) < 0) \
            or (a3*a4 < 0 and (x1-max_x)*(x2-max_x) < 0) or (a4*a1 < 0 and (y1-min_y)*(y2-min_y) < 0):
            return True
        
        return False 
            
    #######################################################
    #MBR: maximum boundary rectangle
    #MUST use R-Tree !!!
    #
    def compute_mesh_mbr(self, locations):
        result = []
        #
        min_x = 100000000
        min_y = 100000000
        max_x = -100000000
        max_y = -100000000   
        for point in locations:
            if min_x > point.x:
                min_x = point.x
            if min_y > point.y:
                min_y = point.y
            if max_x < point.x:
                max_x = point.x
            if max_y < point.y:
                max_y = point.y
        
#        print "compute_mesh_mbr - min,max (X,Y)", min_x, min_y, max_x, max_y
                  
        #Solution 1: linear scan
#        start = time.clock()
        for edge in self.edges.itervalues():
            if self.is_edge_cut_rec(min_x, min_y, max_x, max_y, edge):
                p1_x = self.nodes[edge.start_node_id].x
                p1_y = self.nodes[edge.start_node_id].y
                p2_x = self.nodes[edge.end_node_id].x
                p2_y = self.nodes[edge.end_node_id].y
                result.append(EdgeSegment(p1_x, p1_y, p2_x, p2_y, edge.edge_id))
#        print "Elapsed ", (time.clock() - start)        
#        print "Solution 1: len=", len(result)
#        for edge_seg in result:
#            print edge_seg.cur_edge_id
        
                
        #Solution 2: Interval Tree
#        start = time.clock()
#        set_x = set(self.interval_tree_x.findRange([min_x, max_x]))
#        set_y = set(self.interval_tree_y.findRange([min_y, max_y]))
#        print "set_x", set_x
#        print "set_y", set_y
#        
#        cutting_edges = set_x & set_y
#        print "cutting_edges", cutting_edges
#        result = []
#        for edge_id in cutting_edges:
#            if self.is_edge_cut_rec(min_x, min_y, max_x, max_y, self.edges[edge_id]):
#                p1_x = self.nodes[self.edges[edge_id].start_node_id].x
#                p1_y = self.nodes[self.edges[edge_id].start_node_id].y
#                p2_x = self.nodes[self.edges[edge_id].end_node_id].x
#                p2_y = self.nodes[self.edges[edge_id].end_node_id].y
#                result.append(EdgeSegment(p1_x, p1_y, p2_x, p2_y, edge_id))  
#        print "Elapsed ", (time.clock() - start)         
#        print "Solution 2: len=", len(result)
#        for edge_seg in result:
#            print edge_seg.cur_edge_id
                  
        return result 
         
    #######################################################
    @staticmethod         
    def find_boundary_points(item_list):
        
#        print "length(item_list) = ", len(item_list)
#        for item in item_list:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)
            
        nodes = {}
        result = []
        
        for item in item_list:
            if not nodes.has_key((item.start_x, item.start_y, item.cur_edge_id)):
                nodes[(item.start_x, item.start_y, item.cur_edge_id)] = 1
            else:
                nodes[(item.start_x, item.start_y, item.cur_edge_id)] += 1
                
            if not nodes.has_key((item.end_x, item.end_y, item.cur_edge_id)):
                nodes[(item.end_x, item.end_y, item.cur_edge_id)] = 1   
            else:
                nodes[(item.end_x, item.end_y, item.cur_edge_id)] += 1   
        
        for (point, count) in nodes.iteritems():
            if count == 1:
                result.append(point)
                
        return result
    
             
    
    #######################################################
    @staticmethod
    def check_connected_expanding(item_list):
        
#        print "length(item_list) = ", length(item_list)
#        for item in item_list:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)
        
        
        nodes = {}
        num_node = 0
        MAX_NODE = 1000
        edges = [[0]*MAX_NODE for x in xrange(MAX_NODE)]
        
        for item in item_list:
            if not nodes.has_key((item.start_x, item.start_y)):
                num_node += 1
                nodes[(item.start_x, item.start_y)] = num_node
                start_node = num_node
            else:
                start_node = nodes[(item.start_x, item.start_y)]
                
            if not nodes.has_key((item.end_x, item.end_y)):
                num_node += 1
                nodes[(item.end_x, item.end_y)] = num_node   
                end_node = num_node
            else:
                end_node = nodes[(item.end_x, item.end_y)]
                
            edges[start_node][end_node] = 1
            edges[end_node][start_node] = 1
            
#        for i in range(num_node+1):
#            for j in range(i+1,num_node+1):
#                if edges[i][j] == 1:
#                    print i, j

        # test connected
        visited = [0]*(num_node+1)
        visited[1] = 1
        
        queue = [1]
        while len(queue) > 0:
            cur = queue.pop()
            for k in range(1,num_node+1):
                if edges[cur][k] == 1 and visited[k] == 0:
                        queue.insert(0,k)
                        visited[k] = 1
        
        #
#        print visited
        
        if sum(visited) == num_node:
            return True
        else:
            return False            
            
        
        
    
    
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    
#    draw_map(map_data.nodes, map_data.edges)
#    print "Draw Map : DONE"
        
     #TEST   
#    print "node_id found = ", map_data.get_next_node_id(13875, 24126)
#    print "nearest_edge_id found", map_data.get_nearest_edge_id(13875, 24126, 13935.0, 23953.0)
#    print "nearest_edge_id found", map_data.get_nearest_edge_id(8381, 16296, 8545.43, 16095.95) #7178
#    
#    sum_edge_len = 0
#    for edge_id, edge in map_data.edges.iteritems():
#        sum_edge_len += Edge.length(map_data.nodes[edge.start_node_id], map_data.nodes[edge.end_node_id])
#    print "Avg. edge length = ", sum_edge_len/length(map_data.edges)
    
    #TEST 1: is_node = True
    #result = map_data.compute_fixed_expanding(13875, 24126, 0, 200)
    #result = map_data.compute_fixed_expanding(2096, 11964, 0, 200)
    
    #TEST 2: is_node = False
    for radius in [600,700,800,900,1000,1100,1200,1300,1400,1500]:
        print "radius =", radius
        start = time.clock()
        result = map_data.compute_fixed_expanding(8545.43, 16095.95, 98307178, radius)
        
        elapsed = (time.clock() - start)
        print "Elapsed ", elapsed
        print "length(item_list) = ", len(result)
        
    #600     Elapsed  0.00164145542381
    #700     Elapsed  0.00333834461428
    #800     Elapsed  0.0090295446578
    #900     Elapsed  0.013230192309
    #1000    Elapsed  0.0228197206651
    #1100    Elapsed  0.0399487545624
    #1200    Elapsed  0.073717157418
    #1300    Elapsed  0.131422168682
    #1400    Elapsed  0.247271041862
    #1500    Elapsed  0.45686200739
        
        
        start = time.clock()
        result = EdgeSegmentSet.clean_fixed_expanding(result)
        
        elapsed = (time.clock() - start)
        print "Elapsed ", elapsed
        
        print "length(item_list) = ", len(result)

#        for item in result:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)
    
    
    #TEST 3: EdgeSegmentSet.union()
#    result_1 = map_data.compute_fixed_expanding(8545.43, 16095.95, 98307178, 300)
#    result_1 = EdgeSegmentSet.clean_fixed_expanding(result_1)
#    
#    result_2 = map_data.compute_fixed_expanding(8381, 16296, 98307178, 300)
#    result_2 = EdgeSegmentSet.clean_fixed_expanding(result_2)
#    print "DONE 1,2"

#    print map_data.adj[95]    
#    result_3 = map_data.compute_fixed_expanding(8299, 7790, 9400095, 300)
#    result_3 = EdgeSegmentSet.clean_fixed_expanding(result_3)
#    print "DONE 3"
    
    #TEST 4:
#    EdgeSegmentSet.union(result_1, result_2)
#    
#    query_1 = Query(-1, 8545.43, 16095.95, 0, 0, 0, -1, 98307178)
#    query_2 = Query(-1, 8381, 16296, 0, 0, 0, -1, 98307178)
#    print EdgeSegmentSet.is_set_cover(Point(query_1), result_2)
#    print EdgeSegmentSet.is_set_cover(Point(query_2), result_1)


    #TEST 5:
#    result_1 = map_data.compute_fixed_expanding(8545.43, 16095.95, 98307178, option.MAX_SPEED)
#    result_1 = EdgeSegmentSet.clean_fixed_expanding(result_1)
#    
#    print Map.check_connected_expanding(result_1)
#    
#    #TEST 6:
#    result_1 = map_data.compute_fixed_expanding(8545.43, 16095.95, 98307178, option.MAX_SPEED)
#    result_1 = EdgeSegmentSet.clean_fixed_expanding(result_1)
#    
#    print Map.find_boundary_points(result_1)


    #TEST 7:
#    start = time.clock()
#    result_1 = map_data.compute_fixed_expanding(8545.43, 16095.95, 98307178, option.INIT_DISTANCE)
#    result_1 = EdgeSegmentSet.clean_fixed_expanding(result_1)
#    print "Elapsed ", (time.clock() - start)
#    
#    print "result_1 total_len=", EdgeSegmentSet.length(result_1)
#    
#    print "length(item_list) = ", len(result_1)
#    for item in result_1:
#        print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#            item.start_x, item.start_y, item.end_x, item.end_y)
#        
#    start = time.clock()
#    result_1 = map_data.compute_mesh_expanding(result_1, option.MAX_SPEED)    
#    print "Elapsed ", (time.clock() - start)
#        
#    print "length(item_list) = ", len(result_1)
#    for item in result_1:
#        print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#            item.start_x, item.start_y, item.end_x, item.end_y)
#        
#    print "check_connected_expanding=", Map.check_connected_expanding(result_1)        


    #TEST 8:
#    start = time.clock()
#    point_list = []
#    for edge in map_data.edges.itervalues():
#        x1 = min(map_data.nodes[edge.start_node_id].x, map_data.nodes[edge.end_node_id].x)
#        y1 = min(map_data.nodes[edge.start_node_id].y, map_data.nodes[edge.end_node_id].y)
#        x2 = max(map_data.nodes[edge.start_node_id].x, map_data.nodes[edge.end_node_id].x)
#        y2 = max(map_data.nodes[edge.start_node_id].y, map_data.nodes[edge.end_node_id].y)
#        point_list.append((x1,y1,x2,y2))
#        
#    tree = kdtree(point_list)
#    print "KD-Tree, Elapsed ", (time.clock() - start)
#
#    print "count_tree = ", count_tree(tree)



    #TEST 9:
#    node_1 = Node(-1, 250, 250)
#    node_2 = Node(-2, 150, 160)
#    map_data.nodes[-1] = node_1
#    map_data.nodes[-2] = node_2
#    edge = Edge(0, -1, -2, -1)
#    print map_data.is_edge_cut_rec(0, 0, 100, 100, edge)
    
    
    
    
#    locations = []
#    x1 = 11000
#    y1 = 17000
#    x2 = 12000
#    y2 = 18000
#    locations.append(Query(0, x1, y1, 0, 0, 0, 0, 0))
#    locations.append(Query(0, x2, y2, 0, 0, 0, 0, 0))
#    
#    map_data.compute_mesh_mbr(locations)
    
    
#    query_log = QueryLog(map_data)
#    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE)    
#    print "Load Query : DONE"
#    master = Tk()
#    width = 700
#    visualizer = map_visualizer.MapVisualizer(map_data, query_log , width)
#    w = Canvas(master, width=visualizer.width, height=visualizer.height)
#    w.pack()
#    
#    visualizer.draw_map(w)
#    visualizer.draw_rectangle(x1, y1, x2, y2, "blue")
#    mainloop()



