////#include "add.h"
//#include <stdio.h>
//
//extern int add(int a, int b);
//
//int main() {
//
//	int a = 2;
//	int b = 1;
//
//	printf("a=%d, b=%d\n", a,b);
//	printf("add: %d\n", add(a,b));
//
//	getchar();
//	return 0;
//}

#include <stdio.h>

/* function declaration */
double getAverage(int arr[], int size);

double getAverage(int arr[], int size) {
	int i;
	double avg;
	double sum;

	arr[0] = 1000;
	arr[1] = 2000;
	arr[2] = 3000;
	arr[3] = 4000;
	arr[4] = 6000;

	for (i = 0; i < size; ++i) {
		sum += arr[i];
	}

	avg = sum / size;

	return avg;
}

int main() {
	/* an int array with 5 elements */
	int size = 5;
	int balance[size];

	double avg;

	/* pass pointer to the array as an argument */
	avg = getAverage(balance, 5);

	/* output the returned value */
	printf("Average value is: %f ", avg);

	return 0;
}
