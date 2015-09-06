'''
Created on Jul 29, 2014

@author: huunguye
    - compute row-wise total variance of AP_rw^{t-1}
'''

import sys
import psutil
import time
import random
import math
import igraph as ig
import scipy.io
import scipy.sparse as sp
import numpy as np
from graph_generator import generate_BA_graph


#######################################################
# start_node -> start_node+num_node-1: rows whose variance are computed
# T : time steps
def randwalk_variance(G, T, start_node, num_node):
    deg_list = G.degree(G.vs)         # dict[node] = deg
    
    
    row_var = [[0.0 for i in range(num_node)] for _ in range(T+1)]   # row_var[t][i] , u = start_node+i
    row_var2 = [[0.0 for i in range(num_node)] for _ in range(T+1)]   # row_var[t][i] , u = start_node+i
     
    non_zero_list = [0 for _ in range(T+1)]
    selfloop_list = [0.0 for _ in range(T+1)]
    multiedge_list = [0.0 for _ in range(T+1)]
    
    
    row_dict = []   # list of dicts
    for i in range(num_node):
        u = start_node + i
        v_dict= {}
        for v in G.neighbors(u):
            v_dict[v] = 1           # (v,p) = (v,1)
        row_dict.append(v_dict)
        
    start = time.clock()
    for t in range(2,T+1):
        n_non_zero = 0      # number of non-zero cells in array [start_node, start_node+num_node]x[0 -> n_nodes-1]
        n_selfloop = 0.0
        n_multiedge = 0.0
        for i in range(num_node):
            u = start_node + i
            v_dict = row_dict[i]    # v_dict of node u
            new_v_dict = {}
            for v in v_dict.iterkeys():
                for w in G.neighbors(v):
                    if not new_v_dict.has_key(w):
                        new_v_dict[w] = v_dict[v]*1.0/deg_list[v]
                    else:
                        new_v_dict[w] += v_dict[v]*1.0/deg_list[v]
                        
            # update
            row_dict[i] = new_v_dict
            
            # compute row_var[t][u]
            for (v,p) in row_dict[i].iteritems():
                row_var[t][i] += p*(1-p)
                
                p2 = p - math.floor(p)              # only get the fraction
                row_var2[t][i] += p2*(1-p2)
                
                if v != u:                # multi-edge
                    n_multiedge += math.floor(2*p)  # ERROR here, must be floor(2*p) then divide by 2 !

            if row_dict[i].has_key(u):              # self-loop
                n_selfloop += row_dict[i][u]     
                
            #
            n_non_zero += len(row_dict[i])
        
        non_zero_list[t] = n_non_zero
        selfloop_list[t] = n_selfloop
        multiedge_list[t] = n_multiedge
        
        print "t =", t, "n_non_zero =", n_non_zero, " elapsed", time.clock() - start
                
    #
    return row_var, row_var2, non_zero_list, selfloop_list, multiedge_list
    
    
#######################################################
def export_test_graph_to_MATLAB(file_name):

    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=False)
    A = np.zeros((G.vcount(),G.vcount()))
    print A.shape
    for e in G.es:
        A[e.source][e.target] = 1
        A[e.target][e.source] = 1
    
    # export to MATLAB
    scipy.io.savemat("D:/" + file_name + ".mat", dict(A=A))
     
    
    
#######################################################    
if __name__ == '__main__':
    ## 100k 
#    file_name = "er_100000_00001"       #100k nodes
#    file_name = "sm_100000_005_11"       #100k nodes
    file_name = "ba_1000_5"             #1k nodes
#    file_name = "ba_10000_3"           #10k nodes
#    file_name = "ba_100000_3"           #100k nodes

    ## real graphs
#    file_name = "com_dblp_ungraph"   # 300k nodes, 1M edges
#    file_name = "com_amazon_ungraph"   # 320k nodes, 0.92M edges
#    file_name = "com_youtube_ungraph"   # 1M nodes, 3M edges  


    ######
    # Command-line param for automation (bash): <file_name> <T> <start_node> <end_node> <num_node>
    T = 10
    start_node = 0
    end_node = 1000
    num_node = 100
    file_prefix = ""
    
    if len(sys.argv) > 5:
        file_name = sys.argv[1]
        T = int(sys.argv[2])
        start_node = int(sys.argv[3])
        end_node = int(sys.argv[4])
        num_node = int(sys.argv[5])
    if len(sys.argv) > 6:
        file_prefix = sys.argv[6]
    

#    print "generate synthetic graph(s)"
#    generate_BA_graph(100000, 3, "../data/ba_100000_3.gr")    

    #
    print "file_name =", file_name
    print "T =", T
    print "start_node =", start_node
    print "end_node =", end_node
    print "num_node =", num_node
    
    G = ig.Graph.Read_Edgelist("../data/" + file_name + ".gr", directed=False)      # implicitly remove duplicate edges (i.e. no multiple edges), use type 'int' instead of string
    
    print "#nodes :", G.vcount()
    print "#edges :", G.ecount()
    n_nodes = G.vcount()
    deg_list = G.degree(G.vs)         # dict[node] = deg
    min_deg = min(deg_list)
    max_deg = max(deg_list)
    print "min-deg =", min_deg
    print "max-deg =", max_deg

    # TEST randwalk_variance() - TOY DATA
#    G = ig.Graph()
#    G.add_vertices(4)
#    G.add_edges([(0,1),(0,3),(1,2),(1,3)])
#    
#    row_var = randwalk_variance(G, T, 0, 2) # nodes 0,1
#    print "row_var"
#    for t in range(1,T+1):
#        print row_var[t]
#    
#    row_var = randwalk_variance(G, T, 2, 2) # nodes 2,3
#    print "row_var"
#    for t in range(1,T+1):
#        print row_var[t]

    #
#    print "T =", T
#    print "start_node =", start_node
#    print "num_node =", num_node
#    start = time.clock()

    for i in range((end_node-start_node)/num_node + 1):
        s_node = start_node + i*num_node
        if s_node >= end_node:
            break
        n_node = num_node
        if end_node > s_node and end_node - s_node < num_node:
            n_node = end_node - s_node      # num of nodes
            
        # run 
        row_var, row_var2, non_zero_list, selfloop_list, multiedge_list = randwalk_variance(G, T, s_node, n_node)
        # append sum(row_var) to files
        for t in range(2,T+1):
            s = sum(row_var[t])
            s2 = sum(row_var2[t])
            with open("../variance/" + file_prefix + file_name + "_var_" + str(start_node) + "." + str(t), "a") as f:
                f.write("start_node = " + str(s_node) + " n_node = " + str(n_node) + "\n")
                f.write("n_non_zero = " + str(non_zero_list[t]) + " n_selfloop = " + str(selfloop_list[t]) + " n_multiedge = " + str(multiedge_list[t]) + "\n")
                f.write("sum = " + str(s) + " sum2 = " + str(s2) + "\n")
            f.close()


    
    
#    for t in range(2,T+1):
#        print row_var[t][0], "sum =", sum(row_var[t])
#    print "randwalk_variance, DONE, elapsed", time.clock() - start
    
    
    # TEST export_test_graph_to_MATLAB()
#    export_test_graph_to_MATLAB("ba_1000_5")
    