'''
Created on Feb 14, 2015

@author: Nguyen Huu Hiep
paper: Differentially Private Network Data Release via Structural Inference (KDD'14)
    Mar 23:
    - dendrogram_fitting(): reduce recomputation of logLT 
    Mar 24:
    - add computeTopLevels() (top-down Node.toplevel) to speed up compute_nL_nR()
    - lowestCommonAncestor() runs faster using Node.level 
    Mar 25:
    - logLK() missed minus (-) in the formula of L
    Apr 10:
    - compute_node_levels(): faster
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import * 
from adj_matrix_visualizer import draw_adjacency_matrix

ROOT_NODE = 100000000
SAMPLE_FREQ = 100   # 1 of 100 steps

class Node:
    def __init__(self, id, parent, value):
        self.id = id
        self.parent = parent
        self.value = value
        self.noisy_value = 0.0            # noisy value: by Laplace noise
#        self.is_leaf = is_leaf        # leaf if id >= 0
        self.left = None
        self.right = None
        #
        self.nL = 0
        self.nR = 0
        self.nEdge = 0   # num of edges counted at this node
        self.noisy_nEdge = 0.0
        #
        self.level = -1
        self.toplevel = 0
    #######################################################    
    def copy(self):
        aNode = Node(0,None,0.0)
        
        # do not copy parent, left, right
        aNode.id = self.id
        aNode.value = self.value
        aNode.noisy_value = self.noisy_value
        aNode.nL = self.nL
        aNode.nR = self.nR
        aNode.nEdge = self.nEdge
        aNode.noisy_nEdge = self.noisy_nEdge
        if aNode.id >= 0:   # leaf nodes
            aNode.left = None
            aNode.right = None
        #
        return aNode
        
class Dendrogram:
    def __init__(self):
        self.root_node = None
        self.node_list = []     # list of leaf nodes
        self.node_dict = {}
        self.int_nodes = []     # list of ids of internal nodes
    
    #######################################################
    def copy(self):
        T2 = Dendrogram()
        
        # copy node_dict first
        T2.node_dict = {}
        for (k,u) in self.node_dict.iteritems():
            u2 = u.copy()   # clone u
            T2.node_dict[k] = u2
            
        # int_nodes
        T2.int_nodes.extend(self.int_nodes)
        
        # node_list
        T2.node_list = [None for _ in range(len(self.node_list))]
        for u in T2.node_dict.itervalues():
            if u.id >= 0:
                T2.node_list[u.id] = u
                
        # parent, left, right
        for u in self.node_dict.itervalues():
            u2 = T2.node_dict[u.id]
            if u.parent is not None:
                u2.parent = T2.node_dict[u.parent.id]
            if u.left is not None:
                u2.left = T2.node_dict[u.left.id]
            if u.right is not None:
                u2.right = T2.node_dict[u.right.id]
                
        # root_node
        T2.root_node = T2.node_dict[T.root_node.id]
        
        #
        return T2
                
    
    #######################################################
    def init_by_graph(self, G):
        # compute node_list[]
        for u in G.nodes_iter():
            node = Node(u,None,0.0)      # leaf node
            node.nL = 1                     # IMPORTANT in config_2(), config_3()
            self.node_list.append(node)
        
        # init binary
        self.root_node = init_binary(self.node_list,1)
        
        # compute node_dict{}
        self.node_dict, self.int_nodes = build_node_dict(self.root_node)
        
        #
        start = time.clock()
        self.compute_node_levels()
        self.compute_top_levels()
        
        build_dendrogram(self.node_list, self.node_dict, self.root_node, G)
        print "build_dendrogram - DONE, elapsed", time.clock() - start
#        in_order_print(self.root_node)

    #######################################################
    def init_by_internal_nodes(self, G, int_nodes):
        # compute node_list[]
        for u in G.nodes_iter():
            node = Node(u,None,0.0)      # leaf node
            node.nL = 1                     # IMPORTANT in config_2(), config_3()
            self.node_list.append(node)
        #
        self.root_node = init_bottom_up(self.node_list, int_nodes)
        
        #
        self.node_dict, self.int_nodes = build_node_dict(self.root_node)    
        
#        debug_print(self)
#        print "debug_print - DONE"
        
        #
        start = time.clock()
        self.compute_node_levels()
        self.compute_top_levels()
        
        build_dendrogram(self.node_list, self.node_dict, self.root_node, G)
        print "build_dendrogram - DONE, elapsed", time.clock() - start
#        in_order_print(self.root_node)
        

    #######################################################
    # r_node: internal node, not root
    def config_2(self, G, r_node):
        
        p_node = r_node.parent
        
        # CASE 1: t right
        if r_node.id == p_node.left.id:     
            t_node = r_node.right
            u_node = p_node.right
            # update nL, nR, nEdge, value
            r_node.nR = (t_node.nL + t_node.nR) + (u_node.nL + u_node.nR)
            p_node.nL = (t_node.nL + t_node.nR)
            
            n_st_u = p_node.nEdge
            n_st = r_node.nEdge
            n_tu = count_between_edges(G, t_node, u_node)
            n_su = n_st_u - n_tu
            assert n_su >= 0
            n_s_tu = n_st + n_su
            
            r_node.nEdge = n_s_tu
            r_node.value = float(n_s_tu)/(r_node.nL*r_node.nR)
            p_node.nEdge = n_tu
            p_node.value = float(n_tu)/(p_node.nL*p_node.nR) 
            
            #
            t_node.parent = p_node
            p_node.left = t_node
            if p_node.parent is None:
                r_node.parent = None        # change root
                self.root_node = r_node
            else:
                r_node.parent = p_node.parent
                if p_node.id == p_node.parent.left.id:  # update
                    p_node.parent.left = r_node
                else:
                    p_node.parent.right = r_node
            p_node.parent = r_node
            r_node.right = p_node
            
            
            
        # CASE 2: t left    
        if r_node.id == p_node.right.id:    
            t_node = r_node.left
            u_node = p_node.left
            # update nL, nR, nEdge, value
            r_node.nL = (t_node.nL + t_node.nR) + (u_node.nL + u_node.nR)
            p_node.nR = (t_node.nL + t_node.nR)
            
            n_st_u = p_node.nEdge
            n_st = r_node.nEdge
            n_tu = count_between_edges(G, t_node, u_node)
            n_su = n_st_u - n_tu
            assert n_su >= 0
            n_s_tu = n_st + n_su
            
            r_node.nEdge = n_s_tu
            r_node.value = float(n_s_tu)/(r_node.nL*r_node.nR)
            p_node.nEdge = n_tu
            p_node.value = float(n_tu)/(p_node.nL*p_node.nR) 
            
            #
            t_node.parent = p_node
            p_node.right = t_node
            if p_node.parent is None:   
                r_node.parent = None        # change root
                self.root_node = r_node
            else:
                r_node.parent = p_node.parent
                if p_node.id == p_node.parent.left.id:  # update
                    p_node.parent.left = r_node
                else:
                    p_node.parent.right = r_node
            p_node.parent = r_node
            r_node.left = p_node
    
        #
        return p_node
    
    #######################################################
    # r_node: internal node, not root
    # RETURN r_node (whereas config_2() return p_node)
    def config_3(self, G, r_node):
        
        p_node = r_node.parent
        
        # CASE 1: t right
        if r_node.id == p_node.left.id:     
            t_node = r_node.right
            u_node = p_node.right
            # update nL, nR, nEdge, value
            r_node.nR = (u_node.nL + u_node.nR)
            p_node.nL = (r_node.nL + r_node.nR)
            p_node.nR = (t_node.nL + t_node.nR)
            
            n_st_u = p_node.nEdge
            n_st = r_node.nEdge
            n_tu = count_between_edges(G, t_node, u_node)
            n_su = n_st_u - n_tu
            assert n_su >= 0
            n_su_t = n_st + n_tu
            
            r_node.nEdge = n_su
            r_node.value = float(n_su)/(r_node.nL*r_node.nR)
            p_node.nEdge = n_su_t
            p_node.value = float(n_su_t)/(p_node.nL*p_node.nR) 
            
            #
            t_node.parent = p_node
            p_node.right = t_node
            
            u_node.parent = r_node
            r_node.right = u_node
        
        # CASE 2: t left    
        if r_node.id == p_node.right.id:    
            t_node = r_node.left
            u_node = p_node.left
            # update nL, nR, nEdge, value
            r_node.nL = (u_node.nL + u_node.nR)
            p_node.nL = (t_node.nL + t_node.nR)
            p_node.nR = (r_node.nL + r_node.nR)
            
            n_st_u = p_node.nEdge
            n_st = r_node.nEdge
            n_tu = count_between_edges(G, t_node, u_node)
            n_su = n_st_u - n_tu
            assert n_su >= 0
            n_su_t = n_st + n_tu
            
            r_node.nEdge = n_su
            r_node.value = float(n_su)/(r_node.nL*r_node.nR)
            p_node.nEdge = n_su_t
            p_node.value = float(n_su_t)/(p_node.nL*p_node.nR) 
            
            #
            t_node.parent = p_node
            p_node.left = t_node
            
            u_node.parent = r_node
            r_node.left = u_node
        #
        return r_node

    #######################################################
    def logLK(self):
        L = 0.0
        queue = [self.root_node]
        while len(queue) > 0:
            r = queue.pop(0)
            if r.value > 0.0 and r.value < 1.0:
                L += -r.nL*r.nR * (-r.value * math.log(r.value) - (1-r.value)*math.log(1-r.value))
            
            if r.left.id < 0:            # only internal nodes
                queue.append(r.left)
            if r.right.id < 0:
                queue.append(r.right)    
        #
        return L
        
    #######################################################
    # leaf nodes' level = 1, int_node = max(left,right) + 1
    def compute_node_levels(self):
        # WAY-1
#        for u in self.node_list:
#            u.level = 0
#        while True:
#            # find nodes having leveled children
#            found = False
#            for u_id in self.int_nodes:
#                u = self.node_dict[u_id]
#                if u.level == -1:
#                    if u.left.level != -1 and u.right.level != -1:
#                        u.level = max([u.left.level, u.right.level]) + 1
#                        found = True
#                        break
#            if not found:
#                break
        # WAY-2
        if self.node_list[0].toplevel == 0:
            self.compute_top_levels();          # call compute_top_levels()
            
        toplevel_nodes = {}
        max_toplevel = 0
        for u in self.node_dict.itervalues():
            if u.id >= 0:
                continue
            if not toplevel_nodes.has_key(u.toplevel):
                toplevel_nodes[u.toplevel] = [u]
            else:
                toplevel_nodes[u.toplevel].append(u)
            if max_toplevel < u.toplevel:
                max_toplevel = u.toplevel
                
        # init leaf nodes
        for u in self.node_list:
            u.level = 0
        for i in range(max_toplevel,-1,-1):   # max_toplevel-1: for internal nodes only
            for u in toplevel_nodes[i]:
                if u.level != -1:
                    continue
                if u.left.level == -1 and u.right.level == -1:
                    print "level ERROR at", u.id, " u.toplevel =", u.toplevel
                u.level = max([u.left.level, u.right.level]) + 1   
        
            
    #######################################################
    # root_node's level = 0, int_node = parent + 1
    def compute_top_levels(self):
        self.root_node.toplevel = 0
        queue = [self.root_node]
        while len(queue) > 0:
            cur_node = queue.pop(0)
            if cur_node.left is not None:
                cur_node.left.toplevel = cur_node.toplevel + 1;
                cur_node.right.toplevel = cur_node.toplevel + 1;
                queue.append(cur_node.left)
                queue.append(cur_node.right);      
        
    #######################################################
    def generate_sanitized_sample(self, n_nodes):
    
        self.compute_top_levels()
        
        aG = nx.Graph()
        aG.add_nodes_from(range(n_nodes))
        
        #
        for u_id in range(n_nodes):
            u = self.node_list[u_id]
            for v_id in range(u_id+1, n_nodes):
                v = self.node_list[v_id]
                a_id = lowest_common_ancestor(u, v)     # lowest common ancestor
                a_node = self.node_dict[a_id]
                #
                rand_val = random.random()
                if rand_val < a_node.noisy_value:
                    aG.add_edge(u_id, v_id)
                    
        #
        return aG

    #######################################################
    # phase 2: add noises to internal nodes
    def add_laplace_noise(self, eps2):
        
        queue = [self.root_node]
        
        while len(queue) > 0:
            cur_node = queue.pop(0)
            cur_node.noisy_nEdge = cur_node.nEdge + random.laplace(0.0, 1.0/eps2) 
            prob = cur_node.noisy_nEdge/(cur_node.nL*cur_node.nR)
            if prob < 0.0:
                prob = 0.0
            elif prob > 1.0:
                prob = 1.0
            cur_node.noisy_value = prob
            
            #
            if cur_node.left.id < 0:            # only internal nodes
                    queue.append(cur_node.left)
            if cur_node.right.id < 0:
                queue.append(cur_node.right)
                    
    #######################################################            
    def write_internal_nodes(self, filename):
        
        start = time.clock()
        self.compute_node_levels()              # compute node levels
        print "compute_node_levels - DONE, elapsed", time.clock() - start
        
        f = open(filename, 'w')
        for level in range(1, self.root_node.level + 1):
            for node in self.node_dict.itervalues():
                if node.id >= 0 or node.level != level:     # print by level (ascending)
                    continue
                if node.parent is not None:
                    parent_id = node.parent.id
                else:
                    parent_id = ROOT_NODE
                f.write("%d %d %d %d\n"%(node.id, parent_id, node.left.id, node.right.id))
        f.close()
        
    #######################################################        
    def read_internal_nodes(self, G, filename):
        f = open(filename, 'r')
        fstr = f.read()
        f.close()
        
        int_nodes = []  # list of tuples (id, parent.id, left.id, right.id)
        for line in fstr.split("\n"):
            if len(line) == 0:
                break
            items = line.split(" ")
            int_nodes.append((int(items[0]), int(items[1]), int(items[2]), int(items[3])))
        #
        self.init_by_internal_nodes(G, int_nodes)

########################################################      
## recursive  
#def init_binary(node_list):
#    if len(node_list) == 0:
#        return None
#    if len(node_list) == 1:
#        return node_list[0] 
#    
#    mid = len(node_list)/2
#    mid_node = node_list[mid]
#    
#    left_node = init_binary(node_list[0:mid])    
#    right_node = init_binary(node_list[mid+1:])         
#    
#    if left_node is not None:
#        mid_node.left = left_node
#        left_node.parent = mid_node.id
#    if right_node is not None:
#        mid_node.right = right_node    
#        right_node.parent = mid_node.id
#        
#    return mid_node
        
#######################################################      
# recursive, negative ids for internal nodes
# node_list: list of leaf nodes
def init_binary(node_list, count):
    if len(node_list) == 0:
        return None
    if len(node_list) == 1:
        return node_list[0] 
    
    mid = len(node_list)/2
    root_node = Node(-count,None,0.0)   # internal node, id=-count (negative)
    
    left_node = init_binary(node_list[0:mid], count*2)    
    right_node = init_binary(node_list[mid:], count*2+1)         
    
    if left_node is not None:
        root_node.left = left_node
        left_node.parent = root_node
    if right_node is not None:
        root_node.right = right_node    
        right_node.parent = root_node
        
    return root_node

#######################################################
# node_list: list of leaf nodes
# int_nodes: internal nodes, tuples of (id, parent.id, left.id, right.id) in bottom-up order
def init_bottom_up(node_list, int_nodes):
    #
    int_dict = {}
    for u in int_nodes:
        int_dict[u[0]] = Node(u[0],None,0.0)        # u[0]: u.id
    
    for u in int_nodes:
        if u[1] == ROOT_NODE:  # root                # u[1]: u.parent.id
            int_dict[u[0]].parent = None
            root_node = int_dict[u[0]] 
        else:
            int_dict[u[0]].parent = int_dict[u[1]]
            
        if u[2] >= 0:
            int_dict[u[0]].left = node_list[u[2]]   # point to leaf node
            node_list[u[2]].parent = int_dict[u[0]]
        else:        
            int_dict[u[0]].left = int_dict[u[2]]
            
        if u[3] >= 0:
            int_dict[u[0]].right = node_list[u[3]]  # point to leaf node
            node_list[u[3]].parent = int_dict[u[0]]
        else:    
            int_dict[u[0]].right = int_dict[u[3]]
    #
    return root_node

#######################################################
def find_children(u):
    if u.id >= 0:
        return [u.id]
        
    u_list = []
    queue = [u]
    while len(queue) > 0:
        cur_node = queue.pop(0)
        if cur_node.left.id >= 0:
            u_list.append(cur_node.left.id)
        else:
            queue.append(cur_node.left)
            
        if cur_node.right.id >= 0:
            u_list.append(cur_node.right.id)
        else:
            queue.append(cur_node.right)
    #
    return u_list    

#######################################################
# u, t: internal nodes
def count_between_edges(G, u, t):
    
    # find children of u, t
    u_list = find_children(u)
    t_list = find_children(t)
    
    #
    count = 0
    for u1 in u_list:
        for t1 in t_list:
            if G.has_edge(u1, t1):
                count += 1
    #
    return count


#######################################################      
def build_node_dict(root_node):
    node_dict = {}
    queue = [root_node]
    while len(queue) > 0:
        cur_node = queue.pop(0)
        node_dict[cur_node.id] = cur_node
        
#        if cur_node.left.id < 0:            # only internal nodes
#            queue.append(cur_node.left)
#        if cur_node.right.id < 0:
#            queue.append(cur_node.right)    
        
        if cur_node.left is not None:       # all nodes
            queue.append(cur_node.left)
        if cur_node.right is not None:
            queue.append(cur_node.right)    
    
    #
    int_nodes = []
    for (k,v) in node_dict.iteritems():
        if k < 0:
            int_nodes.append(k)
    return node_dict, int_nodes

#######################################################
# find root from any node
def find_root(any_node):
    a_node = any_node
    while a_node.parent is not None:
        # DEBUG
#        print a_node.id
                 
        a_node = a_node.parent
        
    return a_node

#######################################################      
# in-order traversal
def in_order_print(root_node, nLR_on = False, value_on = True):
    nLeft = 0
    nRight = 0
    if root_node.left is not None:
        nLeft = in_order_print(root_node.left, nLR_on, value_on) 
        
    print root_node.id, 
    if nLR_on:
        print "\tnL =", root_node.nL, "nR =", root_node.nR, "nEdge =", root_node.nEdge,
    if value_on:
        print "value =", root_node.value,
    print 
    
    if root_node.right is not None:
        nRight = in_order_print(root_node.right, nLR_on, value_on)
    #
    return (nLeft + nRight + 1)

#######################################################
# return id of 
def lowest_common_ancestor(u, v):
    t1 = u
    t2 = v
    if (t1.toplevel > t2.toplevel):
        diff = t1.toplevel-t2.toplevel
        for _ in range(diff):
            t1 = t1.parent
    else:
        diff = t2.toplevel-t1.toplevel
        for _ in range(diff):
            t2 = t2.parent
    while t1.id != t2.id:
        t1 = t1.parent
        t2 = t2.parent
    return t1.id

#######################################################
def reset_nL_nR_nEdge(root_node):
    queue = [root_node]
    while len(queue) > 0:
        cur_node = queue.pop(0)
        cur_node.nL = 0
        cur_node.nR = 0
        cur_node.nEdge = 0 
        
        if cur_node.left.id < 0:            # only internal nodes
            queue.append(cur_node.left)
        if cur_node.right.id < 0:
            queue.append(cur_node.right)    
        
#######################################################
# bottom-up
def compute_nL_nR(root_node, node_dict):
    # array of level -> nodes
    level_array = [[] for _ in range(root_node.level+1)]
    for u in node_dict.itervalues():
        level_array[u.level].append(u);
    # bottom-up
    for u in level_array[0]:
        u.nL = 1
        u.nR = 0
    
    for i in range(1, root_node.level+1):
        for u in level_array[i]:
            u.nL = u.left.nL + u.left.nR;
            u.nR = u.right.nL + u.right.nR;
    

#######################################################
# compute values at internal nodes
def build_dendrogram(node_list, node_dict, root_node, G):
    
    # reset nL, nR, nEdge
#    root_node = find_root(node_list[0])
#    reset_nL_nR_nEdge(root_node)
    
    compute_nL_nR(root_node, node_dict)
    
    # compute nEdge
    for e in G.edges_iter():
        u = e[0]
        v = e[1]
#        print u, v
        # find lowest common ancestor
        a_id = lowest_common_ancestor(node_dict[u], node_dict[v])
        node_dict[a_id].nEdge += 1
    # compute value
    for u in node_dict.itervalues():
        if u.id < 0:    # internal nodes
            u.value = float(u.nEdge)/(u.nL*u.nR)
    

#######################################################
# Exponential mechanism by MCMC
# n_samples number of sample T
def dendrogram_fitting(T, G, eps1, n_steps, n_samples, sample_freq=SAMPLE_FREQ):
    list_T = []     # list of sample T
    
    # delta U
    n_nodes = G.number_of_nodes()
    if n_nodes % 2 == 0: 
        nMax = n_nodes*n_nodes/4
    else: 
        nMax = (n_nodes*n_nodes-1)/4 
    dU = math.log(nMax) + (nMax-1)*math.log(1+1.0/(nMax-1))
    print "dU =", dU
    print "#steps =", n_steps + n_samples*sample_freq
    
    out_freq = (n_steps + n_samples*sample_freq)/10
    
    # MCMC
    n_accept = 0
    logLT = T.logLK()
    
    for i in range(n_steps + n_samples*sample_freq):
        # randomly pick an internal node (not root)
        while True:
            ind = T.int_nodes[random.randint(len(T.int_nodes))]
            r_node = T.node_dict[ind]
            if r_node.parent is not None:
                break
        # randomly use config_2(), config_3()
        rand_val = random.randint(2)
        
        if rand_val == 0:   # config_2()
            p_node = T.config_2(G, r_node)
            logLT2 = T.logLK()
            
            prob = math.exp(eps1/(2*dU)*(logLT2-logLT))     # prefer larger values of logLT2
            if prob > 1.0:
                prob = 1.0
            prob_val = random.random()
            if prob_val > prob:      
                # reverse
                p_node = T.config_2(G, p_node)      # p_node
            else:
                n_accept += 1
                logLT = logLT2
            
        else:               # config_3()
            r_node = T.config_3(G, r_node)
            logLT2 = T.logLK()
            
            prob = math.exp(eps1/(2*dU)*(logLT2-logLT))     # prefer larger values of logLT2
            if prob > 1.0:
                prob = 1.0
            prob_val = random.random()
            if prob_val > prob:      
                # reverse
                r_node = T.config_3(G, r_node)      # r_node
            else:
                n_accept += 1
                logLT = logLT2
        #
        if i % out_freq == 0:
            print "i =", i, n_accept, time.clock()
        if i >= n_steps:
            if i % sample_freq == 0:
                T2 = T.copy()
                list_T.append(T2)
    #
    return list_T 
    

#######################################################
def debug_print(T):
    print "debug:"
    for node in T.node_dict.itervalues():
        if node.id >= 0:
            print node.id, "parent :", node.parent.id, "[", node.level, "]"
    for node in T.node_dict.itervalues():
        if node.id < 0:
            print node.id, node.left.id, node.right.id,     # , in the end to prevent new lines
            if node.parent is None:
                print "parent : None", "[", node.level, "]"
            else:
                print "parent :", node.parent.id, "[", node.level, "]"

#######################################################
def write_internal_nodes(list_T, node_file):
    
    for i in range(len(list_T)):
        T = list_T[i]
        T.compute_node_levels()              # compute node levels
        
        filename = node_file + "." + str(i)
        T.write_internal_nodes(filename)
        
#######################################################
# list_T: list of new Dendrogram()
def read_internal_nodes(G, list_T, node_file, n_samples=1):
    for i in range(n_samples):
        T = list_T[i]
        
        filename = node_file + "." + str(i)
        T.init_by_internal_nodes(G, filename)
    
#######################################################
def check_two_dendrograms(T, T2):
    queue = [T.root_node]
    queue2 = [T2.root_node]
    while len(queue) > 0:
        cur_node = queue.pop(0)    
        cur_node2 = queue2.pop(0)
        if cur_node.id != cur_node2.id or cur_node.nEdge != cur_node2.nEdge or \
            cur_node.nL != cur_node2.nL or cur_node.nR != cur_node2.nR:
            print "Error at", cur_node.id, cur_node2.id
            break
        if cur_node.left is not None:
            queue.append(cur_node.left)
        if cur_node.right is not None:
            queue.append(cur_node.right)
        if cur_node2.left is not None:
            queue2.append(cur_node2.left)
        if cur_node2.right is not None:
            queue2.append(cur_node2.right)
    

#######################################################
if __name__ == '__main__':
    # TOY graph
#    G = nx.Graph()
#    G.add_edges_from([(0,1),(1,2),(2,3),(3,4),(4,5),(5,6)])
##    G.add_edges_from([(0,1),(1,2),(0,2),(2,3),(3,4),(4,5),(3,5)])     # example in the paper 
    
#    dataname = "polbooks"       # (105, 441)     build_dendrogram 0.0052s
#    dataname = "polblogs"       # (1224,16715) build_dendrogram 0.306
    dataname = "as20graph"      # (6474,12572) build_dendrogram 0.16s, 75k fitting (424s)
#    dataname = "wiki-Vote"     # (7115,100762)
#    dataname = "ca-HepPh"      # (12006,118489)     
#    dataname = "ca-AstroPh"    # (18771,198050)    
    
    filename = "../_data/" + dataname + ".gr"
    node_file = "../_out/" + dataname + "_dendro"
    
    out_file = "../_sample/" + dataname + "_mcmc_10_10"    # 10 (eps1=1.0), 10 (eps2=1.0)
    
    print filename
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()

    T = Dendrogram()
    T.init_by_graph(G)
    
#    T.init_by_internal_nodes(G, [(-5,-2,1,2),(-6,-3,3,4),(-7,-3,5,6),(-2,-1,0,-5),
#                                 (-3,-1,-6,-7),(-1,ROOT_NODE,-2,-3)])
#    T.init_by_internal_nodes(G, [(-4,-2,0,1),(-2,-1,-4,2),(-5,-3,4,5),(-3,-1,3,-5),
#                                 (-1,ROOT_NODE,-2,-3)])                      # example in the paper 

#    debug_print(T)
#    in_order_print(T.root_node, True, True)
    
    print "logLK =", T.logLK()
    
    # init dendrogram
#    root_node = init_binary(node_list,1)
#    print "DONE"
##    in_order_print(root_node)

    # TEST Node.copy(), Dendrogram.copy()
#    u = T.root_node.copy()
#    print T.root_node.id, u.id
#    u.id = -100
#    print T.root_node.id, u.id
#    
#    T2 = T.copy()
#    print "T"
#    in_order_print(T.root_node)
#    debug_print(T)
#    print "T2"
#    in_order_print(T2.root_node)
#    debug_print(T2)
#    
#    print "change T"
#    r_node = T.node_dict[-5]
#    p_node = T.config_2(G, r_node)
#    print "T"
#    in_order_print(T.root_node)
#    debug_print(T)
#    print "T2"
#    in_order_print(T2.root_node)
#    debug_print(T2)
    
    

    # TEST build_node_dict()
#    node_dict = build_node_dict(root_node)
##    print "node_dict"
##    for (id, node) in node_dict.iteritems():
##        print id, node.id


    # TEST find_root()
#    root_node = find_root(node_list[0])
#    print "root_node.id =", root_node.id

    
    # TEST lowest_common_ancestor()
#    a_id = lowest_common_ancestor(node_dict[0], node_dict[2])
#    print "lowest_common_ancestor =", a_id
    
    
    # TEST compute_nL_nR()
#    compute_nL_nR(node_dict)
#    print "compute_nL_nR - DONE"
#    root_node = find_root(node_list[0])
#    in_order_print(root_node, True, False)
    
    # TEST build_dendrogram()
#    start = time.clock()
#    build_dendrogram(node_list, node_dict, G)
#    print "build_dendrogram - DONE, elapsed", time.clock() - start
#    root_node = find_root(node_list[0])
#    in_order_print(root_node)

    
    # TEST count_between_edges()
#    count = count_between_edges(G, node_dict[-6], node_dict[-7])
#    print "count =", count
    
    # TEST config_2(), config_3() + reverse
#    r_node = T.node_dict[-2]  # node_dict[-2], [-5]
#    
##    p_node = T.config_2(G, r_node)
##    print "config_2: DONE"
#    r_node = T.config_3(G, r_node)
#    print "config_3: DONE"
#    
#    print "logLK =", T.logLK()
#    print "--root_node.id =", T.root_node.id
#    in_order_print(T.root_node, True, True)
#    debug_print(T)
    
    # reverse
#    p_node = T.config_2(G, p_node)
#    print "config_2: reverse DONE"
##    r_node = T.config_3(G, r_node)
##    print "config_3: reverse DONE"
#    
#    print "logLK =", T.logLK()
##    print "--root_node.id =", T.root_node.id
##    in_order_print(T.root_node, True, True)
##    debug_print(T)
    
    
    # TEST write_internal_nodes(), read_internal_nodes(), check_two_dendrograms()
#    for _ in range(10):
#        # randomly pick an internal node (not root)
#        while True:
#            ind = T.int_nodes[random.randint(len(T.int_nodes))]
#            r_node = T.node_dict[ind]
#            if r_node.parent is not None:
#                break
#        #
#        T.config_2(G, r_node)       # config_3
#    print "transform T - DONE"
#    
#    write_internal_nodes([T], node_file)
#    print "write_internal_nodes - DONE"
    
#    T2 = Dendrogram()
#    read_internal_nodes(G, [T2], node_file, n_samples=1)
#    print "read_internal_nodes - DONE"
#    
#    check_two_dendrograms(T, T2)
#    print "check_two_dendrograms - DONE"
            
    # TEST dendrogram_fitting(), add_laplace_noise(), generate_sanitized_sample()
    start = time.clock()
    n_samples = 3
    sample_freq = 1000
    eps1 = 1.0
    eps2 = 1.0
    list_T = dendrogram_fitting(T, G, eps1, 1*G.number_of_nodes(), n_samples, sample_freq)     # 
    print "dendrogram_fitting - DONE, elapsed", time.clock() - start
    
    # check T after fitting
#    nNode = in_order_print(T.root_node, False, False)
#    print "#nodes in T", nNode
    
    for T in list_T:
        print "logLK =", T.logLK()
    print
    
    write_internal_nodes(list_T, node_file)
    print "write_internal_nodes - DONE"
    
    for T2 in list_T:
        T2.add_laplace_noise(eps2)
    print "add_laplace_noise - DONE"
    
    start = time.clock()
    count = 0
    for T2 in list_T:
        aG = T2.generate_sanitized_sample(G.number_of_nodes())          # 17.84s(polblogs), 80s/sample(as20graph)
        nx.write_edgelist(aG, out_file + "." + str(count), '#', '\t', False, 'utf-8')
        count += 1
    print "generate_sanitized_sample - DONE, elapsed", time.clock() - start
    
    
            
            
