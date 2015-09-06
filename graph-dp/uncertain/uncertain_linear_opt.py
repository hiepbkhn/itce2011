'''
Created on Apr 3, 2014

@author: huunguye

Apr 11
    - update prepare_MPS_file to MPS spacing standards (2,5,15,25,...)
Jul 24
    - add WT_ONLY_ORG (weights = 1 for e in E, 0 otherwise)  
'''

import time
import sys
import random
from subprocess import call
import math
import networkx as nx
import scipy.io
import numpy as np
from uncertain_convex_opt import random_new_edges, nearby_new_edges

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

SOLVER_CLP = 0
SOLVER_MOSEK = 1

#######################################################
def generate_weights(G, new_edges, weight_type):
    deg_list = nx.degree(G)
    w = []

    if weight_type == WT_AVG_NEG:
        # 1. average + negative for new_edges
        for e in G.edges_iter():
            w.append((deg_list[e[0]] + deg_list[e[1]])/2.0)
        for e in new_edges:
            w.append(-(deg_list[e[0]] + deg_list[e[1]])/2.0)    # negative weights for new edges    

    if weight_type == WT_AVG_INV:
        # 2. inverse of average
        for e in G.edges_iter():
            w.append(2.0/(deg_list[e[0]] + deg_list[e[1]]))
        for e in new_edges:
            w.append(2.0/(deg_list[e[0]] + deg_list[e[1]]))

    if weight_type == WT_TRIAD:
        pass
    
    if weight_type == WT_ONLY_ORG:
        for e in G.edges_iter():
            w.append(1)
        for e in new_edges:
            w.append(0)  
        
    
    # normalize so that the min in 1
    if weight_type == WT_AVG_NEG or weight_type == WT_AVG_INV:
        min_w = min(w[0:G.number_of_nodes()])
        for i in range(len(w)):
            w[i] = w[i]/min_w
    # 
    return w 

#######################################################
# prepare MPS file for CLP linear programming solver
def prepare_LP_MPS_file(G, new_edges, w, out_file):
    deg_list = nx.degree(G)     # dict[node] = deg
    
    # 1. index edge variables
    idx = {}        # for col ids in matrix A
    idr = {}        # for row ids in matrix A (nodes)
    i = 0
    all_edges = []
    for e in G.edges_iter():
        idx[(e[0], e[1])] = i
        idx[(e[1], e[0])] = i
        i += 1
        all_edges.append((e[0], e[1]))
    for e in new_edges:
        idx[(e[0], e[1])] = i
        idx[(e[1], e[0])] = i
        i += 1
        all_edges.append((e[0], e[1]))
    
    i = 0   
    for u in G.nodes_iter():
        idr[u] = i
        i += 1
    
    n_nodes = G.number_of_nodes()
    n_old = G.number_of_edges()
    n_new = len(new_edges)
    n_vars = n_old + n_new
    print "n_old =", n_old, " n_new =", n_new
    print "n_nodes =", n_nodes
    print "n_vars =", n_vars
    
    
    # 2. write to MPS file
    f = open(out_file, "w")
    f.write("NAME          TESTPROB\n")
    f.write("ROWS\n")
    f.write(" N  COST\n")
    for u in G.nodes_iter():
        row_id = idr[u]  
        f.write(" E  E" + str(row_id) + "\n")
    
    f.write("COLUMNS\n")
    
    ### OLD
#    # for each variable Xi
#    for i in range(n_vars):
#        e = all_edges[i]
#        var_name = "X" + str(i)
#        f.write("    " + var_name + "      " + "COST" + "              " + str(w[i]) + "\n")
#        f.write("    " + var_name + "      " + "E" + str(idr[e[0]]) + "              1" + "\n")
#        f.write("    " + var_name + "      " + "E" + str(idr[e[1]]) + "              1" + "\n")
#    
#    f.write("RHS\n")
#    # for each variable
#    for u in G.nodes_iter():
#        row_id = idr[u]  
#        f.write("    RHS1" + "      " + "E" + str(row_id) + "              " + str(deg_list[u]) + "\n") 
#    
#    f.write("BOUNDS\n")
#    # for each variable
#    for i in range(n_vars):
#        var_name = "X" + str(i)
#        f.write(" LO BND1      " + var_name + "           0" + "\n")
#        f.write(" UP BND1      " + var_name + "           1" + "\n")
    
    ###  LEFT JUSTIFY    (2,5,15,25)
    # for each variable Xi
    for i in range(n_vars):
        e = all_edges[i]
        var_name = "X" + str(i)
        f.write("    " + var_name.ljust(10) + "COST".ljust(10) + str(w[i]) + "\n")
        f.write("    " + var_name.ljust(10) + "E" + str(idr[e[0]]).ljust(9) + "1" + "\n")
        f.write("    " + var_name.ljust(10) + "E" + str(idr[e[1]]).ljust(9) + "1" + "\n")
    
    f.write("RHS\n")
    # for each variable
    for u in G.nodes_iter():
        row_id = idr[u]  
        f.write("    RHS1".ljust(14) + "E" + str(row_id).ljust(9) + str(deg_list[u]) + "\n") 
    
    f.write("BOUNDS\n")
    # for each variable
    for i in range(n_vars):
        var_name = "X" + str(i)
        f.write(" LO BND1      " + var_name.ljust(10) + "0" + "\n")
        f.write(" UP BND1      " + var_name.ljust(10) + "1" + "\n")        
    
    f.write("ENDATA")
    
    f.close()
    # 
    return all_edges
    
#######################################################
# linear programming - call CLP
def linear_opt(G, n_new_edges, random_or_nearby, weight_type, opt_type, in_file, solver):
    #
    if random_or_nearby == 0:       # 0: RANDOM_NEW
        new_edges = random_new_edges(G, n_new_edges)
    elif random_or_nearby == 1:     # 1: NEARBY_NEW
        new_edges = nearby_new_edges(G, n_new_edges)
    else:
        print "WRONG param <random_or_nearby>"
        return
    
    #
    w = generate_weights(G, new_edges, weight_type)
    #
    mps_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".mps"
    all_edges = prepare_LP_MPS_file(G, new_edges, w, mps_file)
    
    # write all_edges file to help the parse of sol_file
    all_edges_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".all_edge"
    f = open(all_edges_file, "w")
    for i in range(len(all_edges)):
        e = all_edges[i]
        f.write("%d %d\n"%(e[0], e[1]))
    f.close()
    
    if solver == SOLVER_CLP:
        # call CLP.exe
        sol_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".sol"
        start = time.clock()
        call(["../clp.exe", mps_file, PARAM_OPT_DICT[opt_type], "-dualsimplex", "-solution", sol_file], shell=False)
        print "call CLP: DONE, elapsed :", time.clock() - start
        
    elif solver == SOLVER_MOSEK:
        # call MOSEK
        start = time.clock()
        call(["mosek", mps_file], shell=False)
        print "call MOSEK: DONE, elapsed :", time.clock() - start
    
    
#    # read sol_file
#    aG = nx.Graph()
#    f = open(sol_file, "r")
#    fstr = f.read()
#    f.close()
#    
#    for line in fstr.split("\n"):
#        if line != '':
#            items = line.split(" ")
#            i = 0
#            non_empty = []
#            for item in items:
#                if item != "":
#                    non_empty.append(item)
#            e_id = int(non_empty[1][1:])
#            e_prob = float(non_empty[2])
#            e = all_edges[e_id]
#            aG.add_edge(e[0], e[1], {'p':e_prob})
#            
#    # write to out_file
#    start = time.clock()
#    out_file = "../out/" + in_file[0:-3] + "_linopt_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + \
#                                                SUFFIX_DICT[weight_type] + SUFFIX_OPT_DICT[opt_type] + ".out"
#    nx.write_edgelist(aG, out_file, '#', '\t', True, 'utf-8')
#    print "write out_file: DONE, elapsed :", time.clock() - start        
    

#######################################################
# parse sol_file of CLP
def parse_sol_file(n_new_edges, random_or_nearby, weight_type, opt_type, in_file):    
    all_edges = []
    
    all_edges_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".all_edge"
    f = open(all_edges_file, "r")
    fstr = f.read()
    f.close()
    for line in fstr.split("\n"):
        if line != '':
            items = line.split(" ")
            u = int(items[0])
            v = int(items[1])
            all_edges.append((u,v))
            
    # read sol_file
    sol_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".sol"
    
    # read sol_file
    aG = nx.Graph()
    f = open(sol_file, "r")
    fstr = f.read()
    f.close()
    
    for line in fstr.split("\n"):
        if line != '':
            items = line.split(" ")
            non_empty = []
            for item in items:
                if item != "":
                    non_empty.append(item)
            e_id = int(non_empty[1][1:])
            e_prob = float(non_empty[2])
            e = all_edges[e_id]
            if e_prob > 0.0001:     # only add non-zero edges
                aG.add_edge(e[0], e[1], {'p':e_prob})
            
    # write to out_file
    start = time.clock()
    out_file = "../out/" + in_file[0:-3] + "_linopt_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + \
                                                SUFFIX_DICT[weight_type] + SUFFIX_OPT_DICT[opt_type] + ".out"
    nx.write_edgelist(aG, out_file, '#', '\t', True, 'utf-8')
    print "write out_file: DONE, elapsed :", time.clock() - start        

#######################################################
# parse bas_file of MOSEK
def parse_bas_file(n_nodes, n_new_edges, random_or_nearby, weight_type, opt_type, in_file):    
    all_edges = []
    
    all_edges_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".all_edge"
    f = open(all_edges_file, "r")
    fstr = f.read()
    f.close()
    for line in fstr.split("\n"):
        if line != '':
            items = line.split(" ")
            u = int(items[0])
            v = int(items[1])
            all_edges.append((u,v))
            
    # read bas_file
    bas_file = "../lp/" + in_file[0:-3] + "_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + SUFFIX_DICT[weight_type] + ".bas"
    print "bas_file =", bas_file
    
    
    # read bas_file
    aG = nx.Graph()
    f = open(bas_file, "r")
    fstr = f.read()
    f.close()
    
    lines = fstr.split("\n")
    for i in range(n_nodes + 12, len(lines)-1):     # skip (n_nodes + 12) lines
        line = lines[i]
        e_id = int(line[12:26].strip())
        e_prob = float(line[29:54].strip())
        e = all_edges[e_id]
        if e_prob > 0.0001:     # only add non-zero edges
            aG.add_edge(e[0], e[1], {'p':e_prob})
            
    # write to out_file
    start = time.clock()
    out_file = "../out/" + in_file[0:-3] + "_linopt_" + str(n_new_edges) + SUFFIX_NEW_DICT[random_or_nearby] + \
                                                SUFFIX_DICT[weight_type] + SUFFIX_OPT_DICT[opt_type] + ".out"
    nx.write_edgelist(aG, out_file, '#', '\t', True, 'utf-8')
    print "write out_file: DONE, elapsed :", time.clock() - start    

#######################################################
if __name__ == '__main__':
    
    #
#    in_file = "er_1000_001.gr"               # 4965 edges
#    in_file = "er_100000_00001.gr"           # 500451 edges, max:4s (solution: all 1.0), (200k, inv, min: 80s)
#    in_file = "sm_100000_005_11.gr"         #100k nodes    (50k, inv, min: 12s), (100k, inv, min: 25s), (200k, inv, min: 48s)
#    in_file = "ba_100000_5.gr"               #100k nodes    (50k, inv, min: 22s), (100k, inv, min: 46s), (200k, inv, min: 70s), (300k, inv, min: 82s)   
    
    # real graphs
#    in_file = "com_dblp_ungraph.gr"         # com_dblp_ungraph (317080, 1049866)    (200k, inv, min: 53s), (400k, inv, min: 152s), 
                                            #                                       (200k nearby, inv, min: 63s), (400k nearby, inv, min: 109s)
#    in_file = "com_youtube_ungraph.gr"     # com_youtube_ungraph (1M, 3M)           (500k, inv, min: 222-324s)
    
    #
#    G = nx.Graph()
#    G.add_edges_from([(0,1),(1,2),(1,3),(2,3),(3,4)])
#    new_edges = [(1,4),(0,3),(0,2)]        #sol (lower=0.0):  0.5 1.0 1.0 1.0 0.5 | 0.5 0.5 0.0
#
##    w = [4,5,6,5,4,4,4,3]                                # result = original
##    w = [0.5, 1.0, 1.0, 1.0, 0.5, 0.5, 0.5, 0.0]        # result = original
##    w = [0.5, 1.0, 1.0, 1.0, 0.1, -0.5, -0.5, -0.5]    # result = swap edges

    # TEST generate_weights
##    new_edges = random_new_edges(G, 1000)
#    new_edges = random_new_edges(G, 100000)
#      
#    w = generate_weights(G, new_edges)

    # TEST prepare_MPS_file
#    prepare_MPS_file(G, new_edges, w, "../data/a_toy.mps")

#    prepare_MPS_file(G, new_edges, w, "../data/er_1000_001.mps")
#    prepare_MPS_file(G, new_edges, w, "../data/er_1000_001_neg.mps")
#    prepare_MPS_file(G, new_edges, w, "../data/er_1000_001_inv.mps")
    
#    prepare_MPS_file(G, new_edges, w, "../data/er_100000_00001_inv.mps")
    
    
    #===========================================
    # Command-line param for automation (bash): <in_file> <n_new_edges> <random_or_nearby> <weight_type> <solver>
    # TEST linear_opt()
    n_new_edges = 200000
    
    random_or_nearby = NEARBY_NEW
#    random_or_nearby = RANDOM_NEW       # CLP run 60s (faster) but crashed after optimal solution found (cannot write to  sol_file) !, run MOSEK ok
    
#    weight_type = WT_AVG_INV
    weight_type = WT_ONLY_ORG           # default
    
    opt_type = OPT_MIN
    solver = SOLVER_CLP
    
    if len(sys.argv) > 5:
        in_file = sys.argv[1]
        n_new_edges = int(sys.argv[2])
        random_or_nearby = int(sys.argv[3])
        weight_type = int(sys.argv[4])
        solver = int(sys.argv[5])
    print "in_file =", in_file
    print "n_new_edges =", n_new_edges
    print "random_or_nearby =", random_or_nearby
    print "weight_type =", weight_type
    print "solver =", solver
    
    #
    G = nx.read_edgelist("../data/" + in_file, '#', '\t', None, nodetype=int, data=False)
    print "read graph G - DONE"

    
    start = time.clock()
    linear_opt(G, n_new_edges, random_or_nearby, weight_type, opt_type, in_file, solver)
    print "call linear_opt - DONE, elapsed", time.clock() - start
    
    if solver == SOLVER_CLP:
        start = time.clock()
        parse_sol_file(n_new_edges, random_or_nearby, weight_type, opt_type, in_file)
        print "call parse_sol_file - DONE, elapsed", time.clock() - start
        
    elif solver == SOLVER_MOSEK:
        # TEST parse_bas_file
        start = time.clock()
        n_nodes = G.number_of_nodes()
    
        parse_bas_file(n_nodes, n_new_edges, random_or_nearby, weight_type, opt_type, in_file)
        print "call parse_bas_file - DONE, elapsed :", time.clock() - start    
    
