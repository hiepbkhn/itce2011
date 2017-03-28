#include <algorithm>
#include "math.h"
#include "MMB-Win32.h"

using namespace std;

class Box {
public:
	float min[2], max[2];
	Box(float min[2], float max[2]) {
		this->min[0] = min[0];
		this->min[1]= min[1];
		this->max[0] = max[0];
		this->max[1]=max[1];
	}
};

//
class Hausdorff {

public:
	static float getHausdorffDistance(Rect rectA, Rect rectB) {
		//Pt min = new Pt(rectA.getX(), rectA.getY());
		float min[2];
		min[0]=rectA.min[0];
		min[1]=rectA.min[1];

		//Pt max = new Pt(rectA.getX() + rectA.getWidth(),
		//rectA.getY() + rectA.getHeight());
		float max[2];
		max[0]=rectA.max[0];
		max[1]=rectA.max[1];

		// Box p = new Box (min, max);
		Box p(min,max);

		//min = new Pt(rectB.getX(), rectB.getY());
		min[0]=rectB.min[0];
		min[1]=rectB.min[1];

		//max = new Pt(rectB.getX() + rectB.getWidth(),
		//rectB.getY() + rectB.getHeight());
		max[0]=rectB.max[0];
		max[1]=rectB.max[1];

		//Box q = new Box (min, max);
		Box q(min,max);

		return h(p, q) > h(q, p)?h(p, q):h(q, p);
	}

	static float h(Box p, Box q) {
		// I probably could do something with Collections.min() as well
		float temp1[2],temp2[2],temp3[2],temp4[2];

		temp1[0]=p.min[0];
		temp1[1]=p.min[1];
		float a = dist(temp1, q);

		temp2[0]=p.min[0];
		temp2[1]=p.max[1];
		float b = dist(temp2, q);

		temp3[0]=p.max[0];
		temp3[1]=p.min[1];
		float c = dist(temp3, q);

		temp4[0]=p.max[0];
		temp4[1]=p.max[1];
		float d = dist(temp4, q);

		float e = max(a,b);
		float f = max(c,d);

		return max(e,f);
	}

	static float dist(float p[2], Box Q) {
		//Pt q = new Pt(Math.min(Math.max(p.getX(), Q.min.getX()), Q.max.getX()),
		//Math.min(Math.max(p.getY(), Q.min.getY()), Q.max.getY()));
		//return Math.sqrt((p.getX() - q.getX()) * (p.getX() - q.getX()) +
		//(p.getY() - q.getY()) * (p.getY() - q.getY()));
		float q[2];
		q[0]= min(max(p[0], Q.min[0]), Q.max[0]);
		q[1]= min(max(p[1], Q.min[1]), Q.max[1]);
		return sqrt((p[0] - q[0]) * (p[0] - q[0]) +
			(p[1] - q[1]) * (p[1] - q[1]));
	}

	/**
	* To get the Hausdorff distance between two rectangles, provide
	* eight numbers: four for each rectangle. For each rectangle, the
	* first two are the top left x,y coordinates, the next two points
	* are the bottom right x,y coordinates.
	*/
	/*public static void main(String[] args) {
	double pX1 = Double.parseDouble(args[0]);
	double pY1 = Double.parseDouble(args[1]);
	double pX2 = Double.parseDouble(args[2]);
	double pY2 = Double.parseDouble(args[3]);

	double qX1 = Double.parseDouble(args[4]);
	double qY1 = Double.parseDouble(args[5]);
	double qX2 = Double.parseDouble(args[6]);
	double qY2 = Double.parseDouble(args[7]);

	Rectangle2D p = new Rectangle2D.Double(pX1, pY1, (pX2 - pX1), (pY2 - pY1));
	Rectangle2D q = new Rectangle2D.Double(qX1, qY1, (qX2 - qX1), (qY2 - qY1));

	double hdist = Functions.getHausdorffDistance(p, q);
	Debug.out("Hausdorff", "Hausorff distance for " + Debug.num(p) +
	" and " + Debug.num(q) + ": "   + Debug.num(hdist));*/
};

