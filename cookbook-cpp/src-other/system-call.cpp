/*
 * system-call.cpp
 *
 *  Created on: Mar 27, 2017
 *      Author: Administrator
 */

#include <stdio.h>      /* printf */
#include <stdlib.h>     /* system, NULL, EXIT_FAILURE */

int main() {
	int i;
	printf("Checking if processor is available...");
	if (system (NULL))
		puts("Ok");
	else
		exit(EXIT_FAILURE);
	printf("Executing external program (mace_go.exe) ...\n");
	i = system("mace_go.exe M mesh.grh mesh.out");
	printf("The value returned was: %d.\n", i);
	return 0;
}

