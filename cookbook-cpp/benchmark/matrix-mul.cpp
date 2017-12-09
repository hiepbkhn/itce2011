/*
 * matrixMul.cpp
 *
 *  Created on: Dec 9, 2017
 *      Author: Administrator
 */


#include <iostream>
#include <cmath>
#include <vector>
#include <map>
#include <string>

#include "helper.h"

using namespace std;


///////////////////
int main(int argc, char* args[]) {
	//
	int dimAx = 320;	// num of cols
	int dimAy = 320;	// num of rows
	int dimBx = 640;
	int dimBy = dimAx;
	int dimCx = dimBx;
	int dimCy = dimAy;
	int size_A = dimAx * dimAy;
	int size_B = dimBx * dimBy;
	int size_C = dimCx * dimCy;
	//
	float *h_A = new float[size_A];
	float *h_B = new float[size_B];
	float *h_C = new float[size_C];

	for (int i = 0; i < size_A; i++)
		h_A[i] = 1.0f;
	for (int i = 0; i < size_B; i++)
		h_B[i] = 0.01f;

	//
	int nIter = 300;
	__int64 start = Timer::get_millisec();
	for (int t = 0 ; t < nIter; t++){
		for (int i = 0; i < dimAy; i++)
			for (int j = 0; j < dimBx; j++){
				h_C[i * dimBx + j] = 0.0f;
				for (int k = 0; k < dimAx; k++)
					h_C[i * dimBx + j] += h_A[i * dimAx + k] * h_B[k * dimBx + j];
			}
	}
	__int64 stop = Timer::get_millisec();

	double msecPerMatrixMul = double(stop - start) / nIter;
	double flopsPerMatrixMul = 2.0 * (double)dimAx * (double)dimAy * (double)dimBx;
	double gigaFlops = (flopsPerMatrixMul * 1.0e-9f) / (msecPerMatrixMul / 1000.0f);
	cout<<"msecPerMatrixMul = "<<msecPerMatrixMul<<endl;
	cout<<"flopsPerMatrixMul = "<<flopsPerMatrixMul<<endl;
	cout<<"gigaFlops = "<<gigaFlops<<endl;

	//
	return 0;
}

