'''
Created on Apr 8, 2013

@author: Nguyen Huu Hiep
src: http://abel.ee.ucla.edu/cvxopt/examples/tutorial/lp.html
'''


from cvxopt import matrix, solvers
A = matrix([ [-1.0, -1.0, 0.0, 1.0], [1.0, -1.0, -1.0, -2.0] ])
b = matrix([ 1.0, -2.0, 0.0, 4.0 ])
c = matrix([ 2.0, 1.0 ])
sol=solvers.lp(c,A,b)
print(sol['x'])