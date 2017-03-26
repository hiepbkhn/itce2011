/*
 * string-test.cpp
 *
 *  Created on: Mar 26, 2017
 *      Author: Nguyen Huu Hiep
 */

#include <iostream>
#include <string>

using namespace std;

int main(){
	double speed = 600;
	string s_speed = to_string(speed);

	cout<<s_speed<<endl;

	//
	string a = "hello world";
	string a_sub = a.substr(0, 5);
	cout<<a_sub<<endl;

	//
	char buff[100];
	snprintf(buff, sizeof(buff), "%.1f", speed);
	string buffAsStdStr = buff;
	cout<<buffAsStdStr<<":size="<<buffAsStdStr.size();

	return 0;
}


