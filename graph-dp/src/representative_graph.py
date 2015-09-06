'''
Created on Mar 12, 2014

@author: Nguyen Huu Hiep
References: 
- The Pursuit of a Good Possible World - Extracting Representative Instances of Uncertain Graphs (SIGMOD'14)

'''

import time
import random
import networkx as nx
import scipy.io
import numpy as np
import math
import heapq
from randomness_measure import read_gml_graph, normalize_graph


N_ADR_STEPS = 1000   # number of rewirings 

#######################################################
def compute_deg_discrepency(G, aG):
    discrepancy_list = [0 for i in range(G.number_of_nodes())]
    expected_deg_G = compute_expected_deg(G)
    
    for u in G.nodes_iter():
        discrepancy_list[u] = len(aG.neighbors(u)) - expected_deg_G[u]
    #
    return discrepancy_list


#######################################################
def assign_probabilities(G, outfile):
    
    G2 = nx.Graph()

    # 1. UNIFORM distribution
#    for e in G.edges_iter():
#        val = random.random()
#        G2.add_edge(e[0], e[1], {'p':val})
    
    # 2. EXPONENTIAL distribution
    lambd = 1.0
    for e in G.edges_iter():
        val = random.expovariate(lambd)
        while val > 1.0:
            val = random.expovariate(lambd)     # conflict with form np import *
        G2.add_edge(e[0], e[1], {'p':val})
    
    
    ###
    # save uncertain graph to file
    nx.write_edgelist(G2, outfile, '#', '\t', True, 'UTF-8')
    
#######################################################
def compute_expected_deg(G):
    expected_deg_G = {}
    for u in G.nodes_iter():
        expected_deg_u = 0.0
        for v in G.neighbors(u):
            expected_deg_u += G.edge[u][v]['p']
            
        expected_deg_G[u] = expected_deg_u
    #
    return expected_deg_G    

#######################################################
# MP - most probable
def most_probable_representative(G):
    aG = nx.Graph()
    for e in G.edges_iter(data=True):
        if e[2]['p'] >= 0.5:
            aG.add_edge(e[0], e[1])
    #
    return aG



#######################################################
# GP - greedy probability
def greedy_probability_representative(G):
    aG = nx.Graph()
    aG.add_nodes_from(G.nodes_iter())
    
    # compute expected degrees
    expected_deg_G = compute_expected_deg(G)
    
    # sort edges in descending order of probabilities 
    e_list = []
    for e in G.edges_iter(data=True):
        e_list.append((e[0], e[1], e[2]['p']))
    
    e_list = sorted(e_list, key=lambda e: e[2], reverse=True)            
    
    # greedy add edges to aG
    for e in e_list:
        u = e[0]
        v = e[1]
        dis_u = len(aG.neighbors(u)) - expected_deg_G[u]
        dis_v = len(aG.neighbors(v)) - expected_deg_G[v]
        if abs(dis_u + 1) + abs(dis_v + 1) < abs(dis_u) + abs(dis_v):
            aG.add_edge(u,v)
    
    #
    return aG

#######################################################
# ADR - average degree rewiring
def adr_representative(G):
    
    aG = nx.Graph()
    aG.add_nodes_from(G.nodes_iter())
    
    # Phase 1
    # compute expected degrees
    expected_deg_G = compute_expected_deg(G)
    
    # sum of expected degrees
    sum_p = sum(e[2]['p'] for e in G.edges_iter(data=True))
    
    e_list = []
    for e in G.edges_iter(data=True):
        e_list.append((e[0], e[1], e[2]['p']))
    
    e_list = sorted(e_list, key=lambda e: e[2], reverse=True)  
        
    # add to aG
    nA_edges = int(math.floor(sum_p))    
    for i in range(nA_edges):
        aG.add_edge(e_list[i][0], e_list[i][1]) 
    
    # Phase 2 - Rewiring (replace edges)
    eA_list = [(e[0], e[1]) for e in aG.edges_iter()]
    eS_list = []
    for e in G.edges_iter():    # E\A
        if not aG.has_edge(e[0], e[1]):
            eS_list.append((e[0], e[1]))
    nS_edges = len(eS_list)
            
    N_STEPS = int(nA_edges*0.1)
    print "N_STEPS =", N_STEPS
#    for i in range(N_ADR_STEPS):
    for i in range(N_STEPS):    
        idx1 = random.randint(0, nA_edges-1)
        idx2 = random.randint(0, nS_edges-1)
        e1 = eA_list[idx1]
        e2 = eS_list[idx2]
        
        u = e1[0]
        v = e1[1]
        dis_u = len(aG.neighbors(u)) - expected_deg_G[u]
        dis_v = len(aG.neighbors(v)) - expected_deg_G[v]
        x = e2[0]
        y = e2[1]
        dis_x = len(aG.neighbors(x)) - expected_deg_G[x]
        dis_y = len(aG.neighbors(y)) - expected_deg_G[y]
        
        d1 = abs(dis_u-1) + abs(dis_v-1) - (abs(dis_u) + abs(dis_v))
        d2 = abs(dis_x+1) + abs(dis_y+1) - (abs(dis_x) + abs(dis_y))
        if d1 + d2 < 0:
            aG.remove_edge(u,v)
            aG.add_edge(x,y)
            
            # update eA_list, eS_list
            del eA_list[idx1]
            eA_list.append((x, y))
            
            del eS_list[idx2]
            eS_list.append((u, v))
            
    
    #
    return aG


#######################################################
def bipartite(wG, dis):
    
    eBP_list = []       # as a priority queue Q
    
    # sort edges in descending order of probabilities 
    e_list = []
    for e in wG.edges_iter(data=True):
        e_list.append([e[0],e[1],e[2]['p']])    # list, not tuple
    
    e_list = sorted(e_list, key=lambda item: item[2], reverse=True)
          
    #
    while len(e_list) > 0:
        e = e_list[0]
        a = e[0]
        b = e[1]
        
        eBP_list.append((a,b))
        
        # delete all edges incident to b, including e!
        i = 0
        while i < len(e_list):
            if e_list[i][1] == b:
                del e_list[i]
            else:
                i += 1
        #
        dis[a] += 1
        if -1.0 < dis[a] and dis[a] < -0.5:
            i = 0
            while i < len(e_list):
                e = e_list[i]
                if e[0] == a:       # edge (a,x)
                    x = e[1]
                    weight = abs(dis[a]) + 2*abs(dis[x]) - abs(dis[a]+1) - 1
                    if weight > 0:
                        # reorder edge (a,x) in e_list
                        del e_list[i]
                        j = i
                        while j > 0 and e_list[j][2] < weight:
                            j = j-1
                        e_list.insert(j+1, [a,x,weight])
                        
                        i += 1
                    else:
                        del e_list[i]
                        
        elif dis[a] > -0.5:     # discard all edges (a,x)
            i = 0
            while i < len(e_list):
                e = e_list[i]
                if e[0] == a:
                    del e_list[i]
                else:
                    i += 1
            
    #
    return eBP_list

#######################################################
# ABM - approximate b-matching
def abm_representative(G):
    
    aG = nx.Graph()
    aG.add_nodes_from(G.nodes_iter())
    aG.add_edges_from(G.edges_iter())
    
    # compute expected degrees
    expected_deg_G = compute_expected_deg(G)

    deg_list = [0 for i in range(G.number_of_nodes())]
    em_list = []        # Em
    # Phase 1
    round_deg_G = {}
    for (u, deg_u) in expected_deg_G.iteritems():
        round_deg_G[u] = int(round(deg_u))
        
    for e in G.edges_iter():
        u = e[0]
        v = e[1]
        if deg_list[u] < round_deg_G[u] and deg_list[v] < round_deg_G[v]:
            em_list.append((u,v))
            deg_list[u] += 1
            deg_list[v] += 1
            #
            aG.remove_edge(u,v)
    
    # Phase 2
    A = set([])
    B = set([])
    C = set([])
    dis = [0 for i in range(G.number_of_nodes())]
    for u in G.nodes_iter():
        dis[u] = deg_list[u] - expected_deg_G[u]
        if dis[u] <= -0.5:
            A.add(u)
        elif dis[u] < 0:
            B.add(u)
        else:
            C.add(u)

    print "len(A, B, C) =", len(A), len(B), len(C)

    #
    wG = nx.Graph()
    wG.add_nodes_from(list(A))
    wG.add_nodes_from(list(B))
    for e in aG.edges_iter():    # edges(aG) = E\Em
        u = e[0]
        v = e[1]
        weight = abs(dis[u]) + 2*abs(dis[v]) - abs(1-dis[u]) - 1
        if (u in A) and (v in B) and (weight > 0):
            wG.add_edge(u, v, {'p':weight})
            
    #
    eBP_list = bipartite(wG, dis)
    
    #
    aG = nx.Graph()
    aG.add_nodes_from(G.nodes_iter())
    aG.add_edges_from(em_list)
    aG.add_edges_from(eBP_list)

    #
    return aG

#######################################################
if __name__ == '__main__':
    
#    filename = "F:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polbooks/polbooks.gml"
##    filename = "F:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/polblogs/polblogs.gml"     # 2 components
##    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/karate/karate.gml"
##    filename = "C:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/dolphins/dolphins.gml"
#
#
#    G = read_gml_graph(filename)
#    G = normalize_graph(G)
#    print "#nodes =", G.number_of_nodes()
#    print "#edges =", G.number_of_edges()
#    print "#components =", len(nx.connected_components(G))
    
    
#    outfile = "../data/polbooks_uncertain.gr"

    # 1. assign probabilities
##    filename = "../data/er_1000_005.gr"
##    outfile = "../data/er_1000_005_uncertain.gr"
#    filename = "../data/er_10000_0005.gr"
##    outfile = "../data/er_10000_0005_uncertain.gr"            # uniform
#    outfile = "../data/er_10000_0005_uncertain_exp_1.0.gr"      # exponential 
#    
#    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True)
#    assign_probabilities(G, outfile)
#    print "DONE !"
    
    
    # 2.
#    filename = "../data/polbooks_uncertain.gr"
    filename = "../data/er_1000_005_uncertain.gr"
#    filename = "../data/er_10000_0005_uncertain.gr"
#    filename = "../data/er_10000_0005_uncertain_exp_1.0.gr"
    
    
    start = time.clock()
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    print "Read graph - Elapsed ", (time.clock() - start)
    
    # TEST most_probable_representative()
    start = time.clock()
    aG = most_probable_representative(G)
    print "MP - most probable representative:"
    print "#nodes =", aG.number_of_nodes()
    print "#edges =", aG.number_of_edges()
    print "#components =", len(nx.connected_components(aG))
    print "Elapsed ", (time.clock() - start)
    
    d_list_MP = compute_deg_discrepency(G, aG)
    
    # TEST greedy_probability_representative()
    start = time.clock()
    aG = greedy_probability_representative(G)
    print "GP - greedy prabability representative:"
    print "#nodes =", aG.number_of_nodes()
    print "#edges =", aG.number_of_edges()
    print "#components =", len(nx.connected_components(aG))
    print "Elapsed ", (time.clock() - start)
    
    d_list_GP = compute_deg_discrepency(G, aG)
    
    # TEST adr_representative()
    start = time.clock()
    aG = adr_representative(G)
    print "ADR - average degree rewiring representative:"
    print "#nodes =", aG.number_of_nodes()
    print "#edges =", aG.number_of_edges()
    print "#components =", len(nx.connected_components(aG))
    print "Elapsed ", (time.clock() - start)
    
    d_list_ADR = compute_deg_discrepency(G, aG)
    
    # TEST abm_representative()
    start = time.clock()
    aG = abm_representative(G)
    print "ABM - approximate b-matching representative:"
    print "#nodes =", aG.number_of_nodes()
    print "#edges =", aG.number_of_edges()
    print "#components =", len(nx.connected_components(aG))
    print "Elapsed ", (time.clock() - start)
    
    d_list_ABM = compute_deg_discrepency(G, aG)
    
    
    # export to MATLAB
#    scipy.io.savemat('C:/discrepancy_list.mat', dict(d_list_MP=np.array(d_list_MP), d_list_GP=np.array(d_list_GP), d_list_ADR=np.array(d_list_ADR)) )
#    print "Export to MATLAB - DONE!"
    
     