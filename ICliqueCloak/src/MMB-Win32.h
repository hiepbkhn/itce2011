#pragma once

#include <vector>
#include <float.h>
#include <ctime>
//#include "resource.h"


using namespace std;

#define LoadMoNumber 50000 //500//80313//57454//50000
//#define MONumber 50
//#define MaxPrivacyLevel 15
#define MaxTolerantTime 1.0//Can tolerate the maximum time
#define AMAX 1
#define AREA 6.52542e+008//2.40331e+009//6400000000 6.52542e+008
//#define RequestInterval 90
//#define MOInterval 90
//#define AREA 600000000
//#define MINPT 0.00005
//#define MAXPT 0.0001

//Specified Anonymous trigger
//bool wh_flag=1;

//Define myRect structure
struct Rect
{
	Rect()  
	{
		min[0]=0;
		min[1]=0;

		max[0]=0;
		max[1]=0;
	}

	Rect(float a_minX, float a_minY, float a_maxX, float a_maxY)
	{
		min[0] = a_minX;
		min[1] = a_minY;

		max[0] = a_maxX;
		max[1] = a_maxY;
	}


	float min[2];
	float max[2];
};

struct QuadCircle
{
	double x;
	double y;
	double r;
};

class MMB
{
public:
	Rect left;
	Rect right;
	Rect top;
	Rect down;
	QuadCircle top_left;
	QuadCircle down_left;
	QuadCircle top_right;
	QuadCircle down_right;

	double Distance(double x1,double y1, double x2,double y2);

public:
	MMB();
	MMB(Rect lf,Rect rt,Rect tp,Rect dn,
		QuadCircle t_l,QuadCircle d_l,QuadCircle t_r,QuadCircle d_r);
	~MMB(){};
	bool inMMB(double x,double y);
	void reset();
};

MMB::MMB()
{
	left.min[0]=0;
	left.min[1]=0;
	left.max[0]=FLT_MAX;
	left.max[1]=FLT_MAX;

	right.min[0]=0;
	right.min[1]=0;
	right.max[0]=FLT_MAX;
	right.max[1]=FLT_MAX;

	top.min[0]=0;
	top.min[1]=0;
	top.max[0]=FLT_MAX;
	top.max[1]=FLT_MAX;

	down.min[0]=0;
	down.min[1]=0;
	down.max[0]=FLT_MAX;
	down.max[1]=FLT_MAX;

	top_left.x=0;
	top_left.y=0;
	top_left.r=FLT_MAX;

	down_left.x=0;
	down_left.y=0;
	down_left.r=FLT_MAX;

	top_right.x=0;
	top_right.y=0;
	top_right.r=FLT_MAX;

	down_right.x=0;
	down_right.y=0;
	down_right.r=FLT_MAX;

};

MMB::MMB(Rect lf,Rect rt,Rect tp,Rect dn,
		 QuadCircle t_l,QuadCircle d_l,QuadCircle t_r,QuadCircle d_r)
{
	left=lf;
	right=rt;
	top=tp;
	down=dn;
	top_left=t_l;
	down_left=d_l;
	top_right=t_r;
	down_right=d_r;
};

void MMB::reset()
{
	left.min[0]=0;
	left.min[1]=0;
	left.max[0]=FLT_MAX;
	left.max[1]=FLT_MAX;

	right.min[0]=0;
	right.min[1]=0;
	right.max[0]=FLT_MAX;
	right.max[1]=FLT_MAX;

	top.min[0]=0;
	top.min[1]=0;
	top.max[0]=FLT_MAX;
	top.max[1]=FLT_MAX;

	down.min[0]=0;
	down.min[1]=0;
	down.max[0]=FLT_MAX;
	down.max[1]=FLT_MAX;

	top_left.x=0;
	top_left.y=0;
	top_left.r=FLT_MAX;

	down_left.x=0;
	down_left.y=0;
	down_left.r=FLT_MAX;

	top_right.x=0;
	top_right.y=0;
	top_right.r=FLT_MAX;

	down_right.x=0;
	down_right.y=0;
	down_right.r=FLT_MAX;
};

double MMB::Distance(double x1,double y1, double x2, double y2)
{
	return sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
};

bool MMB::inMMB(double x, double y)
{
	if(x>=left.min[0] && x<=left.max[0] &&
		y>=left.min[1] && y<=left.max[1])
		return 1;

	if(x>=right.min[0] && x<=right.max[0] &&
		y>=right.min[1] && y<=right.max[1])
		return 1;

	if(x>=down.min[0] && x<=down.max[0] &&
		y>=down.min[1] && y<=down.max[1])
		return 1;

	if(x>=top.min[0] && x<=top.max[0] &&
		y>=top.min[1] && y<=top.max[1])
		return 1;

	if(x>=down_left.x && x<=top_right.x &&
		y>=down_left.y && y<=top_right.y)
		return 1;

	if(Distance(top_left.x,top_left.y,x,y)<=top_left.r) return 1;

	if(Distance(down_left.x,down_left.y,x,y)<=down_left.r) return 1;

	if(Distance(top_right.x,top_right.y,x,y)<=top_right.r) return 1;

	if(Distance(down_right.x,down_right.y,x,y)<=down_right.r) return 1;

	return 0;

}

class MO
{
public:
	int id;
	int timestamp;//Read time + system clock time , the absolute time. Program unit of the timestamp is s
	float x,y; //Location
	float speed;//Speed
	int k;//The minimum degree of anonymity
	double deltT;//The longest Anonymous can tolerate
	MMB mmb;//Maximum movement boundary, as amended MMB.
	int preT;//The last success anonymous timestamp
	Rect preCR; //Moment of success anonymous = anonymous success + timestamp moment
	clock_t enTime; //CPU clock when the object enters
	double Amin;
	//int preId;
	double maxHdist;

public:
	MO();
	~MO(){};
	void UpdateMMB();
	void Reset();
	MO& operator =( const MO &rhs );

};

MO::MO()
{
	id=0;
	timestamp=0;
	x=0;
	y=0;
	speed=0;
	deltT=FLT_MAX;//Is set to infinity
	k=0;
	preT=32766; //Under the initial state is infinite
	maxHdist=FLT_MAX;

};

void MO::Reset()
{
	deltT=3e+37;
	mmb.reset();

	//Set to infinity
	preT=32766;
	preCR.min[0]=0;
	preCR.min[1]=0;
	preCR.max[0]=0;
	preCR.max[1]=0;
	maxHdist=FLT_MAX;
};

void MO::UpdateMMB()
{
	float r;
	r=speed*(timestamp-preT);

	mmb.left.min[0]=preCR.min[0]-r;
	mmb.left.min[1]=preCR.min[1];
	mmb.left.max[0]=preCR.min[0];
	mmb.left.max[1]=preCR.max[1];

	mmb.right.min[0]=preCR.max[0];
	mmb.right.min[1]=preCR.min[1];
	mmb.right.max[0]=preCR.max[0]+r;
	mmb.right.max[1]=preCR.max[1];

	mmb.top.min[0]=preCR.min[0];
	mmb.top.min[1]=preCR.max[1];
	mmb.top.max[0]=preCR.max[0];
	mmb.top.max[1]=preCR.max[1]+r;

	mmb.down.min[0]=preCR.min[0];
	mmb.down.min[1]=preCR.min[1]-r;
	mmb.down.max[0]=preCR.max[0];
	mmb.down.max[1]=preCR.min[1];

	mmb.top_left.r=r;
	mmb.top_left.x=preCR.min[0];
	mmb.top_left.y=preCR.max[1];

	mmb.down_left.r=r;
	mmb.down_left.x=preCR.min[0];
	mmb.down_left.y=preCR.min[1];

	mmb.top_right.r=r;
	mmb.top_right.x=preCR.max[0];
	mmb.top_right.y=preCR.max[1];

	mmb.down_right.r=r;
	mmb.down_right.x=preCR.max[0];
	mmb.down_right.y=preCR.min[1];

	this->maxHdist=r; 
};

MO& MO::operator =( const MO &rhs )
{
	// Not self- copy
	if ( this != &rhs )
	{
		// Class copy semantics here
		this->id=rhs.id;
		this->x=rhs.x;
		this->y=rhs.y;
		this->timestamp=rhs.timestamp;
		this->k=rhs.k;
		this->Amin=rhs.Amin;
		this->deltT=rhs.deltT;
		this->enTime=rhs.enTime;
		this->maxHdist=rhs.maxHdist;
		this->mmb=rhs.mmb;
		this->preCR=rhs.preCR;
		this->preT=rhs.preT;
	}
	// Returns the object is assigned
	return *this;
};

class MO_Briknhoff
{
public:
	char p[10];
	int id;
	int seq;
	int class_id;
	int timestamp;
	float x;
	float y;
	float speed;
	float next_x;
	float next_y;
	int k;

};




