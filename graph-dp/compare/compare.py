'''
Created on Feb 12, 2014

@author: huunguye
    Jul 10 - copy compare_degree_sequence() from compare_igraph
    Jul 23 - compute_total_variance()
'''

import time
import sys
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from entropy_obfuscation import sanitize, compute_eps
from attack_structure import equivalence_class_H1, equivalence_class_H2_closed, equivalence_class_H2_open, bucket_H2

AT_UNCERTAIN = 0
AT_SWITCH = 1
AT_RANDOM_WALK = 2

#######################################################
def read_two_graphs(before_file, b_delimiter, after_file, a_delimiter):
    bG = nx.read_edgelist(before_file, '#', b_delimiter, None, nodetype=int)
    aG = nx.read_edgelist(after_file, '#', a_delimiter, None, nodetype=int, data=False)
    #
    return bG, aG

#######################################################
def read_two_graphs_with_multi(before_file, b_delimiter, after_file, a_delimiter):
    bG = nx.read_edgelist(before_file, '#', b_delimiter, None, nodetype=int)
    aG = nx.read_edgelist(after_file, '#', a_delimiter, create_using=nx.MultiGraph(), nodetype=int, data=False)
    #
    return bG, aG

#######################################################
def compare_simple(bG, aG):
    # 1-difference of edges between aG and bG
    diff_b = []
    for e in bG.edges_iter():
        if not aG.has_edge(e[0],e[1]):
            diff_b.append((e[0],e[1]))
    diff_a = []
    for e in aG.edges_iter():
        if not bG.has_edge(e[0],e[1]):
            diff_a.append((e[0],e[1]))
    #
    print "len(diff_b) =", len(diff_b)
    print "len(diff_a) =", len(diff_a)
#    print "diff_b =", diff_b
#    print "diff_a =", diff_a

    # 2-connectivity
    print "#components bG,aG =", nx.number_connected_components(bG), nx.number_connected_components(aG)
    

    # 3-distance
#    print "#diameter bG,aG =", nx.diameter(bG), nx.diameter(aG)
    
    #
    

#######################################################    
def compare_metrics(bG, aG):
    # 1-centrality
#    print "Deg.centrality :", nx.degree_centrality(bG), nx.degree_centrality(aG)
    print "transitivity :", nx.transitivity(bG), nx.transitivity(aG)
    start = time.clock()
    print "diameter bG,aG =", nx.diameter(bG), nx.diameter(aG)
    print "compute diameter - Elapsed :", time.clock() - start
    
    #     
    
    
#######################################################    
def compare_H1_H2_structural_attack(bG, aG, bins):
    
    cand_size, bin_size, sig_list, bucket_list = equivalence_class_H1(bG, bins)
    print "H1-bG", bin_size
    cand_size, bin_size, sig_list, bucket_list = equivalence_class_H1(aG, bins)
    print "H1-aG", bin_size
    
    h2_list = equivalence_class_H2_open(bG, None)       # open world
    cand_size, bin_size, sig_list, bucket_list = bucket_H2(h2_list, bins)
    print "H2-open-bG", bin_size
    h2_list = equivalence_class_H2_open(aG, None)
    cand_size, bin_size, sig_list, bucket_list = bucket_H2(h2_list, bins)
    print "H2-open-aG", bin_size

#######################################################    
def compare_degree_sequence(before_file, after_file, sample_file, n_samples):
    start = time.clock()
    bG = nx.read_edgelist(before_file, '#', "\t", None, nodetype=int)
    print "read bG: DONE, elapsed :", time.clock() - start
    deg_dict_bG = nx.degree(bG)

    start = time.clock()
    count = 0
    for i in range(n_samples):
        file_name = sample_file + str(i)
        aG = nx.read_edgelist(file_name, '#', "\t", create_using=nx.MultiGraph(), nodetype=int, data=False)
        print "aG.#edges =", aG.number_of_edges()
        # add missing nodes
        aG.add_nodes_from(bG.nodes_iter())
        
        deg_dict_aG = nx.degree(aG)
        num_deg_diff = 0
        sum_deg_diff = 0
        for u in bG.nodes_iter():
            if deg_dict_bG[u] != deg_dict_aG[u]: 
                num_deg_diff += 1   
                sum_deg_diff += abs(deg_dict_bG[u] - deg_dict_aG[u])
        print "num_deg_diff =", num_deg_diff
        print "sum_deg_diff =", sum_deg_diff
        
        print "count =", count
        count += 1

#######################################################
# for (k,e)-obf and MaxVar
def compute_total_variance(org_file_name, file_name_list):
    
    G0 = nx.read_edgelist("../data/" + org_file_name + ".gr", '#', '\t', None, nodetype=int)
    for data_name in file_name_list:
        file_name = '../out/' + data_name + '.out'
        start = time.clock()
        G = nx.read_edgelist(file_name, '#', '\t', None, int, True)
        print "file_name =", file_name, "elapsed", time.clock() - start
        TV = 0.0
        TP = 0.0  # total probabilities = sum (1-p_i)
        for e in G.edges_iter(data=True):
            p = e[2]['p']
            TV += p*(1-p)
            if G0.has_edge(e[0], e[1]):
                TP += (1-p)
        print "TV =", TV, ", TP =", TP

    

#######################################################
if __name__ == '__main__':
#    before_file = "C:/Tailieu/Paper-code/DATA-SET/SNAP/Social networks/facebook_combined.txt"
#    after_file = "../data/facebook_filtered.txt"
    
#    before_file = "../data/er_200_02.gr"
#    before_file = "../data/er_1000_001.gr"
#    before_file = "../data/er_1000_005.gr"
#    before_file = "../data/er_10000_0001.gr"
#    before_file = "../data/er_10000_0005.gr"
    
    
    ## entropy-based (aggregation)
#    after_file = "../data/er_1000_005_en_0.1_filtered.txt"
#    after_file = "../data/er_1000_005_en_0.5_filtered.txt"
#    after_file = "../data/er_1000_005_en_1.0_filtered.txt"

    ## entropy-based (obfuscation)
#    after_file = "../out/er_1000_001_entropy_post_01_2_001.out"
#    after_file = "../out/er_10000_0001_entropy_post.out"
    
    
    ## differential privacy
#    after_file = "../data/er_1000_005_dp_filtered.txt"
#    after_file = "../data/er_1000_005_dp2_0.1_filtered.txt"
#    after_file = "../data/er_1000_005_dp2_1.0_filtered.txt"
#    after_file = "../data/er_1000_005_dp2_10.0_filtered.txt"
#    after_file = "../data/er_1000_005_dp2_100.0_filtered.txt"

#    after_file = "../data/er_10000_0005_dp2_1.0_filtered.txt"

    ## uncertain convex opt.
##    after_file = "../out/er_200_002_cvxopt_rep.out"
##    after_file = "../out/er_200_02_cvxopt_rep.out"
#    after_file = "../out/er_1000_001_cvxopt_rep.out"

    
    # TEST compare_simple(), compare_metrics()
#    bG, aG = read_two_graphs(before_file, '\t', after_file, '\t')
#    compare_simple(bG, aG)
#    compare_metrics(bG, aG)
    
    
    ## structural attacks
#    bins = [0,1,4,10,20,100000000]
#    compare_H1_H2_structural_attack(bG, aG, bins)
    
    
    ## entropy-based
    # TEST compute_eps() of reconstructed graph (post-processing attack)
#    bG = nx.read_edgelist(before_file, '#', '\t', None, nodetype=int)
#    bG = sanitize(bG)
#    eps_bG = compute_eps(bG, bG, k=10)      # k=10, 50
#    print "eps bG", eps_bG
#    
#    aG = nx.read_edgelist(after_file, '#', '\t', None, nodetype=int, data=False)
#    aG = sanitize(aG)
#    eps_aG = compute_eps(aG, aG, k=10)      # k=10, 50
#    print "eps aG", eps_aG
    
    
    # TEST compare_degree_sequence()
#    print "TEST compare_degree_sequence"
#    
#    before_file = "../data/com_dblp_ungraph.gr"
##    before_file = "../data/com_amazon_ungraph.gr"
##    before_file = "../data/com_youtube_ungraph.gr"
#
##    after_file = "../out/com_dblp_ungraph_entropy_01_2_001.out"     # sigma = 0.1
##    after_file = "../out/com_dblp_ungraph_entropy_0001_2_001.out"     # sigma = 0.001
#    
##    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_nb_missing.out"  # nearby, MOSEK
##    after_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_nb_missing.out"  # nearby, MOSEK
#
##    after_file = "../randwalk/com_dblp_ungraph_randwalk_3_10.out"           # t=3, M=10
##    after_file = "../randwalk/com_dblp_ungraph_randwalk_5_10.out"           # t=5, M=10   
#           
##    after_file = "../randwalk/com_dblp_ungraph_randwalk_keep_2_10_0.5.out"           # t=2, M=10
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_keep_3_10_0.5.out"           # t=3, M=10   
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_keep_5_10_0.5.out"           # t=5, M=10
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_keep_10_10_0.5.out"           # t=10, M=10   
#
#
#    
##    algo_type = AT_UNCERTAIN
#    algo_type = AT_RANDOM_WALK
#    
#    n_samples = 1
#    
#    if algo_type == AT_UNCERTAIN:
#        # for UNCERTAIN
#        data_name = after_file[7:-4]
#        sample_file = "../sample/" + data_name + "_sample."
#    elif algo_type == AT_SWITCH:
#        # for SWITCH
#        data_name = after_file[10:-4]
#        sample_file = "../switch/" + data_name + "_sample."    
#    elif algo_type == AT_RANDOM_WALK:    
#        # for RANDOM WALK
#        data_name = after_file[12:-4]
#        sample_file = "../randwalk/" + data_name + "_sample."    
#    else:
#        print "WRONG <algo_type>. Exit..."
#        sys.exit()
#    
#    print "before_file =", before_file
#    print "after_file =", after_file
#    print "sample_file =", sample_file
#    
#    
#    compare_degree_sequence(before_file, after_file, sample_file, n_samples)


    # TEST compute_total_variance()
    print "TEST compute_total_variance"
    compute_total_variance('com_amazon_ungraph',
                           ['com_amazon_ungraph_entropy_0001_2_001',
                            'com_amazon_ungraph_entropy_001_2_001',
                            'com_amazon_ungraph_entropy_01_2_001',
                            'com_amazon_ungraph_cvxopt_200000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_400000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_600000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_800000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_1000000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_1200000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_1400000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_1600000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_1800000_20_nb_missing',
                            'com_amazon_ungraph_cvxopt_2000000_20_nb_missing'
                           ])

<<<<<<< .mine
#    compute_total_variance('ba_10000_3',
#                           [
#                            'ba_10000_3_cvxopt_6000',
#                            'ba_10000_3_6000_nb_only_org',
#                            'ba_10000_3_cvxopt_10000',
#                            'ba_10000_3_10000_nb_only_org',
#                            'ba_10000_3_cvxopt_12000',
#                            'ba_10000_3_12000_nb_only_org',
#                            'ba_10000_3_cvxopt_18000',
#                            'ba_10000_3_18000_nb_only_org',
#                            'ba_10000_3_cvxopt_24000',
#                            'ba_10000_3_24000_nb_only_org',
#                            'ba_10000_3_cvxopt_30000',
#                            'ba_10000_3_30000_nb_only_org',
#                            ])

    compute_total_variance('ba_100000_5',
                           [
                            'ba_100000_5_linopt_100000_nb_only_org_min'
                            ])
=======
#    compute_total_variance('ba_10000_3',
#                           [
#                            'ba_10000_3_cvxopt_6000',
#                            'ba_10000_3_6000_nb_only_org',
#                            'ba_10000_3_cvxopt_10000',
#                            'ba_10000_3_10000_nb_only_org',
#                            'ba_10000_3_cvxopt_12000',
#                            'ba_10000_3_12000_nb_only_org',
#                            'ba_10000_3_cvxopt_18000',
#                            'ba_10000_3_18000_nb_only_org',
#                            'ba_10000_3_cvxopt_24000',
#                            'ba_10000_3_24000_nb_only_org',
#                            'ba_10000_3_cvxopt_30000',
#                            'ba_10000_3_30000_nb_only_org',
#                            ])
>>>>>>> .r645


    
    
    