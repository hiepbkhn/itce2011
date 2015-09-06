'''
Created on Jul 17, 2014

@author: huunguye
'''


import time
import random
import math
import networkx as nx
import igraph as ig
import scipy.io
import scipy.sparse as sp
import numpy as np

#######################################################
def deterministic_walk_transform_multigraph(G, t, alpha=1.0):
    aG = nx.MultiGraph()
    
    deg_dict = nx.degree(G)
    
    # 1 - prepare permutations (routing tables) for nodes
    routers = []    # list of dicts    routers[u][v] = out-link of (v,u)
    for u in G.nodes_iter():
        in_links = G.neighbors(u)
        perm = np.random.permutation(len(in_links))
        out_links = {}
        for i in range(len(perm)):
            out_links[in_links[i]] = in_links[perm[i]]
        routers.append(out_links)
        
    # DEBUG
#    for u in G.nodes_iter():
#        print u, routers[u]    
        
    # walking on routers+nb_list
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            prev_z = u
            # perform (t-1) hop DETERMINISTIC walk from v
            z = v
            for i in range(t-1):
                cur_z = z
                z = routers[cur_z][prev_z]
                prev_z = cur_z
                
            #
            if count == 1:
                if deg_dict[u] == 1:        # EXCEPTION !
                    val = random.random()
                    if val < 0.5:
                        aG.add_edge(u, z)
                else:
                    val = random.random()   # use alpha
                    if val < alpha:
                        aG.add_edge(u, z)
            else:
                val = random.random()
                if val < (0.5*deg_dict[u] - alpha)/(deg_dict[u] - 1):   # use alpha
                    aG.add_edge(u, z)
            #
            count += 1
    
    #
    return aG

if __name__ == '__main__':
    
    ## 100k 
#    file_name = "er_100000_00001"       #100k nodes
#    file_name = "sm_100000_005_11"       #100k nodes
#    file_name = "ba_100000_5"       #100k nodes

    ## real graphs
    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
#    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  
    
    #
    print "file_name =", file_name
    G = nx.read_edgelist("../data/" + file_name + ".gr", '#', '\t', None, nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
#    G = nx.read_edgelist(file_name, '#', ' ', None, nodetype=int)
    
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
    
    
    # TEST deterministic_walk_transform_multigraph()
    print "TEST deterministic_walk_transform_multigraph"
    
    start = time.clock()
    t = 10
    alpha = 0.5
    n_samples = 5
    start_id = 0
    for i in range(n_samples):
        print "sample", i
        
        aG = deterministic_walk_transform_multigraph(G, t, alpha)
        print "#nodes :", aG.number_of_nodes()
        print "#edges :", len(aG.edges())
        print "#self-loops :", aG.number_of_selfloops()
        
        # save uncertain graph for generate_sample (see: graph_generator.py)
        out_file = "../randwalk/" + file_name + "_detwalk_keep_" + str(t) +"_" + str(alpha) + "_sample." + str(i+start_id)
        nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
    
    print "deterministic_walk_transform_multigraph for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start   
    
    
    
    
    
    
    
    