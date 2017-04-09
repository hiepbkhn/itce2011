'''
Created on Dec 30, 2012

@author: Nguyen Huu Hiep
'''

import math

class Prime:
    
#    @staticmethod
#    def isprime(n):
#        """Returns True if n is prime and False otherwise"""
#        if not isinstance(n, int):
#            raise TypeError("argument passed to is_prime is not of 'int' type")
#        if n < 2:
#            return False
#        if n == 2:
#            return True
#        max = int(math.ceil(math.sqrt(n)))
#        i = 2
#        while i <= max:
#            if n % i == 0:
#                return False
#            i += 1
#        return True
#
#    def sum_primes(self, n):
#        """Calculates sum of all primes below given integer n"""
#        return sum([x for x in xrange(2, n) if Prime.isprime(x)]) 
    
    
    
    
    # NON-STATIC
    def isprime(self, n):
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

    def sum_primes(self, n):
        """Calculates sum of all primes below given integer n"""
        return sum([x for x in xrange(2, n) if self.isprime(x)]) 


#######################################################
#    MAIN (test)
#######################################################
if __name__ == "__main__":
    pass
#    print Prime.isprime(10)






