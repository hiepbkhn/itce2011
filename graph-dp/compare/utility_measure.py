'''
Created on Mar 24, 2014

@author: huunguye

10,000 nodes: 
    - snap.GetAnfEffDiam : 0.095 s
    - nx.diameter :        195.6 s

Apr 8
    - add all_statistics_from_file()
'''



import time
from random import *
import math
import networkx as nx
import scipy.io
import numpy as np
import powerlaw
import snap as sn
from graph_generator import generate_sample
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

g_list = []     # list of sample graphs

#######################################################
def convert_networkx_to_SNAP(G):
    
    snap_G = sn.TUNGraph.New()
    for u in G.nodes_iter():
        snap_G.AddNode(u)
    for e in G.edges_iter():
        snap_G.AddEdge(e[0],e[1])
        
    # 
    return snap_G    

#######################################################
# G: deterministic
def degree_statistics_one(G):
    n_nodes = G.number_of_nodes()
    
    #####
    # number of edges s_NE
    s_NE = G.number_of_edges()
    
    # average degree s_AD
    s_AD = 2*float(s_NE) /n_nodes
    
    # maximal degree s_MD
    s_MD = max(G.degree().itervalues())
    
    # degree variance s_DV
    s_DV = 1.0/n_nodes * sum((d - s_AD)*(d-s_AD) for d in G.degree().itervalues())
    
    # clustering coefficient s_CC
    s_CC = nx.transitivity(G)

    
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
    deg_list = list(G.degree().itervalues())
    
    #
    return s_NE, s_AD, s_MD, s_DV, s_CC, deg_list


#######################################################
# G: uncertain
def degree_statistics(G):
    n_nodes = G.number_of_nodes()
    
    start = time.clock()
    # list of sampled graphs
    g_list[:] = []
    for i in range(N_SAMPLES):
        g_list.append(generate_sample(G))
    print "Sampling graphs - Elapsed ", (time.clock() - start)
    
    #####
    # number of edges s_NE
    s_NE = sum(e[2]['p'] for e in G.edges_iter(data=True))
    
    # average degree s_AD
    s_AD = 2*s_NE /n_nodes
    
    # maximal degree s_MD
    sum_MD = 0.0
    for aG in g_list:
        max_deg = max(aG.degree().itervalues())
        sum_MD += max_deg
        
    s_MD = sum_MD/N_SAMPLES
    
    # degree variance s_DV
    sum_DV = 0.0
    for aG in g_list:
        deg_var = 1.0/n_nodes * sum((d - s_AD)*(d-s_AD) for d in aG.degree().itervalues())
        sum_DV += deg_var
    
    s_DV = sum_DV/N_SAMPLES
    
    # clustering coefficient s_CC
    sum_CC = 0.0
    for aG in g_list:
        cc = nx.transitivity(aG)
        sum_CC += cc
    
    s_CC = sum_CC/N_SAMPLES
    
    # degree distribution
    deg_list = [0 for i in range(MAX_DEG)]
    for aG in g_list:
        for d in aG.degree().itervalues():
            deg_list[d] += 1
            
    i = MAX_DEG-1
    while deg_list[i] == 0:
        i = i-1
    deg_list = deg_list[:i+1]
    print "len(deg_list) =", len(deg_list)
    print deg_list
    
    for i in range(len(deg_list)):
        deg_list[i] = float(deg_list[i])/N_SAMPLES
    
    #
    return s_NE, s_AD, s_MD, s_DV, s_CC, deg_list

#######################################################
# G: uncertain
def all_statistics_from_file(n_samples, in_file, sample_file):
    
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
    sum_Diam = 0.0
    count = 0
    for i in range(n_samples):
        print "sample i =", i
        # read sample graph aG
        start = time.clock()
        file_name = sample_file + str(i)  
        aG = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)
        n_nodes = aG.number_of_nodes()
        print "read sample - DONE, elapsed :", time.clock() - start
        
        print "s_DD - degree distribution"
        deg_dict = nx.degree(aG)
        HI_DEG = 10
        deg_count = [0 for i in range(HI_DEG+1)]
        for deg in deg_dict.itervalues():
            if deg <= HI_DEG:
                deg_count[deg] += 1
        print deg_count # 0-10

        # for s_NE, s_AD, s_MD, s_DV, s_CC, deg_list
        start = time.clock()
        sum_NE += aG.number_of_edges()
        s_AD = 2*aG.number_of_edges()/float(n_nodes)      # average degree for this sample graph
        sum_AD += s_AD
        #
        max_deg = max(aG.degree().itervalues())
        sum_MD += max_deg
        #
        deg_var = 1.0/n_nodes * sum((d - s_AD)*(d-s_AD) for d in aG.degree().itervalues())
        sum_DV += deg_var
        print "s_NE, s_AD, s_MD, s_DV - DONE, elapsed :", time.clock() - start
        
        # MOST time-consuming !
        start = time.clock()
        cc = nx.transitivity(aG)
        sum_CC += cc
        print "s_CC - DONE, elapsed :", time.clock() - start
        
#        # deg_list --> HISTOGRAM count
#        for d in aG.degree().itervalues():
#            deg_list[d] += 1

        # deg_list --> keep it as (multi) SET
        deg_list.extend(aG.degree().itervalues())
        
        
        # ANF (for s_APD, s_EDiam, s_CL, s_Diam) 
        print "count =", count
        count += 1
        start = time.clock()
        if n_nodes <= 5000:
            s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(aG, n_nodes)     # use EXACT for small graphs
        else:
            s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(aG, n_nodes)     # use APPROX for large graphs
            
        sum_APD += s_APD
        sum_EDiam += s_EDiam
        sum_CL += s_CL
        sum_Diam += s_Diam
        print "ANF - DONE, elapsed :", time.clock() - start
    
    #    
    s_NE = sum_NE/N_SAMPLES
    s_AD = sum_AD/N_SAMPLES
    s_MD = sum_MD/N_SAMPLES
    s_DV = sum_DV/N_SAMPLES
    s_CC = sum_CC/N_SAMPLES
        
    #        
    s_APD = sum_APD/N_SAMPLES
    s_EDiam = sum_EDiam/N_SAMPLES
    s_CL = sum_CL/N_SAMPLES
    s_Diam = sum_Diam/N_SAMPLES
    
#    i = MAX_DEG-1
#    while deg_list[i] == 0:
#        i = i-1
#    deg_list = deg_list[:i+1]
#    print "len(deg_list) =", len(deg_list)
#    print deg_list
#    
#    for i in range(len(deg_list)):
#        deg_list[i] = float(deg_list[i])/N_SAMPLES
    
    #
    return s_NE, s_AD, s_MD, s_DV, s_CC, deg_list, s_APD, s_EDiam, s_CL, s_Diam

#######################################################
# deg_list: average from N_SAMPLES graphs
def power_law_estimate(deg_list):
    
    data = np.array(deg_list) #data can be list or Numpy array
    results = powerlaw.Fit(data)
    
    return results.power_law.alpha, results.power_law.xmin
    

#######################################################
# G: deterministic and LARGE
def approx_neighborhood_function_statistics(G, n_nodes, n_approx=64, approx_type=APPROX_BFS):

    aG = convert_networkx_to_SNAP(G)
    print "convert to SNAP graph: DONE"

    # TEST
#    s_Diam = 20     # youtube

    # diameter s_Diam (lowerbound)
    start = time.clock()
    if approx_type == APPROX_ANF:
        s_Diam = sn.GetAnfEffDiam(aG, False, 0.99, n_approx)
    elif approx_type == APPROX_BFS:
        s_Diam = sn.GetBfsFullDiam(aG, N_BFS, False)         #
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
    return s_APD, float(s_EDiam), s_CL, float(s_Diam)

#######################################################
# G: deterministic
def exact_neighborhood_function_statistics(G, n_nodes):

    # all shortest paths
    dist_list = []
    for u in G.nodes_iter():
        path_list = nx.shortest_path(G,source=u)    # dict of paths
        for p in path_list.itervalues():
            dist_list.append(len(p)-1)
    
    # average distance s_APD
    total_dist = sum(dist_list)
    
    s_APD = float(total_dist) / len(dist_list)
    
    # effective diameter s_EDiam
    s_EDiam = np.percentile(np.array(dist_list), 90)
    
    # connectivity length s_CL
    total_CL = 0.0
    for d in dist_list:
        if d > 0:
            total_CL += 1.0/d
    s_CL = n_nodes*(n_nodes-1)/total_CL
        
    # diameter s_Diam
    s_Diam = max(dist_list)
    
    # betweenness centrality s_BC (normalized)
#    s_BC = nx.betweenness_centrality(G)
    
    #
    return s_APD, s_EDiam, s_CL, s_Diam

#######################################################
# G: deterministic
def neighborhood_function_statistics_one(G, n_nodes):

    if n_nodes <= 5000:
        s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(G, n_nodes)     # use EXACT for small graphs
    else:
        s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(G, n_nodes)     # use APPROX for large graphs
    #
    return s_APD, s_EDiam, s_CL, s_Diam

#######################################################
# g_list: already created in degree_statistics()
def neighborhood_function_statistics(n_nodes):
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
            s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(aG, n_nodes)     # use APPROX for large graphs
            
        sum_APD += s_APD
        sum_EDiam += s_EDiam
        sum_CL += s_CL
        sum_Diam += s_Diam
    #        
    s_APD = sum_APD/N_SAMPLES
    s_EDiam = sum_EDiam/N_SAMPLES
    s_CL = sum_CL/N_SAMPLES
    s_Diam = sum_Diam/N_SAMPLES
    
    #
    return s_APD, s_EDiam, s_CL, s_Diam


#######################################################
# SPECTRAL, COMMUNITY
def spectral_statistics(org_file, file_list):
    
    # original graph
    G = nx.read_edgelist(org_file, '#', '\t', None, nodetype=int, data=True)
    eig_vals, eig_vecs = compute_spectral_coordinate(G, K_TOP_EIGEN)
    
    dist_1 = [np.linalg.norm(eig_vals, 1)]
    dist_2 = [np.linalg.norm(eig_vals)]
    
    # 
    for filename in file_list:
        G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True)
        eig_vals_1, eig_vecs_1 = compute_spectral_coordinate(G, K_TOP_EIGEN)
        
        dist_1.append(np.linalg.norm(eig_vals_1 - eig_vals, 1))
        dist_2.append(np.linalg.norm(eig_vals_1 - eig_vals))
    
    
    #
#    return eig_vals, eig_vals_1, eig_vals_2, eig_vals_3
    
    #
    return dist_1, dist_2

#######################################################
if __name__ == '__main__':
    
    ## original graph
#    filename = "../data/er_1000_001.gr"
#    filename = "../data/ff_1000_05.gr"

    # real graphs
#    filename = "../data/com_dblp_ungraph.gr"
#    filename = "../data/com_amazon_ungraph.gr"
    filename = "../data/com_youtube_ungraph.gr"

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

#    filename = "../randwalk/com_amazon_ungraph_randwalk_2_10.out"       # t=2, M=10  
#    filename = "../randwalk/com_amazon_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_amazon_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_amazon_ungraph_randwalk_10_10.out"       # t=10, M=10

#    filename = "../randwalk/com_youtube_ungraph_randwalk_2_10.out"       # t=2, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_10_10.out"       # t=10, M=10     
    
    
    ### read graph for DETERMINISTIC G
    start = time.clock()
    print "filename =", filename
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    
    cc_list = nx.connected_components(G)
    print "#components =", len(cc_list)
    cc_size = [len(c) for c in cc_list]
    print "cc_size =", cc_size

    print "Read graph - Elapsed ", (time.clock() - start)
    
    
    # TEST generate_sample
#    aG = generate_sample(G)
#    print "#nodes =", aG.number_of_nodes()
#    print "#edges =", aG.number_of_edges()


    #### 1 - UNCERTAIN G (IN MEMORY)
#    # TEST degree_statistics
#    MAX_DEG = max_deg + 1       # for real graphs !
#    
#    start = time.clock()
#    s_NE, s_AD, s_MD, s_DV, s_CC, deg_list = degree_statistics(G)
#    print "s_NE =", s_NE
#    print "s_AD =", s_AD
#    print "s_MD =", s_MD
#    print "s_DV =", s_DV
#    print "s_CC =", s_CC
#    print "degree_statistics - Elapsed ", (time.clock() - start)
#    
#    # TEST neighborhood_function_statistics()
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = neighborhood_function_statistics(n_nodes=G.number_of_nodes())      #1k, 10k, 100k
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
    
    #### 2 - UNCERTAIN G (FROM SAMPLE FILES) --> NOTE: change n_samples !!!
#    # TEST all_statistics_from_file
#    print "filename =", filename
#    
#    # for UNCERTAIN
##    data_name = filename[7:-4]
##    sample_file = "../sample/" + data_name + "_sample."
#
#    # for SWITCH
##    data_name = filename[10:-4]
##    sample_file = "../switch/" + data_name + "_sample."   
#    
#    # for RANDOM WALK
#    data_name = filename[12:-4]
#    sample_file = "../randwalk/" + data_name + "_sample."    
#    
#    start = time.clock()
#    n_samples = 5   # n_samples=5, 10
#    print "n_samples =", n_samples
#    
#    s_NE, s_AD, s_MD, s_DV, s_CC, deg_list, s_APD, s_EDiam, s_CL, s_Diam = all_statistics_from_file(n_samples, filename, sample_file) 
#    print "s_NE =", s_NE
#    print "s_AD =", s_AD
#    print "s_MD =", s_MD
#    print "s_DV =", s_DV
#    print "s_CC =", s_CC
#    
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "all_statistics_from_file - Elapsed ", (time.clock() - start)
#    
#    # TEST power_law_estimate
#    pl_alpha, pl_xmin = power_law_estimate(deg_list)
#    print "s_PL =", pl_alpha
#    print "pl_xmin =", pl_xmin
    
    
    #### 3 - DETERMINISTIC G
    # TEST degree_statistics
    MAX_DEG = max_deg + 1       # for real graphs !
    
    start = time.clock()
    s_NE, s_AD, s_MD, s_DV, s_CC, deg_list = degree_statistics_one(G)
    print "s_NE =", s_NE
    print "s_AD =", s_AD
    print "s_MD =", s_MD
    print "s_DV =", s_DV
    print "s_CC =", s_CC
    print "degree_statistics - Elapsed ", (time.clock() - start)
    
    # TEST neighborhood_function_statistics()
    start = time.clock()
    s_APD, s_EDiam, s_CL, s_Diam = neighborhood_function_statistics_one(G, n_nodes=G.number_of_nodes())  #1k, 10k, 100k
    print "s_APD =", s_APD
    print "s_EDiam =", s_EDiam
    print "s_CL =", s_CL
    print "s_Diam =", s_Diam
    print "Elapsed ", (time.clock() - start)
    
    # TEST power_law_estimate
    pl_alpha, pl_xmin = power_law_estimate(deg_list)
    print "s_PL =", pl_alpha
    print "pl_xmin =", pl_xmin
    
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
#    s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(G, n_nodes=G.number_of_nodes(), n_approx=64)        #n_approx=64(default) 
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "Elapsed ", (time.clock() - start)
    
#    print "-------------------------------------"
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = approx_neighborhood_function_statistics(G, n_nodes=G.number_of_nodes(), n_approx=64, approx_type=APPROX_BFS)        #n_approx=64,128
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
    
    
    