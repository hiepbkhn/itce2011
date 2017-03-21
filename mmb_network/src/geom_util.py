'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
Jan-10:    add 'distance constraint' to Query
'''

import math
import pprint

def get_edge_length(node1, node2):
    return math.sqrt((node1.x - node2.x)**2 + (node1.y - node2.y)**2)

def get_segment_length(node, x, y):
    return math.sqrt((node.x - x)**2 + (node.y - y)**2)

def get_distance(x1, y1, x2, y2):
    return math.sqrt((x1 - x2)**2 + (y1 - y2)**2)

########################################################
class IntervalTree:
    def __init__(self, data, si, ei, start, end):
        '''
        data: an array of elements where each element contains start coodinate, end coordinate, and element id.
        si: index or key of the start coodinate in each element
        ei: index or key of the end coordinate in each element
        start: position of the start position of the element range
        end: posotion of the end position of the element range

        for example, a reference genome of a million base pairs with the following features:
            features = [[20,400,'id01'],[1020,2400,'id02'],[35891,29949,'id03'],[900000,'id04'],[999000,'id05']]
        to make a tree:
            myTree = intervalTree(features, 0, 1, 1, 1000000)
        '''
        self.si = si
        self.ei = ei
        self.start = start
        self.end = end
        self.elementaryIntervals = self.getElementaryIntervals(data, si, ei)
        self.tree = self.recursiveBuildTree(self.elementaryIntervals)
        self.insertData(self.tree, data, si, ei, start, end)
        self.trimTree(self.tree)

    def getElementaryIntervals(self, data, si, ei):
        '''generates a sorted list of elementary intervals'''
        coords = []
        [coords.extend([x[si],x[ei]]) for x in data]
        coords = list(set(coords))
        coords.sort()

        return coords

    def recursiveBuildTree(self, elIntervals):
        '''
        recursively builds a BST based on the elementary intervals.
        each node is an array: [interval value, left descendent nodes, right descendent nodes, [ids]].
        nodes with no descendents have a -1 value in left/right descendent positions.

        for example, a node with two empty descendents:
            [500,                               interval value
                [-1,-1,-1,['id5','id6']],       left descendent
                [-1,-1,-1,['id4']],             right descendent
                ['id1',id2',id3']]              data values

        '''
        center = int(round(len(elIntervals) / 2))

        left = elIntervals[:center]
        right = elIntervals[center + 1:]
        node = elIntervals[center]

        if len(left) > 1:
            left = self.recursiveBuildTree(left)
        elif len(left) == 1:
            left = [left[0],[-1,-1,-1,[]],[-1,-1,-1,[]],[]]
        else:
            left = [-1,-1,-1,[]]

        if len(right) > 1:
            right = self.recursiveBuildTree(right)
        elif len(right) == 1:
            right = [right[0],[-1,-1,-1,[]],[-1,-1,-1,[]],[]]
        else:
            right = [-1,-1,-1,[]]

        return [node, left, right, []]

    def ptWithin(self, pt, subject):
        '''accessory function to check if a point is within a range'''
        if pt >= subject[0] and pt <= subject[1]:
            return True

        return False

    def isWithin(self, query, subject):
        '''accessory function to check if a range is fully within another range'''
        if self.ptWithin(query[0], subject) and self.ptWithin(query[1], subject):
            return True

        return False

    def overlap(self, query, subject):
        '''accessory function to check if two ranges overlap'''
        if self.ptWithin(query[0], subject) or self.ptWithin(query[1], subject) or self.ptWithin(subject[0], query) or self.ptWithin(subject[1], query):
            return True

        return False

    def recursiveInsert(self, node, coord, data, start, end):
        '''recursively inserts id data into nodes'''
        if node[0] != -1:
            left = (start, node[0])
            right = (node[0], end)

            #if left is totally within coord
            if self.isWithin(left, coord):
                node[1][-1].append(data)
            elif self.overlap(left, coord):
                self.recursiveInsert(node[1], coord, data, left[0], left[1])

            if self.isWithin(right, coord):
                node[2][-1].append(data)
            elif self.overlap(right, coord):
                self.recursiveInsert(node[2], coord, data, right[0], right[1])

    def insertData(self, node, data, si, ei, start, end):
        '''loops through all the data and inserts them into the empty tree'''
        for item in data:
            self.recursiveInsert(node, [item[si], item[ei]], item[-1], start, end)

    def trimTree(self, node):
        '''trims the tree for any empty data nodes'''
        dataLen = len(node[-1])

        if node[1] == -1 and node[2] == -1:
            if dataLen == 0:
                return 1
            else:
                return 0
        else:
            if self.trimTree(node[1]) == 1:
                node[1] = -1

            if self.trimTree(node[2]) == 1:
                node[2] = -1

            if node[1] == -1 and node[2] == -1:
                if dataLen == 0:
                    return 1
                else:
                    return 0

    def find(self, node, findRange, start, end):
        '''recursively finds ids within a range'''
        data = []

        left = (start, node[0])
        right = (node[0], end)

        if self.overlap(left, findRange):
            data.extend(node[-1])
            if node[1] != -1:
                data.extend(self.find(node[1], findRange, left[0], left[1]))

        if self.overlap(right, findRange):
            data.extend(node[-1])
            if node[2] != -1:
                data.extend(self.find(node[2], findRange, right[0], right[1]))

        return list(set(data))

    def findRange(self, findRange):
        '''wrapper for find'''
        return self.find(self.tree, findRange, self.start, self.end)

    def pprint(self, ind):
        '''pretty prints the tree with indentation'''
        pp = pprint.PrettyPrinter(indent=ind)
        pp.pprint(self.tree)
        
###############################################################        

## represent query point (simplified)
class Point:
#    def __init__(self, x, y, cur_edge_id):
#        self.x = x
#        self.y = y
#        self.cur_edge_id = cur_edge_id
        
    def __init__(self, query):
        self.x = query.x
        self.y = query.y
        self.cur_edge_id = query.cur_edge_id    
        

class Node:
    def __init__(self, node_id, x, y):
        self.node_id = node_id
        self.x = x
        self.y = y
        
class Edge:
    def __init__(self, edge_id, start_node_id, end_node_id, edge_class):
        self.edge_id = edge_id
        self.start_node_id = start_node_id
        self.end_node_id = end_node_id
        self.edge_class = edge_class
            
    @staticmethod    
    def length(node_1, node_2):
        return math.sqrt((node_1.x - node_2.x)**2 + (node_1.y - node_2.y)**2)       
        
class Query:
    def __init__(self, obj_id, x=0, y=0, timestamp=0, next_node_x=0, next_node_y=0, 
                 next_node_id=0, cur_edge_id=0, k_anom=0, min_length=0, dist=0):      
        self.obj_id = obj_id
        self.x = x
        self.y = y
        self.timestamp = timestamp
        self.next_node_x = next_node_x
        self.next_node_y = next_node_y
        self.next_node_id = next_node_id
        self.cur_edge_id = cur_edge_id
        #
        self.k_anom = k_anom
        self.min_length = min_length
        self.dist = dist    # distance constraint
        
class MBR:
    def __init__(self, area, min_x, min_y, max_x, max_y):
        self.area = area
        self.min_x = min_x
        self.min_y = min_y      
        self.max_x = max_x
        self.max_y = max_y
        
    #######################################################
    @staticmethod
    def is_mmb_cover(mbr, point, radius):
        
        if (mbr.min_x <= point.x <= mbr.max_x) and (mbr.min_y - radius <= point.y <= mbr.max_y + radius):
            return True
        if (mbr.min_x - radius <= point.x <= mbr.max_x + radius) and (mbr.min_y <= point.y <= mbr.max_y):
            return True
        if get_distance(point.x, point.y, mbr.min_x, mbr.min_y) <= radius:
            return True
        if get_distance(point.x, point.y, mbr.min_x, mbr.max_y) <= radius:
            return True
        if get_distance(point.x, point.y, mbr.max_x, mbr.min_y) <= radius:
            return True
        if get_distance(point.x, point.y, mbr.max_x, mbr.max_y) <= radius:
            return True
        
        return False
          
        
class CloakingSet:
    def __init__(self, clique, query_list):
        self.clique = clique
        self.query_list = query_list
        self.mbr = CloakingSet.compute_mbr(query_list)
    
    #######################################################
    @staticmethod
    def compute_mbr(query_list):
        min_x = 100000000
        min_y = 100000000
        max_x = -100000000
        max_y = -100000000   
        for query in query_list:
            if min_x > query.x:
                min_x = query.x
            if min_y > query.y:
                min_y = query.y
            if max_x < query.x:
                max_x = query.x
            if max_y < query.y:
                max_y = query.y
        return MBR((max_x-min_x)*(max_y-min_y), min_x, max_x, min_y, max_y)
    
    
        
    
    
class EdgeSegment:
    def __init__(self, start_x, start_y, end_x, end_y, cur_edge_id):
        self.start_x = start_x
        self.start_y = start_y
        self.end_x = end_x
        self.end_y = end_y
        self.cur_edge_id = cur_edge_id
      
    def normalize(self):
        if self.start_x > self.end_x or (self.start_x == self.end_x and self.start_y > self.end_y):
            temp = self.start_x
            self.start_x = self.end_x
            self.end_x = temp
            temp = self.start_y
            self.start_y = self.end_y
            self.end_y = temp
      
    @staticmethod    
    def square_length(seg):
        return (seg.start_x - seg.end_x)**2 + (seg.start_y - seg.end_y)**2   
     
    @staticmethod    
    def length(seg):
        return math.sqrt((seg.start_x - seg.end_x)**2 + (seg.start_y - seg.end_y)**2)   
        
    @staticmethod        
    def is_line_cover(point, line):
        if (point.x == line.start_x and point.y == line.start_y) or \
            (point.x == line.end_x and point.y == line.end_y):
            return True
        if (line.start_x == line.end_x):
            return (point.y - line.start_y)*(point.y - line.end_y) < 0
        if (line.start_y == line.end_y):
            return (point.x - line.start_x)*(point.x - line.end_x) < 0
    
        return (point.y - line.start_y)*(point.y - line.end_y) < 0    
        
    @staticmethod
    def union(seg_1, seg_2):
        if seg_1.cur_edge_id != seg_2.cur_edge_id:
            return (False, None)
        
        # swap (not needed, already sorted)
        
        # case 0: conincide <start> OR <end> --> return the longer segment
        if (seg_1.start_x == seg_2.start_x and seg_1.start_y == seg_2.start_y) or \
            (seg_1.end_x == seg_2.end_x and seg_1.end_y == seg_2.end_y):
            if (EdgeSegment.square_length(seg_1) > EdgeSegment.square_length(seg_2)):
                return (True, seg_1)
            else:
                return (True, seg_2)
        
        # 
        if (seg_2.start_x < seg_1.end_x):
            if (seg_2.end_x >= seg_1.end_x):    #overlapped  
                return (True, EdgeSegment(seg_1.start_x, seg_1.start_y, 
                                            seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id ))
            else:                               #covered  
                return (True, seg_1)
            
        if (seg_1.start_x == seg_1.end_x):      #vertical !
            if (seg_1.end_y >= seg_2.start_y):  #not disjoint  
                if (seg_1.end_y < seg_2.end_y): #overlapped  
                    return (True, EdgeSegment(seg_1.start_x, seg_1.start_y, 
                                            seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id ))
                else:    
                    return (True, seg_1)        #covered  

        return (False, None)
    
    @staticmethod
    def intersect(seg_1, seg_2):     #Note: seg_1, seg_2 are already normalized
        # case 0: conincide <start> OR <end> --> return the shorter segment
        if (seg_1.start_x == seg_2.start_x and seg_1.start_y == seg_2.start_y) or \
            (seg_1.end_x == seg_2.end_x and seg_1.end_y == seg_2.end_y):
            if (EdgeSegment.square_length(seg_1) < EdgeSegment.square_length(seg_2)):
                return (True, seg_1)
            else:
                return (True, seg_2)
        
        # 
        if (seg_2.start_x < seg_1.end_x):
            if (seg_2.end_x >= seg_1.end_x):    #overlapped  
                return (True, EdgeSegment(seg_2.start_x, seg_2.start_y, 
                                            seg_1.end_x, seg_1.end_y, seg_1.cur_edge_id ))
            else:                               #covered  
                return (True, seg_2)
            
        if (seg_1.start_x == seg_1.end_x):      #vertical !
            if (seg_1.end_y >= seg_2.start_y):  #not disjoint  
                if (seg_1.end_y < seg_2.end_y): #overlapped  
                    return (True, EdgeSegment(seg_1.start_x, seg_1.start_y, 
                                            seg_2.end_x, seg_2.end_y, seg_1.cur_edge_id ))
                else:    
                    return (True, seg_2)        #covered  
        return (False, None)
    
    
class EdgeSegmentSet:
    def __init__(self):
        self.set = {}

    #######################################################
    @staticmethod
    def clean_fixed_expanding(result):
        new_result = result
        
        # 1. NORMALIZE each edge (left_low_x first)
        for item in new_result:
            item.normalize()
        
        # 2. SORT by cur_edge_id
#         print "new_result.size =", len(new_result)
        new_result = sorted(new_result, key=lambda edge_segment: (edge_segment.cur_edge_id, 
            edge_segment.start_x, edge_segment.start_y, edge_segment.end_x, edge_segment.end_y)) 
        
        # DEBUG
#        print "length(new_result) = ", length(new_result)
#        for item in new_result:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)
         
        # 3. REMOVE duplicates
        cur = 0
        while cur < len(new_result)-1:
            if new_result[cur+1].cur_edge_id == new_result[cur].cur_edge_id and \
                new_result[cur+1].start_x == new_result[cur].start_x and \
                new_result[cur+1].start_y == new_result[cur].start_y and \
                new_result[cur+1].end_x == new_result[cur].end_x and \
                new_result[cur+1].end_y == new_result[cur].end_y:
                new_result.pop(cur+1)
                continue
            else:
                cur += 1
                
#        print "length(new_result) = ", length(new_result)
#        for item in new_result:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)
        
        # 4. UNION
        cur = 0
        while cur < len(new_result)-1:
            test_union = EdgeSegment.union(new_result[cur], new_result[cur+1])
            if test_union[0] == True:
                new_result.pop(cur)
                new_result.pop(cur)
                new_result.insert(cur, test_union[1])
                continue
            else:
                cur += 1
                
        # DEBUG                
#        print "length(new_result) = ", length(new_result)
#        for item in new_result:
#            print "%15d %8.2f %10.2f %10.2f %10.2f %10.2f" % (item.cur_edge_id, EdgeSegment.length(item), \
#                item.start_x, item.start_y, item.end_x, item.end_y)    
        
         
        
        #
        return new_result


    #######################################################        
    @staticmethod        
    def union(set_1, set_2):   
        result = set_1
        result.extend(set_2)
        
        return EdgeSegmentSet.clean_fixed_expanding(result)
        
    #######################################################        
    @staticmethod        
    def intersect(set_1, set_2):        #Note: set_1, set_2 are already sorted by (cur_edge_id, start_x,...) 
        result = []  
        for item_1 in set_1:
            for item_2 in set_2:
                if item_1.cur_edge_id == item_2.cur_edge_id:
                    test_intersect = EdgeSegment.intersect(item_1, item_2)
                    if test_intersect[0] == True:
                        result.append(test_intersect[1])
#        result = sorted(result, key=lambda edge_segment: (edge_segment.cur_edge_id, 
#            edge_segment.start_x, edge_segment.start_y, edge_segment.end_x, edge_segment.end_y))
        
        return result
    
    #######################################################        
    @staticmethod  
    def length(set_1):
        total_len = 0
        for item in set_1:
            total_len += EdgeSegment.length(item)
        
        return total_len 
        
    
    #######################################################        
    @staticmethod        
    def is_set_cover(point, line_set):
        if len(line_set) == 0:
            return False
        
        
        #binary search
        lo = 0
        hi = len(line_set) - 1
        mid = (lo + hi) / 2
        found = False
        while True:
            if line_set[mid].cur_edge_id == point.cur_edge_id:
                found = True
                break
            if line_set[mid].cur_edge_id > point.cur_edge_id:
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
        
#        print found, mid
        
        #
        lo = mid
        while lo-1 > 0 and line_set[lo-1].cur_edge_id == point.cur_edge_id:
            lo = lo - 1
        hi = mid
        while hi+1 < len(line_set) and line_set[hi+1].cur_edge_id == point.cur_edge_id:
            hi = hi + 1
        
        for item in line_set[lo:hi+1]:
            if EdgeSegment.is_line_cover(point, item) == True:
                return True
        
        return False
    
    #######################################################
    @staticmethod
    def compute_mbr(mesh):
        min_x = 100000000
        min_y = 100000000
        max_x = -100000000
        max_y = -100000000   
        for seg in mesh:
            if min_x > seg.start_x:
                min_x = seg.start_x
            if min_y > seg.start_y:
                min_y = seg.start_y
            if max_x < seg.start_x:
                max_x = seg.start_x
            if max_y < seg.start_y:
                max_y = seg.start_y
            if min_x > seg.end_x:
                min_x = seg.end_x
            if min_y > seg.end_y:
                min_y = seg.end_y
            if max_x < seg.end_x:
                max_x = seg.end_x
            if max_y < seg.end_y:
                max_y = seg.end_y    
        return MBR((max_x-min_x)*(max_y-min_y), min_x, max_x, min_y, max_y)
            
#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":        
    
#    print EdgeSegment.is_line_cover(Point(1,1,0), EdgeSegment(0,0,2,2,0))
#    print EdgeSegment.is_line_cover(Point(0,0,0), EdgeSegment(0,0,2,2,0))
#    print EdgeSegment.is_line_cover(Point(2,2,0), EdgeSegment(0,0,2,2,0))
#    print EdgeSegment.is_line_cover(Point(0,1,0), EdgeSegment(0,0,0,2,0))
#    
#    print EdgeSegment.is_line_cover(Point(0,-1,0), EdgeSegment(0,0,0,2,0))
#    print EdgeSegment.is_line_cover(Point(-1,-1,0), EdgeSegment(0,0,2,2,0))
    
    mbr = MBR(200, 0, 0, 10, 20)
    print MBR.is_mmb_cover(mbr, Point(Query(-1, 10, 25)), 5)
        
        
        