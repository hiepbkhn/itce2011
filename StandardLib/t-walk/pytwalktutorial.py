#
#  twalktutorial.py
#  
#  Examples for the twalk implementation in Python,
#  Created by J Andres Christen, jac at cimat.mx .
#
#  Check the current version in the file VERSION.
#
#  See http://www.cimat.mx/~jac/twalk/ for more details.
#


import pytwalk

from numpy import ones, zeros, log, array
from numpy.random import uniform


## You may get the inline help:
# pytwalk.pytwalk?
# pytwalk.pytwalk.Run?


##################################################################
### This sets a MCMC for n=10 independent normals (default)

Normal = pytwalk.pytwalk(n=10)

## This would run the twalk

#Normal.Run( T=10000, x0=10*ones(10), xp0=15*ones(10))

### This does a Random Walk Metriopolis Hastings with 10000 iterations
### initial point x0 and standar dev's for the normal jumps = sigma

#Normal.RunRWMH( T=10000, x0=10*ones(10), sigma=ones(10))

### This will do a basic output analsis

#Normal.Ana()



####################################################################
######### Product of Exponentilas
lambdas = [ 1., 2., 3., 4., 5.]

def ExpU(x):
	"""-log of a product of exponentials"""
	return sum(x * lambdas)

def ExpSupp(x):
	return all(0 < x)

###          we define the objective function with the U function
###          which is -log of the density function.
###          The support is defined in a separate function.
###   The dimension of the parameter space is n
Exp = pytwalk.pytwalk( n=5, U=ExpU, Supp=ExpSupp)

#### This would run the twalk

Exp.Run( T=50000, x0=30*ones(5), xp0=40*ones(5))
Exp.Ana()
#Exp.Save("Exptwalk.dat")



#########################################################################
##### A more complex example:
##### Related Bernoulli trials #####
##### Suppose x_{i,j} ~ Be( theta_j ), i=0,1,2,...,n_j-1, ind. j=0,1,2
##### But it is known that 0 <  theta_0 < theta_3 < theta_2 < 1 

theta = array([ 0.4, 0.5, 0.7 ])  ### True thetas
n = array([ 20, 15, 40]) ### sample sizes
#### Simulated data, but we only need the sum of 1's
r = zeros(3)
for j in range(3):
	r[j] = sum(uniform(size=n[j]) < theta[j])

### Defines the support.  This is basically the prior, uniform in this support
def ReBeSupp(theta):
	rt = True
	rt &= (0 < theta[0])
	rt &= (theta[0] < theta[1])
	rt &= (theta[1] < theta[2])
	rt &= (theta[2] < 1)
	return rt

#### It is a good idea to have a function that produces random initial points,
#### indeed always withinh the support
#### In this case we simulate from something similar to the prior
def ReBeInit():
	theta = zeros(3)
	theta[0] = uniform( low=0, high=1)
	theta[1] = uniform( low=theta[0], high=1)
	theta[2] = uniform( low=theta[1], high=1)
	return theta

####### The function U (Energy): -log of the posterior
def ReBeU(theta):
	return -1*sum(r * log(theta) + (n-r)*log(1.0-theta))
	
###### Define the twalk instance
ReBe = pytwalk.pytwalk( n=3, U=ReBeU, Supp=ReBeSupp)

#### This runs the twalk

ReBe.Run( T=100000, x0=ReBeInit(), xp0=ReBeInit())

### This will do a basic output analsis
#ReBe.Ana()

## evolution of the markov chain (time series)
#ReBe.TS()

### And some histograms
#ReBe.Hist( par=0 )
#ReBe.Hist( par=1 )
#ReBe.Hist( par=2 )	

### Then save it to a text file, with each column for each paramter
### plus the U's in the last column, that is T+1 rows and n+1 colums.
### This may in turn be loaded by other programs
### for more sophisticated output analysis (eg. BOA).

#ReBe.Save("RelatedBer.txt")

### You may access the (T+1) X (n+1) output matrix directly with
#ReBe.Output

###### Check twalk.py for more details.
###### All methods should have help lines.















