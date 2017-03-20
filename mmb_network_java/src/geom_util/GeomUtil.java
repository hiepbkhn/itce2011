package geom_util;

import tuple.PairDouble;

public class GeomUtil {

	public static double get_edge_length(Node node1, Node node2){
	    return Math.sqrt((node1.x - node2.x)*(node1.x - node2.x) + (node1.y - node2.y)*(node1.y - node2.y));
	}

	public static double get_segment_length(Node node, double x, double y){
	    return Math.sqrt((node.x - x)*(node.x - x) + (node.y - y)*(node.y - y));
	}

	public static double get_distance(double x1, double y1, double x2, double y2){
	    return Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	}
	
	public static PairDouble get_point_on_line(double x1, double y1, double x2, double y2, double length){
	    double vector_len = Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	    double x = x1 + (x2-x1)*length/vector_len;
	    double y = y1 + (y2-y1)*length/vector_len;
	    return new PairDouble(x, y);
	}

	public static PairDouble get_point_between(double x1, double y1, double x2, double y2, double ratio){
	    double x = x1 + (x2-x1)*ratio;
		double y = y1 + (y2-y1)*ratio;
	    return new PairDouble(x, y);
	}
}
