'''
Created on Dec 3, 2014

@author: huunguye
paper: Computing Simulations on Finite and Infinite Graphs (FOCS'95)

use BITARRAY in place of SET --> NOT effective
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from bitarray import bitarray

#######################################################
def init_sim_simple(G, label):
    sim = {}
    # SLOW
#    for u in G.nodes_iter():
#        u_set = set([])
#        for v in G.nodes_iter():
#            if label[v] == label[u]:
#                u_set = u_set | set([v])
#        sim[u] = u_set
    # FAST
    n_nodes = G.number_of_nodes()
    label_dict = {}     # dict value is a set
    for u in G.nodes_iter():
        key = label[u]
        if not label_dict.has_key(key):
            label_dict[key] = set([u])
        else:
            label_dict[key] = label_dict[key] | set([u])
    for a_set in label_dict.itervalues():
        for u in a_set:
            sim[u] = bitarray(n_nodes)  # bitarray
            sim[u].setall(0)
            for i in a_set:
                sim[u][i] = 1           # include i 
    #
    return sim
            

#######################################################
def init_sim_full(G, label):
    sim = {}
    for v in G.nodes_iter():
        u_set = set([])
        if G.out_degree(v) == 0:
            for u in G.nodes_iter():
                if label[u] == label[v]:
                    u_set = u_set | set([u])
        else:
            for u in G.nodes_iter():
                if label[u] == label[v] and G.out_degree(u) > 0:
                    u_set = u_set | set([u])
        sim[v] = u_set
    #
    return sim
        

#######################################################
def schematic_similarity(G):
    
    n_nodes = G.number_of_nodes()
    
    # use degree as label
    label = G.degree()  # dict of (in + out)degrees
#    label = G.in_degree()  # dict of in-degrees
#    label = G.out_degree()  # dict of out-degrees

    # 1-init sim{}, post{}
    start = time.clock()
    sim = init_sim_simple(G, label)
    
              
    print "init time :", time.clock() - start
    total_size = sum(sim[u].count() for u in G.nodes_iter())       # count() from bitarray   
    print "before : total_size =", total_size
#    print "before :", sim
    
    #
    post = {}
    for u in G.nodes_iter():
        post[u] = bitarray(n_nodes)  # bitarray
        post[u].setall(0)
        for i in G.neighbors_iter(u):
            post[u][i] = 1
    
    # 2-compute
    start = time.clock()
    while True:
        found = False
        for u in G.nodes_iter(): 
            for v in G.neighbors_iter(u):
                for w in G.nodes_iter():            # expensive
                    if sim[u][w] == True:
                        temp = post[w] & sim[v]
                        if temp.count() == 0:
                            sim[u][w] = 0       
    #                        print "remove", w
                            found = True
        if not found:
            break
    
    print "compute time :", time.clock() - start     
    total_size = sum(sim[u].count() for u in G.nodes_iter())          
    print "after : total_size =", total_size
#    print "after :", sim          


#######################################################
# USE first_time[] TO avoid full initiation of prevsim{}
def refined_similarity(G):
    
    # use degree as label
    label = G.degree()  # dict of (in + out)degrees
#    label = G.in_degree()  # dict of in-degrees
#    label = G.out_degree()  # dict of out-degrees

    # 1-init sim{}, post{}
    start = time.clock()
    sim = init_sim_full(G, label)
    prevsim = {}
    
    
    
    # useless ?
#    for u in G.nodes_iter():
#        prevsim[u] = set([v for v in G.nodes_iter()])    # 
              
    print "init time :", time.clock() - start
    
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "before : total_size =", total_size
#    print "before :", sim
    
    #
    post = {}
    for u in G.nodes_iter():
        post[u] = set(G.neighbors_iter(u))
    
    # 2-compute
    start = time.clock()
    first_time = [True for u in G.nodes_iter()]    # to avoid full initiation of prevsim{}
    while True:
        found = False
        for v in G.nodes_iter():
            if first_time[v] or (len(sim[v]) < len(prevsim[v])):
                found = True
                break
        if not found:
            break
        # I1
        
        # I2
        
        # compute remove
        pre_prevsim = set([])
        if first_time[v]:
            for t in G.nodes_iter():
                pre_prevsim = pre_prevsim | set(G.predecessors_iter(t))
        else: 
            for t in prevsim[v]:
                pre_prevsim = pre_prevsim | set(G.predecessors_iter(t))
                
        pre_sim = set([])
        for t in sim[v]:
            pre_sim = pre_sim | set(G.predecessors_iter(t))
        
        first_time[v] = False 
        remove = pre_prevsim - pre_sim
        
        # update sim, prevsim
        for u in G.predecessors_iter(v):
            sim[u] = sim[u] - remove
        
        prevsim[v] = set(sim[v])
        
    
    print "compute time :", time.clock() - start
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "after : total_size =", total_size
#    print "after :", sim  

#######################################################
# NOT USE first_time[]
def refined_similarity_2(G):
    
    # use degree as label
    label = G.degree()  # dict of (in + out)degrees
#    label = G.in_degree()  # dict of in-degrees
#    label = G.out_degree()  # dict of out-degrees

    # 1-init sim{}, prevsim{}
    start = time.clock()
    sim = init_sim_full(G, label)
    prevsim = {}
    
    # useless ?
    for u in G.nodes_iter():
        prevsim[u] = set([v for v in G.nodes_iter()])    # 
              
    print "init time :", time.clock() - start
    
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "before : total_size =", total_size
#    print "before :", sim
    
    
    # 2-compute
    start = time.clock()
    while True:
        found = False
        for v in G.nodes_iter():
            if len(sim[v]) < len(prevsim[v]):
                found = True
                break
        if not found:
            break
        # I1
        
        # I2
        
        # compute remove
#        pre_prevsim = set([])
#        for t in prevsim[v]:
#            pre_prevsim = pre_prevsim | set(G.predecessors_iter(t))
#                
#        pre_sim = set([])
#        for t in sim[v]:
#            pre_sim = pre_sim | set(G.predecessors_iter(t))
#        remove = pre_prevsim - pre_sim

        # faster with dict !
        pre_prevsim = {}
        for t in prevsim[v]:
            for t1 in G.predecessors_iter(t):
                pre_prevsim[t1] = 1
                
        pre_sim = {}
        for t in sim[v]:
            for t1 in G.predecessors_iter(t):
                pre_sim[t1] = 1
                pre_prevsim[t1] = 0
        
#        remove = set(pre_prevsim.iterkeys()) - set(pre_sim.iterkeys())
        
        remove_list = []
        for (t,val) in pre_prevsim.iteritems():
            if val == 1:
                remove_list.append(t)
        remove = set(remove_list)
        
        # update sim, prevsim
        for u in G.predecessors_iter(v):
            sim[u] = sim[u] - remove
        
        prevsim[v] = sim[v]
        
    
    print "compute time :", time.clock() - start
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "after : total_size =", total_size
#    print "after :", sim  

#######################################################
# 
def efficient_similarity(G):
    
    # use degree as label
    label = G.degree()  # dict of (in + out)degrees
#    label = G.in_degree()  # dict of in-degrees
#    label = G.out_degree()  # dict of out-degrees

    # 1-init sim{}, prevsim{}, remove{}
    start = time.clock()
    sim = init_sim_full(G, label)
    prevsim = {}
    remove = {}
    
    # 
    list_V = []
    for t in G.nodes_iter():
        if G.out_degree(t) > 0:
            list_V.append(t)
    pre_V = set(list_V) 
    
    for v in G.nodes_iter():
        prevsim[v] = set([u for u in G.nodes_iter()])    # 
        pre_sim = set([])
        for t in sim[v]:
            pre_sim = pre_sim | set(G.predecessors_iter(t))
        remove[v] = pre_V - pre_sim
        
              
    print "init time :", time.clock() - start
    
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "before : total_size =", total_size
#    print "before :", sim
    
    
    # 2-compute
    start = time.clock()
    while True:
        found = False
        for v in G.nodes_iter():
            if len(remove[v]) > 0:
                found = True
                break
        if not found:
            break
        
        # I3
        
        for u in G.predecessors_iter(v):
            for w in remove[v]:
                if w in sim[u]:
                    sim[u] = sim[u] - set([w])
                    for w2 in G.predecessors_iter(w):
                        if len(set(G.neighbors_iter(w2)) & sim[u]) == 0:
                            remove[u] = remove[u] | set([w2])
        
#        prevsim[v] = sim[v]    # only used for I3
        remove[v] = set([])
        
    
    print "compute time :", time.clock() - start
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "after : total_size =", total_size
#    print "after :", sim  


#######################################################
# USE count array
def efficient_similarity_2(G):
    
    # use degree as label
    label = G.degree()  # dict of (in + out)degrees
#    label = G.in_degree()  # dict of in-degrees
#    label = G.out_degree()  # dict of out-degrees

    # 1-init sim{}, prevsim{}, remove{}, count[]
    start = time.clock()
    sim = init_sim_full(G, label)
    prevsim = {}
    remove = {}
    
    # 
    list_V = []
    for t in G.nodes_iter():
        if G.out_degree(t) > 0:
            list_V.append(t)
    pre_V = set(list_V) 
    
    for v in G.nodes_iter():
        prevsim[v] = set([u for u in G.nodes_iter()])    # 
        pre_sim = set([])
        for t in sim[v]:
            pre_sim = pre_sim | set(G.predecessors_iter(t))
        remove[v] = pre_V - pre_sim
        
    #
    count = [[0 for u in G.nodes_iter()] for w2 in G.nodes_iter()]
    for w2 in G.nodes_iter():
        for u in G.nodes_iter():
            count[w2][u] = len(set(G.neighbors_iter(w2)) & sim[u])
        
              
    print "init time :", time.clock() - start
    
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "before : total_size =", total_size
#    print "before :", sim
    
    
    # 2-compute
    start = time.clock()
    while True:
        found = False
        for v in G.nodes_iter():
            if len(remove[v]) > 0:
                found = True
                break
        if not found:
            break
        
        # I3
        
        for u in G.predecessors_iter(v):
            for w in remove[v]:
                if w in sim[u]:
                    sim[u] = sim[u] - set([w])
                    for w2 in G.predecessors_iter(w):
                        count[w2][u] = count[w2][u]  - 1
                        if count[w2][u] == 0:
                            remove[u] = remove[u] | set([w2])
        
#        prevsim[v] = sim[v]    # only used for I3
        remove[v] = set([])
        
    
    print "compute time :", time.clock() - start
    total_size = sum(len(sim[u]) for u in G.nodes_iter())          
    print "after : total_size =", total_size
#    print "after :", sim  


#######################################################
#######################################################
if __name__ == '__main__':
    filename = "../data/er_dir_200_002.gr"      # directed
#    filename = "../data/er_dir_200_02.gr"      # directed
#    filename = "../data/er_dir_500_02.gr"      # directed
#    filename = "../data/er_dir_500_01.gr"      # directed
#    filename = "../data/er_dir_2000_005.gr"      # directed
    filename = "../data/er_dir_10000_0005.gr"      # directed,    (set) 28.7s,  bitarray (failed)

#    filename = "../data/sm_1000_005_11.gr"      # undirected,    6s
#    filename = "../data/sm_10000_005_11.gr"      # undirected
    
    G = nx.read_edgelist(filename, '#', '\t', create_using=nx.DiGraph(), nodetype=int)
    
    # TOY GRAPHS
#    G = nx.DiGraph()
##    G.add_edges_from([(0,1),(0,4),(0,6),(1,2),(1,3),(2,3),(3,6),(4,2),(5,0),(5,3),(5,6)])
#    G.add_edges_from([(0,1),(2,3)])
    
    print "#nodes", G.number_of_nodes()
    print "#edges", G.number_of_edges()
    
    print "-----schematic_similarity-----------"
    # TEST schematic_similarity
    start = time.clock()
    schematic_similarity(G)
    print "elapsed :", time.clock() - start
    
#    print "-----refined_similarity-----------"
#    # TEST refined_similarity
#    start = time.clock()
#    refined_similarity(G)
#    print "elapsed :", time.clock() - start
    
#    print "-----refined_similarity_2-----------"
#    # TEST refined_similarity_2
#    start = time.clock()
#    refined_similarity_2(G)
#    print "elapsed :", time.clock() - start

#    print "-----efficient_similarity-----------"
#    # TEST efficient_similarity
#    start = time.clock()
#    efficient_similarity(G)
#    print "elapsed :", time.clock() - start
    
#    print "-----efficient_similarity_2-----------"
#    # TEST efficient_similarity_2
#    start = time.clock()
#    efficient_similarity_2(G)
#    print "elapsed :", time.clock() - start
    