'''
Created on Feb 23, 2014

@author: Nguyen Huu Hiep
References: On Randomness Measures for Social Networks (SDM'09)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import * 
import matplotlib.pyplot as plt


   
#######################################################
def read_gml_graph(filename):
    G = nx.read_gml(filename, 'UTF-8', False)       # NOTE: do not need to add edges (j,i) for each (i,j)

#    f = open(filename)
#    str = f.read()
#    f.close()
#    G = nx.parse_gml(str, False)
    
    
    return G 

#######################################################
# normalize to zero-based indexing of node ids
def normalize_graph(G):
    newG = nx.Graph()
    
    min_id = min(G.nodes_iter())
    max_id = max(G.nodes_iter())
    print "min_id =", min_id
    print "max_id =", max_id
    
    for (u,v) in G.edges_iter():
        if u != v:
            newG.add_edge(u-min_id, v-min_id)
        else:
            print "self-loop at ", u
    #
    return newG

#######################################################
def export_graph_to_matlab(G, filename):
    edge_list = []
    for (i,j) in G.edges_iter():
        edge_list.append((i,j))
    # node labels
    label_dict = {'c':1, 'n':2, 'l':3}
    label_list = []
    #
    for u in G.nodes(True):
        label_list.append(label_dict[u[1]['value']])

    #
    scipy.io.savemat(filename, dict(edge_list=array(edge_list), label_list=array(label_list)) )
    

#######################################################
def draw_graph(G):
    pos = nx.spring_layout(G)
    
    nx.draw(G, pos)  # networkx draw()
    #P.draw()    # pylab draw()
    
    plt.show() # display

#######################################################
def compute_spectral_coordinate(G, k):
    A = nx.adjacency_matrix(G)
    N = A.shape[0]
    
    w, v = eigh(A, None, True, False, False, False, True, (N-k, N-1))
    #
    return w, v 

#######################################################
# input: spectral coordinate
def edge_NR(u, v, coords, norm=False):
    if norm:
        return np.dot(coords[u,...], coords[v,...])/np.linalg.norm(coords[u,...])/np.linalg.norm(coords[v,...])
    else:
        return np.dot(coords[u,...], coords[v,...])

#######################################################
# input: spectral coordinate
def node_NR(u, neighbors, coords, norm=False):
    s = 0.0
    for v in neighbors:
        s += edge_NR(u, v, coords, norm)
    
    return s

#######################################################
# input: spectral coordinate
def graph_NR(G, coords, norm=False):
    s = 0.0
    for (u,v) in G.edges_iter():
            s += edge_NR(u, v, coords, norm)
    
    return s

#######################################################
# input: eig_vals
def graph_NR_2(eig_vals):
       
#    return sum(eig_vals)/2.0
    return sum(eig_vals)

#######################################################
def graph_NR_relative(G, eig_vals, k):
    N = G.number_of_nodes()
    m = G.number_of_edges()
    
    
    p = 2.0*k*m/(N*(N-k))
    
    print "N =", N, "m =", m, "p =", p
    
    meanNR = (N-2*k)*p + k
    sigmaNR = 2.0*k*p*(1-p)
    
    return (graph_NR_2(eig_vals) - meanNR)/math.sqrt(sigmaNR)


#######################################################
def test_ER_random_graph(N = 200, p = 0.2):
    
    G = nx.fast_gnp_random_graph(N, p)
    
    k = 1
    eig_vals, eig_vecs = compute_spectral_coordinate(G, k)
    
    rel_NR = graph_NR_relative(G, eig_vals, k)
    
    print "ER random graph, rel_NR =", rel_NR
    




#######################################################
#######################################################
if __name__ == '__main__':
#    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polbooks/polbooks.gml"
    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polblogs/polblogs.gml"     # 2 components
#    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/karate/karate.gml"
#    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/dolphins/dolphins.gml"
    
#    G = read_gml_graph(filename)
#
#    print "#nodes =", G.number_of_nodes()
#    print "#edges =", G.number_of_edges()
#    
##    print G.nodes(True)
#    print "read_gml_graph: DONE"
#    
##    export_graph_to_matlab(G, 'C:/polbooks.mat')
#
#    # normalize G 
#    G = normalize_graph(G)
#    print "normalize_graph: DONE"
#    print "#nodes =", G.number_of_nodes()
#    print "#edges =", G.number_of_edges()
#    print "#components =", nx.number_connected_components(G)
#    components = nx.connected_components(G)
#    print components[1]     # 2 nodes in 2nd-component {181, 665}     
    
    
#    draw_graph(G)
    
    # TEST edge_NR(), node_NR(), graph_NR()
#    k = 2
#    eig_vals, eig_vecs = compute_spectral_coordinate(G, k)
#    
#    print eig_vals
#    
#    print "edge_NR(1,0) =", edge_NR(1, 0, eig_vecs) 
#    print "node_NR(1) =", node_NR(1, G.neighbors(1), eig_vecs)
##    print "graph_NR() =", graph_NR(G, eig_vecs)
#    print "graph_NR() =", graph_NR_2(eig_vals)
#    print "graph_NR_relative() =", graph_NR_relative(G, eig_vals, k)
    
#    print "NORMALIZED"
#    print "edge_NR(1,0) =", edge_NR(1, 0, eig_vecs, True) 
#    print "node_NR(1) =", node_NR(1, G.neighbors(1), eig_vecs, True)
#    print "graph_NR() =", graph_NR(G, eig_vecs, True)
    
    
    # test test_ER_random_graph()
    test_ER_random_graph(1000, 0.2)
    
    