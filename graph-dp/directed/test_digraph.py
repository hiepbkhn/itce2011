'''
Created on Jan 13, 2015

@author: huunguye
'''

import time
import sys
import networkx as nx
import igraph as ig
import snap as sn
import matplotlib.pyplot as plt
from heapq import *
import scipy.io
import numpy as np


#######################################################
if __name__ == '__main__':
    
#    start = time.clock()
#    G = nx.read_edgelist("../data/test_directed.gr", '#', '\t', create_using=nx.DiGraph(), nodetype=int)    
##    G = nx.read_edgelist("../data/p2p-Gnutella04.txt", '#', '\t', create_using=nx.DiGraph(), nodetype=int)
##    G = nx.read_edgelist("../data/web_NotreDame_directed.gr", '#', '\t', create_using=nx.DiGraph(), nodetype=int)   # read 11.6s
#    
##    G = ig.Graph.Read_Edgelist("../data/test_directed.gr", directed=True)    
##    G = ig.Graph.Read_Edgelist("../data/p2p-Gnutella04.txt", directed=True)      # 
##    G = ig.Graph.Read_Edgelist("../data/web_NotreDame_directed.gr", directed=True)      # read 0.68s, zero out-deg nodes = 187788
##    G = ig.Graph.Read_Edgelist("../data/prod_amazon0302_directed.gr", directed=True)      # read 0.65s, zero out-deg nodes = 4541
#
##    G = sn.LoadEdgeList_PNGraph("../data/web_NotreDame_directed.gr", 0, 1)              # read 0.74s
#    
#    print "read graph, elapsed", time.clock() - start
    
    #---NETWORKX
#    print "#nodes", G.number_of_nodes()
#    print "#edges", G.number_of_edges()
#    # stochastic
#    count = 0
#    for (u, d_out) in G.out_degree_iter():
#        if d_out == 0:
#            count = count+1
#    print "#zero out-deg nodes =", count


    #---IGRAPH
#    print "#nodes", G.vcount()
#    print "#edges", G.ecount()    
#    
#    count = 0
#    for d_out in G.outdegree():
#        if d_out == 0:
#            count = count+1
#    print "#zero out-deg nodes =", count
#    
##    deg_dict = G.degree(mode="OUT")     # out-degrees
##    print deg_dict
##    for u in range(G.vcount()):
##        print G.neighbors(u, mode="OUT")     # successors (out-links)
            

    # 
#    vertex_cluster = G.components()     # strongly connected
#    print "#SCCs =", len(vertex_cluster)
#    print vertex_cluster.summary()
#    
#    maxSCC = vertex_cluster.giant()     # giant (SCC) component
#    print "giant SCC :", maxSCC.vcount(), "nodes", maxSCC.ecount(), "edges"
    
#    tc = G.triad_census()
#    print "#closed triangles =", tc["030C"]
##    print "#empty graphs =", tc["003"]
#    print "#single directed edges =", tc["012"]
#    print "#binary out-trees =", tc["021D"]
#    print "#binary in-trees =", tc["021U"]
#    print "#complete triangles =", tc["201"]
    
#    cc = G.transitivity_undirected()
#    print "clustering coefficient :", cc

#    acc = G.transitivity_avglocal_undirected()
#    print "avg clustering coeff :", acc
#    start = time.clock()
#    acc = G.transitivity_avglocal_undirected(mode="zero")       # 0.3s, same as SNAP computation
#    print "avg clustering coeff :", acc
#    print "elapsed ", time.clock() - start   

    # igraph directed
    aG = ig.Graph(directed=True)
    aG.add_vertices(3)
    aG.add_edges([(0,1),(0,2),(0,2),(2,0)])
    print "#nodes", aG.vcount()
    print "#edges", aG.ecount()   
    for e in aG.es:
        print e.source, e.target
    print aG.degree(0, mode="IN")      # 3
    
    
    #---SNAP
#    print "#nodes ", G.GetNodes()
#    print "#edges ", G.GetEdges()
#    result_degree = sn.TIntV()
#    sn.GetDegSeqV(G, result_degree)
#    max_deg = max(result_degree)
#    print "max_deg =", max_deg
#    start = time.clock()
#    print "avg clustering coeff :", sn.GetClustCf(G)
#    print "elapsed ", time.clock() - start                      # 4.7s
    
    
    # irreducible
    
    # aperiodic
    
    
    
    
    