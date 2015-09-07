'''
Created on Oct 31, 2014

@author: huunguye
paper: k-Nearest Neighbors in Uncertain Graphs (VLDB'10)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from itertools import chain, combinations
from distance_constraint_reachability import powerset, instance_prob

#######################################################
def distance_distr(G,s=0,t=0):
    count = 0
    dist = [0.0 for _ in range(1, G.number_of_nodes())]   # distance distribution
    for edge_set in list(powerset(G.edges_iter(data=True))):
#        print "count=", count
        count = count + 1
        
        aG = nx.Graph()     # undirected
        aG.add_nodes_from(G.nodes_iter())
        aG.add_edges_from(edge_set)
        
        try:
            d = nx.shortest_path_length(aG, s, t)
            p = instance_prob(G,aG)
#            print "edge_set =", edge_set
#            print "d =", d, "p =", p
            dist[d] = dist[d]+p
        except nx.exception.NetworkXNoPath:
            pass
        
    #
    return dist    
            

#######################################################
if __name__ == '__main__':
    G = nx.Graph()      # undirected
    G.add_edges_from([(0,1,{'p':0.2}),(1,2,{'p':0.3}),(0,2,{'p':0.6}),(1,3,{'p':0.4}),(2,3,{'p':0.7})])
    
    # TEST distance_distr()
    dist = distance_distr(G,s=1,t=2)
    
    print dist