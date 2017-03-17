/*
 * Mar 17, 2017
 * 	- translated from mmb_network/line_operator.py (class LineOperator)
 */

package geom_util;

public class LineOperator {
	
	public static double distance_to(double x1, double y1, double x2, double y2, double px, double py){
		double normalLength = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
	    return Math.abs((px-x1)*(y2-y1)-(py-y1)*(x2-x1))/normalLength;
		
	}
	
	
}
