package miniT;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

//import org.apache.jena.query.QueryExecution;
//import org.apache.jena.query.QueryExecutionFactory;
//import org.apache.jena.query.ResultSet;
//import org.apache.jena.query.ResultSetFormatter;
//import org.apache.jena.rdf.model.InfModel;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.util.FileUtils;
//import org.apache.jena.query.QuerySolution;
//import org.apache.jena.query.ResultSet;
//import org.apache.jena.query.ResultSetFormatter;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import utilities.*;

public class MainRDFAccess {
	
	//private static String datasetNameOnServer = "test2";
	private static String graphA1_source_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/fcSimpsonsFamily.ttl";
	private static String graphA1_source_NAME = "fcSimpsonsFamily.ttl";
	private static String graphA1_source_PROV_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/pa1.ttl";
	private static String graphA1_source_PROV_NAME = "pa1.ttl";
	
	private static String graphB2_source_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/fcBouvier.ttl";
	private static String graphB2_source_NAME = "fcBouvier.ttl";
	private static String graphB2_source_PROV_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/pb2.ttl";
	private static String graphB2_source_PROV_NAME = "pb2.ttl";
	
	private static String graphSTA1B2_NAME = "GopC3.ttl";
	private static String graphStOp = "union";
	
	private static String graphC3_NAME = "C3.ttl";
	private static String graphC3_PROV_NAME= "C3prov.ttl";
	
	private static String updateGraphB2_URI_Insert = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/insert.ttl";
	private static String updateGraphB2_URI_Delete = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/delete.ttl";
	private static String updateGraphB2_source_NAME = "UpB2.ttl";
	private static String graphB2prime_PROV_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/pb2updated.ttl";
	//private static String graphB2prime_PROV_source_NAME = "pb2updated.ttl"; //I am assuming the provenance of the source retains the same name
	private static String updateOpInsert = "insert";
	private static String updateOpDelete = "delete";
	
	private static Operator o;
	private static TraditionalOperator t;
	
	public static void main (String[] args) throws IOException {
		
			
		t = new TraditionalOperator(graphA1_source_URI, "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/fcBouvierUpdated.ttl", graphC3_NAME);
		t.loadBothGraphs(graphA1_source_NAME, graphB2_source_NAME);
		t.setGraphStOpType(graphStOp);
		t.setGraphSTA1B2_NAME(graphSTA1B2_NAME);
		t.createSTGraph(graphSTA1B2_NAME);
		Model cBase = t.getGraphSTA1B2_MODEL();
		//System.out.println("cBase");
		//Utilities.writeModelToScreen(cBase, "Turtle");
		
		Model[] results = t.reasonRDFS(true);
		
		Model cBaseAndInf = results[0];
		Model cInfs =  results[1];
		
		//System.out.println("cBaseAndInfs");
		//Utilities.writeModelToScreen(cBaseAndInf, "Turtle");
		//System.out.println("cInfs");
		//Utilities.writeModelToScreen(cInfs, "Turtle");
		
		// First get the triples to be deleted from the Delete Graph
		Model mTriplesToBeDeleted = Utilities.readModelFromFile(updateGraphB2_URI_Delete);
		// Second get their describe
		String describeGraph = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/testJenaWithFiles/testDeleteFromDescribe.ttl";
		Model mDescribedGraph = Utilities.readModelFromFile(describeGraph);
		
		// Third loop over all the triples to be deleted
		StmtIterator itrDeleteModel = mTriplesToBeDeleted.listStatements();
		Set<String> strBaseTriples = new HashSet<String>();
		Set<String> strInfTriples = new HashSet<String>();
		while (itrDeleteModel.hasNext()) {
			Statement baseTripleToBeDeleted = itrDeleteModel.next();
			// Form the triple to be deleted from base graph
			String strBaseTriple = "<" + baseTripleToBeDeleted.getSubject() + "> " 
								 + "<" + baseTripleToBeDeleted.getPredicate() + "> " 
								 + "<" + baseTripleToBeDeleted.getObject() + ">"; 
			strBaseTriples.add(strBaseTriple);
			
			// Now, onto the inf graph (the triple is not in the inf graph, so no need to delete it)
			// we have to loop over their describe graph from the inf
			// StmtIterator itrTriplesToBeDeletedFromDsrcb = mDescribedGraph.listStatements();
			Resource subjectOfInterest = baseTripleToBeDeleted.getSubject();
			//loop over the triples in the inf graph which share the subject
			Selector selectTriplesOfInterest = new SimpleSelector(subjectOfInterest, null, (RDFNode) null);
			StmtIterator itrTriplesWithSameSubject = mDescribedGraph.listStatements(selectTriplesOfInterest);
			while (itrTriplesWithSameSubject.hasNext()) {
				Statement stmtWithSameSubjct = itrTriplesWithSameSubject.next();
				/*System.out.println(stmtWithSameSubjct.getSubject().toString() + " " 
									+ stmtWithSameSubjct.getPredicate().toString() + " " 
									+ stmtWithSameSubjct.getObject().toString());*/
				// now check the property
				Property currentProperty = stmtWithSameSubjct.getPredicate();
				String strCurrentProperty = currentProperty.toString();
				if (strCurrentProperty.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					// get the statement where the current object is the subject in a triple
					// and the property is subclass of
					Property rdfsSubClassOf =  mDescribedGraph.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf");
					Selector selectTriplesWithObjAsSub = new SimpleSelector( (Resource) stmtWithSameSubjct.getObject(), rdfsSubClassOf, (RDFNode) null);
					// for each of the those objects
					StmtIterator itrTypeOfObject = mDescribedGraph.listStatements(selectTriplesWithObjAsSub);
					//loop and create a delete
					while (itrTypeOfObject.hasNext()) {
						Statement stmt = itrTypeOfObject.next();
						String strRDFType = stmt.getObject().toString();
						strInfTriples.add("<" + stmtWithSameSubjct.getSubject().toString() + "> "
										   + "<" + currentProperty.toString() + "> "
											+ "<" + strRDFType + ">");
					}
					
				}
				else {
						//the property is not rdf:type, i.e. someProperty which is a subproperty/superproperty
						//the loop over the property:
						Property rdfsSubPropertyOf = mDescribedGraph.getProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
						Selector selectTriplesWithProperty = new SimpleSelector((Resource) currentProperty, rdfsSubPropertyOf, (RDFNode) null);
						StmtIterator itrTypeOfProperty = mDescribedGraph.listStatements(selectTriplesWithProperty);
						while(itrTypeOfProperty.hasNext()) {
							// 1. get its superproperty
							Statement stmt = itrTypeOfProperty.next();
							//String strRDFType = stmt.getObject().toString();
							//2. create delete statement for subject current-sub/super-property object
							strInfTriples.add("<" + stmtWithSameSubjct.getSubject().toString() + "> "
									   + "<" + stmt.getObject().toString() + "> "
										+ "<" + stmtWithSameSubjct.getObject().toString() + ">");
						}
				}
				//System.out.println(stmt.toString());
				//String deleteTriple = SPARQLUtilities.createUpdateStatement("cBase.ttl", , "delete");
				//System.out.println(deleteTriple);
			}
			/* 
			because there is no transitivity/symmetry... then do no need to do anything else		
			*/
		}

		System.out.println("Printing out base triples to be deleted:");
		System.out.println(SPARQLUtilities.createMultiUpdateStatements("cBase", strBaseTriples, "delete"));
		System.out.println("Printing out inf triples to be deleted:");
		System.out.println(SPARQLUtilities.createMultiUpdateStatements("cInfs", strInfTriples,"delete"));
		// then i need to re-derive the base of the triple
		/*
		o = new Operator(graphA1_source_URI, graphA1_source_PROV_URI, graphB2_source_URI, graphB2_source_PROV_URI);
		createInitialGraph();
		
		System.out.println("\nNow for the update\n");
		o.getB2().setUpdateGraph_URI(updateGraphB2_URI);
		o.getB2().setGraphprime_PROV_URI(graphB2prime_PROV_URI);
		applyUpdate();
		*/
		//try {
		//	TimeUnit.MINUTES.sleep(1);
		//} catch (InterruptedException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		
		//useTraditional();
		////
		
		
		// send the delete query to Fuseki
		
		System.out.println("Finished at "+ Utilities.getTime());
	}
	
	private static void createInitialGraph() {
		o.loadBothGraphsAndProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);	
		o.initialiazeC(graphC3_NAME, graphC3_PROV_NAME );
		o.setGraphStOpType(graphStOp);
		o.setGraphSTA1B2_NAME(graphSTA1B2_NAME);
		
		o.createSTGraph(graphSTA1B2_NAME);
		
		o.reasonRDFS(true);
	}
	
	private static void applyUpdate() {
		// load the update
		o.loadUpdateAndProv("B2", updateGraphB2_source_NAME);
		o.setGraphUpdateType(updateOpDelete);
		
		// re-entail: if insert then entail the describe of the insert, 
		//if delete, then delete and re-derive
		o.applyUpdateAndUpdateProv("B2");	
	}
	
	private static void useTraditional() {
		System.out.println("Now for the traditional way");
		t = new TraditionalOperator(graphA1_source_URI, graphB2_source_URI, graphC3_NAME);
		t.loadBothGraphs(graphA1_source_NAME, graphB2_source_NAME);
		t.setGraphStOpType(graphStOp);
		t.setGraphSTA1B2_NAME(graphSTA1B2_NAME);
		t.createSTGraph(graphSTA1B2_NAME);
		t.reasonRDFS(true);
		try {
			TimeUnit.SECONDS.sleep(30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphB2_source_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/fcBouvierUpdated.ttl";
		t = new TraditionalOperator(graphA1_source_URI, graphB2_source_URI, graphC3_NAME);
		t.loadBothGraphs(graphA1_source_NAME, graphB2_source_NAME);
		t.setGraphStOpType(graphStOp);
		t.setGraphSTA1B2_NAME(graphSTA1B2_NAME);
		t.createSTGraph(graphSTA1B2_NAME);
		t.reasonRDFS(true);
		
	}
}

// Below are the tests for creating the update statements and sending them to Fuseki
/*
String content = new String(Files.readAllBytes(Paths.get("d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/insert.txt")));
System.out.println(content);
String updateStatement = SPARQLUtilities.createUpdateStatement("C3BaseAndInfs.ttl", content, "insert");
System.out.println(updateStatement);
SPARQLUtilities.updateGraph("C3BaseAndInfs.ttl", updateStatement);
*/
/*
String content = new String(Files.readAllBytes(Paths.get("d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/sourceGraphsAB/delete.txt")));
System.out.println(content);
String updateStatement = SPARQLUtilities.createUpdateStatement("C3BaseAndInfs.ttl", content, "delete");
System.out.println(updateStatement);
SPARQLUtilities.updateGraph("C3BaseAndInfs.ttl", updateStatement);
*/
//SPARQLUtilities.setDATASET(datasetNameOnServer);
//String t = "fcsmps:Homer fc:hasWife  fcsmps:Marge";
//String d = SPARQLUtilities.createDescribeStatement("<http://localhost:3030/test2/data/C3BaseAndInfs.ttl>", t);
//System.out.println(d);
//Model m = SPARQLUtilities.describeThatOf(d);
//Utilities.writeModelToScreen(m, "Turtle");
//////
//System.out.println("Testing uploading of update " + graphName + " " + updateGraphURI);
//String updateStatement =  FileUtils.readWholeFileAsUTF8(updateGraphURI);
//System.out.println(updateStatement);