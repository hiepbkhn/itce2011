/*
 * Mar 20, 2017
 * 	- translated from trace_generator/lbs_class.py (class Event)
 */

package trace;

public class Event {

	public int user_id = 0;
    public double x = 0.0;
    public double y = 0.0;  
    public int edge_id = 0;
    public int ts = 0;         // time step
    public double speed = 0.0;
    public int next_x = 0;
    public int next_y = 0; 
    public int k_anom = 0;
    public double min_length = 0.0;
    
    //
    public Event(){
    	
    }
    
    //
	public Event(int user_id, double x, double y, int edge_id, int ts, double speed, int next_x, int next_y,
			int k_anom, double min_length) {
		super();
		this.user_id = user_id;
		this.x = x;
		this.y = y;
		this.edge_id = edge_id;
		this.ts = ts;
		this.speed = speed;
		this.next_x = next_x;
		this.next_y = next_y;
		this.k_anom = k_anom;
		this.min_length = min_length;
	}
    
}
