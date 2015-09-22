package dp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import algs4.EdgeInt;
import algs4.EdgeIntGraph;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

import dp.mcmc.Dendrogram;
import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;
import grph.*;
import grph.algo.AdjacencyMatrix;
import grph.in_memory.*;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import grph.io.GraphBuildException;
import grph.io.GrphTextReader;
import grph.io.ParseException;

public class Test {
	////
	public static void testGrph(){
//		Grph g = new InMemoryGrph(); 
//		g.grid(4, 4);
//		RegularFile f = new RegularFile("_data/myfile.txt"); 
//		try {
//			f.setContent(g.toGrphText().getBytes());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// read text file
		GrphTextReader reader = new GrphTextReader();
		Grph g;
		try {
			RegularFile f = new RegularFile("_data/polbooks.grph");
//			RegularFile f = new RegularFile("_data/as20graph.grph");
			
			g = reader.readGraph(f);
			
			System.out.println("#nodes = " + g.getNumberOfVertices());
			System.out.println("#edges = " + g.getNumberOfEdges());
			
//			for (int v : g.getVertices().toIntArray()){
//				System.out.println(v + ":" + g.getOutVertexDegree(v));
//			}
//			
//			for (VertexPair p : g.getEdgePairs()){
//				System.out.println(p.first + " " + p.second);
//			}
//			System.out.println("DONE");
//			
//			IntSet s = g.getEdgesIncidentTo(1);		// list of edge ids, NOT neighbor vertices !
//			System.out.println(s.contains(4));
//			System.out.println(s.contains(6));
//			System.out.println(s.contains(3));
//			for (IntCursor v:s)
//				System.out.print(v.value + " ");
//			System.out.println();
//			// adjacency matrix
//			AdjacencyMatrix A = g.getAdjacencyMatrix();
//			System.out.println(A.get(6, 4));
			
			
//			g.removeEdge(0);		// remove by edge id: OK
//			g.removeEdge(0, 1);		// Exception !
			g.removeEdge(g.getEdgesConnecting(0, 1).toIntArray()[0]);	// OK but verbose !
			System.out.println("#nodes = " + g.getNumberOfVertices());
			System.out.println("#edges = " + g.getNumberOfEdges());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//// 
	public static void testJMatIO() throws IOException{
		// LAPLACE
		double eps = 1.0;
		double[] lap = new double[10000];
		for(int i = 0; i < lap.length; i++){
			lap[i] = DPUtil.laplaceMechanism(eps);
//			System.out.print(a[i] + " ");
		}
		// GEOMETRIC
		double alpha = Math.exp(-eps);
		int[] geo = new int[10000];
		for(int i = 0; i < geo.length; i++){
			geo[i] = DPUtil.geometricMechanism(alpha);
		}
		
		//
		MLDouble lapArr = new MLDouble("lapArr", lap, 1);
		MLInt32 geoArr = new MLInt32("geoArr", geo, 1);
		String fileName = "lapgeoArr.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
//        towrite.add(lapArr); 
        towrite.add(geoArr);
        
        new MatFileWriter(fileName, towrite );
        System.out.println("Written to file.");
	}
	
	////
	public static void updatePrimitives(Integer s){
		s = s * 2;
	}
	
	
	///////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		
		//
//		testGrph();
		
		//
//		int i = 4/2;
//		System.out.println(i);
//		int[] a = new int[]{0,1,2,3,4,5,6,7,8,9};
//		int[] b = Arrays.copyOfRange(a, 0, 5);
//		System.out.println(b.length);
//		System.out.println();
		
		//
//		Random random = new Random();
//		for (int i = 0; i < 20; i++)
//			System.out.println(random.nextDouble());
		
		// testJMatIO
//		testJMatIO();
		
		
		// TEST Dendrogram.generateSanitizedSample()
//		EdgeListReader reader = new EdgeListReader();
//		EdgeListWriter writer = new EdgeListWriter();
//		Grph G;
//		RegularFile f = new RegularFile("_data/ca-AstroPh.gr");
//		
//		G = reader.readGraph(f);
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		Dendrogram T = new Dendrogram();
//		T.readInternalNodes(G, "_out/ca-AstroPh_hrg.0");		// as20graph: 271ms,  ca-AstroPh_hrg: 2.6s
//		
//		long start = System.currentTimeMillis();
//		Grph aG = T.generateSanitizedSample(G.getNumberOfVertices(), false);
//		System.out.println("generateSanitizedSample - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + aG.getNumberOfVertices());
//		System.out.println("#edges = " + aG.getNumberOfEdges());
		
		////
//		Integer s = 3;
//		System.out.println("Before: s = " + s);
//		updatePrimitives(s);
//		System.out.println("After: s = " + s);
		
		////
//		IntSet T = new IntHashSet();
//		int n = 10000;
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < n; i++)
//			T.add(i);
//		System.out.println("init - elapsed " + (System.currentTimeMillis() - start));
		
		////
		Random random = new Random();
		for (int i = 0; i < 50; i++)
			System.out.print(random.nextInt(2) + " ");
	}

}
