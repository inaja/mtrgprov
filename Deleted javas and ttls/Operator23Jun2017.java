package utilities;
import miniT.GraphSTOperation;
import miniT.ProvenanceHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import utilities.graph.*;

public class Operator {
	
	private String graphStoresLocal_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/GraphStores";
	
	private SourceGraphInSystem a1, b2;
	private String graphStOpType;
	private String graphSTA1B2_NAME; // the name of set theoretic resulting graph
	private Model graphSTA1B2_MODEL; // the set theoretic resulting graph
	
	private GraphInSystem c3;
	
	private String graphUpdateType;
	
	public Operator (String graphA1_source_URI, String graphA1_source_PROV_URI, String graphB2_source_URI, String graphB2_source_PROV_URI) {
		a1 = new SourceGraphInSystem(graphA1_source_URI, graphA1_source_PROV_URI);
		b2 = new SourceGraphInSystem(graphB2_source_URI, graphB2_source_PROV_URI);
	}
	
	public void loadBothGraphsAndProvs(String graphA1_source_NAME, String graphA1_source_PROV_NAME, String graphB2_source_NAME, String graphB2_source_PROV_NAME) {
		a1.loadGraphAndItsProv(graphA1_source_NAME, graphA1_source_PROV_NAME);
		b2.loadGraphAndItsProv(graphB2_source_NAME, graphB2_source_PROV_NAME);
	}
	
	public void initialiazeC(String graphC3_NAME, String graphC3_PROV_NAME) {
		c3 = new GraphInSystem(graphC3_NAME, graphC3_PROV_NAME);
		c3.setGraph_PROV_MODEL(a1.getGraph_PROV_star_COPY_MODEL().union(b2.getGraph_PROV_star_COPY_MODEL()));
		Utilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/cacheStoreC/" + c3.getGraph_PROV_NAME()+"initial.ttl", "ttl");
	}
	
	public void loadUpdateAndProv(String whichGraph, String updateGraph_source_NAME) {
		
		if (whichGraph.equalsIgnoreCase("A1")) {
			// ignore for the moment
		} else if (whichGraph.equalsIgnoreCase("B2")) {
			b2.loadUpdateAndItsProv(updateGraph_source_NAME);
		}
		else throw new IllegalArgumentException("Incorrect Graph Name In loadUpdateAndProv, expected either \"A1\" or \"B2\"");
	}
	
	public void applyUpdateAndUpdateProv(String whichGraph) {
		/* here you have to check the provenance of the original graph to see what the 
		   set theoretic operation was, and then either apply update or apply part of update	
		*/
		if (whichGraph.equalsIgnoreCase("A1")) {
			//check whether to insert/delete all the model or a subset of it
		} else if (whichGraph.equalsIgnoreCase("B2")) {
			//check whether to insert/delete all the model or a subset of it
			generateWhatIsToBeAppliedAsUpdate(whichGraph);
			// update provenance telling it that some or all the update have been used
			
			if (graphUpdateType.equalsIgnoreCase("insert")) {
				applyInsertUpdateOnC3();
				//update the provenance of C3 - or generate provenance or C'3
				//update the provenance of C3 after insertion
				//update the provenance of C3 after reasoning
			}
			else {
				applyDeleteUpdateOnC3();
				//update the provenance of C3 - or generate provenance or C'3
				//update the provenance of C3 after deletion
				//update the provenance of C3 after reasoning?
			}
			
			//	updateProvOfC3(graphUpdateType, graphStOpType);
				
		}
		else throw new IllegalArgumentException("Incorrect Graph Name In loadUpdateAndProv, expected either \"A1\" or \"B2\"");
	}
	
	private String createDescribeStmtForUpdateTriples (Statement stmt) {
		String tripleString = "<" + stmt.getSubject() + "> "
					 		+ "<" + stmt.getPredicate() + "> ";
		if (stmt.getObject().isLiteral()) {
			tripleString = tripleString + "?z";
		}
		else{
			tripleString = tripleString + "<" + stmt.getObject() + ">";
		}
		String describeStmt = SPARQLUtilities.createDescribeStatement(c3.getGraph_BASE_NAME(), tripleString);
		return describeStmt;
	}
	 
	public void applyInsertUpdateOnC3 () {
		// 1. create insert statement
		Model m;
		if (b2.isUseAllUpdate()) {
			m = b2.getUpdateGraph_COPY_MODEL();
		}
		else {
			m = b2.getUpdateGraphSubset_MODEL();
		}
		String sTriplesToBeInserted = SPARQLUtilities.createTriplesForUpdate(m);
		String updateStmt = SPARQLUtilities.createUpdateStatement(c3.getGraph_BASE_NAME(), sTriplesToBeInserted, "insert");
				
		// 2. send insert - or subset of it - to Fuseki to the base graph
		try {
			SPARQLUtilities.updateGraph(c3.getGraph_BASE_NAME(), updateStmt);
		} catch (IOException e) {
			System.err.println("\n************************************************* \n"
					   + "WARNING, COULD NOT UPLOAD TO FUSEKI .... \n"
					   + e.getMessage() 
					   + "************************************************* \n");
		}
		
		// 3. loop over the triples and get their describe from Fuseki,
		//	  union the received describe		
		StmtIterator it2 = m.listStatements();
		Model mDescribe = ModelFactory.createDefaultModel();
		while (it2.hasNext()) {
			Statement stmt = it2.next();
			String ds = createDescribeStmtForUpdateTriples (stmt);
			mDescribe = mDescribe.union(SPARQLUtilities.describeThatOf(ds));
		}
			
		// 4. re-reason the final describe graph 
		String timeCalled = Utilities.getTime();
		//Model[] results = EntailmentUtilities.getBaseAndEntailments(mDescribe, graphStoresLocal_URI + "/graphStoreC/dredCount1.txt");
		Model results = EntailmentUtilities.getEntailmentsOnly(mDescribe, graphStoresLocal_URI + "/graphStoreC/dredCount1.txt");
		Model oldBASE_AND_NEW_TRIPLES = c3.getGraph_BASE_MODEL().union(results);
		// 5. insert the new triples created by the re-reasoning - or model generated
		//c3.setGraph_BASE_AND_INFS_MODEL(oldBASE_AND_NEW_TRIPLES);
		c3.setGraph_BASE_MODEL(oldBASE_AND_NEW_TRIPLES);
		Utilities.writeModelToFile(oldBASE_AND_NEW_TRIPLES, graphStoresLocal_URI + "/graphStoreC/" + c3.getGraph_BASE_NAME()+ "-" + timeCalled + ".ttl", "ttl");
		try {
			SPARQLUtilities.uploadNewGraph(c3.getGraph_BASE_NAME()+timeCalled, oldBASE_AND_NEW_TRIPLES);
		} catch (IOException e) {
			System.err.println("\n************************************************* \n"
							   + "WARNING, COULD NOT UPLOAD TO FUSEKI .... \n"
							   + e.getMessage()
							   + "************************************************* \n");
		}	
		oldBASE_AND_NEW_TRIPLES.write(System.out);
	}
	
	public void applyDeleteUpdateOnC3() {
		Model mTriplesToBeDeleted;
		if (b2.isUseAllUpdate()) {
			mTriplesToBeDeleted = b2.getUpdateGraph_COPY_MODEL();
		}
		else {
			mTriplesToBeDeleted = b2.getUpdateGraphSubset_MODEL();
		}
		
		// 1. loop over triples (or the subset of the triples) to be deleted and 
		//		send a request for their describe to Fuseki, 
		//		union the received describes 
		StmtIterator itDescribe = mTriplesToBeDeleted.listStatements();
		Model mDescribedGraph = ModelFactory.createDefaultModel();
		while (itDescribe.hasNext()) {
			Statement stmt = itDescribe.next();
			String ds = createDescribeStmtForUpdateTriples (stmt);
			mDescribedGraph = mDescribedGraph.union(SPARQLUtilities.describeThatOf(ds));
		}
		// 2. delete from the final describe graph:
		//	  the triples (or their subset)
		// 	  for each triple, check the property, get its super/sub-property and their subject/object
		//
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

		// then i need to re-derive the base of the triple
		//so get the describe from the base and re-derive and insert the new triples
		// send to Fuseki the delete statement
		// 3. maybe check validity
	}
	
	public void generateWhatIsToBeAppliedAsUpdate(String whichGraph) {
		/* here you have to check the provenance of the original graph to see what the 
		   set theoretic operation was, and then either apply update or apply part of update	
		*/
		Model whatToUseInUpdate = ModelFactory.createDefaultModel();
		if (whichGraph.equalsIgnoreCase("A1")) {
			// do nothing for now
		} 
		else
		if (whichGraph.equalsIgnoreCase("B2")) {
			if (graphStOpType.equalsIgnoreCase("union")) {
				if (graphUpdateType.equalsIgnoreCase("insert")) {
					b2.setUseAllUpdate(true);
				} else if(graphUpdateType.equalsIgnoreCase("delete")) {
					b2.setUseAllUpdate(false);
					whatToUseInUpdate = b2.getUpdateGraph_COPY_MODEL().difference(a1.getGraph_MODEL());
					b2.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
				}
			} else if (graphStOpType.equalsIgnoreCase("intersection")) {
				if (graphUpdateType.equalsIgnoreCase("insert")) {
					b2.setUseAllUpdate(false);
					whatToUseInUpdate = b2.getUpdateGraph_COPY_MODEL().intersection(a1.getGraph_MODEL());
					b2.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
				} else if(graphUpdateType.equalsIgnoreCase("delete")) {
					b2.setUseAllUpdate(true);
				} 
			} else if (graphStOpType.equalsIgnoreCase("difference1")) {
				if (graphUpdateType.equalsIgnoreCase("insert")) {
					b2.setUseAllUpdate(true);					
				} else if(graphUpdateType.equalsIgnoreCase("delete")) {
					b2.setUseAllUpdate(true); //doesn't matter, nothing will happen anyway
				} 
			} else if (graphStOpType.equalsIgnoreCase("difference2")) {
				if (graphUpdateType.equalsIgnoreCase("insert")) {
					b2.setUseAllUpdate(false);
					whatToUseInUpdate = b2.getUpdateGraph_COPY_MODEL().difference(a1.getGraph_MODEL());
					b2.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
				} else if(graphUpdateType.equalsIgnoreCase("delete")) {
					b2.setUseAllUpdate(false);
					whatToUseInUpdate = b2.getUpdateGraph_COPY_MODEL().difference(a1.getGraph_MODEL());
					b2.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
				}
			} // end if difference 2
		}// end if B2
	}

	public void createSTGraph (String graphSTA1B2_NAME) {
		this.graphSTA1B2_NAME = graphSTA1B2_NAME;
		String timeCalled = Utilities.getTime();
		
		String stOpActivityName = ProvenanceHandler.createNameOfSTOp(a1.getGraph_COPY_NAME(), b2.getGraph_COPY_NAME(), graphStOpType, timeCalled);
		
		Model graphStOpModel = GraphSTOperation.applyGraphSt(a1.getGraph_MODEL(), b2.getGraph_MODEL(), graphStOpType);
		graphSTA1B2_MODEL = ModelFactory.createDefaultModel().union(graphStOpModel);
		c3.setGraph_MODEL(ModelFactory.createDefaultModel().union(graphStOpModel));
		ProvenanceHandler.updateC3SetOperation(stOpActivityName, this);
		Utilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/cacheStoreC/" + c3.getGraph_PROV_NAME()+"ST.ttl", "ttl");
	}
	
	public void reasonRDFS(boolean uploadToFuseki) {
		String timeCalled = Utilities.getTime();
		String entailActivityName = ProvenanceHandler.createNameOfEntailOp(c3.getGraph_NAME(), timeCalled);
		
		c3.setGraph_BASE_MODEL(graphSTA1B2_MODEL);
		Model results = EntailmentUtilities.getEntailmentsOnly(graphSTA1B2_MODEL, graphStoresLocal_URI + "/cacheStoreC/derivationCountInitial.txt");
		c3.setGraph_INFS_MODEL(results);
		//c3.setGraph_BASE_AND_INFS_MODEL(results[0]);
		Utilities.writeModelToFile(c3.getGraph_BASE_MODEL(), graphStoresLocal_URI + "/graphStoreC/" + c3.getGraph_BASE_NAME()+"first.ttl", "ttl");
		Utilities.writeModelToFile(c3.getGraph_INFS_MODEL(), graphStoresLocal_URI + "/graphStoreC/" + c3.getGraph_INFS_NAME()+"first.ttl", "ttl");
		//Utilities.writeModelToFile(c3.getGraph_BASE_AND_INFS_MODEL(), graphStoresLocal_URI + "/graphStoreC/" + c3.getGraph_BASE_AND_INFS_NAME()+"first.ttl", "ttl");
		
		ProvenanceHandler.updateC3Entailment(entailActivityName, this);
		Utilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/graphStoreC/provStoreC/" + c3.getGraph_PROV_NAME()+"first.ttl", "ttl");
		
		// uploading to Fuseki
		if (uploadToFuseki) {
			try {
				SPARQLUtilities.uploadNewGraph(a1.getGraph_COPY_NAME(), a1.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(b2.getGraph_COPY_NAME(), b2.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(c3.getGraph_BASE_NAME(), c3.getGraph_BASE_MODEL());
				SPARQLUtilities.uploadNewGraph(c3.getGraph_INFS_NAME(), c3.getGraph_INFS_MODEL());
				//SPARQLUtilities.uploadNewGraph(c3.getGraph_BASE_AND_INFS_NAME(), c3.getGraph_BASE_AND_INFS_MODEL());
			}
			catch (Exception e) {
				System.err.println("\n************************************************* \n"
								   + "WARNING, COULD NOT UPLOAD TO FUSEKI .... \n"
								   + e.getMessage()
								   + "************************************************* \n");
			}
		}
	}
	
	public String getGraphStOpType() {
		return graphStOpType;
	}

	public void setGraphStOpType(String graphStOpType) {
		if (graphStOpType.equalsIgnoreCase("union") 
				|| graphStOpType.equalsIgnoreCase("intersection")
				|| graphStOpType.equalsIgnoreCase("difference1") 
				|| graphStOpType.equalsIgnoreCase("difference2") ) {
			this.graphStOpType = graphStOpType;
		}
		else throw new IllegalArgumentException("Invalid graph ST operation type: " 
												+ graphStOpType 
												+ ", was expecting either union, intersection, difference1, or difference2");
	}
	
	public String getGraphStoresLocal_URI() {
		return graphStoresLocal_URI;
	}

	public void setGraphStoresLocal_URI(String graphStoresLocal_URI) {
		this.graphStoresLocal_URI = graphStoresLocal_URI;
		a1.setGraphStoresLocal_URI(graphStoresLocal_URI);
		b2.setGraphStoresLocal_URI(graphStoresLocal_URI);
	}

	public String getGraphSTA1B2_NAME() {
		return graphSTA1B2_NAME;
	}

	public void setGraphSTA1B2_NAME(String graphSTA1B2_NAME) {
		this.graphSTA1B2_NAME = graphSTA1B2_NAME;
	}

	public Model getGraphSTA1B2_MODEL() {
		return graphSTA1B2_MODEL;
	}

	public void setGraphSTA1B2_MODEL(Model graphSTA1B2_MODEL) {
		this.graphSTA1B2_MODEL = graphSTA1B2_MODEL;
	}
	
	public String getGraphUpdateType() {
		return graphUpdateType;
	}

	public void setGraphUpdateType(String graphUpdateType) {
		if (graphUpdateType.equalsIgnoreCase("insert") || graphUpdateType.equalsIgnoreCase("delete")) {
			this.graphUpdateType = graphUpdateType;
		}
		else throw new IllegalArgumentException("Invalid graph update type: " + graphUpdateType + ", was expecting either insert or delete");
	}
	
	public SourceGraphInSystem getA1() {
		return a1;
	}
	
	public SourceGraphInSystem getB2() {
		return b2;
	}

	public GraphInSystem getC3() {
		return c3;
	}
}
