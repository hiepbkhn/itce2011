'''
Created on Jan 17, 2013

@author: Nguyen Huu Hiep
'''

#QUERY_PATH = "../data/"
QUERY_PATH = "../data/"


#QUERY_FILE = "oldenburgGen_5000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"
QUERY_FILE = "oldenburgGen_10000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"
#QUERY_FILE = "oldenburgGen_20000_0_0_0_20_20_1_1000_250_2_5_0005_002.txt"

MAX_TIME_STAMP = 3

f = open(QUERY_PATH + QUERY_FILE, "r")
fstr = f.read()
f.close()

file_idx = 0
FILE_SPLIT = QUERY_FILE[0:-4] + "_t" + str(file_idx) + ".txt"

f = open(QUERY_PATH + FILE_SPLIT, "w")

for line in fstr.split("\n"):
    if line == "": # EOF
        break
    items = line.split("\t")
    
    timestamp = int(items[4])
    
    if timestamp > MAX_TIME_STAMP:
        break
    
    if timestamp != file_idx:
        f.close()
        print "Written to file", FILE_SPLIT
        
        file_idx += 1
        
        FILE_SPLIT = QUERY_FILE[0:-4] + "_t" + str(file_idx) + ".txt"

        f = open(QUERY_PATH + FILE_SPLIT, "w")
    #    
    f.write(line + "\n")
    
f.close()
print "Written to file", FILE_SPLIT    

