'''
Created on Feb 25, 2014

@author: huunguye
'''

import time
from random import *
import math
import networkx as nx
from entropy_obfuscation import compute_X, compute_Y, compute_entropy, sanitize

#######################################################
def find_max_assignment(G, edge_list, max_deg):
    G.add_edges_from(edge_list)
    w_list = [[u,v,0] for (u,v) in edge_list]
    max_ent = [0.0 for i in range(max_deg+1)]
    max_w_list = []
    while True:
        print "w_list =", w_list
        
        for (u,v,w) in w_list:
            G.edge[u][v]['w'] = w/10.0      # 
        X = compute_X(G, max_deg)
        Y = compute_Y(X, G.number_of_nodes(), max_deg)
        ent = compute_entropy(Y, G.number_of_nodes(), max_deg)
        #
        if sum(ent) > sum(max_ent):
            max_ent = ent
            max_w_list = []
            for item in w_list:
                max_w_list.append(item[2])
            
        # next assignments
        i = len(w_list) - 1
        while w_list[i][2] == 10 and i >= 0:
            i = i - 1
        if i == -1:
            break
        else:
            w_list[i][2] += 1
            for j in range(i+1, len(w_list)):
                w_list[j][2] = 0
    
    #
    print "max_ent =", max_ent
    print "max_w_list =", max_w_list
        

#######################################################
if __name__ == '__main__':
    # SAMPLE
    G = nx.Graph()      # undirected-graph
    G.add_edges_from([(1,2),(1,3),(1,4),(4,5)])
    G = sanitize(G)
    
    print G.edges(None, True)
    
    #
    deg_list = nx.degree(G)         
    max_deg = max(deg_list.itervalues())
    
    for v in G.nodes_iter():
        print G.neighbors(v)
    
    #
    X = compute_X(G, max_deg)
    Y = compute_Y(X, G.number_of_nodes(), max_deg)
    ent = compute_entropy(Y, G.number_of_nodes(), max_deg)
    
    print "X"
    print X
    print "Y"
    print Y
    print ent
    
    # asign edge probabilitites
    print "AFTER"
    G.edge[1][3]['w'] = 0.4
    G.edge[1][4]['w'] = 0.1
    G.add_edge(2,4)
    G.edge[2][4]['w'] = 0.4 
    
    X = compute_X(G, max_deg)
    Y = compute_Y(X, G.number_of_nodes(), max_deg)
    ent = compute_entropy(Y, G.number_of_nodes(), max_deg)
    
    print "X"
    print X
    print "Y"
    print Y
    print ent
    
    #
#    edge_list = [(1,3),(1,4),(2,4)]
#    find_max_assignment(G, edge_list, 3)
    
#    edge_list = [(1,3),(1,5),(2,4)]
#    find_max_assignment(G, edge_list, 4)
    
#    edge_list = [(1,3),(1,5),(2,4),(3,5)]
#    find_max_assignment(G, edge_list, 4)
    