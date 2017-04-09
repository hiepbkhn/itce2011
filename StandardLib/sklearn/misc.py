'''
Created on Apr 25, 2013

@author: Nguyen Huu Hiep
'''

import numpy as np
import sklearn.random_projection as rp

a = np.matrix([[1.0, 2.0], [3.0, 4.0]])
print a
print np.linalg.norm(a[:,0])
print np.linalg.norm(a[:,1])


n_components = 2000
n_features = 8192
phi = rp.sparse_random_matrix(n_components, n_features)

print type(phi)

prod = np.abs(np.dot(np.matrix(phi.T),np.matrix(phi)))

print type(prod)
print type(prod.max())

#print "max prod_ij =", prod.max().max()
