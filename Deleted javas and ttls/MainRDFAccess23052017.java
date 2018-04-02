package miniT;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;

import utilities.*;
/***** REMOVE THIS ******/
import testJena.TestJena;
/************************/
public class MainRDFAccess13052017 {
	
	
	private static String graphA1_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/fcSimpsonsFamily.ttl";
	private static String graphA1_PROV_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/pa1.ttl";
	private static String graphA1_COPY_NAME = "fcSimpsonsFamilyCopy.ttl";
	private static String graphB2_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/fcBouvier.ttl";
	private static String graphB2_PROV_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/pb2.ttl";
	private static String graphB2_COPY_NAME = "fcBouvierCopy.ttl";
	
	private static Producer p = new Producer (graphA1_URI, graphA1_COPY_NAME, graphB2_URI, graphB2_COPY_NAME);
	private static String graphSTA1B2_NAME = "GopC3.ttl";
	
	private static String graphStOp = "union";
	
	private static String graphB2Update_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/insert.txt";
	
	public static void main (String[] args) throws IOException {
			
		p.setGraphA1_URI(graphA1_URI); p.setGraphA1_PROV_URI(graphA1_PROV_URI);
		p.setGraphB2_URI(graphB2_URI);  p.setGraphB2_PROV_URI(graphB2_PROV_URI);	
		p.setGraphStOpType(graphStOp);
		p.setGraphSTA1B2_NAME(graphSTA1B2_NAME);
		
		createInitialGraph();
		applyInsertUpdate( p.getGraphC3_BASE_NAME(), graphB2Update_URI, "insert");
		
		
		//System.out.println("\nNow for the update\n");
		//p.setGraphB2Update_URI(graphB2Update_URI);
		//applyInsertUpdate( p.getGraphC3_BASE_NAME(), graphB2Update_NAME, "insert");
		//System.out.println();
		//Utilities.writeModelToScreen(p.getGraphC3_PROV_MODEL(), "Turtle");
				
	}
	
	private static void createInitialGraph() {
		p.loadGraphA1AndProv();// this includes getting the provenance
		System.out.println("Updated provenance of A1");
		Utilities.writeModelToScreen(p.getGraphA1_PROV_MODEL(), "Turtle");
		
		p.loadGraphB2AndProv();// this includes getting the provenance
		System.out.println("Updated provenance of B2");
		Utilities.writeModelToScreen(p.getGraphB2_PROV_MODEL(), "Turtle");
		
		p.setGraphC3_PROV_MODEL(p.getGraphA1_PROV_MODEL().union(p.getGraphB2_PROV_MODEL()));
		System.out.println("Initial provenance of C3");
		Utilities.writeModelToScreen(p.getGraphC3_PROV_MODEL(), "Turtle");
		
		p.createSTGraph();
		p.reasonRDFS();
		
		System.out.println();
		Utilities.writeModelToScreen(p.getGraphC3_PROV_MODEL(), "Turtle");
		
		//SPARQLUtilities.uploadNewGraph(p.getDATASET_NAME(), p.getGraphC3_BASE_NAME(), p.getGraphC3_BASE_MODEL());
		//SPARQLUtilities.uploadNewGraph(p.getDATASET_NAME(), p.getGraphC3_INFS_NAME(), p.getGraphC3_INFS_MODEL());
		//SPARQLUtilities.uploadNewGraph(p.getDATASET_NAME(), p.getGraphC3_BASE_AND_INFS_NAME(), p.getGraphC3_BASE_AND_INFS_MODEL());
	}
		
	private static void applyInsertUpdate (String graphName, String updateGraphURI, String updateOp) throws IOException {
		// load the update
		//String graphB2Update_COPY_NAME;
		//Model graphB2Update_MODEL;
		//String graphUpdateSubset_NAME;
		//Model graphUpdateSubset_MODEL;
		
		p.setGraphB2_URI(updateGraphURI);
		
		p.loadUpdateAndProv();
		
		//System.out.println("Testing uploading of update " + graphName + " " + updateGraphURI);
		//String updateStatement =  FileUtils.readWholeFileAsUTF8(updateGraphURI);
		//System.out.println(updateStatement);
		/* here you have to check the provenance of the original graph to see what the 
		   set theoretic operation was, and then either apply update or apply part of update	
		*/
		//SPARQLUtilities.updateGraph(p.getDATASET_NAME(), graphName, updateStatement);
		// re-entail: if insert then entail the describe of the insert, if delete, then delete and rederive
				
	}
}
