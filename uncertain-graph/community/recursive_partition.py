'''
Created on Mar 26, 2015

@author: huunguye
    - package 'community' (http://perso.crans.org/aynaud/communities/)
    - recursively partition the graph by calling best_partition()
'''

import community
import time
import sys
import networkx as nx
from mcmc_inference import Dendrogram, build_node_dict
from mcmc_inference_degree import DendrogramDeg

MAX_ID = 1000000000

class Node:
    #######################################################
    def __init__(self, id, is_leaf):
        self.id = id
        self.is_leaf = is_leaf
        self.parent = None
        self.children = []      
        self.nodes = []
    
class BinNode:  # binary node for mcmc_inference.py
    #######################################################
    def __init__(self, id):
        self.id = id
        self.parent = None
        self.left = None
        self.right = None
        self.level = -1
        self.toplevel = 0
        

#######################################################
# node_list: list of BinNode(s)
def init_binary(node_list, id_list):
    if len(node_list) == 0:
        return None
    if len(node_list) == 1:
        return node_list[0] 
    
    mid = len(node_list)/2
    
    a_id = id_list.pop(0)       # get and remove an id (each id used only once)
    root_node = BinNode(a_id)   # internal node, id=-count (negative)
    
    left_node = init_binary(node_list[0:mid], id_list)    
    right_node = init_binary(node_list[mid:], id_list)         
    
    if left_node is not None:
        root_node.left = left_node
        left_node.parent = root_node
    if right_node is not None:
        root_node.right = right_node    
        right_node.parent = root_node
        
    return root_node
    
#######################################################
# build binary tree (BinNode) from root_node(Node)
def build_binary_tree(root_node, n_nodes):
    id_list = [-i for i in range(1,n_nodes)]
    
    # init
    node_list = [BinNode(MAX_ID) for _ in root_node.children]
    
    bin_root = init_binary(node_list, id_list)
    
    # two queues in parallel
    queue = []
    queue.extend(root_node.children)
    bin_queue = []
    bin_queue.extend(node_list)
    while len(queue) > 0:
        cur_node = queue.pop(0)
        bin_node = bin_queue.pop(0)     # from bin_queue
        #
        if cur_node.is_leaf == True:
            node_list = [BinNode(i) for i in cur_node.nodes]
            #
            mid_node = init_binary(node_list, id_list)
            
            # replace bin_node by mid_node !!
            if (bin_node.id == bin_node.parent.left.id):
                bin_node.parent.left = mid_node
                mid_node.parent = bin_node.parent 
            else:
                bin_node.parent.right = mid_node
                mid_node.parent = bin_node.parent 
        else:
            node_list = [BinNode(MAX_ID) for _ in cur_node.children]
            
            bin_queue.extend(node_list)
            mid_node = init_binary(node_list, id_list)
            
            # replace bin_node by mid_node !!
            if (bin_node.id == bin_node.parent.left.id):
                bin_node.parent.left = mid_node
                mid_node.parent = bin_node.parent 
            else:
                bin_node.parent.right = mid_node
                mid_node.parent = bin_node.parent 
            #
            queue.extend(cur_node.children)
            
            
        
            
    # 
    return bin_root            

#######################################################
# return root_node
def recursive_partition(G, max_size=100):
    
    result = []     # list of communities with size <= max_size
    queue = []
    node_queue = []
    node_id = 0
    root_node = Node(node_id, False)
    
    # first run
    partition = community.best_partition(G)
    
    for com in set(partition.values()):
        list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
        if len(list_nodes) <= max_size:
            node_id += 1
            a_node = Node(node_id, True)        # leaf
            a_node.parent = root_node
            a_node.nodes.extend(list_nodes)
            root_node.children.append(a_node)
            
            result.append(list_nodes)
        else:
            node_id += 1
            a_node = Node(node_id, False)       # non-leaf
            root_node.children.append(a_node)
            
            queue.append(list_nodes)
            node_queue.append(a_node)
    print "first run - DONE"
    
    # recursive
    count = 0
    while len(queue) > 0:
        nodes = queue.pop(0)
        cur_node = node_queue.pop(0)
        
        print "processing", count, "len(nodes) =", len(nodes)
        count += 1
        
        # get subgraph
        sG = nx.subgraph(G, nodes)
        partition = community.best_partition(sG)
        #
        if len(set(partition.values())) == 1:       # cannot partition further
            print "cannot partition further"
            list_nodes = [u for u in partition.keys()]
            
            node_id += 1
            a_node = Node(node_id, True)        # leaf
            a_node.parent = cur_node
            a_node.nodes.extend(list_nodes)
            cur_node.children.append(a_node)
            
            result.append(list_nodes)
        else:
            print "to sizes",
            for com in set(partition.values()):
                list_nodes = [u for u in partition.keys() if partition[u] == com]
                
                if len(list_nodes) <= max_size:
                    node_id += 1
                    a_node = Node(node_id, True)        # leaf
                    a_node.parent = cur_node
                    a_node.nodes.extend(list_nodes)
                    cur_node.children.append(a_node)
                    
                    result.append(list_nodes)
                    print "small", len(list_nodes),
                else:
                    node_id += 1
                    a_node = Node(node_id, False)       # non-leaf
                    a_node.parent = cur_node
                    cur_node.children.append(a_node)
                    
                    queue.append(list_nodes)
                    node_queue.append(a_node)
                    print "large", len(list_nodes),
            print
    
    #
    return result, root_node            
        
#######################################################
def print_leaves(root_node):
    queue = [root_node]
    while len(queue) > 0:
        cur_node = queue.pop(0)
        if cur_node.is_leaf == True:
            print "id =", cur_node.id,
            if cur_node.parent is not None:
                print "parent", cur_node.parent.id
            else:
                print "parent : None" 
            
            print cur_node.nodes
        else:
            for a_node in cur_node.children:
                queue.append(a_node)
                
#######################################################      
# in-order traversal
def in_order_print(bin_root):
    if bin_root.left is not None:
        in_order_print(bin_root.left) 
        
    print bin_root.id,
    
    if bin_root.right is not None:
        in_order_print(bin_root.right)

#######################################################
def write_to_file(root_node, filename):
    T = Dendrogram()
    T.root_node = root_node
    
    start = time.clock()
    T.node_dict, T.int_nodes = build_node_dict(T.root_node)
    print "build_node_dict - DONE, elapsed", time.clock() - start
    print "T.node_dict.size =", len(T.node_dict)
    print "T.int_nodes.size =", len(T.int_nodes) 
    
    T.node_list = [u for u in T.node_dict.itervalues() if u.id >= 0]
    T.write_internal_nodes(filename)        # 
    print "T.root_node.level =", T.root_node.level
            
#######################################################
if __name__ == '__main__':
    
#    G = nx.karate_club_graph();
    
    #######
#    dataname = "polbooks"           # (105, 441)     max_size=10
##    dataname = "polblogs"           # (1224,16715) 
#    dataname = "as20graph"          # (6474,12572)    max_size=50, 423 com(0.9s)
##    dataname = "wiki-Vote"          # (7115,100762)    max_size=50, 466 com(5.1s)
##    dataname = "ca-HepPh"           # (12006,118489)  max_size=50, 1061 com(6.7s)   
##    dataname = "ca-AstroPh"         # (18771,198050)   max_size=50, 1334 com(11.7s) 
#    # WCC
#    dataname = "polblogs-wcc"       # (1222,16714)     max_size=20
#    dataname = "wiki-Vote-wcc"      # (7066,100736)      max_size=50
#    dataname = "ca-HepPh-wcc"       # (11204,117619)    max_size=50
    dataname = "ca-AstroPh-wcc"     # (17903,196972)    max_size=50
    # LARGE
#    dataname = "com_amazon_ungraph" # (334863,925872) max_size=50,100, mem (1.4GB) (168s) 
#    dataname = "com_dblp_ungraph"  # (317080,1049866) max_size=50
#    dataname = "com_youtube_ungraph"# (1134890,2987624) max_size=50,100, mem (4.7GB) (2930s-Acer)
    

    max_size = 50
    n_runs = 1
    
    ######  Command-line param for automation (bash): <dataname> <n_runs> <max_size>
    if len(sys.argv) > 1:
        dataname = sys.argv[1]
    if len(sys.argv) > 2:
        n_runs = int(sys.argv[2])
    if len(sys.argv) > 3:
        max_size = int(sys.argv[3])


    filename = "../_data/" + dataname + ".gr"
    out_file = "../_out/" + dataname + "_louvain_dendro_" + str(max_size)

    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    # TEST recursive_partition()
    for i in range(n_runs):
        print "run i =", i
        
        start = time.clock()
        
        result, root_node = recursive_partition(G, max_size)
        print "recursive_partition - DONE, elapsed", time.clock() - start
        
#        k = 0
#        num_nodes = 0
#        for com in result:
#            print "community", k, "size :", len(com)
#            print com
#            num_nodes += len(com)
#            k += 1
#        
#        print "num_nodes =", num_nodes
        
        # TEST print_leaves
    #    print "print_leaves -----------"
    #    print_leaves(root_node)
        
        # TEST build_binary_tree()
        print "build_binary_tree"
        n_nodes = G.number_of_nodes()
        bin_root = build_binary_tree(root_node, n_nodes)
        print "build_binary_tree - DONE"
        
    #    in_order_print(bin_root)
    #    print
        
        # TEST write_to_file()
        start = time.clock()
        write_to_file(bin_root, out_file + "." + str(i))
        print "write_to_file - DONE, elapsed", time.clock() - start
        
        
    
    #### Read as20graph_louvain_dendro.0
#    T = DendrogramDeg()
#    T.read_internal_nodes(G, "../_out/" + dataname + "_louvain_dendro.0")
#    # polbooks:    -1054.08 < -1248.65 (max_size=50)  -957.80 (max_size=10)
#    # polblogs:   -66204.85 < -74316.88 (max_size=50)
#    # as20graph:  -71081.52 < -100341.97 (initBinary)
#    # wiki-Vote:  -576119.94 < -618928.62
#    # ca-HepPh:   -382722.13 < -875940.36 
#    # ca-AstroPh:    -904454.65 < -1541114.44  
#
#    print "T.logLK =", T.logLK(), "T.deg_diff_L1 =", T.deg_diff_L1()
#    # 385.58 < 386.99 (max_size=10) 342.59 (max_size=50)
#    # 28245.77 < 31468.96 (max_size=50)
#    # 23485.30 > 23317.28 (!!!)
#    # 234635.72 < 234779.35 (little)
#    # 142560.09 < 276638.36
#    # 276241.66 < 369789.80
#
#    ## edgeVar
#    # polblogs:   15102.01 < 16061.11 (max_size=50)
#    # as20graph:  11204.50 < 12509.93 (initBinary)
#    # wiki-Vote:  96881.08 < 99121.59
#    # ca-HepPh:   70568.52 < 118238.01
#    # ca-AstroPh:    148931.81 < 197784.75
    
    
                 
                                            
    
    
    