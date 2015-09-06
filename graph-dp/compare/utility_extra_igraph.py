'''
Created on Jun 19, 2014

@author: huunguye

'''


import sys
import time
from random import *
import math
import igraph as ig
import scipy.io
import numpy as np
import powerlaw
import snap as sn
from random_walk_igraph import mixing_time 

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
def mixing_time_and_assortativity_one(G, eps):
    
    m_time, dd = mixing_time(G, eps) 
    print "m_time =", m_time
    print "dd =", dd
    
    assortativity = G.assortativity_degree(directed=False)
    
    #
    return m_time, assortativity

#######################################################
def mixing_time_and_assortativity(n_samples, sample_file, eps):
    
    sum_m_time = 0.0
    sum_assortativity = 0.0
    
    for i in range(n_samples):
        print "sample i =", i
        # read sample graph aG
        start = time.clock()
        file_name = sample_file + str(i)  
        aG = ig.Graph.Read_Edgelist(file_name, directed=False)
        print "read sample - DONE, elapsed :", time.clock() - start
        
        #
        m_time, dd = mixing_time(aG, eps) 
        print "m_time =", m_time
        print "dd =", dd
        
        assortativity = aG.assortativity_degree(directed=False)
        
        sum_m_time += m_time
        sum_assortativity += assortativity
        
    #
    return sum_m_time/n_samples, sum_assortativity/n_samples


#######################################################
if __name__ == '__main__':
    
    ## original graph
#    filename = "../data/er_1000_001.gr"
#    filename = "../data/ff_1000_05.gr"

#    filename = "../data/a_toy.gr"

    # real graphs
#    filename = "../data/com_dblp_ungraph.gr"
#    filename = "../data/com_amazon_ungraph.gr"
#    filename = "../data/com_youtube_ungraph.gr"

    # 10k nodes
#    filename = "../data/er_10000_0001.gr"    
#    filename = "../data/sm_10000_005_11.gr"   
#    filename = "../data/ff_10000_05.gr"
#    filename = "../data/ff_10000_05_connected.gr"

    # 100k nodes
#    filename = "../data/er_100000_00001.gr"
#    filename = "../data/sm_100000_005_11.gr"
#    filename = "../data/ba_100000_5.gr"
#    filename = "../data/pl_100000_5_01.gr"
#    filename = "../data/ff_100000_045.gr"
#    filename = "../data/ff_100000_045_connected.gr"
    
    ## switch (random/nearby)
#    filename = "../switch/ba_100000_5_switch_rand_50000.out"
#    filename = "../switch/ba_100000_5_switch_rand_100000.out"
#    filename = "../switch/ba_100000_5_switch_rand_150000.out"

#    filename = "../switch/com_dblp_ungraph_switch_rand_100000.out"    # random
#    filename = "../switch/com_dblp_ungraph_switch_rand_200000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_rand_300000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_rand_400000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_rand_500000.out" 
    
#    filename = "../switch/com_dblp_ungraph_switch_nb_100000.out"    # nearby
#    filename = "../switch/com_dblp_ungraph_switch_nb_200000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_nb_300000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_nb_400000.out"   
#    filename = "../switch/com_dblp_ungraph_switch_nb_500000.out" 
    
#    filename = "../switch/com_amazon_ungraph_switch_rand_100000.out"    # random
#    filename = "../switch/com_amazon_ungraph_switch_rand_200000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_rand_300000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_rand_400000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_rand_500000.out" 
    
#    filename = "../switch/com_amazon_ungraph_switch_nb_100000.out"    # nearby
#    filename = "../switch/com_amazon_ungraph_switch_nb_200000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_nb_300000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_nb_400000.out"   
#    filename = "../switch/com_amazon_ungraph_switch_nb_500000.out"     
    
#    filename = "../switch/com_youtube_ungraph_switch_rand_300000.out"    # random
#    filename = "../switch/com_youtube_ungraph_switch_rand_600000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_rand_900000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_rand_1200000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_rand_1500000.out"   
#    
#    filename = "../switch/com_youtube_ungraph_switch_nb_300000.out"    # nearby
#    filename = "../switch/com_youtube_ungraph_switch_nb_600000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_nb_900000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_nb_1200000.out"   
#    filename = "../switch/com_youtube_ungraph_switch_nb_1500000.out" 
    
    
    ## entropy-based (obfuscation)
#    filename = "../out/er_1000_001_entropy_01_2_001.out"
#    filename = "../out/er_1000_001_entropy_05_2_001.out"
#    filename = "../out/er_1000_001_entropy_10_2_001.out"
#    filename = "../out/ff_1000_05_entropy_01_2_001.out"    # ff_1000_05
#    filename = "../out/ff_1000_05_entropy_05_2_001.out"
#    filename = "../out/ff_1000_05_entropy_10_2_001.out"

#    filename = "../out/er_10000_0001_entropy_001_2_001.out"    
#    filename = "../out/er_10000_0001_entropy_01_2_001.out"
#    filename = "../out/sm_10000_005_11_entropy_01_2_001.out"

#    filename = "../out/er_100000_00001_entropy_001_2_001.out"    # sigma = 0.01
#    filename = "../out/er_100000_00001_entropy_01_2_001.out"
#    filename = "../out/sm_100000_005_11_entropy_001_2_001.out"  # sigma = 0.01    
#    filename = "../out/sm_100000_005_11_entropy_01_2_001.out"    
#    filename = "../out/ba_100000_5_entropy_001_2_001.out"       # sigma = 0.01    
#    filename = "../out/ba_100000_5_entropy_01_2_001.out"    

    # real graphs
#    filename = "../out/com_dblp_ungraph_entropy_01_2_001.out"
#    filename = "../out/com_dblp_ungraph_entropy_001_2_001.out"   # sigma = 0.01
#    filename = "../out/com_dblp_ungraph_entropy_0001_2_001.out"   # sigma = 0.001       
#    filename = "../out/com_amazon_ungraph_entropy_01_2_001.out"
#    filename = "../out/com_amazon_ungraph_entropy_001_2_001.out"   # sigma = 0.01
#    filename = "../out/com_amazon_ungraph_entropy_0001_2_001.out"   # sigma = 0.001    
#    filename = "../out/com_youtube_ungraph_entropy_01_2_001.out"
#    filename = "../out/com_youtube_ungraph_entropy_001_2_001.out"   # sigma = 0.01
#    filename = "../out/com_youtube_ungraph_entropy_0001_2_001.out"   # sigma = 0.001      
    
    ##            (deterministic)
#    filename = "../out/er_1000_001_entropy_post_01_2_001.out"
#    filename = "../out/er_1000_001_entropy_post_05_2_001.out"
#    filename = "../out/er_1000_001_entropy_post_10_2_001.out"
#    filename = "../out/ff_1000_05_entropy_post_01_2_001.out"    # ff_1000_05
#    filename = "../out/ff_1000_05_entropy_post_05_2_001.out"
#    filename = "../out/ff_1000_05_entropy_post_10_2_001.out"
    
#    filename = "../out/er_10000_0001_entropy_post_01_2_001.out"
#    filename = "../out/er_10000_0001_entropy_post_05_2_001.out"
#    filename = "../out/er_10000_0001_entropy_post_10_2_001.out"
    
    
    ## uncertain convex opt.
#    filename = "../out/er_1000_001_cvxopt_1000.out"
#    filename = "../out/er_1000_001_cvxopt_1000_nb.out"
#    filename = "../out/ff_1000_05_cvxopt_1000.out"        # ff_1000_05
#    filename = "../out/ff_1000_05_cvxopt_1000_nb.out"

#    filename = "../out/er_10000_0001_cvxopt_10000.out"
#    filename = "../out/er_10000_0001_cvxopt_10000_nb_missing.out"   # nearby
#    filename = "../out/er_10000_0001_cvxopt_10000_missing.out"
#    filename = "../out/er_10000_0001_cvxopt_10000_missing.out"      # w/o redistribute missing edges
#    filename = "../out/er_10000_0001_cvxopt_5000_missing.out"
#    filename = "../out/er_10000_0001_cvxopt_10000_10_triad_missing.out"   # triad
#    filename = "../out/er_10000_0001_cvxopt_5000_10_triad_missing.out"   # triad
#    filename = "../out/er_10000_0001_cvxopt_5000_10_triad_blossom_missing.out"   # triad
#    filename = "../out/er_100000_00001_cvxopt_100000_100_triad_missing.out"   # triad

#    filename = "../out/sm_10000_005_11_cvxopt_10000.out"
#    filename = "../out/sm_10000_005_11_cvxopt_10000_missing.out"  
#    filename = "../out/sm_10000_005_11_cvxopt_10000_10_triad_missing.out"       # triads

#    filename = "../out/er_100000_00001_cvxopt_100000_nb_missing.out"    # nearby
#    filename = "../out/er_100000_00001_cvxopt_100000_missing.out"        # random
#    filename = "../out/er_100000_00001_cvxopt_100000_uq_missing.out"    # unique   
#    filename = "../out/er_100000_00001_cvxopt_100000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/er_100000_00001_cvxopt_100000_10_rand_missing.out"  # random, MOSEK
#    filename = "../out/er_100000_00001_cvxopt_100000_10_uq_missing.out"    # unique, MOSEK   
#    filename = "../out/er_100000_00001_cvxopt_50000_nb_missing.out"        # nearby
#    filename = "../out/er_100000_00001_cvxopt_50000_missing.out"        # random
#    filename = "../out/er_100000_00001_cvxopt_50000_uq_missing.out"    # unique

#    filename = "../out/sm_100000_005_11_cvxopt_25000_missing.out"          # random
#    filename = "../out/sm_100000_005_11_cvxopt_50000_missing.out"          # random
#    filename = "../out/sm_100000_005_11_cvxopt_50000_10_missing.out"        # random, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_50000_nb_missing.out"          # nearby
#    filename = "../out/sm_100000_005_11_cvxopt_50000_10_nb_missing.out"        # nearby, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_100000_missing.out"
#    filename = "../out/sm_100000_005_11_cvxopt_100000_10_rand_missing.out"     # random, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_100000_10_nb_missing.out"       # nearby, MOSEK 
#    filename = "../out/sm_100000_005_11_cvxopt_100000_10_uq_missing.out"       # unique, MOSEK
#    filename = "../out/sm_100000_005_11_cvxopt_100000_nb_missing.out"       # nearby 
#    filename = "../out/sm_100000_005_11_cvxopt_100000_uq_missing.out"       # unique
#    filename = "../out/sm_100000_005_11_cvxopt_100000_200_nb_missing.out"
#    filename = "../out/sm_100000_005_11_cvxopt_200000_200_nb_missing.out"

#    filename = "../out/ba_100000_5_cvxopt_100000_nb_missing.out"    # nearby
#    filename = "../out/ba_100000_5_cvxopt_100000_missing.out"
#    filename = "../out/ba_100000_5_cvxopt_50000_nb_missing.out"        # nearby
#    filename = "../out/ba_100000_5_cvxopt_50000_missing.out"

    # real graphs
#    filename = "../out/com_dblp_ungraph_cvxopt_50000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_100000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_100000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_100000_nb_missing.out"    # nearby            
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_nb_missing.out"    # nearby
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_5_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_missing.out"
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_075_nb_missing.out"    # nearby, sub=0.75 MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_05_nb_missing.out"    # nearby, sub=0.5 MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_sub_025_nb_missing.out"    # nearby, sub=0.25 MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_missing.out"            # randwalk, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_missing.out"            # randwalk, MOSEK
    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_rw_first_missing.out"            # randwalk, first MOSEK
    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_rw_first_missing.out"            # randwalk, first MOSEK
    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_rw_first_missing.out"            # randwalk, first MOSEK
    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_rw_first_missing.out"            # randwalk, first MOSEK
    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_rw_first_missing.out"            # randwalk, first MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_200000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_400000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_600000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_800000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_dblp_ungraph_cvxopt_1000000_20_rand_missing.out"    # random, MOSEK

#    filename = "../out/com_amazon_ungraph_cvxopt_50000_5_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_50000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_100000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_100000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_200000_10_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_200000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_400000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_600000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_800000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_1000000_20_nb_missing.out"    # nearby, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_200000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_400000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_600000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_800000_20_rand_missing.out"    # random, MOSEK
#    filename = "../out/com_amazon_ungraph_cvxopt_1000000_20_rand_missing.out"    # random, MOSEK

#    filename = "../out/com_youtube_ungraph_cvxopt_300000_30_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_300000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_420000_30_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_420000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_600_missing.out"   # random
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_60_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_30_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_900000_600_missing.out"   # random
#    filename = "../out/com_youtube_ungraph_cvxopt_600000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1200000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1800000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_2400000_60_nb_missing.out"   # nearby, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_3000000_60_nb_missing.out"   # nearby, MOSEK

#    filename = "../out/com_youtube_ungraph_cvxopt_600000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1200000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_1800000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_2400000_60_rand_missing.out"   # random, MOSEK
#    filename = "../out/com_youtube_ungraph_cvxopt_3000000_60_rand_missing.out"   # random, MOSEK
    
    ##            (deterministic) representative .rep
#    filename = "../out/er_1000_001_cvxopt_rep_1000.out"
#    filename = "../out/er_1000_001_cvxopt_rep_1000_nb.out"
#    filename = "../out/ff_1000_05_cvxopt_rep_1000.out"
#    filename = "../out/ff_1000_05_cvxopt_rep_1000_nb.out"

#    filename = "../out/er_10000_0001_cvxopt_rep_10000.out"
    
    # real graphs
#    filename = "../rep/com_dblp_ungraph_entropy_001_2_001.gp.rep"  # GP representative, 
#    filename = "../rep/com_dblp_ungraph_entropy_001_2_001.adr.rep"  # ADR representative, 
#    filename = "../rep/com_dblp_ungraph_entropy_001_2_001.abm.rep"  # ABM representative

#    filename = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.gp.rep"  # GP representative
#    filename = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.adr.rep"  # ADR representative
#    filename = "../rep/com_dblp_ungraph_cvxopt_200000_nb_missing.abm.rep"  # ABM representative, 15s+40s
    
    
    ## uncertain linear opt.
#    filename = "../out/er_100000_00001_linopt_50000_avg_inv_min.out"    # random
#    filename = "../out/er_100000_00001_linopt_100000_avg_inv_min.out"    # random
#    filename = "../out/er_100000_00001_linopt_200000_avg_inv_min.out"    # random    
    
#    filename = "../out/sm_100000_005_11_linopt_50000_avg_inv_min.out"     # random
#    filename = "../out/sm_100000_005_11_linopt_100000_avg_inv_min.out"     # random
#    filename = "../out/sm_100000_005_11_linopt_200000_avg_inv_min.out"     # random

#    filename = "../out/ba_100000_5_linopt_50000_avg_inv_min.out"     # random
#    filename = "../out/ba_100000_5_linopt_100000_avg_inv_min.out"     # random
#    filename = "../out/ba_100000_5_linopt_200000_avg_inv_min.out"     # random
#    filename = "../out/ba_100000_5_linopt_300000_avg_inv_min.out"     # random
    
    # real graphs
#    filename = "../out/com_dblp_ungraph_linopt_200000_nb_avg_inv_min.out"    # nearby    
#    filename = "../out/com_dblp_ungraph_linopt_200000_avg_inv_min.out"  # random
#    filename = "../out/com_dblp_ungraph_linopt_400000_nb_avg_inv_min.out"  # nearby
#    filename = "../out/com_dblp_ungraph_linopt_400000_avg_inv_min.out"  # random

#    filename = "../out/com_youtube_ungraph_linopt_500000_nb_avg_inv_min.out"    # nearby
#    filename = "../out/com_youtube_ungraph_linopt_500000_avg_inv_min.out"       # random (solved with MOSEK)        
    
    ## random walk transform (NDSS'13)
#    filename = "../randwalk/sm_100000_005_11_randwalk_2_10.out"       # t=2, M=10

    # real graphs
#    filename = "../randwalk/com_dblp_ungraph_randwalk_2_10.out"       # t=2, M=10  
#    filename = "../randwalk/com_dblp_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_dblp_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_dblp_ungraph_randwalk_10_10.out"       # t=10, M=10

#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.2.out"       # t=2, M=10, p=0.2
#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.4.out"       # t=2, M=10, p=0.4
#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.6.out"       # t=2, M=10, p=0.6
#    filename = "../randwalk/com_dblp_ungraph_randwalk_replace_2_10_0.8.out"       # t=2, M=10, p=0.8

#    filename = "../randwalk/com_amazon_ungraph_randwalk_2_10.out"       # t=2, M=10  
#    filename = "../randwalk/com_amazon_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_amazon_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_amazon_ungraph_randwalk_10_10.out"       # t=10, M=10

#    filename = "../randwalk/com_youtube_ungraph_randwalk_2_10.out"       # t=2, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_3_10.out"       # t=3, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_5_10.out"       # t=5, M=10
#    filename = "../randwalk/com_youtube_ungraph_randwalk_10_10.out"       # t=10, M=10     
    
    
    ###### Command-line param for automation (bash): <filename> <n_samples> <eps> <algo_type>
    n_samples = 5
    eps = 0.01
    algo_type = AT_UNCERTAIN
    
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    if len(sys.argv) > 2:
        n_samples = int(sys.argv[2])
    if len(sys.argv) > 3:
        eps = float(sys.argv[3])
        

    print "filename =", filename
    print "n_samples =", n_samples
    print "eps =", eps
    
    if filename[-4:] == ".out":
        #### 2 - UNCERTAIN G (FROM SAMPLE FILES) --> NOTE: change n_samples !!!
        # TEST all_statistics_from_file
        if len(sys.argv) > 4:
            algo_type = int(sys.argv[4])
        
        if algo_type == AT_UNCERTAIN:
            # for UNCERTAIN
            data_name = filename[7:-4]
            sample_file = "../sample/" + data_name + "_sample."
        elif algo_type == AT_SWITCH:
            # for SWITCH
            data_name = filename[10:-4]
            sample_file = "../switch/" + data_name + "_sample."   
        elif algo_type == AT_RANDOM_WALK:    
            # for RANDOM WALK
            data_name = filename[12:-4]
            sample_file = "../randwalk/" + data_name + "_sample."    
        else:
            print "WRONG <algo_type>. Exit..."
            sys.exit()
            
        #    
        start = time.clock()
        
        m_time, assortativity = mixing_time_and_assortativity(n_samples, sample_file, eps) 
        print "m_time =", m_time
        print "assortativity =", assortativity

        print "mixing_time_and_assortativity - Elapsed ", (time.clock() - start)
        
    elif filename[-3:] == ".gr":
        #### 3 - DETERMINISTIC G
        start = time.clock()
        G = ig.Graph.Read_Edgelist(filename, directed=False)
        print "#nodes =", G.vcount()
        print "#edges =", G.ecount()
        
        deg_list = G.degree(G.vs)         # dict[node] = deg
        min_deg = min(deg_list)
        max_deg = max(deg_list)
        print "min-deg =", min_deg
        print "max-deg =", max_deg
        
        cc_list = G.clusters("weak")
        print "#components =", len(cc_list)
        cc_size = [len(c) for c in cc_list]
        print "cc_size =", cc_size
    
        print "Read graph - Elapsed ", (time.clock() - start)
        
        # TEST degree_statistics
        
        start = time.clock()
        m_time, assortativity = mixing_time_and_assortativity_one(G, eps) 
        print "m_time =", m_time
        print "assortativity =", assortativity

        print "mixing_time_and_assortativity_one - Elapsed ", (time.clock() - start)
    
    else:
        ####
        print "WRONG <filename>. Exit..."
        sys.exit()
    
    
    