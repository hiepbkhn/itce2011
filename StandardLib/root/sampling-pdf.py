'''
Created on Feb 6, 2014

@author: huunguye
'''

import ROOT
import scipy.io
from numpy import *
import math


# Define the function and limits you want:
# TF1::TF1(const char* name, const char* formula, Double_t xmin = 0, Double_t xmax = 1)

#f = ROOT.TF1("my_pdf", "x * x / 10.", -1, 1)
#f = ROOT.TF1("my_pdf", "sin(x)/x", 1, 10)
#f = ROOT.TF1("my_pdf", "exp(-x*x/2)", -10, 10)
#f = ROOT.TF1("my_pdf", "1/x * exp(-(log(x)-20.0)*(log(x)-20.0)/20.0)", 0.01, 20*math.exp(10.0))          #exponential of normal distribution y = exp(X)
f = ROOT.TF1("my_pdf", "1/x * exp(-(log(x)+20.0)*(log(x)-20.0)/20.0)", 0.0, 1.0)          #exponential of normal distribution y = exp(X)

# Generate 100 random numbers from that distribution
samples = [f.GetRandom() for _ in range(100000)]

#scipy.io.savemat('C:/quadratic-pdf.mat', dict(samples=array(samples)))
#scipy.io.savemat('C:/sinc-pdf.mat', dict(samples=array(samples)))
#scipy.io.savemat('C:/normal-pdf.mat', dict(samples=array(samples)))
#scipy.io.savemat('C:/exp-normal-pdf.mat', dict(samples=array(samples)))
scipy.io.savemat('C:/exp-normal-inverse-pdf.mat', dict(samples=array(samples)))

#You can sample from an arbitrary 2D distribution as well:
# TF2::TF2(const char* name, const char* formula, Double_t xmin = 0, Double_t xmax = 1, Double_t ymin = 0, Double_t ymax = 1)
#f2 = ROOT.TF2("my_pdf2", "x * x / 10. + pow(y, 4)", -1, 1, 3, 4)
#x, y = ROOT.Double(), ROOT.Double()
#f2.GetRandom2(x, y)

#If you only want a histogram of values, not the array, you can avoid the python call overhead:
# TH1D::TH1D(const char* name, const char* title, Int_t nbinsx, Double_t xlow, Double_t xup)
#h = ROOT.TH1D("my_hist", "my_hist", 1000, -1, 1)

# void TH1::FillRandom(const char* fname, Int_t ntimes = 5000)
#In [49]: %timeit h.FillRandom("my_pdf", int(1e6))                                                                                                                             
#10 loops, best of 3: 171 ms per loop
#In [48]: %timeit [f.GetRandom() for _ in range(int(1e6))]                                                                                                                     
#1 loops, best of 3: 2.62 s per loop