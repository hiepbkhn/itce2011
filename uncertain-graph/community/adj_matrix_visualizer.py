'''
Created on Feb 3, 2015

@author: huunguye
- http://sociograph.blogspot.fr/2012/11/visualizing-adjacency-matrices-in-python.html?view=sidebar

'''

import networkx as nx
import numpy as np
import igraph as ig
from scipy import io
from matplotlib import pyplot, patches

#######################################################
def draw_adjacency_matrix(G, node_order=None, partitions=[], colors=[]):
    """
    - G is a networkx graph
    - node_order (optional) is a list of nodes, where each node in G
          appears exactly once
    - partitions is a list of node lists, where each node in G appears
          in exactly one node list
    - colors is a list of strings indicating what color each
          partition should be
    If partitions is specified, the same number of colors needs to be
    specified.
    """
    adjacency_matrix = nx.to_numpy_matrix(G, dtype=np.bool, nodelist=node_order)

    #Plot adjacency matrix in toned-down black and white
    fig = pyplot.figure(figsize=(5, 5)) # in inches
    ax = fig.add_subplot(111)
    ax.imshow(adjacency_matrix,
                  cmap="Greys",
                  interpolation="none")
    pyplot.show()
    
    # The rest is just if you have sorted nodes by a partition and want to
    # highlight the module boundaries
    assert len(partitions) == len(colors)
    ax = pyplot.gca()
    for partition, color in zip(partitions, colors):
        current_idx = 0
        for module in partition:
            ax.add_patch(patches.Rectangle((current_idx, current_idx),
                                          len(module), # Width
                                          len(module), # Height
                                          facecolor="none",
                                          edgecolor=color,
                                          linewidth="1"))
            current_idx += len(module)
            
            
#######################################################
if __name__ == '__main__':
    
#     A = io.mmread("../_data/Caltech.mtx")
#     G = nx.from_scipy_sparse_matrix(A)
#     draw_adjacency_matrix(G)

    PATH = "F:/Tailieu/Paper-code/DATA-SET/Network data - Newman (Umich)/"
    FILE_NAME = PATH + "karate/karate.gml"
    iG = ig.Graph.Read(FILE_NAME, format="gml")
    
    print "#nodes", iG.vcount()
    print "#edges", iG.ecount()   

    G = nx.read_gml(FILE_NAME)
    draw_adjacency_matrix(G, node_order=[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
                                         18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34])
    
#     draw_adjacency_matrix(G, node_order=[1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 17, 18, 20, 22, 
#                                          9, 15, 16, 19, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34])
# 
#     draw_adjacency_matrix(G, node_order=[34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,
#                                          17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1])

                