/*
 * Mar 20, 2017
 * 	- translated from trace_generator/lbs_class.py (class Trace)
 */

package trace;

import java.util.ArrayList;
import java.util.List;

import geom_util.Edge;
import geom_util.Node;

public class Trace {

	public List<Integer> node_list = new ArrayList<Integer>();
    public List<Integer> edge_list = new ArrayList<Integer>();     // equivalent to self.node_list
}
