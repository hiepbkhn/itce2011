'''
Created on Feb 7, 2014

@author: huunguye
'''

import pytwalk

from numpy import ones, zeros, log, array
from numpy.random import uniform

##################################################################
def normal_10_dim():
    # normal distribution of 10 dimensions
    Normal = pytwalk.pytwalk(n=10)
    
    Normal.Run( T=10000, x0=10*ones(10), xp0=15*ones(10))

    ### This does a Random Walk Metriopolis Hastings with 10000 iterations
    ### initial point x0 and standar dev's for the normal jumps = sigma
    
    Normal.RunRWMH( T=10000, x0=10*ones(10), sigma=ones(10))
    
    ### This will do a basic output analsis
    
    Normal.Ana()
    
    Normal.Save("Exptwalk-10-dim.dat")

##################################################################
def normal_1_dim():
    # normal distribution of 1 dimension
    Normal = pytwalk.pytwalk(n=1)
    
    Normal.Run( T=10000, x0=10*ones(1), xp0=15*ones(1))

    ### This does a Random Walk Metriopolis Hastings with 10000 iterations
    ### initial point x0 and standar dev's for the normal jumps = sigma
    
    Normal.RunRWMH( T=10000, x0=10*ones(1), sigma=ones(1))
    
    ### This will do a basic output analsis
    
    Normal.Ana()
    
    Normal.Save("Exptwalk-1-dim.dat")
    
##################################################################
def LinearU(x):
    return 1
    
def LinearSupp(x):
    return all(0 < x and x < 2)    

def linear_1_dim():
    # uniform distribution of 1 dimension
    Linear = pytwalk.pytwalk(n=1, U=LinearU, Supp=LinearSupp)
    
    Linear.Run( T=1000, x0=0.5*ones(1), xp0=1.5*ones(1))

    ### This will do a basic output analsis
    
#    Linear.Ana()
    
    Linear.Save("Lineartwalk-1-dim.dat")    

##################################################################
##################################################################
if __name__ == '__main__':
#    normal_1_dim()

    linear_1_dim()
    
    
    
    