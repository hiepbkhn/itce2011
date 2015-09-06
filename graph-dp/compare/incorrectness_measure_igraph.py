'''
Created on Jun 2, 2014

@author: huunguye
'''

import sys
import time
from random import *
import math
import networkx as nx
import igraph as ig
import scipy.io
from compare_igraph import read_two_graphs
#from graph_generator import generate_sample
from attack_structure_igraph import equivalence_class_H1, equivalence_class_H2_closed, equivalence_class_H2_open, bucket_H2, list_comparator
from entropy_obfuscation_igraph import compute_eps, compute_eps_multi

#N_SAMPLES = 100
#N_SAMPLES = 10
N_SAMPLES = 5

AT_UNCERTAIN = 0
AT_SWITCH = 1
AT_RANDOM_WALK = 2

#######################################################
# incorrectness score = sum of reidentification probabilities
def incorrectness_H1(bG, aG, bins):
    
    cand_size, bin_size, sig_list_b, bucket_list_b = equivalence_class_H1(bG, bins)
    cand_size, bin_size, sig_list_a, bucket_list_a = equivalence_class_H1(aG, bins)
    
    # compute incorrectness score
    re_prob_dict = {}       # re_prob_dict[u] = reidentification probability of u
    
    for sig_b in sig_list_b:
        
        # 1 - binary search in sig_list_a 
        lo = 0                                          
        hi = len(sig_list_a)-1
        while True:
            mid = (lo+hi)/2
            sig_a = sig_list_a[mid]
            #
            if sig_a == sig_b:                          
                set_a = set(bucket_list_a[sig_a])
                for u in bucket_list_b[sig_b]:  
                    if u in set_a:
                        re_prob_dict[u] = 1.0/len(set_a)
                    else:
                        re_prob_dict[u] = 0.0
                break
            #
            if sig_b < sig_a:
                hi = mid-1
                if hi < lo:
                    break
            if sig_b > sig_a:
                lo = mid+1
                if lo > hi:
                    break
            
        
        # 2 - linear search in sig_list_a 
#        for sig_a in sig_list_a:
#            if sig_a == sig_b:                          # need more effective Binary Search
#                set_a = set(bucket_list_a[sig_a])
#                for u in bucket_list_b[sig_b]:  
#                    if u in set_a:
#                        re_prob_dict[u] = 1.0/len(set_a)
#                    else:
#                        re_prob_dict[u] = 0.0
#                break
        
    #
    sum_re_prob = sum(re_prob_dict.itervalues())
    return sum_re_prob, re_prob_dict


#######################################################
# incorrectness score = sum of reidentification probabilities
# Open World
def incorrectness_H2_open(aG, sig_list_b, bucket_list_b, bins):
    
    h2_list = equivalence_class_H2_open(aG, None)
    cand_size, bin_size, sig_list_a, bucket_list_a = bucket_H2(h2_list, bins)
#    print "len A:", len(sig_list_a), len(bucket_list_a)
    
    # compute incorrectness score
    re_prob_dict = {}       # re_prob_dict[u] = reidentification probability of u
    for id_b in range(len(sig_list_b)):
        sig_b = sig_list_b[id_b]
        
        # 1 - binary search in sig_list_a 
        lo = 0                                          
        hi = len(sig_list_a)-1
        while True:
            mid = (lo+hi)/2
            sig_a = sig_list_a[mid]
            #
            if list_comparator(sig_a, sig_b) == 0:
                set_a = set(bucket_list_a[mid])
                for u in bucket_list_b[id_b]:  
                    if u in set_a:
                        re_prob_dict[u] = 1.0/len(set_a)
                    else:
                        re_prob_dict[u] = 0.0
                break
            #
            if list_comparator(sig_b, sig_a) < 0:
                hi = mid-1
                if hi < lo:
                    break
            if list_comparator(sig_b, sig_a) > 0:
                lo = mid+1
                if lo > hi:
                    break
            
        # 2 - linear search in sig_list_a 
#        for id_a in range(len(sig_list_a)):            
#            sig_a = sig_list_a[id_a]
#            if list_comparator(sig_a, sig_b) == 0:      # need more effective Binary Search
#                set_a = set(bucket_list_a[id_a])
#                for u in bucket_list_b[id_b]:  
#                    if u in set_a:
#                        re_prob_dict[u] = 1.0/len(set_a)
#                    else:
#                        re_prob_dict[u] = 0.0
#                break
        
    #
    sum_re_prob = sum(re_prob_dict.itervalues())
    return sum_re_prob, re_prob_dict

#######################################################
# G: uncertain
def incorrectness_uncertain_from_file(before_file, after_file, sample_file, n_samples, bins): 
    
    # compute sig_list_b, bucket_list_b ONCE !
    start = time.clock()
    bG = ig.Graph.Read_Edgelist(before_file, directed=False)
#    G = nx.read_edgelist(after_file, '#', '\t', None, nodetype=int, data=True)
    print "read bG: DONE, elapsed :", time.clock() - start
    
    h2_list = equivalence_class_H2_open(bG, None)
    cand_size, bin_size, sig_list_b, bucket_list_b = bucket_H2(h2_list, bins)
#    print "len B:", len(sig_list_b), len(bucket_list_b)
    
    # H1 score, H2 score
    start = time.clock()
    score_H1 = 0.0
    score_H2 = 0.0
    count = 0
    for i in range(n_samples):
        file_name = sample_file + str(i)
        aG = ig.Graph.Read_Edgelist(file_name, directed=False)
        print "aG.#edges =", aG.ecount()
        
        # H1
        sum_re_prob, re_prob_dict = incorrectness_H1(bG, aG, bins)
        score_H1 += sum_re_prob
        # H2
        sum_re_prob, re_prob_dict = incorrectness_H2_open(aG, sig_list_b, bucket_list_b, bins)
        score_H2 += sum_re_prob
        print "count =", count
        count += 1
    #
    score_H1 = score_H1/n_samples
    score_H2 = score_H2/n_samples
    print "compute score_H1, score_H2: DONE, elapsed :", time.clock() - start
    
    # 
    return score_H1, score_H2

#######################################################
def compare_edges_diff_one(bG, aG):
    diff_b = 0
    for e in bG.es:
        if aG.get_eid(e.source,e.target, directed=False, error=False) == -1:
            diff_b += 1
    diff_a = 0
    for e in aG.es:
        if bG.get_eid(e.source,e.target, directed=False, error=False) == -1:
            diff_a += 1
    #
    return (diff_b, diff_a)
    
#######################################################
def compare_edges_diff_from_file(before_file, after_file, sample_file, n_samples):
    # before_file
    bG = ig.Graph.Read_Edgelist(before_file, directed=False)
    
    # after_file
    diff_b_list = []
    diff_a_list = []
    for i in range(n_samples):
        file_name = sample_file + str(i)
        aG = ig.Graph.Read_Edgelist(file_name, directed=False)
        
        if aG.vcount() < bG.vcount():
            aG.add_vertices(bG.vcount() - aG.vcount())
        
        (diff_b, diff_a) = compare_edges_diff_one(bG, aG)
        print "compare sample", i, " DONE"
        #
        diff_b_list.append(diff_b)
        diff_a_list.append(diff_a)
    #
    return diff_b_list, diff_a_list

#######################################################
#
def read_uncertain_graph(file_name, n_nodes):
    f = open(file_name, 'r')
    fstr = f.read()
    f.close()
    
    #
    edge_list = []
    p_list = []     # probabilities
    for line in fstr.split("\n"):
        if line != '':
            items = line.split("\t")
            u = int(items[0])
            v = int(items[1])
            edge_list.append((u,v))
            p_list.append(float(items[2][6:-1]))
    # debug    
    sum_p = sum(p_list)
    print "sum_p =", sum_p
    
    # check
#    print edge_list[0], p_list[0]
    
    aG = ig.Graph()
    aG.add_vertices(n_nodes)
    aG.add_edges(edge_list)
    e_id = 0    
    for e in aG.es:    
        e['p'] = p_list[e_id]
        e_id += 1 
    # 
    return aG 
    
#######################################################
# compute eps for a given k
# data = True (uncertain), False (deterministic)
def k_obfuscation_measure(before_file, after_file, n_nodes, k_arr, data=True):
    print "n_nodes =", n_nodes
    
    # before_file
    bG = ig.Graph.Read_Edgelist(before_file, directed=False)
    print "read bG - DONE"
    
#    if bG.vcount() < n_nodes:
#        bG.add_vertices(n_nodes)       # only for er_100k

    # Case 1 - aG = bG
    if after_file == before_file:      # after_file is before_file
        for e in bG.es:
            e['p'] = 1.0
        return compute_eps_multi(bG, bG, k_arr) 
        
    # Case 2 - aG is a sample
    # after_file
    if data == True:
#        G = nx.read_edgelist(after_file, '#', '\t', None, nodetype=int, data=True)  # use networkx to read 'p' 
#        aG = ig.Graph()
#        aG.add_vertices(G.number_of_nodes())
#        aG.add_edges(G.edges_iter(data=False))
#        p_list = [e[2]['p'] for e in G.edges_iter(data=True)]
#        e_id = 0    
#        for e in aG.es:    
#            e['p'] = p_list[e_id]
#            e_id += 1
        #
        aG = read_uncertain_graph(after_file, bG.vcount())
 
    else:
        aG = ig.Graph.Read_Edgelist(after_file, directed=False)
        if aG.vcount() < n_nodes:
            aG.add_vertices(n_nodes - aG.vcount())       # only for the cases of KeyError !
        for e in aG.es:
            e['p'] = 1.0
    print "read aG - DONE"
    
    return compute_eps_multi(bG, aG, k_arr) 

#######################################################
# compute eps for a given k
def k_obfuscation_measure_from_file(before_file, after_file, sample_file, n_nodes, k_arr, n_samples=5):
    # before_file
    bG = ig.Graph.Read_Edgelist(before_file, directed=False)
    print "read bG - DONE"
    
#    if bG.vcount() < n_nodes:
#        bG.add_vertices(n_nodes)       # only for er_100k

    # Case 1 - aG = bG
    if after_file == before_file:      # after_file is before_file
        for e in bG.es:
            e['p'] = 1.0
        return compute_eps_multi(bG, bG, k_arr) 
        
    # Case 2 - aG is a sample
    # after_file
    eps_arr_list = []
    for i in range(n_samples):
        file_name = sample_file + str(i)
        aG = ig.Graph.Read_Edgelist(file_name, directed=False)
        print "read sample", i, " DONE"
    
        if aG.vcount() < bG.vcount():
            aG.add_vertices(bG.vcount() - aG.vcount())      # only for er_100k
        #
        for e in aG.es:
            e['p'] = 1.0

        eps_arr = compute_eps_multi(bG, aG, k_arr)
#        print eps_arr
        eps_arr_list.append(eps_arr)
        
    # average eps_arr
    avg_eps_arr = []
    for j in range(len(k_arr)):
        s = 0.0
        for i in range(n_samples):
            s+= eps_arr_list[i][j]
        
        avg_eps_arr.append(s/n_samples)
    
    #
    return avg_eps_arr

#######################################################
if __name__ == '__main__':
    
    ##
#    before_file = "../data/er_200_002.gr"
#    before_file = "../data/er_200_02.gr"
#    before_file = "../data/er_1000_005.gr"
#    before_file = "../data/er_1000_001.gr"
#    before_file = "../data/ff_1000_05.gr"

    ## 10k nodes
#    before_file = "../data/er_10000_0001.gr"
#    before_file = "../data/sm_10000_005_11.gr"

    ## 100k nodes
#    before_file = "../data/er_100000_00001.gr"
#    before_file = "../data/sm_100000_005_11.gr"
#    before_file = "../data/ba_100000_5.gr"

    ## real graphs
    before_file = "../data/com_dblp_ungraph.gr"
#    before_file = "../data/com_amazon_ungraph.gr"
#    before_file = "../data/com_youtube_ungraph.gr"


    ## privacy score of the original graph --> call (1)
#    after_file = before_file


    #### DETERMINISTIC G
    ## entropy-based (obfuscation) - post-processing
#    after_file = "../out/er_1000_001_entropy_post_01_2_001.out"
#    after_file = "../out/er_1000_001_entropy_post_05_2_001.out"
#    after_file = "../out/er_1000_001_entropy_post_10_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_post_01_2_001.out"
#    after_file = "../out/ff_1000_05_entropy_post_01_2_001.out"
#    after_file = "../out/ff_1000_05_entropy_post_05_2_001.out"
#    after_file = "../out/ff_1000_05_entropy_post_10_2_001.out"

    # 10k nodes
#    after_file = "../out/er_10000_0001_entropy_post_01_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_post_05_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_post_10_2_001.out"

    # 100k nodes
#    after_file = "../out/er_100000_00001_entropy_post_01_2_001.out"
#    after_file = "../out/sm_100000_005_11_entropy_post_01_2_001.out"

    ## switch (random, nearby)
#    after_file = "../switch/ba_100000_5_switch_rand_50000.out"
#    after_file = "../switch/ba_100000_5_switch_rand_100000.out"
#    after_file = "../switch/ba_100000_5_switch_rand_150000.out"    

    # real graphs
#    after_file = "../switch/com_dblp_ungraph_switch_rand_100000.out"    
#    after_file = "../switch/com_dblp_ungraph_switch_rand_200000.out"   
#    after_file = "../switch/com_dblp_ungraph_switch_rand_300000.out"   
#    after_file = "../switch/com_dblp_ungraph_switch_rand_400000.out"   
#    after_file = "../switch/com_dblp_ungraph_switch_rand_500000.out"   
    
#    after_file = "../switch/com_dblp_ungraph_switch_nb_100000.out"    
#    after_file = "../switch/com_dblp_ungraph_switch_nb_200000.out"   
#    after_file = "../switch/com_dblp_ungraph_switch_nb_300000.out"   
#    after_file = "../switch/com_dblp_ungraph_switch_nb_400000.out"   
#    after_file = "../switch/com_dblp_ungraph_switch_nb_500000.out" 
    
#    after_file = "../switch/com_amazon_ungraph_switch_rand_100000.out"    
#    after_file = "../switch/com_amazon_ungraph_switch_rand_200000.out"   
#    after_file = "../switch/com_amazon_ungraph_switch_rand_300000.out"   
#    after_file = "../switch/com_amazon_ungraph_switch_rand_400000.out"   
#    after_file = "../switch/com_amazon_ungraph_switch_rand_500000.out"   
    
#    after_file = "../switch/com_amazon_ungraph_switch_nb_100000.out"    
#    after_file = "../switch/com_amazon_ungraph_switch_nb_200000.out"   
#    after_file = "../switch/com_amazon_ungraph_switch_nb_300000.out"   
#    after_file = "../switch/com_amazon_ungraph_switch_nb_400000.out"   
#    after_file = "../switch/com_amazon_ungraph_switch_nb_500000.out"    
    
#    after_file = "../switch/com_youtube_ungraph_switch_rand_300000.out"    
#    after_file = "../switch/com_youtube_ungraph_switch_rand_600000.out"   
#    after_file = "../switch/com_youtube_ungraph_switch_rand_900000.out"   
#    after_file = "../switch/com_youtube_ungraph_switch_rand_1200000.out"   
#    after_file = "../switch/com_youtube_ungraph_switch_rand_1500000.out"   
#    
#    after_file = "../switch/com_youtube_ungraph_switch_nb_300000.out"    
#    after_file = "../switch/com_youtube_ungraph_switch_nb_600000.out"   
#    after_file = "../switch/com_youtube_ungraph_switch_nb_900000.out"   
#    after_file = "../switch/com_youtube_ungraph_switch_nb_1200000.out"   
#    after_file = "../switch/com_youtube_ungraph_switch_nb_1500000.out" 

    ## convex opt.
##    after_file = "../out/er_200_002_cvxopt_rep.out"
##    after_file = "../out/er_200_02_cvxopt_rep.out"
#    after_file = "../out/er_1000_001_cvxopt_rep_1000.out"
#    after_file = "../out/er_1000_001_cvxopt_rep_1000_nb.out"    # nb: nearby
#    after_file = "../out/ff_1000_05_cvxopt_rep_1000.out"
#    after_file = "../out/ff_1000_05_cvxopt_rep_1000_nb.out"     # nb: nearby

    # 10k nodes
#    after_file = "../out/er_10000_0001_cvxopt_rep_10000.out"

    # real graphs (REPRESENTATIVE)
#    after_file = "../rep/com_dblp_ungraph_entropy_001_2_001.gp.rep"  # GP representative, 
#    after_file = "../rep/com_dblp_ungraph_entropy_001_2_001.adr.rep"  # ADR representative, 
#    after_file = "../rep/com_dblp_ungraph_entropy_001_2_001.abm.rep"  # ABM representative
    
#    after_file = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.gp.rep"  #  
#    after_file = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.adr.rep"  #  
#    after_file = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.abm.rep"  # 
     
    
    #### UNCERTAIN G
    ## entropy-based (obfuscation)
#    after_file = "../out/er_1000_001_entropy_01_2_001.out"
#    after_file = "../out/er_1000_001_entropy_05_2_001.out"
#    after_file = "../out/er_1000_001_entropy_10_2_001.out"
#    after_file = "../out/ff_1000_05_entropy_01_2_001.out"    # ff_1000_05
#    after_file = "../out/ff_1000_05_entropy_05_2_001.out"
#    after_file = "../out/ff_1000_05_entropy_10_2_001.out"

    # 10k nodes
#    after_file = "../out/er_10000_0001_entropy_001_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_01_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_05_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_10_2_001.out"

#    after_file = "../out/sm_10000_005_11_entropy_01_2_001.out"
#    after_file = "../out/sm_10000_005_11_entropy_05_2_001.out"
#    after_file = "../out/sm_10000_005_11_entropy_10_2_001.out"

    # 100k nodes
#    after_file = "../out/er_100000_00001_entropy_001_2_001.out"     # sigma = 0.01
#    after_file = "../out/er_100000_00001_entropy_01_2_001.out"
#    after_file = "../out/sm_100000_005_11_entropy_001_2_001.out"    # sigma = 0.01
#    after_file = "../out/sm_100000_005_11_entropy_01_2_001.out"
#    after_file = "../out/ba_100000_5_entropy_001_2_001.out"         # sigma = 0.01
#    after_file = "../out/ba_100000_5_entropy_01_2_001.out"

    # real graphs
#    after_file = "../out/com_dblp_ungraph_entropy_01_2_001.out"
#    after_file = "../out/com_dblp_ungraph_entropy_001_2_001.out"     # sigma = 0.01
#    after_file = "../out/com_dblp_ungraph_entropy_0001_2_001.out"     # sigma = 0.001
#    after_file = "../out/com_amazon_ungraph_entropy_01_2_001.out"
#    after_file = "../out/com_amazon_ungraph_entropy_001_2_001.out"     # sigma = 0.01
#    after_file = "../out/com_amazon_ungraph_entropy_0001_2_001.out"     # sigma = 0.001
#    after_file = "../out/com_youtube_ungraph_entropy_01_2_001.out"
#    after_file = "../out/com_youtube_ungraph_entropy_001_2_001.out"     # sigma = 0.01
#    after_file = "../out/com_youtube_ungraph_entropy_0001_2_001.out"     # sigma = 0.001

    ## convex opt. (uncertain)
#    after_file = "../out/er_1000_001_cvxopt_1000.out"
#    after_file = "../out/er_1000_001_cvxopt_1000_nb.out"
#    after_file = "../out/ff_1000_05_cvxopt_1000.out"        # ff_1000_05
#    after_file = "../out/ff_1000_05_cvxopt_1000_nb.out"

    # 10k nodes
#    after_file = "../out/er_10000_0001_cvxopt_10000.out"
#    after_file = "../out/er_10000_0001_cvxopt_10000_missing.out"    # w/o redistribute missing edges
#    after_file = "../out/er_10000_0001_cvxopt_10000_nb_missing.out"        # nearby
#    after_file = "../out/er_10000_0001_cvxopt_5000_missing.out"
#    after_file = "../out/er_10000_0001_cvxopt_10000_10_triad_missing.out"  # triad
#    after_file = "../out/er_10000_0001_cvxopt_5000_10_triad_missing.out"  # triad
#    after_file = "../out/er_10000_0001_cvxopt_5000_10_triad_blossom_missing.out"  # triad

#    after_file = "../out/sm_10000_005_11_cvxopt_10000.out"    
#    after_file = "../out/sm_10000_005_11_cvxopt_10000_missing.out"  
#    after_file = "../out/sm_10000_005_11_cvxopt_10000_10_triad_missing.out"  # triad

    # 100k nodes
#    after_file = "../out/er_100000_00001_cvxopt_100000_nb_missing.out"      # nearby
#    after_file = "../out/er_100000_00001_cvxopt_100000_missing.out"
#    after_file = "../out/er_100000_00001_cvxopt_100000_uq_missing.out"      # unique    
#    after_file = "../out/er_100000_00001_cvxopt_50000_nb_missing.out"       # nearby
#    after_file = "../out/er_100000_00001_cvxopt_50000_missing.out"
#    after_file = "../out/er_100000_00001_cvxopt_50000_uq_missing.out"      # unique    
#    after_file = "../out/er_100000_00001_cvxopt_100000_100_triad_missing.out"   # triad
#    after_file = "../out/er_100000_00001_cvxopt_100000_10_rand_missing.out"        # random, MOSEK
#    after_file = "../out/er_100000_00001_cvxopt_100000_10_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/er_100000_00001_cvxopt_100000_10_uq_missing.out"        # unique, MOSEK

#    after_file = "../out/sm_100000_005_11_cvxopt_25000_missing.out"         # random
#    after_file = "../out/sm_100000_005_11_cvxopt_50000_missing.out"         # random
#    after_file = "../out/sm_100000_005_11_cvxopt_50000_10_missing.out"        # random, MOSEK
#    after_file = "../out/sm_100000_005_11_cvxopt_50000_nb_missing.out"         # nearby 
#    after_file = "../out/sm_100000_005_11_cvxopt_50000_10_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_missing.out"        # random
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_10_rand_missing.out"        # random, MOSEK
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_10_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_10_uq_missing.out"        # unique, MOSEK
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_nb_missing.out"        # nearby
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_uq_missing.out"        # unique           
#    after_file = "../out/sm_100000_005_11_cvxopt_100000_200_nb_missing.out"    # nearby
#    after_file = "../out/sm_100000_005_11_cvxopt_200000_200_nb_missing.out"    # nearby

#    after_file = "../out/ba_100000_5_cvxopt_100000_nb_missing.out"      # nearby
#    after_file = "../out/ba_100000_5_cvxopt_100000_missing.out"
#    after_file = "../out/ba_100000_5_cvxopt_50000_nb_missing.out"       # nearby
#    after_file = "../out/ba_100000_5_cvxopt_50000_missing.out"

    # real graphs
#    after_file = "../out/com_dblp_ungraph_cvxopt_50000_10_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_100000_10_nb_missing.out"     # nearby, MOSEK    
#    after_file = "../out/com_dblp_ungraph_cvxopt_100000_20_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_100000_nb_missing.out"     # nearby        
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_nb_missing.out"    # nearby
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_10_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_5_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_400000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_600000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_800000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_missing.out"
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_075_nb_missing.out"    # nearby, sub=0.75 MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_05_nb_missing.out"    # nearby, sub=0.5 MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_025_nb_missing.out"    # nearby, sub=0.25 MOSEK

#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_missing.out"    # randwalk, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_missing.out"    # randwalk, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_missing.out"    # randwalk, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_missing.out"    # randwalk, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_missing.out"    # randwalk, MOSEK

    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing.out"    # randwalk, first MOSEK
    after_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing.out"    # randwalk, first MOSEK
    after_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing.out"    # randwalk, first MOSEK
    after_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing.out"    # randwalk, first MOSEK
    after_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing.out"    # randwalk, first MOSEK

#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rand_missing.out"    # random, MOSEK


#    after_file = "../out/com_amazon_ungraph_cvxopt_50000_5_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_50000_10_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_100000_10_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_100000_20_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_200000_10_nb_missing.out"     # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_200000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_400000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_600000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_800000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_1000000_20_nb_missing.out"    # nearby, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_200000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_400000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_600000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_800000_20_rand_missing.out"    # random, MOSEK
#    after_file = "../out/com_amazon_ungraph_cvxopt_1000000_20_rand_missing.out"    # random, MOSEK
    
#    after_file = "../out/com_youtube_ungraph_cvxopt_300000_30_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_300000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_420000_30_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_420000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_600000_600_missing.out"        # random
#    after_file = "../out/com_youtube_ungraph_cvxopt_600000_60_missing.out"        # random, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_600000_30_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_900000_600_missing.out"        # random
#    after_file = "../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out"        # nearby, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_600000_60_rand_missing.out"        # random, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_1200000_60_rand_missing.out"        # random, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_1800000_60_rand_missing.out"        # random, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_2400000_60_rand_missing.out"        # random, MOSEK
#    after_file = "../out/com_youtube_ungraph_cvxopt_3000000_60_rand_missing.out"        # random, MOSEK
    
    ## linear opt. (uncertain)
    # 100k nodes
#    after_file = "../out/er_100000_00001_linopt_50000_avg_inv_min.out"      # random
#    after_file = "../out/er_100000_00001_linopt_100000_avg_inv_min.out"
#    after_file = "../out/er_100000_00001_linopt_200000_avg_inv_min.out"
    
#    after_file = "../out/sm_100000_005_11_linopt_50000_avg_inv_min.out"     # random
#    after_file = "../out/sm_100000_005_11_linopt_100000_avg_inv_min.out"     # random
#    after_file = "../out/sm_100000_005_11_linopt_200000_avg_inv_min.out"     # random
    
#    after_file = "../out/ba_100000_5_linopt_50000_avg_inv_min.out"     # random
#    after_file = "../out/ba_100000_5_linopt_100000_avg_inv_min.out"     # random
#    after_file = "../out/ba_100000_5_linopt_200000_avg_inv_min.out"     # random
#    after_file = "../out/ba_100000_5_linopt_300000_avg_inv_min.out"     # random
    
    # real graphs
#    after_file = "../out/com_dblp_ungraph_linopt_200000_nb_avg_inv_min.out"    # nearby
#    after_file = "../out/com_dblp_ungraph_linopt_200000_avg_inv_min.out"    # random
#    after_file = "../out/com_dblp_ungraph_linopt_400000_nb_avg_inv_min.out"    # nearby
#    after_file = "../out/com_dblp_ungraph_linopt_400000_avg_inv_min.out"    # random
    
#    after_file = "../out/com_youtube_ungraph_linopt_500000_nb_avg_inv_min.out"    # nearby
#    after_file = "../out/com_youtube_ungraph_linopt_500000_avg_inv_min.out"       # random (solved with MOSEK)     

    ## random walk transform (NDSS'13)
    # 100k
#    after_file = "../randwalk/sm_100000_005_11_randwalk_2_10.out"       # t=2, M=10
    
    # real graphs
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_2_10.out"       # t=2, M=10
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_3_10.out"       # t=3, M=10
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_5_10.out"       # t=5, M=10
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_10_10.out"       # t=10, M=10

#    after_file = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.2.out"       # t=2, M=10, p=0.2    
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.4.out"       # t=2, M=10, p=0.4  
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.6.out"       # t=2, M=10, p=0.6  
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.8.out"       # t=2, M=10, p=0.8  

#    after_file = "../randwalk/com_amazon_ungraph_randwalk_2_10.out"       # t=2, M=10
#    after_file = "../randwalk/com_amazon_ungraph_randwalk_3_10.out"       # t=3, M=10
#    after_file = "../randwalk/com_amazon_ungraph_randwalk_5_10.out"       # t=5, M=10
#    after_file = "../randwalk/com_amazon_ungraph_randwalk_10_10.out"       # t=10, M=10    

#    after_file = "../randwalk/com_youtube_ungraph_randwalk_2_10.out"       # t=2, M=10
#    after_file = "../randwalk/com_youtube_ungraph_randwalk_3_10.out"       # t=3, M=10
#    after_file = "../randwalk/com_youtube_ungraph_randwalk_5_10.out"       # t=5, M=10
#    after_file = "../randwalk/com_youtube_ungraph_randwalk_10_10.out"       # t=10, M=10    

    

    
    # Command-line param for automation (bash): <before_file> <after_file> <n_samples> <algo_type>
    n_samples = 5
    algo_type = AT_UNCERTAIN
    
    if len(sys.argv) > 3:
        before_file = sys.argv[1]
        after_file = sys.argv[2]
        n_samples = int(sys.argv[3])
        

    print "before_file =", before_file
    print "after_file =", after_file
    print "n_samples =", n_samples
    
    #
    if after_file == before_file:
        #### (1) DETERMINISTIC: incorrectness_H1(), incorrectness_H2_open()  
        bG, aG = read_two_graphs(before_file, '\t', after_file, '\t')
        print "bG#nodes =", bG.vcount()
        print "bG#edges =", bG.ecount()
        print "aG#nodes =", aG.vcount()
        print "aG#edges =", aG.ecount()
        bins = [0,1,4,10,20,100000000]
        sum_re_prob, re_prob_dict = incorrectness_H1(bG, aG, bins)
        
        print "sum_re_prob H1 =", sum_re_prob
    #    print re_prob_dict
        
        start = time.clock()
        h2_list = equivalence_class_H2_open(bG, None)
        cand_size, bin_size, sig_list_b, bucket_list_b = bucket_H2(h2_list, bins)
        
        sum_re_prob, re_prob_dict = incorrectness_H2_open(aG, sig_list_b, bucket_list_b, bins)
        print "sum_re_prob H2 (open) =", sum_re_prob
        print "elapsed :", time.clock() - start
    #    print re_prob_dict
    
        # TEST compare_edges_diff_one()
        start = time.clock()
        (diff_b, diff_a) = compare_edges_diff_one(bG, aG)
        print "diff_b =", diff_b
        print "diff_a =", diff_a
        print "elapsed :", time.clock() - start
        
        # TEST k_obfuscation_measure(data=False)
        start = time.clock()
        eps_arr = k_obfuscation_measure(before_file, after_file, n_nodes=bG.vcount(), k_arr=[30, 50, 100], data=False)
        print "eps_arr =", eps_arr
        print "elapsed :", time.clock() - start
    
    else:
        #### (2) UNCERTAIN: incorrectness_uncertain (average of n_samples)
        if len(sys.argv) > 4:
            algo_type = int(sys.argv[4])
            print "algo_type =", algo_type
        
        if algo_type == AT_UNCERTAIN:
            # for UNCERTAIN
            data_name = after_file[7:-4]
            sample_file = "../sample/" + data_name + "_sample."
        elif algo_type == AT_SWITCH:
            # for SWITCH
            data_name = after_file[10:-4]
            sample_file = "../switch/" + data_name + "_sample."    
        elif algo_type == AT_RANDOM_WALK:    
            # for RANDOM WALK
            data_name = after_file[12:-4]
            sample_file = "../randwalk/" + data_name + "_sample."    
        else:
            print "WRONG <algo_type>. Exit..."
            sys.exit()
        
        print "sample_file =", sample_file
        
        bins = [0,1,4,10,20,100000000]
        start = time.clock()
        
    #    score_H1, score_H2 = incorrectness_uncertain(before_file, after_file, bins)
        score_H1, score_H2 = incorrectness_uncertain_from_file(before_file, after_file, sample_file, n_samples, bins)    # n_samples = 5
        print "score_H1 =", score_H1
        print "score_H2 =", score_H2
        print "elapsed :", time.clock() - start
        
        
        # TEST compare_edges_diff_from_file()
        start = time.clock()
        diff_b_list, diff_a_list = compare_edges_diff_from_file(before_file, after_file, sample_file, n_samples)
        print diff_b_list
        print diff_a_list
        print "compare_edges_diff_from_file: DONE, elapsed :", time.clock() - start
    
        # (3) TEST k_obfuscation_measure(),  k_obfuscation_measure_from_file()
        start = time.clock()
        
        n_nodes = 0
    #    n_nodes = 100000
    #    n_nodes = 317080    # dblp
    #    n_nodes = 334863    # amazon, 25s/graph
    #    n_nodes = 1134890   # youtube, 437s/5 samples
        
        
        if algo_type == AT_UNCERTAIN:
            # for UNCERTAIN
            eps_arr = k_obfuscation_measure(before_file, after_file, n_nodes, k_arr=[30, 50, 100])  # 100000, 317080, 1134890 nodes
            print "eps_arr =", eps_arr
        elif algo_type == AT_SWITCH:
            # for SWITCH (degree sequence UNCHANGED -> need only 1 sample !)
            avg_eps_arr = k_obfuscation_measure_from_file(before_file, after_file, sample_file, n_nodes, k_arr=[30, 50, 100], n_samples=1)  
            print "eps_arr =", avg_eps_arr
        elif algo_type == AT_RANDOM_WALK:  
            # for RANDOM WALK
            avg_eps_arr = k_obfuscation_measure_from_file(before_file, after_file, sample_file, n_nodes, k_arr=[30, 50, 100], n_samples=n_samples)  
            print "eps_arr =", avg_eps_arr
        
        print "elapsed :", time.clock() - start
    
    
    
    