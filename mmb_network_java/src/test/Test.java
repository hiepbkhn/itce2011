package test;

import java.util.HashMap;
import java.util.Map;

import map_loader.PairInt;

public class Test {

	public static void main(String[] args) {
		Map<PairInt, Integer> map = new HashMap<PairInt, Integer>();
		
		map.put(new PairInt(1,2), 3);
		
		System.out.println(map.get(new PairInt(1,2)));
		
		String a = "171-1944:0.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,|;";
		System.out.println(a.split(";").length);
	}
}
