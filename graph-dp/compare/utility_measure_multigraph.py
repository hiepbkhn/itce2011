'''
Created on Jul 10, 2014

@author: huunguye

Apr 8
    - add all_statistics_from_file()
'''


import sys
import time
from random import *
import math
import networkx as nx
import igraph as ig
import scipy.io
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

        # 1 - for s_NE, s_AD, s_MD, s_DV, s_CC, deg_list
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
        
        
        # 2 - ANF (for s_APD, s_EDiam, s_CL, s_Diam)
        G = nx.read_edgelist(file_name, '#', '\t', None, nodetype=int)      # NEW : <Graph> for removing multiedges, still have selfloops
        for u in G.nodes_iter():
            if G.has_edge(u, u):    # remove selfloops
                G.remove_edge(u, u)
        aG = ig.Graph()             # convert to aG (igraph)
        aG.add_vertices(n_nodes)
        aG.add_edges(G.edges_iter())
        G = None                    # delete object?
         
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
# G: deterministic
def exact_neighborhood_function_statistics(G, n_nodes):

    # all shortest paths
    dist_list = []
    for u in G.vs:
        path_lengths = G.shortest_paths_dijkstra(u)
        dist_list.extend(path_lengths)
    
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
if __name__ == '__main__':
    
    ###### Command-line param for automation (bash): <filename> <n_samples> <algo_type> <n_nodes>
    n_samples = 5
    algo_type = AT_UNCERTAIN
    
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    if len(sys.argv) > 2:
        n_samples = int(sys.argv[2])
        

    print "filename =", filename
    print "n_samples =", n_samples
    
    if filename[-4:] == ".out":
        #### 2 - UNCERTAIN G (FROM SAMPLE FILES) --> NOTE: change n_samples !!!
        # TEST all_statistics_from_file
        if len(sys.argv) > 3:
            algo_type = int(sys.argv[3])
        
        if algo_type == AT_UNCERTAIN:
            # for UNCERTAIN
            data_name = filename[7:-4]
            sample_file = "../sample/" + data_name + "_sample."
        elif algo_type == AT_SWITCH:
            # for SWITCH
            data_name = filename[10:-4]
            sample_file = "../switch/" + data_name + "_sample."   
        elif algo_type == AT_RANDOM_WALK:    
            # for RANDOM WALK
            data_name = filename[12:-4]
            sample_file = "../randwalk/" + data_name + "_sample."    
        else:
            print "WRONG <algo_type>. Exit..."
            sys.exit()
        
        n_nodes = 0
        if len(sys.argv) > 4:
            n_nodes = int(sys.argv[4])
            
        #    
        start = time.clock()
        
        s_NE, s_AD, s_MD, s_DV, s_CC, deg_list, s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i = \
                                                all_statistics_from_file(n_samples, filename, sample_file, n_nodes) 
        print "s_NE =", s_NE
        print "s_AD =", s_AD
        print "s_MD =", s_MD
        print "s_DV =", s_DV
        print "s_CC =", s_CC
        
        print "s_APD =", s_APD
        print "s_EDiam =", s_EDiam
        print "s_CL =", s_CL
        print "s_Diam =", s_Diam
        print "----"
        print "s_APD_i =", s_APD_i
        print "s_EDiam_i =", s_EDiam_i
        print "s_CL_i =", s_CL_i
        print "all_statistics_from_file - Elapsed ", (time.clock() - start)
        
        # TEST power_law_estimate
        pl_alpha, pl_xmin = power_law_estimate(deg_list)
        print "s_PL =", pl_alpha
        print "pl_xmin =", pl_xmin
    
    else:
        ####
        print "WRONG <filename>. Exit..."
        sys.exit()
    
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
    
    
    