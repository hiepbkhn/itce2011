'''
Created on Feb 4, 2014

@author: huunguye
implement the paper "Injecting Uncertainty in Graphs for Identity Obfuscation" (VLDB'12)
'''
# CA-GrQc: 3s/try (max_deg = 81)
# CA-HepPh
# facebook: 13.5s/try (do max_deg = 1054), compute_X: 9.7s

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from entropy_obfuscation import compute_X, compute_Y, compute_entropy

#######################################################
N_TRIES = 3


#######################################################
def compute_deg_dict(G, node_list):
    deg_dict = {}
    for u in node_list:
        deg_dict[G.degree(u)] = 1
    #     
    return deg_dict    
        
#######################################################
def compute_X_one_node(max_deg, weight_list):
        
    # use two rows
    if len(weight_list) == 0:      # no neighbors
        X = [0.0 for j in range(max_deg+1)]
        X[0] = 1.0
        return X
    
    #
    X = [0.0 for j in range(max_deg+1)]    
    d = [[0.0 for j in range(max_deg+1)] for l in range(2)]
    # first neighbor (edge)
    p = weight_list[0]    
    d[0][0] = 1.0 - p
    d[0][1] = p
    
    for l in range(2,len(weight_list)+1):
        p = weight_list[l-1]
        d[1][0] = d[0][0]*(1.0-p)     # column j=0  
        for j in range(1,l+1):
            d[1][j] = d[0][j-1]*p + d[0][j]*(1.0-p)
        # copy    
        for j in range(l+1):
            d[0][j] = d[1][j]
            
    #
    for j in range(max_deg+1):
        X[j] = d[0][j]
    #
    return X

#######################################################
# unique[deg]
def uniqueness(sigma, G, max_deg):
    deg_list = nx.degree(G)         # dict[node] = deg
    n_nodes = G.number_of_nodes()
    
    common = [0.0 for i in range(n_nodes)]
    unique = [0.0 for i in range(n_nodes)]
    for i in range(1,max_deg+1):    # i: degree (from 1 -> max_deg)
        s = 0.0
        for deg in deg_list.itervalues():
            d = abs(i-deg)
            s += 1.0/(math.sqrt(2*math.pi)*sigma) * math.exp(-(d*d)/(2*sigma*sigma))
        #
        common[i] = s
        if common[i] > 0:
            unique[i] = 1.0/common[i]
        else:
            unique[i] = 1e10
    #
    return unique


#######################################################
# different from compute_eps() in entropy_obfuscation.py
def compute_eps(G2, k, u, S1, S2):
    max_deg_G2 = max(nx.degree(G2).itervalues())
    print "max_deg_G2 =", max_deg_G2
    deg_list = nx.degree(G2)
    
#    start = time.clock()
    X = compute_X(G2, max_deg_G2)
#    print "compute_X: done"
#    print "Elapsed ", (time.clock() - start
    Y = compute_Y(X, G2.number_of_nodes(), max_deg_G2)
#    print "compute_Y: done"
    ent = compute_entropy(Y, G2.number_of_nodes(), max_deg_G2)
#    print "len(ent) =", len(ent)
    
    num_violated = 0
    LOG2K = math.log(k,2)
    print "LOG2K =", LOG2K
    for (v, deg) in deg_list.iteritems():   # check the original graph
        if (v == u or v in S1) and ent[deg] < LOG2K:
            num_violated += 1
    # check and update eps_min (if ok)
    eps2 = float(num_violated)/G2.number_of_nodes()
    
    #
    return eps2
 

#######################################################
#  
def sample_on_list(L, sum_list):      # binary search
    val = random.random()
#    print "val =", val
    #
    lo = 0
    hi = len(sum_list)
    if val >= sum_list[-1]:
        return L[-1]
    
    while True:
        if lo+1 == hi:
            mid = lo
            break
        mid = (lo + hi)/2
#        print "mid, lo, hi", mid, lo, hi
        
        if sum_list[mid] == val:
            break
        if sum_list[mid] < val:
            lo = mid
        if sum_list[mid] > val:
            hi = mid
    #
    return L[mid] 
    
       


#######################################################
# G: sub graph of u with radius 2
# u, S1, S2: 
def generate_obfuscation(G, u, S1, S2, sigma, k, eps, c, q):
    deg_list = nx.degree(G)         # dict[node] = deg
    n_nodes = G.number_of_nodes()
    max_deg = max(deg_list.itervalues())
    
    #
    unique = uniqueness(sigma, G, max_deg)   # uniqueness for degs
    unique_list = []
    for (v, deg) in deg_list.iteritems():
        unique_list.append((v, unique[deg]))    # tuples (v,unique[deg]) for sorting
        
    # sort unique_list (descending by uniqueness)
    unique_list = sorted(unique_list, key=lambda pair:pair[1], reverse=True)
    
#    print "compute and sort uniqueness: DONE"
    
       
    # compute Q1, Q2 for u+S1 and S2
    Q1 = list(S1)
    Q1.append(u)
    Q2 = list(S2)
    sum_list_1 = [0.0]
    sum_list_2 = [0.0]
    for u in Q1:
        sum_list_1.append(unique[deg_list[u]])
    for u in Q2:
        sum_list_2.append(unique[deg_list[u]])
    # normalize sum_list
    for i in range(1,len(sum_list_1)):
        sum_list_1[i] = sum_list_1[i-1] + sum_list_1[i]
    for i in range(0,len(sum_list_1)):
        sum_list_1[i] = sum_list_1[i]/sum_list_1[-1]  # sum_list_1[-1] is the total
        
    for i in range(1,len(sum_list_2)):
        sum_list_2[i] = sum_list_2[i-1] + sum_list_2[i]
    for i in range(0,len(sum_list_2)):
        sum_list_2[i] = sum_list_2[i]/sum_list_2[-1]
#    print "compute Q1/2, sum_list_1/2: DONE"
    
    eps_min = 1e10     # infinity
    G_min = nx.Graph()
    
    for t in range(N_TRIES):    # try N_TRIES times
        start = time.clock()
#        print "try: t =", t
        
        EC = {}
        E = {}
        for (u,v) in G.edges():
            E[(u,v)] = 1
            E[(v,u)] = 1
            EC[(u,v)] = 1
            EC[(v,u)] = 1
#        print "len(E) =", len(E)
        
        # sample from V_H according to Q
        count = 0
        while len(EC) < c*len(E):         # 
            u = sample_on_list(Q1, sum_list_1)
            v = sample_on_list(Q2, sum_list_2)
            if u == v:
                continue
            if (u,v) in E:
                if (u,v) in EC:
                    del EC[(u,v)]
                    del EC[(v,u)]
            else:
                EC[(u,v)] = 1
                EC[(v,u)] = 1
                
#        print "FINISH: len(EC) =", len(EC)
        
        # re-distribute Uncertainty
        EC_list = []    # keep 1 edge for a pair (u,v),(v,u) in EC
        for (u,v) in EC.iterkeys():
            if u < v:   
                EC_list.append((u,v))
        
        unique_edge_list = []
        unique_edge_sum = 0.0
        for (u,v) in EC_list:
            unique_edge = (unique[deg_list[u]] + unique[deg_list[v]])/2     # formula (7)
            unique_edge_list.append([u,v,unique_edge])
            unique_edge_sum += unique_edge
        #
        for i in range(len(unique_edge_list)):
            unique_edge_list[i][2] = sigma * len(EC)/2 * unique_edge_list[i][2]/unique_edge_sum     # formula (7) 
            
        #
        G2 = nx.Graph()
        G2.add_nodes_from(G.nodes())
#        print "G2.number_of_nodes() =", G2.number_of_nodes() 
            
        # draw r_e for each edge
        for i in range(len(unique_edge_list)):
            val = random.random()
            if val < q:
                r_e = random.random()   # uniform from [0,1]
            else:
                while True:
                    r_e = gauss(0,unique_edge_list[i][2])    #
                    if r_e >= 0.0 and r_e <= 1.0:
                        break
            #
            u = unique_edge_list[i][0]
            v = unique_edge_list[i][1]
            if (u,v) in E:
                G2.add_edge(u, v)
                G2.edge[u][v]['p'] = 1-r_e
            else:
                G2.add_edge(u, v)
                G2.edge[u][v]['p'] = r_e
        
        # count the number of violated nodes 
        eps2 = compute_eps(G2, k, u, S1, S2) 
#        print "eps2 =", eps2
        
        if eps2 <= eps and eps2 < eps_min:
            eps_min = eps2
            G_min = G2
        
        print "Elapsed ", (time.clock() - start)
    # end of N_TRIES
    
    #
    return eps_min, G_min    
        
#######################################################
def aggregate_max(G, sigma, k, eps, c, q, filename):
    edge_dict = {}
    count = 0
    for u in G.nodes_iter():
        if G.degree(u) < 5:
            continue
        print "u =", u
        count += 1
        if count % 10 == 0:
            print "count =", count        
    
        sG, S1, S2 = get_subgraph(G, u)
        (eps_min, sG_min) = generate_obfuscation(sG, u, S1, S2, sigma, k, eps, c, q)
        
        for e in sG_min.edges_iter():
            v = e[0]
            w = e[1]
            if v > w:   # swap to normalize v < w
                v = e[1]
                w = e[0]
            if (v,w) not in edge_dict:
                edge_dict[(v,w)] = sG_min[v][w]['p']
            else:
                if edge_dict[(v,w)] < sG_min[v][w]['p']:
                    edge_dict[(v,w)] = sG_min[v][w]['p']    # max
    #
    aG = nx.Graph()
    for ((v,w),weight) in edge_dict.iteritems():
        aG.add_edge(v, w, {'p':weight})    
    #
    nx.write_edgelist(aG, filename, '#', '\t', data=['p'])
    
    
#######################################################
def sanitize(G):
    # remove self-loops
    remove_list = []
    for e in G.edges_iter():    
        if e[0] == e[1]:
            remove_list.append((e[0],e[1]))
    for (u,v) in remove_list:
        G.remove_edge(u,v)
        
    # remove zero-deg nodes --> NOT NECESSARY
#    remove_list = []
#    for n in G.nodes_iter():
#        if G.degree(n) == 0:
#            remove_list.append(n)
#    for n in remove_list:
#        G.remove_node(n)
    
    # assign weights to edges
    for e in G.edges_iter(): 
        G.edge[e[0]][e[1]]['p'] = 1.0

    #
    return G

#######################################################
def find_neighbors(G, u):
    # compute list of radius-1 edges
    r10_list = []
    for v in G.neighbors(u):
        r10_list.append((u,v))
        
    
    # compute list of radius-1.5 edges
    r15_list = []
    for v in G.neighbors(u):
        for w in G.neighbors(u):
            if (v != w) and (v < w) and G.has_edge(v,w):
                r15_list.append((v,w))
    
    # compute list of radius-2 edges
    r20_list = []
    for v in G.neighbors(u):
        for w in G.neighbors(v):
            if (w != u) and (not G.has_edge(u,w)):
                r20_list.append((v,w))
    #
    return r10_list, r15_list, r20_list

#######################################################
def get_subgraph(G, u):
    sG = nx.Graph()
    S1 = set([])    # nodes at distance 1 to u
    S2 = set([])    # nodes at distance 2 to u
    
    # nodes
#    sG.add_nodes_from(G.nodes_iter())
    
    # edges
    for v in G.neighbors(u):
        sG.add_edge(u,v)
        S1.add(v)
        
    for v in G.neighbors(u):
        for w in G.neighbors(v):
            if (w != u):
                sG.add_edge(v,w)
                if w not in S1:
                    S2.add(w)
    # assign weights to edges
    for e in sG.edges_iter(): 
        sG.edge[e[0]][e[1]]['p'] = 1.0
        
    #
    return sG, S1, S2
    
    

#######################################################
if __name__ == '__main__':
    
#    # init graph
#    N_NODES = 100
#    N_NEW_EDGES = 5
#    G = nx.barabasi_albert_graph(N_NODES, N_NEW_EDGES)
#    # assign probabilities (weights) to edges
#    weights = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
#    n_weight = len(weights)
#    for e in G.edges_iter():
#        G.edge[e[0]][e[1]]['p'] = weights[random.randint(0,n_weight-1)]
#     
#    # write to file
#    nx.write_edgelist(G, "../data/ba-weighted-100-5.gr", None, '\t', True)  # True: write weights to file 
         
    # read from file
#    G = nx.read_edgelist("../data/ba-weighted-100-5.gr", '#', '\t', None, nodetype=int)
         
    # SAMPLE
#    G = nx.Graph()
#    G.add_nodes_from([1,2,3,4])
#    G.add_edges_from([(1,2),(1,3),(1,4),(3,4)])
##    G.add_edges_from([(1,2,{'p':0.7}),(1,3,{'p':0.9}),(1,4,{'p':0.8}),(2,3,{'p':0.8}),(2,4,{'p':0.1}),(3,4,{'p':0.0})])
#    
#    print G.edges()
#    for n in G.nodes_iter():
#        print n 
    
#    print nx.nodes(G)
#    print nx.degree(G)
    
#    X = compute_X(G)
#    Y = compute_Y(X, G.number_of_nodes())
#    ent = compute_entropy(Y, G.number_of_nodes())
#    unique = uniqueness(1.0, G)
#    
#    print "X", X
#    print "Y", Y
#    print "deg entropy", ent
#    print "log_2(3) =", math.log(3,2)
#    print "unique =", unique
    
    # TEST generate_obfuscation()
##    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-GrQc.txt"         # 5,242 nodes, 28,980 edges, after sanitization: 5,242 nodes, 14,484 edges
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-HepPh.txt"         # 12,008 nodes, 237,010 edges, after sanitiz   12008 nodes, 118489
#    G = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
#    file_name = "C:/Tailieu/Paper-code/DATA-SET/SNAP/Social networks/facebook_combined.txt"     # 4,039 nodes, 88,234 edges
#    G = nx.read_edgelist(file_name, '#', ' ', None, nodetype=int)    
    
#    file_name = "../data/er_1000_005.gr"      # 1,000 nodes, 24,912 edges
    file_name = "../data/er_10000_0005.gr"    # 10,000 nodes  248,793 edges
    
    G = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)
    
    G = sanitize(G)
    
    print "#nodes :", G.number_of_nodes()
    print "#edges :", len(G.edges())
    print "#self-loops :", G.number_of_selfloops()
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    print "#components =", nx.number_connected_components(G)
    
    #
#    start = time.clock()
#    print "START compute_X"
#    X = compute_X(G, max_deg)
#    print "Elapsed ", (time.clock() - start)
#    
#    start = time.clock()
#    print "START compute_Y"
#    Y = compute_Y(X, G.number_of_nodes(), max_deg)
#    print "Elapsed ", (time.clock() - start)
#    
#    start = time.clock()
#    print "START compute_entropy"
#    ent = compute_entropy(Y, G.number_of_nodes(), max_deg)
#    print "Elapsed ", (time.clock() - start)
#    
#    start = time.clock()
#    print "START uniqueness"
#    unique = uniqueness(1.0, G, max_deg)
#    print "Elapsed ", (time.clock() - start)
    
    #
#    print "START generate_obfuscation"
#    (eps_min, G_min) = generate_obfuscation(G, sigma=1.0, k=50, eps=0.01, c=2, q=0.01)
#    print "eps_min", eps_min
    
    # TEST compute eps of original graph
#    print "START compute_eps"
#    eps = compute_eps(G, k=50)
#    print "eps", eps
    
    
    
    # TEST get_subgraph
#    print "get_subgraph()"
#    u = 11
#    
#    sG, S1, S2 = get_subgraph(G, u)
#    print "sG.#nodes =", len(sG.nodes())
#    print "sG.#edges =", len(sG.edges())
#    print "len(S1) =", len(S1)
#    print "len(S2) =", len(S2)
#    
#    print "START compute_eps"
#    k=10
#    sigma=0.1
#    eps = compute_eps(sG, k, u, S1, S2)
#    print "eps", eps
#    
#    print "START generate_obfuscation"
#    (eps_min, sG_min) = generate_obfuscation(sG, u, S1, S2, sigma, k=10, eps=0.15, c=2.0, q=0.01)
#    print "eps_min", eps_min
#    nx.write_edgelist(sG_min, '../data/facebook_{0}.out'.format(u), '#', '\t', data=['p'])
    
    
    # TEST aggregate_max
#    G2 = G.subgraph(range(200))
#    
#    print "#nodes :", G2.number_of_nodes()
#    print "#edges :", len(G2.edges())
#    print "#self-loops :", G2.number_of_selfloops()
#    deg_list = nx.degree(G2)         # dict[node] = deg
#    min_deg = min(deg_list.itervalues())
#    max_deg = max(deg_list.itervalues())
#    print "min-deg =", min_deg
#    print "max-deg =", max_deg
#    print "#components =", nx.number_connected_components(G2)
    
    # TEST aggregate_max
#    aggregate_max(G, sigma=0.1, k=10, eps=0.15, c=2.0, q=0.01, filename="../data/facebook.out")
#    aggregate_max(G, sigma=0.1, k=10, eps=0.15, c=2.0, q=0.01, filename="../data/er_1000_005_en_0.1.out")
#    aggregate_max(G, sigma=0.5, k=10, eps=0.15, c=2.0, q=0.01, filename="../data/er_1000_005_en_0.5.out")
#    aggregate_max(G, sigma=1.0, k=10, eps=0.15, c=2.0, q=0.01, filename="../data/er_1000_005_en_1.0.out")

    aggregate_max(G, sigma=0.5, k=10, eps=0.15, c=2.0, q=0.01, filename="../data/er_10000_0005_en_0.5.out")
    
    # TEST compute_X_one_node
#    X = compute_X_one_node(10, [0.9, 0.8, 0.3, 0.7, 0.7])
#    print X
    
    
