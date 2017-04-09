'''
Created on Apr 9, 2017

@author: Nguyen Huu Hiep

http://jakevdp.github.io/blog/2013/06/15/numba-vs-cython-take-2/
'''

import numpy as np
from timeit import default_timer as timer


X = np.random.random((1000, 3))

def pairwise_numpy(X):
    return np.sqrt(((X[:, None, :] - X) ** 2).sum(-1))


# timing (0.058s)
start = timer()
pairwise_numpy(X)
end = timer()
print(end - start)   
