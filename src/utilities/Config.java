package utilities;

public class Config 
{
	
	public static final String localhostString = "http://localhost:3030/";
	
	public static final String DATASET_ORIGINALS = "OriginalData";
	
	public static final String DATASET_UNION = "MTUnion";
	public static final String DATASET4COPIES_UNION = "CopiesUnion";
	public static final String DATASET4TRADITIONAL_UNION = "TraditionalUnion";
	
	public static final String DATASET_INTERSECTION = "MTIntersection";
	public static final String DATASET4COPIES_INTERSECTION = "CopiesIntersection";
	public static final String DATASET4TRADITIONAL_INTERSECTION = "TraditionalIntersection";
	
	public static final String DATASET_DIFFERENCE1 = "MTDifference";
	public static final String DATASET4COPIES_DIFFERENCE1 = "CopiesDifference";
	public static final String DATASET4TRADITIONAL_DIFFERENCE1 = "TraditionalDifference";
	
	public static final String DATASET_DIFFERENCE2 = "MTDifference2";
	public static final String DATASET4COPIES_DIFFERENCE2 = "CopiesDifference2";
	public static final String DATASET4TRADITIONAL_DIFFERENCE2 = "TraditionalDifference2";
	
	public static String PREFIXES = "PREFIX owl:   <http://www.w3.org/2002/07/owl#>" + "\n" 
						+ "PREFIX rdf:   	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n"
						+ "PREFIX xsd:   	<http://www.w3.org/2001/XMLSchema#>" + "\n"
						+ "PREFIX rdfs:  	<http://www.w3.org/2000/01/rdf-schema#>" + "\n"
						+ "PREFIX prov:  	<http://www.w3.org/ns/prov#>" + "\n"
						+ "PREFIX rgprov: 	<http://www.ecs.soton.ac.uk/rgprov#>" + "\n"
						+ "PREFIX fc:    	<http://example.org/fictionalchars#>" + "\n"
						+ "PREFIX fcsmps: 	<http://example.org/fcsmps#>" + "\n"
						+ "PREFIX inaja: <http://www.ecs.soton.ac.uk/inaja#>" + "\n"
						+ "PREFIX : 		<http://example.org/>"+ "\n";
	//public String NAMESPACE;
	public static final boolean UPLOAD_TO_FUSEKI = true; 
	
	public static final String LOCAL_URI = "d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/GraphStores";
	
	//public static final String graphA1_source_URI = LOCAL_URI + "/sourceGraphsAB/fcSimpsonsFamily.ttl";
	public static final String graphA1_source_URI = localhostString + DATASET_ORIGINALS + "/data/fcSimpsonsFamily.ttl";
	public static final String graphA1_source_NAME = "fcSimpsonsFamily.ttl";
	public static final String graphA1_source_PROV_URI = localhostString + DATASET_ORIGINALS + "/data/ProvA1.ttl";
	public static final String graphA1_source_PROV_NAME = "ProvA1.ttl";
	
	//public static final String graphB2_source_URI = LOCAL_URI + "/sourceGraphsAB/fcBouvier.ttl";
	public static final String graphB2_source_URI = localhostString + DATASET_ORIGINALS + "/data/fcOthers.ttl";
	public static final String graphB2_source_NAME = "fcOthers.ttl";
	public static final String graphB2_source_PROV_URI = localhostString + DATASET_ORIGINALS + "/data/ProvB2.ttl";
	public static final String graphB2_source_PROV_NAME = "ProvB2.ttl";
	
	public static final String graphSTA1B2_NAME = "GopC3.ttl";
	
	public static final String graphC3_NAME = "C3.ttl";
	public static final String graphC3_PROV_NAME = "C3prov.ttl";
	public static final String graphC3Updated_NAME = "C3Updated.ttl";
	
	public static final String graphSTA1B2_INSERT_NAME = "GopINSERTC3.ttl";
	public static final String updateGraphB2_INSERT_URI = localhostString + DATASET_ORIGINALS + "/data/insert.ttl";
	public static final String updateGraphB2_INSERT_NAME = "InsertedTriples.ttl";
	public static final String graphB2prime_sourceInserted_URI = localhostString + DATASET_ORIGINALS + "/data/fcOthersUpdatedInsert.ttl";
	public static final String updateGraphB2_sourceINSERTED_NAME = "fcOthersUpdatedInsert.ttl";
	public static final String graphB2prime_INSERTED_PROV_URI = localhostString + DATASET_ORIGINALS + "/data/ProvB2UpdatedInsert.ttl";
	public static final String graphB2_source_PROV_INSERTED_NAME = "ProvB2UpdatedInsert.ttl";
	
	public static final String graphSTA1B2_DELETE_NAME = "GopDELETEC3.ttl";
	public static final String updateGraphB2_DELETE_URI = localhostString + DATASET_ORIGINALS + "/data/delete2.ttl";
	public static final String updateGraphB2_DELETE_NAME = "DeletedTriples.ttl";
	public static final String graphB2prime_sourceDeleted_URI = localhostString + DATASET_ORIGINALS + "/data/fcOthersUpdatedDelete.ttl";
	public static final String updateGraphB2_sourceDELETED_NAME = "fcOthersUpdatedDelete.ttl";
	public static final String graphB2prime_DELETED_PROV_URI = localhostString + DATASET_ORIGINALS + "/data/ProvB2UpdatedDelete.ttl";
	public static final String graphB2_source_PROV_DELETED_NAME = "ProvB2UpdatedDelete.ttl";
	
	//public static final String graphB2prime_PROV_source_NAME = "pb2updated.ttl"; //I am assuming the provenance of the source retains the same name
}
