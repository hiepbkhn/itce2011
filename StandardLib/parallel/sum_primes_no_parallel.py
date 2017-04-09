'''
Created on Dec 27, 2012

@author: Nguyen Huu Hiep
'''
#!/usr/bin/python
# File: sum_primes.py
# Author: VItalii Vanovschi
# Desc: This program demonstrates parallel computations with pp module
# It calculates the sum of prime numbers below a given integer in parallel
# Parallel Python Software: http://www.parallelpython.com

import math, sys, time

def isprime(n):
    """Returns True if n is prime and False otherwise"""
    if not isinstance(n, int):
        raise TypeError("argument passed to is_prime is not of 'int' type")
    if n < 2:
        return False
    if n == 2:
        return True
    max = int(math.ceil(math.sqrt(n)))
    i = 2
    while i <= max:
        if n % i == 0:
            return False
        i += 1
    return True

def sum_primes(n):
    """Calculates sum of all primes below given integer n"""
    return sum([x for x in xrange(2, n) if isprime(x)])


########
start_time = time.time()

result = sum_primes(100)

print "Sum of primes below 100 is", result

inputs = (100000, 100100, 100200, 100300, 100400, 100500, 100600, 100700)
for input in inputs:
    result = sum_primes(input)
    print "Sum of primes below", input, "is", result

print "Time elapsed: ", time.time() - start_time, "s"


