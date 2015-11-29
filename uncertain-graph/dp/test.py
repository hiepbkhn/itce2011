'''
Created on Feb 14, 2015

@author: Nguyen Huu Hiep
'''
import numpy as np
#import bintrees as bt
import time
from subprocess import call, check_output
from random import *
import math
import sys
import networkx as nx
import igraph as ig
import snap as sn
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import * 
from utility_measure import convert_networkx_to_SNAP


#######################################################
#def test_bintrees():
#    T = bt.BinaryTree() # unbalanced binary tree
    


#######################################################
def test_graph_by_deg_sequence():
        dataname = "polbooks"               # (105,441)        
    #    dataname = "polblogs"               # (1224,16715) 
        dataname = "as20graph"              # (6474,12572)        config_model: 11500 edges (parallel edges, selfloops removed)
    #    dataname = "wiki-Vote"              # (7115,100762)   
    #    dataname = "ca-HepPh"               # (12006,118489)     
        dataname = "ca-AstroPh"             # (18771,198050)      config_model: 197009 edges  
        dataname = "com_amazon_ungraph"     # (334863,925872)      config_model: 925842 edges (9s)
    #    dataname = "com_dblp_ungraph"       # (317080,1049866)     
        dataname = "com_youtube_ungraph"    # (1134890,2987624)    config_model: 2962400 edges (32s) (7GB mem), havel_hakimi: 2987624 (21s)
        
        print "dataname =", dataname

        filename = "../_data/" + dataname + ".gr"       
        
        print "filename =", filename
        
        G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
        print "#nodes =", G.number_of_nodes()
        print "#edges =", G.number_of_edges()
        
        deg_seq = list(G.degree().itervalues())
        start = time.clock()
#        G2 = nx.configuration_model(deg_seq, None, None)
        G2 = nx.havel_hakimi_graph(deg_seq, None)
        print "configuration_model - DONE, elapsed", time.clock() - start
        
        # remove parallel edges and selfloops
        G2 = nx.Graph(G2)
        G2.remove_edges_from(G2.selfloop_edges())
        print "#nodes =", G2.number_of_nodes()
        print "#edges =", G2.number_of_edges()


#######################################################
def test_clustering_coefficient():
    
    dataname = "example"            # (13, 20)  
#    dataname = "polbooks"           # (105, 441)     
#    dataname = "polblogs"           # (1224,16715)     # s_CC = 0.225958517359
#    dataname = "polblogs-wcc"       # (1222,16714) 
#    dataname = "as20graph"          # (6474,12572)     

    filename = "../_data/" + dataname + ".gr"
    
    ### read graph for DETERMINISTIC G
    start = time.clock()
    print "filename =", filename
    G = ig.Graph.Read_Edgelist(filename, directed=False)
    print "#nodes =", G.vcount()

    # igraph
    s_CC = G.transitivity_undirected()
    print "s_CC =", s_CC
    
    s_agv_local_CC = G.transitivity_avglocal_undirected()
    print "s_agv_local_CC =", s_agv_local_CC
    
    # SNAP
    aG = convert_networkx_to_SNAP(G)
    print "convert to SNAP graph: DONE"
    
    s_CC = sn.GetClustCf(aG)
    print "s_CC =", s_CC
    

#######################################################
if __name__ == '__main__':
    # normalize vector
#    vec = np.array([1, 2, 3, 4])
#    print vec
#    vecL1 = vec / float(np.linalg.norm(vec, 1))
#    print "L1-normalized :", vecL1 
#    vecL2 = vec / np.linalg.norm(vec, 2)
#    print "L2-normalized :", vecL2
#    
#    a = np.array([[1, 2], [3, 4]])
#    print np.dot(a.transpose(),a)

    # TEST test_graph_by_deg_sequence()
#    test_graph_by_deg_sequence()

    # TEST test_clustering_coefficient()
    test_clustering_coefficient()
    
    
    
    
    
