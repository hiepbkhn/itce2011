'''
Created on Jan 2, 2013

@author: Nguyen Huu Hiep
'''

import mmap
import os

map = mmap.mmap(-1, 13)
map.write("Hello world!")

pid = os.fork()

if pid == 0: # In a child process
    map.seek(0)
    print map.readline()

    map.close()
