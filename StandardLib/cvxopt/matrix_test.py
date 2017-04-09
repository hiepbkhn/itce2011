'''
Created on Apr 8, 2013

@author: Nguyen Huu Hiep
'''
from cvxopt import matrix

A = matrix(range(16),(4,4))
print(A[[0,1,2,3],[0,2]])

print(A[::5])

A[::5] = -1
print(A)