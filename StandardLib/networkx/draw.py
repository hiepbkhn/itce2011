'''
Created on Nov 27, 2012

@author: Nguyen Huu Hiep
'''

import pylab as P #
import networkx as nx
import matplotlib.pyplot as plt


G = nx.dodecahedral_graph()
pos = nx.spring_layout(G)

nx.draw(G, pos)  # networkx draw()
#P.draw()    # pylab draw()

plt.savefig("fig/dodecahedral_graph.png") # save as png
plt.show() # display