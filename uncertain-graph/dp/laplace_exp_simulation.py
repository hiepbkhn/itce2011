'''
Created on Feb 25, 2015

@author: Nguyen Huu Hiep
paper: Mechanism Design via Differential Privacy (FOCS'07)
    Differentially Private Network Data Release via Structural Inference (KDD'14)
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
from __builtin__ import abs


#######################################################
# MCMC (Markov Chain Monte Carlo)
# org_value: true count value 
# step_size: randomly jump from current location to [curV - step_size, curV + step_size]
# dQ: delta Q (of scores) 
def mcmc_simulation(org_value, eps, step_size, n_steps, n_burns, dQ, is_two = False):
    
    values = [0.0 for _ in range(n_steps-n_burns)]
    curV = org_value
    
    for i in range(n_steps):
        rand_val = random.random()
        newV = curV + (-step_size + 2.0*step_size*rand_val)
        #
        if is_two:
            prob = math.exp(eps/(2*dQ)*(abs(curV-org_value) - abs(newV-org_value)))      # prefer smaller values of |curV-org_value| 
        else:
            prob = math.exp(eps/dQ*(abs(curV-org_value) - abs(newV-org_value)))          # not 2*dQ
        if prob > 1.0:
            prob = 1.0
        prob_val = random.random()
        if prob_val <= prob:    # accept newV
            curV = newV
        
        #
        if i >= n_burns:
            values[i-n_burns] = curV
    #
    return values
    

#######################################################
if __name__ == '__main__':
    
    # demo from http://matplotlib.org/1.3.1/examples/pylab_examples/histogram_demo_extended.html
#    mu, sigma = 200, 25
#    x = mu + sigma*P.randn(10000)
#    
#    # the histogram of the data with histtype='step'
#    n, bins, patches = P.hist(x, 50, normed=1, histtype='stepfilled')
#    P.setp(patches, 'facecolor', 'g', 'alpha', 0.75)
#    
#    # add a line showing the expected distribution
#    y = P.normpdf( bins, mu, sigma)
#    l = P.plot(bins, y, 'k--', linewidth=1.5)
#
#    P.show()
    
    
    # test against Laplace(0.0, eps=1.0, dQ=1.0)
    eps = 1.0
    dQ = 1.0
    step_size = 10.0
    n_steps = 20000
    n_burns = 2000      # burn-in
    
    loc = 0.0
    scale = dQ/eps
    
    n_bins = 100
    
    start = time.clock()
    values = mcmc_simulation(loc, eps, step_size, n_steps, n_burns, dQ, is_two=False)
    print "mcmc_simulation - DONE, elapsed", time.clock() - start
    
    # the histogram of the data with histtype='step'
    x = np.array(values)
    n, bins, patches = P.hist(x, n_bins, normed=1, histtype='stepfilled')   # bins: ndarray of 51 elements
    P.setp(patches, 'facecolor', 'g', 'alpha', 0.75)
    
    # add a line showing the expected distribution
    y = np.exp(-abs(bins-loc/scale))/(2.*scale)
    l = P.plot(bins, y, 'k--', linewidth=1.5)

    P.show()
    



