'''
Created on Mar 24, 2014

@author: huunguye
'''

import snap


##################
Graph = snap.GenRndGnm(snap.PNGraph, 100, 1000)
SrcNId = 0
DistNbrsV = snap.TIntFltKdV()
snap.GetAnf(Graph, SrcNId, DistNbrsV, 3, False, 32)
for item in DistNbrsV:
    print item.Dat()
print "DONE"

UGraph = snap.GenRndGnm(snap.PUNGraph, 100, 1000)
SrcNId = 0
DistNbrsV = snap.TIntFltKdV()
snap.GetAnf(UGraph, SrcNId, DistNbrsV, 3, False, 32)
for item in DistNbrsV:
    print item.Dat()
print "DONE"

Network = snap.GenRndGnm(snap.PNEANet, 100, 1000)
SrcNId = 0
DistNbrsV = snap.TIntFltKdV()
snap.GetAnf(Network, SrcNId, DistNbrsV, 3, False, 32)
for item in DistNbrsV:
    print item.Dat()
print "DONE"    


##################
print "All pairs" 
Graph = snap.GenRndGnm(snap.PNGraph, 100, 1000)
DistNbrsV = snap.TIntFltKdV()
snap.GetAnf(Graph, DistNbrsV, 3, False, 32)
for item in DistNbrsV:
    print item.Dat()
print "DONE"    
UGraph = snap.GenRndGnm(snap.PUNGraph, 100, 1000)
DistNbrsV = snap.TIntFltKdV()
snap.GetAnf(UGraph, DistNbrsV, 3, False, 32)
for item in DistNbrsV:
    print item.Dat()
print "DONE"    
Network = snap.GenRndGnm(snap.PNEANet, 100, 1000)
DistNbrsV = snap.TIntFltKdV()
snap.GetAnf(Network, DistNbrsV, 3, False, 32)
for item in DistNbrsV:
    print item.Dat()
print "DONE"    