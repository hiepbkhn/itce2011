'''
Created on Mar 28, 2014

@author: huunguye

Jan 14 2015    
    - convert to directed: G.degree() -> G.out_degree(), Graph() -> DiGraph()
'''

import time
import sys
import os.path
from subprocess import call
import random
import math
import networkx as nx
import scipy.io
import numpy as np
import pp
from uncertain_convex_opt_directed import random_new_edges, nearby_new_edges, randwalk_new_edges, max_variance_opt, prepare_QP_MPS_file
from graph_generator import convert_to_metis_file
from switch_obfuscation import random_switch, nearby_switch

PARTITION_SIZE = 1000.0
N_PARTITIONS = 20  # 5, 10, 20, 30, 60, 100, 200, 400, 600

RANDOM_NEW = 0
NEARBY_NEW = 1
UNIQUE_NEW = 2
RDWALK_NEW = 3
SUFFIX_NEW_DICT = {0:"_rand", 1:"_nb", 2:"_uq", 3:"_rw"}

REP_GP = 0
REP_ADR = 1
REP_ABM = 2

REP_DICT = {0:"gp", 1:"adr", 2:"abm"}

SWITCH_RANDOM = 0
SWITCH_NEARBY = 1
SUFFIX_SWITCH_DICT = {0:"_SWrand", 1:"_SWnb"}

#######################################################
# WARNING: ff_10000_05_connected.gr: last part with 25k edges!
def uniform_edges_distribute(G, graph_list, part_dict):
    #
    for e in G.edges_iter():
        u = e[0]
        v = e[1]
        if part_dict[u] != part_dict[v]:    # u,v in different subgraphs
            # add (u,v) to the subgraph with less edges
            G_u = graph_list[part_dict[u]]
            G_v = graph_list[part_dict[v]]
            if G_u.number_of_edges() < G_v.number_of_edges():
                G_u.add_edge(u,v)
            else:
                G_v.add_edge(v,u)
    #
    return graph_list    

#######################################################
def write_subgraphs(graph_list, in_file):
    # write subgraphs to files
    i = 0
    for aG in graph_list:
        filename = "../part/" + in_file[0:len(in_file)-3] + "." + str(i) 
        nx.write_edgelist(aG, filename, '#', '\t', False, 'utf-8')
        i += 1
    print "write graph_list to files: DONE"
    
#######################################################
def write_subgraphs_triads(graph_list, in_subgraph_edges, in_file):
    # write subgraphs to files
    write_subgraphs(graph_list, in_file)
    
    # write in_subgraph_edges
    i = 0
    for e_list in in_subgraph_edges:
        filename = "../part/" + in_file[0:len(in_file)-3] + "_ne." + str(i)     # ne: contains new edges
        f = open(filename, "w")
        for e in e_list:
            f.write("%d %d\n"%(e[0], e[1]))
        f.close()
        i += 1
    print "write in_subgraph_edges to files: DONE"

#######################################################    
def read_partitions_from_part_files(G, part_file, n_partitions): 

    # read partitions from part_file
    f = open(part_file, "r")
    fstr = f.read()
    f.close()
    
    id_list = [[] for i in range(n_partitions)]
    part_dict = {}      # part_dict[u] = part_id of node u
    node_id = 0
    for line in fstr.split("\n"):
        if line != '':
            part_id = int(line)
        id_list[part_id].append(node_id)
        part_dict[node_id] = part_id
        
        node_id += 1
    
    # extract subgraphs from G
    graph_list = []  # list of subgraphs
#    zero_deg = [[] for i in range(n_partitions)]    # list of lists
    for i in range(n_partitions):
        aG = G.subgraph(id_list[i])
#        aG.add_nodes_from(id_list[i])           # IMPORTANT: read nodes as nodes may be 0-degree
#        for u in id_list[i]:
#            if len(aG.neighbors(u)) == 0:
#                zero_deg[i].append(u)
        graph_list.append(aG)    
    
    #
    return graph_list, part_dict

    
#######################################################
# use METIS
def prepare_jobs_metis(G, in_file, n_partitions):

    metis_file = "../data/" + in_file[0:len(in_file)-3] + ".metis"
    print "metis_file =", metis_file
    
#    convert_to_metis_file(G, metis_file)    # partition G
    
#    start = time.clock()
#    call(["../gpmetis.exe", metis_file, str(n_partitions)], shell=False)
#    print "call GPMATIS: DONE, elapsed :", time.clock() - start

    #
    part_file = "../data/" + in_file[0:len(in_file)-3] + ".metis.part." + str(n_partitions)
    print "part_file =", part_file
    
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)
        
    #    
    return graph_list, part_dict    

#######################################################
# call MOSEK 7 - partition first, add edges later
# n_deleted: number of edges deleted in each subgraph !
def prepare_mps_and_solve(G, in_file, n_partitions, n_new_edges, random_or_nearby, n_deleted=0):
    
    #
    part_file = "../data/" + in_file[0:len(in_file)-3] + ".metis.part." + str(n_partitions)
    print "part_file =", part_file
    
    # read partitions from .part file
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)     
    
    sum_nodes = sum(aG.number_of_nodes() for aG in graph_list)
    sum_edges = sum(aG.number_of_edges() for aG in graph_list)
    print "sum_nodes =", sum_nodes
    print "sum_edges =", sum_edges
    for aG in graph_list:
        print aG.number_of_nodes(), "-", aG.number_of_edges()
    print "#missing edges =", G.number_of_edges() - sum_edges
    
    # create MPS files
    str_n_deleted = ""
    if n_deleted > 0:
        str_n_deleted = "_" + str(n_deleted * n_partitions)
        
    all_deleted_edges = []
    start = time.clock()
    i = 0
    for aG in graph_list:
        # 
        if random_or_nearby == RANDOM_NEW:       # 0: RANDOM_NEW
            new_edges = random_new_edges(aG, n_new_edges)
        elif random_or_nearby == NEARBY_NEW:     # 1: NEARBY_NEW
            new_edges = nearby_new_edges(aG, n_new_edges)
#        elif random_or_nearby == UNIQUE_NEW:     # 2: UNIQUE_NEW
#            new_edges = unique_new_edges(aG, n_new_edges, sigma=0.1, eps=0.1)
        elif random_or_nearby == RDWALK_NEW:     # 3: RDWALK_NEW    
            new_edges = randwalk_new_edges(aG, n_new_edges, t=2, M=10)
        else:
            print "WRONG param <random_or_nearby>"
            return

        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + \
                                                                    SUFFIX_NEW_DICT[random_or_nearby] + "." + str(i) + ".mps"
        deleted_edges = prepare_QP_MPS_file(aG, new_edges, mps_file, n_deleted)
        all_deleted_edges.extend(deleted_edges)
        print "file", mps_file, "written"
        i += 1
        
    print "prepare MPS files, DONE, elapsed :", time.clock() - start
    
    # call MOSEK
    for i in range(n_partitions):
        start = time.clock()
        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + \
                                                                    SUFFIX_NEW_DICT[random_or_nearby] + "." + str(i) + ".mps"
        call(["mosek", mps_file], shell=False)
        print "call MOSEK part i =", i, "DONE, elapsed :", time.clock() - start

    # for combine_sol_files()
    return all_deleted_edges

#######################################################
# call MOSEK 7 - add edges first, partition later
def prepare_mps_and_solve_add_edges_first(G, in_file, n_partitions, n_new_edges, random_or_nearby):
    
    # 1 - add edges FIRST
    if random_or_nearby == RANDOM_NEW:       # 0: RANDOM_NEW
        new_edges = random_new_edges(G, n_new_edges * n_partitions)
    elif random_or_nearby == NEARBY_NEW:     # 1: NEARBY_NEW
        new_edges = nearby_new_edges(G, n_new_edges * n_partitions)
#    elif random_or_nearby == UNIQUE_NEW:     # 2: UNIQUE_NEW
#        new_edges = unique_new_edges(G, n_new_edges * n_partitions, sigma=0.1, eps=0.1)
    elif random_or_nearby == RDWALK_NEW:     # 3: RDWALK_NEW    
        new_edges = randwalk_new_edges(G, n_new_edges * n_partitions, t=2, M=10)
    else:
        print "WRONG param <random_or_nearby>"
        return
    
    aG = nx.DiGraph()
    aG.add_edges_from(G.edges_iter())
    aG.add_edges_from(new_edges)
    
    # 2 - convert to METIS file
    metis_file = "../data/" + in_file[0:len(in_file)-3] + SUFFIX_NEW_DICT[random_or_nearby] + "_first_" + str(n_new_edges) + ".metis"        # suffix _triad + str(n_new_edges)
    print "metis_file =", metis_file
    
    convert_to_metis_file(aG, metis_file)    # partition aG
    
    # partition aG
    start = time.clock()
    call(["../gpmetis.exe", metis_file, str(n_partitions)], shell=False)
    print "call GPMATIS: DONE, elapsed :", time.clock() - start

    # read full_graph_list
    part_file = "../data/" + in_file[0:len(in_file)-3] + SUFFIX_NEW_DICT[random_or_nearby] + "_first_" + str(n_new_edges) + ".metis.part." + str(n_partitions)    # suffix _triad + str(n_new_edges)
    print "part_file =", part_file
    
    full_graph_list, part_dict = read_partitions_from_part_files(aG, part_file, n_partitions)     # IMPORTANT: use aG, not G !!
    
    # build graph_list (subgraphs of G) and new_edges_list (new_edges for each subgraph)
    graph_list = []
    new_edges_list = []
    for aG in full_graph_list:
        graph_list.append(nx.subgraph(G, aG.nodes_iter()))
        temp_list = []
        for e in aG.edges_iter():
            if not G.has_edge(e[0], e[1]):
                temp_list.append((e[0], e[1]))
        new_edges_list.append(temp_list)
    
    # 3 - create MPS files
    start = time.clock()
    i = 0
    for aG in graph_list:
        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + \
                                                    SUFFIX_NEW_DICT[random_or_nearby] + "_first" + "." + str(i) + ".mps"
        prepare_QP_MPS_file(aG, new_edges_list[i], mps_file)
        print "file", mps_file, "written"
        i += 1
        
    print "prepare MPS files, DONE, elapsed :", time.clock() - start
    
    # 4 - call MOSEK
    for i in range(n_partitions):
        start = time.clock()
        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + \
                                                    SUFFIX_NEW_DICT[random_or_nearby] + "_first" + "." + str(i) + ".mps"
        call(["mosek", mps_file], shell=False)
        print "call MOSEK part i =", i, "DONE, elapsed :", time.clock() - start


#######################################################
# call MOSEK 7 - partition first, then switch, finally add edges
# num of switches = switch_ratio * subgraph.num_of_edges
def prepare_mps_and_solve_with_switch(G, in_file, n_partitions, n_new_edges, random_or_nearby, switch_ratio=0.0, switch_type=SWITCH_NEARBY):
    
    #
    part_file = "../data/" + in_file[0:len(in_file)-3] + ".metis.part." + str(n_partitions)
    print "part_file =", part_file
    
    # read partitions from .part file
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)     
    
    sum_nodes = sum(aG.number_of_nodes() for aG in graph_list)
    sum_edges = sum(aG.number_of_edges() for aG in graph_list)
    print "sum_nodes =", sum_nodes
    print "sum_edges =", sum_edges
    for aG in graph_list:
        print aG.number_of_nodes(), "-", aG.number_of_edges()
    print "#missing edges =", G.number_of_edges() - sum_edges
    
    # create MPS files
    all_deleted_edges = []
    start = time.clock()
    i = 0
    for aG in graph_list:
        # switch edges if any
        if switch_ratio > 0.0:
            num_sw = int(switch_ratio*aG.number_of_edges())
            if switch_type == SWITCH_RANDOM:
                # RANDOM
                bG = random_switch(aG, num_sw)
            elif switch_type == SWITCH_NEARBY:
                # NEARBY
                bG = nearby_switch(aG, num_sw)
            for (u,v) in aG.edges_iter():       # find deleted edges
                if not bG.has_edge(u,v):
                    all_deleted_edges.append((u,v))
        
        # 
        if random_or_nearby == RANDOM_NEW:       # 0: RANDOM_NEW
            new_edges = random_new_edges(aG, n_new_edges)
        elif random_or_nearby == NEARBY_NEW:     # 1: NEARBY_NEW
            new_edges = nearby_new_edges(aG, n_new_edges)
#        elif random_or_nearby == UNIQUE_NEW:     # 2: UNIQUE_NEW
#            new_edges = unique_new_edges(aG, n_new_edges, sigma=0.1, eps=0.1)
        elif random_or_nearby == RDWALK_NEW:     # 3: RDWALK_NEW    
            new_edges = randwalk_new_edges(aG, n_new_edges, t=2, M=10)
        else:
            print "WRONG param <random_or_nearby>"
            return

        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + SUFFIX_NEW_DICT[random_or_nearby] + \
                                                                    "_switch_" + str(switch_ratio) + SUFFIX_NEW_DICT[switch_type] + "." + str(i) + ".mps"
        prepare_QP_MPS_file(bG, new_edges, mps_file)    # use bG not aG !
        print "file", mps_file, "written"
        i += 1
        
    print "prepare MPS files, DONE, elapsed :", time.clock() - start
    
    # call MOSEK
    for i in range(n_partitions):
        start = time.clock()
        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + SUFFIX_NEW_DICT[random_or_nearby] + \
                                                                    "_switch_" + str(switch_ratio) + SUFFIX_NEW_DICT[switch_type] + "." + str(i) + ".mps"
        call(["mosek", mps_file], shell=False)
        print "call MOSEK part i =", i, "DONE, elapsed :", time.clock() - start

    # for combine_sol_files()
    return all_deleted_edges

        
#######################################################
# extract subset of newedges
# sub_size = 0.75, 0.5, 0.25 ...
def extract_subset_and_solve(G, in_file, n_partitions, n_new_edges, random_or_nearby, sub_size):
    
    part_file = "../data/" + in_file[0:len(in_file)-3] + ".metis.part." + str(n_partitions)
    print "part_file =", part_file
    
    # read partitions from .part file
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)
    
    # extract new_edges from .out file
    new_edges = [[] for i in range(n_partitions)]   # list of lists
    
    out_file = "../out/" + in_file[0:len(in_file)-3] + "_cvxopt_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + \
                                SUFFIX_NEW_DICT[random_or_nearby] + "_missing.out"
    
    start = time.clock()
    print "out_file =", out_file
    outG = nx.read_edgelist(out_file, '#', '\t', None, nodetype=int, data=False)    # do not read 'p'
    print "read outG - DONE, elapsed", time.clock() - start
    
    
    for e in outG.edges_iter():
        if not G.has_edge(e[0], e[1]):
            for i in range(n_partitions):
                if graph_list[i].has_node(e[0]) and  graph_list[i].has_node(e[1]):
                    new_edges[i].append((e[0], e[1]))
                    break
    
    # get sub_new_edges with size of sub_size (per subgraph)
    sub_new = int(n_new_edges*sub_size)   # size of subset per subgraph
    print "sub_new =", sub_new
    perm = np.random.permutation(n_new_edges)
    
    sub_new_edges = [[] for i in range(n_partitions)]   # list of lists
    for i in range(n_partitions):
        for j in range(sub_new):
            sub_new_edges[i].append(new_edges[i][perm[j]]) 
    
    
    # create MPS files
    sub_size_str = str(sub_size)
    sub_size_str = sub_size_str.replace(".", "")
    print "sub_size_str =", sub_size_str
    
    i = 0
    for aG in graph_list:
        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + \
                                        "_sub_" + sub_size_str + SUFFIX_NEW_DICT[random_or_nearby] + "." + str(i) + ".mps"
        prepare_QP_MPS_file(aG, sub_new_edges[i], mps_file)
        print "file", mps_file, "written"
        
        i += 1
    
    # call MOSEK
    for i in range(len(graph_list)):
        start = time.clock()
        mps_file = "../qp/" + in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + \
                                        "_sub_" + sub_size_str + SUFFIX_NEW_DICT[random_or_nearby] + "." + str(i) + ".mps"
        call(["mosek", mps_file], shell=False)
        print "call MOSEK part i =", i, "DONE, elapsed :", time.clock() - start
    
    

#######################################################
# even slower than 1 core !
def parallel_max_variance_opt(aG, n_new, i):
    # 1.
    new_edges = random_new_edges(aG, n_new)
    
    # 2.
    out_file = "../out/temp_" + str(i) + ".out"
    max_variance_opt(aG, new_edges, lower=0.0, out_file=out_file)
    
    #
    return 0

#######################################################
# parallel by processing independent files --> ok
def parallel_max_variance_opt_file(in_file, n_new, i, random_or_nearby):
    
    data_name = in_file[0:len(in_file)-3]
    filename = "../part/" + data_name + "." + str(i)
    aG = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=False) 
    
    # 1. add n_new edges
    if random_or_nearby == 0:       # 0: RANDOM_NEW
        new_edges = random_new_edges(aG, n_new)
    elif random_or_nearby == 1:     # 1: NEARBY_NEW
        new_edges = nearby_new_edges(aG, n_new)
#    elif random_or_nearby == 2:     # 2: UNIQUE_NEW
#        new_edges = unique_new_edges(aG, n_new, sigma=0.1, eps=0.1)
    else:
        print "WRONG param <random_or_nearby>"
        return
    
    # 2. output probabilitites to files
    out_file = "../part/" + data_name + "_out" + "." + str(i)
    max_variance_opt(aG, new_edges, lower=0.0, out_file=out_file)
    
    #
    return 0

#######################################################
# use check_struture.compute_triad_weights()
def parallel_max_variance_opt_file_no_new_edges(in_file, i):
    
    # sub graph
    data_name = in_file[0:len(in_file)-3]
    filename = "../part/" + data_name + "." + str(i)
    aG = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=False) 
    
    # 
    in_subgraph_edges = []
    filename = "../part/" + data_name + "_ne." + str(i)         # ne: new edges
    f = open(filename, "r")
    fstr = f.read()
    for line in fstr.split("\n"):
        if line != '':
            items = line.split(" ")
            u = int(items[0])
            v = int(items[1])
            in_subgraph_edges.append((u,v))
    f.close()
    
    # output probabilitites to files
    out_file = "../part/" + data_name + "_out" + "." + str(i)
    max_variance_opt(aG, in_subgraph_edges, lower=0.0, out_file=out_file)      # new_edges = []
    
    #
    return 0


#######################################################
def export_to_MATLAB(filename, mat_file):    
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True) 
    #
    edge_prob = [e[2]['p'] for e in G.edges_iter(data=True)]
                
    scipy.io.savemat(mat_file, dict(edge_prob=np.array(edge_prob)) )
    
#######################################################
def combine_temp_files(G, n_partitions, out_file):
    for e in G.edges(data=True):
        e[2]['p'] = 1.0
    missing_parts = []
    for i in range(n_partitions):
        part_name = "../part/temp_out." + str(i)
        if os.path.isfile(part_name):
            aG = nx.read_edgelist(part_name, '#', '\t', None, nodetype=int, data=True) 
            for e in aG.edges_iter(data=True):
                if G.has_edge(e[0],e[1]):
                    G.edge[e[0]][e[1]]['p'] = e[2]['p']
                else:
                    G.add_edge(e[0],e[1], {'p':e[2]['p']})
        else:
            missing_parts.append(i)
    #
    nx.write_edgelist(G, out_file, '#', '\t', True, 'utf-8')
    
    #
    print "missing parts =", missing_parts
    return missing_parts
                
#######################################################
# combine .sol files (output by MOSEK)
def combine_sol_files(G, n_partitions, mps_file, out_file, all_deleted_edges=[]):
    
    # all_deleted_edges
    for (u,v) in all_deleted_edges:
        G.remove_edge(u,v)
        
    #debug
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    sum_n_old = 0
    total_subsum_p = 0.0
    n_dup_edges = 0
    
    for e in G.edges(data=True):
        e[2]['p'] = 1.0
    
    # read .sol files + .all_edge files
    for i in range(n_partitions):
        print "read .sol file i =", i
        
        # read .all_edge file
        all_edges = []
        
        all_edges_file = "../qp/" + mps_file + "." + str(i) + ".all_edge"
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
        sol_file = "../qp/" + mps_file + "." + str(i) + ".sol"
        f = open(sol_file, "r")
        fstr = f.read()
        f.close()
        
        subsum_p = 0.0      # debug
        n_new = 0
        n_old = 0
        
        
        lines = fstr.split("\n")
        for i in range(len(lines)-1-len(all_edges), len(lines)-1):     # len(lines)-1, avoid the empty (last) line
            line = lines[i]
            e_id = int(line[12:26].strip())                             # ex: 73391      X73391         SB 6.10930060231238e-001    
            e_prob = float(line[29:54].strip())
            subsum_p += e_prob
            e = all_edges[e_id]
            if not G.has_edge(e[0], e[1]):
                G.add_edge(e[0], e[1], {'p':e_prob})    # new edge
                n_new += 1
            else:
                if G.edge[e[0]][e[1]]['p'] < 1.0:       # existing edge
                    n_dup_edges += 1
                G.edge[e[0]][e[1]]['p'] = e_prob
                n_old += 1
        
        print "n_dup_edges =", n_dup_edges
        print "subsum_p =", subsum_p, "n_new =", n_new, "n_old =", n_old
        sum_n_old += n_old
        total_subsum_p += subsum_p
        
    print "total_subsum_p =", total_subsum_p
    sum_p = sum(e[2]['p'] for e in G.edges_iter(data=True))
    print "sum_p =", sum_p
    print "sum_n_old =", sum_n_old
    print "#missing =", G.number_of_edges() - sum_n_old
                    
    #
    nx.write_edgelist(G, out_file, '#', '\t', True, 'utf-8')
    
    #
                
#######################################################
if __name__ == '__main__':
    
    ## 10k nodes
#    in_file = "er_10000_0001.gr"        # ER    29k edges missing, redistribute: 59s
#    in_file = "sm_10000_005_11.gr"      # SM    4.4k edges missing, redistribute: 3s
#    in_file = "ba_10000_5.gr"           # BA
#    in_file = "pl_10000_5_01.gr"        # PL
#    in_file = "ff_10000_05_connected.gr"        # FF, 17k edges missing, redistribute: 14s
    
    ## 100k nodes
#    in_file = "er_100000_00001.gr"          # ER
#    in_file = "sm_100000_005_11.gr"        # SM
#    in_file = "ba_100000_5.gr"            # BA
#    in_file = "pl_100000_5_01.gr"        # PL
#    in_file = "ff_100000_045_connected.gr"    # FF

    ## real graphs
    file_name = "web_NotreDame_directed"   # 325k nodes, 1.5M edges  
#    file_name = "prod_amazon0302_directed"   # 260k nodes, 1.23M edges
    
    
    ### 2 - SERIAL (call MOSEK)
    ######
    # Command-line param for automation (bash): <in_file> <n_new_edges> <n_partitions> <algo_type> <add_edges_first> <n_deleted> <switch_ratio> <switch_type>
    n_new_edges = 20000
    n_deleted = 0
    n_partitions = 0
    algo_type = -1
    add_edges_first = -1        # 0: NO, 1: YES
    switch_ratio = 0.0
    switch_type = -1
    if len(sys.argv) > 5:
        in_file = sys.argv[1]
        n_new_edges = int(sys.argv[2])
        n_partitions = int(sys.argv[3])
        algo_type = int(sys.argv[4])
        add_edges_first = int(sys.argv[5])
    if len(sys.argv) > 6:
        n_deleted = int(sys.argv[6])
    if len(sys.argv) > 7:
        switch_ratio = float(sys.argv[7])
    if len(sys.argv) > 8:
        switch_type = int(sys.argv[8])

    print "in_file =", in_file
    print "n_new_edges =", n_new_edges
    print "n_partitions =", n_partitions
    print "algo_type =", algo_type
    print "add_edges_first =", add_edges_first
    print "n_deleted =", n_deleted
    print "switch_ratio =", switch_ratio
    print "switch_type =", switch_type
    
    #
    G = nx.read_edgelist("../data/" + in_file, '#', '\t', create_using=nx.DiGraph(), nodetype=int, data=False)      # DIRECTED
    print "read graph G - DONE"
    
    # check add edges FIRST or not
    str_n_deleted = ""
    if n_deleted > 0:
        str_n_deleted = "_" + str(n_deleted * n_partitions)
    
    start = time.clock()
    if add_edges_first == 0:
        # algo_type -> 0: RANDOM_NEW, 1: NEARBY_NEW, 2: UNIQUE_NEW, 3:RDWALK_NEW
        all_deleted_edges = prepare_mps_and_solve(G, in_file, n_partitions, n_new_edges, algo_type, n_deleted) 
        print "len(all_deleted_edges) =", len(all_deleted_edges)
        print "prepare_mps_and_solve - DONE, elapsed :", time.clock() - start
        
        mps_file = in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + SUFFIX_NEW_DICT[algo_type]
        out_file = "../out/" + in_file[0:len(in_file)-3] + "_cvxopt_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + \
                                                    SUFFIX_NEW_DICT[algo_type] + "_missing.out"
        
    elif add_edges_first == 1:
        prepare_mps_and_solve_add_edges_first(G, in_file, n_partitions, n_new_edges, algo_type)   
        print "prepare_mps_and_solve_add_edges_first - DONE, elapsed :", time.clock() - start
        
        mps_file = in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + SUFFIX_NEW_DICT[algo_type] + "_first"
        out_file = "../out/" + in_file[0:len(in_file)-3] + "_cvxopt_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + \
                                                    SUFFIX_NEW_DICT[algo_type] + "_first" + "_missing.out"
                                                    
    elif add_edges_first == 2:  # for SWITCH
        all_deleted_edges = prepare_mps_and_solve_with_switch(G, in_file, n_partitions, n_new_edges, algo_type, switch_ratio, switch_type)   
        print "prepare_mps_and_solve_add_edges_first - DONE, elapsed :", time.clock() - start
        
        mps_file = in_file[0:len(in_file)-3] + "_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + SUFFIX_NEW_DICT[algo_type] + \
                                                                                    "_switch_" + str(switch_ratio) + SUFFIX_NEW_DICT[switch_type]
        out_file = "../out/" + in_file[0:len(in_file)-3] + "_cvxopt_" + str(n_new_edges * n_partitions) + "_" + str(n_partitions) + str_n_deleted + \
                                                    SUFFIX_NEW_DICT[algo_type] + "_switch_" + str(switch_ratio) + SUFFIX_NEW_DICT[switch_type] + "_missing.out"
    else:
        print "WRONG <add_edges_first>. Exit..."
        sys.exit()

    print "mps_file =", mps_file
    print "out_file =", out_file
    
    combine_sol_files(G, n_partitions, mps_file, out_file, all_deleted_edges)
    print "combine_sol_files - DONE, elapsed :", time.clock() - start
    
    
    



    
    