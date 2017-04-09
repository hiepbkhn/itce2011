'''
Created on Mar 7, 2014

@author: huunguye
References:
    1. Markov Chains and Monte Carlo Methods (Ioana)

'''

import random

#######################################################
# Gibbs sampler for bivariate normal (p.49 [1])
# 
def sample_bivariate_normal():
    mu1 = 0.0
    mu2 = 0.0
    sig1 = 1.0  # sigma_1^2
    sig2 = 1.0  # sigma_2^2
    sig12 = 0.3 # sigma_12
    
    MAX_T = 10000
    X1 = [0.0 for i in range(MAX_T+1)]
    X2 = [0.0 for i in range(MAX_T+1)]
    for t in range(1,MAX_T+1):
        X1[t] = random.gauss(mu1 + sig12/sig2*(X2[t-1]-mu2), sig1-sig12*sig12/sig2)
        X2[t] = random.gauss(mu2 + sig12/sig1*(X1[t]-mu1), sig2-sig12*sig12/sig1) 

    count = 0
    for t in range(1,MAX_T+1): 
        if X1[t] >= 0 and X2[t] >= 0:
            count += 1
    #
    print "% area in first quadrant =", float(count)/MAX_T
    

#######################################################
if __name__ == '__main__':
    sample_bivariate_normal()