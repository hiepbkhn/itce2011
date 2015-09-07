'''
Created on Jun 2, 2014

@author: huunguye

10,000 nodes: 
    - snap.GetAnfEffDiam : 0.095 s
    - nx.diameter :        195.6 s

Apr 8
    - add all_statistics_from_file()
Apr 5, 2015
    - exact_neighborhood_function_statistics(): updated to take into account disconnected (inf distance)
    - cut_query(), compute_utility_and_export_matlab(): added
'''


import sys
import time
import math
from random import *
import math
import igraph as ig
import scipy.io
from numpy import *
import numpy as np
import powerlaw
import snap as sn
from graph_generator_igraph import generate_sample
from randomness_measure import compute_spectral_coordinate

#N_SAMPLES = 100
#N_SAMPLES = 10
N_SAMPLES = 5

N_BFS = 1000

MAX_DEG = 30000
MAX_DIST = 50
K_TOP_EIGEN = 100

APPROX_ANF = 0
APPROX_BFS = 1
APPROX_BFS_IGRAPH = 2   # use igraph

g_list = []     # list of sample graphs

AT_UNCERTAIN = 0
AT_SWITCH = 1
AT_RANDOM_WALK = 2

#######################################################
def convert_networkx_to_SNAP(G):
    
    snap_G = sn.TUNGraph.New()
    for u in G.vs:
        snap_G.AddNode(u.index)
    for e in G.es():
        snap_G.AddEdge(e.source,e.target)
        
    # 
    return snap_G    

#######################################################
# G: deterministic
def degree_statistics_one(G):
    n_nodes = G.vcount()
    
    #####
    # number of edges s_NE
    s_NE = G.ecount()
    
    # average degree s_AD
    s_AD = 2*float(s_NE) /n_nodes
    
    # maximal degree s_MD
    s_MD = G.maxdegree()
    
    # degree variance s_DV
    s_DV = 1.0/n_nodes * sum((d - s_AD)*(d-s_AD) for d in G.degree(G.vs))
    
    # clustering coefficient s_CC
    s_CC = G.transitivity_undirected()

    
    # degree distribution --> HISTOGRAM count
#    deg_list = [0 for i in range(MAX_DEG)]
#    for d in G.degree().itervalues():
#        deg_list[d] += 1
#            
#    i = MAX_DEG-1
#    while deg_list[i] == 0:
#        i = i-1
#    deg_list = deg_list[:i+1]
#    print "len(deg_list) =", len(deg_list)
#    print deg_list

    # degree distribution --> keep it as (multi) SET
    deg_list = G.degree(G.vs)
    
    #
    return s_NE, s_AD, s_MD, s_DV, s_CC, deg_list


#######################################################
# G: uncertain
def degree_statistics(G, n_samples):
    n_nodes = G.vcount()
    
    start = time.clock()
    # list of sampled graphs
    g_list[:] = []
    for i in range(n_samples):
        g_list.append(generate_sample(G))
    print "Sampling graphs - Elapsed ", (time.clock() - start)
    
    #####
    # number of edges s_NE
    s_NE = sum(e['p'] for e in G.es)
    
    # average degree s_AD
    s_AD = 2*s_NE /n_nodes
    
    # maximal degree s_MD
    sum_MD = 0.0
    for aG in g_list:
        max_deg = aG.maxdegree()
        sum_MD += max_deg
        
    s_MD = sum_MD/n_samples
    
    # degree variance s_DV
    sum_DV = 0.0
    for aG in g_list:
        deg_var = 1.0/n_nodes * sum((d - s_AD)*(d-s_AD) for d in aG.degree(aG.vs))
        sum_DV += deg_var
    
    s_DV = sum_DV/n_samples
    
    # clustering coefficient s_CC
    sum_CC = 0.0
    for aG in g_list:
        cc = aG.transitivity_undirected()
        sum_CC += cc
    
    s_CC = sum_CC/n_samples
    
    # degree distribution
    deg_list = [0 for i in range(MAX_DEG)]
    for aG in g_list:
        for d in aG.degree(aG.vs):
            deg_list[d] += 1
            
    i = MAX_DEG-1
    while deg_list[i] == 0:
        i = i-1
    deg_list = deg_list[:i+1]
    print "len(deg_list) =", len(deg_list)
    print deg_list
    
    for i in range(len(deg_list)):
        deg_list[i] = float(deg_list[i])/n_samples
    
    #
    return s_NE, s_AD, s_MD, s_DV, s_CC, deg_list

#######################################################
# G: uncertain
def all_statistics_from_file(n_samples, in_file, sample_file, n_nodes):
    
    s_APD_i = 0
    s_EDiam_i = 0
    s_CL_i = 0
    #
    sum_NE = 0.0
    sum_AD = 0.0
    sum_MD = 0.0
    sum_DV = 0.0
    sum_CC = 0.0
#    deg_list = [0 for i in range(MAX_DEG)]
    deg_list = []   # (multi) SET
    sum_APD = 0.0
    sum_EDiam = 0.0
    sum_CL = 0.0
    sum_APD_i = 0.0
    sum_EDiam_i = 0.0
    sum_CL_i = 0.0
    sum_Diam = 0.0
    count = 0
    for i in range(n_samples):
        print "sample i =", i
        # read sample graph aG
        start = time.clock()
        file_name = sample_file + str(i)  
        aG = ig.Graph.Read_Edgelist(file_name, directed=False)
        
#        n_nodes = aG.vcount()    # n_nodes from input
        if aG.vcount() < n_nodes:                       # IMPORTANT !
            aG.add_vertices(n_nodes - aG.vcount())
        print "read sample - DONE, elapsed :", time.clock() - start
        
        print "n_nodes =", n_nodes
        
        print "s_DD - degree distribution"
        deg_dict = aG.degree(aG.vs)
        HI_DEG = 10
        deg_count = [0 for i in range(HI_DEG+1)]
        for deg in deg_dict:
            if deg <= HI_DEG:
                deg_count[deg] += 1
        print deg_count # 0-10

        # for s_NE, s_AD, s_MD, s_DV, s_CC, deg_list
        start = time.clock()
        sum_NE += aG.ecount()
        s_AD = 2*aG.ecount()/float(n_nodes)      # average degree for this sample graph
        sum_AD += s_AD
        #
        max_deg = aG.maxdegree()
        sum_MD += max_deg
        #
        deg_var = 1.0/n_nodes * sum((d - s_AD)*(d-s_AD) for d in deg_dict)
        sum_DV += deg_var
        print "s_NE, s_AD, s_MD, s_DV - DONE, elapsed :", time.clock() - start
        
        # MOST time-consuming in networkx, very fast in igraph!
        start = time.clock()
        cc = aG.transitivity_undirected()
        sum_CC += cc
        print "s_CC - DONE, elapsed :", time.clock() - start
        
#        # deg_list --> HISTOGRAM count
#        for d in deg_dict:
#            deg_list[d] += 1

        # deg_list --> keep it as (multi) SET
        deg_list.extend(deg_dict)
        
        
        # ANF (for s_APD, s_EDiam, s_CL, s_Diam) 
        print "count =", count
        count += 1
        start = time.clock()
        if n_nodes <= 5000:
            s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(aG, n_nodes)     # use EXACT for small graphs
        else:
            s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i = approx_neighborhood_function_statistics(aG, n_nodes)     # use APPROX for large graphs
            
        sum_APD += s_APD
        sum_EDiam += s_EDiam
        sum_CL += s_CL
        sum_Diam += s_Diam
        sum_APD_i += s_APD_i
        sum_EDiam_i += s_EDiam_i
        sum_CL_i += s_CL_i
        print "ANF - DONE, elapsed :", time.clock() - start
    
    #    
    s_NE = sum_NE/n_samples
    s_AD = sum_AD/n_samples
    s_MD = sum_MD/n_samples
    s_DV = sum_DV/n_samples
    s_CC = sum_CC/n_samples
        
    #        
    s_APD = sum_APD/n_samples
    s_EDiam = sum_EDiam/n_samples
    s_CL = sum_CL/n_samples
    s_Diam = sum_Diam/n_samples
    s_APD_i = sum_APD_i/n_samples
    s_EDiam_i = sum_EDiam_i/n_samples
    s_CL_i = sum_CL_i/n_samples
    
#    i = MAX_DEG-1
#    while deg_list[i] == 0:
#        i = i-1
#    deg_list = deg_list[:i+1]
#    print "len(deg_list) =", len(deg_list)
#    print deg_list
#    
#    for i in range(len(deg_list)):
#        deg_list[i] = float(deg_list[i])/n_samples
    
    #
    return s_NE, s_AD, s_MD, s_DV, s_CC, deg_list, s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i

#######################################################
# deg_list: average from n_samples graphs
def power_law_estimate(deg_list):
    
    data = np.array(deg_list) #data can be list or Numpy array
    results = powerlaw.Fit(data)
    
    return results.power_law.alpha, results.power_law.xmin

#######################################################
def bfs_samples(G):
    perm = np.random.permutation(G.vcount())    # random N_BFS vertices
    v_list = perm[:N_BFS]
    
    length_dict = {}
    for v in v_list:
        path_lengths = G.shortest_paths_dijkstra(v)
        for l in path_lengths[0]:
            if not length_dict.has_key(l):
                length_dict[l] = 1
            else:
                length_dict[l] += 1
    # remove Infinity in length_dict (due to disconnected graphs) !
    for k in length_dict.iterkeys():
        if math.isinf(k):
            del length_dict[k]
            break
    
    #
    s_Diam = max(length_dict.iterkeys())
    s_EDiam_i = 0 
    
    # s_APD_i
    sum_APD = sum(k*v for (k,v) in length_dict.iteritems())
    num_APD = sum(length_dict.itervalues())
    print "num_APD =", num_APD
    s_APD_i = sum_APD/float(num_APD)
    
    # s_EDiam_i
    length_list = list(length_dict.iteritems())
    length_list = sorted(length_list, key=lambda item:item[0])
    print "length_dict =", length_dict
    s = 0
    for i in range(len(length_list)):
        if s > 0.9*num_APD:
            s_EDiam_i = length_list[i][0]
            break
        s += length_list[i][1]
    
    # s_CL_i
    sum_CL = 0.0
    for (k,v) in length_dict.iteritems():
        if k > 0:
            sum_CL += v/float(k)
    s_CL_i = num_APD/sum_CL 
    
    #
    return s_APD_i, s_EDiam_i, s_CL_i, s_Diam

#######################################################
# G: deterministic and LARGE
def approx_neighborhood_function_statistics(G, n_nodes, n_approx=64, approx_type=APPROX_BFS_IGRAPH):

    aG = convert_networkx_to_SNAP(G)
    print "convert to SNAP graph: DONE"

    # TEST (fixed)
#    s_Diam = 20     # youtube

    # diameter s_Diam (lowerbound)
    start = time.clock()
    if approx_type == APPROX_ANF:
        s_Diam = sn.GetAnfEffDiam(aG, False, 0.99, n_approx)
    elif approx_type == APPROX_BFS:
        s_Diam = sn.GetBfsFullDiam(aG, N_BFS, False)         #
    elif approx_type == APPROX_BFS_IGRAPH:
        s_APD_i, s_EDiam_i, s_CL_i, s_Diam = bfs_samples(G)     # _i: igraph
    else:
        print "Wrong <approx_type> !"
    print "compute s_Diam, elapsed :", time.clock() - start

    # average distance s_APD
    DistNbrsV = sn.TIntFltKdV()
    MxDist = int(math.ceil(s_Diam))
    print "MxDist =", MxDist
    
    start = time.clock()
    sn.GetAnf(aG, DistNbrsV, MxDist, False, n_approx) # n_approx=32, 64...
    print "GetAnf, elapsed :", time.clock() - start
#    for item in DistNbrsV:
#        print item.Key(), "-", item.Dat()
    
    sum_APD = 0.0
    dist_list = []      # list of pairs
    for item in DistNbrsV:
        dist_list.append([item.Key(), item.Dat()])
    num_APD = dist_list[-1][1]
        
    # WAY 2 - compute s_EDiam from dist_list
    s_EDiam = 0
    for i in range(len(dist_list)):
        if dist_list[i][1] >= 0.9 * num_APD:
            s_EDiam = dist_list[i][0]
            break
    
    for i in range(len(dist_list)-1, 1, -1):    # do not subtract [0] from [1] !
        dist_list[i][1] = dist_list[i][1] - dist_list[i-1][1]       # compute differences
        sum_APD += dist_list[i][0] * dist_list[i][1] 
    s_APD = sum_APD/num_APD
    print "num_APD =", num_APD

    # for s_PDD
    print "s_PDD :"
    d_list = []
    for item in dist_list:
#        print item[0], "-", item[1]
        d_list.append(item[1])
    print d_list

    # WAY 1 - effective diameter s_EDiam ( SNAP)
#    start = time.clock()
#    if approx_type == APPROX_ANF:
#        s_EDiam = sn.GetAnfEffDiam(aG, False, 0.9, n_approx)       # 90%
#    elif approx_type == APPROX_BFS:
#        s_EDiam = sn.GetBfsEffDiam(aG, 1000, False)
#    else:
#        print "Wrong <approx_type> !"
#    print "compute s_EDiam, elapsed :", time.clock() - start
    
    # connectivity length s_CL
    sum_CL = 0.0
    for item in dist_list:
        if item[0] > 0:
            sum_CL += item[1]/item[0]
#    s_CL = n_nodes*(n_nodes-1)/sum_CL
    s_CL = num_APD/sum_CL 
    
    
    
    #
    return s_APD, float(s_EDiam), s_CL, float(s_Diam), s_APD_i, float(s_EDiam_i), s_CL_i

#######################################################
# G: deterministic, n_nodes <= 20000
def exact_neighborhood_function_statistics(G, n_nodes):

    # all shortest paths
    s_Diam = 0
    s_EDiam = 0.0
    s_CL = 0.0
    
    dist_hist = [0 for _ in range(n_nodes)]
    n_inf = 0           # inf distance (non-connected)
    n_dist = 0
    total_dist = 0.0
    for u in G.vs:
        path_lengths = G.shortest_paths_dijkstra(u)
        del path_lengths[0][u.index]                # remove dist(u,u)
        for d in path_lengths[0]:
            if math.isinf(d):
                n_inf += 1
            else:
                total_dist += d
                n_dist += 1
                dist_hist[d] += 1
                if s_Diam < d:
                    s_Diam = d
                
    
    s_APD = total_dist / n_dist
    
    # connectivity length
    total_CL = 0.0
    for d in range(1,n_nodes):      # d > 0
        total_CL += dist_hist[d]/float(d)
    s_CL = n_dist/total_CL
    
    # effective diameter s_EDiam (0.9-percentile)
    total_d = 0
    d = 0
    while total_d < 0.9*n_dist:
        d += 1
        total_d += dist_hist[d]
    s_EDiam = d
        
    # diameter s_Diam

    
    # betweenness centrality s_BC (normalized)
#    s_BC = nx.betweenness_centrality(G)
    
    #
    return s_APD, s_EDiam, s_CL, s_Diam, dist_hist, n_inf

#######################################################
# G: deterministic
def neighborhood_function_statistics_one(G, n_nodes):

    s_APD_i = 0
    s_EDiam_i = 0
    s_CL_i = 0
    
    if n_nodes <= 20000:    # 5000
        s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(G, n_nodes)     # use EXACT for small graphs
    else:
        s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i = approx_neighborhood_function_statistics(G, n_nodes)     # use APPROX for large graphs
    #
    return s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i

#######################################################
# g_list: already created in degree_statistics()
def neighborhood_function_statistics(n_nodes, n_samples):
    if len(g_list) == 0:
        return 0, 0, 0, 0
    
    sum_APD = 0.0
    sum_EDiam = 0.0
    sum_CL = 0.0
    sum_Diam = 0.0
    count = 0
    for aG in g_list:
        count += 1
        if count % 10 == 0:
            print "count =", count
        if n_nodes <= 5000:
            s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(aG, n_nodes)     # use EXACT for small graphs
        else:
            s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i = approx_neighborhood_function_statistics(aG, n_nodes)     # use APPROX for large graphs
            
        sum_APD += s_APD
        sum_EDiam += s_EDiam
        sum_CL += s_CL
        sum_Diam += s_Diam
    #        
    s_APD = sum_APD/n_samples
    s_EDiam = sum_EDiam/n_samples
    s_CL = sum_CL/n_samples
    s_Diam = sum_Diam/n_samples
    
    #
    return s_APD, s_EDiam, s_CL, s_Diam


#######################################################
# SPECTRAL, COMMUNITY
def spectral_statistics(org_file, file_list):
    
    # original graph
    G = ig.Graph.Read_Edgelist(org_file, directed=False)
    eig_vals, eig_vecs = compute_spectral_coordinate(G, K_TOP_EIGEN)
    
    dist_1 = [np.linalg.norm(eig_vals, 1)]
    dist_2 = [np.linalg.norm(eig_vals)]
    
    # 
    for filename in file_list:
        G = ig.Graph.Read_Edgelist(filename, directed=False)
        eig_vals_1, eig_vecs_1 = compute_spectral_coordinate(G, K_TOP_EIGEN)
        
        dist_1.append(np.linalg.norm(eig_vals_1 - eig_vals, 1))
        dist_2.append(np.linalg.norm(eig_vals_1 - eig_vals))
    
    
    #
#    return eig_vals, eig_vals_1, eig_vals_2, eig_vals_3
    
    #
    return dist_1, dist_2

#######################################################
# return number of edges between S and T
def cut_query(G, S, T):
    c = 0
    for u in S:
        for v in T:
            if G.get_eid(u,v,False, False) != -1:
                c += 1
    #
    return c

#######################################################
def compute_utility_and_export_matlab(graph_file, cut_query_file, matlab_file, n_queries, n_nodes):
    
    start = time.clock()
    G = ig.Graph.Read_Edgelist(graph_file, directed=False)
    G.add_vertices(n_nodes-G.vcount())
    
    print "#nodes =", G.vcount()
    print "#edges =", G.ecount()
    print "read graph - Elapsed ", (time.clock() - start)
    
    # 1. degree metrics
    start = time.clock()
    s_NE, s_AD, s_MD, s_DV, s_CC, deg_list = degree_statistics_one(G)
    print "s_NE =", s_NE
    print "s_AD =", s_AD
    print "s_MD =", s_MD
    print "s_DV =", s_DV
    print "s_CC =", s_CC
    print "degree_statistics - Elapsed ", (time.clock() - start)
    
    # degree distribution
    deg_distr = [0.0 for _ in range(n_nodes)]
    for d in deg_list:
        deg_distr[d] += 1
    for d in range(n_nodes):
        deg_distr[d] = deg_distr[d] / n_nodes
    
        
    # 2. shortest-path metrics
    start = time.clock()
    s_APD, s_EDiam, s_CL, s_Diam, dist_hist, n_inf = exact_neighborhood_function_statistics(G, n_nodes)
    print "s_APD =", s_APD
    print "s_EDiam =", s_EDiam
    print "s_CL =", s_CL
    print "s_Diam =", s_Diam
    print "exact_neighborhood_... - Elapsed ", (time.clock() - start)
    
    # degree distribution
    dist_distr = [0.0 for _ in range(n_nodes+1)]     # dist_distr[n_nodes] contains inf-distance
    num_dist = sum(dist_hist) + n_inf
    for d in range(n_nodes):
        dist_distr[d] = dist_hist[d]/float(num_dist)
    dist_distr[n_nodes] = n_inf/float(num_dist)    
        
    
    # power_law_estimate
    pl_alpha, pl_xmin = power_law_estimate(deg_list)
    print "s_PL =", pl_alpha
    print "pl_xmin =", pl_xmin
    
    # 3. cut queries
    start = time.clock()
    cut_queries = [0 for _ in range(n_queries)]
    
    f = open(cut_query_file, "r")
    fstr = f.read()
    f.close()
    
    lines = fstr.split("\n")
    for i in range(len(lines)/2):
        # S set
        line = lines[2*i]
        items = line.split(",")
        del items[-1]
        S = [int(item) for item in items]
        
        # T set
        line = lines[2*i+1]
        items = line.split(",")
        del items[-1]
        T = [int(item) for item in items]
        
        cut_queries[i] = cut_query(G, S, T)
        
    print "cut queries - Elapsed ", (time.clock() - start)
    
    # write to MATLAB
    scipy.io.savemat(matlab_file, dict(s_AD=s_AD, s_MD=s_MD, s_DV=s_DV, s_CC=s_CC, s_PL=pl_alpha, \
                                       s_APD=s_APD, s_EDiam=s_EDiam, s_CL=s_CL, s_Diam=s_Diam, \
                                       deg_distr=array(deg_distr), dist_distr=array(dist_distr),
                                        cut_queries=array(cut_queries)) )
    

#######################################################
if __name__ == '__main__':
    
    ## original graph
#    filename = "../data/er_1000_001.gr"
#    filename = "../data/ff_1000_05.gr"

#    filename = "../data/a_toy.gr"

    # small graphs
    filename = "../data/polbooks.gr"
#    filename = "../data/polblogs.gr"        # exact_neighborhood_.. 1.6s
#    filename = "../data/as20graph.gr"       # exact_neighborhood_.. 21s
#    filename = "../data/wiki-Vote.gr"
#    filename = "../data/ca-HepPh.gr"
#    filename = "../data/ca-AstroPh.gr"      # exact_neighborhood_.. 326s
    
    # real graphs
#    filename = "../data/com_dblp_ungraph.gr"
#    filename = "../data/com_amazon_ungraph.gr"
#    filename = "../data/com_youtube_ungraph.gr"

    # 10k nodes
#    filename = "../data/er_10000_0001.gr"    
#    filename = "../data/sm_10000_005_11.gr"   
#    filename = "../data/ff_10000_05.gr"
#    filename = "../data/ff_10000_05_connected.gr"

    # 100k nodes
#    filename = "../data/er_100000_00001.gr"
#    filename = "../data/sm_100000_005_11.gr"
#    filename = "../data/ba_100000_5.gr"
#    filename = "../data/pl_100000_5_01.gr"
#    filename = "../data/ff_100000_045.gr"
#    filename = "../data/ff_100000_045_connected.gr"
    
    ## switch (random/nearby)
#    filename = "../switch/ba_100000_5_switch_rand_50000.out"
#    filename = "../switch/ba_100000_5_switch_rand_100000.out"
#    filename = "../switch/ba_100000_5_switch_rand_150000.out"

#    filename = "../switch/com_dblp_ungraph_switch_rand_100000.out"    # random
#    filename = "../switch/com_dblp_ungraph_switch_rand_200000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_rand_300000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_rand_400000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_rand_500000.out" 
    
#    filename = "../switch/com_dblp_ungraph_switch_nb_100000.out"    # nearby
#    filename = "../switch/com_dblp_ungraph_switch_nb_200000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_nb_300000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_nb_400000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_nb_500000.out" 
    
#    filename = "../switch/com_amazon_ungraph_switch_rand_100000.out"    # random
#    filename = "../switch/com_amazon_ungraph_switch_rand_200000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_rand_300000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_rand_400000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_rand_500000.out" 
    
#    filename = "../switch/com_amazon_ungraph_switch_nb_100000.out"    # nearby
#    filename = "../switch/com_amazon_ungraph_switch_nb_200000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_nb_300000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_nb_400000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_nb_500000.out"     
    
#    filename = "../switch/com_youtube_ungraph_switch_rand_300000.out"    # random
#    filename = "../switch/com_youtube_ungraph_switch_rand_600000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_rand_900000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_rand_1200000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_rand_1500000.out"   
#    
#    filename = "../switch/com_youtube_ungraph_switch_nb_300000.out"    # nearby
#    filename = "../switch/com_youtube_ungraph_switch_nb_600000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_nb_900000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_nb_1200000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_nb_1500000.out" 
    
    
    ## entropy-based (obfuscation)
#    filename = "../out/er_1000_001_entropy_01_2_001.out"
#    filename = "../out/er_1000_001_entropy_05_2_001.out"
#    filename = "../out/er_1000_001_entropy_10_2_001.out"
#    filename = "../out/ff_1000_05_entropy_01_2_001.out"    # ff_1000_05
#    filename = "../out/ff_1000_05_entropy_05_2_001.out"
#    filename = "../out/ff_1000_05_entropy_10_2_001.out"

#    filename = "../out/er_10000_0001_entropy_001_2_001.out"    
#    filename = "../out/er_10000_0001_entropy_01_2_001.out"
#    filename = "../out/sm_10000_005_11_entropy_01_2_001.out"

#    filename = "../out/er_100000_00001_entropy_001_2_001.out"    # sigma = 0.01
#    filename = "../out/er_100000_00001_entropy_01_2_001.out"
#    filename = "../out/sm_100000_005_11_entropy_001_2_001.out"  # sigma = 0.01    
#    filename = "../out/sm_100000_005_11_entropy_01_2_001.out"    
#    filename = "../out/ba_100000_5_entropy_001_2_001.out"       # sigma = 0.01    
#    filename = "../out/ba_100000_5_entropy_01_2_001.out"    

    # real graphs
#    filename = "../out/com_dblp_ungraph_entropy_01_2_001.out"
#    filename = "../out/com_dblp_ungraph_entropy_001_2_001.out"   # sigma = 0.01
#    filename = "../out/com_dblp_ungraph_entropy_0001_2_001.out"   # sigma = 0.001       
#    filename = "../out/com_amazon_ungraph_entropy_01_2_001.out"
#    filename = "../out/com_amazon_ungraph_entropy_001_2_001.out"   # sigma = 0.01
#    filename = "../out/com_amazon_ungraph_entropy_0001_2_001.out"   # sigma = 0.001    
#    filename = "../out/com_youtube_ungraph_entropy_01_2_001.out"
#    filename = "../out/com_youtube_ungraph_entropy_001_2_001.out"   # sigma = 0.01
#    filename = "../out/com_youtube_ungraph_entropy_0001_2_001.out"   # sigma = 0.001      
    
    ##            (deterministic)
#    filename = "../out/er_1000_001_entropy_post_01_2_001.out"
#    filename = "../out/er_1000_001_entropy_post_05_2_001.out"
#    filename = "../out/er_1000_001_entropy_post_10_2_001.out"
#    filename = "../out/ff_1000_05_entropy_post_01_2_001.out"    # ff_1000_05
#    filename = "../out/ff_1000_05_entropy_post_05_2_001.out"
#    filename = "../out/ff_1000_05_entropy_post_10_2_001.out"
    
#    filename = "../out/er_10000_0001_entropy_post_01_2_001.out"
#    filename = "../out/er_10000_0001_entropy_post_05_2_001.out"
#    filename = "../out/er_10000_0001_entropy_post_10_2_001.out"
    
    
    ## uncertain convex opt.
#    filename = "../out/er_1000_001_cvxopt_1000.out"
#    filename = "../out/er_1000_001_cvxopt_1000_nb.out"
#    filename = "../out/ff_1000_05_cvxopt_1000.out"        # ff_1000_05
#    filename = "../out/ff_1000_05_cvxopt_1000_nb.out"

#    filename = "../out/er_10000_0001_cvxopt_10000.out"
#    filename = "../out/er_10000_0001_cvxopt_10000_nb_missing.out"   # nearby
#    filename = "../out/er_10000_0001_cvxopt_10000_missing.out"
#    filename = "../out/er_10000_0001_cvxopt_10000_missing.out"      # w/o redistribute missing edges
#    filename = "../out/er_10000_0001_cvxopt_5000_missing.out"
#    filename = "../out/er_10000_0001_cvxopt_10000_10_triad_missing.out"   # triad
#    filename = "../out/er_10000_0001_cvxopt_5000_10_triad_missing.out"   # triad
#    filename = "../out/er_10000_0001_cvxopt_5000_10_triad_blossom_missing.out"   # triad
#    filename = "../out/er_100000_00001_cvxopt_100000_100_triad_missing.out"   # triad

#    filename = "../out/sm_10000_005_11_cvxopt_10000.out"
#    filename = "../out/sm_10000_005_11_cvxopt_10000_missing.out"  
#    filename = "../out/sm_10000_005_11_cvxopt_10000_10_triad_missing.out"       # triads

#    filename = "../out/er_100000_00001_cvxopt_100000_nb_missing.out"    # nearby
#    filename = "../out/er_100000_00001_cvxopt_100000_missing.out"        # random
#    filename = "../out/er_100000_00001_cvxopt_100000_uq_missing.out"    # unique   
#    filename = "../out/er_100000_00001_cvxopt_100000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/er_100000_00001_cvxopt_100000_10_rand_missing.out"  # random, MOSEK
#    filename = "../out/er_100000_00001_cvxopt_100000_10_uq_missing.out"    # unique, MOSEK   
#    filename = "../out/er_100000_00001_cvxopt_50000_nb_missing.out"        # nearby
#    filename = "../out/er_100000_00001_cvxopt_50000_missing.out"        # random
#    filename = "../out/er_100000_00001_cvxopt_50000_uq_missing.out"    # unique

#    filename = "../out/sm_100000_005_11_cvxopt_25000_missing.out"          # random
#    filename = "../out/sm_100000_005_11_cvxopt_50000_missing.out"          # random
#    filename = "../out/sm_100000_005_11_cvxopt_50000_10_missing.out"        # random, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_50000_nb_missing.out"          # nearby
#    filename = "../out/sm_100000_005_11_cvxopt_50000_10_nb_missing.out"        # nearby, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_100000_missing.out"
#    filename = "../out/sm_100000_005_11_cvxopt_100000_10_rand_missing.out"     # random, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_100000_10_nb_missing.out"       # nearby, MOSEK 
#    filename = "../out/sm_100000_005_11_cvxopt_100000_10_uq_missing.out"       # unique, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_100000_nb_missing.out"       # nearby 
#    filename = "../out/sm_100000_005_11_cvxopt_100000_uq_missing.out"       # unique
#    filename = "../out/sm_100000_005_11_cvxopt_100000_200_nb_missing.out"
#    filename = "../out/sm_100000_005_11_cvxopt_200000_200_nb_missing.out"

#    filename = "../out/ba_100000_5_cvxopt_100000_nb_missing.out"    # nearby
#    filename = "../out/ba_100000_5_cvxopt_100000_missing.out"
#    filename = "../out/ba_100000_5_cvxopt_50000_nb_missing.out"        # nearby
#    filename = "../out/ba_100000_5_cvxopt_50000_missing.out"

    # real graphs
#    filename = "../out/com_dblp_ungraph_cvxopt_50000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_100000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_100000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_100000_nb_missing.out"    # nearby            
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_nb_missing.out"    # nearby
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_5_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_missing.out"
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_075_nb_missing.out"    # nearby, sub=0.75 MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_05_nb_missing.out"    # nearby, sub=0.5 MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_025_nb_missing.out"    # nearby, sub=0.25 MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing.out"            # randwalk, first MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing.out"            # randwalk, first MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing.out"            # randwalk, first MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing.out"            # randwalk, first MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing.out"            # randwalk, first MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_rand_missing.out"    # random, MOSEK

#    filename = "../out/com_amazon_ungraph_cvxopt_50000_5_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_50000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_100000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_100000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_200000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_200000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_400000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_600000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_800000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_1000000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_200000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_400000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_600000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_800000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_1000000_20_rand_missing.out"    # random, MOSEK

#    filename = "../out/com_youtube_ungraph_cvxopt_300000_30_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_300000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_420000_30_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_420000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_600_missing.out"   # random
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_60_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_30_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_900000_600_missing.out"   # random
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out"   # nearby, MOSEK

#    filename = "../out/com_youtube_ungraph_cvxopt_600000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1200000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1800000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_2400000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_3000000_60_rand_missing.out"   # random, MOSEK
    
    ##            (deterministic) representative .rep
#    filename = "../out/er_1000_001_cvxopt_rep_1000.out"
#    filename = "../out/er_1000_001_cvxopt_rep_1000_nb.out"
#    filename = "../out/ff_1000_05_cvxopt_rep_1000.out"
#    filename = "../out/ff_1000_05_cvxopt_rep_1000_nb.out"

#    filename = "../out/er_10000_0001_cvxopt_rep_10000.out"
    
    # real graphs
#    filename = "../rep/com_dblp_ungraph_entropy_001_2_001.gp.rep"  # GP representative, 
#    filename = "../rep/com_dblp_ungraph_entropy_001_2_001.adr.rep"  # ADR representative, 
#    filename = "../rep/com_dblp_ungraph_entropy_001_2_001.abm.rep"  # ABM representative

#    filename = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.gp.rep"  # GP representative
#    filename = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.adr.rep"  # ADR representative
#    filename = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.abm.rep"  # ABM representative, 15s+40s
    
    
    ## uncertain linear opt.
#    filename = "../out/er_100000_00001_linopt_50000_avg_inv_min.out"    # random
#    filename = "../out/er_100000_00001_linopt_100000_avg_inv_min.out"    # random
#    filename = "../out/er_100000_00001_linopt_200000_avg_inv_min.out"    # random    
    
#    filename = "../out/sm_100000_005_11_linopt_50000_avg_inv_min.out"     # random
#    filename = "../out/sm_100000_005_11_linopt_100000_avg_inv_min.out"     # random
#    filename = "../out/sm_100000_005_11_linopt_200000_avg_inv_min.out"     # random

#    filename = "../out/ba_100000_5_linopt_50000_avg_inv_min.out"     # random
#    filename = "../out/ba_100000_5_linopt_100000_avg_inv_min.out"     # random
#    filename = "../out/ba_100000_5_linopt_200000_avg_inv_min.out"     # random
#    filename = "../out/ba_100000_5_linopt_300000_avg_inv_min.out"     # random
    
    # real graphs
#    filename = "../out/com_dblp_ungraph_linopt_200000_nb_avg_inv_min.out"    # nearby    
#    filename = "../out/com_dblp_ungraph_linopt_200000_avg_inv_min.out"  # random
#    filename = "../out/com_dblp_ungraph_linopt_400000_nb_avg_inv_min.out"  # nearby
#    filename = "../out/com_dblp_ungraph_linopt_400000_avg_inv_min.out"  # random

#    filename = "../out/com_youtube_ungraph_linopt_500000_nb_avg_inv_min.out"    # nearby
#    filename = "../out/com_youtube_ungraph_linopt_500000_avg_inv_min.out"       # random (solved with MOSEK)        
    
    ## random walk transform (NDSS'13)
#    filename = "../randwalk/sm_100000_005_11_randwalk_2_10.out"       # t=2, M=10

    # real graphs
#    filename = "../randwalk/com_dblp_ungraph_randwalk_2_10.out"       # t=2, M=10  
#    filename = "../randwalk/com_dblp_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_dblp_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_dblp_ungraph_randwalk_10_10.out"       # t=10, M=10

#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.2.out"       # t=2, M=10, p=0.2
#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.4.out"       # t=2, M=10, p=0.4
#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.6.out"       # t=2, M=10, p=0.6
#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.8.out"       # t=2, M=10, p=0.8

#    filename = "../randwalk/com_amazon_ungraph_randwalk_2_10.out"       # t=2, M=10  
#    filename = "../randwalk/com_amazon_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_amazon_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_amazon_ungraph_randwalk_10_10.out"       # t=10, M=10

#    filename = "../randwalk/com_youtube_ungraph_randwalk_2_10.out"       # t=2, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_10_10.out"       # t=10, M=10     
    
    
    ### read graph for DETERMINISTIC G
#    start = time.clock()
#    print "filename =", filename
#    G = ig.Graph.Read_Edgelist(filename, directed=False)
#    print "#nodes =", G.vcount()
#    print "#edges =", G.ecount()
#    
#    deg_list = G.degree(G.vs)         # dict[node] = deg
#    min_deg = min(deg_list)
#    max_deg = max(deg_list)
#    print "min-deg =", min_deg
#    print "max-deg =", max_deg
#    
#    cc_list = G.clusters("weak")
#    print "#components =", len(cc_list)
#    cc_size = [len(c) for c in cc_list]
#    print "cc_size =", cc_size
#
#    print "Read graph - Elapsed ", (time.clock() - start)
    
    
    # TEST generate_sample
#    aG = generate_sample(G)
#    print "#nodes =", aG.vcount()
#    print "#edges =", aG.ecount()


    #### 1 - UNCERTAIN G (IN MEMORY)
#    # TEST degree_statistics
#    MAX_DEG = max_deg + 1       # for real graphs !
#    
#    start = time.clock()
#    s_NE, s_AD, s_MD, s_DV, s_CC, deg_list = degree_statistics(G, n_samples)
#    print "s_NE =", s_NE
#    print "s_AD =", s_AD
#    print "s_MD =", s_MD
#    print "s_DV =", s_DV
#    print "s_CC =", s_CC
#    print "degree_statistics - Elapsed ", (time.clock() - start)
#    
#    # TEST neighborhood_function_statistics()
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = neighborhood_function_statistics(n_nodes=G.vcount(), n_samples)      #1k, 10k, 100k
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "Elapsed ", (time.clock() - start)
#    
#    # TEST power_law_estimate
#    pl_alpha, pl_xmin = power_law_estimate(deg_list)
#    print "s_PL =", pl_alpha
#    print "pl_xmin =", pl_xmin
    
    ###### Command-line param for automation (bash): <filename> <n_samples> <algo_type> <n_nodes>
#    n_samples = 5
#    algo_type = AT_UNCERTAIN
#    
#    if len(sys.argv) > 1:
#        filename = sys.argv[1]
#    if len(sys.argv) > 2:
#        n_samples = int(sys.argv[2])
#        
#
#    print "filename =", filename
#    print "n_samples =", n_samples
#    
#    if filename[-4:] == ".out":
#        #### 2 - UNCERTAIN G (FROM SAMPLE FILES) --> NOTE: change n_samples !!!
#        # TEST all_statistics_from_file
#        if len(sys.argv) > 3:
#            algo_type = int(sys.argv[3])
#        
#        if algo_type == AT_UNCERTAIN:
#            # for UNCERTAIN
#            data_name = filename[7:-4]
#            sample_file = "../sample/" + data_name + "_sample."
#        elif algo_type == AT_SWITCH:
#            # for SWITCH
#            data_name = filename[10:-4]
#            sample_file = "../switch/" + data_name + "_sample."   
#        elif algo_type == AT_RANDOM_WALK:    
#            # for RANDOM WALK
#            data_name = filename[12:-4]
#            sample_file = "../randwalk/" + data_name + "_sample."    
#        else:
#            print "WRONG <algo_type>. Exit..."
#            sys.exit()
#        
#        n_nodes = 0
#        if len(sys.argv) > 4:
#            n_nodes = int(sys.argv[4])
#            
#        #    
#        start = time.clock()
#        
#        s_NE, s_AD, s_MD, s_DV, s_CC, deg_list, s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i = \
#                                                all_statistics_from_file(n_samples, filename, sample_file, n_nodes) 
#        print "s_NE =", s_NE
#        print "s_AD =", s_AD
#        print "s_MD =", s_MD
#        print "s_DV =", s_DV
#        print "s_CC =", s_CC
#        
#        print "s_APD =", s_APD
#        print "s_EDiam =", s_EDiam
#        print "s_CL =", s_CL
#        print "s_Diam =", s_Diam
#        print "----"
#        print "s_APD_i =", s_APD_i
#        print "s_EDiam_i =", s_EDiam_i
#        print "s_CL_i =", s_CL_i
#        print "all_statistics_from_file - Elapsed ", (time.clock() - start)
#        
#        # TEST power_law_estimate
#        pl_alpha, pl_xmin = power_law_estimate(deg_list)
#        print "s_PL =", pl_alpha
#        print "pl_xmin =", pl_xmin
#    
#    elif filename[-3:] == ".gr":
#        #### 3 - DETERMINISTIC G
#        start = time.clock()
#        G = ig.Graph.Read_Edgelist(filename, directed=False)
#        print "#nodes =", G.vcount()
#        print "#edges =", G.ecount()
#        
#        deg_list = G.degree(G.vs)         # dict[node] = deg
#        min_deg = min(deg_list)
#        max_deg = max(deg_list)
#        print "min-deg =", min_deg
#        print "max-deg =", max_deg
#        
#        cc_list = G.clusters("weak")
#        print "#components =", len(cc_list)
#        cc_size = [len(c) for c in cc_list]
#        print "cc_size =", cc_size
#    
#        print "Read graph - Elapsed ", (time.clock() - start)
#        
#        # TEST degree_statistics
#        MAX_DEG = max_deg + 1       # for real graphs !
#        
#        start = time.clock()
#        s_NE, s_AD, s_MD, s_DV, s_CC, deg_list = degree_statistics_one(G)
#        print "s_NE =", s_NE
#        print "s_AD =", s_AD
#        print "s_MD =", s_MD
#        print "s_DV =", s_DV
#        print "s_CC =", s_CC
#        print "degree_statistics - Elapsed ", (time.clock() - start)
#        
#        # TEST neighborhood_function_statistics()
#        start = time.clock()
#        s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i = neighborhood_function_statistics_one(G, n_nodes=G.vcount())  #1k, 10k, 100k
#        print "s_APD =", s_APD
#        print "s_EDiam =", s_EDiam
#        print "s_CL =", s_CL
#        print "s_Diam =", s_Diam
#        print "----"
#        print "s_APD_i =", s_APD_i
#        print "s_EDiam_i =", s_EDiam_i
#        print "s_CL_i =", s_CL_i
#        print "Elapsed ", (time.clock() - start)
#        
#        # TEST power_law_estimate
#        pl_alpha, pl_xmin = power_law_estimate(deg_list)
#        print "s_PL =", pl_alpha
#        print "pl_xmin =", pl_xmin
#    
#    else:
#        ####
#        print "WRONG <filename>. Exit..."
#        sys.exit()
    
    # TEST spectral_statistics()
##    dist_1, dist_2 = spectral_statistics("../data/er_1000_001.gr", ["../out/er_1000_001_entropy_post_01_2_001.out", "../out/er_1000_001_entropy_post_05_2_001.out",
##                                         "../out/er_1000_001_entropy_post_10_2_001.out", "../out/er_1000_001_cvxopt_rep_1000.out", "../out/er_1000_001_cvxopt_rep_1000_nb.out"])
#    dist_1, dist_2 = spectral_statistics("../data/ff_1000_05.gr", ["../out/ff_1000_05_entropy_post_01_2_001.out", "../out/ff_1000_05_entropy_post_05_2_001.out",
#                                         "../out/ff_1000_05_entropy_post_10_2_001.out", "../out/ff_1000_05_cvxopt_rep_1000.out", "../out/ff_1000_05_cvxopt_rep_1000_nb.out"])
#    print "dist_1 =", dist_1
#    print "dist_2 =", dist_2
    
    
    
    ##########################################
    # TEST convert_networkx_to_SNAP
#    G = nx.Graph()
#    G.add_edges_from([(1,2),(2,3),(2,4),(3,4),(4,5)])
#    snap_G = convert_networkx_to_SNAP(G)
#    
#    print "G1: Nodes %d, Edges %d" % (snap_G.GetNodes(), snap_G.GetEdges())
#    for NI in snap_G.Nodes():
#        print "node id %d with out-degree %d and in-degree %d" % (
#            NI.GetId(), NI.GetOutDeg(), NI.GetInDeg())
#    for EI in snap_G.Edges():
#        print "edge (%d, %d)" % (EI.GetSrcNId(), EI.GetDstNId())
    
    
    # TEST exact_neighborhood_function_statistics()
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(G, n_nodes=1000)
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "Elapsed ", (time.clock() - start)
    
    # TEST approx_neighborhood_function_statistics()
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(G, n_nodes=G.vcount(), n_approx=64)        #n_approx=64(default) 
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "Elapsed ", (time.clock() - start)
    
#    print "-------------------------------------"
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(G, n_nodes=G.vcount(), n_approx=64, approx_type=APPROX_BFS)        #n_approx=64,128
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "Elapsed ", (time.clock() - start)
#    
#    print "s_DD - degree distribution"
#    HI_DEG = 10
#    deg_count = [0 for i in range(HI_DEG+1)]
#    for deg in deg_list.itervalues():
#        if deg <= HI_DEG:
#            deg_count[deg] += 1
#    for i in range(HI_DEG+1):
#        print i, " - ", deg_count[i]



    # TEST compute_utility_and_export_matlab()
#    compute_utility_and_export_matlab("../data/polbooks.gr", "../data/polbooks.cut", 
#                                      "../../uncertain-graph-java/_matlab/polbooks.mat", 
#                                      1000, 105)   
#    
#    compute_utility_and_export_matlab("../data/polblogs.gr", "../data/polblogs.cut", 
#                                      "../../uncertain-graph-java/_matlab/polblogs.mat", 
#                                      1000, 1224)   # polblogs: cut_query (21s)
#
#    compute_utility_and_export_matlab("../data/as20graph.gr", "../data/as20graph.cut", 
#                                      "../../uncertain-graph-java/_matlab/as20graph.mat", 
#                                      1000, 6474) 
#
#    compute_utility_and_export_matlab("../data/wiki-Vote.gr", "../data/wiki-Vote.cut", 
#                                      "../../uncertain-graph-java/_matlab/wiki-Vote.mat", 
#                                      1000, 7115) 
#    
#    compute_utility_and_export_matlab("../data/ca-HepPh.gr", "../data/ca-HepPh.cut", 
#                                      "../../uncertain-graph-java/_matlab/ca-HepPh.mat", 
#                                      1000, 12006) 
#    
#    compute_utility_and_export_matlab("../data/ca-AstroPh.gr", "../data/ca-AstroPh.cut", 
#                                      "../../uncertain-graph-java/_matlab/ca-AstroPh.mat", 
#                                      1000, 18771) 
    
    ## Sep 6, 2015
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/polbooks_tmfpart_5_1.0_sample.0", "../data/polbooks.cut", 
#                                      "../../uncertain-graph-java/_matlab/polbooks_tmfpart_5_1.0.0.mat", 
#                                      1000, 105)   
#    
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/polbooks_tmfpart_5_2.0_sample.0", "../data/polbooks.cut", 
#                                      "../../uncertain-graph-java/_matlab/polbooks_tmfpart_5_2.0.0.mat", 
#                                      1000, 105)   
#    
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/polbooks_tmfpart_5_4.0_sample.0", "../data/polbooks.cut", 
#                                      "../../uncertain-graph-java/_matlab/polbooks_tmfpart_5_4.0.0.mat", 
#                                      1000, 105)   
    
    ##
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/as20graph_tmfpart_2.0.0", "../data/as20graph.cut", 
#                                      "../../uncertain-graph-java/_matlab/as20graph_tmfpart_2.0.0.mat", 
#                                      1000, 6474)   
#    
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/as20graph_tmfpart_4.0.0", "../data/as20graph.cut", 
#                                      "../../uncertain-graph-java/_matlab/as20graph_tmfpart_4.0.0.mat", 
#                                      1000, 6474) 
#    
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/as20graph_tmfpart_8.0.0", "../data/as20graph.cut", 
#                                      "../../uncertain-graph-java/_matlab/as20graph_tmfpart_8.0.0.mat", 
#                                      1000, 6474)   

    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/as20graph_tmfpart_10_2.0_sample.0", "../data/as20graph.cut", 
                                      "../../uncertain-graph-java/_matlab/as20graph_tmfpart_10_2.0.0.mat", 
                                      1000, 6474)   
    
    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/as20graph_tmfpart_10_4.0_sample.0", "../data/as20graph.cut", 
                                      "../../uncertain-graph-java/_matlab/as20graph_tmfpart_10_4.0.0.mat", 
                                      1000, 6474) 
    
    compute_utility_and_export_matlab("../../uncertain-graph-java/_sample/as20graph_tmfpart_10_8.0_sample.0", "../data/as20graph.cut", 
                                      "../../uncertain-graph-java/_matlab/as20graph_tmfpart_10_8.0.0.mat", 
                                      1000, 6474)
    
    
    