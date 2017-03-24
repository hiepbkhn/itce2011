'''
Created on Oct 24, 2012

@author: Nguyen Huu Hiep
'''

#MAP_PATH = "D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/"
#MAP_PATH = "F:/Tailieu/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/"
#MAP_PATH = "C:/Tailieu/My Box Files/Default Sync Folder/oldenburgGen/"
#MAP_PATH = "C:/Tailieu/My Box Files/Default Sync Folder/California/"
MAP_PATH = "../data/"
#MAP_PATH = "C:/"
# MAP_PATH = "../../trace_generator/out/"

#MAP_FILE = "busan_highway"
#MAP_FILE = "illinois2"
MAP_FILE = "oldenburgGen"
# MAP_FILE = "oldenburgGen_mod"
#MAP_FILE = "SanJoaquin"
#MAP_FILE = "cal"
# MAP_FILE = "cal_mod"
# MAP_FILE = "synthetic_24_30_30_40"

#QUERY_PATH = "C:/Tailieu/My Box Files/Default Sync Folder/oldenburgGen/"
#QUERY_PATH = "C:/Tailieu/My Box Files/Default Sync Folder/California/"
#QUERY_PATH = "../data/"
QUERY_PATH = "../query/"
# QUERY_PATH = "../../trace_generator/out/"

#RESULT_PATH = "C:/"
RESULT_PATH = "../out/"

QUERY_TYPE = 0;     # QUERY_TYPE = 0 (Brinkhoff), 1 (TraceGenerator)

#QUERY_FILE = "oldenburgGen_500_0_50_0_60_20_3_1000_50.txt"
#QUERY_FILE = "oldenburgGen_1000_0_20_0_1000_20_0_1000_50.txt"
#QUERY_FILE = "oldenburgGen_500_0_50_0_60_20_3_1000_250.txt"
#QUERY_FILE = "oldenburgGen_1000_0_20_0_200_20_0_1000_250.txt"
#QUERY_FILE = "oldenburgGen_5000_0_10_0_100_20_0_1000_250.txt"
#QUERY_FILE = "oldenburgGen_10000_0_100_0_10_20_0_1000_250.txt"

#QUERY_FILE = "oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001.txt" #2_5_00005_0001: k_anom + min_length
#QUERY_FILE = "oldenburgGen_20000_0_10_0_10_20_1_1000_250_2_5_00005_0001.txt"  

#QUERY_FILE = "oldenburgGen_2000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"    #k-anom: 2-5, slow
#QUERY_FILE = "oldenburgGen_2000_0_0_0_20_20_1_1000_250_2_10_0005_002.txt"    #k-anom: 2-10, slow
#QUERY_FILE = "oldenburgGen_2000_0_0_0_20_20_1_1000_250_2_15_0005_002.txt"    #k-anom: 2-15, slow

QUERY_FILE = "oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"    #k-anom: 2-5, slow
#QUERY_FILE = "oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_10_0005_002.txt"    #k-anom: 2-10, slow
#QUERY_FILE = "oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_15_0005_002.txt"    #k-anom: 2-15, slow

#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"    #k-anom: 2-5, slow
#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_10_0005_002.txt"    #k-anom: 2-10, slow
#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_15_0005_002.txt"    #k-anom: 2-15, slow

#QUERY_FILE = "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"    #k-anom: 2-5, slow
#QUERY_FILE = "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_10_0005_002.txt"    #k-anom: 2-10, slow
#QUERY_FILE = "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_15_0005_002.txt"    #k-anom: 2-15, slow
   
#QUERY_FILE = "oldenburgGen_50000_0_0_0_15_20_1_1000_250_2_5_0005_002.txt"    #k-anom: 2-5, slow
  

#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_50_2_5_0005_002_2_4.txt"    #k-anom: 2-5, medium
#QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_10_2_5_0005_002_2_4.txt"    #k-anom: 2-5, fast

#QUERY_FILE = "oldenburgGen_1000_0_10_0_1000_20_0_200_250.txt"


#QUERY_FILE = "cal_5000_0_5_0_20_20_1_1000_250_2_5_00005_0001.txt"
#QUERY_FILE = "cal_10000_0_5_0_20_20_1_1000_250_2_5_00005_0001.txt"

######
# QUERY_FILE = "synthetic_24_30_30_40_events.txt"    #k-anom: 2-10
#QUERY_FILE = "oldenburgGen_mod_5k_1.0_2_5_0005_002_5_15_20_80_10_20_events.txt"    #k-anom: 2-5, medium
#QUERY_FILE = "oldenburgGen_mod_5k_0.5_2_5_0005_002_5_15_20_80_10_20_events.txt"    #k-anom: 2-5, slow
#QUERY_FILE = "oldenburgGen_mod_10k_0.5_2_5_0005_002_5_15_20_80_10_20_events.txt"        #k-anom: 2-5, slow
#QUERY_FILE = "oldenburgGen_mod_10k_0.5_2_5_0005_002_2_10_20_80_10_20_events.txt"        #k-anom: 2-5, slow
# QUERY_FILE = "oldenburgGen_mod_10k_0.5_2_5_0005_0020_2_10_20_80_10_20_events.txt"

#QUERY_FILE = "cal_mod_10k_0.5_2_5_0005_002_5_15_20_80_10_20_events.txt"        #k-anom: 2-5, slow
#QUERY_FILE = "cal_mod_10k_0.5_2_5_0005_002_2_10_20_80_10_20_events_1.txt"        #k-anom: 2-5, slow
# QUERY_FILE = "cal_mod_10k_0.5_2_5_0005_002_2_10_20_80_10_20_events.txt"        #k-anom: 2-5, slow

MAX_SPEED = 300
#MAX_SPEED = 6000
INIT_DISTANCE = 1000
MAX_USER = 10000

DISTANCE_CONSTRAINT = 500        # oldenburgGen
# DISTANCE_CONSTRAINT = 15000     # cal  
# DISTANCE_CONSTRAINT = 1500      # for synthetic_24_30_30_40_events (trace_generator)

MAP_RATIO = 0.2

K_ANONYMITY = 2
DELAY_MAX = 3   #report prob. = 0.2 --> 5 timestamps

K_GLOBAL = 2
S_GLOBAL = 2 

INIT_COVER_KEEP_RATIO = 1.0     #0.8, 0.85, 0.9, 0.95, 1.0 (for K_GLOBAL = 2)
NEXT_COVER_KEEP_RATIO = 1.0

MAX_MESH_LENGTH = 5*INIT_DISTANCE
MIN_MESH_LENGTH = 1.5*INIT_DISTANCE

MACE_EXECUTABLE = "../mace_go.exe"
#MACE_EXECUTABLE = "../mace_go"

MAXIMAL_CLIQUE_FILE_IN = "../mesh.grh"
MAXIMAL_CLIQUE_FILE_OUT = "../mesh.out"

