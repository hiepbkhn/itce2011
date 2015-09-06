'''
Created on Apr 4, 2014

@author: huunguye
'''

import time
from random import *
import math
import networkx as nx
import scipy.io


#######################################################
def compute_triad_weights(G):
    
    triad_count = {}    # edge (u,v) with u < v
    
    # clone G
    aG = nx.Graph()
    aG.add_edges_from(G.edges_iter())
    e_list = []
    for e in aG.edges_iter():
        if e[0] < e[1]:
            e_list.append((e[0], e[1]))
        else:
            e_list.append((e[1], e[0]))
            
    # visit each edge once ! , each triad is computed twice!
    for e in e_list:
        for u in aG.neighbors(e[0]):
            if u != e[1] and (not aG.has_edge(u, e[1])):
                if u < e[1]:
                    u1 = u
                    u2 = e[1]
                else:
                    u1 = e[1]
                    u2 = u
                if triad_count.has_key((u1,u2)):
                    triad_count[(u1,u2)] += 1
                else:
                    triad_count[(u1,u2)] = 1
                    
        for u in aG.neighbors(e[1]):
            if u != e[0] and (not aG.has_edge(u, e[0])):
                if u < e[0]:
                    u1 = u
                    u2 = e[0]
                else:
                    u1 = e[0]
                    u2 = u
                if triad_count.has_key((u1,u2)):
                    triad_count[(u1,u2)] += 1
                else:
                    triad_count[(u1,u2)] = 1
        #
#        aG.remove_edge(e[0], e[1]) 
    
    for k in triad_count.iterkeys():
        triad_count[k] = triad_count[k] / 2
    #
    return triad_count

#######################################################
if __name__ == '__main__':
    
    ##
#    filename = "../data/er_200_002.gr"
#    filename = "../data/er_200_02.gr"
#    filename = "../data/er_1000_005.gr"    #k=5:zero, k=3,     k=4, 472 cliques, total_size=2018, total_set=833
#    filename = "../data/sm_1000_005_11.gr"  #k=5, 44 cliques, total_size=1098, total_set=996
#    filename = "../data/ba_1000_5.gr"
#    filename = "../data/pl_1000_5_01.gr"
#    filename = "../data/ff_1000_05.gr"

    ## 10k nodes
#    filename = "../data/er_10000_0001.gr"   #k=4,5:zero,  k=3: (153, 460, 449)
#    filename = "../data/sm_10000_005_11.gr" #k=5, (351, 10818, 9954), k=10:zero 

    ## 100k nodes
    
    filename = "../data/er_100000_00001.gr"     #k=4,5:zero, k=3 (181, 543, 543)
#    filename = "../data/sm_100000_005_11.gr"    #k=5: (3370,108104,99719)  k=10:zero 

    #
    start = time.clock()
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    print "Read graph - Elapsed ", (time.clock() - start)

    # CHECK k-clique communities
#    k = 3
#    print "k =", k
#    com = list(nx.k_clique_communities(G, k))
#    print "len(com) =", len(com)
#    total_size = 0
#    total_set = set([])
#    for i in range(len(com)):
#        total_size += len(com[i])
#        total_set = total_set | com[i]
#        print "i =", i, "len(com[i]) =", len(com[i]), "com =", list(com[i])
#    print "total_size =", total_size
#    print "len(total_set) =", len(total_set)
    
    
    # TEST compute_triad_weights
#    G = nx.Graph()
#    G.add_edges_from([(1,2),(1,3),(1,5),(2,3),(2,4),(2,5),(2,6)])
    
    start = time.clock()
    triad_count = compute_triad_weights(G)
    print "Elapsed ", (time.clock() - start)
    
    print "len(triad_count) =", len(triad_count)
    print "sum(triad_count) =", sum(triad_count.itervalues())
    
    triad_list = [(k, v) for (k, v) in triad_count.iteritems()]
    triad_list = sorted(triad_list, key=lambda item:item[1])
    print triad_list[0:20]
    print triad_list[-20:-1]
    
    
    