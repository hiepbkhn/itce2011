'''
Created on Jan 13, 2015

@author: huunguye
- implement the paper "Preserving Link Privacy in Social Network Based Systems" (NDSS'13) --> extension for DIRECTED graphs
(RandWalk)
'''

import sys
import time
import random
import math
import igraph as ig
import scipy.io
import scipy.sparse as sp
import numpy as np

N_SAMPLES = 5

DIST_TV = 0
MAX_MIXING_TIME = 200
DELTA_DIST = 0.0001

#######################################################
def random_walk_transform(G, t, M, alpha=1.0):
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    edge_dict = {}  
    edge_list = []
    
    deg_dict = G.degree(mode="OUT")     # out-degrees
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in range(G.vcount()):
        nb_list.append(G.neighbors(u, mode="OUT"))   # successors (out-links)
        nb_len.append(len(G.neighbors(u, mode="OUT")))
    #
    num_failed = 0
    for u in range(G.vcount()):
        count = 1
        for v in G.neighbors(u, mode="OUT"):     # successors (out-links)
            loop = 1
            z = u
            zero_d_out = False
            while (u == z or edge_dict.has_key((u,z)) or zero_d_out) and (loop <= M):       # added zero_d_out
                # perform (t-1) hop random walk from v
                z = v
                zero_d_out = False
                for i in range(t-1):
                    if nb_len[z] == 0:          # zero out-degree node
                        zero_d_out = True
                        break
                    z = nb_list[z][random.randint(0,nb_len[z]-1)]
                loop += 1
                
            #
            if loop <= M:
                if count == 1:
                    if deg_dict[u] == 1:        # EXCEPTION !
                        edge_dict[(u,z)] = 1    # prob = 1.0 instead of 0.5
                        edge_list.append((u,z))
                    else:
                        val = random.random()   # use 2*alpha
                        if val < 2*alpha:       # 
                            edge_dict[(u,z)] = 1
                            edge_list.append((u,z))
                else:
                    val = random.random()
                    if val < (deg_dict[u] - 2*alpha)/(deg_dict[u] - 1):   # use 2*alpha, use 1.0 instead of 0.5
                        edge_dict[(u,z)] = 1
                        edge_list.append((u,z))
            else:
                num_failed += 1
            #
            count += 1
    print "num_failed =", num_failed
    #
    aG.add_edges(edge_list)
    
    return aG

#######################################################
# p: percentage of replaced edges
def random_walk_replace(G, rwG, t, M, p):
    
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    
    edge_list = []
    
    for e in G.es:
        val = random.random()
        if val >= p:
            edge_list.append((e.source, e.target))
    
    for e in rwG.es:
        val = random.random()
        if val < p:
            edge_list.append((e.source, e.target))

    #
    aG.add_edges(edge_list)
    
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
    

    #
#    file_name = "../sample/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing_sample.0"
#    file_name ="../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.2_sample.0"
    

    # 
#    G = nx.Graph()
#    G.add_edges_from([(0,1),(0,2),(0,3),(1,2)])


    ######
    # Command-line param for automation (bash): <file_name> <t> <M> [<p>] <n_samples> <start_id> <alpha>
    n_samples = 5
    start_id = 0
    t = 2
    M = 10
    p = 0.8
    alpha = 1.0
    file_name = sys.argv[1]
    t = int(sys.argv[2])
    M = int(sys.argv[3])
    if len(sys.argv) == 7:
        n_samples = int(sys.argv[4])
        start_id = int(sys.argv[5])
        alpha = float(sys.argv[6])
    if len(sys.argv) == 8:
        p = float(sys.argv[4])
        n_samples = int(sys.argv[5])
        start_id = int(sys.argv[6])
        alpha = float(sys.argv[7])
        

    print "file_name =", file_name
    print "n_samples =", n_samples
    print "start_id =", start_id
    print "t =", t
    print "M =", M
    print "alpha =", alpha
    if len(sys.argv) == 7:
        print "p =", p
    
    #
    print "file_name =", file_name
    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=True)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
    print "#nodes :", G.vcount()
    print "#edges :", G.ecount()
    print "#components :", len(G.clusters("weak"))
    n_nodes = G.vcount()
    deg_list = G.degree(G.vs)         # dict[node] = deg
    min_deg = min(deg_list)
    max_deg = max(deg_list)
    print "min-deg =", min_deg
    print "max-deg =", max_deg

    # Command-line param for automation (bash): <file_name> <t> <M> <n_samples> <start_id>
    if len(sys.argv) == 7:
        # TEST random_walk_transform()
        start = time.clock()
        for i in range(n_samples):
            print "sample", i+start_id
            
            aG = random_walk_transform(G, t, M, alpha)
            
            # save uncertain graph for generate_sample (see: graph_generator.py)
            out_file = "../randwalk/" + file_name + "_randwalk_" + str(t) +"_" + str(M) + "_" + str(alpha) + "_sample." + str(i+start_id)
            aG.write_edgelist(out_file)
        
        print "random_walk_transform for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start
    
    # Command-line param for automation (bash): <file_name> <t> <M> <p> <n_samples> <start_id>
    if len(sys.argv) == 8:
        # TEST random_walk_replace()
        start = time.clock()
        rwG = random_walk_transform(G, t, M, alpha)    # run once
        
        for i in range(n_samples):
            print "sample", i+start_id
            
            aG = random_walk_replace(G, rwG, t, M, p)
            
            # save uncertain graph for generate_sample (see: graph_generator.py)
            out_file = "../randwalk/" + file_name + "_randwalk_replace_" + str(t) +"_" + str(M) + "_" + str(p) + "_" + str(alpha) + "_sample." + str(i+start_id)
            aG.write_edgelist(out_file)
        
        print "random_walk_replace for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start


    
    