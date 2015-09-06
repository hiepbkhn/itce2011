'''
Created on Oct 24, 2012

@author: Nguyen Huu Hiep
'''

#MAP_PATH = "D:/Documents/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/"
#MAP_PATH = "F:/Tailieu/Paper-code/Network-based-Generator-of-Moving-Objects/DataFiles/"
#MAP_PATH = "C:/Users/Nguyen Huu Hiep/Documents/My Box Files/Default Sync Folder/oldenburgGen/"
MAP_PATH = "../data/"
#MAP_PATH = "C:/"

#MAP_FILE = "busan_highway"
#MAP_FILE = "illinois2"
MAP_FILE = "oldenburgGen"
#MAP_FILE = "SanJoaquin"

#QUERY_PATH = "C:/Users/Nguyen Huu Hiep/Documents/My Box Files/Default Sync Folder/oldenburgGen/"
QUERY_PATH = "../data/"

#RESULT_PATH = "C:/"
RESULT_PATH = "../out/"


#QUERY_FILE = "oldenburgGen_500_0_50_0_60_20_3_1000_50.txt"
#QUERY_FILE = "oldenburgGen_1000_0_20_0_1000_20_0_1000_50.txt"

#QUERY_FILE = "oldenburgGen_500_0_50_0_60_20_3_1000_250.txt"
#QUERY_FILE = "oldenburgGen_1000_0_20_0_200_20_0_1000_250.txt"
#QUERY_FILE = "oldenburgGen_5000_0_10_0_100_20_0_1000_250.txt"
#QUERY_FILE = "oldenburgGen_10000_0_100_0_10_20_0_1000_250.txt"

QUERY_FILE = "oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001.txt"   #2_5_00005_0001: k_anom + min_area
#QUERY_FILE = "oldenburgGen_20000_0_10_0_10_20_1_1000_250_2_5_00005_0001.txt"

#QUERY_FILE = "oldenburgGen_1000_0_10_0_1000_20_0_200_250.txt"

FRAME_FILE = "oldenburgGen_10000_0_100_0_10_20_0_1000_250_2_5_00005_0001_t"  # + timestamp, e.g. t0,t1,...
#FRAME_FILE = "oldenburgGen_20000_0_10_0_10_20_1_1000_250_2_5_00005_0001_t"

MAX_SPEED = 300
INIT_DISTANCE = 1000
MAX_USER = 10000

DISTANCE_CONSTRAINT = 600
MAP_RATIO = 0.2

K_ANONYMITY = 2
DELAY_MAX = 3   #report prob. = 0.2 --> 5 timestamps

K_GLOBAL = 2
S_GLOBAL = 2 

INIT_COVER_KEEP_RATIO = 1.0     #0.8, 0.85, 0.9, 0.95, 1.0 (for K_GLOBAL = 2)
NEXT_COVER_KEEP_RATIO = 1.0

MAX_MESH_LENGTH = 5*INIT_DISTANCE
MIN_MESH_LENGTH = 1.5*INIT_DISTANCE

#MACE_EXECUTABLE = "../mace_go.exe"
MACE_EXECUTABLE = "../mace_go"

MAXIMAL_CLIQUE_FILE_IN = "../mesh.grh"
MAXIMAL_CLIQUE_FILE_OUT = "../mesh.out"

