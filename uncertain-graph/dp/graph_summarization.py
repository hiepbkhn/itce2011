'''
Created on Feb 27, 2015

@author: Nguyen Huu Hiep
paper: Differentially Private Spatial Decompositions (ICDE'12)
       Differentially Private Publication of Sparse Data (ICDT'12) 
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from scipy.linalg import *
import matplotlib.pylab as P 

#######################################################
def test_geom_distr(alpha):
    # GEOMETRIC DISTR
    X = np.random.geometric(alpha, 10000)
    
    # plot geometric distribution
    n_bins = 10
    n, bins, patches = P.hist(X, n_bins, range=(1,n_bins+1), normed=1, histtype='stepfilled')   # bins: ndarray of 51 elements
    P.setp(patches, 'facecolor', 'g', 'alpha', 0.75)
    print bins
    # add a line showing the expected distribution
    x = np.array(range(1, n_bins+1))    # x: 1 -> n_bins
    y = np.power(1-alpha, x-1)*alpha    # y = (1-alpha)^(x-1)*alpha
    l = P.plot(bins[0:n_bins], y, 'k--', linewidth=1.5)

    P.show()

#######################################################
# Pr[X=x] = (1-alpha)/(1+alpha)*alpha^|x|
def geometric_mechanism(alpha, n_samples):
    
    if n_samples == 1:
        u = random.random()
        if u <= (1-alpha)/(1+alpha):
            return 0
        else:
            s = random.random()
            if s < 0.5: 
                return -int(math.floor(math.log((1-u)*(1+alpha)/2) / math.log(alpha)))
            else:
                return int(math.floor(math.log((1-u)*(1+alpha)/2) / math.log(alpha)))
    #
    U = np.random.random(n_samples)     # uniform [0,1]
    X = np.zeros(n_samples)
    for i in range(n_samples):
        u = U[i]
        if u <= (1-alpha)/(1+alpha):
            X[i] = 0
        else:
            X[i] = int(math.floor(math.log((1-u)*(1+alpha)/2) / math.log(alpha)))
    
    S = np.random.random(n_samples)-0.5
    X = np.multiply(np.sign(S), X)
    #
    return X
    
#######################################################    
def test_geometric_mechanism(alpha, n_bins=10):  
    X = geometric_mechanism(alpha, 10000) 
    # plot geometric distribution
    n, bins, patches = P.hist(X, 2*n_bins, range=(-n_bins,n_bins), normed=1, histtype='stepfilled')   # bins: ndarray of 51 elements
    P.setp(patches, 'facecolor', 'g', 'alpha', 0.75)
    print bins
    # add a line showing the expected distribution
    x = np.array(range(-n_bins, n_bins+1))          # x: -n_bins -> n_bins
    y = np.power(alpha, abs(x))*(1-alpha)/(1+alpha)    # y = (1-alpha)/(1+alpha)*alpha^|x|
    l = P.plot(x, y, 'k--', linewidth=1.5)

    P.show()

#######################################################
# NZ: non-zeros elements 
# return: nz_list, z_list: locations and values of selected cells
def sparse_filter(NZ, n_zeros, eps, theta):
    alpha = math.exp(-eps)
    
    # for non-zero elements
    M1 = NZ + geometric_mechanism(alpha, NZ.size)
    nz_list = []
    for i in range(M1.size):
        if M1[i] >= theta:             # 1-sided filter
            nz_list.append((i,M1[i]))
    
    # for zero elements
    p_theta = alpha**theta/(1+alpha)
     
    k = np.random.binomial(n_zeros, p_theta)    # num of zero locs
    print "k =", k
    print "p_theta =", p_theta
    
    G = np.random.geometric(1-alpha, k) + theta - 1
    z_idx = []  # list of k locs sampled from (0, n_zeros-1)
    z_dict = {}
    while len(z_idx) < k:
        i = int(math.floor(np.random.random()*n_zeros))
        if not z_dict.has_key(i):
            z_idx.append(i)
            z_dict[i] = 1
    z_list = zip(z_idx, G)
    
    #
    return nz_list, z_list

#######################################################


#######################################################
if __name__ == '__main__':
    # TEST test_geom_distr()
#    test_geom_distr(0.5)
    
    # TEST geometric_mechanism()
    test_geometric_mechanism(0.5)       # = math.exp(-0.69314)
    test_geometric_mechanism(math.exp(-1.0))
    test_geometric_mechanism(math.exp(-0.5), n_bins=15)
    test_geometric_mechanism(math.exp(-0.2), n_bins=20)

#    print geometric_mechanism(0.5, 10)

    # TEST sparse_filter()
#    NZ = np.array([1,1,1,1,1,1,1,1,1,1])
##    NZ = np.array([1,2,3,4,5,6,7,8,9,10])
#    n_zeros = 10 
#    eps = 1.0
#    theta = 0.0
#    nz_list, z_list = sparse_filter(NZ, n_zeros, eps, theta)
#    
#    print nz_list
#    print z_list
    
    
    