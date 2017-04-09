'''
Created on Jan 2, 2013

@author: Nguyen Huu Hiep
'''

from datetime import datetime
import mmap
import os
import pp

#############################
def parse_query_file():
    f = open("../data/oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001_t0.txt", "r+") 
    map = mmap.mmap(f.fileno(), 0)
    
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

#############################
def parse_expanding_list_file():
    f = open("../out/expanding_list.txt", "r+") 
    map = mmap.mmap(f.fileno(), 0)
    
    map.seek(0)
        
    while True:
        line = map.readline()
        
        if line == "": # EOF
            break
        obj_id = int(line)
        
        #
        line = map.readline()
        seg_list = line.split(":")
        seg_list.pop()
        
        for seg_line in seg_list:
            items = seg_line.split(",")
            x1 = float(items[0])
            y1 = float(items[1])
            x2 = float(items[2])
            y2 = float(items[3])
            seg_id = int(items[4])
        

    map.close()
    
    print "Parsing query file: DONE"


#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    
    print datetime.now()
    
    ppservers = ()
    num_cpus = 2
    job_server = pp.Server(ncpus=num_cpus, ppservers=ppservers)
    print "Starting pp with", job_server.get_ncpus(), "workers"
    
    jobs = [job_server.submit(parse_expanding_list_file, 
                (), 
                (), 
                ("mmap",)) for i in range(num_cpus)]
    print datetime.now()

    for job in jobs:
        res = job()
    job_server.print_stats()    
    print datetime.now()    

    print datetime.now()