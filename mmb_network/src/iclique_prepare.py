'''
Created on Jan 26, 2013

@author: Nguyen Huu Hiep
'''

import option
from map_loader import Map 
from random import random

#MAP_PATH = "F:/Tailieu/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/"
MAP_PATH = "C:/Users/Nguyen Huu Hiep/Documents/My Box Files/Default Sync Folder/oldenburgGen/"

MAP_FILE = "oldenburgGen"

#QUERY_PATH = "F:/Tailieu/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/"
QUERY_PATH = "C:/Users/Nguyen Huu Hiep/Documents/My Box Files/Default Sync Folder/oldenburgGen/"

#QUERY_FILE = "oldenburgGen_100_0_0_0_20_20_1_1000_250_2_5_00005_0001_2_4.txt"
#QUERY_FILE = "oldenburgGen_500_0_0_0_20_20_1_1000_250_2_5_00005_0001_2_4.txt"
#QUERY_FILE = "oldenburgGen_1000_0_0_0_20_20_1_1000_250_2_5_00005_0001_2_4.txt"
#QUERY_FILE = "oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_00005_0001_2_4.txt"

#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"       #slow
#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4.txt"    #medium, k-anom: 2-5
#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_10_0005_002_2_4.txt"    #medium, k-anom: 2-10
QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_15_0005_002_2_4.txt"    #medium, k-anom: 2-15
#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_10_2_5_0005_002_2_4.txt"    #fast

def convert_to_iclique_data():
    map_data = Map()
    map_data.read_map(MAP_PATH, MAP_FILE)
    
    f = open(QUERY_PATH + QUERY_FILE, "r")
    fstr = f.read()
    f.close()
    
    #
    f = open(QUERY_PATH + "iclique_" + QUERY_FILE, "w")
    timestamp = 0
    for line in fstr.split("\n"):
        if line == "": # EOF
            break
        items = line.split("\t")
        
        # timestamp
        timestamp = int(items[4])
        
        # area min (generated 
        a_min_ratio = float(items[11])
        a_min = map_data.area * a_min_ratio
        
        ####
        #f.write("%s\t%s\t%s\t%s\t%s\t%s\t%f"%(items[1],items[5],items[6],items[4],items[7],items[10],a_min))
        
        f.write("%s\t%s\t%s\t%d\t%s\t%s\t%f"%(items[1],items[5],items[6],timestamp * 60,items[7],items[10],a_min))
        
        #f.write("%s\t%s\t%s\t%d\t%s\t%s\t%f"%(items[1],items[5],items[6],timestamp,items[7],items[10],a_min))
        #timestamp += 1
        
        f.write("\n")
    
    f.close()


def convert_to_iclique_data_from_mesh_cloak():
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    
    f = open(option.QUERY_PATH + option.QUERY_FILE, "r")
    fstr = f.read()
    f.close()
    
    #
    f = open(option.QUERY_PATH + "iclique_" + option.QUERY_FILE, "w")
    timestamp = 0
    LARGE_STEP = 90     #60,90,120 seconds   : for separating time steps
    NUM_RANGES = 10      #5,10                : for sub-dividing in each time step
    SMALL_STEP = LARGE_STEP / NUM_RANGES    #ranges
    NUM_USERS = 5000
    REQUESTS_PER_RANGE = NUM_USERS/NUM_RANGES
    request_id = 0
    for line in fstr.split("\n"):
        if line == "": # EOF
            break
        items = line.split("\t")
        
        ### 1 - FOR Brinkhoff generator 
#        # timestamp
#        timestamp = int(items[4])
#        if timestamp > 5:
#            break
#        
#        # area min (generated randomly from range 0.00005-0.0001)
#        a_min_ratio = generate_a_min_ratio()
#        a_min = map_data.area * a_min_ratio
#        
#        #
#        timestep = timestamp * LARGE_STEP + (request_id % NUM_USERS) / REQUESTS_PER_RANGE * SMALL_STEP
#        f.write("%s\t%s\t%s\t%d\t%s\t%s\t%f"%(items[1],items[5],items[6],timestep,items[7],items[10],a_min))
        
        ### 2 - FOR TraceGenerator
        # timestamp
        timestamp = int(items[1])
        if timestamp > 5:
            break
        
        # area min (generated randomly from range 0.00005-0.0001)
        a_min_ratio = generate_a_min_ratio()
        a_min = map_data.area * a_min_ratio
        
        #
        timestep = timestamp * LARGE_STEP + (request_id % NUM_USERS) / REQUESTS_PER_RANGE * SMALL_STEP
        f.write("%s\t%s\t%s\t%d\t%s\t%s\t%f"%(items[0],items[2],items[3],timestep,items[4],items[7],a_min))
        # (End) 2 - FOR TraceGenerator 
        
        
        f.write("\n")
        request_id += 1
    
    f.close()    

def generate_a_min_ratio():
    min_val = 0.00005
    max_val = 0.0001
    return min_val + (max_val - min_val) * random()

#######################################################
if __name__ == '__main__':
    
    convert_to_iclique_data_from_mesh_cloak()
    
    print "Convert query file - DONE !"

#    print generate_a_min_ratio()
