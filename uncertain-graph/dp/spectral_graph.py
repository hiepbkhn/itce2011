'''
Created on Feb 12, 2015

@author: Nguyen Huu Hiep
paper: Differential Privacy Preserving Spectral Graph Analysis (PAKDD'13)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import * 
from adj_matrix_visualizer import draw_adjacency_matrix

K_TOP_EIGEN = 5

#######################################################
# top-k eigenvalues/eigenvectors
def compute_spectral_coordinate(G, k):
    A = nx.adjacency_matrix(G)
    N = A.shape[0]
    
    w, v = eigh(A, None, True, False, False, False, True, (N-k, N-1))
    #
    return w, v 

#######################################################
def export_matrix_to_matlab(G, filename):
    A = nx.adjacency_matrix(G)
    
    scipy.io.savemat(filename, dict(A=A) )
    
#######################################################
# eps_0: for eigenvalues, eps_1 for eigenvectors
def laplace_mechanism(G, eps_0, eps_1):
    n_nodes = G.number_of_nodes()
    
    eig_vals, eig_vecs = compute_spectral_coordinate(G, K_TOP_EIGEN)
    # eigenvalues (following Result 1)
    scale = math.sqrt(2*K_TOP_EIGEN)/eps_0
    noisy_vals = eig_vals + np.random.laplace(0.0, scale)               # Laplace noise
    
    # eigenvectors (following Result 2)
    noisy_vecs = np.ndarray(shape=eig_vecs.shape, dtype=float64)
    scale_0 = math.sqrt(n_nodes)/abs(eig_vals[0]-eig_vals[1])
    noisy_vecs[:,0] = eig_vecs[:,0] + np.random.laplace(0.0, scale_0)
    
    for k in range(1,K_TOP_EIGEN-1):
        scale_k = math.sqrt(n_nodes)/min(abs(eig_vals[k-1]-eig_vals[k]),abs(eig_vals[k]-eig_vals[k+1]))
        scale_k = scale_k/eps_1
        noisy_vecs[:,k] = eig_vecs[:,k] + np.random.laplace(0.0, scale_k)
    scale_k = math.sqrt(n_nodes)/abs(eig_vals[K_TOP_EIGEN-2]-eig_vals[K_TOP_EIGEN-1])
    scale_k = scale_k/eps_1
    noisy_vecs[:,K_TOP_EIGEN-1] = eig_vecs[:,K_TOP_EIGEN-1] + np.random.laplace(0.0, scale_k)
    
    # normalize and orthogonalize noisy_vecs
    for k in range(K_TOP_EIGEN):
        noisy_vecs[:,k] = noisy_vecs[:,k]/np.linalg.norm(noisy_vecs[:,k], 2)
        
    C = np.dot(noisy_vecs.transpose(), noisy_vecs)      # C=inv(X'*X)
    U = np.dot(noisy_vecs, np.linalg.inv(C))
    
    #
    return noisy_vals, U

#######################################################
if __name__ == '__main__':
    filename = "../_data/polbooks.gr"
    
    G = nx.read_edgelist(filename, '#', '\t', None, nodetype=int)
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    
    eig_vals, eig_vecs = compute_spectral_coordinate(G, K_TOP_EIGEN)
    
#    print eig_vals
##    print type(eig_vecs)        # <type 'numpy.ndarray'>
##    print eig_vecs.shape        # (105,5)
#    print eig_vecs[:,0]
#    print eig_vecs.dtype
    
    noisy_vals, U = laplace_mechanism(G, 1.0, 50.0)
    print noisy_vals
    
    # plot
#    draw_adjacency_matrix(G)
    
    
    # TEST
#    export_matrix_to_matlab(G, "C:/polbooks.mat")
    