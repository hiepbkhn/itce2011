'''
Created on Feb 6, 2014

@author: huunguye
'''

from ROOT import gROOT, TCanvas, TF1
 
gROOT.Reset()
c1 = TCanvas( 'c1', 'Example with Formula', 200, 10, 700, 500 )
 
#
# Create a one dimensional function and draw it
#
fun1 = TF1( 'fun1', 'abs(sin(x)/x)', 0, 10 )
c1.SetGridx()
c1.SetGridy()
fun1.Draw()
c1.Update()

