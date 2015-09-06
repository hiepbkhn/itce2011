'''
Created on May 30, 2014

@author: huunguye
'''

import time
import random
import math
import networkx as nx
import snap as sn
import igraph
import scipy.io
import scipy.sparse as sp
import numpy as np

N_BFS = 1000

#######################################################
def test_igraph(file_name):
    
#    print igraph.__version__

    # toy graph
#    g = igraph.Graph()
#    g.add_vertices(5)
#    g.add_edges([(0,1),(1,2),(1,3),(3,4)])
    
    # read graph
    start = time.clock()
    g = igraph.Graph().Read_Edgelist("../data/" + file_name + ".gr", directed=False)
    print "elapsed ", time.clock() - start   
    
    n_vertices = len(g.vs)
    print "#vertices ", len(g.vs)
    print "#edges ", len(g.es)
    max_deg = max(g.degree())
    print "max_deg =", max_deg
    
    # transitivity (global clustering coefficient
#    start = time.clock()
#    clustering_coeff = g.transitivity_undirected()
#    print "clustering_coeff =", clustering_coeff
#    print "elapsed ", time.clock() - start   
    
    # BFS
    start = time.clock()
    perm = np.random.permutation(n_vertices)    # random N_BFS vertices
    v_list = perm[:N_BFS]
    
    length_dict = {}
    count = 0
    for v in v_list:
#        count += 1
#        if count % 10 == 0:
#            print count
        path_lengths = g.shortest_paths_dijkstra(v)
        for l in path_lengths[0]:
            if not length_dict.has_key(l):
                length_dict[l] = 1
            else:
                length_dict[l] += 1
    
    #
    print length_dict
    print "elapsed ", time.clock() - start   
    
#######################################################
def test_snap(file_name):
    
    start = time.clock()
    g = sn.LoadEdgeList_PUNGraph("../data/" + file_name + ".gr", 0, 1)
    print "elapsed ", time.clock() - start   
    
    print "#nodes ", g.GetNodes()
    print "#edges ", g.GetEdges()
    result_degree = sn.TIntV()
    sn.GetDegSeqV(g, result_degree)
    max_deg = max(result_degree)
    print "max_deg =", max_deg
    
    # transitivity (global clustering coefficient
#    start = time.clock()
#    clustering_coeff = sn.GetClustCf(g)
#    print "clustering_coeff =", clustering_coeff
#    print "elapsed ", time.clock() - start    
    
    # BFS
    start = time.clock()
    s_Diam = sn.GetBfsFullDiam(g, N_BFS, False)
    print "s_Diam =", s_Diam
    print "elapsed ", time.clock() - start    
    
#######################################################
def test_networkx(file_name):     
    
    start = time.clock()
    g = nx.read_edgelist("../data/" + file_name + ".gr", '#', '\t', None, nodetype=int, data=False)
    print "elapsed ", time.clock() - start 
    
    print "#nodes =", g.number_of_nodes()
    print "#edges =", g.number_of_edges()
    
    deg_list = nx.degree(g) 
    max_deg = max(deg_list.itervalues())
    print "max_deg =", max_deg
    
    #
    start = time.clock()
    clustering_coeff = nx.transitivity(g)
    print "clustering_coeff =", clustering_coeff
    print "elapsed ", time.clock() - start    
    

#######################################################
if __name__ == '__main__':
    
    ## 100k 
#    file_name = "er_100000_00001"       #100k nodes
#    file_name = "sm_100000_005_11"       #100k nodes
#    file_name = "ba_100000_5"       #100k nodes

    ## real graphs
#    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  
    
    # TEST test_igraph(), test_snap(), test_networkx()
    print "file_name =", file_name
    
    print "test_igraph()"
    start = time.clock()
    test_igraph(file_name)
    print "elapsed ", time.clock() - start   
    
#    print "test_snap()"
#    start = time.clock()
#    test_snap(file_name)
#    print "elapsed ", time.clock() - start  
    
#    print "test_networkx()"
#    start = time.clock()
#    test_networkx(file_name)
#    print "elapsed ", time.clock() - start  
    
    
    
    