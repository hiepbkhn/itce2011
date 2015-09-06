'''
Created on May 21, 2014

@author: huunguye
'''

import sys
import time
import random
import math
import networkx as nx
import scipy.io
import numpy as np

N_SAMPLES = 5

SWITCH_RANDOM = 0
SWITCH_NEARBY = 1

#######################################################
def random_switch(G, num_sw):
    aG = nx.Graph()
    aG.add_edges_from(G.edges_iter())

    n_nodes = G.number_of_nodes()
    node_list = [u for u in G.nodes_iter()]

    count = 0
    while count < num_sw:
        # find a random pair
        u = node_list[random.randint(0, n_nodes-1)]
        v = node_list[random.randint(0, n_nodes-1)]
        if not aG.has_node(u) or not aG.has_node(v):    # to avoid node missing error !
            continue
        if u != v:
            u_neighbors = aG.neighbors(u)
            v_neighbors = aG.neighbors(v)
            u_num_nbrs = len(u_neighbors)
            v_num_nbrs = len(v_neighbors)
            # select w,t
            w = u_neighbors[random.randint(0, u_num_nbrs-1)]
            t = v_neighbors[random.randint(0, v_num_nbrs-1)]
            if w != v and t != u and w != t and not aG.has_edge(u, t) and not aG.has_edge(v, w):
                count += 1
                aG.add_edge(u, t)
                aG.add_edge(v, w)
                aG.remove_edge(u, w)
                aG.remove_edge(v, t)
#                aG.add_nodes_from([u,v,w,t])    # to avoid node missing error !

    # 
    return aG

#######################################################
def nearby_switch(G, num_sw):
    aG = nx.Graph()
    aG.add_edges_from(G.edges_iter())

    n_nodes = G.number_of_nodes()
    node_list = [u for u in G.nodes_iter()]

    count = 0
    while count < num_sw:
        # find a random pair
        u = node_list[random.randint(0, n_nodes-1)]
        if not aG.has_node(u):              # to avoid node missing error !
            continue
        u_neighbors = aG.neighbors(u)
        u_num_nbrs = len(u_neighbors)
        
        v = u_neighbors[random.randint(0, u_num_nbrs-1)]
        v_neighbors = aG.neighbors(v)
        v_num_nbrs = len(v_neighbors)
        # select w,t
        w = u_neighbors[random.randint(0, u_num_nbrs-1)]
        t = v_neighbors[random.randint(0, v_num_nbrs-1)]
        if w != v and t != u and w != t and not aG.has_edge(u, t) and not aG.has_edge(v, w):
            count += 1
            aG.add_edge(u, t)
            aG.add_edge(v, w)
            aG.remove_edge(u, w)
            aG.remove_edge(v, t)
#            aG.add_nodes_from([u,v,w,t])    # to avoid node missing error !
            
    # 
    return aG

#######################################################
if __name__ == '__main__':
    
#    file_name = "er_200_02"
#    file_name = "er_500_02"
#    file_name = "er_1000_02"
#    file_name = "er_1000_01"
#    file_name = "er_1000_005"
#    file_name = "er_1000_001"
#    file_name = "er_2000_005"
#    file_name = "er_5000_005"
#    file_name = "er_5000_0025"
#    file_name = "er_5000_001"
#    file_name = "er_5000_0005"
#    file_name = "er_10000_0005"
#    file_name = "er_10000_0002"
#    file_name = "er_10000_0001"        #10k nodes
#    file_name = "er_20000_0002"
#    file_name = "er_20000_00005"
#    file_name = "er_100000_00001"       #100k nodes

#    file_name = "sm_10000_005_11"       #10k nodes
#    file_name = "sm_100000_005_11"       #100k nodes

#    file_name = "ba_100000_5"           #100k nodes,     30s/5 samples

#    file_name = "ff_1000_05"           # 3455 edges

    ## real graphs
#    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  

    
    # TEST random_switch()
#    num_sw = 50000      # number of switches
#    start = time.clock()
#    aG = random_switch(G, num_sw)
#    print "random_switch: DONE, elapsed :", time.clock() - start
#
#    out_file = "../switch/" + file_name + "_switch_rand_" + str(num_sw) +".out"
#    nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
    
    ######
    # Command-line param for automation (bash): <file_name> <num_sw> <switch_type> <n_samples> <start_id>
    n_samples = 1
    start_id = 5
    num_sw = 100000
    switch_type = 1
    if len(sys.argv) > 5:
        file_name = sys.argv[1]
        num_sw = int(sys.argv[2])
        switch_type = int(sys.argv[3])
        n_samples = int(sys.argv[4])
        start_id = int(sys.argv[5])

    print "file_name =", file_name
    print "num_sw =", num_sw
    print "switch_type =", switch_type
    print "n_samples =", n_samples
    print "start_id =", start_id
    
    #
    G = nx.read_edgelist("../data/" + file_name + ".gr", '#', '\t', None, nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
    print "#nodes :", G.number_of_nodes()
    print "#edges :", len(G.edges())
    print "#self-loops :", G.number_of_selfloops()
    print "#components :", len(nx.connected_components(G))
    n_nodes = G.number_of_nodes()
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg

    #
    start = time.clock()
    for i in range(n_samples):
        print "sample", i+start_id
        
        if switch_type == SWITCH_RANDOM:
            # RANDOM
            aG = random_switch(G, num_sw)
            out_file = "../switch/" + file_name + "_switch_rand_" + str(num_sw) +"_sample." + str(i+start_id)
        elif switch_type == SWITCH_NEARBY:
            # NEARBY
            aG = nearby_switch(G, num_sw)
            out_file = "../switch/" + file_name + "_switch_nb_" + str(num_sw) + "_sample." + str(i+start_id)
        else:
            print "WRONG <switch_type>. Exit..."
            sys.exit()
            
        nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
        
    print "random/nearby_switch for " + str(n_samples) +" samples : DONE, elapsed :", time.clock() - start
    
    
    

