'''
Created on Jun 3, 2014

@author: huunguye

Jan 15, 2015
    - added generate_sample_directed(), generate_samples_and_save_directed()
'''

import sys
import time
import random
import math
import igraph as ig
import numpy as np
from incorrectness_measure_igraph import read_uncertain_graph 

#######################################################
# G: uncertain
def generate_sample(G):
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    
    n_edges = G.ecount()
    rand_arr = np.random.random_sample((n_edges,))  # list of random values for selecting edges from G
    
    i = 0 
    edge_list = []
    for e in G.es:
        if rand_arr[i] < e['p']:
            edge_list.append((e.source, e.target))
        i += 1
    aG.add_edges(edge_list)
    #
    return aG

#######################################################
def generate_samples_and_save(G, n_samples, data_name, start_id=0):
    for i in range(n_samples):
        print "sample", i+start_id
        aG = generate_sample(G)
        file_name = "../sample/" + data_name + "_sample." + str(i+start_id)
        aG.write_edgelist(file_name)
        
#######################################################
# G: uncertain
def generate_sample_directed(G):
    aG = ig.Graph(directed=True)    # DIRECTED
    aG.add_vertices(G.vcount())
    
    n_edges = G.ecount()
    rand_arr = np.random.random_sample((n_edges,))  # list of random values for selecting edges from G
    
    i = 0 
    edge_list = []
    for e in G.es:
        if rand_arr[i] < e['p']:
            edge_list.append((e.source, e.target))
        i += 1
    aG.add_edges(edge_list)
    #
    return aG

#######################################################
def generate_samples_and_save_directed(G, n_samples, data_name, start_id=0):
    for i in range(n_samples):
        print "sample", i+start_id
        aG = generate_sample_directed(G)
        file_name = "../sample/" + data_name + "_sample." + str(i+start_id)
        aG.write_edgelist(file_name)        

        
#######################################################
if __name__ == '__main__':
        
    # TEST generate_samples_and_save()
    start = time.clock()
    ## entropy-based
#    data_name = "er_100000_00001_entropy_001_2_001"
#    data_name = "er_100000_00001_entropy_01_2_001"
#    data_name = "sm_100000_005_11_entropy_001_2_001"
#    data_name = "sm_100000_005_11_entropy_01_2_001"
#    data_name = "ba_100000_5_entropy_001_2_001"
#    data_name = "ba_100000_5_entropy_01_2_001"
    
    # real graphs
#    data_name = "com_dblp_ungraph_entropy_0001_2_001"
#    data_name = "com_dblp_ungraph_entropy_001_2_001"
#    data_name = "com_dblp_ungraph_entropy_01_2_001"

#    data_name = "com_amazon_ungraph_entropy_0001_2_001"
#    data_name = "com_amazon_ungraph_entropy_001_2_001"
#    data_name = "com_amazon_ungraph_entropy_01_2_001"

#    data_name = "com_youtube_ungraph_entropy_0001_2_001"    
#    data_name = "com_youtube_ungraph_entropy_001_2_001"
#    data_name = "com_youtube_ungraph_entropy_01_2_001"

    ## convex opt
#    data_name = "er_100000_00001_cvxopt_100000_nb_missing"
#    data_name = "er_100000_00001_cvxopt_100000_missing"
#    data_name = "er_100000_00001_cvxopt_100000_uq_missing"
#    data_name = "er_100000_00001_cvxopt_50000_missing"
#    data_name = "er_100000_00001_cvxopt_50000_uq_missing"

#    data_name = "sm_100000_005_11_cvxopt_25000_missing"
#    data_name = "sm_100000_005_11_cvxopt_50000_missing"
#    data_name = "sm_100000_005_11_cvxopt_50000_10_missing"        # random, MOSEK
#    data_name = "sm_100000_005_11_cvxopt_50000_10_nb_missing"        # nearby, MOSEK
#    data_name = "sm_100000_005_11_cvxopt_50000_nb_missing"
#    data_name = "sm_100000_005_11_cvxopt_100000_missing"        # random
#    data_name = "sm_100000_005_11_cvxopt_100000_10_missing"        # random, MOSEK
#    data_name = "sm_100000_005_11_cvxopt_100000_10_nb_missing"        # nearby, MOSEK
#    data_name = "sm_100000_005_11_cvxopt_100000_10_uq_missing"        # unique, MOSEK
#    data_name = "sm_100000_005_11_cvxopt_100000_nb_missing"     # nearby
#    data_name = "sm_100000_005_11_cvxopt_100000_uq_missing"    # unique

#    data_name = "com_dblp_ungraph_cvxopt_50000_10_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_100000_10_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_100000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_200000_5_nb_missing"      # nearby, MOSEK    
#    data_name = "com_dblp_ungraph_cvxopt_200000_10_nb_missing"      # nearby, MOSEK

#    data_name = "com_dblp_ungraph_cvxopt_200000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_400000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_600000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_800000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_1000000_20_nb_missing"      # nearby, MOSEK

#    data_name = "com_dblp_ungraph_cvxopt_200000_20_sub_075_nb_missing"      # nearby, sub=0.75, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_200000_20_sub_05_nb_missing"      # nearby, sub=0.5, MOSEK

    data_name = "com_dblp_ungraph_cvxopt_200000_20_rw_missing"      # randwalk, MOSEK

#    data_name = "com_dblp_ungraph_cvxopt_200000_20_rand_missing"      # random, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_400000_20_rand_missing"      # random, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_600000_20_rand_missing"      # random, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_800000_20_rand_missing"      # random, MOSEK
#    data_name = "com_dblp_ungraph_cvxopt_1000000_20_rand_missing"      # random, MOSEK

#    data_name = "com_amazon_ungraph_cvxopt_50000_5_nb_missing"      # nearby, MOSEK 
#    data_name = "com_amazon_ungraph_cvxopt_50000_10_nb_missing"      # nearby, MOSEK   
#    data_name = "com_amazon_ungraph_cvxopt_100000_10_nb_missing"      # nearby, MOSEK    
#    data_name = "com_amazon_ungraph_cvxopt_100000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_200000_10_nb_missing"      # nearby, MOSEK

#    data_name = "com_amazon_ungraph_cvxopt_200000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_400000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_600000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_800000_20_nb_missing"      # nearby, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_1000000_20_nb_missing"      # nearby, MOSEK

#    data_name = "com_amazon_ungraph_cvxopt_200000_20_rand_missing"      # random, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_400000_20_rand_missing"      # random, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_600000_20_rand_missing"      # random, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_800000_20_rand_missing"      # random, MOSEK
#    data_name = "com_amazon_ungraph_cvxopt_1000000_20_rand_missing"      # random, MOSEK

#    data_name = "com_youtube_ungraph_cvxopt_300000_30_nb_missing"      # nearby, MOSEK    
#    data_name = "com_youtube_ungraph_cvxopt_300000_60_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_420000_30_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_420000_60_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_600000_600_missing"
#    data_name = "com_youtube_ungraph_cvxopt_600000_30_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_900000_600_missing"      # 260s/5

#    data_name = "com_youtube_ungraph_cvxopt_600000_60_nb_missing"      # nearby, MOSEK    
#    data_name = "com_youtube_ungraph_cvxopt_1200000_60_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_1800000_60_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_2400000_60_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_3000000_60_nb_missing"      # nearby, MOSEK

#    data_name = "com_youtube_ungraph_cvxopt_600000_60_rand_missing"      # random, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_1200000_60_rand_missing"      # random, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_1800000_60_rand_missing"      # random, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_2400000_60_rand_missing"      # random, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_3000000_60_rand_missing"      # random, MOSEK
    
    ## linear opt
#    data_name = "er_100000_00001_linopt_50000_avg_inv_min"
#    data_name = "er_100000_00001_linopt_100000_avg_inv_min"
#    data_name = "er_100000_00001_linopt_200000_avg_inv_min"
    
#    data_name = "com_dblp_ungraph_linopt_200000_avg_inv_min"    # random
#    data_name = "com_dblp_ungraph_linopt_200000_nb_avg_inv_min"    # nearby
#    data_name = "com_dblp_ungraph_linopt_400000_avg_inv_min"          # 77s/5
#    data_name = "com_dblp_ungraph_linopt_400000_nb_avg_inv_min"     # nearby

#    data_name = "com_youtube_ungraph_linopt_500000_nb_avg_inv_min"    # nearby
#    data_name = "com_youtube_ungraph_linopt_500000_avg_inv_min"    # random (solved with MOSEK)        

    ######
    # Command-line param for automation (bash): <data_name> <n_nodes> <n_samples> <start_id> [<directed>]
    n_samples = 5
    start_id = 0
    n_nodes = 0
    directed = 0
    if len(sys.argv) > 4:
        data_name = sys.argv[1]
        n_nodes = int(sys.argv[2])
        n_samples = int(sys.argv[3])
        start_id = int(sys.argv[4])
    if len(sys.argv) > 5:
        directed = int(sys.argv[5])

    print "data_name =", data_name
    print "n_nodes =", n_nodes
    print "n_samples =", n_samples
    print "start_id =", start_id
    print "directed =", directed
    
#    n_nodes = 100000
#    n_nodes = 317080    # dblp
#    n_nodes = 334863    # amazon
#    n_nodes = 1134890   # youtube
 
    G = read_uncertain_graph("../out/" + data_name + ".out", n_nodes)
    print "read uncertain graph G - DONE, elapsed", time.clock() - start
    
    start = time.clock()
    if directed == 0:
        generate_samples_and_save(G, n_samples, data_name, start_id)      # NOTE: change start_id !!!
        print "generate_samples_and_save - DONE, elapsed", time.clock() - start
    else:
        generate_samples_and_save_directed(G, n_samples, data_name, start_id)      # NOTE: change start_id !!!
        print "generate_samples_and_save_directed - DONE, elapsed", time.clock() - start
    #
    print "DONE"
    