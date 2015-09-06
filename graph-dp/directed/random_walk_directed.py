'''
Created on Jan 13, 2015

@author: huunguye
- implement the paper "Preserving Link Privacy in Social Network Based Systems" (NDSS'13) --> extension for DIRECTED graphs
(RandWalk-mod)
'''

import time
import sys
import random
import math
import networkx as nx
import igraph as ig
import scipy.io
import scipy.sparse as sp
import numpy as np

N_SAMPLES = 5

DIST_TV = 0
MAX_MIXING_TIME = 200
DELTA_DIST = 0.0001


#######################################################
def random_walk_transform_multigraph(G, t, alpha=1.0):
    aG = nx.MultiDiGraph()          # directed
    
    deg_dict = G.out_degree()     # out-degrees
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in G.nodes_iter():
        nb_list.append(G.successors(u))  # successors (out-links)
        nb_len.append(len(G.successors(u)))
        
    # WAY-1
    num_failed = 0
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            # perform (t-1) hop random walk from v
            z = v
            zero_d_out = False
            for i in range(t-1):
                if nb_len[z] == 0:          # zero out-degree node
                    zero_d_out = True
                    break
                z = nb_list[z][random.randint(0,nb_len[z]-1)]
            
            #
            if zero_d_out:
                num_failed += 1
                continue
            
            if count == 1:
                if deg_dict[u] == 1:        # EXCEPTION !
                    aG.add_edge(u, z)       # prob = 1.0 instead of 0.5
                else:
                    val = random.random()   # use 2*alpha
                    if val < 2*alpha:
                        aG.add_edge(u, z)
            else:
                val = random.random()
                if val < (deg_dict[u] - 2*alpha)/(deg_dict[u] - 1):   # use 2*alpha, use 1.0 instead of 0.5
                    aG.add_edge(u, z)
            #
            count += 1
        
    print "num_failed =", num_failed
            
#    # WAY-2
#    potential_edges = []    # list of edges
#    for u in G.nodes_iter():
#        count = 1
#        for v in G.neighbors_iter(u):
#            # perform (t-1) hop random walk from v
#            z = v
#            for i in range(t-1):
#                z = nb_list[z][random.randint(0,nb_len[z]-1)]
#            #
#            if count == 1:
#                if deg_dict[u] == 1:        # EXCEPTION !
#                    val = random.random()
#                    if val < 0.5:
#                        aG.add_edge(u, z)
#                else:
#                    aG.add_edge(u, z)
#            else:
#                potential_edges.append((u,z))
#            #
#            count += 1
#    
#    rand_vals = np.random.random_sample((len(potential_edges),))
#    i = 0
#    for (u,z) in potential_edges:
##        val = random.random()
#        val = rand_vals[i]
#        i +=1
#        if val < (0.5*deg_dict[u] - 1.0)/(deg_dict[u] - 1.0):
#            aG.add_edge(u, z)
    
    
    #
    return aG

#######################################################
# p: percentage of replaced edges
def random_walk_multigraph_replace(G, rwG, t, p):
    
    aG = nx.MultiGraph()
    aG.add_nodes_from(G.nodes_iter())
    
    edge_list = []
    
    for e in G.edges_iter():
        val = random.random()
        if val >= p:
            edge_list.append((e[0], e[1]))
    
    for e in rwG.edges_iter():
        val = random.random()
        if val < p:
            edge_list.append((e[0], e[1]))

    #
    aG.add_edges_from(edge_list)
    
    return aG


#######################################################
if __name__ == '__main__':
    
    ## 100k 
#    file_name = "er_100000_00001"       #100k nodes
#    file_name = "sm_100000_005_11"       #100k nodes
#    file_name = "ba_100000_5"       #100k nodes

    ## real graphs
    file_name = "web_NotreDame_directed"   # 325k nodes, 1.5M edges  
#    file_name = "prod_amazon0302_directed"   # 260k nodes, 1.23M edges


    # Command-line param for automation (bash): <file_name> <t> [<p>] <n_samples> <start_id>
    n_samples = 5
    start_id = 0
    t = 2   
    p = 0.8
    alpha = 0.5
    file_name = sys.argv[1]
    t = int(sys.argv[2])    
    if len(sys.argv) == 5:
        n_samples = int(sys.argv[3])
        start_id = int(sys.argv[4])
    if len(sys.argv) == 6:
        p = float(sys.argv[3])
        n_samples = int(sys.argv[4])
        start_id = int(sys.argv[5])
        
    print "file_name =", file_name
    print "n_samples =", n_samples
    print "start_id =", start_id
    print "t =", t
    print "alpha =", alpha
    if len(sys.argv) == 6:
        print "p =", p
        
    G = nx.read_edgelist("../data/" + file_name + ".gr", '#', '\t', create_using=nx.DiGraph(), nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
#    G = nx.read_edgelist(file_name, '#', ' ', None, nodetype=int)
    
    print "#nodes :", G.number_of_nodes()
    print "#edges :", G.number_of_edges()
    print "#self-loops :", G.number_of_selfloops()
#    print "#components :", len(nx.connected_components(G))    # not allowed in digraph
    n_nodes = G.number_of_nodes()
    deg_list = G.out_degree()     # out-degrees
    min_deg = min(deg_list)
    max_deg = max(deg_list)
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    
    
    # TEST random_walk_transform_multi_graph()
    print "TEST random_walk_transform_multigraph"
#    t = 2
#    M = 10
#    alpha = 0.5
#    print "t =", t
#    print "M =", M
#    print "alpha =", alpha
#    
##    G = nx.Graph()
##    G.add_edges_from([(0,1),(0,3),(1,2),(1,3)])
#    
##    mat_file = "C:/com_dblp_ungraph_randwalk_failed_t_2.mat"
##    aG = random_walk_transform_save_MATLAB(G, t, M, mat_file)
#    aG = random_walk_transform(G, t, M, alpha)
#    print "#nodes :", aG.number_of_nodes()
#    print "#edges :", len(aG.edges())
#    print "#self-loops :", aG.number_of_selfloops()

    if len(sys.argv) == 5:
        # TEST random_walk_transform_multigraph
        start = time.clock()
        for i in range(n_samples):
            print "sample", i
            
            aG = random_walk_transform_multigraph(G, t, alpha)
            print "#nodes :", aG.number_of_nodes()
            print "#edges :", aG.number_of_edges()
            print "#self-loops :", aG.number_of_selfloops()
            
            # save uncertain graph for generate_sample (see: graph_generator.py)
            out_file = "../randwalk/" + file_name + "_randwalk_keep_" + str(t) +"_" + str(alpha) + "_sample." + str(i+start_id)
            nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
        
        print "random_walk_transform_multi_graph for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start    

    if len(sys.argv) == 6:
        # TEST random_walk_multigraph_replace
        start = time.clock()
        rwG = random_walk_transform_multigraph(G, t, alpha)    # run once
        
        for i in range(n_samples):
            print "sample", i
            
            aG = random_walk_multigraph_replace(G, rwG, t, p)
            print "#nodes :", aG.number_of_nodes()
            print "#edges :", aG.number_of_edges()
            print "#self-loops :", aG.number_of_selfloops()
            
            # save uncertain graph for generate_sample (see: graph_generator.py)
            out_file = "../randwalk/" + file_name + "_randwalk_keep_replace" + str(t) +"_" + str(alpha) + "_" + str(p) + "_sample." + str(i+start_id)
            nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
        
        print "random_walk_multigraph_replace for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start    
        

    
    
    
    
    
    
    
    
    
    