'''
Created on Apr 9, 2017

@author: Nguyen Huu Hiep
'''

#from numba import double
from numba.decorators import jit, autojit
import numpy as np
from timeit import default_timer as timer

X = np.random.random((1000, 3))

def pairwise_python(X):
    M = X.shape[0]
    N = X.shape[1]
    D = np.empty((M, M), dtype=np.float)
    for i in range(M):
        for j in range(M):
            d = 0.0
            for k in range(N):
                tmp = X[i, k] - X[j, k]
                d += tmp * tmp
            D[i, j] = np.sqrt(d)
    return D





pairwise_numba = autojit(pairwise_python)

# timing (0.25s)
start = timer()
pairwise_numba(X)
end = timer()
print(end - start) 

