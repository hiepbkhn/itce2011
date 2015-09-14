'''
Created on Jan 27, 2015

@author: huunguye
    Apr 21
    - test infomap, multilevel, leading_eigenvector, label_propagation
    - test multilevel on Live Journal (4M,34M)
'''

import time
import sys
import networkx as nx
import igraph as ig
#import snap as sn
import matplotlib.pyplot as plt
from heapq import *
import scipy.io
import numpy as np


#######################################################
def get_clusters(vertex_cluster):
    subgraphs = vertex_cluster.subgraphs()
    clusters = []   # list of lists of node ids
    for sg in subgraphs:
#        print sg.vcount()
#        print sg.ecount()
#        print "Nodes :", [v['id'] for v in sg.vs] 
#        cl = [int(v['id']) for v in sg.vs]            # for .gml graphs
        cl = [v for v in sg.vs]                         # for .gr graphs
        clusters.append(cl)
    #
    return clusters
        
#######################################################
def test_fastgreeedy(G):
    dendro = G.community_fastgreedy()
    
    vertex_cluster = dendro.as_clustering(2)     # 2 cluster-cut
    print "fastgreeedy          modularity =", G.modularity(vertex_cluster)
    
    print vertex_cluster.summary()

    
    #
    return get_clusters(vertex_cluster)
        
#######################################################
def test_infomap(G):
    vertex_cluster = G.community_infomap()
    print "infomap              modularity =", G.modularity(vertex_cluster)
    
    print vertex_cluster.summary()

    #
    return get_clusters(vertex_cluster)

#######################################################
def test_multilevel(G):
    start = time.clock()
    vertex_cluster = G.community_multilevel()
    print "multilevel           modularity =", G.modularity(vertex_cluster)
#    print "community_multilevel - DONE, elapsed", time.clock() - start
    
    print vertex_cluster.summary()

    #
    return get_clusters(vertex_cluster)

#######################################################
def test_leading_eigenvector(G):
    vertex_cluster = G.community_leading_eigenvector()
    print "leading_eigenvector  modularity =", G.modularity(vertex_cluster)
    
    print vertex_cluster.summary()

    #
    return get_clusters(vertex_cluster)


#######################################################
def test_label_propagation(G):
    vertex_cluster = G.community_label_propagation()
    print "label_propagation    modularity =", G.modularity(vertex_cluster)
    
    print vertex_cluster.summary()

    #
    return get_clusters(vertex_cluster)



#######################################################
if __name__ == '__main__':
    PATH = "../_data/"
#    FILE_NAME = PATH + "karate.gml"
#    FILE_NAME = PATH + "adjnoun.gml"
#    G = ig.Graph.Read(FILE_NAME, format="gml")
    
    FILE_NAME = PATH + "karate.gr"
#    FILE_NAME = PATH + "polbooks.gr"
#    FILE_NAME = PATH + "polblogs.gr"
#    FILE_NAME = PATH + "as20graph.gr"
#    FILE_NAME = PATH + "wiki-Vote.gr"
#    FILE_NAME = PATH + "ca-HepPh.gr"
#    FILE_NAME = PATH + "ca-AstroPh.gr"
#    FILE_NAME = PATH + "com_amazon_ungraph.gr"
#    FILE_NAME = PATH + "com_dblp_ungraph.gr"
#    FILE_NAME = PATH + "com_youtube_ungraph.gr"
    G = ig.Graph.Read_Edgelist(FILE_NAME, directed=False)

##    G = ig.Graph.Read_Edgelist("../_data/com_amazon_ungraph.gr", directed=False)    # 254 clusters, 6.3s
#    G = ig.Graph.Read_Edgelist("../_data/com_dblp_ungraph.gr", directed=False)    # 278 clusters, 5s
##    G = ig.Graph.Read_Edgelist("../_data/com_youtube_ungraph.gr", directed=False)   # 13517 clusters, 11.5s
##    PATH = "E:/Tailieu/Paper-code/DATA-SET/SNAP/Networks with ground-truth communities/"
##    G = ig.Graph.Read_Edgelist(PATH + "com_lj_ungraph.gr", directed=False) # (3997962, 34681189)   (mem 2.8GB, 286s), 7322 clusters
    
    print "#nodes", G.vcount()
    print "#edges", G.ecount()   
    
    
    # TEST community detection algorithms
    start = time.clock()
    clusters = test_fastgreeedy(G)              #
    print "elapsed", time.clock() - start 
    start = time.clock()
    clusters = test_infomap(G)                  #
    print "elapsed", time.clock() - start
    start = time.clock()
    clusters = test_multilevel(G)               # (fast unfolding ...)
    print "elapsed", time.clock() - start
    start = time.clock()
    clusters = test_leading_eigenvector(G)      #
    print "elapsed", time.clock() - start
    start = time.clock() 
    clusters = test_label_propagation(G)        #
    print "elapsed", time.clock() - start 
    
#    print clusters
    
    
    # PLOT
#    nxG = nx.read_gml(FILE_NAME)
#    pos = nx.spring_layout(nxG)
##    nx.draw(nxG, pos)   # networkx draw()
##    #P.draw()            # pylab draw()
##    plt.show()          # display
#    
#    color = ["red", "green", "blue", "cyan", "white", "black", "yellow", "magenta"]
#    count = 0
#    for com in clusters:
#        count += 1
#        nx.draw_networkx_nodes(nxG, pos, com, node_size = 100, node_color = color[count % 8])
#    nx.draw_networkx_edges(nxG, pos, alpha=0.5)
#    plt.show()
    
    
    # Sep 9, 2015 - test modularity
#    G = ig.Graph()
#    G.add_vertices(4)
##    G.add_edges([(0,1),(1,2),(1,3),(2,3)])
#    G.add_edges([(0,1),(0,2),(0,3),(1,2),(1,3),(2,3)])      # clique
#    print "#nodes", G.vcount()
#    print "#edges", G.ecount()   
#    
#    membership = ig.VertexClustering(G, [0,0,0,0])
#    print "modularity", G.modularity(membership, weights=None)
#    
#    membership = ig.VertexClustering(G, [0,0,1,1])
#    print "modularity", G.modularity(membership, weights=None)
#
#    membership = ig.VertexClustering(G, [0,1,1,1])
#    print "modularity", G.modularity(membership, weights=None)
    
    
    