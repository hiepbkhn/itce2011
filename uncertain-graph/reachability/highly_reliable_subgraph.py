'''
Created on Oct 30, 2014

@author: huunguye
paper: Discovering Highly Reliable Subgraphs in Uncertain Graphs (KDD'11)
'''

import time
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from itertools import chain, combinations

#######################################################
def powerset(iterable):
    xs = list(iterable)
    # note we return an iterator rather than a list
    return chain.from_iterable( combinations(xs,n) for n in range(len(xs)+1) )

if __name__ == '__main__':
    pass