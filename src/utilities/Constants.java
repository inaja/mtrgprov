package utilities;

public class Constants 
{
	/*
	 *  First, we specify the URI of the Graph Store and the names of the Datasets 
	 */
	public static final String tripleStoreURI = "http://localhost:3030/"; // this is my local Fuseki
	public static final boolean UPLOAD_TO_FUSEKI = true;
	
	public static final String DATASET_ORIGINALS = "OriginalData"; // this is where graphs A1 and B2 are
	// the following three specify the names of the datasets for the union operation
	public static final String DATASET_UNION = "MTUnion";
	public static final String DATASET4COPIES_UNION = "CopiesUnion";
	public static final String DATASET4TRADITIONAL_UNION = "TraditionalUnion";
	// the following three specify the names of the datasets for the intersection operation
	public static final String DATASET_INTERSECTION = "MTIntersection";
	public static final String DATASET4COPIES_INTERSECTION = "CopiesIntersection";
	public static final String DATASET4TRADITIONAL_INTERSECTION = "TraditionalIntersection";
	// the following three specify the names of the datasets for the difference 1 operation
	public static final String DATASET_DIFFERENCE1 = "MTDifference";
	public static final String DATASET4COPIES_DIFFERENCE1 = "CopiesDifference";
	public static final String DATASET4TRADITIONAL_DIFFERENCE1 = "TraditionalDifference";
	// the following three specify the names of the datasets for the difference 2 operation
	public static final String DATASET_DIFFERENCE2 = "MTDifference2";
	public static final String DATASET4COPIES_DIFFERENCE2 = "CopiesDifference2";
	public static final String DATASET4TRADITIONAL_DIFFERENCE2 = "TraditionalDifference2";
	
	/*
	 * Now, we specify the namespaces and prefixes
	 */
	public static String egNamespace = "http://example.org/";
	public static String egPrefix = "";
	public static String provNamespace = "http://www.w3.org/ns/prov#";
	public static String rgprovNamespace = "http://www.ecs.soton.ac.uk/rgprov#";
	public static final String customNameSpace = "http://www.ecs.soton.ac.uk/inaja#";
	public static final String customNameSpaceFull = "PREFIX inaja: <http://www.ecs.soton.ac.uk/inaja#>";
	public static final String customPrefix = "inaja";
	public static String PREFIXES = "PREFIX owl:   <http://www.w3.org/2002/07/owl#>" + "\n" 
						+ "PREFIX rdf:   	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n"
						+ "PREFIX xsd:   	<http://www.w3.org/2001/XMLSchema#>" + "\n"
						+ "PREFIX rdfs:  	<http://www.w3.org/2000/01/rdf-schema#>" + "\n"
						+ "PREFIX prov:  	<http://www.w3.org/ns/prov#>" + "\n"
						+ "PREFIX rgprov: 	<http://www.ecs.soton.ac.uk/rgprov#>" + "\n"
						+ "PREFIX fc:    	<http://example.org/fictionalchars#>" + "\n"
						+ "PREFIX fcsmps: 	<http://example.org/fcsmps#>" + "\n"
						+ customNameSpaceFull + "\n"
						+ "PREFIX : 		<http://example.org/>"+ "\n";
	
	/* 
	 * Now, we specify information about our local system.
	 * This represents the locations on disc for local graph store, and cache
	 * and anywhere else we want outputted files to be saved
	 */
	public static final String LOCAL_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/GraphStores";
	public static final String cacheNonT = "/cacheStoreC/nontraditional/";
	public static final String cacheT = "/cacheStoreC/traditional/";
	public static final String tempNonT = "/tempMemoryC/nontraditional/";
	public static final String tempT = "/tempMemoryC/traditional/";
	public static final String graphStoreCNonT = "/graphStoreC/nontraditional/";
	public static final String graphStoreCT = "/graphStoreC/traditional/";
	public static final String provStoreCNontT = "/graphStoreC/provStoreC/nontraditional/";
	public static final String provStoreCT = "/graphStoreC/provStoreC/traditional/";
	
	/* 
	 * Now, we specify information about the files: their names and their provenance on Fuseki 
	 * As well as their names and the names of their provenances in the local system.
	 */
	public static final String graphA1_source_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/fcSimpsonsFamily.ttl";
	public static final String graphA1_source_NAME = "fcSimpsonsFamily.ttl";
	public static final String graphA1_source_PROV_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/ProvA1.ttl";
	public static final String graphA1_source_PROV_NAME = "ProvA1.ttl";
	
	public static final String graphB2_source_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/fcOthers.ttl";
	public static final String graphB2_source_NAME = "fcOthers.ttl";
	public static final String graphB2_source_PROV_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/ProvB2.ttl";
	public static final String graphB2_source_PROV_NAME = "ProvB2.ttl";
	
	public static final String graphSTA1B2_NAME = "GopC3.ttl";
	
	public static final String graphC3_NAME = "C3.ttl";
	public static final String graphC3_PROV_NAME = "C3prov.ttl";
	//public static final String graphC3Updated_NAME = "C3Updated.ttl";
	public static final String graphC3Updated_INSERT_NAME = "C3UpdatedAfterInsert.ttl";
	public static final String graphC3Updated_DELETE_NAME = "C3UpdatedAfterDelete.ttl";
	
	public static final String graphSTA1B2_INSERT_NAME = "GopINSERTC3.ttl";
	public static final String updateGraphB2_INSERT_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/insert.ttl";
	public static final String updateGraphB2_INSERT_NAME = "InsertedTriples.ttl";
	public static final String graphB2prime_sourceInserted_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/fcOthersUpdatedInsert.ttl";
	public static final String updateGraphB2_sourceINSERTED_NAME = "fcOthersUpdatedInsert.ttl";
	public static final String graphB2prime_INSERTED_PROV_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/ProvB2UpdatedInsert.ttl";
	public static final String graphB2_source_PROV_INSERTED_NAME = "ProvB2UpdatedInsert.ttl";
	
	public static final String graphSTA1B2_DELETE_NAME = "GopDELETEC3.ttl";
	public static final String updateGraphB2_DELETE_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/delete2.ttl";
	public static final String updateGraphB2_DELETE_NAME = "DeletedTriples.ttl";
	public static final String graphB2prime_sourceDeleted_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/fcOthersUpdatedDelete.ttl";
	public static final String updateGraphB2_sourceDELETED_NAME = "fcOthersUpdatedDelete.ttl";
	public static final String graphB2prime_DELETED_PROV_URI = tripleStoreURI + DATASET_ORIGINALS + "/data/ProvB2UpdatedDelete.ttl";
	public static final String graphB2_source_PROV_DELETED_NAME = "ProvB2UpdatedDelete.ttl";
	
	//public static final String graphB2prime_PROV_source_NAME = "pb2updated.ttl"; //I am assuming the provenance of the source retains the same name
	
	/* 
	 * Now we specify some things for EvoGen and LUBM
	 */
	public static final String ontology = "http://www.ecs.soto.ac.uk/izn1g08/univBenchEdited.rdf";
    public static final String ontologyLocation = LOCAL_URI + "/univ-benchEdited.ttl";
}
