'''
Created on Oct 28, 2014

@author: huunguye
paper: Distance-Constraint Reachability Computation in Uncertain Graphs (VLDB'11)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from itertools import chain, combinations

#######################################################
def findsubsets(S,m):
    return set(combinations(S, m))

#######################################################
def powerset(iterable):
    xs = list(iterable)
    # note we return an iterator rather than a list
    return chain.from_iterable( combinations(xs,n) for n in range(len(xs)+1) )


#######################################################
def instance_prob(G,aG):
    p = 1.0
    for e in G.edges_iter(data=True):
        if aG.has_edge(e[0], e[1]):
            p = p * e[2]['p']
        else:
            p = p * (1-e[2]['p'])
    return p

#######################################################
def reachability_prob(G,s=0,t=0):
    R_d = 0.0       # reachability from s to t
    count = 0
    for edge_set in list(powerset(G.edges_iter(data=True))):
#        print "count=", count
        count = count + 1
        
        aG = nx.DiGraph()
        aG.add_nodes_from(G.nodes_iter())
        aG.add_edges_from(edge_set)
        
#        print "#nodes=", aG.number_of_nodes()
#        print aG.edges(data=True)

        # compute probability of aG
        
#        print "p(aG) =", p
        #
        if nx.has_path(aG, s, t):
            R_d = R_d + instance_prob(G,aG)
    
    #
#    print "s =", s, "t =", t
#    print "R_d =", R_d

    return R_d

#######################################################
def generating_prob(G,E1,E2):
    p = 1.0
    for e in E1:
        p = p * e[2]['p']
    for e in E2:
        p = p * (1-e[2]['p'])
        
    return p

#######################################################
def prefix_reachability_prob(G,E1,E2,s=0,t=0):
    Ed = []
    for e in G.edges_iter(data=True):
        existed = False
        for e1 in E1:
            if (e[0]==e1[0]) and (e[1]==e1[1]):
                existed = True
                break  
        if existed:
            continue
        for e2 in E2:
            if (e[0]==e2[0]) and (e[1]==e2[1]):
                existed = True
                break  
        if not existed:
            Ed.append(e)
    #
    R_pd = 0.0
    for edge_set in list(powerset(Ed)):
        aG = nx.DiGraph()
        aG.add_nodes_from(G.nodes_iter())
        aG.add_edges_from(E1)
        aG.add_edges_from(edge_set)
        
        # compute probability of aG
        p = 1.0
        for e in G.edges_iter(data=True):
            if aG.has_edge(e[0], e[1]):
                p = p * e[2]['p']
            else:
                p = p * (1-e[2]['p'])
        #
        if nx.has_path(aG, s, t):
            R_pd = R_pd + p
    #
    p_E12 = generating_prob(G,E1,E2)
#    print "p_E12 =", p_E12
    
    return R_pd/p_E12

    

#######################################################
def check_lemma1(G,E1,E2,e,s,t):
    R = prefix_reachability_prob(G,E1,E2,s,t)
    E1n = []
    E1n.extend(E1)
    E1n.append(e)
    R1 = prefix_reachability_prob(G,E1n,E2,s,t)
    E2n = []
    E2n.extend(E2)
    E2n.append(e)
    R2 = prefix_reachability_prob(G,E1,E2n,s,t)
    
    #
    print "R =", R
    print "R1 =", R1
    print "R2 =", R2
    p = e[2]['p']
    print "p =", p
    print "e*R1 + (1-e)*R2 =", p*R1 + (1-p)*R2
    

#######################################################
if __name__ == '__main__':
    G = nx.DiGraph()
#    G.add_edges_from([(0,1,{'p':0.5}),(1,2,{'p':0.6}),(0,2,{'p':0.7})])
#    # TEST check_lemma1
#    print check_lemma1(G, [], [], (0,1,{'p':0.5}), s=0, t=2)
#    print check_lemma1(G, [], [], (1,2,{'p':0.6}), s=0, t=2)

    G.add_edges_from([(0,1,{'p':0.5}),(1,2,{'p':0.6}),(0,3,{'p':0.8}),(3,2,{'p':0.7})])
    # TEST check_lemma1
    print check_lemma1(G, [], [], (0,1,{'p':0.5}), s=0, t=2)
    print check_lemma1(G, [], [], (1,2,{'p':0.6}), s=0, t=2)
    
#    G.add_edges_from([(0,1,{'p':0.5}),(1,2,{'p':0.6}),(0,3,{'p':0.8}),(3,2,{'p':0.7}),(1,3,{'p':0.3})])
    # TEST check_lemma1
    print check_lemma1(G, [], [], (0,1,{'p':0.5}), s=0, t=2)
    print check_lemma1(G, [], [], (1,2,{'p':0.6}), s=0, t=2)
    
#    print "#nodes=", G.number_of_nodes()
#    print "#edges=", G.number_of_edges()
#    print G.edges(data=True)
#    print nx.has_path(G, 0, 2)
#    print nx.has_path(G, 2, 0)
    
    # TEST powerset()
#    print  list(powerset(G.edges_iter()))
    
    #
#    check_lemma1(G,s=0,t=2)

    # 
#    print reachability_prob(G,s=0,t=2)
#    print prefix_reachability_prob(G, [(0,2,{'p':0.7})], [],s=0,t=2)
#    print prefix_reachability_prob(G, [], [(1,2,{'p':0.6})],s=0,t=2)
    
    # TEST check_lemma1
    print check_lemma1(G, [], [], (0,1,{'p':0.5}), s=0, t=2)
    print check_lemma1(G, [], [], (1,2,{'p':0.6}), s=0, t=2)
    
    
    