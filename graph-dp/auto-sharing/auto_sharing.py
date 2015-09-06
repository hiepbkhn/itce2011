'''
Created on Mar 4, 2014

@author: Nguyen Huu Hiep
autonomous sharing
- each node share using a BFS tree
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np

D_TRUST = 0.1

#######################################################
def find_bfs_edges(G, u):
    nG = nx.Graph()
    for e in G.edges_iter(None, True):
        nG.add_edge(e[0], e[1], weight= -math.log(e[2]['t']))
    
    #
    paths = nx.shortest_path(nG, source=u, target=None, weight="weight")
    
#    print paths
    
    # build BFS tree from paths
    marked = {}
    for v in G.nodes_iter():
        marked[v] = False 
    marked[u] = True
    bfs_edges = []
    idx = 1
    while True:
        stopped = True
        for v in G.nodes_iter():
            if not marked[v]:   # v 
                stopped = False
                break
        if stopped:
            break
        #
        for p in paths.itervalues():
            if idx < len(p) and (not marked[p[idx]]):
                bfs_edges.append((p[idx-1], p[idx]))
                marked[p[idx]] = True 
        #
        idx += 1
    
    #
    return bfs_edges

#######################################################
def prepare_friend_list(G, u, t_level):
    
    result = []
    #
    n_nodes = G.number_of_nodes()
    f_list = G.neighbors(u)     # true friend list
    
    perm = np.random.permutation(n_nodes)
    n_noisy = int(ceil((1.0 - t_level)*len(f_list)))    # number of noisy (fake) edges
    n_list = []
    if n_noisy > 0:
        for i in perm:
            if not G.has_edge(u,i):
                n_list.append(i)
                if len(n_list) == n_noisy:  # only select n_noisy fake friends
                    break
    
    # assign probabilities
    sigma = D_TRUST/t_level
    
    for v in f_list:
        while True:
            r_e = gauss(0,sigma)    #    normal distribution
            if r_e >= 0.0 and r_e <= 1.0:
                break
        result.append((u,v,1.0-r_e))    # note 1.0-r_e ~ 1.0
    
    for v in n_list:
        while True:
            r_e = gauss(0,sigma)    #
            if r_e >= 0.0 and r_e <= 1.0:
                break
        result.append((u,v,r_e))        
    
    #
    return result


#######################################################
# compute new_list from a_list 
def forward_friend_list(a_list, t_level):
    
    # change probabilities in a_list
    sigma = D_TRUST/t_level
    
    new_list = []
    for e in a_list:
        while True:
            r_e = gauss(0,sigma)    #
            if r_e >= 0.0 and r_e <= 1.0:
                break
        new_e = (e[0], e[1], e[2]*(1.0 - r_e))      # reduce edge's probability
        new_list.append(new_e)
    # 
    return new_list    
    

#######################################################
# fl_dict[v] contains all edges that v receives
def share_from_user(G, u, fl_dict):
    # compute BFS/DFS-tree with root u
    
#    tree_edges = nx.bfs_edges(G, u)        # use built-in
#    tree_edges = find_bfs_edges(G, u)        # use weighted
    
    tree_edges = nx.dfs_edges(G, u)
    
    
    # (exact) my friend list
    my_list = []
    for v in G.neighbors(u):
        my_list.append((u, v, 1.0))
    fl_dict[u].extend(my_list)

    # init
    list_at_node = [[] for v in G.nodes_iter()]
    
    # propagate
    for (v,w) in tree_edges:
#        print "v,w =", v, w 
        t_level = G.edge[v][w]['t']
        
        if v == u:  # prepare
            a_list = prepare_friend_list(G, u, t_level)
#            print "len(a_list) =", len(a_list)
            list_at_node[w] = a_list
        else:       # forward
            new_list = forward_friend_list(list_at_node[v], t_level)
#            print "len(new_list) =", len(new_list)
            list_at_node[w] = new_list
            
        # add new_list into fl_dict[w]
        fl_dict[w].extend(list_at_node[w])
#        print "w, len(fl_dict[w]) =", w, len(fl_dict[w])

#######################################################
def filter_max(fl_dict): 
    graph_list = []     # list of aggregated graph at nodes
    for (u, e_list) in fl_dict.iteritems():
#        print "len(e_list) =", len(e_list)
        aG = nx.Graph()
        for e in e_list:
            aG.add_edge(e[0],e[1]) 
        #
        graph_list.append(aG)
    
    #
    return graph_list

#######################################################
def filter_threshold(fl_dict, threshold): 
    graph_list = []     # list of aggregated graph at nodes
    for (u, e_list) in fl_dict.iteritems():
#        print "len(e_list) =", len(e_list)
        aG = nx.Graph()
        for e in e_list:
            if e[2] >= threshold:
                aG.add_edge(e[0],e[1]) 
        #
        graph_list.append(aG)
    
    #
    return graph_list

#######################################################
def filter_num_edges(fl_dict, num_edges): 
    graph_list = []     # list of aggregated graph at nodes
    for (u, e_list) in fl_dict.iteritems():
#        print "len(e_list) =", len(e_list)

        # collect max edges
        e_dict = {}
        for e in e_list:
            v = e[0]
            w = e[1]
            if v > w:
                v = e[1]
                w = e[0]
            if not e_dict.has_key((v,w)):
                e_dict[(v,w)] = e[2]
            else:
                e_dict[(v,w)] = max([e_dict[(v,w)], e[2]])
                
        # sort and retain top num_edges
        temp_list = []
        for ((v,w), weight) in e_dict.iteritems():
            temp_list.append((v,w,weight))
        temp_list = sorted(temp_list, key=lambda item:item[2], reverse=True)

        #
        aG = nx.Graph()
        for i in range(num_edges):
            aG.add_edge(temp_list[i][0], temp_list[i][1]) 
        #
        graph_list.append(aG)
    
    #
    return graph_list
    
#######################################################
def auto_share(G, fl_dict):
    for u in G.nodes_iter():
        share_from_user(G, u, fl_dict)
    #

#######################################################
def compare_original(bG, aG):
    diff_b = []
    for e in bG.edges_iter():
        if not aG.has_edge(e[0],e[1]):
            diff_b.append((e[0],e[1]))
    diff_a = []
    for e in aG.edges_iter():
        if not bG.has_edge(e[0],e[1]):
            diff_a.append((e[0],e[1]))
    #
    return (-len(diff_b), len(diff_a))
    

#######################################################
if __name__ == '__main__':
#    data_set = "er_trust_200_002"
#    data_set = "er_trust_200_01"

#    data_set = "er_trust_200_005_1"
#    data_set = "er_trust_200_005_2"
#    data_set = "er_trust_200_005_3"

#    data_set = "sm_trust_200_005_5_1"
#    data_set = "sm_trust_200_005_5_2"
    data_set = "sm_trust_200_005_5_3"


    
    file_name = "../data/" + data_set + ".gr"
    G = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)
    
    #
    print "#nodes :", G.number_of_nodes()
    print "#edges :", len(G.edges())
    print "#self-loops :", G.number_of_selfloops()
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    print "#components =", nx.number_connected_components(G)
    print "diameter =", nx.diameter(G)
    
    # TEST nx.bfs_tree()
#    G = nx.Graph()
#    G.add_edges_from([(1,2,{'t':1.0}),(1,3,{'t':0.9}),(1,5,{'t':1.0}),(2,3,{'t':0.6}),(3,4,{'t':0.8}),(3,6,{'t':0.7}),(4,6,{'t':0.3}),(5,6,{'t':1.0})])
#    tree = nx.bfs_tree(G, 1)
#    print tree.edges()
#    tree = nx.dfs_tree(G, 5)
#    print tree.edges()
#    dfs_edges = nx.dfs_edges(G, 5)
#    for e in dfs_edges:
#        print e 
    
    #
    fl_dict = {}
    for u in G.nodes_iter():
        fl_dict[u] = []
    
    
    # TEST prepare_friend_list() and forward_friend_list()
#    a_list = prepare_friend_list(G, 0, 0.1)
#    print a_list
#    
#    new_list = forward_friend_list(a_list, 0.8)
#    print new_list
    
    # TEST share_from_user()
#    share_from_user(G, 0, fl_dict)
#    print "DONE !"
##    print fl_dict[0]
##    print fl_dict[199]
    
    # TEST auto_share()
    auto_share(G, fl_dict)
    print "DONE !"
#    print fl_dict[0]

#    graph_list = filter_max(fl_dict)
#    graph_list = filter_threshold(fl_dict, 0.5)
    graph_list = filter_num_edges(fl_dict, len(G.edges()))    # 970, 1007, 983
    
    diff_list_1 = []
    diff_list_2 = []
    for aG in graph_list:
        (d1, d2) = compare_original(G, aG)
        diff_list_1.append(d1)
        diff_list_2.append(d2)
        print d1, d2
    #
    print np.mean(diff_list_1), np.mean(diff_list_2)


    # TEST find_bfs_edges()
#    bfs_edges = find_bfs_edges(G, 1)
#    print bfs_edges
        
        
        