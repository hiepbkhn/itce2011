'''
Created on May 23, 2011

@author: map
'''

import time
import multiprocessing as mp


# declare a worker function, args and kwargs are optional, but we want
# arguments for illustration here
def worker(*args, **kwargs):
    x, waittime = args                    # unwrap argument tuple
    print "working on ", kwargs['name']   # access keyword argument
    time.sleep(waittime)                  # do some time consuming work
    print 'done with ', kwargs['name']
    return x * x
    

if __name__ == '__main__':
    pool = mp.Pool(processes = 2)         # instantiate a poll of 4 processes
    results = []                          # results go here
    for i in xrange(0, 11):               # nr of tasks to do > nr processes
        args = (i, i)                     # wrap arguments for worker function
        kwargs = {'name' : 'process ' + str(i)}   # keyword arguments (optional)
        results.append(pool.apply_async(worker, args, kwargs))   # add to pool
        
    pool.close()   # no more processes accepted by this pool    
    pool.join()    # wait till all processes are finished
    
    print ''
    print 'results:'
    for i in xrange(0, 11):
        print(results[i].get(timeout = 1))   # print the result

    print ''    
    print 'done'
    