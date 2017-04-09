
import time
from cvxopt import matrix, log, exp, solvers
import numpy as np
from numpy import array, matlib, linspace

#As an example, we solve the small GP of section 2.4 of the paper <A Tutorial on Geometric Programming>

#Aflr  = 1000.0
#Awall = 100.0
#alpha = 0.5
#beta  = 2.0
#gamma = 0.5
#delta = 2.0
#
#F = matrix( [[-1., 1., 1., 0., -1.,  1.,  0.,  0.],
#             [-1., 1., 0., 1.,  1., -1.,  1., -1.],
#             [-1., 0., 1., 1.,  0.,  0., -1.,  1.]])
#g = log( matrix( [1.0, 2/Awall, 2/Awall, 1/Aflr, alpha, 1/beta, gamma, 1/delta]) )
#K = [1, 2, 1, 1, 1, 1, 1]
#h, w, d = exp( solvers.gp(K, F, g)['x'] )


###########################
## min. x1^-2 + 2.x2^-2 
## s.t. x1 + x2 = 4
#F = matrix( [[-2.,  0., 1., 0.],
#             [ 0., -2., 0., 1.]])
#g = log(matrix( [1., 2., 0.25, 0.25]))
#
#K = [2, 2]
#x1, x2 = exp( solvers.gp(K, F, g)['x'] ) 
#
#print "Solution (x1,x2) =", x1, x2

###########################
## min. x1^-2 + 2.x2^-2 + 3.x3^-2
## s.t. x1 + x2 + 2.x3 = 3
#F = matrix( [[-2.,  0., 0., 1., 0., 0.],
#             [ 0., -2., 0., 0., 1., 0.],
#             [ 0., 0., -2., 0., 0., 1.]])
#g = log(matrix( [1., 2., 3., 0.25, 0.25, 0.5]))
#
#print F
#
#K = [3, 3]
#x1, x2, x3 = exp( solvers.gp(K, F, g)['x'] ) 
#
#print "Solution (x1,x2,x3) =", x1, x2, x3


########################### SCALABILITY TEST
# n = 1000 --> 20.2s
# n = 2000 --> 161.3s
#A = matrix([[1, 2, 3]])
n = 1000
A = matrix(linspace(1,n,num=n))
#print A

n = A.size[0]
print n

eps = 1.0


F = matrix(np.vstack([matlib.eye(n)*(-2), matlib.eye(n)])) 
#print F
#print type(F)

g = log(matrix(np.vstack([A, matlib.ones((n,1))*eps/n]) ))
#print g
#print type(g)

K = [n, n]

#x1, x2, x3 = exp( solvers.gp(K, F, g)['x'] ) 
#print "Solution (x1,x2,x3) =", x1, x2, x3

start_time = time.clock()
x = exp( solvers.gp(K, F, g)['x'] ) 
#print "Solution (x1,x2,x3) =", x

print "time elapsed ",time.clock() - start_time 



