'''
Created on Feb 12, 2015

copied from "graph-dp/compare/utility_measure_igraph.py"

@author: huunguye

use IGRAPH functions

    Apr 30
    - normalize_graph(): add param is_directed
    
'''


import sys
import time
from random import *
import math
import igraph as ig
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
import snap as sn
from igraph.statistics import power_law_fit

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
# normalize to zero-based indexing of node ids
def normalize_graph(G, is_directed = False):
    newG = nx.Graph()       # undirected
    if is_directed:
        newG = nx.DiGraph()
    
    min_id = min(G.nodes_iter())
    max_id = max(G.nodes_iter())
    print "min_id =", min_id
    print "max_id =", max_id
    
    id_map = {}
    count = 0
    for u in G.nodes_iter():
        id_map[u] = count
        count += 1
    
    for (u,v) in G.edges_iter():
        if u != v:
            newG.add_edge(id_map[u], id_map[v])
        else:
            print "self-loop at ", u
    #
    return newG

#######################################################
def test_normalize_graphs():
#    filename = "../_data/as20graph.txt"     #
#    outfile = "../_data/as20graph.gr"       # no self-loops (6474 nodes, 12572 edges)
#    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)

#    filename = "../_data/polblogs.gml"      # DIRECTED
#    outfile = "../_data/polblogs.gr"
#    filename = "../_data/polbooks.gml"    # 
#    outfile = "../_data/polbooks.gr"
    filename = "../_data/karate.gml"    # 
    outfile = "../_data/karate.gr"
    filename = "../_data/adjnoun.gml"    # 
    outfile = "../_data/adjnoun.gr"
    filename = "../_data/keystone.gml"    # DIRECTED
    outfile = "../_data/keystone.gr"
    G = nx.read_gml(filename)

    print "is_directed =", G.is_directed()
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()

    # normalize G 
    G = normalize_graph(G, is_directed = True)
    
    print "normalize_graph: DONE"
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    print "#components =", nx.number_connected_components(G)

    #
    nx.write_edgelist(G, outfile, "#", '\t', data=False)
    print "write to file - DONE"

#######################################################
# .grph format for Java (with Grph)
def convert_to_grph():
#    filename = "../_data/polbooks.gr"    #
#    filename = "../_data/polblogs.gr"    #  
#    filename = "../_data/as20graph.gr"    #
#    filename = "../_data/wiki-Vote.gr"
#    filename = "../_data/ca-HepPh.gr"
#    filename = "../_data/ca-AstroPh.gr"
    filename = "../_data/sm_50000_005_11.gr"
    filename = "../_data/sm_100000_005_11.gr"
    
    outfile = filename[0:-3] + ".grph"
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    
    f = open(outfile, "w")
    f.write("graph class=grph.in_memory.InMemoryGrph\n")
    f.write("# edges (e = simple edge, a = simple arc, E = hyperedge, A = directed hyperedge)\n")
    e_id = 0
    for e in G.edges_iter():
        f.write("%s %d %d\n"%("e"+str(e_id), e[0], e[1]))
        e_id += 1
        
    f.close()

#######################################################
# directed graphs: polblogs, wiki-Vote
# 
def convert_directed_graph():
#    filename = "../_data/polblogs.gml"      # DIRECTED, 2 components
#    outfile = "../_data/polblogs.gr"
#    filename = "../_data/wiki-Vote.txt"      # DIRECTED, 24 components
#    outfile = "../_data/wiki-Vote.gr"
#    filename = "../_data/ca-HepPh.txt"      # DIRECTED, 276 components
#    outfile = "../_data/ca-HepPh.gr"
    filename = "../_data/ca-AstroPh.txt"      # DIRECTED, 289 components
    outfile = "../_data/ca-AstroPh.gr"
    
#    G = nx.read_gml(filename)
    G = nx.read_edgelist(filename, '#', '\t', create_using=nx.DiGraph(), nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    print "is_directed =", G.is_directed()
    
    newG = nx.Graph()       # undirected
    
    min_id = min(G.nodes_iter())
    max_id = max(G.nodes_iter())
    print "min_id =", min_id
    print "max_id =", max_id
    
    # 1.
    id_map = {}
    count = 0
    for u in G.nodes_iter():
        if G.degree(u) == 0:     # remove orphan nodes
            continue
        u_neighbors = G.neighbors(u);
        for v in u_neighbors:
            if v != u:
                id_map[u] = count
                count += 1
                break;
    
    for (u,v) in G.edges_iter():
        if u != v:
            newG.add_edge(id_map[u], id_map[v])
        else:
            print "self-loop at ", u
            
            
            
    print "after conversion"
    print "#nodes =", newG.number_of_nodes()
    print "#edges =", newG.number_of_edges()     
    min_id = min(newG.nodes_iter())
    max_id = max(newG.nodes_iter())
    print "min_id =", min_id
    print "max_id =", max_id   
    print "#components =", nx.number_connected_components(newG)
    
    #
    nx.write_edgelist(newG, outfile, "#", '\t', data=False)
    print "write to file - DONE"
    

#######################################################
#######################################################
# G: deterministic (igraph)
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
def community_statistics_one(G, n_clusters):
    s_Assort = G.assortativity_degree(directed=False)
    
    dendro = G.community_fastgreedy()
    vertex_cluster = dendro.as_clustering(n_clusters)     # 2 clusters
    s_Q = G.modularity(vertex_cluster)
    
#    s_Bet = G.betweenness()        # list

    (vec, s_Eigen) = G.evcent(directed=False, scale=True, weights=None, return_eigenvalue=True)
    
    #
    return s_Assort, s_Q, s_Eigen
    
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
    
#    print "length_dict =", length_dict
    
    #
    s_Diam = max(length_dict.iterkeys())
    s_EDiam_i = 0 
    
    # s_APD_i
    sum_APD = sum(k*v for (k,v) in length_dict.iteritems())
    num_APD = 0
    for v in length_dict.itervalues():
        num_APD += v
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
    return s_APD, float(s_EDiam), s_CL, float(s_Diam), s_APD_i, float(s_EDiam_i), s_CL_i, dist_list

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
def power_law_estimate(deg_list):
    
    results = power_law_fit(deg_list)       #ig.statistics.FittedPowerLaw
    return results.alpha, results.xmin

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
    if n_nodes < 20000:
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
    
    else:
        s_APD, s_EDiam, s_CL, s_Diam, s_APD_i, s_EDiam_i, s_CL_i, dist_list = approx_neighborhood_function_statistics(G, n_nodes) 
        print "s_APD =", s_APD
        print "s_EDiam =", s_EDiam
        print "s_CL =", s_CL
        print "s_Diam =", s_Diam
        print "----"
        print "s_APD_i =", s_APD_i
        print "s_EDiam_i =", s_EDiam_i
        print "s_CL_i =", s_CL_i
        print "approx_neighborhood_... - Elapsed ", (time.clock() - start)
        
        # degree distribution (from dist_list: list of pairs (d, num) )
        dist_distr = [0.0 for _ in range(100)]
        sum_len = 0.0
        for item in dist_list:
            sum_len += item[1] 
        for item in dist_list:
            dist_distr[item[0]] = item[1]/sum_len    
        
        
        
    
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
    
#    filename = "../_data/as20graph.gr"
#    filename = "../_out/as20graph.1k.gen"
#    filename = "../_out/as20graph.2k.gen"

#    dataname = "polbooks"       # (105, 441)     
#    dataname = "polblogs"       # (1224,16715) 
    dataname = "as20graph"      # (6474,12572)     exact_neighbor...: 14.1s    s_APD = 3.7044, s_EDiam = 5, s_CL = 3.4435, s_Diam = 9
#    dataname = "wiki-Vote"     # (7115,100762)    
    dataname = "ca-HepPh"      # (12006,118489)    exact_neighbor...: 86s     s_APD = 4.6721, s_EDiam = 7, s_CL = 4.9594, s_Diam = 13
#    dataname = "ca-AstroPh"    # (18771,198050)    exact_neighbor...: 241s    s_APD = 4.1937, s_EDiam = 6, s_CL = 4.3063, s_Diam = 14
    dataname = "com_amazon_ungraph" # (334863,925872)
#    dataname = "com_dblp_ungraph"  # (317080,1049866) 
#    dataname = "com_youtube_ungraph"# (1134890,2987624)

    
    filename = "../_data/" + dataname + ".gr"
    
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
    
    #
#    start = time.clock()
#    s_NE, s_AD, s_MD, s_DV, s_CC, deg_list = degree_statistics_one(G)
#    print "s_NE =", s_NE
#    print "s_AD =", s_AD
#    print "s_MD =", s_MD
#    print "s_DV =", s_DV
#    print "s_CC =", s_CC
#    print "degree_statistics - Elapsed ", (time.clock() - start)
    
    # TEST exact_neighborhood_function_statistics()
#    start = time.clock()
#    s_APD, s_EDiam, s_CL, s_Diam = exact_neighborhood_function_statistics(G, G.vcount())     # use EXACT for small graphs
#    print "s_APD =", s_APD
#    print "s_EDiam =", s_EDiam
#    print "s_CL =", s_CL
#    print "s_Diam =", s_Diam
#    print "exact_neighborhood_function_statistics - Elapsed ", (time.clock() - start)

#    pl_alpha, pl_xmin = power_law_estimate(deg_list)
#    print "s_PL =", pl_alpha
#    print "pl_xmin =", pl_xmin

    # community_statistics_one()
#    s_Assort, s_Q, s_Eigen = community_statistics_one(G, n_clusters=2)
#    print "s_Assort =", s_Assort
#    print "s_Q =", s_Q
#    print "s_Eigen =", s_Eigen
    
    
    # TEST test_normalize_graphs()
    test_normalize_graphs()

    # TEST convert_directed_graph()
#    convert_directed_graph() 

    # TEST convert_to_grph()
#    convert_to_grph()
#    print "convert_to_grph - DONE"
    
    
    ## TEST compute_utility_and_export_matlab()
#    start = time.clock()
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_data/com_amazon_ungraph.gr", 
#                                      "../../uncertain-graph-java/_data/com_amazon_ungraph.cut", 
#                                      "../../uncertain-graph-java/_matlab/com_amazon_ungraph.mat", 
#                                      1000, 334863) 
#    print "compute_utility_and_export_matlab (amazon) - elapsed", time.clock() - start
#    
#    start = time.clock()
#    compute_utility_and_export_matlab("../../uncertain-graph-java/_data/com_youtube_ungraph.gr", 
#                                      "../../uncertain-graph-java/_data/com_youtube_ungraph.cut", 
#                                      "../../uncertain-graph-java/_matlab/com_youtube_ungraph.mat", 
#                                      1000, 1134890) 
#    print "compute_utility_and_export_matlab (youtube) - elapsed", time.clock() - start
    
    
    ##### Command-line param for automation (bash): <dataname> <sample_file> <n_samples> <n_nodes>
#    sample_file = ""
#    n_samples = 10
#    if len(sys.argv) > 1:
#        dataname = sys.argv[1]
#    if len(sys.argv) > 2:
#        sample_file = sys.argv[2]
#    if len(sys.argv) > 3:
#        n_samples = int(sys.argv[3])
#    if len(sys.argv) > 4:
#        n_nodes = int(sys.argv[4])
#        
#    print "dataname =", dataname
#    print "sample_file =", sample_file
#    print "n_samples =", n_samples
#    print "n_nodes =", n_nodes
#        
#    for i in range(n_samples):
#        print "sample i =", i
#        start = time.clock()
#        compute_utility_and_export_matlab("../../uncertain-graph/_sample/" + sample_file + "." + str(i), 
#                                          "../../uncertain-graph-java/_data/" + dataname + ".cut", 
#                                          "../../uncertain-graph-java/_matlab/" + sample_file + "." + str(i) + ".mat", 
#                                          1000, n_nodes) 
#        print "compute_utility_and_export_matlab - elapsed", time.clock() - start


    