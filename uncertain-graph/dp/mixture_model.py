'''
Created on Apr 29, 2015

@author: huunguye
    - Mixture models and exploratory analysis in networks (PNAS, 2007)
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
import matplotlib.pylab as plt


########################################################
def read_theta_q(r, n_nodes, filename):
    THETA_LINE = 5
    Q_LINE = THETA_LINE + n_nodes + 2
    
    theta = np.ndarray(shape=(r, n_nodes), dtype=float)
    q = np.ndarray(shape=(r, n_nodes), dtype=float)
    
    f = open(filename, "r")
    fstr = f.read()
    f.close() 

    lines = fstr.split("\n")
    
    for i in range(n_nodes):
        items = lines[THETA_LINE + i].split(" ")
        for j in range(r):
            theta[j][i] = float(items[j])
            
    for i in range(n_nodes):
        items = lines[Q_LINE + i].split(" ")
        for j in range(r):
            q[j][i] = float(items[j])
    
    return theta, q

########################################################
def reconstruct(A2, n_nodes, deg_dict):
    
    # 1-based (e.g. karate)
#    G2 = nx.Graph()
#    G2.add_nodes_from(range(1,n_nodes+1))
#    count = 0
#    for i in range(n_nodes):
#        for j in range(n_nodes):
#            val = random.random()
#            if val < A2[i][j]*deg_dict[i+1]*0.5:
#                count += 1
#                G2.add_edge(i+1,j+1)
    
    # 0-based (e.g. 
    G2 = nx.Graph()
    G2.add_nodes_from(range(n_nodes))
    count = 0
    for i in range(n_nodes):
        for j in range(n_nodes):
            val = random.random()
            if val < A2[i][j]*deg_dict[i]*0.5:
                count += 1
                G2.add_edge(i,j)
                
    #
    return G2

########################################################
def edit_distance(G, G2):
    d = 0
    for (u,v) in G.edges_iter():
        if not G2.has_edge(u,v):
            d += 1
    for (u,v) in G2.edges_iter():
        if not G.has_edge(u,v):
            d += 1
    #
    return d
    
########################################################
if __name__ == '__main__':
    
    dataname = "karate"
#    dataname = "adjnoun"
    dataname = "polbooks"
#    dataname = "polblogs"
#    dataname = "as20graph"

    graph_file = "../_data/" + dataname + ".gr"
    out_file = "../../uncertain-graph-java/_data/" + dataname + ".out"
    
#    G = nx.read_gml(graph_file)
    G = nx.read_edgelist(graph_file, '#', '\t', None, nodetype=int)
    
    print "is_directed =", G.is_directed()
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    n_nodes = G.number_of_nodes()
    r = 2
    theta, q = read_theta_q(r, n_nodes, out_file)
    
    # compute A2_ij
#    A2 = np.ndarray(shape=(n_nodes, n_nodes), dtype=float)
    
    A2 = np.dot(np.transpose(q), theta)
    
    
    
#    print A2
#    print A2.sum(axis=1)    # row sum    = 1
#    print A2.sum(axis=0)    # col sum
    
    # node 1
    print G.neighbors(1)
#    print A2[0,:]
    
    # reconstruct G2
    print "deg_dict =", G.degree()
    start = time.clock()
    G2 = reconstruct(A2, n_nodes, G.degree())
    print "reconstruct - DONE, elapsed", time.clock() - start
    print "#nodes =", G2.number_of_nodes()
    print "#edges =", G2.number_of_edges()
    
    print "edit distance(G,G2) =", edit_distance(G, G2)
    
    # plot
#    pos = nx.spring_layout(G2)
#    color = ["red", "green", "blue", "cyan", "white", "black", "yellow", "magenta"]
#    count = 0
#    clusters = [[1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 17, 18, 20, 22], [9, 15, 16, 19, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34]] # karate
##    clusters = [[0, 1, 2, 3, 4, 5, 8, 9, 15, 19, 20, 22, 25, 26, 27, 39, 40, 41, 42, 43, 45, 46, 56, 60, 61, 62, 63, 64, 71, 73, 76, 77, 79, 80, 
##                 81, 83, 85, 88, 90, 91, 92, 94, 95, 96, 97, 98, 100, 101, 106, 109, 110, 111], 
##                [6, 7, 10, 11, 12, 13, 14, 16, 17, 18, 21, 23, 24, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 44, 47, 48, 49, 50, 51, 52, 53, 54, 
##                 55, 57, 58, 59, 65, 66, 67, 68, 69, 70, 72, 74, 75, 78, 82, 84, 86, 87, 89, 93, 99, 102, 103, 104, 105, 107, 108]]                 # adjnoun 
#    for com in clusters:
#        count += 1
#        nx.draw_networkx_nodes(G2, pos, com, node_size = 100, node_color = color[count % 8])
#    nx.draw_networkx_edges(G2, pos, alpha=0.5)
#    plt.show()
    
    
    
    
    
    