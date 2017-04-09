'''
Created on Dec 30, 2012

@author: Nguyen Huu Hiep
'''

import time
import sys
import pp
from lib_1 import Prime

if __name__ == "__main__":
    
    # tuple of all parallel python servers to connect with
    ppservers = ()
    #ppservers = ("10.0.0.1",)
    
    if len(sys.argv) > 1:
        ncpus = int(sys.argv[1])
        # Creates jobserver with ncpus workers
        job_server = pp.Server(ncpus, ppservers=ppservers)
    else:
        # Creates jobserver with automatically detected number of workers
        job_server = pp.Server(ppservers=ppservers)
    
    print "Starting pp with", job_server.get_ncpus(), "workers"
    
    #LOOP
    start = time.clock();
    
    for i in range(3):
    
        prime = Prime()
        
        # Execution starts as soon as one of the workers will become available
        job1 = job_server.submit(prime.sum_primes, (100,), (prime.isprime,), ("math", ))
        
        # Retrieves the result calculated by job1
        # The value of job1() is the same as sum_primes(100)
        # If the job has not been finished yet, execution will wait here until result is available
        result = job1()
        
        print "Sum of primes below 100 is", result
        
        start_time = time.time()
        
        # The following submits 8 jobs and then retrieves the results
        inputs = (100000, 100100, 100200, 100300, 100400, 100500, 100600, 100700)
        jobs = [(input, job_server.submit(prime.sum_primes, (input,), (prime.isprime,), ("math", ))) 
                for input in inputs]
        for input, job in jobs:
            print "Sum of primes below", input, "is", job()
        
        print "Time elapsed: ", time.time() - start_time, "s"
        job_server.print_stats()

    #
    print "Elapsed: ", time.clock() - start


