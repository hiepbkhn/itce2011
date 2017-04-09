'''
Created on Jan 2, 2013

@author: Nguyen Huu Hiep
'''

from datetime import datetime
import mmap
import os

f = open("../data/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001_t0.txt", "r+") 
map = mmap.mmap(f.fileno(), 0)

print datetime.now()
    
for i in range(4):
    pid = os.fork()
    
    if pid == 0: # In a child process
        map.seek(0)
        
        while True:
            line = map.readline()
            
            if line == "": # EOF
                break
            items = line.split("\t")
            
            obj_id = int(items[1])

            x = float(items[5])
            y = float(items[6])
            timestamp = int(items[4])
            speed = float(items[7])
            next_node_x = int(items[8])
            next_node_y = int(items[9])
            k_anom = int(items[10])
            min_area = float(items[11])
    
        map.close()
        
        print "Parsing query file: DONE"


print datetime.now()