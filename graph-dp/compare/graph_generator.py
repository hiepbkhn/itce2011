'''
Created on Feb 27, 2014

@author: huunguye
'''

import time
import random
import math
import networkx as nx
import numpy as np
from socialModels import forestFire_mod
from randomness_measure import normalize_graph
import snap as sn

trust_list_1 = [0.1, 0.3, 0.5, 0.8, 1.0]
trust_list_2 = [0.5, 0.8, 1.0]
trust_list_3 = [0.8, 0.9, 1.0]

#######################################################
# ER: Erdos-Renyi
def generate_ER_graph(N, p, filename):
    G = nx.fast_gnp_random_graph(N, p)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')

  
#######################################################
# SM: small-world
def generate_SM_graph(N, p, k, filename):
    G = nx.connected_watts_strogatz_graph(N, k, p)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')    

#######################################################
# BA: Barabasi-Albert
def generate_BA_graph(N, m, filename):
    G = nx.barabasi_albert_graph(N, m)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')    

#######################################################
# PL: power law
def generate_PL_graph(N, m, p, filename):
    G = nx.powerlaw_cluster_graph(N, m, p)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')    

#######################################################
# FF: Forest Fire
def generate_FF_graph(N, p, filename):
    G = forestFire_mod(N, p)
    nx.write_edgelist(G, filename, '#', '\t', False, 'utf-8')

#######################################################
# ER: Erdos-Renyi
def generate_ER_graph_with_trust(N, p, trust_list, filename):
    N_VALS = len(trust_list)
    G = nx.fast_gnp_random_graph(N, p)
    for (u,v) in G.edges_iter():
        val = random.randint(0,N_VALS-1)
        G.edge[u][v]['t'] = trust_list[val]
        
    nx.write_edgelist(G, filename, '#', '\t', True, 'utf-8')

#######################################################
# SM: small-world
def generate_SM_graph_with_trust(N, p, k, trust_list, filename):
    N_VALS = len(trust_list)
    G = nx.connected_watts_strogatz_graph(N, k, p)
    for (u,v) in G.edges_iter():
        val = random.randint(0,N_VALS-1)
        G.edge[u][v]['t'] = trust_list[val]
        
    nx.write_edgelist(G, filename, '#', '\t', True, 'utf-8')

#######################################################
def generate_trust_network(N, trust_list, filename):
    N_VALS = len(trust_list)
    G = nx.Graph()
    for i in range(N-1):
        for j in range(N):
            val = random.randint(0,N_VALS-1)
            G.add_edge(i, j, {'t':trust_list[val]})
            
    nx.write_edgelist(G, filename, '#', '\t', True, 'utf-8')

#######################################################
# G: uncertain
def generate_sample(G):
    aG = nx.Graph()
    aG.add_nodes_from(G.nodes_iter())
    
    n_edges = G.number_of_edges()
    rand_arr = np.random.random_sample((n_edges,))  # list of random values for selecting edges from G
    
    i = 0 
    for e in G.edges_iter(data=True):
        if rand_arr[i] < e[2]['p']:
            aG.add_edge(e[0], e[1])
        i += 1
    
    #
    return aG

#######################################################
def generate_samples_and_save(G, n_samples, data_name, start_id=0):
    for i in range(n_samples):
        print "sample", i+start_id
        aG = generate_sample(G)
        file_name = "../sample/" + data_name + "_sample." + str(i+start_id)
        nx.write_edgelist(aG, file_name, '#', '\t', False, 'utf-8')     # data=False

#######################################################
# convert G to connected graph
def convert_to_connected(G, out_file):
    component_list = nx.connected_components(G) # in decreasing size
    #
    node_list = [] # representative nodes for components
    for c in component_list:
        node_id = c[random.randint(0,len(c)-1)]
        node_list.append(node_id)
        
    #
    for i in range(len(node_list)-1):
        u = node_list[i]
        v = node_list[i+1]
        G.add_edge(u,v)
    
    #
    nx.write_edgelist(G, out_file, '#', '\t', False, 'utf-8')   # data = False

#######################################################
# to METIS format, for graph partitioning
def convert_to_metis_file(G, out_file):
    
    max_id = max(G.nodes_iter())
    
    f = open(out_file, 'w')
    # write n_nodes, n_edges
    n_nodes = G.number_of_nodes()
    n_edges = G.number_of_edges()
    n_selfloops = G.number_of_selfloops()               # self-loops in G for FF graphs
    f.write("%d %d\n"%(max_id+1, n_edges-n_selfloops/2))
    
    # write adjacent nodes for each node
    for u in range(max_id+1):     # for u in G.nodes_iter() : WRONG !
        if G.has_node(u):
            for v in G.neighbors_iter(u):
                f.write("%d "%(v+1))     # 1-based index
        f.write("\n")
    
    f.close()
    
#######################################################
#
def normalize_and_save_graph(G, new_file):
    newG = nx.Graph()
    
    min_id = min(G.nodes_iter())
    max_id = max(G.nodes_iter())
    print "min_id =", min_id
    print "max_id =", max_id
    
    i = 0
    id_dict = {}    # dict for node ids
    for u in G.nodes_iter():
        id_dict[u] = i
        i += 1
    
    for (u,v) in G.edges_iter():
        if u != v:
            newG.add_edge(id_dict[u], id_dict[v])
        else:
            print "self-loop at ", u

    # write to file
    nx.write_edgelist(newG, new_file, '#', '\t', False, 'utf-8')   # data = False
        
#######################################################
if __name__ == '__main__':
        
    ### ER   
#    generate_ER_graph(200, 0.2, "../data/er_200_02.gr")
#    generate_ER_graph(200, 0.02, "../data/er_200_002.gr")
#    generate_ER_graph(500, 0.2, "../data/er_500_02.gr")
#    generate_ER_graph(500, 0.05, "../data/er_500_005.gr")

#    generate_ER_graph(1000, 0.2, "../data/er_1000_02.gr")
#    generate_ER_graph(1000, 0.1, "../data/er_1000_01.gr")
#    generate_ER_graph(1000, 0.05, "../data/er_1000_005.gr")
#    generate_ER_graph(1000, 0.01, "../data/er_1000_001.gr")

#    generate_ER_graph(2000, 0.05, "../data/er_2000_005.gr")

#    generate_ER_graph(5000, 0.05, "../data/er_5000_005.gr")
#    generate_ER_graph(5000, 0.025, "../data/er_5000_0025.gr")
#    generate_ER_graph(5000, 0.01, "../data/er_5000_001.gr")
#    generate_ER_graph(5000, 0.005, "../data/er_5000_0005.gr")

#    generate_ER_graph(10000, 0.005, "../data/er_10000_0005.gr")
#    generate_ER_graph(10000, 0.002, "../data/er_10000_0002.gr")
#    generate_ER_graph(10000, 0.001, "../data/er_10000_0001.gr")
    
#    generate_ER_graph(20000, 0.002, "../data/er_20000_0002.gr")
#    generate_ER_graph(20000, 0.0005, "../data/er_20000_00005.gr")

#    generate_ER_graph(100000, 0.0001, "../data/er_100000_00001.gr")


    ### SM
#    generate_SM_graph(1000, 0.05, 11, "../data/sm_1000_005_11.gr")
    
#    generate_SM_graph(10000, 0.05, 11, "../data/sm_10000_005_11.gr")
#    generate_SM_graph(100000, 0.05, 11, "../data/sm_100000_005_11.gr")
    
    ### BA
#    generate_BA_graph(1000, 5, "../data/ba_1000_5.gr")
    
#    generate_BA_graph(10000, 5, "../data/ba_10000_5.gr")
#    generate_BA_graph(100000, 5, "../data/ba_100000_5.gr")
    
    ### PL
#    generate_PL_graph(1000, 5, 0.1, "../data/pl_1000_5_01.gr")
    
#    generate_PL_graph(10000, 5, 0.1, "../data/pl_10000_5_01.gr")
#    generate_PL_graph(100000, 5, 0.1, "../data/pl_100000_5_01.gr")

    ### FF - disconnected graphs --> need convert_to_connected()
#    generate_FF_graph(1000, 0.1, "../data/ff_1000_01.gr")
#    generate_FF_graph(1000, 0.5, "../data/ff_1000_05.gr")
#    generate_FF_graph(1000, 0.7, "../data/ff_1000_07.gr")           # slow, 200k edges
#    generate_FF_graph(10000, 0.5, "../data/ff_10000_05.gr")
#    generate_FF_graph(100000, 0.4, "../data/ff_100000_04.gr")       # 175k edges
#    generate_FF_graph(100000, 0.45, "../data/ff_100000_045.gr")       # 230k edges
#    generate_FF_graph(100000, 0.5, "../data/ff_100000_05.gr")       # 1.6M edges

    ### ER with trust
#    generate_ER_graph_with_trust(200, 0.02, "../data/er_trust_200_002.gr")
#    generate_ER_graph_with_trust(200, 0.05, trust_list_1, "../data/er_trust_200_005_1.gr")
#    generate_ER_graph_with_trust(200, 0.05, trust_list_2, "../data/er_trust_200_005_2.gr")
#    generate_ER_graph_with_trust(200, 0.05, trust_list_3, "../data/er_trust_200_005_3.gr")
#    generate_ER_graph_with_trust(200, 0.1, "../data/er_trust_200_01.gr")

    ### SM with trust
#    generate_SM_graph_with_trust(200, 0.05, 11, trust_list_1, "../data/sm_trust_200_005_5_1.gr")    # k=11 --> 200*(11-1)/2 = 1000 edges
#    generate_SM_graph_with_trust(200, 0.05, 11, trust_list_2, "../data/sm_trust_200_005_5_2.gr")
#    generate_SM_graph_with_trust(200, 0.05, 11, trust_list_3, "../data/sm_trust_200_005_5_3.gr")
    
    
    ##### convert_to_connected
##    in_file = "../data/ff_10000_05.gr"
##    out_file = "../data/ff_10000_05_connected.gr"
##    in_file = "../data/ff_100000_05.gr"
##    out_file = "../data/ff_100000_05_connected.gr"
#    in_file = "../data/ff_100000_045.gr"
#    out_file = "../data/ff_100000_045_connected.gr"
#    
#    G = nx.read_edgelist(in_file, '#', '\t', None, nodetype=int, data=False)
#    
#    convert_to_connected(G, out_file)
   
    ##### convert_to_metis_file
    ## real graphs
#    in_file = "../data/com_dblp_ungraph.gr"        # com_dblp_ungraph (300k nodes, 1M edges)
#    out_file = "../data/com_dblp_ungraph.metis"
#    in_file = "../data/com_amazon_ungraph.gr"        # com_amazon_ungraph (1M nodes, 3M edges) , read:36s, normalize:35s, convert METIS:14s
#    out_file = "../data/com_amazon_ungraph.metis"
#    in_file = "../data/com_youtube_ungraph.gr"        # com_youtube_ungraph (1M nodes, 3M edges) , read:36s, normalize:35s, convert METIS:14s
#    out_file = "../data/com_youtube_ungraph.metis"

    # directed graphs
    in_file = "../data/prod_amazon0302_directed.gr"        # prod_amazon0302_directed, 260k nodes, 1.23M edges (899792 undirected edges in METIS)
    out_file = "../data/prod_amazon0302_directed.metis"
    
    ## 10k nodes
#    in_file = "../data/er_10000_0001.gr"        # ER
#    out_file = "../data/er_10000_0001.metis"
#    in_file = "../data/sm_10000_005_11.gr"      # SM
#    out_file = "../data/sm_10000_005_11.metis"
#    in_file = "../data/ba_10000_5.gr"           # BA
#    out_file = "../data/ba_10000_5.metis"
#    in_file = "../data/pl_10000_5_01.gr"        # PL
#    out_file = "../data/pl_10000_5_01.metis"
#    in_file = "../data/ff_10000_05_connected.gr"        # FF, have selfoops
#    out_file = "../data/ff_10000_05_connected.metis"
    
    ## 100k nodes
#    in_file = "../data/er_100000_00001.gr"        # ER
#    out_file = "../data/er_100000_00001.metis"
#    in_file = "../data/sm_100000_005_11.gr"      # SM
#    out_file = "../data/sm_100000_005_11.metis"
#    in_file = "../data/ba_100000_5.gr"           # BA
#    out_file = "../data/ba_100000_5.metis"
#    in_file = "../data/pl_100000_5_01.gr"        # PL
#    out_file = "../data/pl_100000_5_01.metis"
#    in_file = "../data/ff_100000_045_connected.gr"        # FF, have selfoops
#    out_file = "../data/ff_100000_045_connected.metis"
    
    # TEST normalize_and_save_graph() + convert to METIS file
    print "in_file =", in_file
    print "out_file =", out_file
    start = time.clock()
#    G = nx.read_edgelist("../data/com_amazon_ungraph_org.gr", '#', '\t', None, nodetype=int, data=False)
    G = nx.read_edgelist(in_file, '#', '\t', None, nodetype=int, data=False)        # undirected G even for directed graphs !
    print "#edges =", G.number_of_edges()
    print "#selfloops =", G.number_of_selfloops()
    print "Read graph - elapsed ", time.clock() - start
    
#    start = time.clock()
#    normalize_and_save_graph(G, "../data/com_amazon_ungraph.gr")   #zero-based node ids and remove selfloops
#    print "Normalize and save graph - elapsed ", time.clock() - start
    
    start = time.clock()
    convert_to_metis_file(G, out_file)
    print "Convert to METIS file - elapsed ", time.clock() - start

    # TEST generate_samples_and_save()
    start = time.clock()
    ## entropy-based
#    data_name = "er_100000_00001_entropy_001_2_001"
#    data_name = "er_100000_00001_entropy_01_2_001"
#    data_name = "sm_100000_005_11_entropy_001_2_001"
#    data_name = "sm_100000_005_11_entropy_01_2_001"
#    data_name = "ba_100000_5_entropy_001_2_001"
    data_name = "ba_100000_5_entropy_01_2_001"
    
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
#    data_name = "com_youtube_ungraph_cvxopt_600000_60_missing"      # random, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_600000_30_nb_missing"      # nearby, MOSEK
#    data_name = "com_youtube_ungraph_cvxopt_900000_600_missing"      # 260s/5
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

    #
#    print "data_name =", data_name
#    G = nx.read_edgelist("../out/" + data_name + ".out", '#', '\t', None, nodetype=int, data=True)
#    print "read uncertain graph G - DONE, elapsed", time.clock() - start
#    
#    start = time.clock()
#    generate_samples_and_save(G, 5, data_name, start_id=0)      # NOTE: change start_id !!!
#    print "generate_samples_and_save - DONE, elapsed", time.clock() - start
#    
#    #
#    print "DONE"
    