'''
Created on Jan 30, 2014

@author: Nguyen Huu Hiep
'''

import time
from random import *
import networkx as nx
import matplotlib.pyplot as plt
import scipy.io
from numpy import *
import numpy as np
import ROOT

N_GRID_TRIALS = 20000   # only used for accelerate sample_subgraph_2()

#######################################################
def synthetic_graph():
    #    #
#    N_NODES = 100
#    N_NEW_EDGES = 5
#    G = nx.barabasi_albert_graph(N_NODES, N_NEW_EDGES)
#    
#    #
#    nx.write_edgelist(G, "../data/ba-100-5.gr", None, '\t', False)
    
    #
    G = nx.read_edgelist("../data/ba-100-5.gr", '#', '\t', None, nodetype=int)
    
    # subgraph at a given radius
    sG = nx.ego_graph(G, 99, radius=2)
#    print G.neighbors(0)                # equivalent to radius=1
    print sG.nodes()
    print sG.edges()
    
    #
#    pos = nx.spring_layout(G)
#    nx.draw(G, pos)             # networkx draw()
#    plt.show()


#######################################################
def sanitize(G):
    # remove self-loops
    remove_list = []
    for e in G.edges_iter():    
        if e[0] == e[1]:
            remove_list.append((e[0],e[1]))
    for (u,v) in remove_list:
        G.remove_edge(u,v)
        
    # assign weights to edges
    for e in G.edges_iter(): 
        G.edge[e[0]][e[1]]['w'] = 1.0

    #
    return G

#######################################################
def statistics_by_radius(G, radius=1):
    # radius = 1
    edges = {}  # dict[edge] = count
    
    for u in G.nodes():
        for v in G.neighbors(u):
            if (u,v) not in edges:  # radius = 1
                edges[(u,v)] = 1
            else:
                edges[(u,v)] += 1
                
    s = sum(edges.itervalues())
    print "radius=1, #edges =", s
    
    # radius = 2
    edges = {}  # dict[edge] = count
    
    for u in G.nodes():
        for v in G.neighbors(u):
            if (u,v) not in edges:  # radius = 1
                edges[(u,v)] = 1
            else:
                edges[(u,v)] += 1
            for w in G.neighbors(v):  # radius = 2  
                if (v,w) not in edges:
                    edges[(v,w)] = 1
                else:
                    edges[(v,w)] += 1
    # sum of edges{}
    s = sum(edges.itervalues())
    print "radius=2, #edges =", s
    
    # radius = 3
    edges = {}  # dict[edge] = count
    
    for u in G.nodes():
        for v in G.neighbors(u):
            if (u,v) not in edges:  # radius = 1
                edges[(u,v)] = 1
            else:
                edges[(u,v)] += 1
            for w in G.neighbors(v):  # radius = 2  
                if (v,w) not in edges:
                    edges[(v,w)] = 1
                else:
                    edges[(v,w)] += 1
                for t in G.neighbors(v):  # radius = 3  
                    if (w,t) not in edges:
                        edges[(w,t)] = 1
                    else:
                        edges[(w,t)] += 1
    # sum of edges{}
    s = sum(edges.itervalues())
    print "radius=3, #edges =", s
    
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
def find_S1_S2(G, u):
    S1 = set([])
    for v in G.neighbors(u):
        S1.add(v)
        
    S2 = set([])
    for v in G.neighbors(u):
        for w in G.neighbors(v):
            if (w != u) and (not w in S2):
                S2.add(w)
    #
    return S1, S2

#######################################################
# r4_list: set of candidate length-2 connections
def find_r4_list(G, u):
    S1, S2 = find_S1_S2(G, u)
    
#    print "S1 :", S1
#    print "S2 :", S2
    
    r4_list = []
    # u-S2
    for v in S2:
        if not G.has_edge(u,v):
            r4_list.append((u,v))
    # S1-S1
    for v in S1:
        for w in S1:
            if (v < w) and (not G.has_edge(v,w)):
                r4_list.append((v,w))
    # S2-S2
#    for v in G.neighbors(u):
#        e_list = []
#        for w in G.neighbors(v):
#            if w != u:
#                e_list.append(w)
#        for i in range(len(e_list)-1):
#            for j in range(i+1,len(e_list)):
#                w1 = e_list[i]
#                w2 = e_list[j]
#                if not G.has_edge(w1,w2):
#                    r4_list.append((w1,w2))
    #
    return r4_list
    

#######################################################
# exponential mechanism
def sample_subgraph(G, u, eps, k1, k2, k3):
    #
    r1_list, r2_list, r3_list = find_neighbors(G, u)
    n1 = len(r1_list)
    n2 = len(r2_list)
    n3 = len(r3_list)
#    print "len =", n1, n2, n3 
    
    print "u = ", u
    if n1 == 0:
        return []
    ###
#    if n1 + n2 + n3 > 1000:
#        return []
    
    #
    mu = (k1*n1 + k2*n2 + k3*n3)/2
    sigma2 = mu   # 2*sigma^2
    
    func_str = "1/x * exp(-(log(x)/%s+%s)*(log(x)/%s+%s)/%s)"%(eps,mu,eps,mu,sigma2)
#    print "func_str =", func_str
    
    f = ROOT.TF1("my_pdf", func_str, 0, 1)
    N_SIZE = 1000   #10000, 1000
    samples = [f.GetRandom() for _ in range(N_SIZE)]
#    print "generate N_SIZE samples: DONE"
    if max(samples) == 0.0:
        return []
    
    # select a value from samples
    idx = int(random.random()*N_SIZE) 
    val_y = samples[idx]      # Y
    while val_y == 0.0:
        idx = int(random.random()*N_SIZE) 
        val_y = samples[idx]      
    val_x = -log(val_y)/eps     # X = 
#    print "val_y =", val_y, "val_x =", val_x
    
    # convert val to nearest (x1,x2,x3)
    min_dist = 1e10
    cand_list = []  
    
    u1 = min(n1, int(math.floor(val_x/k1)))
    for x1 in range(u1+1):
        u2 = min(n2, int(math.floor((val_x-x1*k1)/k2)))
        for x2 in range(u2+1):
            u3 = min(n3, int(math.floor((val_x-x1*k1-x2*k2)/k3)))
            for x3 in range(u3+1):
                dist = abs(val_x - x1*k1-x2*k2-x3*k3)
                if min_dist > dist:
                    min_dist = dist
                    cand_list = [(x1,x2,x3)]
                elif min_dist == dist:
                    cand_list.append((x1,x2,x3))
#    print "min_dist =", min_dist
#    print "cand_list =", cand_list

    if len(cand_list) == 0:
        return []

    # select a random tuple from cand_list
    idx = int(random.random()*len(cand_list))
    (x1,x2,x3) = cand_list[idx]  
#    print "(x1,x2,x3) =", x1, x2, x3
    
    # sample edges: x1 from n1 (edges to remove from A), x2 from n2, x3 from n3 (edges to add to A)
    A = r1_list
    if x1 > 0:
        p_list = np.random.permutation(n1)
        p_list = p_list[:x1]
        p_list = sorted(p_list, None, None, reverse=True)
        for idx in p_list:
            del A[idx]
    if x2 > 0:
        p_list = np.random.permutation(n2)
        p_list = p_list[:x2]
        for idx in p_list:
            A.append(r2_list[idx])
    if x3 > 0:
        p_list = np.random.permutation(n3)
        p_list = p_list[:x3]
        for idx in p_list:
            A.append(r3_list[idx])

    #
    return A

#######################################################
# exponential mechanism + support r4_list
# differences from sample_subgraph()
# - search (x1,x2,x3,x4): loops in reverse order 
def sample_subgraph_2(G, u, eps, k1, k2, k3, k4):
    #
    r1_list, r2_list, r3_list = find_neighbors(G, u)
    r4_list = find_r4_list(G, u)
    n1 = len(r1_list)
    n2 = len(r2_list)
    n3 = len(r3_list)
    n4 = len(r4_list)
#    print "len =", n1, n2, n3, n4 
    
#    print "u = ", u
    if n1 == 0:
        return []
    ###
#    if n1 + n2 + n3 > 1000:
#        return []
    
    #
    mu = (k1*n1 + k2*n2 + k3*n3 + k4*n4)/2
    sigma2 = mu   # 2*sigma^2
    
    func_str = "1/x * exp(-(log(x)/%s+%s)*(log(x)/%s+%s)/%s)"%(eps,mu,eps,mu,sigma2)
#    print "func_str =", func_str
    
    f = ROOT.TF1("my_pdf", func_str, 0, 1)
    N_SIZE = 10000   #10000, 1000, 100
    samples = [f.GetRandom() for _ in range(N_SIZE)]
#    print "generate N_SIZE samples: DONE"
    if max(samples) == 0.0:
        return []
    
    # select a value from samples
    idx = int(random.random()*N_SIZE) 
    val_y = samples[idx]      # Y
    while val_y == 0.0:
        idx = int(random.random()*N_SIZE) 
        val_y = samples[idx]      
    val_x = -log(val_y)/eps     # X = 
#    print "val_y =", val_y, "val_x =", val_x
    
    # convert val to nearest (x1,x2,x3,x4)
    min_dist = 1e10
    cand_list = []  
    
    # stopped after 100 trials
    count = 0
    stopped = False
    
    u1 = min(n1, int(math.floor(val_x/k1)))
    for x1 in range(u1,-1,-1):
        if stopped: break 
        u2 = min(n2, int(math.floor((val_x-x1*k1)/k2)))
        for x2 in range(u2,-1,-1):
            if stopped: break
            u3 = min(n3, int(math.floor((val_x-x1*k1-x2*k2)/k3)))
            for x3 in range(u3,-1,-1):
                if stopped: break
                u4 = min(n4, int(math.floor((val_x-x1*k1-x2*k2-x3*k3)/k4)))
                for x4 in range(u4,-1,-1):
                    count += 1
                    if count == N_GRID_TRIALS:
                        stopped = True
                    
                    dist = abs(val_x - x1*k1-x2*k2-x3*k3-x4*k4)
                    if min_dist > dist:
                        min_dist = dist
                        cand_list = [(x1,x2,x3,x4)]
                    elif min_dist == dist:
                        cand_list.append((x1,x2,x3,x4))
#    print "min_dist =", min_dist
#    print "cand_list =", cand_list

    if len(cand_list) == 0:
        return []

    # select a random tuple from cand_list
    idx = int(random.random()*len(cand_list))
    (x1,x2,x3,x4) = cand_list[idx]  
#    print "(x1,x2,x3,x4) =", x1, x2, x3, x4
    
    # sample edges: x1 from n1 (edges to remove from A), x2 from n2, x3 from n3, x4 from n4 (edges to add to A)
    A = r1_list
    if x1 > 0:
        p_list = np.random.permutation(n1)
        p_list = p_list[:x1]
        p_list = sorted(p_list, None, None, reverse=True)
        for idx in p_list:
            del A[idx]
            
    if x2 > 0:
        p_list = np.random.permutation(n2)
        p_list = p_list[:x2]
        for idx in p_list:
            A.append(r2_list[idx])
    if x3 > 0:
        p_list = np.random.permutation(n3)
        p_list = p_list[:x3]
        for idx in p_list:
            A.append(r3_list[idx])
    if x4 > 0:
        p_list = np.random.permutation(n4)
        p_list = p_list[:x4]
        for idx in p_list:
            A.append(r4_list[idx])

    #
    return A

#######################################################
def aggregate(G, eps, k1, k2, k3, k4, out_filename):
    
    print "eps, k1,k2,k3,k4 :", eps, k1, k2, k3, k4
    
#    if abs(0.1 - (k1+k2+k3+k4)) > 0.0001:
#        print "Invalid k1, k2, k3, k4 !"
#        return 
    
    edges = {}      # edges[e] = num of occurrences
    count = 0
    error_list = []
    for u in G.nodes_iter():
        count += 1
        if count % 100 == 0:
            print "count =", count
        
#        A = sample_subgraph(G, u, eps, k1, k2, k3)
        A = sample_subgraph_2(G, u, eps, k1, k2, k3, k4)
        if len(A) == 0:
            print "error at node :", u
            error_list.append(u)
            
        for (v,w) in A:
            if (v,w) not in edges:
                edges[(v,w)] = 1
                edges[(w,v)] = 1
            else:
                edges[(v,w)] += 1
                edges[(w,v)] += 1
    #
    print "aggregation DONE !"
    
    # write to file
    f = open(out_filename, 'w')
    for (e, num) in edges.iteritems():
        if e[0] < e[1]:
            f.write("%d\t%d\t%d,"%(e[0],e[1],num))
            f.write("\n")
    f.close()
    print "write to file: DONE"
    print "error_list =", error_list

#######################################################
if __name__ == '__main__':
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-GrQc.txt"         # (3s) 5,242 nodes, 28,980 edges, after sanitization: 5,242 nodes, 14,484 edges
#    file_name = "C:\Tailieu\Paper-code\DATA-SET\SNAP\Collaboration networks\CA-HepPh.txt"         # (90s) 12,008 nodes, 237,010 edges, after sanitization   12008 nodes, 118489
#    G = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string

#    file_name = "C:/Tailieu/Paper-code/DATA-SET/SNAP/Social networks/facebook_combined.txt"     # 4,039 nodes, 88,234 edges
#    G = nx.read_edgelist(file_name, '#', ' ', None, nodetype=int)  

#    data_set = "er_1000_005"     # 1,000 nodes  24,912 edges
    data_set = "er_10000_0005"    # 10,000 nodes  248,793 edges
    
    file_name = "../data/" + data_set + ".gr"
    
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
    print "avg-deg =", sum(list(deg_list.itervalues()))/float(G.number_of_nodes())
    
    #
#    statistics_by_radius(G)

    #
#    G = nx.Graph()
#    G.add_nodes_from([1,2,3,4])
#    G.add_edges_from([(1,2),(1,3),(1,4),(3,4)])
    #
#    scipy.io.savemat('C:/deg_list.mat', dict(node_list=array(list(deg_list.iterkeys())), deg_list=array(list(deg_list.itervalues()))))
    
    
    # TEST find_neighbors
#    deg_list = np.zeros((G.number_of_nodes(), 4))
#    idx = 0
#    for u in G.nodes_iter():
#        r1_list, r2_list, r3_list = find_neighbors(G, u)
#        n1 = len(r1_list)
#        n2 = len(r2_list)
#        n3 = len(r3_list)
#        deg_list[idx][0] = u
#        deg_list[idx][1] = n1
#        deg_list[idx][2] = n2
#        deg_list[idx][3] = n3
#        idx += 1
##        print "len =", n1, n2, n3
#    print "write to file"
#    scipy.io.savemat('C:/deg_list_full_CA-GrQc.mat', dict(deg_list=deg_list))
##    scipy.io.savemat('C:/deg_list_full_facebook.mat', dict(deg_list=deg_list))
                                                  
    # TEST find_r4_list()
##    G = nx.Graph()
###    G.add_edges_from([(1,2),(1,3),(2,4),(2,5),(3,5),(3,6),(1,7),(7,8),(7,9)])
##    G.add_edges_from([(1,2),(1,3),(2,4),(2,5),(3,5),(3,6),(1,7),(7,8),(7,9),()])
#    u = 1
#    r1_list, r2_list, r3_list = find_neighbors(G, u)
#    r4_list = find_r4_list(G, u)
#    print len(r1_list)
#    print len(r2_list)
#    print len(r3_list)
#    print len(r4_list)
    
    
    # TEST sample_subgraph
#    start = time.clock()
##    A = sample_subgraph(G, 1, eps=1.0, k1=0.05, k2=0.03, k3=0.02)
#    A = sample_subgraph_2(G, 1, eps=1.0, k1=0.04, k2=0.03, k3=0.02, k4=0.01)
#    print "len(A) =", len(A)
#    print A
#    print "Elapsed ", (time.clock() - start)
    
    # TEST aggregate
#    aggregate(G, eps=1.0, k1=0.05, k2=0.03, k3=0.02, out_filename='../data/CA-GrQc.out')
#    aggregate(G, eps=1.0, k1=0.05, k2=0.03, k3=0.02, out_filename='../data/er_1000_005_dp.out')

    ### 'er_1000_005.gr'
#    aggregate(G, eps=0.1, k1=0.05, k2=0.03, k3=0.019, k4=0.001, out_filename='../data/' + data_set + '_dp2_0.1.out')
#    aggregate(G, eps=0.1, k1=0.05, k2=0.03, k3=0.01, k4=0.01, out_filename='../data/' + data_set + '_dp2_0.1.out')

#    aggregate(G, eps=1.0, k1=0.05, k2=0.03, k3=0.019, k4=0.001, out_filename='../data/' + data_set + '_dp2_1.0.out')
    aggregate(G, eps=1.0, k1=0.05, k2=0.03, k3=0.01, k4=0.01, out_filename='../data/' + data_set + '_dp2_1.0.out')
#    aggregate(G, eps=1.0, k1=0.025, k2=0.025, k3=0.025, k4=0.025, out_filename='../data/' + data_set + '_dp2_1.0.out')
#    aggregate(G, eps=1.0, k1=0.25, k2=0.25, k3=0.25, k4=0.25, out_filename='../data/' + data_set + '_dp2_1.0.out')
    
#    aggregate(G, eps=10.0, k1=0.05, k2=0.03, k3=0.019, k4=0.001, out_filename='../data/' + data_set + '_dp2_10.0.out')
#    aggregate(G, eps=10.0, k1=0.05, k2=0.03, k3=0.01, k4=0.01, out_filename='../data/' + data_set + '_dp2_10.0.out')

#    aggregate(G, eps=100.0, k1=0.05, k2=0.03, k3=0.019, k4=0.001, out_filename='../data/' + data_set + '_dp2_100.0.out')
#    aggregate(G, eps=100.0, k1=0.05, k2=0.03, k3=0.01, k4=0.01, out_filename='../data/' + data_set + '_dp2_100.0.out')
    
    
    