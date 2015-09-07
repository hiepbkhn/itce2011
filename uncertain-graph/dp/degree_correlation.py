'''
Created on Jan 6, 2015

@author: huunguye
paper: Preserving Differential Privacy in Degree-Correlation based Graph Generation (TDP 2013)
'''


import time
from subprocess import call, check_output
from random import *
import math
import networkx as nx
import scipy.io
from numpy import *
import numpy as np
from itertools import chain, combinations

DKDIST_PATH = "D:/CppProjects/workspace-cpp/orbis/out/dkDist.exe"
DKTOPOGEN1K_PATH = "D:/CppProjects/workspace-cpp/orbis/out/dkTopoGen1k.exe"
DKTOPOGEN2K_PATH = "D:/CppProjects/workspace-cpp/orbis/out/dkTopoGen2k.exe"

OUTPATH = "../_out/"

#######################################################
def read_gen_graph(filename):
    G = nx.read_edgelist(filename, '#', ' ', None, nodetype=int)
    
    print "#nodes =", G.number_of_nodes()
    print "#edges =", G.number_of_edges()
    min_id = min(G.nodes_iter())
    max_id = max(G.nodes_iter())
    print "min_id =", min_id
    print "max_id =", max_id
    print "#components =", nx.number_connected_components(G)

#######################################################
def get_1k_2k_series(filename, outfile):
    
    outfile_1k = outfile + ".1k"
    outfile_2k = outfile + ".2k"
    
    cmd1k = DKDIST_PATH + " -k 1 -i " + filename + " > " + outfile_1k
    cmd2k = DKDIST_PATH + " -k 2 -i " + filename + " > " + outfile_2k
    call(cmd1k, shell=True)     # shell = True
    call(cmd2k, shell=True)
    
#    output = check_output(DKDIST_PATH + " -k 1 -i " + filename)
#    print output
    
     
#######################################################
def regenerate_graph_1k(filename):
    
    outfile = filename + ".gen"
    cmd1k = DKTOPOGEN1K_PATH + " -i " + filename + " > " + outfile
    call(cmd1k, shell=True)     # shell = True

#######################################################
def regenerate_graph_2k(filename):
    
    outfile = filename + ".gen"
    cmd2k = DKTOPOGEN2K_PATH + " -i " + filename + " > " + outfile
    call(cmd2k, shell=True) # shell = True


#######################################################
if __name__ == '__main__':

    filename = "D:/workspace-python/uncertain-graph/_data/as20graph.gr"     #
    outfile = "D:/workspace-python/uncertain-graph/_out/as20graph"
    
    # TEST get_1k_2k_series()
    get_1k_2k_series(filename, outfile)
    print "DONE"
    
    # TEST regenerate_graph_1k, regenerate_graph_2k()
    filename1k = "D:/workspace-python/uncertain-graph/_out/as20graph.1k"
    regenerate_graph_1k(filename1k)
    print "DONE"
    
    filename2k = "D:/workspace-python/uncertain-graph/_out/as20graph.2k"
    regenerate_graph_2k(filename2k)
    print "DONE"

    #
#    read_gen_graph("D:/workspace-python/uncertain-graph/_out/as20graph.1k.gen")
#    read_gen_graph("D:/workspace-python/uncertain-graph/_out/as20graph.2k.gen")


