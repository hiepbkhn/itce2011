'''
Created on Jun 4, 2014

@author: huunguye
- implement the paper "Preserving Link Privacy in Social Network Based Systems" (NDSS'13)
'''

import sys
import time
import random
import math
import igraph as ig
import scipy.io
import scipy.sparse as sp
import numpy as np

N_SAMPLES = 5

DIST_TV = 0
MAX_MIXING_TIME = 200
DELTA_DIST = 0.0001

#######################################################
def random_walk_transform(G, t, M, alpha=1.0):
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    edge_dict = {}  
    edge_list = []
    
    deg_dict = G.degree(G.vs)
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in range(G.vcount()):
        nb_list.append(G.neighbors(u))
        nb_len.append(len(G.neighbors(u)))
    #
    num_failed = 0
    for u in range(G.vcount()):
        count = 1
        for v in G.neighbors(u):
            loop = 1
            z = u
            while (u == z or edge_dict.has_key((u,z))) and (loop <= M):
                # perform (t-1) hop random walk from v
                z = v
                for i in range(t-1):
                    z = nb_list[z][random.randint(0,nb_len[z]-1)]
                loop += 1
            #
            if loop <= M:
                if count == 1:
                    if deg_dict[u] == 1:        # EXCEPTION !
                        val = random.random()
                        if val < 0.5:
                            edge_dict[(u,z)] = 1
                            edge_dict[(z,u)] = 1
                            edge_list.append((u,z))
                    else:
                        val = random.random()   # use alpha
                        if val < alpha:
                            edge_dict[(u,z)] = 1
                            edge_dict[(z,u)] = 1
                            edge_list.append((u,z))
                else:
                    val = random.random()
                    if val < (0.5*deg_dict[u] - alpha)/(deg_dict[u] - 1):   # use alpha
                        edge_dict[(u,z)] = 1
                        edge_dict[(z,u)] = 1
                        edge_list.append((u,z))
            else:
                num_failed += 1
            #
            count += 1
    print "num_failed =", num_failed
    #
    aG.add_edges(edge_list)
    
    return aG

#######################################################
# p: percentage of replaced edges
def random_walk_replace(G, rwG, t, M, p):
    
    aG = ig.Graph()
    aG.add_vertices(G.vcount())
    
    edge_list = []
    
    for e in G.es:
        val = random.random()
        if val >= p:
            edge_list.append((e.source, e.target))
    
    for e in rwG.es:
        val = random.random()
        if val < p:
            edge_list.append((e.source, e.target))

    #
    aG.add_edges(edge_list)
    
    return aG

#######################################################
def distribution_distance(distr_a, distr_b, distance_type):
    
    # Total variation distance
    if distance_type == DIST_TV:
        s = sum(abs(distr_a[i]-distr_b[i]) for i in range(len(distr_a)))
        return 0.5*s

#######################################################
def mixing_time(G, eps):
    
    # compute sparse transition matrix
    node_list = range(G.vcount())
    n_nodes = len(node_list)
    deg_list = G.degree(G.vs)
    
    row = []
    col = []
    data = []
    for u in range(G.vcount()):
        for v in G.neighbors(u):
            row.append(v)   # NOTE: column-sum = 1 (left stochastic)
            col.append(u)
            data.append(1.0/deg_list[u])
    P = sp.coo_matrix( (np.array(data),(np.array(row), np.array(col))), shape=(n_nodes, n_nodes)).tolil()    # lil_matrix()

    #
    stationary_x = []
    n_edges = 2.0*G.ecount()
    for u in node_list:
        stationary_x.append(deg_list[u]/n_edges)
    
    # uniform
    x = [1.0/n_nodes for _ in range(n_nodes)]
    x[0] = 1.0 - float(n_nodes-1)/n_nodes       # assure the sum = 1.0
    
    # Dirac
#    x = [0.0 for _ in range(n_nodes)]
#    x[0] = 1.0

    # convergence test
    m_time = 0
    prev_dd = 1.0
    while True:
        m_time += 1
        x = P.dot(x)
        dd = distribution_distance(stationary_x, x, DIST_TV)
##        print "x =", x, "TV(x) =", dd
#        print "DIST_TV(x) =", dd
    
        if prev_dd - dd < DELTA_DIST or m_time == MAX_MIXING_TIME:
            break
        prev_dd = dd
        
        if dd < eps:
            break
    #
    return m_time, dd


#######################################################
def mixing_time_largest_cc(G, eps):
    
    # remove zero-deg nodes
    deg_list = G.degree(G.vs)
    node_dict = {}
    i = 0
    for u in range(G.vcount()):
        if deg_list[u] > 0:
            node_dict[u] = i
            i += 1
    edge_list = []
    for e in G.es:
        edge_list.append((node_dict[e.source], node_dict[e.target]))
        
    aG = ig.Graph()
    aG.add_vertices(i)
    aG.add_edges(edge_list)
    
    # find largest connected component on aG
    cc_list = aG.clusters("weak")
    print "#components =", len(cc_list)
    max_size = 0
    max_id = 0
    cc_list_len = len(cc_list)
    
    start = time.clock()
    cc_len = [len(cc_list[i]) for i in range(cc_list_len)]
    print "compute cc_len, elapsed", time.clock() - start
    
    for i in range(cc_list_len):
        if max_size < cc_len[i]:
            max_size = cc_len[i]
            max_id = i
    max_cc = cc_list[max_id]
    print "#max_cc =", len(max_cc)
    
    #
    aG = aG.induced_subgraph(max_cc)     # auto re-index vertices to zero-based
    
    #
    m_time, dd = mixing_time(aG, eps)
    
    #
    return m_time, dd

#######################################################
# sparse matrix multiplication
def selfloop_multiedge(G, t):
    
    # compute sparse transition matrix
    node_list = range(G.vcount())
    n_nodes = G.vcount()
    deg_list = G.degree(G.vs)
    print "n_nodes =", n_nodes
    
    row = []
    col = []
    dataA = []
    dataP = []
    for u in range(G.vcount()):
        for v in G.neighbors(u):
            row.append(u)   # NOTE: row-sum = 1 (right stochastic)
            col.append(v)
            dataA.append(1.0)
            dataP.append(1.0/deg_list[u])
    A = sp.coo_matrix( (np.array(dataA),(np.array(row), np.array(col))), shape=(n_nodes, n_nodes)).tolil()    # lil_matrix()
    P = sp.coo_matrix( (np.array(dataP),(np.array(row), np.array(col))), shape=(n_nodes, n_nodes)).tolil()    # lil_matrix()
    
    n_edges = 0.0
    Acoo = A.tocoo()
    for i in range(len(Acoo.row)):
        n_edges += Acoo.data[i]
    print "BEFORE n_edges =", n_edges/2
    #
    AP = A.dot(P)
    for i in range(t-2):
        AP = AP.dot(P)
    
    AP = AP.tocoo()
    # compute the number of selfloops/multiedges
    n_selfloops = 0.0
    n_multiedges = 0.0
    n_edges = 0.0
    for i in range(len(AP.row)):
        u = AP.row[i]
        v = AP.col[i]
        n_edges += AP.data[i]
        if u == v:
            n_selfloops += AP.data[i]
        if u < v and AP.data[i] > 1:
            n_multiedges += math.floor(AP.data[i])
    
    print "n_selfloops =", n_selfloops/2    # because 1 selfloop is equivalent to probabilities 2.0
    print "n_multiedges =", n_multiedges
    print "AFTER n_edges =", n_edges/2


#######################################################
if __name__ == '__main__':
    
    ## 100k 
#    file_name = "er_100000_00001"       #100k nodes
#    file_name = "sm_100000_005_11"       #100k nodes
#    file_name = "ba_100000_5"       #100k nodes

    ## real graphs
#    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  

    #
#    file_name = "../sample/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing_sample.0"
#    file_name ="../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.2_sample.0"
    

    # 
#    G = nx.Graph()
#    G.add_edges_from([(0,1),(0,2),(0,3),(1,2)])


    ######
#    # Command-line param for automation (bash): <file_name> <t> <M> [<p>] <n_samples> <start_id> <alpha>
#    n_samples = 5
#    start_id = 0
#    t = 2
#    M = 10
#    p = 0.8
#    alpha = 1.0
#    file_name = sys.argv[1]
#    t = int(sys.argv[2])
#    M = int(sys.argv[3])
#    if len(sys.argv) == 7:
#        n_samples = int(sys.argv[4])
#        start_id = int(sys.argv[5])
#        alpha = float(sys.argv[6])
#    if len(sys.argv) == 8:
#        p = float(sys.argv[4])
#        n_samples = int(sys.argv[5])
#        start_id = int(sys.argv[6])
#        alpha = float(sys.argv[7])
#        
#
#    print "file_name =", file_name
#    print "n_samples =", n_samples
#    print "start_id =", start_id
#    print "t =", t
#    print "M =", M
#    if len(sys.argv) == 7:
#        print "p =", p
#    
#    #
#    print "file_name =", file_name
#    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=False)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
##    G = ig.Graph.Read_Edgelist(file_name, directed=False)                        # TEST
#    
#    print "#nodes :", G.vcount()
#    print "#edges :", G.ecount()
#    print "#components :", len(G.clusters("weak"))
#    n_nodes = G.vcount()
#    deg_list = G.degree(G.vs)         # dict[node] = deg
#    min_deg = min(deg_list)
#    max_deg = max(deg_list)
#    print "min-deg =", min_deg
#    print "max-deg =", max_deg
#
#    # Command-line param for automation (bash): <file_name> <t> <M> <n_samples> <start_id>
#    if len(sys.argv) == 7:
#        # TEST random_walk_transform()
#        start = time.clock()
#        for i in range(n_samples):
#            print "sample", i+start_id
#            
#            aG = random_walk_transform(G, t, M, alpha)
#            
#            # save uncertain graph for generate_sample (see: graph_generator.py)
#            out_file = "../randwalk/" + file_name + "_randwalk_" + str(t) +"_" + str(M) + "_" + str(alpha) + "_sample." + str(i+start_id)
#            aG.write_edgelist(out_file)
#        
#        print "random_walk_transform for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start
#    
#    # Command-line param for automation (bash): <file_name> <t> <M> <p> <n_samples> <start_id>
#    if len(sys.argv) == 8:
#        # TEST random_walk_replace()
#        start = time.clock()
#        rwG = random_walk_transform(G, t, M, alpha)    # run once
#        
#        for i in range(n_samples):
#            print "sample", i+start_id
#            
#            aG = random_walk_replace(G, rwG, t, M, p)
#            
#            # save uncertain graph for generate_sample (see: graph_generator.py)
#            out_file = "../randwalk/" + file_name + "_randwalk_replace_" + str(t) +"_" + str(M) + "_" + str(p) + "_" + str(alpha) + "_sample." + str(i+start_id)
#            aG.write_edgelist(out_file)
#        
#        print "random_walk_replace for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start


    # TEST mixing_time()
#    print G.vcount()
#    
#    start = time.clock()
#    mt = mixing_time(G, 0.1)
#    print "mixing time =", mt
#    print "elapsed ", time.clock() - start    
#    
#    start = time.clock()
#    mt = mixing_time(G, 0.01)
#    print "mixing time =", mt
#    print "elapsed ", time.clock() - start


    # TEST mixing_time_largest_cc()
#    print "TEST mixing_time_largest_cc"
#    m_time, dd = mixing_time_largest_cc(G, 0.02)
#    print "m_time =", m_time
#    print "dd =", dd
    
    
    # TEST selfloop_multiedge()
    print "TEST selfloop_multiedge"
#    G = ig.Graph()
#    G.add_vertices(4)
#    G.add_edges([(0,1),(0,3),(1,2),(1,3)])

    print "file_name =", file_name
    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=False)
    start = time.clock()
    selfloop_multiedge(G, 2)
    print "elapsed ", time.clock() - start
    
    