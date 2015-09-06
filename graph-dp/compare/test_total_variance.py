'''
Created on Jul 25, 2014

@author: huunguye
    - test Quadratic and Linear programming on 10k-node, 60k-edge synthetic graphs
'''

import time
from subprocess import call
import random
import math
import networkx as nx
import numpy as np
from graph_generator import generate_BA_graph
from uncertain_convex_opt import random_new_edges, nearby_new_edges, prepare_QP_MPS_file
from uncertain_linear_opt import linear_opt

WT_AVG_NEG = 0
WT_AVG_INV = 1
WT_TRIAD = 2
WT_ONLY_ORG = 3     # weights = 1 for e in E, 0 otherwise   
SUFFIX_DICT = {0:"_avg_neg", 1:"_avg_inv", 2:"_triad", 3:"_only_org"}

OPT_MAX = 0
OPT_MIN = 1
PARAM_OPT_DICT = {0:"-maximize", 1:"-minimize"}
SUFFIX_OPT_DICT = {0:"_max", 1:"_min"}

RANDOM_NEW = 0
NEARBY_NEW = 1
SUFFIX_NEW_DICT = {0:"", 1:"_nb"}

#######################################################
def read_sol_file(G, mps_file, n_vars, dir):
    
    # read .all_edge file
    all_edges = []
    
    all_edges_file = dir + mps_file + ".all_edge"
    f = open(all_edges_file, "r")
    fstr = f.read()
    f.close()
    for line in fstr.split("\n"):
        if line != '':
            items = line.split(" ")
            u = int(items[0])
            v = int(items[1])
            all_edges.append((u,v))
                
    #            
    sol_file = dir + mps_file + ".sol"
    f = open(sol_file, "r")
    fstr = f.read()
    f.close()
    
    lines = fstr.split("\n")
    for i in range(len(lines)-1-n_vars, len(lines)-1):     # len(lines)-1, avoid the empty (last) line
        line = lines[i]
        e_id = int(line[12:26].strip())                             # ex: 73391      X73391         SB 6.10930060231238e-001    
        e_prob = float(line[29:54].strip())
        e = all_edges[e_id]
        if not G.has_edge(e[0], e[1]):
            G.add_edge(e[0], e[1], {'p':e_prob})    # new edge
        else:
            G.edge[e[0]][e[1]]['p'] = e_prob
    
    #
    out_file = "../out/" + mps_file + ".out" 
    nx.write_edgelist(G, out_file, '#', '\t', True, 'utf-8')

#######################################################
if __name__ == '__main__':
    
    in_file = "ba_10000_3.gr"   # 30k edges
    n_new_edges = 30000         # 6k, 12k, 18k, 24k, 30k
    
    # 1 - 
#    print "generate synthetic graph(s)"
#    generate_BA_graph(10000, 3, "../data/" + in_file)
    
    # 
    G = nx.read_edgelist("../data/" + in_file, '#', '\t', None, nodetype=int, data=False)
    print "read graph G - DONE"
    
    ##### 2 - Quadratic/Linear program
    new_edges = nearby_new_edges(G, n_new_edges)
    
    # 2.1 - Quadratic
    mps_file = in_file[0:len(in_file)-3] + "_cvxopt_" + str(n_new_edges) + ".mps"
    mps_path = "../qp/" + mps_file
                                                                    
    prepare_QP_MPS_file(G, new_edges, mps_path, n_deleted=0)
    
    #
    start = time.clock()
    call(["mosek", mps_path], shell=False)
    print "call MOSEK DONE, elapsed :", time.clock() - start

    # 3.1 - Read sol file
    n_vars = G.number_of_edges() + n_new_edges
    
    start = time.clock()
    read_sol_file(G, mps_file[0:-4], n_vars, "../qp/")
    print "read_sol_file, elapsed :", time.clock() - start    
    
    # 2.2 - Linear
    random_or_nearby = NEARBY_NEW
    weight_type = WT_ONLY_ORG
    opt_type = OPT_MIN
    
    linear_opt(G, n_new_edges, random_or_nearby, weight_type, opt_type, in_file)
    mps_file = in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".mps"
    mps_path = "../lp/" + mps_file
    
    #
    start = time.clock()
    call(["mosek", mps_path], shell=False)
    print "call MOSEK DONE, elapsed :", time.clock() - start
    
    
    # 3.2 - Read sol file
    n_vars = G.number_of_edges() + n_new_edges
    
    start = time.clock()
    read_sol_file(G, mps_file[0:-4], n_vars, "../lp/")
    print "read_sol_file, elapsed :", time.clock() - start
    
    
    
    
    