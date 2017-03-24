'''
Created on Oct 23, 2012

@author: Nguyen Huu Hiep
'''
import sys
import pprint
from subprocess import call
import option
import time

from query_loader import QueryLog
from map_loader import Map 
from geom_util import EdgeSegment, EdgeSegmentSet, Point, Query, CloakingSet, MBR, get_distance
from map_visualizer import MapVisualizer
from weighted_set_cover import find_init_cover, find_next_cover

########################################################
class Node: pass
 
def kdtree(point_list, depth=0):
 
    if not point_list:
        return None
 
    # Select axis based on depth so that axis cycles through all valid values
    k = len(point_list[0]) # assumes all points have the same dimension
    axis = depth % k
 
    # Sort point list and choose median as pivot element
    point_list.sort(key=lambda point: point[axis])
    median = len(point_list) // 2 # choose median
 
    # Create node and construct subtrees
    node = Node()
    node.location = point_list[median]
    node.left_child = kdtree(point_list[:median], depth + 1)
    node.right_child = kdtree(point_list[median + 1:], depth + 1)
    return node

def pre_order(tree):
    print tree.location
    if tree.left_child:
        pre_order(tree.left_child) 
    if tree.right_child:
        pre_order(tree.right_child)

def in_order(tree):
    if tree.left_child:
        in_order(tree.left_child) 
    print tree.location    
    if tree.right_child:
        in_order(tree.right_child)
        
def post_order(tree):
    if tree.left_child:
        post_order(tree.left_child) 
    if tree.right_child:
        post_order(tree.right_child)                
    print tree.location


########################################################
class intervalTree:
    def __init__(self, data, si, ei, start, end):
        '''
        data: an array of elements where each element contains start coodinate, end coordinate, and element id.
        si: index or key of the start coodinate in each element
        ei: index or key of the end coordinate in each element
        start: position of the start position of the element range
        end: posotion of the end position of the element range

        for example, a reference genome of a million base pairs with the following features:
            features = [[20,400,'id01'],[1020,2400,'id02'],[35891,29949,'id03'],[900000,'id04'],[999000,'id05']]
        to make a tree:
            myTree = intervalTree(features, 0, 1, 1, 1000000)
        '''
        self.si = si
        self.ei = ei
        self.start = start
        self.end = end
        self.elementaryIntervals = self.getElementaryIntervals(data, si, ei)
        self.tree = self.recursiveBuildTree(self.elementaryIntervals)
        self.insertData(self.tree, data, si, ei, start, end)
        self.trimTree(self.tree)

    def getElementaryIntervals(self, data, si, ei):
        '''generates a sorted list of elementary intervals'''
        coords = []
        [coords.extend([x[si],x[ei]]) for x in data]
        coords = list(set(coords))
        coords.sort()

        return coords

    def recursiveBuildTree(self, elIntervals):
        '''
        recursively builds a BST based on the elementary intervals.
        each node is an array: [interval value, left descendent nodes, right descendent nodes, [ids]].
        nodes with no descendents have a -1 value in left/right descendent positions.

        for example, a node with two empty descendents:
            [500,                               interval value
                [-1,-1,-1,['id5','id6']],       left descendent
                [-1,-1,-1,['id4']],             right descendent
                ['id1',id2',id3']]              data values

        '''
        center = int(round(len(elIntervals) / 2))

        left = elIntervals[:center]
        right = elIntervals[center + 1:]
        node = elIntervals[center]

        if len(left) > 1:
            left = self.recursiveBuildTree(left)
        elif len(left) == 1:
            left = [left[0],[-1,-1,-1,[]],[-1,-1,-1,[]],[]]
        else:
            left = [-1,-1,-1,[]]

        if len(right) > 1:
            right = self.recursiveBuildTree(right)
        elif len(right) == 1:
            right = [right[0],[-1,-1,-1,[]],[-1,-1,-1,[]],[]]
        else:
            right = [-1,-1,-1,[]]

        return [node, left, right, []]

    def ptWithin(self, pt, subject):
        '''accessory function to check if a point is within a range'''
        if pt >= subject[0] and pt <= subject[1]:
            return True

        return False

    def isWithin(self, query, subject):
        '''accessory function to check if a range is fully within another range'''
        if self.ptWithin(query[0], subject) and self.ptWithin(query[1], subject):
            return True

        return False

    def overlap(self, query, subject):
        '''accessory function to check if two ranges overlap'''
        if self.ptWithin(query[0], subject) or self.ptWithin(query[1], subject) or self.ptWithin(subject[0], query) or self.ptWithin(subject[1], query):
            return True

        return False

    def recursiveInsert(self, node, coord, data, start, end):
        '''recursively inserts id data into nodes'''
        if node[0] != -1:
            left = (start, node[0])
            right = (node[0], end)

            #if left is totally within coord
            if self.isWithin(left, coord):
                node[1][-1].append(data)
            elif self.overlap(left, coord):
                self.recursiveInsert(node[1], coord, data, left[0], left[1])

            if self.isWithin(right, coord):
                node[2][-1].append(data)
            elif self.overlap(right, coord):
                self.recursiveInsert(node[2], coord, data, right[0], right[1])

    def insertData(self, node, data, si, ei, start, end):
        '''loops through all the data and inserts them into the empty tree'''
        for item in data:
            self.recursiveInsert(node, [item[si], item[ei]], item[-1], start, end)

    def trimTree(self, node):
        '''trims the tree for any empty data nodes'''
        dataLen = len(node[-1])

        if node[1] == -1 and node[2] == -1:
            if dataLen == 0:
                return 1
            else:
                return 0
        else:
            if self.trimTree(node[1]) == 1:
                node[1] = -1

            if self.trimTree(node[2]) == 1:
                node[2] = -1

            if node[1] == -1 and node[2] == -1:
                if dataLen == 0:
                    return 1
                else:
                    return 0

    def find(self, node, findRange, start, end):
        '''recursively finds ids within a range'''
        data = []

        left = (start, node[0])
        right = (node[0], end)

        if self.overlap(left, findRange):
            data.extend(node[-1])
            if node[1] != -1:
                data.extend(self.find(node[1], findRange, left[0], left[1]))

        if self.overlap(right, findRange):
            data.extend(node[-1])
            if node[2] != -1:
                data.extend(self.find(node[2], findRange, right[0], right[1]))

        return list(set(data))

    def findRange(self, findRange):
        '''wrapper for find'''
        return self.find(self.tree, findRange, self.start, self.end)

    def pprint(self, ind):
        '''pretty prints the tree with indentation'''
        pp = pprint.PrettyPrinter(indent=ind)
        pp.pprint(self.tree)

########################################################
def test_stack():
    stack = []
    print stack, len(stack)
    
    stack.append((1,'a',2))
    stack.append((2,'b',2))
    stack.append((3,'c',2))
    
    print stack
    
    a = stack.pop()
    print "a = ", a
    
    print stack
    
    stack.pop()
    
    print stack
    
    stack.append((3,'c',2))
    
    print stack, len(stack)
    
def test_queue():
    queue = []
    print queue, len(queue)
    
    queue.append((1,'a',2))
    queue.append((2,'b',2))
    queue.append((3,'c',2))
    
    print queue
    
    a = queue.pop(0)
    print "a = ", a
    
    print queue
    
    queue.pop()
    
    print queue
    
    queue.append((3,'c',2))
    
    print queue, len(queue)    
    
def test_binary_search():
    data = (1,3,5,7,9,11)
    point = 8
    
    #
    lo = 0
    hi = len(data) - 1
    mid = (lo + hi) / 2
    found = False
    while True:
        if data[mid] == point:
            found = True
            break
        if data[mid] > point:
            hi = mid - 1
            if hi < lo: 
                break
        else:
            lo = mid + 1
            if lo > hi:
                break
        mid = (lo + hi) / 2    
    
    print found, mid , lo, hi  

def check_mc_set():
    
    mc_set = [set([514, 684, 655]), set([802, 514, 655]), set([538, 684, 655]), set([538, 684, 655]), set([544, 802, 655]), set([544, 802, 655]), set([514, 684]), set([802, 514]), set([657, 830]), set([368, 658]), set([658, 699]), set([659, 375]), set([659, 661]), set([872, 375]), set([929, 663]), set([244, 663]), set([464, 663]), set([664, 66]), set([664, 595]), set([64, 665]), set([665, 326]), set([64, 779]), set([666, 380]), set([912, 667]), set([65, 667]), set([83, 667]), set([160, 667]), set([393, 667]), set([562, 667]), set([624, 667]), set([0, 668, 717]), set([668, 782]), set([668, 549]), set([0, 740]), set([740, 549]), set([914, 669]), set([44, 669]), set([218, 669]), set([669, 221]), set([669, 311]), set([669, 365]), set([669, 381]), set([669, 389]), set([416, 669]), set([449, 669]), set([483, 669]), set([669, 495]), set([516, 669]), set([796, 669]), set([669, 574]), set([669, 583]), set([16, 670]), set([17, 670]), set([75, 670]), set([670, 102]), set([140, 670]), set([248, 670]), set([670, 543]), set([16, 764]), set([17, 764]), set([764, 102]), set([764, 140]), set([675, 543]), set([241, 671]), set([673, 406]), set([673, 471]), set([673, 601]), set([674, 359]), set([722, 359]), set([48, 675]), set([675, 77]), set([173, 847]), set([708, 173]), set([88, 677]), set([123, 677]), set([387, 677]), set([328, 678]), set([680, 334]), set([680, 453]), set([680, 582]), set([680, 607]), set([682, 226]), set([682, 310]), set([682, 357]), set([488, 682]), set([841, 683]), set([683, 638]), set([202, 684]), set([346, 684]), set([480, 684]), set([700, 918]), set([161, 702]), set([163, 702]), set([704, 748, 726]), set([706, 31]), set([706, 391]), set([706, 590]), set([185, 707]), set([709, 943]), set([184, 709]), set([579, 709]), set([712, 67, 710]), set([712, 93, 710]), set([712, 710, 174]), set([712, 249, 710]), set([712, 603, 710]), set([712, 883, 174]), set([867, 174]), set([712, 883, 831]), set([144, 714]), set([714, 155]), set([714, 164]), set([714, 178]), set([714, 258]), set([714, 596]), set([714, 429]), set([731, 164]), set([258, 731]), set([731, 429]), set([716, 629]), set([913, 717]), set([109, 718]), set([268, 718]), set([74, 719]), set([324, 719]), set([720, 946]), set([721, 497]), set([722, 220]), set([723, 187]), set([725, 135]), set([899, 726]), set([728, 204]), set([728, 281]), set([729, 821, 519]), set([730, 195]), set([128, 731]), set([731, 524]), set([40, 733]), set([69, 734]), set([379, 734]), set([688, 737]), set([738, 333]), set([738, 438]), set([738, 452]), set([738, 518]), set([617, 738]), set([739, 7]), set([880, 12, 742]), set([813, 246, 743]), set([848, 743]), set([137, 751]), set([752, 121, 862]), set([754, 781]), set([808, 781]), set([285, 799]), set([777, 285]), set([744, 757]), set([757, 341]), set([898, 758, 238]), set([761, 355]), set([762, 541]), set([633, 762]), set([762, 634]), set([762, 654]), set([763, 198]), set([198, 775]), set([337, 764]), set([765, 247]), set([792, 765]), set([315, 766]), set([769, 308]), set([856, 770, 171]), set([771, 62]), set([771, 284]), set([432, 774]), set([317, 775]), set([466, 775]), set([520, 775]), set([776, 275]), set([777, 787]), set([777, 631]), set([778, 735]), set([800, 47]), set([800, 79]), set([800, 433]), set([800, 605]), set([842, 803, 804]), set([505, 807]), set([808, 190]), set([809, 741]), set([810, 37]), set([810, 52]), set([810, 364]), set([810, 447]), set([811, 909]), set([816, 19]), set([816, 298]), set([819, 84]), set([820, 270]), set([820, 575]), set([635, 820]), set([864, 821]), set([513, 823]), set([824, 261]), set([824, 266]), set([825, 169]), set([441, 828]), set([536, 828]), set([897, 854, 134, 863]), set([897, 694, 854, 863]), set([139, 855]), set([857, 931]), set([857, 532]), set([859, 86]), set([860, 639]), set([864, 282]), set([864, 329]), set([312, 865]), set([865, 430]), set([865, 551]), set([866, 805]), set([867, 295]), set([552, 868]), set([818, 869]), set([38, 870]), set([794, 870]), set([564, 870]), set([874, 50]), set([877, 213]), set([280, 881]), set([885, 662]), set([888, 267, 887]), set([888, 932]), set([888, 478]), set([889, 660]), set([890, 925]), set([890, 756]), set([307, 892]), set([852, 894]), set([896, 56]), set([896, 691]), set([896, 338]), set([896, 614]), set([697, 898]), set([895]), set([893]), set([891]), set([886]), set([884]), set([882]), set([879]), set([878]), set([876]), set([875]), set([873]), set([871]), set([861]), set([858]), set([853]), set([851]), set([850]), set([829]), set([827]), set([826]), set([822]), set([817]), set([815]), set([814]), set([812]), set([806]), set([801]), set([773]), set([772]), set([768]), set([767]), set([849]), set([759]), set([753]), set([750]), set([736]), set([732]), set([798]), set([797]), set([715]), set([713]), set([711]), set([705]), set([701]), set([685]), set([681]), set([679]), set([672]), set([656]), set([653]), set([652]), set([651]), set([650]), set([647]), set([646]), set([645]), set([644]), set([643]), set([642]), set([641]), set([640]), set([637]), set([636]), set([632]), set([630]), set([628]), set([627]), set([626]), set([625]), set([623]), set([622]), set([621]), set([620]), set([619]), set([618]), set([616]), set([615]), set([613]), set([612]), set([611]), set([610]), set([609]), set([608]), set([606]), set([604]), set([602]), set([600]), set([591]), set([589]), set([588]), set([587]), set([586]), set([585]), set([584]), set([581]), set([580]), set([578]), set([577]), set([576]), set([573]), set([572]), set([571]), set([570]), set([569]), set([568]), set([567]), set([566]), set([565]), set([563]), set([561]), set([560]), set([559]), set([558]), set([557]), set([556]), set([555]), set([554]), set([553]), set([550]), set([542]), set([540]), set([537]), set([535]), set([534]), set([533]), set([531]), set([599]), set([846]), set([528]), set([795]), set([526]), set([845]), set([523]), set([522]), set([521]), set([517]), set([515]), set([512]), set([510]), set([509]), set([508]), set([507]), set([506]), set([504]), set([503]), set([502]), set([749]), set([500]), set([496]), set([494]), set([493]), set([492]), set([491]), set([490]), set([489]), set([487]), set([793]), set([485]), set([484]), set([482]), set([481]), set([479]), set([477]), set([476]), set([475]), set([474]), set([473]), set([472]), set([470]), set([469]), set([468]), set([467]), set([465]), set([463]), set([462]), set([461]), set([459]), set([458]), set([457]), set([456]), set([455]), set([454]), set([448]), set([446]), set([445]), set([444]), set([443]), set([442]), set([440]), set([844]), set([437]), set([436]), set([435]), set([434]), set([431]), set([428]), set([427]), set([426]), set([425]), set([424]), set([423]), set([422]), set([420]), set([419]), set([418]), set([417]), set([415]), set([414]), set([413]), set([412]), set([411]), set([698]), set([843]), set([407]), set([405]), set([747]), set([403]), set([402]), set([401]), set([598]), set([399]), set([398]), set([397]), set([396]), set([395]), set([394]), set([392]), set([390]), set([388]), set([386]), set([385]), set([384]), set([383]), set([382]), set([378]), set([377]), set([791]), set([374]), set([746]), set([372]), set([371]), set([370]), set([369]), set([367]), set([366]), set([363]), set([361]), set([360]), set([358]), set([356]), set([354]), set([353]), set([352]), set([351]), set([350]), set([349]), set([348]), set([547]), set([790]), set([344]), set([343]), set([342]), set([340]), set([339]), set([336]), set([335]), set([332]), set([597]), set([327]), set([325]), set([322]), set([321]), set([320]), set([319]), set([318]), set([840]), set([314]), set([696]), set([309]), set([839]), set([838]), set([304]), set([303]), set([302]), set([301]), set([300]), set([299]), set([297]), set([296]), set([294]), set([695]), set([837]), set([291]), set([290]), set([289]), set([288]), set([789]), set([745]), set([279]), set([278]), set([649]), set([276]), set([274]), set([273]), set([272]), set([271]), set([265]), set([693]), set([263]), set([262]), set([260]), set([692]), set([257]), set([255]), set([254]), set([253]), set([252]), set([251]), set([250]), set([245]), set([594]), set([242]), set([593]), set([239]), set([237]), set([236]), set([235]), set([234]), set([233]), set([232]), set([788]), set([230]), set([229]), set([836]), set([227]), set([225]), set([223]), set([222]), set([219]), set([216]), set([215]), set([214]), set([212]), set([211]), set([690]), set([209]), set([208]), set([207]), set([206]), set([205]), set([203]), set([201]), set([200]), set([199]), set([786]), set([196]), set([194]), set([785]), set([192]), set([191]), set([189]), set([188]), set([186]), set([183]), set([182]), set([592]), set([180]), set([179]), set([177]), set([176]), set([835]), set([172]), set([170]), set([168]), set([167]), set([784]), set([165]), set([162]), set([159]), set([158]), set([689]), set([156]), set([154]), set([783]), set([498]), set([151]), set([150]), set([149]), set([148]), set([147]), set([146]), set([145]), set([143]), set([142]), set([141]), set([138]), set([545]), set([133]), set([132]), set([131]), set([130]), set([129]), set([127]), set([126]), set([125]), set([124]), set([834]), set([119]), set([118]), set([117]), set([116]), set([115]), set([114]), set([113]), set([112]), set([111]), set([110]), set([108]), set([107]), set([833]), set([105]), set([104]), set([103]), set([101]), set([100]), set([832]), set([98]), set([97]), set([96]), set([95]), set([94]), set([92]), set([91]), set([90]), set([89]), set([87]), set([85]), set([82]), set([648]), set([80]), set([76]), set([687]), set([72]), set([71]), set([70]), set([68]), set([63]), set([61]), set([60]), set([59]), set([58]), set([57]), set([55]), set([53]), set([51]), set([49]), set([46]), set([43]), set([42]), set([41]), set([39]), set([36]), set([35]), set([34]), set([33]), set([32]), set([30]), set([29]), set([28]), set([27]), set([26]), set([25]), set([24]), set([23]), set([21]), set([20]), set([18]), set([686]), set([14]), set([13]), set([11]), set([10]), set([9]), set([8]), set([6]), set([5]), set([780]), set([3]), set([2]), set([1]), set([900]), set([901]), set([902]), set([903]), set([904]), set([905]), set([906]), set([907]), set([908]), set([910]), set([911]), set([915]), set([916]), set([917]), set([919]), set([920]), set([921]), set([922]), set([923]), set([924]), set([926]), set([927]), set([928]), set([930]), set([933]), set([934]), set([935]), set([936]), set([937]), set([938]), set([939]), set([940]), set([941]), set([942]), set([944]), set([945]), set([947]), set([948]), set([949])]
    for i in range(len(mc_set)):
        for j in range(i+1, len(mc_set)):
            if mc_set[i] == mc_set[j]:
                print i,j,mc_set[i] 
    

def test_2d_array():
    MAX_NODE = 3
    edges = [[0]*MAX_NODE for x in xrange(MAX_NODE)]
    
    edges[0][0] = 1
    
    print edges
    
def test_return_pair():
    return (1, [1,2,3])    

############################################
if __name__ == '__main__':
    
    #test_queue()
    #test_stack()
    #test_binary_search()
    #check_mc_set()

#    print "%15d %10.2f" % (427204608, 24162.3349917)
    
#    print "%15d %10.2f %10.2f %10.2f %10.2f" % item.cur_edge_id, \
#                item.start_x, item.start_y, item.end_x, item.end_y

    ##
    ## LIST
#    list_1 = [1,2,3]
#    list_2 = [4,5,6]
#    list_1.extend(list_2)
#    
#    print list_1


    ##
    ## SET
#    mc_set_1 = [set(['a','b','c']), set(['a','c','d'])]
#    mc_set_2 = [set(['a','e','f']), set(['a','d','e'])]
    
#    mc_set_1 = [set([1,2,3]), set([1,3,4])]
#    mc_set_2 = [set([1,5,6]), set([1,4,5])]
#        
##    print mc_set_1
##    print mc_set_1[0]
#
#    for set_1 in mc_set_1:
#        for set_2 in mc_set_2:
#            print set_1 & set_2

#    set_1 = set([1,2,3])
#    print set_1 | [2,4]
    
#    set_1 = set([1,2,3])
#    set_2 = set([1,3,2])
#    print set_1 == set_2
#    set_2 = set([1,3,2,4])
#    print set_1 == set_2

#    print len(set([3]) - set([1,3]))

#    print type([(1,2)][0][0])

#    test_2d_array()


#    print [0]*3
#    
#    print range(1,3)
    
    #TEST, count mc_set
#    mc_set = [set([304, 593, 534, 428, 173, 477]), set([416, 511, 180, 78, 495]), set([416, 442, 78, 495]), set([224, 491, 588, 93, 454]), set([224, 67, 93, 454, 491]), set([224, 454, 329, 491, 93]), set([224, 277, 454, 491]), set([153, 99, 588, 317]), set([489, 183, 20, 303]), set([489, 98, 20, 303]), set([488, 529, 334, 143]), set([488, 544, 143]), set([486, 591]), set([577, 258, 308, 346, 486]), set([161, 308, 577, 97, 57, 458, 476, 142]), set([161, 258, 308, 577, 57, 346]), set([265, 485, 357, 561]), set([348, 357, 485]), set([450, 18, 220, 482]), set([18, 171, 220, 482]), set([480, 473, 311]), set([480, 338, 311, 410]), set([480, 144, 338, 410]), set([480, 338, 533, 410]), set([304, 316, 428, 477, 173]), set([161, 97, 57, 458, 476, 74, 142]), set([161, 97, 57, 458, 476, 525, 142]), set([384, 89, 475, 457]), set([384, 457, 11]), set([457, 330, 11, 245]), set([465, 434, 331, 570]), set([465, 434, 331, 594]), set([65, 570, 83, 284]), set([65, 570, 284, 62]), set([65, 570, 83, 6]), set([408, 508, 453, 255]), set([408, 403, 453, 255]), set([408, 273, 453, 255]), set([408, 453, 262, 255]), set([408, 41, 453, 255]), set([0, 576, 154, 452]), set([0, 154, 452, 599]), set([452, 118]), set([296, 193, 445, 278]), set([296, 566, 445, 278]), set([416, 373, 442, 285, 78]), set([389, 442, 285, 78, 373]), set([409, 442]), set([565, 188, 125, 318, 429]), set([338, 410, 429]), set([256, 424, 34, 178, 583]), set([256, 424, 34, 178, 552]), set([420, 269, 422, 87]), set([328, 25, 419, 500, 199]), set([416, 180, 373, 78]), set([104, 408, 343]), set([376, 400, 299, 289]), set([376, 400, 299, 585]), set([120, 2, 395, 194]), set([353, 202, 596]), set([161, 346, 229]), set([344, 562, 43]), set([344, 43, 572]), set([344, 83]), set([337, 226, 343, 104, 107, 524]), set([337, 226, 513, 104, 343, 107]), set([337, 343, 548, 513, 104, 107]), set([337, 343, 548, 513, 107, 350]), set([584, 330, 11, 245]), set([555, 330, 11, 245]), set([330, 11, 10]), set([328, 294, 199]), set([99, 317, 277]), set([99, 317, 295]), set([99, 317, 261]), set([192, 304]), set([280, 387, 239]), set([280, 306, 239]), set([88, 306, 280, 15]), set([265, 31]), set([265, 325]), set([592, 106, 259, 156, 138]), set([106, 259, 156, 138, 567]), set([256, 34, 244, 178]), set([8, 245]), set([537, 2, 194]), set([194, 2, 274]), set([57, 161, 519]), set([8, 40, 23]), set([513, 548, 117, 350]), set([547, 132, 437, 342, 392, 170]), set([545, 27, 5]), set([518, 214, 7]), set([307, 268, 518]), set([512, 100, 207]), set([347, 509, 231]), set([347, 231, 499]), set([505, 444, 405]), set([451, 468, 505, 267]), set([409, 505]), set([44, 374, 503]), set([210, 44, 374]), set([44, 532, 374]), set([385, 502, 252, 254]), set([385, 418, 252, 254]), set([385, 182, 252, 254]), set([385, 55, 252, 254]), set([288, 404, 246, 134, 492, 77]), set([404, 230, 492, 77]), set([404, 412, 230]), set([288, 404, 412]), set([288, 68, 134]), set([449, 483, 324, 381, 111]), set([449, 324, 358, 481, 145, 381, 367]), set([449, 324, 481, 145, 218, 381, 367]), set([449, 324, 481, 145, 73, 381, 367]), set([449, 324, 481, 145, 516, 381, 367]), set([449, 324, 516, 381, 111]), set([33, 324, 358, 481, 136, 145, 367]), set([33, 324, 481, 136, 145, 218, 367]), set([33, 324, 481, 136, 73, 145, 367]), set([33, 324, 481, 136, 145, 516, 367]), set([33, 324, 516, 111]), set([33, 517, 481, 136, 145, 367]), set([481, 372, 367]), set([368, 332, 479]), set([368, 327, 479]), set([368, 112, 479]), set([467, 478]), set([467, 302]), set([432, 469]), set([432, 158]), set([51, 468, 358, 267, 451]), set([515, 51, 468, 267, 451]), set([225, 52, 462]), set([128, 460]), set([360, 166, 456, 31]), set([16, 121, 190, 31]), set([281, 443, 119]), set([389, 26, 285, 373]), set([285, 54, 373]), set([440, 345, 497]), set([307, 438, 268, 380]), set([307, 268, 147]), set([59, 427, 222]), set([371, 21, 75, 415]), set([21, 543, 75, 415]), set([371, 21, 248, 75]), set([21, 248, 75, 543]), set([371, 326, 248, 75]), set([326, 248, 75, 543]), set([248, 402, 21, 543]), set([148, 45, 407]), set([72, 401, 356]), set([370, 76, 397]), set([393, 247]), set([122, 140, 391]), set([361, 379]), set([379, 157, 461]), set([402, 326, 248, 543]), set([402, 326, 248, 546]), set([320, 279]), set([29, 309]), set([29, 39]), set([306, 187]), set([306, 276]), set([88, 377, 15]), set([192, 82, 137, 270]), set([176, 506, 94, 287]), set([506, 287, 439]), set([176, 530, 94]), set([176, 90]), set([282, 91]), set([3, 243, 137, 270]), set([260, 103]), set([253, 206, 215]), set([160, 249, 159, 127]), set([544, 221, 238]), set([221, 238, 519]), set([113, 235, 236]), set([235, 53]), set([228, 124, 13]), set([201, 213, 30]), set([133, 191]), set([16, 121, 190, 543]), set([84, 108, 189]), set([61, 149]), set([38, 70, 295]), set([38, 261, 70]), set([73, 354]), set([354, 517]), set([412, 375]), set([409, 22]), set([402, 14]), set([35, 470, 383]), set([35, 163, 383]), set([354, 372]), set([329, 295]), set([329, 261]), set([329, 155, 174]), set([281, 204]), set([17, 348]), set([81, 455]), set([116, 399]), set([394, 63]), set([378, 85]), set([17, 293]), set([336, 203]), set([9, 313]), set([9, 79]), set([292, 37]), set([37, 463]), set([80, 257]), set([208, 53]), set([1]), set([4]), set([12]), set([19]), set([24]), set([28]), set([32]), set([36]), set([42]), set([46]), set([47]), set([48]), set([49]), set([50]), set([56]), set([58]), set([60]), set([64]), set([66]), set([69]), set([71]), set([86]), set([92]), set([95]), set([96]), set([101]), set([102]), set([105]), set([109]), set([110]), set([114]), set([115]), set([123]), set([126]), set([129]), set([130]), set([131]), set([135]), set([139]), set([141]), set([146]), set([150]), set([151]), set([162]), set([164]), set([165]), set([167]), set([168]), set([169]), set([172]), set([175]), set([177]), set([179]), set([181]), set([184]), set([185]), set([186]), set([195]), set([196]), set([197]), set([198]), set([200]), set([205]), set([209]), set([211]), set([212]), set([216]), set([217]), set([219]), set([223]), set([227]), set([232]), set([233]), set([234]), set([237]), set([240]), set([241]), set([242]), set([250]), set([251]), set([263]), set([264]), set([266]), set([271]), set([272]), set([275]), set([283]), set([286]), set([290]), set([291]), set([297]), set([298]), set([300]), set([301]), set([305]), set([310]), set([312]), set([314]), set([315]), set([319]), set([321]), set([322]), set([323]), set([333]), set([335]), set([339]), set([340]), set([341]), set([349]), set([351]), set([352]), set([355]), set([359]), set([362]), set([363]), set([364]), set([365]), set([366]), set([369]), set([382]), set([386]), set([388]), set([390]), set([396]), set([398]), set([406]), set([411]), set([413]), set([414]), set([417]), set([421]), set([423]), set([425]), set([426]), set([430]), set([431]), set([433]), set([435]), set([436]), set([441]), set([446]), set([447]), set([448]), set([459]), set([464]), set([466]), set([471]), set([472]), set([474]), set([484]), set([487]), set([490]), set([493]), set([494]), set([496]), set([498])]
#    print len(mc_set)
#    max_size = 0;
#    size_arr = [0]*100;
#    for  s in mc_set:
#        s_size = len(s)
#        if max_size < s_size:
#            max_size = s_size
#        size_arr[s_size] += 1
#    
#    print "max_size=", max_size
#    for i in range(1,max_size+1):
#        print i, size_arr[i]     

    #TEST: KD-Tree 
#    point_list = [(2,3), (5,4), (9,6), (4,7), (8,1), (7,2)]
#    tree = kdtree(point_list)
#    
#    print "pre_order"
#    pre_order(tree)
#
#    print "in_order"
#    in_order(tree)
#    
#    print "post_order"
#    post_order(tree)

    #TEST: Interval-Tree
#    features = [ [1,90,'A'],[90,125,'B'],[25,60,'C'],[100,170,'D'],[170,220,'E'],[220,250,'F'],[600,700,'G'],\
#                [120,125,'H'],[500,550,'I'],[1000,1200,'J'],[800,850,'K'],[1100,1500,'L'],[10,500,'M'] ]
##    features = [ [1,90,1],[90,125,2],[25,60,3],[100,170,4],[170,220,5],[220,250,6],[600,700,7],\
##                [120,125,8],[500,550,9],[1000,1200,10],[800,850,11],[1100,1500,12],[10,500,13] ]
#    myTree = intervalTree(features, 0, 1, 1, 2000)
#    results = myTree.findRange([100,200])
#    
#    print results

    #TEST
#    point_list = [(2,3), (5,4), (9,6), (4,7), (8,1), (7,2)]
#    print point_list
#    point_list = sorted(point_list, key=lambda pair: pair[1])
#    print point_list   
#    point_list.pop()
#    print point_list  
#    print max(point_list, key=lambda pair: pair[0])

    #TEST
#    can_cr = [set([1,2]), set([1,2,3]), set([1])]
#    print can_cr
#    can_cr = sorted(can_cr, key=lambda clique:len(clique), reverse=True)
#    print can_cr
#    
    
    
    #TEST - write text file
#    list_edges = [(1,2), (3,4), (3,2), (1,1), (2,5)]
#    
##    list_edges.sort()
##    print list_edges
#    
#    max_edge_id = max(list_edges, key=lambda e: e[0])[0]
#    print max_edge_id
#    max_edge_id = max(max_edge_id, max(list_edges, key=lambda e: e[1])[1])
#    print max_edge_id
#    
#    list_adj = [[] for i in range(max_edge_id+1)]
#    print list_adj
#    list_adj[0].append(2)
#    print len(list_adj[0])
#    print list_adj
    
#    f = open('../edges_1.txt', 'w')
#    for e in list_edges:
#        f.write("%d %d\n"%(e[0],e[1]))
#    f.close()
    #
    
    
    #TEST
#    (a, b) = test_return_pair()
#    print a, b


    #TEST - subprocess.call(): call external commands
#    call(["../mace_go.exe", "M", "../edges_3.txt", "../edges_3.out"], shell=False)

#    f = open("../edges_3.out", "r")
#    fstr = f.read()
#    mc_set = []     #list of sets
#    for line in fstr.split("\n"):
#        node_list = line.split(" ")
#        if len(node_list) < 2:
#            continue
#        mc_set.append(set([int(node) for node in node_list]))
#        
#    print len(mc_set)     
        
    #TEST    
#     name = "oldenburgGen_5000_0_10_0_100_20_0_1000_250.txt"
#     print name[0:-4]
        
    #TEST
#    str = "11694.00,16656.00,11730.00,16535.00,55501082:11762.00,16432.00,11769.00,16364.00,55600557:"
#    list = str.split(":")
#    print len(list)
#    print list
#    
#    list.pop()
#    print len(list)
#    print list

    #TEST - sort two lists 
#    labels = [ 'B', 'C', 'A' ]
#    values = [ 30, 10, 20 ]
#    z = sorted(zip(values,labels))
#    # z
#    # [(10, 'first'), (20, 'second'), (30, 'third')]
#    values_sorted, labels_sorted = zip(*z)
#    # labels_sorted
#    # (10, 20, 30)
#    # values_sorted
#    # ('first', 'second', 'third')
#    print labels_sorted
#    print values_sorted
#    print values[0:1]


    #TEST - parallel iteration
#    list_a = [1,2,3,4]
#    list_b = ['a','b','c','d']
#    
#    for a,b in zip(list_a, list_b):
#        print a, b

    #TEST - list of lists
#    a = []
#    a.append([])
#    a.append([])
#    a.append([])
#    a[0].append(1)
#    a[1].append(2)
#    a[1].append(3)
#    a[2].append(4)
#    a[2].append(5)
#    
#    print a
#    
#    for (i, s) in enumerate(a):
#        print i, s
    
#    del a[1]
#    print a


    #TEST - command line param
#    for param in sys.argv:
#        print param

    #TEST - print float value
#    a = 1.0/3
#    b = 2
#    print a,"%d"%b 
#    print "%f" % a
#    print "%.2f" % a
#    print "%f" % b
#    
#    f = open("../temp.txt", "w")
#    f.write(str(a) + ",")
#    f.write(str(b))
#    f.close()

    #TEST - compare sets
#    results = [set([1,2,3]), set([1,3,2]), set([1,2,3]), set([2,1]), set([1,2]), set([1,2,3,4])]
#    results = sorted(results, key=lambda a_set:len(a_set), reverse=True)
#    i = 0
#    while i < len(results)-1:
#        if len(results[i]) == len(results[i+1]):
#            if len(results[i]) == len(results[i] & results[i+1]): 
#                del results[i+1]
#        else:
#            i += 1
#    
#    print results
    
    
    #TEST - list of lists
#    listoflists = []
#    a_list = []
#    for i in range(0,10):
#        a_list.append(i)
#        if len(a_list)>3:
#            a_list.remove(a_list[0])
#            listoflists.append((list(a_list), a_list[0]))
#    print listoflists
    
#     ac_events = []
#     for i in range(5):
#         ac_events.append(list([]))
#     print ac_events   
#     
#     ac_events[0].append(2)
#     ac_events[0].append(3)
#     ac_events[3].append([1,4,3])
#     print ac_events
    
    # TEST Map.compute_fixed_expanding(), EdgeSegmentSet.clean_fixed_expanding()
    timestep = 3
    print "MAP_FILE =", option.MAP_FILE
    print "timestep =", timestep
    print "QUERY_FILE =", option.QUERY_FILE
    print "DISTANCE_CONSTRAINT =", option.DISTANCE_CONSTRAINT
     
    #    
    start_time = time.clock()
         
    map_data = Map()
    map_data.read_map(option.MAP_PATH, option.MAP_FILE)
    print "Load Map : DONE"
    query_log = QueryLog(map_data)
    query_log.read_query(option.QUERY_PATH, option.QUERY_FILE, timestep, option.QUERY_TYPE)   # default: max_time_stamp = 10 (40: only for attack) 
    print "Load Query : DONE"
     
    print "max_speed = ", query_log.max_speed
    print "elapsed : ", (time.clock() - start_time)  
    
    #
    timestamp = 0;
    expanding_list = {}                                 #dict of lists
    query_list = query_log.frames[timestamp]       # timestamp
    
    
    #1. compute expanding_list
    start_time = time.clock()    
    print "#users =", len(query_list)
    count = 0
    for query in query_list:
        seg_list = map_data.compute_fixed_expanding(query.x, query.y, 
                                        query.cur_edge_id, query.dist)  #old: option.DISTANCE_CONSTRAINT
        
        seg_list_length = 0.0
        for seg in seg_list:
            seg_list_length += EdgeSegment.length(seg);
        print "seg_list.size = %d - %f" %(len(seg_list), seg_list_length)
        
        seg_list = EdgeSegmentSet.clean_fixed_expanding(seg_list)
        print "AFTER seg_list.size =", len(seg_list)
        
        expanding_list[query.obj_id] = seg_list
        
        count += 1
        if count == 10:
            break;
    print "expanding_list - elapsed : ", (time.clock() - start_time)   
    
