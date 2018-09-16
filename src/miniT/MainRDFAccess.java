package miniT;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelFactoryBase;

import operators.Operator;
import operators.TraditionalOperator;
import operators.UpdatedOperator;
import utilities.*;

public class MainRDFAccess 
{
	//private static String datasetNameOnServer = Constants.DATASET;
	//private static String graphStoresLocal_URI = Constants.LOCAL_URI;
	private static int napDuration = 15;
	
	private static String graphA1_source_URI = Constants.graphA1_source_URI;
	private static String graphA1_source_NAME = Constants.graphA1_source_NAME;
	private static String graphA1_source_PROV_URI = Constants.graphA1_source_PROV_URI;
	private static String graphA1_source_PROV_NAME = Constants.graphA1_source_PROV_NAME;
	
	private static String graphB2_source_URI = Constants.graphB2_source_URI;
	private static String graphB2_source_NAME = Constants.graphB2_source_NAME;
	private static String graphB2_source_PROV_URI = Constants.graphB2_source_PROV_URI;
	private static String graphB2_source_PROV_NAME = Constants.graphB2_source_PROV_NAME;
	
	private static String graphSTA1B2_NAME = Constants.graphSTA1B2_NAME;
	private static String graphStOp;
	
	private static String graphC3_NAME = Constants.graphC3_NAME;
	private static String graphC3_PROV_NAME = Constants.graphC3_PROV_NAME;
	
	// the update details
	private static String graphC3prime_INSERTED_NAME = Constants.graphC3Updated_INSERT_NAME;
	private static String graphC3prime_DELETED_NAME = Constants.graphC3Updated_DELETE_NAME;
	
	private static String updateOpInsert = "insert";
	private static String graphSTA1B2PRIME_INSERT_NAME = Constants.graphSTA1B2_INSERT_NAME;
	private static String updateGraphB2_URI_Insert = Constants.updateGraphB2_INSERT_URI; /* the file with the triples to be inserted */
	private static String updateGraphB2_NAME_Insert = Constants.updateGraphB2_INSERT_NAME; /* the file with the triples to be inserted */
	//private static String updateGraphB2_sourceINSERTED_NAME = Constants.updateGraphB2_sourceINSERTED_NAME; /* B2prime, i.e. B2 with triples inserted*/
	private static String graphB2prime_sourceInserted_URI = Constants.graphB2prime_sourceInserted_URI; /* URI of B2prime, i.e. B2 with triples inserted*/
	private static String graphB2prime_INSERTED_PROV_URI = Constants.graphB2prime_INSERTED_PROV_URI; /*ProvB2UpdatedInsert.ttl*/
	private static String graphB2prime_INSERTED_PROV_source_NAME = Constants.graphB2_source_PROV_INSERTED_NAME; //I am assuming the provenance of the source retains the same name
	
	private static String updateOpDelete = "delete";
	private static String graphSTA1B2PRIME_DELETE_NAME = Constants.graphSTA1B2_DELETE_NAME;
	private static String updateGraphB2_URI_Delete = Constants.updateGraphB2_DELETE_URI; /* the file with the triples to be deleted */
	private static String updateGraphB2_NAME_Delete = Constants.updateGraphB2_DELETE_NAME; /* the file with the triples to be inserted */
	//private static String updateGraphB2_sourceDELETED_NAME = Config.updateGraphB2_sourceDELETED_NAME; /* B2'', i.e. B2 with triples that were deleted*/
	private static String graphB2prime_sourceDeleted_URI = Constants.graphB2prime_sourceDeleted_URI; /* fcOthersUpdatedDelete.ttl */
	private static String graphB2prime_DELETED_PROV_URI = Constants.graphB2prime_DELETED_PROV_URI; /* ProvB2UpdatedDelete.ttl */
	private static String graphB2prime_DELETED_PROV_source_NAME = Constants.graphB2_source_PROV_DELETED_NAME;
	
	private static Operator o;
	private static TraditionalOperator t;
	private static UpdatedOperator uoInsert;
	private static UpdatedOperator uoDelete;
	
	public static void main (String[] args) throws Exception 
	{
		//Model cinfs = SPARQLUtilities.loadGraphFromFuseki(Config.DATASET_UNION, "C3Infs.ttl");
		//System.out.println("Marge has Father in infs: " + SPARQLUtilities.checkIfTripleIsInInfGraph(cinfs, "fcsmps:Marge fc:hasFather fcsmps:Clancy"));
		//System.out.println("Marge has Parent in infs: " + SPARQLUtilities.checkIfTripleIsInInfGraph(cinfs, "fcsmps:Marge fc:hasParent fcsmps:Clancy"));
		
		System.out.println("********************************************");
		System.out.println("Started at " + MiscUtilities.getTime()[0]);
		graphStOp = "intersection";
		//graphStOp = "intersection";
		//graphStOp = "difference1";
		//System.out.println("Remember: this will swap delete and insert!");
		//graphStOp = "difference2";
		System.out.println("The ST op is:" + graphStOp);
		System.out.println("---------------------------------------------");
		System.out.println("Create initial graph...");
		o = new Operator(graphA1_source_URI, graphA1_source_PROV_URI, graphB2_source_URI, graphB2_source_PROV_URI);
		createInitialGraph(graphStOp);
				
		MiscUtilities.goToSleep(napDuration);		
		System.out.println("Now for the Insert update...");
		System.out.println("---------------------------------------------");
		uoInsert = new UpdatedOperator(graphA1_source_URI, graphA1_source_PROV_URI, updateGraphB2_URI_Insert, graphB2prime_INSERTED_PROV_URI);
		applyInsertUpdate();
				
		MiscUtilities.goToSleep(napDuration);
		System.out.println("Now for the Delete update...");
		System.out.println("---------------------------------------------");
		uoDelete = new UpdatedOperator(graphA1_source_URI, graphA1_source_PROV_URI, updateGraphB2_URI_Delete, graphB2prime_DELETED_PROV_URI);
		applyDeleteUpdate();
		
		MiscUtilities.goToSleep(napDuration);
		useTraditional(graphStOp);
		
		System.out.println("Finished at " + MiscUtilities.getTime()[0]);
		
	}
	
	private static void createInitialGraph(String graphStOpType) throws IOException 
	{
		graphStOp = graphStOpType;
		o.setGraphStOpType(graphStOp);
		o.loadBothGraphsAndTheirProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);	
		o.initialiazeC(graphC3_NAME, graphC3_PROV_NAME );
		
		o.createSTGraph(graphSTA1B2_NAME);
		
		o.reasonRDFS(Constants.UPLOAD_TO_FUSEKI, "First_" + graphStOp);
	}
	
	private static void applyInsertUpdate() throws Exception 
	{
		uoInsert.loadUpdateAndProv("B2", graphB2_source_NAME, updateGraphB2_NAME_Insert, graphB2prime_INSERTED_PROV_source_NAME);
		uoInsert.setDATASETS(graphStOp);
		uoInsert.getA1().setGraph_source_NAME(graphA1_source_NAME);
		uoInsert.initializeCprime(graphC3_NAME, graphC3_PROV_NAME, graphC3prime_INSERTED_NAME);
		uoInsert.setGraphUpdateType(updateOpInsert);
		uoInsert.applyUpdateAndUpdateProv("B2", graphSTA1B2PRIME_INSERT_NAME);	
	}
	
	private static void applyDeleteUpdate() throws Exception 
	{
		uoDelete.loadUpdateAndProv("B2", graphB2_source_NAME, updateGraphB2_NAME_Delete, graphB2prime_DELETED_PROV_source_NAME);
		uoDelete.setDATASETS(graphStOp);
		uoDelete.getA1().setGraph_source_NAME(graphA1_source_NAME);
		uoDelete.initializeCprime(graphC3_NAME, graphC3_PROV_NAME, graphC3prime_DELETED_NAME);
		uoDelete.setGraphUpdateType(updateOpDelete);
		uoDelete.applyUpdateAndUpdateProv("B2", graphSTA1B2PRIME_DELETE_NAME);	
	}
	
	private static void useTraditional(String graphStOpType) throws IOException 
	{
		System.out.println("Now for the traditional way \n----------------------------------------------");
		graphStOp = graphStOpType;
		
		t = new TraditionalOperator(graphA1_source_URI, graphA1_source_PROV_URI, graphB2_source_URI, graphB2_source_PROV_URI);
		t.setGraphStOpType(graphStOp);
		t.loadBothGraphsAndTheirProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);
		t.initializeC(graphC3_NAME, graphC3_PROV_NAME);
		t.createSTGraph(graphSTA1B2_NAME);
		t.reasonRDFS(Constants.UPLOAD_TO_FUSEKI, "First_" + graphStOp);
			
		MiscUtilities.goToSleep(napDuration);
		t = new TraditionalOperator(graphA1_source_URI, graphA1_source_PROV_URI, graphB2prime_sourceInserted_URI, graphB2prime_INSERTED_PROV_URI);
		t.setGraphStOpType(graphStOp);
		t.loadBothGraphsAndTheirProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);		
		t.initializeC(graphC3_NAME, graphC3_PROV_NAME);
		t.createSTGraph(graphSTA1B2_NAME);
		t.reasonRDFS(Constants.UPLOAD_TO_FUSEKI, "Insert_" + graphStOp);

		
		MiscUtilities.goToSleep(napDuration);
		t = new TraditionalOperator(graphA1_source_URI, graphA1_source_PROV_URI, graphB2prime_sourceDeleted_URI, graphB2prime_DELETED_PROV_URI);		
		t.setGraphStOpType(graphStOp);
		t.loadBothGraphsAndTheirProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);
		t.initializeC(graphC3_NAME, graphC3_PROV_NAME);
		t.createSTGraph(graphSTA1B2_NAME);
		t.reasonRDFS(Constants.UPLOAD_TO_FUSEKI, "Delete_ " + graphStOp);
		
	}
/*	
	public static void main (String[] args) throws Exception 
	{
		 // This method is just to test the number of triples in graph union, update, diff1 & diff2
		
		Model a1 = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphA1_source_URI);
		Model a1entailed = EntailmentUtilities.getEntailedGraph(a1);
		Utilities.writeModelToFile(a1entailed, Config.LOCAL_URI + "/graphStoreC/basedata/a1entailed.ttl", "ttl");
		Model b2 = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphB2_source_URI);
		Model b2entailed = EntailmentUtilities.getEntailedGraph(b2);
		Utilities.writeModelToFile(b2entailed, Config.LOCAL_URI + "/graphStoreC/basedata/b2entailed.ttl", "ttl");
		
		System.out.println("Size of A1:" + a1.size() + "," + "Size of A1Entailed:" + a1entailed.size());
		System.out.println("Size of B1:" + b2.size() + "," + "Size of B2Entailed:" + b2entailed.size());
		
		Model unionM = a1.union(b2);	
		System.out.println("Size of union:" + unionM.size());
		Utilities.writeModelToFile(unionM, Config.LOCAL_URI + "/graphStoreC/basedata/unionBaseFirst.ttl", "ttl");
		Model entailUnionM = EntailmentUtilities.getEntailedGraph(unionM);
		System.out.println("Size of entailed union:" + entailUnionM.size());
		Utilities.writeModelToFile(entailUnionM, Config.LOCAL_URI + "/graphStoreC/basedata/unionInfsFirst.ttl", "ttl");
		
		Model unionMEntailed = a1entailed.union(b2entailed);	
		System.out.println("Size of union of entailed A and B:" + unionMEntailed.size());
		Utilities.writeModelToFile(unionMEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/unionMEntailed.ttl", "ttl");
		Model unionEntailedMEntailed = EntailmentUtilities.getEntailedGraph(unionMEntailed);
		System.out.println("Size of entailed union of entailed A and B:" + unionEntailedMEntailed.size());
		Utilities.writeModelToFile(unionEntailedMEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/unionEntailedMEntailed.ttl", "ttl");
		
		Model intersectM = ModelFactory.createDefaultModel();
		intersectM = a1.intersection(b2);
		intersectM.setNsPrefixes(a1.getNsPrefixMap());
		System.out.println("Size of intersection:" + intersectM.size());
		Utilities.writeModelToFile(intersectM, Config.LOCAL_URI + "/graphStoreC/basedata/intersectionBaseFirst.ttl", "ttl");
		Model entailintersectM = EntailmentUtilities.getEntailedGraph(intersectM);
		System.out.println("Size of entailed intersection:" + entailintersectM.size());
		Utilities.writeModelToFile(entailintersectM, Config.LOCAL_URI + "/graphStoreC/basedata/intersectionInfsFirst.ttl", "ttl");
		
		Model intersectMEntailed = a1entailed.intersection(b2entailed);	
		System.out.println("Size of union of entailed A and B:" + intersectMEntailed.size());
		Utilities.writeModelToFile(intersectMEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/intersectMEntailed.ttl", "ttl");
		Model intersectEntailedMEntailed = EntailmentUtilities.getEntailedGraph(intersectMEntailed);
		System.out.println("Size of entailed intersection of entailed A and B:" + intersectEntailedMEntailed.size());
		Utilities.writeModelToFile(intersectEntailedMEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/intersectEntailedMEntailed.ttl", "ttl");
		
		
		Model diff1M = a1.difference(b2);
		System.out.println("Size of difference1:" + diff1M.size());
		Utilities.writeModelToFile(diff1M, Config.LOCAL_URI + "/graphStoreC/basedata/difference1BaseFirst.ttl", "ttl");
		Model entaildiff1M = EntailmentUtilities.getEntailedGraph(diff1M);
		System.out.println("Size of entailed difference1:" + entaildiff1M.size());
		Utilities.writeModelToFile(entaildiff1M, Config.LOCAL_URI + "/graphStoreC/basedata/difference1InfsFirst.ttl", "ttl");
		
		Model diff1MEntailed = a1entailed.difference(b2entailed);	
		System.out.println("Size of diff1 of entailed A and B:" + diff1MEntailed.size());
		Utilities.writeModelToFile(diff1MEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/diff1MEntailed.ttl", "ttl");
		Model diff1EntailedMEntailed = EntailmentUtilities.getEntailedGraph(diff1MEntailed);
		System.out.println("Size of entailed diff1 of entailed A and B:" + diff1EntailedMEntailed.size());
		Utilities.writeModelToFile(diff1EntailedMEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/diff1EntailedMEntailed.ttl", "ttl");
		
		
		Model diff2M = b2.difference(a1);
		System.out.println("Size of difference2:" + diff2M.size());
		Utilities.writeModelToFile(diff2M, Config.LOCAL_URI + "/graphStoreC/basedata/difference2BaseFirst.ttl", "ttl");
		Model entaildiff2M = EntailmentUtilities.getEntailedGraph(diff2M);
		System.out.println("Size of entailed difference2:" + entaildiff2M.size());	
		Utilities.writeModelToFile(entaildiff2M, Config.LOCAL_URI + "/graphStoreC/basedata/difference2InfsFirst.ttl", "ttl");
		
		Model diff2MEntailed = b2entailed.difference(a1entailed);	
		System.out.println("Size of diff2 of entailed A and B:" + diff2MEntailed.size());
		Utilities.writeModelToFile(diff2MEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/diff2MEntailed.ttl", "ttl");
		Model diff2EntailedMEntailed = EntailmentUtilities.getEntailedGraph(diff2MEntailed);
		System.out.println("Size of entailed diff2 of entailed A and B:" + diff2EntailedMEntailed.size());
		Utilities.writeModelToFile(diff2EntailedMEntailed, Config.LOCAL_URI + "/graphStoreC/basedata/diff2EntailedMEntailed.ttl", "ttl");
		
		
		System.out.println("\n" + "After Insert");
		Model b2prime = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphB2prime_sourceInserted_URI);
		
		System.out.println("Size of A1:" + a1.size());
		System.out.println("Size of B1prime:" + b2prime.size());
		
		Model unionMprime = a1.union(b2prime);	
		System.out.println("Size of union:" + unionMprime.size());
		Utilities.writeModelToFile(unionMprime, Config.LOCAL_URI + "/graphStoreC/basedata/unionBaseInsert.ttl", "ttl");
		Model entailUnionMprime = EntailmentUtilities.getEntailedGraph(unionMprime);
		System.out.println("Size of entailed union:" + entailUnionMprime.size());
		Utilities.writeModelToFile(entailUnionMprime, Config.LOCAL_URI + "/graphStoreC/basedata/unionInfsInsert.ttl", "ttl");
		
		Model intersectMprime = a1.intersection(b2prime);
		System.out.println("Size of intersection:" + intersectMprime.size());
		Utilities.writeModelToFile(intersectMprime, Config.LOCAL_URI + "/graphStoreC/basedata/intersectionBaseInsert.ttl", "ttl");
		Model entailintersectMprime = EntailmentUtilities.getEntailedGraph(intersectMprime);
		System.out.println("Size of entailed intersection:" + entailintersectMprime.size());
		Utilities.writeModelToFile(entailintersectMprime, Config.LOCAL_URI + "/graphStoreC/basedata/intersectionInfsInsert.ttl", "ttl");
		
		Model diff1Mprime = a1.difference(b2prime);
		System.out.println("Size of difference1:" + diff1Mprime.size());
		Utilities.writeModelToFile(diff1Mprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff1BaseInsert.ttl", "ttl");
		Model entaildiff1Mprime = EntailmentUtilities.getEntailedGraph(diff1Mprime);
		System.out.println("Size of entailed difference1:" + entaildiff1Mprime.size());
		Utilities.writeModelToFile(entaildiff1Mprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff1InfsInsert.ttl", "ttl");
		
		Model diff2Mprime = b2prime.difference(a1);
		System.out.println("Size of difference2:" + diff2Mprime.size());
		Utilities.writeModelToFile(diff2Mprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff2BaseInsert.ttl", "ttl");
		Model entaildiff2Mprime = EntailmentUtilities.getEntailedGraph(diff2Mprime);
		System.out.println("Size of entailed difference2:" + entaildiff2Mprime.size());	
		Utilities.writeModelToFile(entaildiff2Mprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff2InfsInsert.ttl", "ttl");
		
		System.out.println("\n" + "After Delete");
		Model b2primeprime = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphB2prime_sourceDeleted_URI);
		
		System.out.println("Size of A1:" + a1.size());
		System.out.println("Size of B1:" + b2primeprime.size());
		
		Model unionMprimeprime = a1.union(b2primeprime);	
		System.out.println("Size of union:" + unionMprimeprime.size());
		Utilities.writeModelToFile(unionMprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/unionBaseDelete.ttl", "ttl");
		Model entailUnionMprimeprime = EntailmentUtilities.getEntailedGraph(unionMprimeprime);
		System.out.println("Size of entailed union:" + entailUnionMprimeprime.size());
		Utilities.writeModelToFile(entailUnionMprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/unionInfsDelete.ttl", "ttl");
		
		Model intersectMprimeprime = a1.intersection(b2primeprime);
		System.out.println("Size of intersection:" + intersectMprimeprime.size());
		Utilities.writeModelToFile(intersectMprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/intersectionBaseDelete.ttl", "ttl");
		Model entailintersectMprimeprime = EntailmentUtilities.getEntailedGraph(intersectMprimeprime);
		System.out.println("Size of entailed intersection:" + entailintersectMprimeprime.size());
		Utilities.writeModelToFile(entailintersectMprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/intersectionInfsDelete.ttl", "ttl");
		
		Model diff1Mprimeprime = a1.difference(b2primeprime);
		System.out.println("Size of difference1:" + diff1Mprimeprime.size());
		Utilities.writeModelToFile(diff1Mprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff1BaseDelete.ttl", "ttl");
		Model entaildiff1Mprimeprime = EntailmentUtilities.getEntailedGraph(diff1Mprimeprime);
		System.out.println("Size of entailed difference1:" + entaildiff1Mprimeprime.size());
		Utilities.writeModelToFile(entaildiff1Mprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff1BaseDelete.ttl", "ttl");
		
		Model diff2Mprimeprime = b2primeprime.difference(a1);
		System.out.println("Size of difference2:" + diff2Mprimeprime.size());
		Utilities.writeModelToFile(diff2Mprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff2BaseDelete.ttl", "ttl");
		Model entaildiff2Mprimeprime = EntailmentUtilities.getEntailedGraph(diff2Mprimeprime);
		System.out.println("Size of entailed difference2:" + entaildiff2Mprimeprime.size());	
		Utilities.writeModelToFile(entaildiff2Mprimeprime, Config.LOCAL_URI + "/graphStoreC/basedata/diff2BaseDelete.ttl", "ttl");
		
	}
*/	
	/*public static void main (String[] args) throws Exception 
	{
		// This method is just to test
		Model b2prov = SPARQLUtilities.loadGraphFromFuseki(Config.DATASET_UNION, "C3prov.ttl");
		System.out.println(SPARQLUtilities.getStOpFromProvGraph(b2prov, "http://localhost:3030/MTUnion/data/C3prov.ttl", "inaja:C3.ttl"));
	}*/
	/*
	public static void main (String[] args) throws Exception 
	{
		// This method is just to test the set theoretic operation update propagation
		Model a1 = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphA1_source_URI);
		Model b2 = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphB2_source_URI);
		Model a1Ub2 = a1.union(b2), a1Ib2 = a1.intersection(b2), a1D1b2=a1.difference(b2), a1D2b2 = b2.difference(a1);
		
		Model b2primeInsert = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphB2prime_sourceInserted_URI);
		Model a1Ub2primeInsert = a1.union(b2primeInsert), a1Ib2primeInsert = a1.intersection(b2primeInsert), a1D1b2primeInsert=a1.difference(b2primeInsert), a1D2b2primeInsert = b2primeInsert.difference(a1);
		Utilities.writeModelToFile(a1Ub2primeInsert, Config.LOCAL_URI + "/testStpropagation/a1Ub2primeInsert.ttl", "TURTLE");
		Utilities.writeModelToFile(a1Ib2primeInsert, Config.LOCAL_URI + "/testStpropagation/a1Ib2primeInsert.ttl", "TURTLE");
		Utilities.writeModelToFile(a1D1b2primeInsert, Config.LOCAL_URI + "/testStpropagation/a1D1b2primeInsert.ttl", "TURTLE");
		Utilities.writeModelToFile(a1D2b2primeInsert, Config.LOCAL_URI + "/testStpropagation/a1D2b2primeInsert.ttl", "TURTLE");
		
		Model b2InsertUpdate = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS,updateGraphB2_URI_Insert);
		Model a1Ub2InsertUpdate = a1Ub2.union(b2InsertUpdate);
		Utilities.writeModelToFile(a1Ub2InsertUpdate, Config.LOCAL_URI + "/testStpropagation/a1Ub2InsertUpdate.ttl", "TURTLE");
		Model a1Ib2InsertUpdate = a1Ib2.union(b2InsertUpdate.intersection(a1));
		Utilities.writeModelToFile(a1Ib2InsertUpdate, Config.LOCAL_URI + "/testStpropagation/a1Ib2InsertUpdate.ttl", "TURTLE");
		Model a1D1b2InsertUpdate = a1D1b2.difference(b2InsertUpdate);
		Utilities.writeModelToFile(a1D1b2InsertUpdate, Config.LOCAL_URI + "/testStpropagation/a1D1b2InsertUpdate.ttl", "TURTLE");
		Model a1D2b2InsertUpdate = a1D2b2.union(b2InsertUpdate.difference(a1));
		Utilities.writeModelToFile(a1D2b2InsertUpdate, Config.LOCAL_URI + "/testStpropagation/a1D2b2InsertUpdate.ttl", "TURTLE");

		 
		Model b2primeDelete = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS, graphB2prime_sourceDeleted_URI);
		Model a1Ub2primeDelete = a1.union(b2primeDelete), a1Ib2primeDelete = a1.intersection(b2primeDelete), a1D1b2primeDelete=a1.difference(b2primeDelete), a1D2b2primeDelete = b2primeDelete.difference(a1);
		a1Ib2primeDelete.setNsPrefixes(a1.getNsPrefixMap()); a1D1b2primeDelete.setNsPrefixes(a1.getNsPrefixMap()); a1D2b2primeDelete.setNsPrefixes(a1.getNsPrefixMap());
		Utilities.writeModelToFile(a1Ub2primeDelete, Config.LOCAL_URI + "/testStpropagation/a1Ub2primeDelete.ttl", "TURTLE");
		Utilities.writeModelToFile(a1Ib2primeDelete, Config.LOCAL_URI + "/testStpropagation/a1Ib2primeDelete.ttl", "TURTLE");
		Utilities.writeModelToFile(a1D1b2primeDelete, Config.LOCAL_URI + "/testStpropagation/a1D1b2primeDelete.ttl", "TURTLE");
		Utilities.writeModelToFile(a1D2b2primeDelete, Config.LOCAL_URI + "/testStpropagation/a1D2b2primeDelete.ttl", "TURTLE");
		
		Model b2UpdateDelete = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(Config.DATASET_ORIGINALS,updateGraphB2_URI_Delete);
		Model a1Ub2DeleteUpdate = a1Ub2primeInsert.difference(b2UpdateDelete.difference(a1));
		a1Ub2DeleteUpdate.setNsPrefixes(a1.getNsPrefixMap());
		Utilities.writeModelToFile(a1Ub2DeleteUpdate, Config.LOCAL_URI + "/testStpropagation/a1Ub2DeleteUpdate.ttl", "TURTLE");
		Model a1Ib2DeleteUpdate = a1Ib2primeInsert.difference(b2UpdateDelete);
		a1Ib2DeleteUpdate.setNsPrefixes(a1.getNsPrefixMap());
		Utilities.writeModelToFile(a1Ib2DeleteUpdate, Config.LOCAL_URI + "/testStpropagation/a1Ib2DeleteUpdate.ttl", "TURTLE");
		Model a1D1b2DeleteUpdate = a1D1b2primeInsert.union(b2UpdateDelete.intersection(a1));
		a1D1b2DeleteUpdate.setNsPrefixes(a1.getNsPrefixMap());
		Utilities.writeModelToFile(a1D1b2DeleteUpdate, Config.LOCAL_URI + "/testStpropagation/a1D1b2DeleteUpdate.ttl", "TURTLE");
		Model a1D2b2DeleteUpdate = a1D2b2primeInsert.difference(b2UpdateDelete.difference(a1));
		a1D2b2DeleteUpdate.setNsPrefixes(a1.getNsPrefixMap());
		Utilities.writeModelToFile(a1D2b2DeleteUpdate, Config.LOCAL_URI + "/testStpropagation/a1D2b2DeleteUpdate.ttl", "TURTLE");

	}*/


}