
'''
Created on Jan 28, 2014

@author: huunguye

10,000 nodes: 
    - snap.GetAnfEffDiam : 0.095 s
    - nx.diameter :        195.6 s
    
'''

import time
import sys
import networkx as nx
import igraph as ig
import matplotlib.pyplot as plt
from heapq import *
import scipy.io
import numpy as np
import math
import powerlaw
from incorrectness_measure_igraph import read_uncertain_graph
#from random_walk_igraph import mixing_time
from random_walk import mixing_time  
from incorrectness_measure_multigraph import compare_edges_diff_one

#######################################################
def test_graph_metrics():
    start = time.clock()
#    G = nx.Graph()
    
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-AstroPh.txt"    # 18,772 nodes, 396,160 edges
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-CondMat.txt"    # 23,133 nodes, 186,936 edges
    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-GrQc.txt"        # 5,242 nodes, 28,980 edges
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-HepPh.txt"      # 12,008 nodes, 237,010 edges  
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-HepTh.txt"      # 9,877 nodes, 51,971 edges  
    
#    NODE_ID = 81626    # for graph 'CA-CondMat.txt'
#    G = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
    
    # SYNTHETIC (simple, undirected) graph
    NODE_ID = 2        # for synthetic graph     
    G = nx.Graph()
#    edge_list = [(1,2),(1,3),(3,4),(2,5),(2,8),(8,9),(6,7)]            # 2 components
#    edge_list = [(1,2),(1,3),(3,4),(2,5),(2,8),(8,9),(6,7),(4,6)]       # 1 component
    edge_list = [(1,2),(1,3),(3,4),(2,5),(2,8),(8,9),(6,7),(2,9)]            # 2 components
    G.add_edges_from(edge_list)
    
    
    print "#nodes :", G.number_of_nodes()
    print "#edges :", len(G.edges())
    print "#edges :", G.number_of_edges()
    print "#self-loops :", G.number_of_selfloops()
    
    print "Elapsed ", (time.clock() - start)
    
    # STATISTICS
    component_list = nx.connected_components(G)
    print "#components", len(component_list)
#    print "max_component :", len(component_list[0])
#    print "min_component :", len(component_list[-1])
    
    node_list = G.nodes()
    min_node_id = min(node_list)
    max_node_id = max(node_list)
    print node_list[0]
    print "min_node_id, max_node_id = ", min_node_id, max_node_id
    print "neighbors of node 1:", G.neighbors(1)
    


    
    #### 1 - CENTRALITY
#    #
#    print "Degree Centrality"
#    print len(G.neighbors(NODE_ID))/float(G.number_of_nodes() - 1)        # = degree_centrality[NODE_ID]
#    
#    #
#    degree_centrality = nx.degree_centrality(G)
#    print degree_centrality[NODE_ID]
#    
#    #
#    print "Closeness Centrality"
##    closeness_centrality_u = nx.closeness_centrality(G, NODE_ID)        # normalized = True (default)
#    closeness_centrality_u = nx.closeness_centrality(G, NODE_ID, None, normalized=False)
#    print closeness_centrality_u
#    
#    u_com = nx.node_connected_component(G, NODE_ID)
#    print len(u_com)
#    
#    #
#    print "Betweenness Centrality"
#    betweenness_centrality = nx.betweenness_centrality(G, None)
##    print betweenness_centrality[NODE_ID]
#    print betweenness_centrality
#    print sum(betweenness_centrality.itervalues())
#    
#    #
#    print "Eigenvector Centrality"
#    eigenvector_centrality = nx.eigenvector_centrality(G, 100, tol=0.01)       # by power iteration
#    print eigenvector_centrality
    
    
    #### 2 - CLUSTERING 
#    print "Triangles"
#    print "#triangles of NODE_ID :", nx.triangles(G, NODE_ID)
#    
#    print "Transitivity"
#    print nx.transitivity(G)
#    
#    print "Clustering (local)"
#    print nx.clustering(G, NODE_ID)
    
    
    #### 3 - ASSORTATIVITY 
    print "Degree Assortativity"
    degree_assortativity = nx.degree_assortativity_coefficient(G)
    print degree_assortativity
    
    
    #### 4 - GRAPH GENERATOR
#    pos = nx.spring_layout(G)
#    nx.draw(G, pos)             # networkx draw()
#    plt.show()
    
    
    #### 5 - SUB-GRAPH (Graph.copy(), Graph.to_undirected(), Graph.to_directed(), Graph.subgraph()
#    G1 = G.subgraph([1,2,3,5])
#    print G1.nodes()
#    print G1.edges()


#######################################################
def test_heap():
    h = []
    heappush(h, (5, 'write code'))
    heappush(h, (7, 'release product'))
    heappush(h, (1, 'write spec'))
    heappush(h, (3, 'create tests'))
#    print heappop(h)
    print h


#######################################################
def test_diameter():
    
#    filename = "../data/er_10000_0001.gr"               # diam = 7, 202s
#    filename = "../data/sm_10000_005_11.gr"            # diam = 12, 156s
#    filename = "../data/ba_10000_5.gr"                  # diam = 6, 177s
    filename = "../data/pl_10000_5_01.gr"               # diam = 6, 179s
#    filename = "../data/ff_10000_05_connected.gr"       # diam = 25, 218s
    
    start = time.clock()
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    diam = nx.diameter(G)
    print "diamter =", diam
    print "elapsed :", time.clock() - start

#######################################################
def test_igraph():
    G = ig.Graph()
    G.add_vertices(5)
#    G.add_edges([(0,1), (1,2), (1,3), (3,4)])
    G.add_edges([(1,3), (3,4), (0,1), (1,2)])
    print "node list"
    for v in G.vs:
        print v.index, G.degree(v)  # node id, node degree
    print "edge list"
    for e in G.es:
        print e.index, e.source, e.target
    
    # attributes
#    for e in G.es:
#        e['p'] = 1.0
    for e in G.es:
        e_id = G.get_eid(e.target, e.source)    # intentionally to test get_eid()
        G.es[e_id]['p'] = 0.5
        
    print "edge list with attributes"    
    for e in G.es:
        print e.index, e.source, e.target, e['p']
        
    ##
    print "test Read_Edgelist()"
    G = ig.Graph.Read_Edgelist("../data/a_toy.gr", directed=False)
    G.add_vertices(1)
    print "nodes"
    for v in G.vs:
        print v.index 
    print "edges"
    for e in G.es:
        print e.source, e.target

#######################################################
#
#######################################################
if __name__ == '__main__':
    
#    test_heap()
#    test_diameter()

    # TEST powerlaw
##    deg_list = [1728, 216, 64, 27]
#    deg_list = [1 for _ in range(1728)] + [2 for _ in range(216)] + [3 for _ in range(64)] + [4 for _ in range(27)]
#    
#    print "len(deg_list) =", len(deg_list)
#    
##    alpha = 3
##    deg_list = np.array([1/math.pow(i, alpha) for i in range(1,11)])
#    
##    print "deg_list =", deg_list
#    data = np.array(deg_list)           #data can be list or Numpy array
#    results = powerlaw.Fit(data)                                # continuous
##    results = powerlaw.Fit(data, xmin=0.001, discrete=True)    # discrete  
#
##    theoretical_distribution = powerlaw.Power_Law(xmin=5.0, parameters=[3.0])
##    simulated_data = theoretical_distribution.generate_random(10000)
###    scipy.io.savemat("C:/simulated_data.mat", dict(simulated_data=np.array(list(simulated_data))))
##    
##    results = powerlaw.Fit(simulated_data)
#
#    print "alpha =", results.power_law.alpha
#    print "sigma =", results.power_law.sigma
#    print "xmin =", results.power_law.xmin
    
#    R, p = results.distribution_compare('power_law', 'lognormal')
#    print "R =", R
#    print "p =", p
    
    # draw
#    ax = powerlaw.plot_pdf(deg_list, color='b')
#    ax.show()
    
#    plt.savefig("../fig/test.png") # save as png

#    plt.plot([1,2,3,4])
#    plt.ylabel('some numbers')
#    plt.show()

    # TEST  test_igraph()
#    test_igraph()
    
    # TEST read_uncertain_graph
#    aG = read_uncertain_graph("../out/com_dblp_ungraph_cvxopt_200000_20_rand_missing.out", 317080)
#    aG = read_uncertain_graph("../out/com_dblp_ungraph_cvxopt_200000_20_rw_missing.out", 317080)
#    aG = read_uncertain_graph("../out/_com_dblp_ungraph_cvxopt_200000_20_rw_missing.out", 317080)

    # TEST igraph
##    aG = ig.Graph.Read_Edgelist("../sample/com_dblp_ungraph_cvxopt_400000_20_nb_missing_sample.0", directed=False)
##    start = time.clock()
##    assortativity = aG.assortativity_degree(directed=False)
##    print "assortativity =", assortativity, " elapsed", time.clock() - start
#    
##    n_nodes = 317080
#    n_nodes = 1134890
#    aG = nx.read_edgelist("../sample/com_youtube_ungraph_entropy_01_2_001_sample.0", '#', '\t', None, nodetype=int) 
#    aG.add_nodes_from(range(n_nodes))
#    print "read aG : DONE"
#    start = time.clock()
#    m_time = mixing_time(aG, 0.01)
#    print "m_time =", m_time, " elapsed", time.clock() - start

    # TEST nx.MultiGraph
    G = nx.MultiGraph()
    G.add_edges_from([(0,1),(0,1),(1,1),(2,3),(1,3),(2,3)])
    print "#nodes :", G.number_of_nodes()
    print "#edges :", len(G.edges())
    print "#self-loops :", G.number_of_selfloops()
    for u in G.nodes_iter():
        print u, G.degree(u)
    for u in G.nodes_iter():
        print "neighbors of", u, list(G.neighbors_iter(u))
    for e in G.edges_iter():
        print e[0], e[1]
        
    print "compare_edges_diff_one"
    bG = nx.Graph()
    bG.add_edges_from([(0,1),(0,2),(0,3),(1,2),(1,3),(2,3)])
    (diff_b, diff_a) = compare_edges_diff_one(bG, G)
    print diff_b
    print diff_a
    

    
    