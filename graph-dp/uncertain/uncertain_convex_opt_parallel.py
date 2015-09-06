'''
Created on Mar 28, 2014

@author: huunguye
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
from uncertain_convex_opt import random_new_edges, nearby_new_edges, unique_new_edges, randwalk_new_edges, max_variance_opt, get_representative_graph, prepare_QP_MPS_file
from graph_generator import convert_to_metis_file
from check_structure import compute_triad_weights
from entropy_obfuscation import compute_deg_index, uniqueness, sample_on_list
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
# hier_size = [1 2 2 5 5], prod = 100
# recursive !
# ERROR due to 1-based index of METIS !!!!
def prepare_jobs_topdown(G, root_name, hier_size, i=1, j=1):
    
#    if i == 1:
#        data_name = in_file[0:len(in_file)-3]
#        root_name = "../data/" + data_name
    
    if i == len(hier_size):
        return
    
    # 1. prepare metis file
    metis_file = root_name + "." + str(j) 
    print "metis_file =", metis_file
    
    convert_to_metis_file(G, metis_file)
    print "convert_to_metis_file : DONE"
    
    # 2. create part file
    start = time.clock()
    call(["../gpmetis.exe", metis_file, str(hier_size[i])], shell=False)
    print "call GPMATIS: DONE, elapsed :", time.clock() - start

    part_file = metis_file + ".part." + str(hier_size[i])
    print "part_file =", part_file
    
    # 3. read part_file to sub graphs
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, hier_size[i])

    # update root_name
    root_name = root_name + "." + str(hier_size[i])
    
    # call RECURSIVELY
    for k in range(hier_size[i]):
        sG = graph_list[k]
        prepare_jobs_topdown(sG, root_name, hier_size, i+1, k+1)
        
    
#######################################################
def prepare_jobs_minimum_tree(G, in_file, n_partitions):
    aG = nx.Graph()      #
    deg_dict = G.degree()
    for e in G.edges_iter():
        u = e[0]
        v = e[1]
        aG.add_edge(u, v, {'w':abs(deg_dict[u]-deg_dict[v])})   # weight = degree discrepancy
        
    # minimum spanning tree
    mst = nx.minimum_spanning_edges(aG, weight='w', data=False)   # do not return weights
    
    T = nx.Graph()
    T.add_edges_from(mst)
    
    # partition T by METIS
    metis_file = "../data/" + in_file[0:len(in_file)-3] + "_mst.metis"
    print "metis_file =", metis_file
    
    convert_to_metis_file(T, metis_file)    # partition T
          
    n_nodes = G.number_of_nodes()
    
    start = time.clock()
    call(["../gpmetis.exe", metis_file, str(n_partitions)], shell=False)
    print "call GPMATIS: DONE, elapsed :", time.clock() - start
    
    #
    part_file = "../data/" + in_file[0:len(in_file)-3] + "_mst.metis.part." + str(n_partitions)
    print "part_file =", part_file
    
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)
    
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
# use METIS, compute triads for new edges
def prepare_jobs_metis_triads(G, in_file, n_partitions, n_new_edges):

    # compute triad_count
    triad_count = compute_triad_weights(G)
    
    triad_list = [(k, v) for (k, v) in triad_count.iteritems()]
    triad_list = sorted(triad_list, key=lambda item:item[1])
    
    # select n_new_edges smallest edges in triad_list
    new_edges = [triad_list[i][0] for i in range(n_new_edges)]
    
    # add new_edges to aG !
    aG = nx.Graph()
    aG.add_edges_from(G.edges_iter())
    aG.add_edges_from(new_edges)
    
    # convert to METIS file
    metis_file = "../data/" + in_file[0:len(in_file)-3] + "_triad_" + str(n_new_edges) + ".metis"        # suffix _triad + str(n_new_edges)
    print "metis_file =", metis_file
    
    convert_to_metis_file(aG, metis_file)    # partition aG
    
    # partition aG
    start = time.clock()
    call(["../gpmetis.exe", metis_file, str(n_partitions)], shell=False)
    print "call GPMATIS: DONE, elapsed :", time.clock() - start

    #
    part_file = "../data/" + in_file[0:len(in_file)-3] + "_triad_" + str(n_new_edges) + ".metis.part." + str(n_partitions)    # suffix _triad + str(n_new_edges)
    print "part_file =", part_file
    
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)     # IMPORTANT: use G, not aG !!
    
    # compute missing edges in aG/G
    in_subgraph_edges = [[] for i in range(n_partitions)]   # list of lists
    missing_edges = []
    for e in new_edges:
        found = False
        for i in range(n_partitions):
            sG = graph_list[i]
            if sG.has_node(e[0]) and sG.has_node(e[1]):
                in_subgraph_edges[i].append(e)
                found = True
                break
        if not found:
            missing_edges.append(e) 
        
    #    
    return graph_list, in_subgraph_edges, missing_edges

#######################################################
# use METIS, BLOSSOM_5, compute triads for new edges
def prepare_jobs_metis_triads_blossom(G, in_file, n_partitions, n_new_edges):

    # compute triad_count
    triad_count = compute_triad_weights(G)
    
    # write to file .triad
    triad_file = "../data/" + in_file[0:len(in_file)-3] + ".triad"
    triad_file_out = triad_file + "_out"
    f = open(triad_file, "w")
    f.write("%d %d\n"%(G.number_of_nodes(), len(triad_count)))
    for (k, v) in triad_count.iteritems():
        f.write("%d %d %d\n"%(k[0], k[1], v))
    f.close()
    print "write to triad file: DONE"
    
    # call blossom5.exe
    start = time.clock()
    call(["../blossom5.exe", "-e", triad_file, "-w", triad_file_out], shell=False)
    print "call BLOSSOM_5: DONE, elapsed :", time.clock() - start
    
    # read triad_file_out and add edges to aG
    aG = nx.Graph()    
    aG.add_edges_from(G.edges_iter())
    
    new_edges = []
    f = open(triad_file_out, "r")
    fstr = f.read()
    f.close()
    for line in fstr.split("\n"):
        if line != '':
            items = line.split(" ")
            new_edges.append( (int(items[0]), int(items[1])) )
    del new_edges[0]    # remove the first pair
    
    aG.add_edges_from(new_edges)
    
    # convert to METIS file
    metis_file = "../data/" + in_file[0:len(in_file)-3] + "_triad_blossom_" + str(n_new_edges) + ".metis"        # suffix _triad + str(n_new_edges)
    print "metis_file =", metis_file
    
    convert_to_metis_file(aG, metis_file)    # partition aG
    
    # partition aG
    start = time.clock()
    call(["../gpmetis.exe", metis_file, str(n_partitions)], shell=False)
    print "call GPMATIS: DONE, elapsed :", time.clock() - start

    #
    part_file = "../data/" + in_file[0:len(in_file)-3] + "_triad_blossom_" + str(n_new_edges) + ".metis.part." + str(n_partitions)    # suffix _triad + str(n_new_edges)
    print "part_file =", part_file
    
    graph_list, part_dict = read_partitions_from_part_files(G, part_file, n_partitions)     # IMPORTANT: use G, not aG !!
    
    # compute missing edges in aG/G
    in_subgraph_edges = [[] for i in range(n_partitions)]   # list of lists
    missing_edges = []
    for e in new_edges:
        found = False
        for i in range(n_partitions):
            sG = graph_list[i]
            if sG.has_node(e[0]) and sG.has_node(e[1]):
                in_subgraph_edges[i].append(e)
                found = True
                break
        if not found:
            missing_edges.append(e) 
        
    #    
    return graph_list, in_subgraph_edges, missing_edges

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
        elif random_or_nearby == UNIQUE_NEW:     # 2: UNIQUE_NEW
            new_edges = unique_new_edges(aG, n_new_edges, sigma=0.1, eps=0.1)
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
    elif random_or_nearby == UNIQUE_NEW:     # 2: UNIQUE_NEW
        new_edges = unique_new_edges(G, n_new_edges * n_partitions, sigma=0.1, eps=0.1)
    elif random_or_nearby == RDWALK_NEW:     # 3: RDWALK_NEW    
        new_edges = randwalk_new_edges(G, n_new_edges * n_partitions, t=2, M=10)
    else:
        print "WRONG param <random_or_nearby>"
        return
    
    aG = nx.Graph()
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
        elif random_or_nearby == UNIQUE_NEW:     # 2: UNIQUE_NEW
            new_edges = unique_new_edges(aG, n_new_edges, sigma=0.1, eps=0.1)
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
    elif random_or_nearby == 2:     # 2: UNIQUE_NEW
        new_edges = unique_new_edges(aG, n_new, sigma=0.1, eps=0.1)
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
def combine_and_find_representative(data_name, n_partitions, n_new):
    G = nx.Graph()
    for i in range(n_partitions):
        filename = "../part/" + data_name + "_out." + str(i)
        aG = nx.read_edgelist(filename, '#', '\t', None, nodetype=int, data=True) 
        G.add_edges_from(aG.edges_iter(data=True))
    
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges() 
    
    #
    out_file = "../out/" + data_name + "_cvxopt_" + str(n_new) + ".out"
    print "out_file =", out_file
    nx.write_edgelist(G, out_file, '#', '\t', True, 'utf-8')    # data=True
    print "combine subgraphs and write to file: DONE"
    
    #
    after_file = "../out/" + data_name + "_cvxopt_rep_" + str(n_new) + ".out"
    print "after_file =", after_file
    get_representative_graph(out_file, after_file)
    print "get representative graph: DONE"
    
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
    in_file = "com_dblp_ungraph.gr"     # com_dblp_ungraph (317080, 1049866)
#    in_file = "com_amazon_ungraph.gr"     # com_amazon_ungraph (334863, 925872)
#    in_file = "com_youtube_ungraph.gr"     # com_youtube_ungraph (1M, 3M)
    
    #
#    G = nx.read_edgelist("../data/" + in_file, '#', '\t', None, nodetype=int, data=False)
#    print "read graph G - DONE"
    
    
    ### TEST prepare_jobs (1) - WITH distributing missing edges
#    graph_list, part_dict = prepare_jobs_metis(G, in_file, N_PARTITIONS)
#    
#    sum_nodes = sum(aG.number_of_nodes() for aG in graph_list)
#    sum_edges = sum(aG.number_of_edges() for aG in graph_list)
#    print "sum_nodes =", sum_nodes
#    print "sum_edges =", sum_edges
#    for aG in graph_list:
#        print aG.number_of_nodes(), "-", aG.number_of_edges()
#    print "#missing edges =", G.number_of_edges() - sum_edges
#    
#    # TEST uniform_edges_distribute
#    start = time.clock()
#    graph_list = uniform_edges_distribute(G, graph_list, part_dict)
#    print "distribute edges: DONE, elapsed :", time.clock() - start
#    
#    sum_nodes = sum(aG.number_of_nodes() for aG in graph_list)
#    sum_edges = sum(aG.number_of_edges() for aG in graph_list)
#    print "sum_nodes =", sum_nodes
#    print "sum_edges =", sum_edges
#    for aG in graph_list:
#        print aG.number_of_nodes(), "-", aG.number_of_edges()
#    print "#missing edges =", G.number_of_edges() - sum_edges
#    
#    # TEST write_subgraphs
#    write_subgraphs(graph_list, in_file)


    ### TEST prepare_jobs (2) - WITHOUT distributing missing edges
    # Type 1 - partition the graph first, then add edges to subgraphs
#    graph_list, part_dict = prepare_jobs_metis(G, in_file, N_PARTITIONS)
##    graph_list, part_dict = prepare_jobs_minimum_tree(G, in_file, N_PARTITIONS)    # MST
#
#    sum_nodes = sum(aG.number_of_nodes() for aG in graph_list)
#    sum_edges = sum(aG.number_of_edges() for aG in graph_list)
#    print "sum_nodes =", sum_nodes
#    print "sum_edges =", sum_edges
#    for aG in graph_list:
#        print aG.number_of_nodes(), "-", aG.number_of_edges()
#    print "#missing edges =", G.number_of_edges() - sum_edges
#    
#    # TEST write_subgraphs
#    in_file = "temp.gr"     # !!! NOTICE: overwrite the exact file name, (for _mst, _missing tests, etc.)
#    write_subgraphs(graph_list, in_file)  


    # Type 2 - add edges first, then partition the graph !
#    n_new_edges = 5000     # ~ 10% of G.number_of_edges()
##    n_new_edges = 10000     # ~ 20% of G.number_of_edges()
##    n_new_edges = 100000     
#    
##    graph_list, in_subgraph_edges, missing_edges = \
##            prepare_jobs_metis_triads(G, in_file, N_PARTITIONS, n_new_edges)   # use check_structure.compute_triad_weights()
#            
#    graph_list, in_subgraph_edges, missing_edges = \
#            prepare_jobs_metis_triads_blossom(G, in_file, N_PARTITIONS, n_new_edges)   # call BLOSSOM_5
#    
#    sum_nodes = sum(aG.number_of_nodes() for aG in graph_list)
#    sum_edges = sum(aG.number_of_edges() for aG in graph_list)
#    print "sum_nodes =", sum_nodes
#    print "sum_edges =", sum_edges
#    for aG in graph_list:
#        print aG.number_of_nodes(), "-", aG.number_of_edges()
#    sum_in_subgraph = sum(len(e_list) for e_list in in_subgraph_edges)
#    print "#new edges in subgraphs =", sum_in_subgraph    
#    print "#old edges in subgraphs =", sum_edges - sum_in_subgraph
#    print "#missing edges in G =", G.number_of_edges() - (sum_edges - sum_in_subgraph)
#    
#    # TEST write_subgraphs
#    in_file = "temp.gr"     # !!! NOTICE: overwrite the exact file name, (for _mst, _missing tests, etc.)
##    write_subgraphs(graph_list, in_file)                                # for Type 1
#    write_subgraphs_triads(graph_list, in_subgraph_edges, in_file)      # for Type 2
    
    
    # TEST parallel_max_variance_opt()
#    parallel_max_variance_opt(graph_list[0], 1000, 0)
    
    
    ### 1 - PARALLEL
#    in_file = "temp.gr"    # temp output files moved to outer folders !
#    
#    ppservers = ()
#    ncpus = 2
#    job_server = pp.Server(ncpus, ppservers=ppservers)
#    print "Starting pp with", job_server.get_ncpus(), "workers"
#    
##    n_new = 1500        # for 'youtube'
##    n_new = 1000        # when N_PARTITIONS = 100
#    n_new = 500         # when N_PARTITIONS = 200
##    n_new = 250         # for sm_100000_005_11
#    
#    start = time.clock()
#    # Type 1 - partition the graph first, then add edges to subgraphs
#    # NOTICE" random_new_edges() OR nearby_new_edges()
#    jobs = [(i, job_server.submit(parallel_max_variance_opt_file, 
#                                (in_file, n_new, i, UNIQUE_NEW, ),           # 0: RANDOM_NEW, 1: NEARBY_NEW, 2: UNIQUE_NEW
#                                (random_new_edges, nearby_new_edges, unique_new_edges, max_variance_opt, compute_deg_index, uniqueness, sample_on_list, ), 
#                                ("uncertain_convex_opt", "entropy_obfuscation", "networkx as nx", "math", "random", "cvxopt", "cvxopt.solvers", "numpy as np", ))) 
#                                for i in range(N_PARTITIONS)]
#
#    # Type 2 - use compute_triad_weights() - add edges first, then partition the graph
##    jobs = [(i, job_server.submit(parallel_max_variance_opt_file_no_new_edges, 
##                                (in_file, i, ),           
##                                (max_variance_opt,), 
##                                ("uncertain_convex_opt", "networkx as nx", "random", "cvxopt", "cvxopt.solvers", "numpy as np", ))) 
##                                for i in range(N_PARTITIONS)]
#    
#    results = []
#    for i, job in jobs:
#        res = job()
#        results.append(res)
#        print "DONE one subgraph !"
#    
#    print "Time elapsed: ", time.clock() - start, "s"
#    job_server.print_stats()
    
    
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
    G = nx.read_edgelist("../data/" + in_file, '#', '\t', None, nodetype=int, data=False)
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
    
    
    # TEST prepare_mps_and_solve()
#    n_new_edges = 40000  # n_new_edges for each part
#    start = time.clock()
#    # 0: RANDOM_NEW, 1: NEARBY_NEW, 2: UNIQUE_NEW, 3:RDWALK_NEW
#    prepare_mps_and_solve(G, in_file, N_PARTITIONS, n_new_edges, RDWALK_NEW)   
#    print "prepare_mps_and_solve - DONE, elapsed :", time.clock() - start
    
    
    # TEST prepare_mps_and_solve_add_edges_first()    --> rw_first_missing !
#    n_new_edges = 40000  # n_new_edges for each part
#    start = time.clock()
#    # 0: RANDOM_NEW, 1: NEARBY_NEW, 2: UNIQUE_NEW, 3:RDWALK_NEW
#    prepare_mps_and_solve_add_edges_first(G, in_file, N_PARTITIONS, n_new_edges, RDWALK_NEW)   
#    print "prepare_mps_and_solve_add_edges_first - DONE, elapsed :", time.clock() - start


    # TEST extract_subset_and_solve()
#    n_new_edges = 10000
#    start = time.clock()
#    extract_subset_and_solve(G, in_file, N_PARTITIONS, n_new_edges, NEARBY_NEW, sub_size=0.75)
#    print "extract_subset_and_solve - DONE, elapsed :", time.clock() - start
    

    start = time.clock()

#    mps_file = "er_100000_00001_100000_10_rand"
#    out_file = "../out/er_100000_00001_cvxopt_100000_10_rand_missing.out"       # random,    10s
#    mps_file = "er_100000_00001_100000_10_nb"
#    out_file = "../out/er_100000_00001_cvxopt_100000_10_nb_missing.out"       # nearby,    10s
#    mps_file = "er_100000_00001_100000_10_uq"
#    out_file = "../out/er_100000_00001_cvxopt_100000_10_uq_missing.out"       # unique,    10s

    
#    mps_file = "sm_100000_005_11_100000_10"
#    out_file = "../out/sm_100000_005_11_cvxopt_100000_10_missing.out"       # random,    10s
#    mps_file = "sm_100000_005_11_100000_10_nb"
#    out_file = "../out/sm_100000_005_11_cvxopt_100000_10_nb_missing.out"       # nearby,    10s
#    mps_file = "sm_100000_005_11_100000_10_uq"
#    out_file = "../out/sm_100000_005_11_cvxopt_100000_10_uq_missing.out"       # unique,    10s

#    mps_file = "sm_100000_005_11_50000_10"
#    out_file = "../out/sm_100000_005_11_cvxopt_50000_10_missing.out"       # random,    9s
#    mps_file = "sm_100000_005_11_50000_10_nb"
#    out_file = "../out/sm_100000_005_11_cvxopt_50000_10_nb_missing.out"       # nearby,    9s

#    mps_file = "ba_100000_5_100000_10_nb"
#    out_file = "../out/ba_100000_5_cvxopt_100000_10_nb_missing.out"       # nearby,    9s
#    mps_file = "ba_100000_5_200000_10_nb"
#    out_file = "../out/ba_100000_5_cvxopt_200000_10_nb_missing.out"       # nearby,    9s

#    mps_file = "com_dblp_ungraph_50000_10_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_50000_10_nb_missing.out"       # nearby,    24s
#    mps_file = "com_dblp_ungraph_100000_10_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_100000_10_nb_missing.out"       # nearby,    ??s
#    mps_file = "com_dblp_ungraph_100000_20_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_100000_20_nb_missing.out"       # nearby,    24s
#    mps_file = "com_dblp_ungraph_200000_5_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_5_nb_missing.out"       # nearby,    25s
#    mps_file = "com_dblp_ungraph_200000_10_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_10_nb_missing.out"       # nearby,    25s
#    mps_file = "com_dblp_ungraph_200000_20_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_nb_missing.out"       # nearby,    24s
#    mps_file = "com_dblp_ungraph_400000_20_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_400000_20_nb_missing.out"       # nearby,    29s
#    mps_file = "com_dblp_ungraph_600000_20_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_600000_20_nb_missing.out"       # nearby,    32.1s
#    mps_file = "com_dblp_ungraph_800000_20_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_800000_20_nb_missing.out"       # nearby,    36s
#    mps_file = "com_dblp_ungraph_1000000_20_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_nb_missing.out"       # nearby,    43.2s

#    mps_file = "com_dblp_ungraph_200000_20_sub_075_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_075_nb_missing.out"       # nearby, sub=0.75,   24s
#    mps_file = "com_dblp_ungraph_200000_20_sub_05_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_05_nb_missing.out"       # nearby, sub=0.5,   24s
#    mps_file = "com_dblp_ungraph_200000_20_sub_025_nb"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_025_nb_missing.out"       # nearby, sub=0.25,   24s

#    mps_file = "com_dblp_ungraph_200000_20_rand"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rand_missing.out"       # random,    23.7s
#    mps_file = "com_dblp_ungraph_400000_20_rand"
#    out_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rand_missing.out"       # random,    28.7s
#    mps_file = "com_dblp_ungraph_600000_20_rand"
#    out_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rand_missing.out"       # random,    31.7s
#    mps_file = "com_dblp_ungraph_800000_20_rand"
#    out_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rand_missing.out"       # random,    34.7s
#    mps_file = "com_dblp_ungraph_1000000_20_rand"
#    out_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rand_missing.out"       # random,    ??s

#    mps_file = "com_dblp_ungraph_200000_20_rw"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_dblp_ungraph_400000_20_rw"
#    out_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_dblp_ungraph_600000_20_rw"
#    out_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_dblp_ungraph_800000_20_rw"
#    out_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_dblp_ungraph_1000000_20_rw"
#    out_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_missing.out"       # randwalk,    27s

#    mps_file = "com_dblp_ungraph_200000_20_rw_first"
#    out_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_dblp_ungraph_400000_20_rw_first"
#    out_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_dblp_ungraph_600000_20_rw_first"
#    out_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_dblp_ungraph_800000_20_rw_first"
#    out_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_dblp_ungraph_1000000_20_rw_first"
#    out_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing.out"       # randwalk, first   27s

#    mps_file = "com_amazon_ungraph_50000_5_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_50000_5_nb_missing.out"       # nearby,    ??s
#    mps_file = "com_amazon_ungraph_50000_10_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_50000_10_nb_missing.out"       # nearby,    ??s
#    mps_file = "com_amazon_ungraph_100000_10_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_100000_10_nb_missing.out"       # nearby,    ??s
#    mps_file = "com_amazon_ungraph_100000_20_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_100000_20_nb_missing.out"       # nearby,    20s
#    mps_file = "com_amazon_ungraph_200000_10_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_200000_10_nb_missing.out"       # nearby,    ??s
#    mps_file = "com_amazon_ungraph_200000_20_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_200000_20_nb_missing.out"       # nearby,    20s
#    mps_file = "com_amazon_ungraph_400000_20_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_400000_20_nb_missing.out"       # nearby,    26s
#    mps_file = "com_amazon_ungraph_600000_20_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_600000_20_nb_missing.out"       # nearby,    30.3s
#    mps_file = "com_amazon_ungraph_800000_20_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_800000_20_nb_missing.out"       # nearby,    33.9s
#    mps_file = "com_amazon_ungraph_1000000_20_nb"
#    out_file = "../out/com_amazon_ungraph_cvxopt_1000000_20_nb_missing.out"       # nearby,    37.2s

#    mps_file = "com_amazon_ungraph_200000_20_rand"
#    out_file = "../out/com_amazon_ungraph_cvxopt_200000_20_rand_missing.out"       # random,    22s
#    mps_file = "com_amazon_ungraph_400000_20_rand"
#    out_file = "../out/com_amazon_ungraph_cvxopt_400000_20_rand_missing.out"       # random,    25.7s
#    mps_file = "com_amazon_ungraph_600000_20_rand"
#    out_file = "../out/com_amazon_ungraph_cvxopt_600000_20_rand_missing.out"       # random,    31.8s
#    mps_file = "com_amazon_ungraph_800000_20_rand"
#    out_file = "../out/com_amazon_ungraph_cvxopt_800000_20_rand_missing.out"       # random,    33.6s
#    mps_file = "com_amazon_ungraph_1000000_20_rand"
#    out_file = "../out/com_amazon_ungraph_cvxopt_1000000_20_rand_missing.out"       # random,    36.9s

#    mps_file = "com_amazon_ungraph_200000_20_rw"
#    out_file = "../out/com_amazon_ungraph_cvxopt_200000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_amazon_ungraph_400000_20_rw"
#    out_file = "../out/com_amazon_ungraph_cvxopt_400000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_amazon_ungraph_600000_20_rw"
#    out_file = "../out/com_amazon_ungraph_cvxopt_600000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_amazon_ungraph_800000_20_rw"
#    out_file = "../out/com_amazon_ungraph_cvxopt_800000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_amazon_ungraph_1000000_20_rw"
#    out_file = "../out/com_amazon_ungraph_cvxopt_1000000_20_rw_missing.out"       # randwalk,    27s

#    mps_file = "com_amazon_ungraph_200000_20_rw_first"
#    out_file = "../out/com_amazon_ungraph_cvxopt_200000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_amazon_ungraph_400000_20_rw_first"
#    out_file = "../out/com_amazon_ungraph_cvxopt_400000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_amazon_ungraph_600000_20_rw_first"
#    out_file = "../out/com_amazon_ungraph_cvxopt_600000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_amazon_ungraph_800000_20_rw_first"
#    out_file = "../out/com_amazon_ungraph_cvxopt_800000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_amazon_ungraph_1000000_20_rw_first"
#    out_file = "../out/com_amazon_ungraph_cvxopt_1000000_20_rw_first_missing.out"       # randwalk, first   27s

#    mps_file = "com_youtube_ungraph_300000_30_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_300000_30_nb_missing.out"       # nearby,    63s
#    mps_file = "com_youtube_ungraph_300000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_300000_60_nb_missing.out"       # nearby,    63s
#    mps_file = "com_youtube_ungraph_420000_30_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_420000_30_nb_missing.out"       # nearby,    66s
#    mps_file = "com_youtube_ungraph_420000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_420000_60_nb_missing.out"       # nearby,    68s
#    mps_file = "com_youtube_ungraph_600000_60"
#    out_file = "../out/com_youtube_ungraph_cvxopt_600000_60_missing.out"       # random,    70s
#    mps_file = "com_youtube_ungraph_600000_30_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_600000_30_nb_missing.out"       # nearby,    71s
#    mps_file = "com_youtube_ungraph_600000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out"       # nearby,    71s
#    mps_file = "com_youtube_ungraph_1200000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out"       # nearby,    80s
#    mps_file = "com_youtube_ungraph_1800000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out"       # nearby,    92.6s
#    mps_file = "com_youtube_ungraph_2400000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out"       # nearby,    103.5s
#    mps_file = "com_youtube_ungraph_3000000_60_nb"
#    out_file = "../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out"       # nearby,    118.1s
#    mps_file = "com_youtube_ungraph_600000_60_rand"

#    mps_file = "com_youtube_ungraph_600000_60_rand"
#    out_file = "../out/com_youtube_ungraph_cvxopt_600000_60_rand_missing.out"       # random,    65.4s
#    mps_file = "com_youtube_ungraph_1200000_60_rand"
#    out_file = "../out/com_youtube_ungraph_cvxopt_1200000_60_rand_missing.out"       # random,    78.8s
#    mps_file = "com_youtube_ungraph_1800000_60_rand"
#    out_file = "../out/com_youtube_ungraph_cvxopt_1800000_60_rand_missing.out"       # random,    97.1s
#    mps_file = "com_youtube_ungraph_2400000_60_rand"
#    out_file = "../out/com_youtube_ungraph_cvxopt_2400000_60_rand_missing.out"       # random,    110.4s
#    mps_file = "com_youtube_ungraph_3000000_60_rand"
#    out_file = "../out/com_youtube_ungraph_cvxopt_3000000_60_rand_missing.out"       # random,    138.9s

#    mps_file = "com_youtube_ungraph_200000_20_rw"
#    out_file = "../out/com_youtube_ungraph_cvxopt_200000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_youtube_ungraph_400000_20_rw"
#    out_file = "../out/com_youtube_ungraph_cvxopt_400000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_youtube_ungraph_600000_20_rw"
#    out_file = "../out/com_youtube_ungraph_cvxopt_600000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_youtube_ungraph_800000_20_rw"
#    out_file = "../out/com_youtube_ungraph_cvxopt_800000_20_rw_missing.out"       # randwalk,    27s
#    mps_file = "com_youtube_ungraph_1000000_20_rw"
#    out_file = "../out/com_youtube_ungraph_cvxopt_1000000_20_rw_missing.out"       # randwalk,    27s

#    mps_file = "com_youtube_ungraph_200000_20_rw_first"
#    out_file = "../out/com_youtube_ungraph_cvxopt_200000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_youtube_ungraph_400000_20_rw_first"
#    out_file = "../out/com_youtube_ungraph_cvxopt_400000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_youtube_ungraph_600000_20_rw_first"
#    out_file = "../out/com_youtube_ungraph_cvxopt_600000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_youtube_ungraph_800000_20_rw_first"
#    out_file = "../out/com_youtube_ungraph_cvxopt_800000_20_rw_first_missing.out"       # randwalk, first   27s
#    mps_file = "com_youtube_ungraph_1000000_20_rw_first"
#    out_file = "../out/com_youtube_ungraph_cvxopt_1000000_20_rw_first_missing.out"       # randwalk, first   27s
    
#    combine_sol_files(G, N_PARTITIONS, mps_file, out_file)
#    print "combine_sol_files - DONE, elapsed :", time.clock() - start
    
    
    
    # TEST get_representative_graph()
#    out_name = "com_dblp_ungraph_entropy_001_2_001"             # entropy
#    
##    out_name = "com_dblp_ungraph_cvxopt_200000_nb_missing"        # cvxopt
#    
#    rep_type = REP_ABM
#    out_file = "../out/" + out_name + ".out"
#    rep_file = "../rep/" + out_name + "." + REP_DICT[rep_type] + ".rep"    # GP or ADR or ABM representative ENTROPY(70s, 78s, 76s)    CVXOPT(44s, 56s, 49s)
#    
#    print "out_file =", out_file
#    print "rep_file =", rep_file
#    start = time.clock()
#    get_representative_graph(out_file, rep_file, rep_type)
#    print "get representative graph: DONE, elapsed:", time.clock() - start
    
    
    
    # TEST combine_temp_files (only for PARALLEL)
#    start = time.clock()
##    combine_temp_files(G, 10, "../out/er_10000_0001_mst_cvxopt_10000_missing.out")          # MST
##    combine_temp_files(G, 10, "../out/er_10000_0001_cvxopt_10000_nb_missing.out")        # nearby
##    combine_temp_files(G, 10, "../out/er_10000_0001_cvxopt_10000_missing.out")        
##    combine_temp_files(G, 10, "../out/er_10000_0001_cvxopt_5000_missing.out")
##    combine_temp_files(G, 10, "../out/er_10000_0001_cvxopt_10000_10_triad_missing.out")       # triad
##    combine_temp_files(G, 10, "../out/er_10000_0001_cvxopt_5000_10_triad_missing.out")       # triad
##    combine_temp_files(G, 10, "../out/er_10000_0001_cvxopt_5000_10_triad_blossom_missing.out")       # triad
##    combine_temp_files(G, 10, "../out/sm_10000_005_11_cvxopt_10000_missing.out")
##    combine_temp_files(G, 10, "../out/sm_10000_005_11_cvxopt_10000_10_triad_missing.out")       # triad
#
##    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_100000_nb_missing.out")            # nearby    ,15s
##    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_100000_missing.out")              # random    ,15s
##    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_100000_uq_missing.out")              # unique    ,14s
##    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_50000_nb_missing.out")              # nearby,      12s
##    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_50000_missing.out")       # 12s
#    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_50000_uq_missing.out")              # unique,      12s
##    combine_temp_files(G, 100, "../out/er_100000_00001_cvxopt_100000_100_triad_missing.out")     # triad, 100 parts, 19s
#
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_25000_missing.out")     # random,    17s
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_50000_missing.out")     # random,    18s
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_50000_nb_missing.out")     # nearby,    18s
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_100000_missing.out")     # random,    19s
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_100000_nb_missing.out")     # nearby,    20s
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_100000_uq_missing.out")     # unique,     19s    
##    combine_temp_files(G, 200, "../out/sm_100000_005_11_cvxopt_200000_200_nb_missing.out")     # 200 parts, 24s
##    combine_temp_files(G, 200, "../out/sm_100000_005_11_cvxopt_100000_200_nb_missing.out")     # 200 parts, 19s
##    combine_temp_files(G, 100, "../out/sm_100000_005_11_cvxopt_100000_100_triad_missing.out")     # triad, 100 parts, 19s
#
##    combine_temp_files(G, 100, "../out/ba_100000_5_cvxopt_100000_nb_missing.out")            # nearby    ,15s
##    combine_temp_files(G, 100, "../out/ba_100000_5_cvxopt_100000_missing.out")              # 15s
##    combine_temp_files(G, 100, "../out/ba_100000_5_cvxopt_50000_nb_missing.out")              # nearby,      12s
##    combine_temp_files(G, 100, "../out/ba_100000_5_cvxopt_50000_missing.out")       # 12s
#
##    combine_temp_files(G, 200, "../out/com_dblp_ungraph_cvxopt_200000_nb_missing.out")     # 44s, nearby
##    combine_temp_files(G, 200, "../out/com_dblp_ungraph_cvxopt_200000_missing.out")     # 46s, random
##    combine_temp_files(G, 200, "../out/com_dblp_ungraph_cvxopt_100000_nb_missing.out")     # 40s, nearby
#
##    combine_temp_files(G, 600, "../out/com_youtube_ungraph_cvxopt_600000_600_missing.out")     #  ??s, random
##    combine_temp_files(G, 600, "../out/com_youtube_ungraph_cvxopt_900000_600_missing.out")     #  ??s, random    
#    print "DONE - elapsed :", time.clock() - start
    
    
    # TEST combine_and_find_representative
#    combine_and_find_representative("er_10000_0001", 10, 10000)
#    combine_and_find_representative("sm_10000_005_11", 10, 10000)
#    combine_and_find_representative("ba_10000_5", 10, 10000)
#    combine_and_find_representative("pl_10000_5_01", 10, 10000)
#    combine_and_find_representative("ff_10000_05_connected", 10, 10000)
    
#    # TEST export_to_MATLAB (for histogram of edge probabilities)
#    export_to_MATLAB("../out/er_1000_001_cvxopt_1000.out", "D:/er_1000_001.mat")
#    export_to_MATLAB("../out/er_10000_0001_cvxopt_10000.out", "D:/er_10000_0001.mat")
#    export_to_MATLAB("../out/er_1000_001_entropy_01_2_001.out", "D:/er_1000_001_entropy_01_2_001.mat")
#    export_to_MATLAB("../out/sm_10000_005_11_cvxopt_10000.out", "D:/sm_10000_005_11_cvxopt_10000.mat")
#    export_to_MATLAB("../out/ba_10000_5_cvxopt_10000.out", "D:/ba_10000_5_cvxopt_10000.mat")
#    export_to_MATLAB("../out/pl_10000_5_01_cvxopt_10000.out", "D:/pl_10000_5_01_cvxopt_10000.mat")
#    export_to_MATLAB("../out/er_10000_0001_cvxopt_10000_missing.out", "D:/er_10000_0001_cvxopt_10000_missing.mat")
#    export_to_MATLAB("../out/sm_100000_005_11_cvxopt_200000_200_nb_missing.out", "C:/sm_100000_005_11_cvxopt_200000_200_nb_missing.mat")
#    export_to_MATLAB("../out/sm_100000_005_11_cvxopt_100000_10_missing.out", "C:/sm_100000_005_11_cvxopt_100000_10_missing.mat")
#    export_to_MATLAB("../out/sm_100000_005_11_cvxopt_50000_10_missing.out", "C:/sm_100000_005_11_cvxopt_50000_10_missing.mat")
#    export_to_MATLAB("../out/com_dblp_ungraph_entropy_001_2_001.out", "C:/com_dblp_ungraph_entropy_001_2_001.mat")
#    export_to_MATLAB("../out/com_dblp_ungraph_entropy_01_2_001.out", "C:/com_dblp_ungraph_entropy_01_2_001.mat")
#    export_to_MATLAB("../out/com_dblp_ungraph_cvxopt_100000_nb_missing.out", "C:/com_dblp_ungraph_cvxopt_100000_nb_missing.mat")
#    export_to_MATLAB("../out/com_dblp_ungraph_cvxopt_200000_nb_missing.out", "C:/com_dblp_ungraph_cvxopt_200000_nb_missing.mat")
    
    
    # TEST prepare_jobs_topdown - ERROR due to 1-based index of METIS !!!!
#    data_name = in_file[0:len(in_file)-3]
#    root_name = "../data/" + data_name + "_metis"
#    prepare_jobs_topdown(G, root_name, [1, 2, 2, 5, 5], 1, 1)

    # CHECK error parts
#    in_file = "temp.gr"
#    parallel_max_variance_opt_file_no_new_edges(in_file, 98)    # er_100000_00001_cvxopt_100000_100_triad_missing
#    print "DONE"



    
    