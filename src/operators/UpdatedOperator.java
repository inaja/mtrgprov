package operators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utilities.*;
import utilities.graph.*;
import miniT.ProvenanceHandler;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class UpdatedOperator 
{
	
	private String graphStoresLocal_URI = Config.LOCAL_URI;
	private String DATASET_ORIGINALS = Config.DATASET_ORIGINALS;

	private String DATASET4MT;
	private String DATASET4COPIES;
	
	private String graphUpdateType;
	private SourceGraphInSystem a1;
	private UpdatedSourceGraphInSystem b2prime;
	
	private String queriedGraphStOpType;
	
	protected String graphSTA1B2prime_NAME; // the name of set theoretic resulting graph
	protected Model graphSTA1B2prime_MODEL; // the set theoretic resulting graph
	protected GraphInSystem c3prime;

	public UpdatedOperator (String graphA1_source_URI, String graphA1_source_PROV_URI,
							String updateGraphB2_InsDel_URI, String graphB2prime_InsDel_PROV_URI) 
	{
		a1 = new SourceGraphInSystem(graphA1_source_URI, graphA1_source_PROV_URI);
		b2prime = new UpdatedSourceGraphInSystem(updateGraphB2_InsDel_URI, graphB2prime_InsDel_PROV_URI);
	}

	
	public void loadUpdateAndProv(String whichGraph, String graphOriginalName, String updateGraph_source_NAME, String graph_source_PROVprime_NAME) throws IOException {
		if (whichGraph.equalsIgnoreCase("A1")) {
			// ignore for the moment
		} else if (whichGraph.equalsIgnoreCase("B2")) {
			b2prime.loadUpdateAndItsProv(DATASET_ORIGINALS, graphOriginalName, updateGraph_source_NAME, graph_source_PROVprime_NAME);
			
			//Utilities.writeModelToFile(b2.getGraph_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + b2.getGraph_COPY_NAME(), "TURTLE");
			//Utilities.writeModelToFile(b2.getGraph_PROV_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + b2.getGraph_PROV_COPY_NAME(), "TURTLE");
			//Utilities.writeModelToFile(b2.getGraphprime_PROV_star_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + b2.getGraphprime_PROV_star_COPY_NAME(), "TURTLE");
		}
		else throw new IllegalArgumentException("Incorrect Graph Name In loadUpdateAndProv, expected either \"A1\" or \"B2\"");
	}
		
	public void initializeCprime (String graphC3_NAME, String graphC3_PROV_NAME, String graphC3prime_NAME) throws IOException 
	{
		c3prime = new GraphInSystem(graphC3_NAME, graphC3_PROV_NAME, graphC3prime_NAME);
		//Get C3 from Fuseki
		Model c3base = SPARQLUtilities.loadGraphFromFuseki(DATASET4MT,c3prime.getGraph_BASE_NAME());
		c3prime.setGraph_BASE_MODEL(c3base);
		Model c3infs = SPARQLUtilities.loadGraphFromFuseki(DATASET4MT,c3prime.getGraph_INFS_NAME());
		c3prime.setGraph_INFS_MODEL(c3infs);
		//Model c3primeModel = c3base.union(c3infs);
		//c3prime.setGraph_BASE_MODEL(c3primeModel);
		
		//Get C3's provenance from Fuseki
		Model c3PreviousProv = SPARQLUtilities.loadGraphFromFuseki(DATASET4MT, graphC3_PROV_NAME);
		c3prime.setGraph_PROV_MODEL(c3PreviousProv);
		
		//check C3's provenance to see which graph operation was applied to create it
		String retreivedOp = SPARQLUtilities.getStOpFromProvGraph(c3PreviousProv, 
							Config.LOCAL_URI + DATASET4MT + "/data/" + c3prime.getGraph_PROV_NAME(), 
							"inaja:" + c3prime.getGraph_NAME());
		setQueriedGraphStOpType(retreivedOp);
		//check C3's provenance to see which other graph was used along with this one in the st op
		
		Model c3PROVAfterloading;
		c3PROVAfterloading = ProvenanceHandler.updateC3ProvAfterLoadingUpdate(c3PreviousProv, c3prime.getGraph_NAME(), b2prime.getGraphOriginal_source_NAME());
		c3prime.setGraph_PROV_MODEL(c3PROVAfterloading);
	}
	
	public void applyUpdateAndUpdateProv(String whichGraph, String graphSTA1B2_Update_Name) throws Exception 
	{	
		this.graphSTA1B2prime_NAME = graphSTA1B2_Update_Name;
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
	
	
	public void applyInsertUpdateOnC3 () throws Exception 
	{
		// 1. create insert statement
		String timeCalled [] = Utilities.getTime();
		Model m;
		if (b2prime.isUseAllUpdate()) 
			m = b2prime.getUpdateGraph_MODEL();
		else 
			m = b2prime.getUpdateGraphSubset_MODEL();
		
		List <Statement> notToBeInserted = new ArrayList<Statement>();
		StmtIterator itStmt = m.listStatements();
		while (itStmt.hasNext()) {
			Statement currentTriple = itStmt.next();
			//check if object is literal
			String predicate;
			if (currentTriple.getObject().isLiteral()) {
				predicate = "\"" + currentTriple.getObject().toString() + "\"";
			}
			else {
				predicate = "<" + currentTriple.getObject().toString() + "> ";
			}
			String triple = //currentTriple.getSubject().getNameSpace()+ currentTriple.getSubject().getLocalName() + " "
							//currentTriple.getSubject().getLocalName() + " "
							"<" + currentTriple.getSubject().toString()+ "> " +
							"<" + currentTriple.getPredicate().toString() + "> " +
							predicate;
			//System.out.println(triple);
			if (SPARQLUtilities.checkIfTripleIsInInfGraph(c3prime.getGraph_INFS_MODEL(), triple)) {
				System.out.println("Won't be inserting: " + triple);
				notToBeInserted.add(currentTriple);			
			}
		}
		m.remove(notToBeInserted);
		
		c3prime.setGraph_BASE_MODEL(c3prime.getGraph_BASE_MODEL().union(m));
		Utilities.writeModelToFile(c3prime.getGraph_BASE_MODEL(), graphStoresLocal_URI + "/graphStoreC/nontraditional/" + c3prime.getWithoutTTL_Graph_BASE_NAME() + "AFTER_INSERT-" + timeCalled[1] + ".ttl", "ttl");
		
		String sTriplesToBeInserted = SPARQLUtilities.createTriplesForUpdate(m);
		String updateStmt = SPARQLUtilities.createUpdateStatement(DATASET4MT, c3prime.getGraph_BASE_NAME(), sTriplesToBeInserted, "insert");
				
		// 2. send insert - or subset of it - to Fuseki to the base graph
		SPARQLUtilities.copyGraphOnFuseki(DATASET4MT, DATASET4COPIES, c3prime.getGraph_BASE_NAME());
		SPARQLUtilities.updateGraph(DATASET4MT, c3prime.getGraph_BASE_NAME(), updateStmt);
			
		// 3. loop over the triples and get their describe from Fuseki,
		//	  union the received describe		
		StmtIterator it2 = m.listStatements();
		Model mDescribe = ModelFactory.createDefaultModel();
		
		while (it2.hasNext()) {
			Statement stmt = it2.next();
			String ds = SPARQLUtilities.createDescribeStmtForUpdateTriples(DATASET4MT, c3prime.getGraph_BASE_NAME(), stmt);
			mDescribe = mDescribe.union(SPARQLUtilities.describeThatOf(DATASET4MT, ds));
		}
			
		// 4. re-reason the final describe graph 
		timeCalled = Utilities.getTime();
		Model results = EntailmentUtilities.getEntailmentsOnly(mDescribe, graphStoresLocal_URI + "/graphStoreC/nontraditional/dredCountInsert" + queriedGraphStOpType + timeCalled [1]+ ".txt");
		//Model oldBASE_AND_NEW_TRIPLES = c3.getGraph_BASE_MODEL().union(results);
		Model oldInfs_AND_NEW_Infs = c3prime.getGraph_INFS_MODEL().union(results);
		// 5. insert the new triples created by the re-reasoning - or model generated
		//c3.setGraph_BASE_AND_INFS_MODEL(oldBASE_AND_NEW_TRIPLES);
		c3prime.setGraph_INFS_MODEL(oldInfs_AND_NEW_Infs);
		Utilities.writeModelToFile(oldInfs_AND_NEW_Infs, graphStoresLocal_URI + "/graphStoreC/nontraditional/" + c3prime.getWithoutTTL_Graph_INFS_NAME() + "AFTER_INSERT-" + timeCalled[1] + ".ttl", "ttl");
		try {
			SPARQLUtilities.copyGraphOnFuseki(DATASET4MT, DATASET4COPIES, c3prime.getGraph_INFS_NAME());
			SPARQLUtilities.uploadNewGraph(DATASET4MT, c3prime.getGraph_INFS_NAME(), oldInfs_AND_NEW_Infs);
		} catch (IOException e) {
			System.err.println("\n************************************************* \n"
							   + "ERROR from Method applyInsertUpdateOnC3 in Class Operator,\n"
							   + "COULD NOT UPLOAD TO FUSEKI, intended graph" +  c3prime.getGraph_INFS_NAME()+timeCalled [1]+"\n"
							   + e.getMessage()
							   + "************************************************* \n");
		}	
		
		//oldBASE_AND_NEW_TRIPLES.write(System.out);
	}
	
	public void applyDeleteUpdateOnC3() throws Exception 
	{
		Model mTriplesToBeDeleted;
		if (b2prime.isUseAllUpdate()) 
			mTriplesToBeDeleted = b2prime.getUpdateGraph_MODEL();
		else 
			mTriplesToBeDeleted = b2prime.getUpdateGraphSubset_MODEL();
		
		//loop over the triples to be deleted, if they are inferred, remove them from the
		// list of triples to be deleted
		
		List <Statement> notToBeDeleted = new ArrayList<Statement>();
		StmtIterator itStmt = mTriplesToBeDeleted.listStatements();
		while (itStmt.hasNext()) {
			Statement currentTriple = itStmt.next();
			String predicate;
			if (currentTriple.getObject().isLiteral()) {
				predicate = "\"" + currentTriple.getObject().toString() + "\"";
			}
			else {
				predicate = "<" + currentTriple.getObject().toString() + "> ";
			}
			String triple = //currentTriple.getSubject().getNameSpace()+ currentTriple.getSubject().getLocalName() + " "
							//currentTriple.getSubject().getLocalName() + " "
							"<" + currentTriple.getSubject().toString()+ "> " +
							"<" + currentTriple.getPredicate().toString() + "> " +
							predicate;
			System.out.println(triple);
			if (SPARQLUtilities.checkIfTripleIsInInfGraph(c3prime.getGraph_INFS_MODEL(), triple)) {
				System.out.println("Won't be deleting: " + triple);
				notToBeDeleted.add(currentTriple);			
			}
		}mTriplesToBeDeleted.remove(notToBeDeleted);	
		
		// 1. loop over triples (or the subset of the triples) to be deleted and 
		//		send a request for their describe to Fuseki, 
		//		union the received describes 
		StmtIterator itDescribe = mTriplesToBeDeleted.listStatements();
		Model mDescribedGraph = ModelFactory.createDefaultModel();
		while (itDescribe.hasNext()) {
			Statement stmt = itDescribe.next();
			String ds = SPARQLUtilities.createDescribeStmtForUpdateTriples(DATASET4MT, c3prime.getGraph_INFS_NAME(), stmt);
			mDescribedGraph = mDescribedGraph.union(SPARQLUtilities.describeThatOf(DATASET4MT, ds));
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
								 + "<" + baseTripleToBeDeleted.getPredicate() + "> ";
			if (baseTripleToBeDeleted.getObject().isLiteral()) {
				strBaseTriple += "\"" + baseTripleToBeDeleted.getObject() + "\"";
			} else {
				strBaseTriple += "<" + baseTripleToBeDeleted.getObject() + ">";
			}
			strBaseTriples.add(strBaseTriple); // i think this is redundant
			
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
							// 1. get its super properties
							Statement stmt = itrTypeOfProperty.next();
							//String strRDFType = stmt.getObject().toString();
							//2. create delete statement for subject current-sub/super-property object
							strInfTriples.add("<" + stmtWithSameSubjct.getSubject().toString() + "> "
									   + "<" + stmt.getObject().toString() + "> "
										+ "<" + stmtWithSameSubjct.getObject().toString() + ">");
						}
				}		
			}
			/* 
			because there is no transitivity/symmetry... then do no need to do anything else		
			*/
		}
		String strDeleteBaseTriples = SPARQLUtilities.createMultiUpdateStatements(DATASET4MT, c3prime.getGraph_BASE_NAME(), strBaseTriples, "delete");
		String strDeleteInfTriples = SPARQLUtilities.createMultiUpdateStatements(DATASET4MT, c3prime.getGraph_INFS_NAME(), strInfTriples, "delete");
		SPARQLUtilities.copyGraphOnFuseki(DATASET4MT, DATASET4COPIES, c3prime.getGraph_BASE_NAME());
		SPARQLUtilities.copyGraphOnFuseki(DATASET4MT, DATASET4COPIES,c3prime.getGraph_INFS_NAME());
		SPARQLUtilities.updateGraph(DATASET4MT, c3prime.getGraph_BASE_NAME(), strDeleteBaseTriples);
		SPARQLUtilities.updateGraph(DATASET4MT, c3prime.getGraph_INFS_NAME(), strDeleteInfTriples);

		// then i need to re-derive the base of the triple
		// so get the describe from the base and re-derive and insert the new triples
		StmtIterator itSubjects = mTriplesToBeDeleted.listStatements();
		Model mDescribeSubjectsAndProperties = ModelFactory.createDefaultModel();
		while(itSubjects.hasNext()){
			Statement stmt = itSubjects.next();
			String subject = stmt.getSubject().toString(); 
			String ds = SPARQLUtilities.createDescribeStmtForUpdatedSubjects(DATASET4MT, c3prime.getGraph_BASE_NAME(), subject);
			mDescribeSubjectsAndProperties = mDescribeSubjectsAndProperties.union(SPARQLUtilities.describeThatOf(DATASET4MT, ds));
		}
		// Trying to only get the describe of the subject results in some missing inferences
		// so  I also need to get the describe of the properties of the base
		StmtIterator itProperties = mDescribeSubjectsAndProperties.listStatements();
		while(itProperties.hasNext()){
			Statement stmt = itProperties.next();
			String property = stmt.getPredicate().toString(); 
			String ds = SPARQLUtilities.createDescribeStmtForUpdatedSubjects(DATASET4MT, c3prime.getGraph_BASE_NAME(), property);
			mDescribeSubjectsAndProperties = mDescribeSubjectsAndProperties.union(SPARQLUtilities.describeThatOf(DATASET4MT, ds));
		}
		
		String [] timeCalled = Utilities.getTime();
		Model results = EntailmentUtilities.getEntailmentsOnly(mDescribeSubjectsAndProperties, graphStoresLocal_URI + "/graphStoreC/nontraditional/dredCountDelete" + queriedGraphStOpType + timeCalled [1]+ ".txt");
		//Utilities.writeModelToFile(c3prime.getGraph_BASE_MODEL(), graphStoresLocal_URI + "/graphStoreC/nontraditional/" + c3prime.getWithoutTTL_Graph_BASE_NAME() + "AFTER_DELETE-" + timeCalled[1] + ".ttl", "ttl");
		//Utilities.writeModelToFile(c3prime.getGraph_INFS_MODEL(), graphStoresLocal_URI + "/graphStoreC/nontraditional/" + c3prime.getWithoutTTL_Graph_INFS_NAME() + "AFTER_DELETE-" + timeCalled[1] + ".ttl", "ttl");
		String triplesToBeInserted = SPARQLUtilities.createTriplesForUpdate(results);
		String insertQuery = SPARQLUtilities.createUpdateStatement(DATASET4MT, c3prime.getGraph_INFS_NAME(), triplesToBeInserted, "insert");
		try {
			SPARQLUtilities.updateGraph(DATASET4MT, c3prime.getGraph_INFS_NAME(), insertQuery);
		} catch (IOException e) {
			System.err.println("\n************************************************* \n"
							   + "ERROR from Method applyInsertUpdateOnC3 in Class Operator,\n"
							   + "COULD NOT UPLOAD TO FUSEKI, intended graph" +  c3prime.getGraph_INFS_NAME()+timeCalled[1] +"\n"
							   + e.getMessage()
							   + "************************************************* \n");
		}
		// 3. maybe check validity
	}
	
	public void generateWhatIsToBeAppliedAsUpdate(String whichGraph) throws IOException 
	{
		Model whatToUseInUpdate = ModelFactory.createDefaultModel();
		String timeCalled = Utilities.getTime()[1];
		if (whichGraph.equalsIgnoreCase("A1")) {
			// we only care about this in difference operations and in chain of updates
		} 
		else
		if (whichGraph.equalsIgnoreCase("B2")) 
		{
			//now calculate the subset or return the full set 
			if (queriedGraphStOpType.equalsIgnoreCase("union")) 
			{
				if (graphUpdateType.equalsIgnoreCase("insert")) {
					b2prime.setUseAllUpdate(true);
					ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
				} 
				else if(graphUpdateType.equalsIgnoreCase("delete")) 
				{
					b2prime.setUseAllUpdate(false);
					//load a1, if from its source use
					//a1.loadGraphAndItsProv(DATASET_ORIGINALS, "A1SourceName.ttl", "A1SourceProvName.ttl");
					// if from internal graph store:
					a1.loadGraphOnly(DATASET_ORIGINALS, a1.getGraph_source_URI());
					
					whatToUseInUpdate = b2prime.getUpdateGraph_MODEL().difference(a1.getGraph_MODEL());
					b2prime.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
					ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
				}
			} 
			else if (queriedGraphStOpType.equalsIgnoreCase("intersection")) 
			{
				if (graphUpdateType.equalsIgnoreCase("insert")) 
				{
					b2prime.setUseAllUpdate(false);
					//load a1, if from its source use
					//a1.loadGraphAndItsProv(DATASET_ORIGINALS, "A1SourceName.ttl", "A1SourceProvName.ttl");
					// if from internal graph store:
					a1.loadGraphOnly(DATASET_ORIGINALS, a1.getGraph_source_URI());
					
					whatToUseInUpdate = b2prime.getUpdateGraph_MODEL().intersection(a1.getGraph_MODEL());
					b2prime.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
					ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
				} 
				else if(graphUpdateType.equalsIgnoreCase("delete")) 
				{
					b2prime.setUseAllUpdate(true);
					ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
				} 
			} 
			else if (queriedGraphStOpType.equalsIgnoreCase("difference")) 
			{
				// check whether it is difference 1 or difference 2 by checking if B is the Subtrahend
				String subtrahend = SPARQLUtilities.getHasSubtrahend(c3prime.getGraph_PROV_MODEL(), 
						Config.LOCAL_URI + DATASET4MT + "/data/" + c3prime.getGraph_PROV_NAME(),
						"inaja:" + c3prime.getGraph_NAME());
				if (subtrahend.equalsIgnoreCase("Gcopy_" + Config.graphB2_source_NAME)) {
					setQueriedGraphStOpType("difference1");
				} else if (subtrahend.equalsIgnoreCase("Gcopy_" + Config.graphA1_source_NAME)) {
					setQueriedGraphStOpType("difference2");
				}
				// now that is sorted, go into getting the update
				if (queriedGraphStOpType.equalsIgnoreCase("difference1")) 
				{
					if (graphUpdateType.equalsIgnoreCase("insert")) {
						b2prime.setUseAllUpdate(true);	
						setGraphUpdateType("delete");
						ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
					} 
					else if(graphUpdateType.equalsIgnoreCase("delete")) 
					{
						b2prime.setUseAllUpdate(false);
						//load a1, if from its source use
						//a1.loadGraphAndItsProv(DATASET_ORIGINALS, "A1SourceName.ttl", "A1SourceProvName.ttl");
						// if from internal graph store:
						a1.loadGraphOnly(DATASET_ORIGINALS, a1.getGraph_source_URI());
						
						whatToUseInUpdate = b2prime.getUpdateGraph_MODEL().intersection(a1.getGraph_MODEL());
						b2prime.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
						setGraphUpdateType("insert");
						ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
					} 
				} 
				else if (queriedGraphStOpType.equalsIgnoreCase("difference2")) 
				{
					if (graphUpdateType.equalsIgnoreCase("insert")) 
					{
						b2prime.setUseAllUpdate(false);
						//load a1, if from its source use
						//a1.loadGraphAndItsProv(DATASET_ORIGINALS, "A1SourceName.ttl", "A1SourceProvName.ttl");
						// if from internal graph store:
						a1.loadGraphOnly(DATASET_ORIGINALS, a1.getGraph_source_URI());
						
						whatToUseInUpdate = b2prime.getUpdateGraph_MODEL().difference(a1.getGraph_MODEL());
						b2prime.setUpdateGraphSubset_MODEL(whatToUseInUpdate);
						ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
					} 
					else if(graphUpdateType.equalsIgnoreCase("delete")) {
						b2prime.setUseAllUpdate(true);
						ProvenanceHandler.updateC3ProvSetOperation(this, timeCalled);
					}
				} // end if difference 2
			}
			
		}// end if B2
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

	
	public String getGraphStoresLocal_URI() {
		return graphStoresLocal_URI;
	}

	public void setGraphStoresLocal_URI(String graphStoresLocal_URI) {
		this.graphStoresLocal_URI = graphStoresLocal_URI;
	}

	public String getDATASET_ORIGINALS() {
		return DATASET_ORIGINALS;
	}

	public void setDATASET_ORIGINALS(String dATASET_ORIGINALS) {
		DATASET_ORIGINALS = dATASET_ORIGINALS;
	}

	public String getDATASET4MT() {
		return DATASET4MT;
	}

	public void setDATASET4MT(String dATASET4MT) {
		DATASET4MT = dATASET4MT;
	}

	public String getDATASET4COPIES() {
		return DATASET4COPIES;
	}

	public void setDATASET4COPIES(String dATASET4COPIES) {
		DATASET4COPIES = dATASET4COPIES;
	}

	public SourceGraphInSystem getA1() {
		return a1;
	}

	public void setA1(SourceGraphInSystem a1) {
		this.a1 = a1;
	}

	public UpdatedSourceGraphInSystem getB2prime() {
		return b2prime;
	}

	public void setB2prime(UpdatedSourceGraphInSystem b2prime) {
		this.b2prime = b2prime;
	}

	public String getQueriedGraphStOpType() {
		return queriedGraphStOpType;
	}

	public void setQueriedGraphStOpType(String queriedGraphStOpType) {
		this.queriedGraphStOpType = queriedGraphStOpType;
	}

	public String getGraphSTA1B2prime_NAME() {
		return graphSTA1B2prime_NAME;
	}

	public void setGraphSTA1B2prime_NAME(String graphSTA1B2prime_NAME) {
		this.graphSTA1B2prime_NAME = graphSTA1B2prime_NAME;
	}

	public Model getGraphSTA1B2prime_MODEL() {
		return graphSTA1B2prime_MODEL;
	}

	public void setGraphSTA1B2prime_MODEL(Model graphSTA1B2prime_MODEL) {
		this.graphSTA1B2prime_MODEL = graphSTA1B2prime_MODEL;
	}

	public GraphInSystem getC3prime() {
		return c3prime;
	}

	public void setC3prime(GraphInSystem c3prime) {
		this.c3prime = c3prime;
	}
	
	public void setDATASETS(String graphStOpType)
	{
		if (graphStOpType.equalsIgnoreCase("union")) {
			DATASET4MT = Config.DATASET_UNION;
			DATASET4COPIES = Config.DATASET4COPIES_UNION;
		} else if (graphStOpType.equalsIgnoreCase("intersection")) {
			DATASET4MT = Config.DATASET_INTERSECTION;
			DATASET4COPIES = Config.DATASET4COPIES_INTERSECTION;
		} else if (graphStOpType.equalsIgnoreCase("difference1")) {
			DATASET4MT = Config.DATASET_DIFFERENCE1;
			DATASET4COPIES = Config.DATASET4COPIES_DIFFERENCE1;
		} else if (graphStOpType.equalsIgnoreCase("difference2")) {
			DATASET4MT = Config.DATASET_DIFFERENCE2;
			DATASET4COPIES = Config.DATASET4COPIES_DIFFERENCE2;
		} else throw new IllegalArgumentException("Invalid graph ST operation type: " 
				+ graphStOpType 
				+ ", was expecting either union, intersection, difference1, or difference2");
	}
	
}
