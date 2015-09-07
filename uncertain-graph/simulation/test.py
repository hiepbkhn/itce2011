'''
Created on Dec 3, 2014

@author: huunguye
'''

from bitarray import bitarray

if __name__ == '__main__':
    a = bitarray(2**4)
    print a
    a.setall(0)     # set all bits to 0
    print a
    
    a = bitarray('0110')
    b = bitarray('0101')
    
    print a, b
    print a & b 
    print a | b
    print a ^ b
    print ~a
    
    print a.count(), b.count()
    c = a & b
    print c.count()
    
    for i in a:
        print i
    for i in range(a.length()):
        print a[i]
    