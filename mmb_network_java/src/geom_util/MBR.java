/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class MBR)
 */

package geom_util;

public class MBR {

	public double area;
    public double min_x;
    public double min_y;   
    public double max_x;
    public double max_y;
    
    
    
    public MBR(double area, double min_x, double min_y, double max_x, double max_y) {
		super();
		this.area = area;
		this.min_x = min_x;
		this.min_y = min_y;
		this.max_x = max_x;
		this.max_y = max_y;
	}



	//
    public static boolean is_mmb_cover(MBR mbr, Point point, double radius){
        
        if ((mbr.min_x <= point.x && point.x <= mbr.max_x) && (mbr.min_y - radius <= point.y && point.y <= mbr.max_y + radius))
            return true;
        if ((mbr.min_x - radius <= point.x && point.x <= mbr.max_x + radius) && (mbr.min_y <= point.y && point.y <= mbr.max_y))
            return true;
        if (GeomUtil.get_distance(point.x, point.y, mbr.min_x, mbr.min_y) <= radius)
            return true;
        if (GeomUtil.get_distance(point.x, point.y, mbr.min_x, mbr.max_y) <= radius)
            return true;
        if (GeomUtil.get_distance(point.x, point.y, mbr.max_x, mbr.min_y) <= radius)
            return true;
        if (GeomUtil.get_distance(point.x, point.y, mbr.max_x, mbr.max_y) <= radius)
            return true;
        
        return false;
    }
}
