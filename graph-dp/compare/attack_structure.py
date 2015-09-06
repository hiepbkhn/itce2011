'''
Created on Mar 21, 2014

@author: huunguye
References:
- Resisting Structural Reidentification Anonymized Social Networks (VLDB'08)
- Quantifying Location Privacy (S&P'11)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io


#######################################################
# G: deterministic
# bins: [0-1, 2-4, 5-10, 11-20, 21-inf], inf = 10^9
def equivalence_class_H1(G, bins):
    
    deg_dict = nx.degree(G)
    # counting 
    max_deg = max(deg_dict.itervalues())
    
    sig_list = [i for i in range(max_deg+1)]        # sig_list[i] <--> bucket_list[i], including deg-0
    bucket_list = [[] for i in range(max_deg+1)]    # list of lists

    for (u, deg) in deg_dict.iteritems():
        bucket_list[deg].append(u)
    
    # compute candidate list size for each node u, then arrange cand_size to bins
    cand_size = {}
    bin_size = [0 for i in range(len(bins)-1)]
    for deg in range(max_deg+1):
        for u in bucket_list[deg]:
            cand_size[u] = len(bucket_list[deg])
            #
            for i in range(len(bins)-1):
                if bins[i] <= cand_size[u] and cand_size[u] <= bins[i+1]:
                    bin_size[i] += 1
                    break
    
    #
    return cand_size, bin_size, sig_list, bucket_list


#######################################################
# -1: smaller, 0: equal, +1: larger
def list_comparator(x, y):
    n = min(len(x),len(y))
    for i in range(n):
        if x[i] < y[i]:
            return -1
        if x[i] > y[i]:
            return 1
    if len(x) < len(y):
        return -1
    elif len(x) > len(y):
        return 1
    else:
        return 0
    

#######################################################
# h2_list: already sorted
def bucket_H2(h2_list, bins=None):
    
    sig_list = []           # list of signatures, each sig is a list !
    bucket_list = []        # list of lists
    
    # 1-
    interval_list = []
    
    i = 0    
    while i < len(h2_list):
        u = h2_list[i][0]
        u_list = h2_list[i][1]
        
        sig_list.append(u_list)
        bucket_list.append([u])
        
        j = i+1
        while j < len(h2_list):
            v = h2_list[j][0]
            v_list = h2_list[j][1]
            order = list_comparator(u_list, v_list)
            if order == 0:
                bucket_list[-1].append(v)
                j += 1
            else:
                break
        #
        interval_list.append((i,j-1))
        
        if j == len(h2_list):
            break
        
        # next interval
        i = j
        
    # 2-
    cand_size = {}      # cand_size[u]: number of nodes having the same signature as u's
    for i in range(len(interval_list)):
        lo = interval_list[i][0]
        hi = interval_list[i][1]
        for j in range(lo,hi+1):
            u = h2_list[j][0]
            cand_size[u] = hi+1-lo
        
    #
    interval_size = [item[1]-item[0]+1 for item in interval_list]

    if bins is None:
        max_size = max(interval_size)
        bin_size = [0 for i in range(max_size+1)]
        for s in interval_size:
            bin_size[s] += s
            
    else:
        bin_size = [0 for i in range(len(bins)-1)]
        for s in interval_size:
            for i in range(len(bins)-1):
                if bins[i] <= s and s <= bins[i+1]:
                    bin_size[i] += s
                    break
    
    #
    return cand_size, bin_size, sig_list, bucket_list
    

#######################################################
# G: deterministic    
# Closed World: exact info about node neighbors
def equivalence_class_H2_closed(G, bins=None):
    
    deg_dict = nx.degree(G)
    #
    h2_dict = {}        # dict of lists, h2_dict[u] = {1,2,2,3}...
    for u in G.nodes():
        h2_dict[u] = []
        for v in G.neighbors(u):
            h2_dict[u].append(deg_dict[v])
            
        # sort h2_dict[u]
        h2_dict[u] = sorted(h2_dict[u])
        
    
    # sort h2_dict[u]
    h2_list = []
    for (u, h2) in h2_dict.iteritems():
        h2_list.append((u, h2))
    
    h2_list = sorted(h2_list, cmp=list_comparator, key=lambda item:item[1])
            
    #
    return h2_list        
    
#######################################################
# G: deterministic    
# Open World: contracted info about node neighbors, e.g. [1,1,2,2,2,3,4,4] -> [1,2,3,4]
def equivalence_class_H2_open(G, bins=None):
    
    deg_dict = nx.degree(G)
    #
    h2_dict = {}        # dict of lists, h2_dict[u] = {1,2,2,3}...
    for u in G.nodes():
        h2_dict[u] = []
        for v in G.neighbors(u):
            h2_dict[u].append(deg_dict[v])
            
        # sort h2_dict[u], then extract unique values, e.g. [1,1,2,2,2,3,4,4] -> [1,2,3,4]
        h2_dict[u] = sorted(h2_dict[u])
        i = 1
        while i < len(h2_dict[u])-1:
            if h2_dict[u][i-1] == h2_dict[u][i]:
                del h2_dict[u][i-1]
            else:
                i += 1
    # sort h2_dict[u]
    h2_list = []
    for (u, h2) in h2_dict.iteritems():
        h2_list.append((u, h2))
    
    h2_list = sorted(h2_list, cmp=list_comparator, key=lambda item:item[1])
    
    #
    return h2_list       

#######################################################
if __name__ == '__main__':
    
#    filename = "../data/er_200_02.gr"           # 4002 edges
#    filename = "../data/er_500_005.gr"          # 6117 edges
    filename = "../data/er_10000_0001.gr"       # 50424 edges
#    filename = "../data/er_10000_0002.gr"       # 99615 edges   
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    
    
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    deg_list = nx.degree(G)         # dict[node] = deg
    min_deg = min(deg_list.itervalues())
    max_deg = max(deg_list.itervalues())
    print "min-deg =", min_deg
    print "max-deg =", max_deg
    print "avg-deg =", sum(list(deg_list.itervalues()))/float(G.number_of_nodes())
    
    
    # TEST equivalence_class_H1()
#    bins = [0,1,4,10,20,100000000]
#    cand_size, bin_size, sig_list, bucket_list = equivalence_class_H1(G, bins)
#    print bin_size

    # TEST equivalence_class_H2_closed()
#    h2_list = equivalence_class_H2_closed(G)
#    print h2_list
    
    # TEST equivalence_class_H2_open()
    h2_list = equivalence_class_H2_open(G)
#    print h2_list


#    h2_list = [(1,[1,2]),(2,[1,2]),(3,[1,2,4]),(4,[1,2,4]),(5,[1,2,4]),(6,[1,3,4])]
    bins = [0,1,4,10,20,100000000]
    cand_size, bin_size, sig_list, bucket_list = bucket_H2(h2_list, bins)
    print bin_size

    print "DONE"
    
    
    
