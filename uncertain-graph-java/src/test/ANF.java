/*
 * Nov 17, 2015
 * 	- convert from C++ (SNAP) /snap/snap-core/anf.h
 */

package test;

import java.io.IOException;

import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.HyperBall;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.logging.ProgressLogger;


public class ANF {

	
	//// return a byte array
	public static byte[] loadGraph(String filename) throws IOException{
		
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");
		
		StringBuilder ret = new StringBuilder();
		for (int u = 0; u < G.V(); u++)
			for (int v : G.adj(u).keySet()){
			ret.append(u + " " + v +"\n");
			
		}
		
		//
		return ret.toString().getBytes("ASCII");
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws IOException {
		int log2m = 8;
		int size = 10000;
		
//		ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, 0.03, 1, true ) ).immutableView();
//		System.out.println("ErdosRenyiGraph - DONE");
		
		long start = System.currentTimeMillis();
//		ArcListASCIIGraph ag = ArcListASCIIGraph.loadOnce( new FastByteArrayInputStream( "0 2\n0 1\n1 0\n1 2\n2 0\n2 1".getBytes( "ASCII" ) ) );
//		ArcListASCIIGraph ag = ArcListASCIIGraph.loadOnce(new FastByteArrayInputStream(loadGraph("_data/as20graph.gr")) );
//		ArcListASCIIGraph ag = ArcListASCIIGraph.loadOnce(new FastByteArrayInputStream(loadGraph("_data/ca-AstroPh.gr")) );
		ArcListASCIIGraph ag = ArcListASCIIGraph.loadOnce(new FastByteArrayInputStream(loadGraph("_data/com_amazon_ungraph.gr")) );	// log2m = 8; 1GB mem, 27s
//		ArcListASCIIGraph ag = ArcListASCIIGraph.loadOnce(new FastByteArrayInputStream(loadGraph("_data/com_youtube_ungraph.gr")) );	// log2m = 8; 2.2GB mem, 41s
		
		ImmutableGraph g = new ArrayListMutableGraph( ag ).immutableView();
		System.out.println("loadGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		
		start = System.currentTimeMillis();
		int numberOfThreads = 1;
		HyperBall hyperBall = new HyperBall( g, null, log2m, null, numberOfThreads, 10, 10, false, false, false, null, 1);
		hyperBall.init();
		do {
			hyperBall.iterate();
			final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
		} while( hyperBall.modified() != 0 );
		System.out.println("hyperBall - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		
		for (double d : hyperBall.neighbourhoodFunction)
			System.out.println(d);
		
		hyperBall.close();

	}

}
