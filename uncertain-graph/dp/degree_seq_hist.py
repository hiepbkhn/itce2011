'''
Created on Mar 17, 2015

@author: huunguye
    - Degree sequence: Accurate Estimation of the Degree Distribution of Private Networks (ICDM'09)
    - Degree histogram: Towards Accurate Histogram Publication under Differential Privacy (SDM'14)
    Apr 23
    - adjust_sorted_deg_seq()
    Apr 27
    - use nx.configuration_model(), nx.havel_hakimi_graph() instead of dkTopoGen1k
    Apr 28
    - isotonic_deg_sequence: use IsotonicRegression (from scikit-learn)
'''


import time
from subprocess import call, check_output
from random import *
import math
import sys
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import * 
import matplotlib.pylab as P
from sklearn.isotonic import IsotonicRegression
from sklearn.utils import check_random_state
from adj_matrix_visualizer import draw_adjacency_matrix
from graph_summarization import geometric_mechanism

#DKTOPOGEN1K_PATH = "D:/CppProjects/workspace-cpp/orbis/out/dkTopoGen1k.exe"
DKTOPOGEN1K_PATH = "D:/CppProjects/workspace/orbis/out/dkTopoGen1k.exe"        # out: 32-bit,    out2: 64-bit
SAMPLE_FREQ = 100   # 1 of 100 steps

########################################################
# deg_seq may be sorted or unsorted
def naive_deg_sequence(deg_seq, eps):
    noisy_deg_seq = [0 for _ in range(len(deg_seq))]
    
    alpha = math.exp(-eps/4)    # global seensitivity = 4
    
    for i in range(len(deg_seq)):
        noisy_deg_seq[i] = deg_seq[i] + geometric_mechanism(alpha, 1)       # Geometric noise
    
    #
    return noisy_deg_seq 

########################################################
# deg_seq is sorted (ICDM'09)
def isotonic_deg_sequence(deg_seq, eps):

    # index-sort deg_seq  
    idx = [i[0] for i in sorted(enumerate(deg_seq), key=lambda x:x[1])]
    sorted_deg_seq = [deg_seq[idx[i]] for i in range(len(deg_seq))]
    
    
    alpha = math.exp(-eps/4)    # global seensitivity = 4
    
    s1 = [0.0 for _ in range(len(deg_seq))]
    
    # s1 (sorted)
    for i in range(len(deg_seq)):
        s1[i] = sorted_deg_seq[i] + geometric_mechanism(alpha, 1)       # Geometric noise
        
    # s
    ir = IsotonicRegression()

    s = ir.fit_transform(range(len(deg_seq)), s1)
    
    #
    return s, s1, sorted_deg_seq

########################################################
def write_node_deg(deg_seq, filename_deg):
    
    f = open(filename_deg, 'w')
    for u in range(len(deg_seq)):
        f.write("%d %d\n"%(u, deg_seq[u]))
    f.close()
    
########################################################
def read_node_deg(filename_deg):
    
    f = open(filename_deg, 'r')
    fstr = f.read()
    f.close()    
    
    deg_seq = []
    for line in fstr.split("\n"):
        if len(line) == 0:
            break
        items = line.split(" ")
        deg_seq.append(int(items[1]))
    #
    return deg_seq
    
########################################################
def write_deg_seq(deg_seq, filename_1k):
    # count (histogram)
    deg_count = {}
    for deg in deg_seq:
        if not deg_count.has_key(deg):
            deg_count[deg] = 1
        else:
            deg_count[deg] += 1
            
    # sort and write to file
    key_array = list(deg_count.iterkeys())
    key_array = sorted(key_array)
    f = open(filename_1k, 'w')
    for deg in key_array:
        f.write("%d %d\n"%(deg, deg_count[deg]))
    f.close()

########################################################
# deg_seq: noisy degree sequence
def adjust_deg_seq(deg_seq):
    n_nodes = len(deg_seq)
    new_deg_seq = []
    new_deg_seq.extend(deg_seq)     # copy
    
    s = sum(new_deg_seq)
    if s % 2 == 1:  # odd sum
        new_deg_seq[0] += 1
        s += 1
    
    # compute deg_ratio
    deg_ratio = [0.0 for _ in range(n_nodes)]
    for u in range(n_nodes):
        ## WAY-1
#        deg_ratio[u] = float(abs(new_deg_seq[u]))/s     # use ABS()
        ## WAY-2
        if new_deg_seq[u] >= 0:
            deg_ratio[u] = float(abs(new_deg_seq[u]))/s     # use ABS()
        else:
            deg_ratio[u] = 1.0/s
            
    # find max deg
    max_u = -1
    max_deg = 0
    for u in range(n_nodes):
        if new_deg_seq[u] > max_deg:
            max_deg = new_deg_seq[u]
            max_u = u
    print "new_deg_seq[max_u] =", new_deg_seq[max_u]
    
    # adjust by alternating ceiling/floor
    new_s = 0
    
    for u in range(n_nodes):
        if u == max_u:
            continue
        if u % 2 == 0:
            new_deg_seq[u] = int(math.ceil(deg_ratio[u]*s))    # ceiling
        else:     
            new_deg_seq[u] = int(math.floor(deg_ratio[u]*s))    # floor
        new_s += new_deg_seq[u]
        
    new_deg_seq[max_u] = s - new_s      # will be negative !
    print "new_deg_seq[max_u] =", new_deg_seq[max_u]
    
    #
    return new_deg_seq 

########################################################
# deg_seq: noisy degree sequence
def adjust_sorted_deg_seq(deg_seq):
    n_nodes = len(deg_seq)
    new_deg_seq = []
    new_deg_seq.extend(deg_seq)     # copy
    
    s = sum(new_deg_seq)
    if s % 2 == 1:  # odd sum
        new_deg_seq[0] += 1
        s += 1
    
    # compute deg_ratio
    deg_ratio = [0.0 for _ in range(n_nodes)]
    for u in range(n_nodes):
        if new_deg_seq[u] > 0:
            deg_ratio[u] = float(new_deg_seq[u])/s     # use ABS()
        else:
            deg_ratio[u] = 1.0/s
            
    # sort ascending (index sort)
    idx = [i[0] for i in sorted(enumerate(deg_ratio), key=lambda x:x[1])]
            
    # find max deg
    max_u = idx[n_nodes-1]
    max_deg = new_deg_seq[max_u]
    print "new_deg_seq[max_u] =", new_deg_seq[max_u]
    
    # adjust 
    ceil_s = 0
    for u in range(n_nodes):
        ceil_s += int(math.ceil(deg_ratio[u]*s))
    n_ceil = n_nodes - (ceil_s - s)   
    print "n_ceil =", n_ceil    
    
#    floor_s = 0
#    for u in range(n_nodes):
#        floor_s += int(math.floor(deg_ratio[u]*s))
#    n_floor = n_nodes - (floor_s - s)   
#    print "n_floor =", n_floor
    
    for i in range(n_nodes):
        u = idx[i]
        if i < n_ceil:
            new_deg_seq[u] = int(math.ceil(deg_ratio[u]*s))    # ceiling
        else:     
            new_deg_seq[u] = int(math.ceil(deg_ratio[u]*s)) - 1
        
    print "new_deg_seq[max_u] =", new_deg_seq[max_u]
    
    #
    return new_deg_seq 
    
########################################################
# deg_seq: noisy degree sequence
def dk1_init(deg_seq, filename_1k, outfile_1k, filename_deg):
    
    #
    
    
    # write noisy degrees
    write_deg_seq(deg_seq, filename_1k)
    print "write_deg_seq - DONE"
    write_node_deg(deg_seq, filename_deg)
    print "write_node_deg - DONE"
    
    #
    cmd1k = DKTOPOGEN1K_PATH + " -i " + filename_1k + " > " + outfile_1k
    call(cmd1k, shell=True)     # shell = True
    
########################################################
# reorder node ids in outfile_1k to match original degrees in filename_deg
def reorder_nodes(n_nodes, outfile_1k, filename_deg):

    aG = nx.read_edgelist(outfile_1k, '#', ' ', None, nodetype=int)
    aG.add_nodes_from(range(n_nodes))
    print "aG"
    print "#nodes =", aG.number_of_nodes()
    print "#edges =", aG.number_of_edges()
    
    # original degrees
    deg_list = read_node_deg(filename_deg)
    org_pairs = []
    for u in range(n_nodes):
        org_pairs.append((u, deg_list[u]))
        
    org_pairs = sorted(org_pairs, key=lambda pair: pair[1])     # sort by degree
    
    # degrees in .gen file
    gen_pairs = []
    for (u, deg) in aG.degree().iteritems():
        gen_pairs.append((u,deg))
    
    gen_pairs = sorted(gen_pairs, key=lambda pair: pair[1])     # sort by degree    
    
    # map
    node_map = {}
#    print "node_map"
    for u in range(n_nodes):
        node_map[u] = org_pairs[u][0]
        # debug
#        print u, gen_pairs[u][1], org_pairs[u][0], org_pairs[u][1] 
    
    G0 = nx.Graph()
    G0.add_nodes_from(range(n_nodes))
    for (u,v) in aG.edges_iter():
        G0.add_edge(node_map[u], node_map[v])
    #
    return G0

########################################################
# edit distance score (half)
def init_score(G, G0):
    score = 0 
    for e in G.edges_iter():
        if not G0.has_edge(e[0],e[1]):
            score += 1
#    for e in G0.edges_iter():
#        if not G.has_edge(e[0],e[1]):
#            score += 1
    #
    return score

########################################################
def edge_switch(G0, G, cur_score, u,v,w,t):
    G0.add_edge(u, t)
    G0.add_edge(v, w)
    G0.remove_edge(u, w)
    G0.remove_edge(v, t)
    
    # update score
    score = cur_score
    if G.has_edge(u, t):
        score -= 1
    if G.has_edge(v, w):
        score -= 1 
    if G.has_edge(u, w):
        score += 1
    if G.has_edge(v, t):
        score += 1 
    #
    return score

########################################################
def edge_switch_simple(G0, u,v,w,t):
    G0.add_edge(u, t)
    G0.add_edge(v, w)
    G0.remove_edge(u, w)
    G0.remove_edge(v, t)

########################################################
# multiple or one switch
def random_switch(G0, G, n_nodes, node_list, cur_score, max_switch):
    # find a random pair
    count = 0
    switch_list = []        # list of (u,v,w,t)
    n_switch = 5 + random.randint(max_switch)
    
    score = cur_score
    while count < n_switch:
        u = node_list[random.randint(0, n_nodes-1)]
        v = node_list[random.randint(0, n_nodes-1)]
        if u != v and G0.degree(u) > 0 and G0.degree(v) > 0:
            u_neighbors = G0.neighbors(u)
            v_neighbors = G0.neighbors(v)
            u_num_nbrs = len(u_neighbors)
            v_num_nbrs = len(v_neighbors)
            # select w,t
            w = u_neighbors[random.randint(0, u_num_nbrs)]
            t = v_neighbors[random.randint(0, v_num_nbrs)]
            if w != v and t != u and w != t and (not G0.has_edge(u, t)) and (not G0.has_edge(v, w)):
                # fast
#                print u,v,w,t
                score = edge_switch(G0, G, score, u,v,w,t)      # not cur_score in param list !
                switch_list.append((u,v,w,t))
                
                count += 1
                
                # debug
#                edge_switch_simple(G0, u,v,w,t)
#                score = init_score(G, G0)
                #
#                break
    
    #
#    return score, u,v,w,t
    return score, switch_list

########################################################
# start with G0
def mcmc_sampling(G, G0, eps2, n_steps, n_samples, sample_freq, max_switch):
    n_nodes = G0.number_of_nodes()
    node_list = list(G0.nodes_iter())
    
#    dU = n_nodes*(n_nodes-1)    # O(n^2)
#    dU = math.log(n_nodes*(n_nodes-1))
    dU = 2.0    
    out_freq = (n_steps + n_samples*sample_freq)/10
    
    print "#steps =", n_steps + n_samples*sample_freq
    
    cur_score = init_score(G, G0)
    #
    start = time.clock()
    n_accept = 0
    n_accept_positive = 0
    n_equal = 0
    
    
    for i in range(n_steps + n_samples*sample_freq):
        if i % out_freq == 0:
            print "i =", i, "cur_score =", cur_score, "n_accept =", n_accept, "n_accept_positive =", n_accept_positive, \
                "time =", time.clock() - start
        
#        score, u,v,w,t = random_switch(G0, G, n_nodes, node_list, cur_score)
        score, switch_list = random_switch(G0, G, n_nodes, node_list, cur_score, max_switch)
        
        # debug
        if cur_score == score:
            n_equal += 1
            
        if score < cur_score:
            cur_score = score
            n_accept += 1
            n_accept_positive += 1
        else:
            prob = math.exp(eps2/(2*dU)*(cur_score-score))
            prob_val = random.random()
            if prob_val < prob:
                # accept
                cur_score = score
                n_accept += 1
            else:  
                # reverse
    #            edge_switch_simple(G0, u,v,t,w)
                if len(switch_list) > 0:
                    for (u,v,w,t) in switch_list[::-1]:     # reverse list
                        edge_switch_simple(G0, u,v,t,w)     # IMPORTANT: t,w NOT w,t
    #
    print "n_accept =", n_accept
    print "n_equal =", n_equal
    print "final score =", cur_score
    print "check final score =", init_score(G, G0)          # OK
    


########################################################
if __name__ == '__main__':  
    
    dataname = "polbooks"           # (105,441)        105k steps, max_switch = 105 (90s)
#    dataname = "polblogs"           # (1224,16715) 
#    dataname = "as20graph"          # (6474,12572)    
#    dataname = "wiki-Vote"          # (7115,100762)   
#    dataname = "ca-HepPh"           # (12006,118489)     
#    dataname = "ca-AstroPh"         # (18771,198050)    
#    dataname = "com_amazon_ungraph" # (334863,925872)  1k-series: 13s    (isotonic: 24s, adjust: 2s)
#    dataname = "com_dblp_ungraph"   # (317080,1049866)  1k-series: 15s   
#    dataname = "com_youtube_ungraph"# (1134890,2987624) 1k-series: 5064s !!! (64bit: 11.5h)
    
    ######  Command-line param for automation (bash): <dataname> <n_samples> <eps>
    n_samples = 10
    eps = 2.0
    
    if len(sys.argv) > 1:
        dataname = sys.argv[1]
    if len(sys.argv) > 2:
        n_samples = int(sys.argv[2])
    if len(sys.argv) > 3:
        eps = float(sys.argv[3])
        
    print "dataname =", dataname
    print "n_samples =", n_samples
    print "eps =", eps
    
    filename = "../_data/" + dataname + ".gr"       
    filename_1k = "../_out/" + dataname + "_noisy.1k"
    
    filename_deg = filename_1k[0:-3] + ".deg"
    outfile_1k = filename_1k[0:-3] + ".gen"
    
    print "filename =", filename
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    n_nodes = G.number_of_nodes()
    
    # TEST naive_deg_sequence(), adjust_deg_seq()
    deg_dict = G.degree()
    deg_seq = list(deg_dict.itervalues())
    print "max_deg =", max(deg_seq)
    
#    for i in range(n_samples):
#        print "sample", i
#        
#        ## NOISY DEGREE SEQUENCE
###        sample_file = "../_sample/" + dataname + "_orbis_1k_" + str(eps) + "." + str(i)
##        sample_file = "../_sample/" + dataname + "_config_1k_" + str(eps) + "." + str(i)    # configuration_model()
###        sample_file = "../_sample/" + dataname + "_hakimi_1k_" + str(eps) + "." + str(i)    # havel_hakimi_graph()
##        
##        noisy_deg_seq = naive_deg_sequence(deg_seq, eps)
##        
##        print "sum(deg_seq) =", sum(deg_seq)
##        print "sum(noisy_deg_seq) =", sum(noisy_deg_seq) 
###        print noisy_deg_seq
##        n_neg = 0
##        for d in noisy_deg_seq:
##            if d < 0:
##                n_neg +=1
##        print "n_neg =", n_neg
##        
##        if n_neg > 0:
##            ad_deg_seq = adjust_sorted_deg_seq(noisy_deg_seq)
##            print "adjust_sorted_deg_seq - DONE"
##            print "sum(ad_deg_seq) =", sum(ad_deg_seq)
###            print ad_deg_seq 
##            n_neg = 0
##            for d in ad_deg_seq:
##                if d < 0:
##                    n_neg +=1
##            print "n_neg =", n_neg
##            
##            # reconstruct (write to sample_file) using dkTopoGen1k
###            start = time.clock()
###            dk1_init(ad_deg_seq, filename_1k, sample_file, filename_deg)
###            print "dk1_init - DONE"
###            print "1k-series reconstruct - DONE, elapsed", time.clock() - start
##            
##            # reconstruct (write to sample_file) using config_model/havel_hakimi
##            start = time.clock()
##            aG = nx.configuration_model(ad_deg_seq, None, None)
###            aG = nx.havel_hakimi_graph(ad_deg_seq, None)
##
##            aG = nx.Graph(aG)                           # remove parallel edges, selfloops
##            aG.remove_edges_from(aG.selfloop_edges())
##            nx.write_edgelist(aG, sample_file, "#", "\t", False, 'utf-8')
##            print "1k-series reconstruct - DONE, elapsed", time.clock() - start
##            
##        else:
##            # reconstruct (write to sample_file) using dkTopoGen1k
###            start = time.clock()
###            dk1_init(noisy_deg_seq, filename_1k, sample_file, filename_deg)
###            print "dk1_init - DONE"
###            print "1k-series reconstruct - DONE, elapsed", time.clock() - start    
##        
##            # reconstruct (write to sample_file) using config_model/havel_hakimi
##            start = time.clock()
##            aG = nx.configuration_model(noisy_deg_seq, None, None)
###            aG = nx.havel_hakimi_graph(noisy_deg_seq, None)
##
##            aG = nx.Graph(aG)                           # remove parallel edges, selfloops
##            aG.remove_edges_from(aG.selfloop_edges())
##            nx.write_edgelist(aG, sample_file, "#", "\t", False, 'utf-8')
##            print "1k-series reconstruct - DONE, elapsed", time.clock() - start
#        
#        ## TRUE DEGREE SEQUENCE
#        sample_file = "../_sample/" + dataname + "_orbis_1k." + str(i)
##        sample_file = "../_sample/" + dataname + "_config_1k." + str(i)    # configuration_model()
#
#
#        # reconstruct (write to sample_file) using dkTopoGen1k
#        start = time.clock()
#        dk1_init(deg_seq, filename_1k, sample_file, filename_deg)
#        print "dk1_init - DONE"
#        print "1k-series reconstruct - DONE, elapsed", time.clock() - start
#
#        # reconstruct (write to sample_file) using config_model
##        start = time.clock()
##        aG = nx.configuration_model(deg_seq, None, None)
##
##        aG = nx.Graph(aG)                           # remove parallel edges, selfloops
##        aG.remove_edges_from(aG.selfloop_edges())
##        nx.write_edgelist(aG, sample_file, "#", "\t", False, 'utf-8')
##        print "1k-series reconstruct - DONE, elapsed", time.clock() - start
        
        
        
        
    # TEST isotonic_deg_sequence()
#    P.plot(deg_seq, 'g')
#    noisy_deg_seq = naive_deg_sequence(deg_seq, eps)
#    P.plot(noisy_deg_seq, 'r')

    start = time.clock()
    s, s1, sorted_deg_seq = isotonic_deg_sequence(deg_seq, eps)
    print "isotonic_deg_sequence - DONE, elapsed", time.clock() - start
    start = time.clock()
    ad_deg_seq = adjust_sorted_deg_seq(s1)
    print "adjust_sorted_deg_seq - DONE, elapsed", time.clock() - start
    print "sum(sorted_deg_seq) =", sum(sorted_deg_seq)
    print "sum(s1) =", sum(s1) 
    print "sum(s) =", sum(s)
    print "sum(ad_deg_seq) =", sum(ad_deg_seq)    
    P.plot(sorted_deg_seq, 'g')
    P.plot(s1, 'r')
    P.plot(s, 'b')
    P.plot(sorted(ad_deg_seq), 'c')
    P.show()

        
        
    # TEST reorder_nodes()
#    G0 = reorder_nodes(n_nodes, outfile_1k, filename_deg)
#    print "degrees after reordering :", nx.degree(G0)

     
     
    ################################################### 
    # TEST init_score()
#    cur_score = init_score(G, G0)
#    print "score =", cur_score
    
    # TEST mcmc_sampling()
#    eps2 = 4.0        # for NON-PRIVATE search
##    eps2 = 1.0
#    n_samples = 5
#    sample_freq = 100
#    burn_factor = 100
#    max_switch = 10
#    print "eps2 =", eps2
#    print "burn_factor =", burn_factor
#    print "n_samples =", n_samples
#    print "sample_freq =", sample_freq
#    print "max_switch =", max_switch
#    
#    start = time.clock()
#    mcmc_sampling(G, G0, eps2, burn_factor*G.number_of_nodes(), n_samples, sample_freq, max_switch)
#    print "mcmc_sampling - DONE, elapsed", time.clock() - start
    
    
    