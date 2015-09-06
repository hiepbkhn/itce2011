'''
Created on Apr 28, 2014

@author: huunguye
- implement the paper "Preserving Link Privacy in Social Network Based Systems" (NDSS'13)
'''

import time
import sys
import random
import math
import networkx as nx
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
    aG = nx.Graph()
    
    deg_dict = nx.degree(G)
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in G.nodes_iter():
        nb_list.append(G.neighbors(u))
        nb_len.append(len(G.neighbors(u)))
        
    #
    num_failed = 0
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            loop = 1
            z = u
            while (u == z or aG.has_edge(u, z)) and (loop <= M):
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
                            aG.add_edge(u, z)
                    else:
                        val = random.random()   # use alpha
                        if val < alpha:
                            aG.add_edge(u, z)
                else:
                    val = random.random()
                    if val < (0.5*deg_dict[u] - alpha)/(deg_dict[u] - 1):   # use alpha
                        aG.add_edge(u, z)
            else:
                num_failed += 1
            #
            count += 1
    
    print "num_failed =", num_failed
    #
    return aG

#######################################################
def random_walk_transform_save_MATLAB(G, t, M, mat_file, alpha=1.0):
    aG = nx.Graph()
    
    deg_dict = nx.degree(G)
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in G.nodes_iter():
        nb_list.append(G.neighbors(u))
        nb_len.append(len(G.neighbors(u)))
        
    #
    num_failed = 0
    failed_u_list = []
    failed_v_list = []
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            loop = 1
            z = u
            while (u == z or aG.has_edge(u, z)) and (loop <= M):
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
                            aG.add_edge(u, z)
                    else:
                        val = random.random()   # use alpha
                        if val < alpha:
                            aG.add_edge(u, z)
                else:
                    val = random.random()
                    if val < (0.5*deg_dict[u] - alpha)/(deg_dict[u] - 1):   # use alpha 
                        aG.add_edge(u, z)
            else:
                failed_u_list.append(u)
                failed_v_list.append(v)
                num_failed += 1
            #
            count += 1
    
    print "num_failed =", num_failed
    # export to MATLAB
    scipy.io.savemat(mat_file, dict(failed_u_list=np.array(failed_u_list), failed_v_list=np.array(failed_v_list)) )
    #
    return aG

#######################################################
def random_walk_transform_multigraph(G, t, alpha=1.0):
    aG = nx.MultiGraph()
    
    deg_dict = nx.degree(G)
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in G.nodes_iter():
        nb_list.append(G.neighbors(u))
        nb_len.append(len(G.neighbors(u)))
        
    # WAY-1
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            # perform (t-1) hop random walk from v
            z = v
            for i in range(t-1):
                z = nb_list[z][random.randint(0,nb_len[z]-1)]
            #
            if count == 1:
                if deg_dict[u] == 1:        # EXCEPTION !
                    val = random.random()
                    if val < 0.5:
                        aG.add_edge(u, z)
                else:
                    val = random.random()   # use alpha
                    if val < alpha:
                        aG.add_edge(u, z)
            else:
                val = random.random()
                if val < (0.5*deg_dict[u] - alpha)/(deg_dict[u] - 1):   # use alpha
                    aG.add_edge(u, z)
            #
            count += 1
            
#    # WAY-2
#    potential_edges = []    # list of edges
#    for u in G.nodes_iter():
#        count = 1
#        for v in G.neighbors_iter(u):
#            # perform (t-1) hop random walk from v
#            z = v
#            for i in range(t-1):
#                z = nb_list[z][random.randint(0,nb_len[z]-1)]
#            #
#            if count == 1:
#                if deg_dict[u] == 1:        # EXCEPTION !
#                    val = random.random()
#                    if val < 0.5:
#                        aG.add_edge(u, z)
#                else:
#                    aG.add_edge(u, z)
#            else:
#                potential_edges.append((u,z))
#            #
#            count += 1
#    
#    rand_vals = np.random.random_sample((len(potential_edges),))
#    i = 0
#    for (u,z) in potential_edges:
##        val = random.random()
#        val = rand_vals[i]
#        i +=1
#        if val < (0.5*deg_dict[u] - 1.0)/(deg_dict[u] - 1.0):
#            aG.add_edge(u, z)
    
    
    #
    return aG

#######################################################
# p: percentage of replaced edges
def random_walk_multigraph_replace(G, rwG, t, p):
    
    aG = nx.MultiGraph()
    aG.add_nodes_from(G.nodes_iter())
    
    edge_list = []
    
    for e in G.edges_iter():
        val = random.random()
        if val >= p:
            edge_list.append((e[0], e[1]))
    
    for e in rwG.edges_iter():
        val = random.random()
        if val < p:
            edge_list.append((e[0], e[1]))

    #
    aG.add_edges_from(edge_list)
    
    return aG

#######################################################
# represent aG in uncertain form
def random_walk_transform_multigraph_uncertain(G, t, M, alpha=1.0):
    aG = nx.MultiGraph()
    
    deg_dict = nx.degree(G)
    nb_list = []    # neighbor list: list of lists
    nb_len = []
    for u in G.nodes_iter():
        nb_list.append(G.neighbors(u))
        nb_len.append(len(G.neighbors(u)))
        
    potential_edges = []    # list of edges
    for u in G.nodes_iter():
        count = 1
        for v in G.neighbors_iter(u):
            # perform (t-1) hop random walk from v
            z = v
            for i in range(t-1):
                z = nb_list[z][random.randint(0,nb_len[z]-1)]
            #
            if count == 1:
                if deg_dict[u] == 1:        # EXCEPTION !
                    aG.add_edge(u, z, None, {'p':0.5})
                else:
                    aG.add_edge(u, z, None, {'p':alpha})    # use alpha
            else:
                aG.add_edge(u, z, None, {'p':(0.5*deg_dict[u] - alpha)/(deg_dict[u] - 1.0)})  # use alpha
            #
            count += 1
    
    # sum of edge probabilities
    sum_prob = 0.0
    for e in aG.edges_iter(data=True):
        sum_prob += e[2]['p']
    
    print "sum of edge probs:", sum_prob
    #
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
    node_list = G.nodes()
#    n_nodes = len(node_list)        # error !
    n_nodes = max(node_list) + 1
    deg_list = nx.degree(G)
    row = []
    col = []
    data = []
    for u in node_list:
        for v in G.neighbors(u):
            row.append(v)   # NOTE: column-sum = 1 (left stochastic)
            col.append(u)
            data.append(1.0/deg_list[u])
    P = sp.coo_matrix( (np.array(data),(np.array(row), np.array(col))), shape=(n_nodes, n_nodes)).tolil()    # lil_matrix()

    #
    stationary_x = []
    n_edges = 2.0*G.number_of_edges()
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
#        print "x =", x, "TV(x) =", dd
#        print "DIST_TV(x) =", dd
    
        if prev_dd - dd < DELTA_DIST or m_time == MAX_MIXING_TIME:
            break
        if dd < eps:
            break
    #
    return m_time, dd

#######################################################
def mixing_time_largest_cc(G, eps):
    
    # find largest connected component
    cc_list = nx.connected_components(G)
    print "#components =", len(cc_list)
    max_size = 0
    max_id = 0
    cc_list_len = len(cc_list)
    
#    start = time.clock()
    cc_len = [len(cc_list[i]) for i in range(cc_list_len)]
#    print "compute cc_len, elapsed", time.clock() - start
    
    for i in range(cc_list_len):
#        if i % 100 == 0:
#            print i
            
        if max_size < cc_len[i]:
            max_size = cc_len[i]
            max_id = i
    max_cc = cc_list[max_id]
    print "#max_cc =", len(max_cc), ", max node_id in max_cc=", max(max_cc)
    
    #
    start = time.clock()
    aG = G.subgraph(max_cc)
    
    # convert to zero-based node id
    node_dict = {}
    i = 0
    for u in max_cc:
        node_dict[u] = i
        i += 1
    edge_list = [(node_dict[e[0]], node_dict[e[1]]) for e in aG.edges_iter()]
    aG = nx.Graph()
    aG.add_edges_from(edge_list)
    print "convert largest cc: DONE, elapsed", time.clock() - start
    #
    m_time, dd = mixing_time(aG, eps)
    return m_time, dd


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


    # Command-line param for automation (bash): <file_name> <t> [<p>] <n_samples> <start_id>
    n_samples = 5
    start_id = 0
    t = 2   
    p = 0.8
    alpha = 0.5
    file_name = sys.argv[1]
    t = int(sys.argv[2])    
    if len(sys.argv) == 5:
        n_samples = int(sys.argv[3])
        start_id = int(sys.argv[4])
    if len(sys.argv) == 6:
        p = float(sys.argv[3])
        n_samples = int(sys.argv[4])
        start_id = int(sys.argv[5])
        
    print "file_name =", file_name
    print "n_samples =", n_samples
    print "start_id =", start_id
    print "t =", t
    print "alpha =", alpha
    if len(sys.argv) == 6:
        print "p =", p
        
    G = nx.read_edgelist("../data/" + file_name + ".gr", '#', '\t', None, nodetype=int)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
#    G = nx.read_edgelist(file_name, '#', ' ', None, nodetype=int)
    
    print "#nodes :", G.number_of_nodes()
    print "#edges :", len(G.edges())
    print "#self-loops :", G.number_of_selfloops()
    print "#components :", len(nx.connected_components(G))
    n_nodes = G.number_of_nodes()
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg


    # TEST random_walk_transform()
#    start = time.clock()
#    for i in range(N_SAMPLES):
#        print "sample", i
#        t = 10
#        M = 10
#        aG = random_walk_transform(G, t, M)
#        
#        # save uncertain graph for generate_sample (see: graph_generator.py)
#        out_file = "../randwalk/" + file_name + "_randwalk_" + str(t) +"_" + str(M) + "_sample." + str(i)
#        nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
#    
#    print "random_walk_transform for " + str(N_SAMPLES) +" samples DONE, elapsed :", time.clock() - start


    # TEST mixing_time()
#    print G.number_of_nodes()
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
#    # toy graph
##    G = nx.Graph()
##    G.add_edges_from([(1,3),(3,4),(2,5)])
##    G.add_node(0)
##    print "#nodes :", G.number_of_nodes()
##    print "#edges :", len(G.edges())
#
#    print "TEST mixing_time_largest_cc"
#    m_time, dd = mixing_time_largest_cc(G, 0.02)
#    print "m_time =", m_time
#    print "dd =", dd
#    
#    m_time, dd = mixing_time(G, 0.02)
#    print "m_time =", m_time
#    print "dd =", dd
    
    
    # TEST random_walk_transform_multi_graph()
    print "TEST random_walk_transform_multigraph"
#    t = 2
#    M = 10
#    alpha = 0.5
#    print "t =", t
#    print "M =", M
#    print "alpha =", alpha
#    
##    G = nx.Graph()
##    G.add_edges_from([(0,1),(0,3),(1,2),(1,3)])
#    
##    mat_file = "C:/com_dblp_ungraph_randwalk_failed_t_2.mat"
##    aG = random_walk_transform_save_MATLAB(G, t, M, mat_file)
#    aG = random_walk_transform(G, t, M, alpha)
#    print "#nodes :", aG.number_of_nodes()
#    print "#edges :", len(aG.edges())
#    print "#self-loops :", aG.number_of_selfloops()

    if len(sys.argv) == 5:
        # TEST random_walk_transform_multigraph
        start = time.clock()
        for i in range(n_samples):
            print "sample", i
            
            aG = random_walk_transform_multigraph(G, t, alpha)
            print "#nodes :", aG.number_of_nodes()
            print "#edges :", len(aG.edges())
            print "#self-loops :", aG.number_of_selfloops()
            
            # save uncertain graph for generate_sample (see: graph_generator.py)
            out_file = "../randwalk/" + file_name + "_randwalk_keep_" + str(t) +"_" + str(alpha) + "_sample." + str(i+start_id)
            nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
        
        print "random_walk_transform_multi_graph for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start    

    if len(sys.argv) == 6:
        # TEST random_walk_multigraph_replace
        start = time.clock()
        rwG = random_walk_transform_multigraph(G, t, alpha)    # run once
        
        for i in range(n_samples):
            print "sample", i
            
            aG = random_walk_multigraph_replace(G, rwG, t, p)
            print "#nodes :", aG.number_of_nodes()
            print "#edges :", len(aG.edges())
            print "#self-loops :", aG.number_of_selfloops()
            
            # save uncertain graph for generate_sample (see: graph_generator.py)
            out_file = "../randwalk/" + file_name + "_randwalk_keep_replace" + str(t) +"_" + str(alpha) + "_" + str(p) + "_sample." + str(i+start_id)
            nx.write_edgelist(aG, out_file, '#', '\t', False, 'utf-8')
        
        print "random_walk_multigraph_replace for " + str(n_samples) +" samples DONE, elapsed :", time.clock() - start    
        

    # TEST read MultiGraph and count number of selfloops/multiedges 
#    file_name = "../randwalk/com_dblp_ungraph_randwalk_keep_2_0.5_sample.0"
#    print "file_name =", file_name
#    G = nx.read_edgelist(file_name, '#', '\t', create_using=nx.MultiGraph(), nodetype=int, data=False)      # allow selfloops, multiedges
#    print "#nodes :", G.number_of_nodes()
#    print "#edges :", G.number_of_edges()
#    print "#self-loops :", G.number_of_selfloops()
#    # remove multi edges
#    print "===remove multi edges"
#    aG = nx.Graph()
#    aG.add_edges_from(G.edges_iter())
#    print "#nodes :", aG.number_of_nodes()
#    print "#edges :", aG.number_of_edges()
#    print "#self-loops :", aG.number_of_selfloops()
#    # remove (single) selfloops
#    print "===remove (single) selfloops"
#    for u in aG.nodes_iter():
#        if aG.has_edge(u, u):
#            aG.remove_edge(u, u)
#    print "#nodes :", aG.number_of_nodes()
#    print "#edges :", aG.number_of_edges()
#    print "#self-loops :", aG.number_of_selfloops()

    # TEST read MultiGraph and count number of selfloops/multiedges [igraph]
#    file_name = "../randwalk/com_dblp_ungraph_randwalk_keep_2_0.5_sample.0"
#    print "file_name =", file_name
#    G = ig.Graph.Read_Edgelist(file_name, directed=False)
#    print "#nodes :", G.vcount()
#    print "#edges :", G.ecount()
#    n_selfloops = 0
#    for e in G.es:
#        if e.source == e.target:
#            n_selfloops += 1
#    print "#self-loops :", n_selfloops
    
    
    
    
    
    
    
    
    
    