'''
Created on June 26, 2014

@author: huunguye
'''

import time
import sys
from random import *
import math
import networkx as nx
import scipy.io
from compare import read_two_graphs_with_multi
from graph_generator import generate_sample
from attack_structure import equivalence_class_H1, equivalence_class_H2_closed, equivalence_class_H2_open, bucket_H2, list_comparator
from entropy_obfuscation import compute_eps, compute_eps_deterministic_multi

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
def incorrectness_uncertain(before_file, after_file, bins): 
    
    # compute sig_list_b, bucket_list_b ONCE !
    bG = nx.read_edgelist(before_file, '#', '\t', None, nodetype=int)
    G = nx.read_edgelist(after_file, '#', '\t', None, nodetype=int, data=True)
    
    h2_list = equivalence_class_H2_open(bG, None)
    cand_size, bin_size, sig_list_b, bucket_list_b = bucket_H2(h2_list, bins)
#    print "len B:", len(sig_list_b), len(bucket_list_b)
    
    # list of sampled graphs
    g_list = [] 
    start = time.clock()
    for i in range(N_SAMPLES):
        g_list.append(generate_sample(G))
    print "Sampling graphs - Elapsed ", (time.clock() - start)
    
    # H1 score
    start = time.clock()
    score_H1 = 0.0
    for aG in g_list:
        sum_re_prob, re_prob_dict = incorrectness_H1(bG, aG, bins)
        score_H1 += sum_re_prob
    score_H1 = score_H1/N_SAMPLES
    print "compute score_H1: DONE, elapsed :", time.clock() - start
    
    
    # H2 score
    score_H2 = 0.0
    count = 0
    for aG in g_list:
        sum_re_prob, re_prob_dict = incorrectness_H2_open(aG, sig_list_b, bucket_list_b, bins)
        score_H2 += sum_re_prob
        print "count =", count
        count += 1
    score_H2 = score_H2/N_SAMPLES
    
    # 
    return score_H1, score_H2

#######################################################
# G: uncertain
def incorrectness_uncertain_from_file(before_file, after_file, sample_file, n_samples, bins): 
    
    # compute sig_list_b, bucket_list_b ONCE !
    start = time.clock()
    bG = nx.read_edgelist(before_file, '#', '\t', None, nodetype=int)
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
        aG = nx.read_edgelist(file_name, '#', '\t', create_using=nx.MultiGraph(), nodetype=int, data=False)     # IMPORTANT: MultiGraph
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
# aG: MultiGraph
def compare_edges_diff_one(bG, aG):
    diff_b = 0
    for e in bG.edges_iter():
        if not aG.has_edge(e[0],e[1]):
            diff_b += 1
            
    diff_a = 0
    marked_edge = {}
    for e in aG.edges_iter():
        if not bG.has_edge(e[0],e[1]):
            diff_a += 1
        else:
            u = e[0]
            v = e[1]
            if u > v:
                u = e[1]
                v = e[0]
            if not marked_edge.has_key((u,v)):
                marked_edge[(u,v)] = 0   # because minus 1 in bG
            else:
                marked_edge[(u,v)] += 1
    diff_a += sum(marked_edge.itervalues())
    
    #
    return (diff_b, diff_a)
    
#######################################################
def compare_edges_diff_from_file(before_file, after_file, sample_file, n_samples):
    # before_file
    bG = nx.read_edgelist(before_file, '#', '\t', None, nodetype=int)
    
    # after_file
    diff_b_list = []
    diff_a_list = []
    for i in range(n_samples):
        file_name = sample_file + str(i)
        aG = nx.read_edgelist(file_name, '#', '\t', create_using=nx.MultiGraph(), nodetype=int, data=False)     # IMPORTANT: MultiGraph
        
        (diff_b, diff_a) = compare_edges_diff_one(bG, aG)
        print "compare sample", i, " DONE"
        #
        diff_b_list.append(diff_b)
        diff_a_list.append(diff_a)
    #
    return diff_b_list, diff_a_list


#######################################################
# compute eps for a given k
def k_obfuscation_measure_from_file(before_file, after_file, sample_file, n_nodes, k_arr, n_samples=5):
    # before_file
    bG = nx.read_edgelist(before_file, '#', '\t', None, nodetype=int)
    print "read bG - DONE"
    
#    if bG.number_of_nodes() < n_nodes:
#        bG.add_nodes_from(range(n_nodes))       # only for er_100k

    # Case 1 - aG = bG
    if after_file == before_file:      # after_file is before_file
        for e in bG.edges_iter():
            bG[e[0]][e[1]]['p'] = 1.0
        return compute_eps_deterministic_multi(bG, bG, k_arr) 
        
    # Case 2 - aG is a sample
    # after_file
    eps_arr_list = []
    for i in range(n_samples):
        file_name = sample_file + str(i)
        aG = nx.read_edgelist(file_name, '#', '\t', create_using=nx.MultiGraph(), nodetype=int, data=False)     # IMPORTANT: MultiGraph
        print "read sample", i, " DONE"
    
        if aG.number_of_nodes() < bG.number_of_nodes():
            aG.add_nodes_from(range(bG.number_of_nodes()))       # only for er_100k
        #
        for e in aG.edges_iter():
            aG[e[0]][e[1]]['p'] = 1.0

        eps_arr = compute_eps_deterministic_multi(bG, aG, k_arr)        # REPLACE: compute_eps_multi by compute_eps_deterministic_multi
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


    #
#    after_file = "../randwalk/com_dblp_ungraph_randwalk_keep_2_0.5_sample.0"       # t=2, multigraph
#    
#    #
#    print "before_file =", before_file
#    print "after_file =", after_file
#    
#    # (1) DETERMINISTIC: incorrectness_H1(), incorrectness_H2_open()  
#    bG, aG = read_two_graphs_with_multi(before_file, '\t', after_file, '\t')
#    print "bG#nodes =", bG.number_of_nodes()
#    print "bG#edges =", bG.number_of_edges()
#    print "aG#nodes =", aG.number_of_nodes()
#    print "aG#edges =", aG.number_of_edges()
#    bins = [0,1,4,10,20,100000000]
#    sum_re_prob, re_prob_dict = incorrectness_H1(bG, aG, bins)
#    
#    print "sum_re_prob H1 =", sum_re_prob
##    print re_prob_dict
#    
#    start = time.clock()
#    h2_list = equivalence_class_H2_open(bG, None)
#    cand_size, bin_size, sig_list_b, bucket_list_b = bucket_H2(h2_list, bins)
#    
#    sum_re_prob, re_prob_dict = incorrectness_H2_open(aG, sig_list_b, bucket_list_b, bins)
#    print "sum_re_prob H2 (open) =", sum_re_prob
#    print "elapsed :", time.clock() - start
##    print re_prob_dict
#
##    # TEST compare_edges_diff_one()
##    (diff_b, diff_a) = compare_edges_diff_one(bG, aG)
##    print "diff_b =", diff_b
##    print "diff_a =", diff_a
##    
##    # TEST k_obfuscation_measure(data=False)
##    eps_arr = k_obfuscation_measure(before_file, after_file, n_nodes=bG.number_of_nodes(), k_arr=[30, 50, 100], data=False)
##    print "eps_arr =", eps_arr
    
    
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
    if after_file != before_file:
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
        
        
        if algo_type == AT_SWITCH:
            # for SWITCH (degree sequence UNCHANGED -> need only 1 sample !)
            avg_eps_arr = k_obfuscation_measure_from_file(before_file, after_file, sample_file, n_nodes, k_arr=[30, 50, 100], n_samples=1)  
            print "eps_arr =", avg_eps_arr
        elif algo_type == AT_RANDOM_WALK:  
            # for RANDOM WALK
            avg_eps_arr = k_obfuscation_measure_from_file(before_file, after_file, sample_file, n_nodes, k_arr=[30, 50, 100], n_samples=n_samples)  
            print "eps_arr =", avg_eps_arr
        
        print "elapsed :", time.clock() - start

    
    
    
    