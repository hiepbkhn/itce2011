'''
Created on Jun 4, 2014

@author: huunguye
'''

import sys
import time
import random
import math
import igraph as ig
import scipy.io
import numpy as np

N_SAMPLES = 5
SWITCH_RANDOM = 0
SWITCH_NEARBY = 1

#######################################################
def random_switch(G, num_sw):
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    edge_dict = {}  
    for e in G.es:
        edge_dict[(e.source, e.target)] = 1
        edge_dict[(e.target, e.source)] = 1

    n_nodes = G.vcount()
    node_list = range(G.vcount())

    count = 0
    while count < num_sw:
        # find a random pair
        u = node_list[random.randint(0, n_nodes-1)]
        v = node_list[random.randint(0, n_nodes-1)]
        if u != v:
            u_neighbors = G.neighbors(u)    # use G because aG is empty
            v_neighbors = G.neighbors(v)
            u_num_nbrs = len(u_neighbors)
            v_num_nbrs = len(v_neighbors)
            if u_num_nbrs == 0 or v_num_nbrs == 0:
                continue
            # select w,t
            w = u_neighbors[random.randint(0, u_num_nbrs-1)]
            t = v_neighbors[random.randint(0, v_num_nbrs-1)]
            if w != v and t != u and w != t and not edge_dict.has_key((u,t)) and not edge_dict.has_key((v,w)):
                count += 1
                if count % 1000 == 0:
                    print count
                edge_dict[(u,t)] = 1
                edge_dict[(t,u)] = 1
                edge_dict[(v,w)] = 1
                edge_dict[(w,v)] = 1
                del edge_dict[(u,w)]
                del edge_dict[(w,u)]
                del edge_dict[(v,t)]
                del edge_dict[(t,v)]

    #
    edge_list = []
    for e in edge_dict.iterkeys():
        if e[0] < e[1]:
            edge_list.append(e)
    aG.add_edges(edge_list) 
    return aG

#######################################################
def nearby_switch(G, num_sw):
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    edge_list = [(e.source, e.target) for e in G.es]
    aG.add_edges(edge_list)

    n_nodes = G.vcount()
    node_list = range(G.vcount())

    count = 0
    while count < num_sw:
        # find a random pair
        u = node_list[random.randint(0, n_nodes-1)]
        u_neighbors = aG.neighbors(u)
        u_num_nbrs = len(u_neighbors)
        
        v = u_neighbors[random.randint(0, u_num_nbrs-1)]
        v_neighbors = aG.neighbors(v)
        v_num_nbrs = len(v_neighbors)
        # select w,t
        w = u_neighbors[random.randint(0, u_num_nbrs-1)]
        t = v_neighbors[random.randint(0, v_num_nbrs-1)]
        if w != v and t != u and w != t and aG.get_eid(u,t,directed=False, error=False) == -1 and aG.get_eid(v,w,directed=False, error=False) == -1:
            count += 1
            if count % 1000 == 0:
                print count
            aG.add_edge(u, t)
            aG.add_edge(v, w)
            aG.delete_edges([(u, w), (v, t)])

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
    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
#    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  

    
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
    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=False)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
    print "#nodes :", G.vcount()
    print "#edges :", G.ecount()
    print "#components :", len(G.clusters("weak"))
    n_nodes = G.vcount()
    deg_list = G.degree(G.vs)         # dict[node] = deg
    min_deg = min(deg_list)
    max_deg = max(deg_list)
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    
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
            
        aG.write_edgelist(out_file)
        
    print "random/nearby_switch for " + str(n_samples) +" samples : DONE, elapsed :", time.clock() - start
    
    
    

