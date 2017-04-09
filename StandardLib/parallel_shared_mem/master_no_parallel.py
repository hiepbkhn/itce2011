'''
Created on Dec 30, 2012

@author: Nguyen Huu Hiep
'''

import time
import sys
from lib_1 import Prime

if __name__ == "__main__":
    
    #LOOP
    start = time.clock();
    
    for i in range(3):
    
        prime = Prime()
        
        result = prime.sum_primes(100)
        
        print "Sum of primes below 100 is", result
        
        start_time = time.time()
        
        # The following submits 8 jobs and then retrieves the results
        inputs = (100000, 100100, 100200, 100300, 100400, 100500, 100600, 100700)
        
        for input in inputs:
            print "Sum of primes below", input, "is", prime.sum_primes(input)
        
        print "Time elapsed: ", time.time() - start_time, "s"

    #
    print "Elapsed: ", time.clock() - start


