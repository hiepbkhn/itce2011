'''
Created on Dec 15, 2014

@author: huunguye
paper: Capturing Topology in Graph Pattern Matching (VLDB'11)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
import matplotlib.pyplot as plt


#######################################################
def draw_graph(G):
    pos = nx.spring_layout(G)
    
    nx.draw(G, pos)  # networkx draw()
    #P.draw()    # pylab draw()
    
    plt.show() # display

#######################################################
def generate_labels(G, label_list):
    labelG = {}
    for u in G.nodes_iter():
        idx = np.random.random_integers(0,len(label_list)-1)
        labelG[u] = label_list[idx]
    #
    return labelG

#######################################################
def init_sim_simple(Q, G, labelQ, labelG):
    sim = {}
    # G
    label_dict_G = {}     # dict value is a set
    for v in G.nodes_iter():
        key = labelG[v]
        if not label_dict_G.has_key(key):
            label_dict_G[key] = set([v])
        else:
            label_dict_G[key] = label_dict_G[key] | set([v])

    for u in Q.nodes_iter():
        if label_dict_G.has_key(labelQ[u]):
            a_set = label_dict_G[labelQ[u]]
            sim[u] = a_set
        else:
            sim[u] = set([])
    #
    return sim
            
        

#######################################################
def schematic_dual_similarity(Q, G, labelQ=None, labelG=None):
    
    # use degree as label
#    labelG = G.degree()  # dict of (in + out)degrees
##    labelG = G.in_degree()  # dict of in-degrees
##    labelG = G.out_degree()  # dict of out-degrees
#
#    labelQ = Q.degree()

    # random labels
#    labelG = generate_labels(G, ['A','B','C','D'])
#    labelQ = generate_labels(Q, ['A','B'])
    
    print "labelG :", labelG
    print "labelQ :", labelQ

    # 1-init sim{}, post{}
    start = time.clock()
    sim = init_sim_simple(Q, G, labelQ, labelG)
    
              
    print "init time :", time.clock() - start
    total_size = sum(len(sim[u]) for u in Q.nodes_iter())          
    print "before : total_size =", total_size
#    print "before :", sim
    
    # save
    sim_b = {}      # before
    for (k,v) in sim.iteritems():
        sim_b[k] = v
    
    #
    postG = {}
    for v in G.nodes_iter():
        postG[v] = set(G.neighbors_iter(v))
        
    preG = {}
    for v in G.nodes_iter():
        preG[v] = set(G.successors_iter(v))
        
    # 2-compute
    start = time.clock()
    while True:
        found = False
        # child nodes
        for u in Q.nodes_iter(): 
            for w in Q.neighbors_iter(u):   
                for v in sim[u]:
                    if len(postG[v] & sim[w]) == 0:
                        sim[u] = sim[u] - set([v])
#                        print "remove", v
                        found = True
        # parent nodes
        for u in Q.nodes_iter(): 
            for w in Q.successors_iter(u):   
                for v in sim[u]:
                    if len(preG[v] & sim[w]) == 0:
                        sim[u] = sim[u] - set([v])
#                        print "remove", v
                        found = True
        if not found:
            break
    
    print "compute time :", time.clock() - start     
    total_size = sum(len(sim[u]) for u in Q.nodes_iter())          
    print "after : total_size =", total_size
    for u in Q.nodes_iter():
        print "--node", u
        print sim_b[u]
        print sim[u]


#######################################################
if __name__ == '__main__':
    
#    filename = "E:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polblogs/polblogs.gml"     # (directed, multi)  2 components
##    filename = "E:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/karate/karate.gml"    # (undirected, simple)
##    filename = "E:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/dolphins/dolphins.gml" # (undirected, simple)
##    filename = "E:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/cond-mat/cond-mat.gml" # (undirected, simple)
##    filename = "E:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polbooks/polbooks.gml" # (undirected, simple)
##    filename = "E:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polbooks/polbooks-2.gml" # (undirected, simple)
#    
#    G = nx.gml.read_gml(filename)
#    print "#nodes =", G.number_of_nodes()
#    print "#edges =", G.number_of_edges()
#    
##    print G.nodes(True)
#    print "read_gml_graph: DONE"
#    
#    print "Directed ?", G.is_directed()
#    print "Multigraph ?", G.is_multigraph()
    
#    draw_graph(G)
    ##
    filename = "../data/er_dir_200_002.gr"      # directed
#    filename = "../data/er_dir_200_02.gr"      # directed
#    filename = "../data/er_dir_500_02.gr"      # directed
#    filename = "../data/er_dir_500_01.gr"      # directed
#    filename = "../data/er_dir_2000_005.gr"      # directed        0.6s, 238s, 61s, 204s, 150s
#    filename = "../data/er_dir_10000_0005.gr"      # directed,    28.7s

#    filename = "../data/sm_1000_005_11.gr"      # undirected,    6s
#    filename = "../data/sm_10000_005_11.gr"      # undirected

#    filename = "../data/ba_1000_5.gr"      # undirected,    (before 142k, after 98k)
#    filename = "../data/ba_10000_5.gr"      # undirected,    (before 14M, after 9M)    174s, 
    
    
#    G = nx.read_edgelist(filename, '#', '\t', create_using=nx.DiGraph(), nodetype=int)      # directed
    
    # TOY GRAPHS
    G = nx.DiGraph()
    G.add_edges_from([(0,1),(0,4),(0,6),(1,2),(1,3),(2,3),(3,6),(4,2),(5,0),(5,3),(5,6)])
#    G.add_edges_from([(0,1),(2,3)])
#    G.add_edges_from([(1,4)])
#    G.add_edges_from([(1,4)])
    labelG = {0: 'A', 1: 'C', 2: 'C', 3: 'B', 4: 'D', 5: 'C', 6: 'B'}
    
    Q = nx.DiGraph()
    Q.add_edges_from([(0,1),(0,2)])
    Q.add_edges_from([(1,2)])               # good matching
#    labelQ = {0: 'B', 1: 'B', 2: 'B'}
#    labelQ = {0: 'A', 1: 'C', 2: 'D'}       # bad matching
    labelQ = {0: 'C', 1: 'B', 2: 'B'}       # bad matching
    
    
    print "#nodes", G.number_of_nodes()
    print "#edges", G.number_of_edges()
    
    print "-----schematic_dual_similarity-----------"
    # TEST schematic_dual_similarity
    start = time.clock()
    schematic_dual_similarity(Q, G, labelQ, labelG)
    print "elapsed :", time.clock() - start
    
    
#    start = time.clock()
#    schematic_dual_similarity(G, Q, labelG, labelQ)     # swap Q and G
#    print "elapsed :", time.clock() - start









