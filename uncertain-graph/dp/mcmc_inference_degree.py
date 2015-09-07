'''
Created on Mar 18, 2015

@author: Nguyen Huu Hiep
paper: Differentially Private Network Data Release via Structural Inference (KDD'14)
    - dendrogram_fitting(): use L1-degree distance instead of loglikelihood
    - add_laplace_noise():  
    Mar 23:
    - dendrogram_fitting(): reduce recomputation of logDT 
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
from mcmc_inference import Node, Dendrogram, lowest_common_ancestor, write_internal_nodes, read_internal_nodes, debug_print

ROOT_NODE = 100000000
SAMPLE_FREQ = 100   # 1 of 10000 steps

class DendrogramDeg (Dendrogram):
    def __init__(self):
        Dendrogram.__init__(self)
        #
        self.deg_list = []        # expected degrees (of leaf nodes)
        self.org_deg_list = []    # original degrees in G
    
    ####################################################### <overrride>
    def init_by_graph(self, G):
        Dendrogram.init_by_graph(self, G)
        #
        self.org_deg_list = list(G.degree().itervalues())
        
    ####################################################### <overrride>
    def init_by_internal_nodes(self, G, int_nodes):
        Dendrogram.init_by_internal_nodes(self, G, int_nodes)    
        #
        self.org_deg_list = list(G.degree().itervalues())
    
    ####################################################### <overrride>
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
        
        # deg_list, org_deg_list
        T2.org_deg_list.extend(T.org_deg_list)
        
        #
        return T2
                
        
    #######################################################
    # compute expected degrees of nodes in T
    # called after build_dendrogram()
    def expected_degrees(self):
        self.deg_list = []
        for u in self.node_list:
            d = 0.0
            while True:
                prob = u.parent.value
                if u.id == u.parent.left.id:    # u is left
                    d += prob * (u.parent.right.nL + u.parent.right.nR) 
                else:                           # u is right
                    d += prob * (u.parent.left.nL + u.parent.left.nR)
                u = u.parent
                if u.parent is None:
                    break
            self.deg_list.append(d)
            
    #######################################################
    # L1-distance between deg_list and org_deg_list       
    def deg_diff_L1(self):
        # recompute expected_degrees()
        self.expected_degrees()
        
        dist = 0.0
        n_nodes = len(self.node_list)
        for u in range(n_nodes):
            dist += abs(self.deg_list[u] - self.org_deg_list[u])
        #
        return dist

#######################################################
# Exponential mechanism by MCMC
# n_samples number of sample T
def dendrogram_fitting(T, G, eps1, n_steps, n_samples, sample_freq=SAMPLE_FREQ):
    list_T = []     # list of sample T
    
    # delta U
    n_edges = G.number_of_edges()
    dU = math.log(2*n_edges)
    print "dU =", dU
    
    print "#steps =", n_steps + n_samples*sample_freq
    out_freq = (n_steps + n_samples*sample_freq)/10
    
    # MCMC
    n_accept = 0
    for i in range(n_steps + n_samples*sample_freq):
        # randomly pick an internal node (not root)
        while True:
            ind = T.int_nodes[random.randint(len(T.int_nodes))]
            r_node = T.node_dict[ind]
            if r_node.parent is not None:
                break
        # randomly use config_2(), config_3()
        rand_val = random.randint(2)
        cur_deg_diff_L1 = T.deg_diff_L1()
        
        if rand_val == 0:   # config_2()
            logDT = math.log(cur_deg_diff_L1)
            p_node = T.config_2(G, r_node)
            next_deg_diff_L1 = T.deg_diff_L1() 
            logDT2 = math.log(next_deg_diff_L1)
            
            prob = math.exp(eps1/(2*dU)*(logDT-logDT2))         # prefer smaller values of logDT2
            if prob > 1.0:
                prob = 1.0
            prob_val = random.random()
            if prob_val > prob:      
                # reverse
                p_node = T.config_2(G, p_node)      # p_node
            else:
                n_accept += 1
                cur_deg_diff_L1 = next_deg_diff_L1
            
        else:               # config_3()
            logDT = math.log(cur_deg_diff_L1)
            r_node = T.config_3(G, r_node)
            next_deg_diff_L1 = T.deg_diff_L1() 
            logDT2 = math.log(next_deg_diff_L1)
            
            prob = math.exp(eps1/(2*dU)*(logDT-logDT2))         # prefer smaller values of logDT2
            if prob > 1.0:
                prob = 1.0
            prob_val = random.random()
            if prob_val > prob:      
                # reverse
                r_node = T.config_3(G, r_node)      # r_node
            else:
                n_accept += 1
                cur_deg_diff_L1 = next_deg_diff_L1
        #
#        print i
        if i % out_freq == 0:       # out_freq
            print "i =", i, n_accept, time.clock()
        if i >= n_steps:
            if i % sample_freq == 0:
                T2 = T.copy()
                list_T.append(T2)
    #
    return list_T 
    

#######################################################
if __name__ == '__main__':
    # TOY graph
#    G = nx.Graph()
##    G.add_edges_from([(0,1),(1,2),(2,3),(3,4),(4,5),(5,6)])
#    G.add_edges_from([(0,1),(1,2),(0,2),(2,3),(3,4),(4,5),(3,5)])     # example in the paper 
    
    dataname = "polbooks"       # (105,441) build_dendrogram 0.0052s, 11.5k steps (18s)
#    dataname = "polblogs"       # DIRECTED, undirected (1224,16715) build_dendrogram 0.306
    dataname = "as20graph"      # (6474,12572) build_dendrogram 0.16s, 6.7k steps (677s), 650k (??s)
    
    filename = "../_data/" + dataname + ".gr"
    node_file = "../_out/" + dataname + "_deg_dendro"
    
    out_file = "../_sample/" + dataname + "_mcmc_deg_10_10"    # 10 (eps1=1.0), 10 (eps2=1.0)
    
    print filename
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()

    T = DendrogramDeg()
    T.init_by_graph(G)
    
#    T.init_by_internal_nodes(G, [(-5,-2,1,2),(-6,-3,3,4),(-7,-3,5,6),(-2,-1,0,-5),
#                                 (-3,-1,-6,-7),(-1,ROOT_NODE,-2,-3)])
#    T.init_by_internal_nodes(G, [(-4,-2,0,1),(-2,-1,-4,2),(-5,-3,4,5),(-3,-1,3,-5),
#                                 (-1,ROOT_NODE,-2,-3)])                      # example in the paper 
#    T.init_by_internal_nodes(G, [(-4,-3,0,1),(-3,-2,-4,2),(-5,-1,4,5),(-2,-1,-3,3),
#                                 (-1,ROOT_NODE,-2,-5)])                      # example in the paper 

#    debug_print(T)
#    in_order_print(T.root_node, True, True)
    
    print "logLK =", T.logLK()
#    print "expected degrees :", T.deg_list
    print "deg_diff_L1 =", T.deg_diff_L1()
    
            
    # TEST dendrogram_fitting(), add_laplace_noise(), generate_sanitized_sample()
    start = time.clock()
    n_samples = 3
    sample_freq = 1000
    eps1 = 1.0
    eps2 = 1.0
    list_T = dendrogram_fitting(T, G, eps1, 10*G.number_of_nodes(), n_samples, sample_freq)     # 
    print "dendrogram_fitting - DONE, elapsed", time.clock() - start
    
    for T2 in list_T:
        print "logLK =", T2.logLK(), "deg_diff_L1 =", T2.deg_diff_L1()
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
    
    
            
            
