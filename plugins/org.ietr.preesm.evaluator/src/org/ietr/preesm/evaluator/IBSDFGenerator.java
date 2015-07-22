package org.ietr.preesm.evaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.ietr.dftools.algorithm.exporter.GMLSDFExporter;
import org.ietr.dftools.algorithm.model.sdf.*;
import org.ietr.dftools.algorithm.model.sdf.esdf.*;
import org.ietr.preesm.algorithm.importSdf3Xml.Sdf3XmlParser;

public class IBSDFGenerator {
	
	// Parameters
	public int nbactors;
	
	// Set of graphs
	public ArrayList<SDFGraph> graphSet;
	
	public IBSDFGenerator(int n, int d) {
		graphSet = new ArrayList<SDFGraph>();
		nbactors = n;
	}
	
	
	/**
	 * Generates randomly a set of SDF graphs (with Turbine) which will be used to construct an IBSDF graph
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void graphSet_gen() throws IOException, InterruptedException {
		int remaining_actors = nbactors;
		int randomNum;
		Random rand = new Random();
		Process p;
		p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "rm -f /home/blaunay/Bureau/turbine-master/turbine/IBgen/*"});
		p.waitFor();
		while (remaining_actors > 0) {
			// draw a random number for the graph size (no graph with 1 actor)
		    randomNum = rand.nextInt((nbactors*2/3 -1) +1) +2;
		    if (remaining_actors < randomNum || remaining_actors - randomNum == 1)
		    	randomNum = remaining_actors;
		    remaining_actors -= randomNum;
		    
		    // Generation of the graph
		    p = Runtime.getRuntime().exec(new String[]{
		    		"/home/blaunay/Bureau/turbine-master/turbine/test.py",
		    		Integer.toString(randomNum),Integer.toString(graphSet.size())});
		    p.waitFor();
			
		    // Import it in PREESM
		    File file = new File("/home/blaunay/Bureau/turbine-master/turbine/IBgen/"+String.valueOf(graphSet.size())+".sdf3");
			InputStream iStream = new FileInputStream(file);
			Sdf3XmlParser importer = new Sdf3XmlParser();
			SDFGraph G = importer.parse(iStream); 
			G.setName(String.valueOf(graphSet.size()));
			graphSet.add(G);
		}
	}
	
	
	/**
	 * Pick randomly nb vertices in the graph G
	 * @param G
	 * @param nb
	 * @return list of vertices
	 */
	private ArrayList<SDFAbstractVertex> randomVertices(SDFGraph G, int nb) {
		Random rand = new Random();
		ArrayList<SDFAbstractVertex> vertices = new ArrayList<SDFAbstractVertex>(G.vertexSet());
		ArrayList<SDFAbstractVertex> verticestoreturn = new ArrayList<SDFAbstractVertex>();
		for(int i=0; i<nb; i++) 
			verticestoreturn.add(vertices.get(rand.nextInt((G.vertexSet().size()-1) + 1)));
		
		return verticestoreturn;
	}
	
	
	/**
	 * Insert the graph g in the vertex v by adding corresponding interfaces
	 * and ensuring the liveness of the whole graph
	 * @param g
	 * @param v
	 */
	private void insert(SDFGraph g, SDFAbstractVertex v) {
		v.setGraphDescription(g);
		int nbsources = v.getSources().size();
		int nbsinks = v.getSinks().size();
		// we need nbsources+nbsinks vertices to connect to the interfaces
		ArrayList<SDFAbstractVertex> toconnect = randomVertices(g, nbsources+nbsinks);
		
		// Add the necessary interfaces and edges connecting them to the graph
		for (int i=0; i<nbsources; i++) {
			SDFSourceInterfaceVertex s = new SDFSourceInterfaceVertex();
			s.setName(v.getSources().get(i).getName());
			s.setId(v.getSources().get(i).getId());
			g.addVertex(s);
			v.addSource(s);
			SDFSourceInterfaceVertex inPort = new SDFSourceInterfaceVertex();
			inPort.setName("from"+s.getName());
			toconnect.get(i).addSource(inPort);
			SDFEdge newEdge = g.addEdge(s, toconnect.get(i));
			newEdge.setSourceInterface(s);
			newEdge.setTargetInterface(inPort);
			//TODO put the rates
		}
		for (int i=0; i<nbsinks; i++) {
			SDFSinkInterfaceVertex s = new SDFSinkInterfaceVertex();
			s.setName(v.getSinks().get(i).getName());
			s.setId(v.getSinks().get(i).getId());
			g.addVertex(s);
			v.addSink(s);
			SDFSinkInterfaceVertex outPort = new SDFSinkInterfaceVertex();
			outPort.setName("to"+s.getName());
			toconnect.get(i+nbsources).addSource(outPort);
			SDFEdge newEdge = g.addEdge(toconnect.get(i+nbsources),s);
			newEdge.setSourceInterface(outPort);
			newEdge.setTargetInterface(s);
			//TODO put the rates
		}
		
		//TODO pour chaque interface d'entrée
		// bellman ford retournant longueur et edges du + court chemin
		// pour les interfaces de sortie tq longeur < 0
			// add -longueur tokens sur les arcs du +court chemin de façon random
	}
	
	//rand.nextInt((max - min) + 1) + min
	
	private void hierarchize() throws IOException, InterruptedException {
		int remaining_graphs, current,r;
		Random rand = new Random();
		// graphe de reference
		current = 0;
		remaining_graphs = graphSet.size()-1;
		while (remaining_graphs > 0) {
			// pick r < remaining_graphs
			r = rand.nextInt(remaining_graphs)+1;
			// insert the r next graphs in random vertices of top
			for (int i=1; i<=r; i++) {
				insert(graphSet.get(current+i),randomVertices(graphSet.get(current), 1).get(0));
				//TODO make sure vertex not already picked before
			}
			remaining_graphs -= r;
			// top = premier graphe parmi les r
			current += r; 
		}
		
		for (int i=0; i<graphSet.size(); i++) {
			System.out.println(i);
			if (graphSet.get(i).getParentVertex() != null)
				System.out.println(graphSet.get(i).getName()+" - "+graphSet.get(i).getParentVertex().getBase().getName()+"("+graphSet.get(i).getParentVertex().getName()+")");
		}
		
		// Export the graph
		/*GMLSDFExporter exporter = new GMLSDFExporter();
		Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "rm -f /home/blaunay/Bureau/turbine-master/turbine/IBSDF/*"});
		p.waitFor();
		exporter.export(graphSet.get(0), "/home/blaunay/Bureau/turbine-master/turbine/IBSDF/top.graphml");*/
	}
	
	
	public static void main(String [] args) throws IOException, InterruptedException
	{
		IBSDFGenerator x = new IBSDFGenerator(25, 2);
		x.graphSet_gen();
		x.hierarchize();
	}
}