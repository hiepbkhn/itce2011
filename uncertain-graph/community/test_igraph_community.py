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
def test_fastgreeedy(G):
    dendro = G.community_fastgreedy()
    
    vertex_cluster = dendro.as_clustering(2)     # 2 cluster-cut
    
    print vertex_cluster.summary()

    subgraphs = vertex_cluster.subgraphs()
    clusters = []   # list of lists of node ids
    for sg in subgraphs:
#        print sg.vcount()
#        print sg.ecount()
#        print "Nodes :", [v['id'] for v in sg.vs] 
        cl = [int(v['id']) for v in sg.vs] 
        clusters.append(cl)
    #
    return clusters
        
#######################################################
def test_infomap(G):
    vertex_cluster = G.community_infomap()
    
    print vertex_cluster.summary()

    subgraphs = vertex_cluster.subgraphs()
    clusters = []   # list of lists of node ids
    for sg in subgraphs:
#        print sg.vcount()
#        print sg.ecount()
#        print "Nodes :", [v['id'] for v in sg.vs] 
        cl = [int(v['id']) for v in sg.vs] 
        clusters.append(cl)
    #
    return clusters

#######################################################
def test_multilevel(G):
    start = time.clock()
    vertex_cluster = G.community_multilevel()
    print "community_multilevel - DONE, elapsed", time.clock() - start
    
    print vertex_cluster.summary()

    subgraphs = vertex_cluster.subgraphs()
    clusters = []   # list of lists of node ids
    for sg in subgraphs:
##        print sg.vcount()
##        print sg.ecount()
##        print "Nodes :", [v['id'] for v in sg.vs] 
#        cl = [int(v['id']) for v in sg.vs]            # for .gml graphs
        cl = [v for v in sg.vs]                         # for .gr graphs
        clusters.append(cl)
    #
    return clusters

#######################################################
def test_leading_eigenvector(G):
    vertex_cluster = G.community_leading_eigenvector()
    
    print vertex_cluster.summary()

    subgraphs = vertex_cluster.subgraphs()
    clusters = []   # list of lists of node ids
    for sg in subgraphs:
#        print sg.vcount()
#        print sg.ecount()
#        print "Nodes :", [v['id'] for v in sg.vs] 
        cl = [int(v['id']) for v in sg.vs] 
        clusters.append(cl)
    #
    return clusters


#######################################################
def test_label_propagation(G):
    vertex_cluster = G.community_label_propagation()
    
    print vertex_cluster.summary()

    subgraphs = vertex_cluster.subgraphs()
    clusters = []   # list of lists of node ids
    for sg in subgraphs:
#        print sg.vcount()
#        print sg.ecount()
#        print "Nodes :", [v['id'] for v in sg.vs] 
        cl = [int(v['id']) for v in sg.vs] 
        clusters.append(cl)
    #
    return clusters



#######################################################
if __name__ == '__main__':
    PATH = "../_data/"
#    FILE_NAME = PATH + "karate.gml"
    FILE_NAME = PATH + "adjnoun.gml"
    
    
    G = ig.Graph.Read(FILE_NAME, format="gml")

##    G = ig.Graph.Read_Edgelist("../_data/com_amazon_ungraph.gr", directed=False)    # 254 clusters, 6.3s
#    G = ig.Graph.Read_Edgelist("../_data/com_dblp_ungraph.gr", directed=False)    # 278 clusters, 5s
##    G = ig.Graph.Read_Edgelist("../_data/com_youtube_ungraph.gr", directed=False)   # 13517 clusters, 11.5s
##    PATH = "E:/Tailieu/Paper-code/DATA-SET/SNAP/Networks with ground-truth communities/"
##    G = ig.Graph.Read_Edgelist(PATH + "com_lj_ungraph.gr", directed=False) # (3997962, 34681189)   (mem 2.8GB, 286s), 7322 clusters
    
    print "#nodes", G.vcount()
    print "#edges", G.ecount()   
    
    
    # TEST test_fastgreeedy()
    clusters = test_fastgreeedy(G)
#    clusters = test_infomap(G)
#    clusters = test_multilevel(G)               # fast unfolding ...
#    clusters = test_leading_eigenvector(G)
#    clusters = test_label_propagation(G)
    
    print clusters
    
    
    # PLOT
    nxG = nx.read_gml(FILE_NAME)
    pos = nx.spring_layout(nxG)
#    nx.draw(nxG, pos)   # networkx draw()
#    #P.draw()            # pylab draw()
#    plt.show()          # display
    
    color = ["red", "green", "blue", "cyan", "white", "black", "yellow", "magenta"]
    count = 0
    for com in clusters:
        count += 1
        nx.draw_networkx_nodes(nxG, pos, com, node_size = 100, node_color = color[count % 8])
    nx.draw_networkx_edges(nxG, pos, alpha=0.5)
    plt.show()
    
    
    
    
    
    
    