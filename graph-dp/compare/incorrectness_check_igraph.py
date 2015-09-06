'''
Created on Jun 25, 2014

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
from incorrectness_measure_igraph import compare_edges_diff_one

#N_SAMPLES = 100
#N_SAMPLES = 10
N_SAMPLES = 5

AT_UNCERTAIN = 0
AT_SWITCH = 1
AT_RANDOM_WALK = 2


#######################################################
def check_H2_open(bG, aG, bins, deg_list, max_deg):
    
#    start = time.clock()
#    h2_list_bG = equivalence_class_H2_open(bG, None)
#    h2_list_aG = equivalence_class_H2_open(aG, None)
#    print "compute equivalence_class_H2_open DONE, elapsed:", time.clock() - start
#    
#    # deg = 1
#    n_d1_bG = 0
#    d1_bG_list = []
#    for (u, h2) in h2_list_bG:
#        if len(h2) == 1:
#            n_d1_bG += 1
#            d1_bG_list.append(u)
#    bG_set = set(d1_bG_list)
#
#    # 
#    n_d1_aG = 0
#    d1_aG_list = []
#    for (u, h2) in h2_list_aG:
#        if len(h2) == 1:
#            n_d1_aG += 1
#            d1_aG_list.append(u)
#    aG_set = set(d1_aG_list)

    ##########
    h2_list = equivalence_class_H2_open(bG, None)
    cand_size, bin_size, sig_list_b, bucket_list_b = bucket_H2(h2_list, bins)
    print "len B:", len(sig_list_b), len(bucket_list_b)
    
    h2_list = equivalence_class_H2_open(aG, None)
    cand_size, bin_size, sig_list_a, bucket_list_a = bucket_H2(h2_list, bins)
    print "len A:", len(sig_list_a), len(bucket_list_a)
    
    deg_bG_count = [0 for _ in range(max_deg+1)]        # degree histogram in bG
    deg_re_count = [0 for _ in range(max_deg+1)]        # number of reidentifed nodes by degree
    deg_re_prob = [0.0 for _ in range(max_deg+1)]       # sum of reidentification probability by degree
    
    for u in bG.vs:
        deg_bG_count[deg_list[u.index]] += 1
    
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
                        
                        deg_re_count[deg_list[u]] += 1
                        deg_re_prob[deg_list[u]] += 1.0/len(set_a)
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
            
    #
    sum_re_prob = sum(re_prob_dict.itervalues())
    print "sum_re_prob H2 (open) =", sum_re_prob
    
    sum_re_prob_2 = 0.0
    for deg in range(1,21):
        print "deg =", deg, "deg_bG_count:", deg_bG_count[deg], "deg_re_count:", deg_re_count[deg], "deg_re_prob:", deg_re_prob[deg]
        sum_re_prob_2 += deg_re_prob[deg]
    print "sum_re_prob_2 =", sum_re_prob_2


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

#    after_file = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing.out"    # randwalk, first MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing.out"    # randwalk, first MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing.out"    # randwalk, first MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing.out"    # randwalk, first MOSEK
#    after_file = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing.out"    # randwalk, first MOSEK

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

    
    # TEST 
#    after_file = "../switch/com_dblp_ungraph_switch_nb_100000_sample.0"
#    after_file = "../switch/com_dblp_ungraph_switch_nb_100000_sample.1"
    after_file = "../randwalk/com_dblp_ungraph_randwalk_2_10_sample.0"
    
    print "before_file =", before_file
    print "after_file =", after_file
    
    bG, aG = read_two_graphs(before_file, '\t', after_file, '\t')
    print "bG#nodes =", bG.vcount()
    print "bG#edges =", bG.ecount()
    print "aG#nodes =", aG.vcount()
    print "aG#edges =", aG.ecount()
    bins = [0,1,4,10,20,100000000]
    
    deg_list = bG.degree(bG.vs)         # list[node] = deg
    min_deg = min(deg_list)
    max_deg = max(deg_list)
    
    start = time.clock()
    check_H2_open(bG, aG, bins, deg_list, max_deg)
    print "check_H2_open: DONE, elapsed", time.clock() - start
    
    start = time.clock()
    (diff_b, diff_a) = compare_edges_diff_one(bG, aG)
    print "diff_b =", diff_b
    print "diff_a =", diff_a
    print "elapsed :", time.clock() - start