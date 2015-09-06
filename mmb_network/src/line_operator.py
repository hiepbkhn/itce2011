'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''

from planar import Vec2
from planar.line import Line

class LineOperator:
    
    @staticmethod
    def distance_to(x1,y1,x2,y2,px,py):
        p = Vec2(x1,y1)
        direction = Vec2(x2-x1,y2-y1)
        line = Line(p, direction)
        
        return abs(line.distance_to(Vec2(px,py)))
        

if __name__ == "__main__":
    p = Vec2(0,0)
    direction = Vec2(1,1)
    line = Line(p, direction)
    
    print line.distance_to(Vec2(0,1))
    print line.distance_to(Vec2(1,0))
    print line.distance_to(Vec2(2,2))
    
