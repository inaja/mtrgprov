package operators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.RDF;

public class UpdatedOperator 
{
	private String graphStoresLocal_URI = Constants.LOCAL_URI;
	private String DATASET_ORIGINALS = Constants.DATASET_ORIGINALS;

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

	
	public void loadUpdateAndProv(String whichGraph, String graphOriginalName, String updateGraph_source_NAME, String graph_source_PROVprime_NAME) throws IOException 
	{
		if (whichGraph.equalsIgnoreCase("A1")) {
			// ignore for the moment
		} else if (whichGraph.equalsIgnoreCase("B2")) {
			b2prime.loadUpdateAndItsProv(DATASET_ORIGINALS, graphOriginalName, updateGraph_source_NAME, graph_source_PROVprime_NAME);
			
			//Utilities.writeModelToFile(b2.getGraph_MODEL(), graphStoresLocal_URI + graphStoreCNonT + b2.getGraph_COPY_NAME(), "TURTLE");
			//Utilities.writeModelToFile(b2.getGraph_PROV_COPY_MODEL(), graphStoresLocal_URI + graphStoreCNonT + b2.getGraph_PROV_COPY_NAME(), "TURTLE");
			//Utilities.writeModelToFile(b2.getGraphprime_PROV_star_COPY_MODEL(), graphStoresLocal_URI + graphStoreCNonT+ b2.getGraphprime_PROV_star_COPY_NAME(), "TURTLE");
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
				
		//Get C3's provenance from Fuseki
		Model c3PreviousProv = SPARQLUtilities.loadGraphFromFuseki(DATASET4MT, graphC3_PROV_NAME);
		c3prime.setGraph_PROV_MODEL(c3PreviousProv);
		
		//check C3's provenance to see which graph operation was applied to create it
		String retreivedOp = SPARQLUtilities.getStOpFromProvGraph(c3PreviousProv, 
							Constants.tripleStoreURI + DATASET4MT + "/data/" + c3prime.getGraph_PROV_NAME(), 
							Constants.customPrefix + ":" + c3prime.getGraph_NAME());
		setQueriedGraphStOpType(retreivedOp);
		//check C3's provenance to see which other graph was used along with this one in the set-theoretic operation
		
		Model c3PROVAfterloading = ProvenanceHandler.updateC3ProvAfterLoadingUpdate(c3PreviousProv, c3prime.getGraph_NAME(), b2prime.getGraphOriginal_source_NAME());
		c3prime.setGraph_PROV_MODEL(c3PROVAfterloading);
	}
	
	public void applyUpdateAndUpdateProv(String whichGraph, String graphSTA1B2_Update_Name) throws Exception 
	{	
		this.graphSTA1B2prime_NAME = graphSTA1B2_Update_Name;
		if (whichGraph.equalsIgnoreCase("A1")) {
			//check whether to insert/delete all the model or a subset of it
		} 
		else if (whichGraph.equalsIgnoreCase("B2")) 
		{
			//check whether to insert/delete all the model or a subset of it
			generateWhatIsToBeAppliedAsUpdate(whichGraph);
			
			if (graphUpdateType.equalsIgnoreCase("insert")) {
				applyInsertUpdateOnC3();
			}
			else {
				applyDeleteUpdateOnC3();
			}
			
		}
		else throw new IllegalArgumentException("Incorrect Graph Name In loadUpdateAndProv, expected either \"A1\" or \"B2\"");
	}
	
	
	public void applyInsertUpdateOnC3 () throws Exception 
	{
		// 1. create insert statement
		String timeCalled [] = MiscUtilities.getTime();
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
				//System.out.println("Won't be inserting: " + triple);
				notToBeInserted.add(currentTriple);			
			}
		}
		m.remove(notToBeInserted);
		
		c3prime.setGraph_BASE_MODEL(c3prime.getGraph_BASE_MODEL().union(m));
		MiscUtilities.writeModelToFile(c3prime.getGraph_BASE_MODEL(), graphStoresLocal_URI + Constants.graphStoreCNonT, c3prime.getWithoutTTL_Graph_BASE_NAME() + "AFTER_INSERT-" + timeCalled[1] + ".ttl", "ttl");
		
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
		String [] timeNowStart = MiscUtilities.getTime();
		String entailActivityName = ProvenanceHandler.createNameOfEntailOp(c3prime.getGraph_UpdatedNAME(), timeNowStart[1]);
		Model results = EntailmentUtilities.getEntailmentsOnly(mDescribe, graphStoresLocal_URI + Constants.graphStoreCNonT, "dredCountInsert" + queriedGraphStOpType +  timeCalled[1]+ ".txt");
		String [] timeNowEnd = MiscUtilities.getTime();
		//Model oldBASE_AND_NEW_TRIPLES = c3.getGraph_BASE_MODEL().union(results);
		Model oldInfs_AND_NEW_Infs = c3prime.getGraph_INFS_MODEL().union(results);
		// 5. insert the new triples created by the re-reasoning - or model generated
		//c3.setGraph_BASE_AND_INFS_MODEL(oldBASE_AND_NEW_TRIPLES);
		c3prime.setGraph_INFS_MODEL(oldInfs_AND_NEW_Infs);
		MiscUtilities.writeModelToFile(oldInfs_AND_NEW_Infs, graphStoresLocal_URI + Constants.graphStoreCNonT, c3prime.getWithoutTTL_Graph_INFS_NAME() + "AFTER_INSERT-" + timeCalled[1] + ".ttl", "ttl");
		
		ProvenanceHandler.updateC3ProvEntailment(entailActivityName, this, timeNowStart, timeNowEnd);
		
		try {
			SPARQLUtilities.copyGraphOnFuseki(DATASET4MT, DATASET4COPIES, c3prime.getGraph_INFS_NAME());
			SPARQLUtilities.uploadNewGraph(DATASET4MT, c3prime.getGraph_INFS_NAME(), oldInfs_AND_NEW_Infs);
			SPARQLUtilities.uploadNewGraph(DATASET4MT, c3prime.getGraph_PROV_NAME(), c3prime.getGraph_PROV_MODEL());
		} catch (IOException e) {
			System.err.println("\n************************************************* \n"
							   + "ERROR from Method applyInsertUpdateOnC3 in Class Operator,\n"
							   + "COULD NOT UPLOAD TO FUSEKI, intended graph" +  c3prime.getGraph_INFS_NAME()+ timeCalled[1]+"\n"
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
		
		/* we do not to loop over the triples to be deleted and check if they are inferred (so as to remove them from the list of triples to be deleted)
		 * because we are splitting the graph into 1-)base/ground and 2)inferred triples
		 * so, we just check if they are in the inf graph and remove them from the list of triples to be deleted
		 * since they will be deleted according to the rdfs entailment rules, this way we are not overdeleting*/
		List <Statement> notToBeDeleted = new ArrayList<Statement>();
		
		StmtIterator itStmt = mTriplesToBeDeleted.listStatements();
		while (itStmt.hasNext()) 
		{
			Statement currentTriple = itStmt.next();
			String predicate;
			if (currentTriple.getObject().isLiteral()) {
				predicate = "\"" + currentTriple.getObject().toString() + "\"";
			}
			else {
				predicate = "<" + currentTriple.getObject().toString() + "> ";
			}
			String triple ="<" + currentTriple.getSubject().toString()+ "> " +
							"<" + currentTriple.getPredicate().toString() + "> " +
							predicate;

			if (SPARQLUtilities.checkIfTripleIsInInfGraph(c3prime.getGraph_INFS_MODEL(), triple)) 
			{
				//System.out.println("Won't be deleting: " + triple);
				notToBeDeleted.add(currentTriple);			
			}
		}
		mTriplesToBeDeleted.remove(notToBeDeleted);	
		
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
		Model modelInfTriplesToBeDeleted = ModelFactory.createDefaultModel();
		while (itrDeleteModel.hasNext()) 
		{
			Statement baseTripleToBeDeleted = itrDeleteModel.next();				
			// Now, onto the inf graph (the triple is not in the inf graph, so no need to delete it)
			// we have to loop over their describe graph from the inf
			Resource subjectOfInterest = baseTripleToBeDeleted.getSubject();
			//loop over the triples in the inf graph which share the subject
			Selector selectTriplesOfInterest = new SimpleSelector(subjectOfInterest, null, (RDFNode) null);
			StmtIterator itrTriplesWithSameSubject = mDescribedGraph.listStatements(selectTriplesOfInterest);
			while (itrTriplesWithSameSubject.hasNext()) 
			{
				Statement stmtWithSameSubjct = itrTriplesWithSameSubject.next();
				// now check the property
				Resource currentSubject = stmtWithSameSubjct.getSubject();
				Property currentProperty = stmtWithSameSubjct.getPredicate(); 
				String strCurrentProperty = currentProperty.toString();
				
				/* the below if statement checks for rdfs6, rdfs8, rdf9, rdfs10, rdfs12, rdfs13 */
				if (strCurrentProperty.equalsIgnoreCase("rdf:type")
							|| strCurrentProperty.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) 
				{
					Resource currentObject = stmtWithSameSubjct.getPredicate();
					String strCurrentObject = currentObject.toString();
					
					/* the below if statement checks for rdfs6 */
					if (strCurrentObject.equalsIgnoreCase("rdf:Property") 
							|| strCurrentObject.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"))
					{
						/* we found rdfs6 "xxx rdf:type rdf:Property" so we are removing "xxx rdfs:subPropertyOf xxx" */
						modelInfTriplesToBeDeleted.add(currentSubject, RDFS.subPropertyOf, currentSubject);
					} 
					/* the below if statement checks for rdfs8 and rdfs 10*/
					else if (strCurrentObject.equalsIgnoreCase("rdfs:Class") 
							|| strCurrentObject.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#Class"))
					{
						/* we found rdfs8 "xxx rdf:type rdfs:Class" so we are removing "xxx rdfs:subClassOf rdfs:Resource" */
						modelInfTriplesToBeDeleted.add(currentSubject, RDFS.subClassOf, RDFS.Resource);
						/* we found rdfs10 "xxx rdf:type rdfs:Class" so we are removing "xxx rdfs:subClassOf xxx" */
						modelInfTriplesToBeDeleted.add(currentSubject, RDFS.subClassOf, currentSubject);
					}
					/* the below if statement checks for rdfs12*/
					else if (strCurrentObject.equalsIgnoreCase("rdfs:ContainerMembershipProperty") 
							|| strCurrentObject.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty"))
					{
						/* we found rdfs12 "xxx rdf:type rdfs:ContainerMembershipProperty" so we are removing "xxx rdfs:subPropertyOf rdfs:member" */
						modelInfTriplesToBeDeleted.add(currentSubject, RDFS.subPropertyOf, RDFS.member);
					}
					/* the below if statement checks for rdfs13*/
					else if (strCurrentObject.equalsIgnoreCase("rdfs:Datatype") 
							|| strCurrentObject.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#Datatype"))
					{
						/* we found rdfs13 "xxx rdf:type rdfs:Datatype" so we are removing "xxx rdfs:subClassOf rdfs:Literal" */
						modelInfTriplesToBeDeleted.add(currentSubject, RDFS.subClassOf, RDFS.Literal);
					}
					
					/* the below else statement is for rdfs9
					 * it deals with having "xxx rdfs:subClassOf yyy ." and deleting "zzz rdf:type xxx ." 
					 * and thus deletes "zzz rdf:type yyy ."*/
					else 
					{
						// get the statement where the current object is the subject in a triple
						// and the property is subclass of
						Selector selectTriplesWithObjAsSub = new SimpleSelector( (Resource) stmtWithSameSubjct.getObject(), RDFS.subClassOf, (RDFNode) null);
						// for each of the those objects
						StmtIterator itrTypeOfObject = mDescribedGraph.listStatements(selectTriplesWithObjAsSub);
						//loop and create a delete
						while (itrTypeOfObject.hasNext()) 
						{
							Statement stmt = itrTypeOfObject.next();
							modelInfTriplesToBeDeleted.add(currentSubject, RDF.type, stmt.getObject());
						}
					}
				}
				//rdfs2, rdfs3, rdf5, rdfs7, rdfs9, rdfs11 removing "aaa rdfs:subPropertyOf bbb .", rdfs11, rdfs9 removing "xxx rdfs:subclassOf yyy" 
				/* now deal with rdfs2 and rdfs3, removing "yyy aaa zzz ." so deleting 
				 * for rdfs2 "yyy rdf:type xxx ."
				 * and for rdfs3 	"zzz rdf:type xxx ."
				 * BUT ONLY IF THEY ARE NOT PRESENT IN THE base GRAPH
				 * preferably if they also do not become inferred thanks to rdfs9
				 */
				else if (strCurrentProperty.equalsIgnoreCase("rdfs:subClassOf")
						|| strCurrentProperty.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subClassOf")) 
				{
					
				}
				else if (strCurrentProperty.equalsIgnoreCase("rdfs:subPropertyOf")
						|| strCurrentProperty.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subPropertyOf")) 
				{
					
				}
				else if (strCurrentProperty.equalsIgnoreCase("rdfs:domain")
						|| strCurrentProperty.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#domain")) 
				{
					
				}
				else if (strCurrentProperty.equalsIgnoreCase("rdfs:range")
						|| strCurrentProperty.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#range")) 
				{
					
				}
				/*the below else statement is for rdfs7 */
				else 
				{
					//the property is not rdf:type, i.e. someProperty which is a subproperty/superproperty or a property with domain/range
					//the loop over the property:
					Selector selectTriplesWithProperty = new SimpleSelector((Resource) currentProperty, RDFS.subPropertyOf, (RDFNode) null);
					StmtIterator itrTypeOfProperty = mDescribedGraph.listStatements(selectTriplesWithProperty);
					/*the below while statement is for rdfs7
					 * it deals with having "aaa rdfs:subPropertyOf bbb ." and deleting "xxx aaa yyy ."*
					 * and so deletes "xxx bbb yyy ."*/
					while(itrTypeOfProperty.hasNext()) 
					{
						//get its super properties
						Statement stmt = itrTypeOfProperty.next();
						modelInfTriplesToBeDeleted.add(currentSubject, 
								modelInfTriplesToBeDeleted.createProperty(stmt.getObject().toString()), 
								stmtWithSameSubjct.getObject());
					}
				}		
			}
			/* 
			because there is no transitivity/symmetry... then do no need to do anything else		
			*/
		}
		
		// TODO: the below, but when, after deleting but before re-deriving, or after re-deriving?
		// now loop over all triples to be deleted and check for rdfs1 and rdfs4a and rdfs4b
		// what you do is get all IRIs in the to-be-deleted base triples
		// get their describe from the base graph   ... if none is found then delete them as Resource of literal
		
		String strDeleteBaseTriples = SPARQLUtilities.createMultiUpdateStatements(DATASET4MT, c3prime.getGraph_BASE_NAME(), mTriplesToBeDeleted, "delete");
		String strDeleteInfTriples = SPARQLUtilities.createMultiUpdateStatements(DATASET4MT, c3prime.getGraph_INFS_NAME(), modelInfTriplesToBeDeleted, "delete");

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
		
		String [] timeNowStart = MiscUtilities.getTime();
		String entailActivityName = ProvenanceHandler.createNameOfEntailOp(c3prime.getGraph_UpdatedNAME(), timeNowStart[1]);
		Model results = EntailmentUtilities.getEntailmentsOnly(mDescribeSubjectsAndProperties, graphStoresLocal_URI + Constants.graphStoreCNonT, "dredCountDelete" + queriedGraphStOpType + timeNowStart [1]+ ".txt");
		String [] timeNowEnd = MiscUtilities.getTime();
		//Utilities.writeModelToFile(c3prime.getGraph_BASE_MODEL(), graphStoresLocal_URI + graphStoreCNonT + c3prime.getWithoutTTL_Graph_BASE_NAME() + "AFTER_DELETE-" + [1] + ".ttl", "ttl");
		//Utilities.writeModelToFile(c3prime.getGraph_INFS_MODEL(), graphStoresLocal_URI + graphStoreCNonT + c3prime.getWithoutTTL_Graph_INFS_NAME() + "AFTER_DELETE-" + [1] + ".ttl", "ttl");
		String triplesToBeInserted = SPARQLUtilities.createTriplesForUpdate(results);
		String insertQuery = SPARQLUtilities.createUpdateStatement(DATASET4MT, c3prime.getGraph_INFS_NAME(), triplesToBeInserted, "insert");
		
		ProvenanceHandler.updateC3ProvEntailment(entailActivityName, this, timeNowStart, timeNowEnd);
		
		try {
			SPARQLUtilities.copyGraphOnFuseki(DATASET4MT, DATASET4COPIES, c3prime.getGraph_INFS_NAME());
			SPARQLUtilities.updateGraph(DATASET4MT, c3prime.getGraph_INFS_NAME(), insertQuery);
			SPARQLUtilities.uploadNewGraph(DATASET4MT, c3prime.getGraph_PROV_NAME(), c3prime.getGraph_PROV_MODEL());
		} catch (IOException e) {
			System.err.println("\n************************************************* \n"
							   + "ERROR from Method applyInsertUpdateOnC3 in Class Operator,\n"
							   + "COULD NOT UPLOAD TO FUSEKI, intended graph" +  c3prime.getGraph_INFS_NAME()+timeNowStart[1] +"\n"
							   + e.getMessage()
							   + "************************************************* \n");
		}
		
	}
	
	public void generateWhatIsToBeAppliedAsUpdate(String whichGraph) throws IOException 
	{
		Model whatToUseInUpdate = ModelFactory.createDefaultModel();
		String timeCalled = MiscUtilities.getTime()[1];
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
						Constants.tripleStoreURI + DATASET4MT + "/data/" + c3prime.getGraph_PROV_NAME(),
						Constants.customPrefix + ":" + c3prime.getGraph_NAME());
				if (subtrahend.equalsIgnoreCase("Gcopy_" + Constants.graphB2_source_NAME)) {
					setQueriedGraphStOpType("difference1");
				} else if (subtrahend.equalsIgnoreCase("Gcopy_" + Constants.graphA1_source_NAME)) {
					setQueriedGraphStOpType("difference2");
				}
				// now that is sorted, go into getting the update
				if (queriedGraphStOpType.equalsIgnoreCase("difference1")) 
				{
					if (graphUpdateType.equalsIgnoreCase("insert")) {
						b2prime.setUseAllUpdate(true);	
						setGraphUpdateType("delete");
						ProvenanceHandler.updateC3ProvSetOperation(this,timeCalled );
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
			DATASET4MT = Constants.DATASET_UNION;
			DATASET4COPIES = Constants.DATASET4COPIES_UNION;
		} else if (graphStOpType.equalsIgnoreCase("intersection")) {
			DATASET4MT = Constants.DATASET_INTERSECTION;
			DATASET4COPIES = Constants.DATASET4COPIES_INTERSECTION;
		} else if (graphStOpType.equalsIgnoreCase("difference1")) {
			DATASET4MT = Constants.DATASET_DIFFERENCE1;
			DATASET4COPIES = Constants.DATASET4COPIES_DIFFERENCE1;
		} else if (graphStOpType.equalsIgnoreCase("difference2")) {
			DATASET4MT = Constants.DATASET_DIFFERENCE2;
			DATASET4COPIES = Constants.DATASET4COPIES_DIFFERENCE2;
		} else throw new IllegalArgumentException("Invalid graph ST operation type: " 
				+ graphStOpType 
				+ ", was expecting either union, intersection, difference1, or difference2");
	}
	
}
