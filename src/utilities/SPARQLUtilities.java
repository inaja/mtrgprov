/**
 * @author Emm
 * This class is for interfacing with Fuseki. 
 * It does all the uploading to and reading from Fuseki
 */

package utilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;

public class SPARQLUtilities 
{
	private static String tripleStoreURI = Constants.tripleStoreURI;
	//private static String DATASET = Config.DATASET;
	//private static String DATASET_ORIGINALS = Config.DATASET_ORIGINALS;
	//private static String DATASET4COPIES = Config.DATASET4COPIES;
	private static String PREFIXES = Constants.PREFIXES;
	
	/**
	 * This method loads a graph from Fuseki
	 * @param graphName, the name of the graph to be retrieved from Fuseki
	 * @throws IOException, in case of errors
	 */
	public static Model loadGraphFromFuseki(String DATASET, String graphName) throws IOException {
		String serviceURI =  tripleStoreURI + DATASET + "/data";
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serviceURI);
		Model m = accessor.getModel(serviceURI + "/" + graphName);
		return m;
	}
	
	/**
	 * This method loads a graph from Fuseki
	 * @param graphName, the name of the graph to be retrieved from Fuseki
	 * @throws IOException, in case of errors
	 */
	public static Model loadOriginalGraphFromFusekiFullURI(String DATASET_ORIGINALS, String fullGraphURI) throws IOException {
		String serviceURI =  tripleStoreURI + DATASET_ORIGINALS + "/data";
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serviceURI);
		Model m = accessor.getModel(fullGraphURI);
		return m;
	}
	
	/**
	 * This method uploads/writes-over a graph to Fuseki
	 * @param graphName, the name of the graph to be created on Fuseki
	 * @param model, the model, i.e. the actual graph
	 * @throws IOException, in case of errors
	 */
	/*public static void uploadNewGraph(String graphName, Model model) throws IOException {
		String serviceURI =  localhostString + DATASET + "/data";
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serviceURI);
		accessor.putModel(graphName, model);		
	}*/
	
	/**
	 * This method uploads/writes-over a graph to Fuseki. It is to be used when the intended
	 * dataset is different than the default one.
	 * @param dataset, the name of the dataset.
	 * @param graphName, the name of the graph to be created on Fuseki
	 * @param model, the model, i.e. the actual graph
	 * @throws IOException, in case of errors
	 */
	public static void uploadNewGraph(String dataset, String graphName, Model model) throws IOException {
		String serviceURI =  tripleStoreURI + dataset + "/data";
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serviceURI);
		accessor.putModel(graphName, model);		
	}
	
	/**
	 * This method updates a graph that is already on Fuseki
	 * @param graphName, the name of the graph on Fuseki that is to be updated
	 * @param updateStatement, the statement to be applied
	 * @throws IOException, in case of errors
	 */
	public static void updateGraph(String DATASET, String graphName, String updateStatement) throws IOException {
		String url =  tripleStoreURI + DATASET + "/update";
		//System.out.println(url);
		UpdateProcessor upp = UpdateExecutionFactory.createRemote(UpdateFactory.create(String.format(updateStatement, graphName)),url);
		upp.execute();
	}
		
	/**
	 * This method makes a copy of graph on Fuseki
	 * @param graphName1, the name of the graph to be copied
	 * @param newGraphName, the name of the graph to be created
	 * @throws IOException, in case of errors
	 */
	public static void copyGraphOnFuseki(String DATASET, String DATASET4COPIES, String graphName) throws IOException {
		//String serviceURI =  localhostString + DATASET + "/update";
		//String copyQuery = "copy <" + localhostString + DATASET + "/data/" + graphName1 + ">"
		//				  + " TO <" + localhostString + DATASET + "/data/" + newGraphName + ">";
		String strFromServiceURI =  tripleStoreURI + DATASET + "/data";
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(strFromServiceURI);
		Model m = accessor.getModel(strFromServiceURI + "/" + graphName);
		
		String strToServiceURI =  tripleStoreURI + DATASET4COPIES + "/data";
		String timeCalled[] = MiscUtilities.getTime();
		accessor = DatasetAccessorFactory.createHTTP(strToServiceURI);
		accessor.putModel(graphName + "Old" + timeCalled[1] + ".ttl", m);
	}
	
	/** This method returns from Fuseki a graph created from the describe statement
	 * @param describeStatement, the describe statement to be sent to Fuseki 
	 * @return Model, the graph generated by running the describe statement
	 * @throws IOException, in case of errors
	 */
	public static Model describeThatOf (String DATASET, String describeStatement) throws Exception {
		String url =  tripleStoreURI + DATASET + "/query";
		QueryExecution qExec = QueryExecutionFactory.sparqlService(url, describeStatement);
		Model results = qExec.execDescribe();
		qExec.close();
		return results;
	}
	
	/** This method returns from Fuseki an Iterator over Triples graph created from the describe statement
	 * @param describeStatement, the describe statement to be sent to Fuseki 
	 * @return an iterator over the the graph generated by running the describe statement
	 * @throws IOException, in case of errors
	 */
	public static Iterator<Triple> describeTriplesOf (String DATASET, String describeStatement) throws Exception {
		String url =  tripleStoreURI + DATASET + "/query";
		QueryExecution qExec = QueryExecutionFactory.sparqlService(url, describeStatement);
		Iterator<Triple> results = qExec.execDescribeTriples();
		qExec.close();
		return results;
	}
	
	/**
	 * This method does not interact with Fuseki. It produces ONE update statement to be 
	 * applied to the graph that is already on Fuseki.
	 * @param graphName, the name of the graph on Fuseki to be updated 
	 * @param triplesToBeUpdated, the triples to be added/removed from the graph
	 * @param updateOp, insert or delete
	 * @return the update statement that will actually be sent to Fuseki to update the graph
	 */
	public static String createUpdateStatement (String DATASET, String graphName, String tripleToBeUpdated, String updateOp) {
		//setTestPrefixes();
		String updateStatement = PREFIXES + "\n";
		if (updateOp.equalsIgnoreCase("insert")) {
			updateStatement = updateStatement
							  + "INSERT DATA { \n"
							  + "GRAPH <" + tripleStoreURI + DATASET + "/data/" 
							  + graphName + ">\n"
							  + "{" + tripleToBeUpdated + " }"
							  + "}";
		} 
		//then it is delete
		else {
			updateStatement = updateStatement
							  + "DELETE DATA { \n"
							  + "GRAPH <" + tripleStoreURI + DATASET + "/data/" 
							  + graphName + ">\n"	
							  + "{" + tripleToBeUpdated + "}"
							  + "}";
		}
		return updateStatement;
	}
	
	/**
	 * This method does not interact with Fuseki. It produces ONE update statement containing
	 * MULTIPLE INSERT or DELETE statements to be 
	 * applied to the graph that is already on Fuseki.
	 * @param graphName, the name of the graph on Fuseki to be updated 
	 * @param triplesToBeUpdated, the triples to be added/removed from the graph
	 * @param updateOp, insert or delete
	 * @return the update statement that will actually be sent to Fuseki to update the graph
	 */
	public static String createMultiUpdateStatements (String DATASET, String graphName, Set<String> triplesToBeUpdated, String updateOp) {
		//setTestPrefixes();
		String updateStatement = PREFIXES + "\n";
		if (updateOp.equalsIgnoreCase("insert")) {
			updateStatement = updateStatement 
					  + "INSERT DATA { \n"
					  + "GRAPH <" + tripleStoreURI + DATASET + "/data/" 
					  + graphName + ">\n {\n";
			for (String triple: triplesToBeUpdated) {
				updateStatement = updateStatement +  triple + ".\n";
			}
		} 
		//then it is delete
		else {
			updateStatement = updateStatement
					  + "DELETE DATA { \n"
					  + "GRAPH <" + tripleStoreURI + DATASET + "/data/" 
					  + graphName + ">\n {\n";
			for (String triple: triplesToBeUpdated) {
				updateStatement = updateStatement +  triple + " .\n";
			}
		}
		updateStatement = updateStatement + "}\n}";
		return updateStatement;
	}
	
	/**
	 * This method does not interact with Fuseki. It produces the describe statement to be 
	 * applied to the graph that is already on Fuseki.
	 * @param graphName, the name of the graph on Fuseki to get the describe of the triple from it 
	 * @param tripleToBeDescribed, the triple to be described
	 * @return a String containing the describe Statement
	 */
	public static String createDescribeStatement (String DATASET, String graphName, String toBeDescribed) {
		String describeStatement;
		//setTestPrefixes();
		describeStatement = PREFIXES + "\n"  
							+ "DESCRIBE  " + toBeDescribed
							+ "\n where { "
							+ "GRAPH <" + tripleStoreURI + DATASET + "/data/" 
							+ graphName + ">"	
							+ " {}" + " }";
		return describeStatement;
	}
	
	public static String getStOpFromProvGraph(Model model, String graphPROVURI, String graphName) {
		String stringResults = "";
		String queryString = PREFIXES
							+ "\n" + "SELECT  ?setoptype"
							+ "\n" + "FROM <" + graphPROVURI + ">"
							+ "\n" + "WHERE {"
							+ "\n" + "?setop rdf:type ?setoptype ."
							+ "\n" + "?gop prov:wasGeneratedBy ?setop ."
							+ "\n" + graphName + " rgprov:wasEntailedFrom ?gop "
							+ "\n" + "}";
		Query qry = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(qry, model);
		ResultSet results = qe.execSelect();
		if (results.hasNext()) {
			QuerySolution binding = results.nextSolution();
			Resource subj = (Resource) binding.get("setoptype");
			stringResults = subj.getLocalName();
		}
				
		return stringResults;
		
	}
	
	public static String getHasSubtrahend(Model model, String graphPROVURI, String graphName) {
		String stringResults = "";
		String queryString = PREFIXES
							+ "\n" + "SELECT  ?b"
							+ "\n" + "FROM <" + graphPROVURI + ">"
							+ "\n" + "WHERE {"
							+ "\n" + "?setop rdf:type ?setoptype ."
							+ "\n" + "?gop prov:wasGeneratedBy ?setop ."
							+ "\n" + graphName + " rgprov:wasEntailedFrom ?gop ."
							+ "\n" + "?setop rgprov:hadSubtrahend ?b ."
							+ "\n" + "}";
		Query qry = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(qry, model);
		ResultSet results = qe.execSelect();
		if (results.hasNext()) {
			QuerySolution binding = results.nextSolution();
			Resource subj = (Resource) binding.get("b");
			stringResults = subj.getLocalName();
		}
				
		return stringResults;
		
	}
	/* untested */
	public static String selectFromModelOnFuseki (String DATASET, String selectStatement) {
		String url =  DATASET + "/query";
		QueryExecution qe = QueryExecutionFactory.sparqlService(url, selectStatement);
		ResultSet results = qe.execSelect();
		String stringResult = ResultSetFormatter.asText(results);
	    qe.close();
	    return stringResult;
	}
	/* untested */
	public static void printSelectFromModelOnFuseki (String DATASET, String graphName, String selectStatement) {
		String url =  tripleStoreURI + DATASET + "/query";
		QueryExecution qe = QueryExecutionFactory.sparqlService(url, selectStatement);
		ResultSet results = qe.execSelect();
	    ResultSetFormatter.out(System.out, results);
	    qe.close();
	}
	
	/**
	 * This method does not interact with Fuseki. It produces a String statement of the Triples
	 * to be inserted or deleted to a graph that is already on Fuseki.
	 * @param updateModel, the model from which the triples will be restructured
	 * @return a String containing a list of triples to be added to an update statement
	 */
	public static String createTriplesForUpdate(Model updateModel) {
		String sTriples = "";
		StmtIterator it = updateModel.listStatements();
		while (it.hasNext()) {
			Statement stmt = it.next();
			sTriples = 	sTriples 
						+ "<" + stmt.getSubject() + "> "
						+ "<" + stmt.getPredicate() + "> ";
			if (stmt.getObject().isLiteral()) {
				sTriples = sTriples + "\"" + stmt.getObject() + "\". \n";
			}
			else{
				sTriples = sTriples + "<" + stmt.getObject() + ">. \n";
			}
		}
		return sTriples;
	}
	
	public static String createDescribeStmtForUpdateTriples (String dataset, String graphName, Statement stmt) {
		String tripleString = "<" + stmt.getSubject() + "> "
					 		+ "<" + stmt.getPredicate() + "> ";
		if (stmt.getObject().isLiteral()) 
			tripleString = tripleString + "?z";
		else
			tripleString = tripleString + "<" + stmt.getObject() + ">";

		String describeStmt = createDescribeStatement(dataset, graphName, tripleString);
		return describeStmt;
	}
	
	public static String createDescribeStmtForUpdatedSubjects (String dataset, String graphName, String subject) {
		String tripleString = "<" + subject + "> ";
		
		String describeStmt = createDescribeStatement(dataset, graphName, tripleString);
		return describeStmt;
	}
	
	public static boolean checkIfTripleIsInInfGraph(Model m, String triple) 
	{
		String queryString = PREFIXES + "\n"
							+ "ASK \n" 
							//+ "from " +  url + "\n"
							+ "where \n"
							+ "{" + triple + "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, m);		
		boolean isInGraph = qe.execAsk();
		qe.close();
		return isInGraph;
	}
	
	/*public static String getDATASET() {
		return DATASET;
	}*/

	/*public static void setDATASET(String dataset) {
		SPARQLUtilities.DATASET = dataset;
	}*/

	public static String getPREFIXES() {
		return PREFIXES;
	}

	public static void setPREFIXES(String prefixes) {
		PREFIXES = prefixes;
	}
	
	/*
	public static void setTestPrefixes() {
		PREFIXES = "PREFIX owl:   <http://www.w3.org/2002/07/owl#>" + "\n" 
					+ "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n"
					+ "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>" + "\n"
					+ "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>" + "\n"
					+ "prefix fc:    <http://example.org/fictionalchars#>" + "\n"
					+ "prefix fcsmps: <http://example.org/fcsmps#>"+ "\n";
	}
	
	public static void testDescribeAndSelect() {
		String url = "http://localhost:3030/test2/query";
		String PREFIXES = "prefix owl:   <http://www.w3.org/2002/07/owl#>" + "\n" 
				+ "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\n"
				+ "prefix xsd:   <http://www.w3.org/2001/XMLSchema#>" + "\n"
				+ "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>" + "\n"
				+ "prefix fc:    <http://example.org/fictionalchars#>" + "\n"
				+ "prefix fcsmps: <http://example.org/fcsmps#>"+ "\n";
		
		String qDescribe = PREFIXES + "DESCRIBE ?x ?y ?z where { " +
						   "GRAPH <http://localhost:3030/test2/data/C3BaseAndInfs.ttl> " +
						   "{  fcsmps:Homer ?y ?z } }" ;
		QueryExecution qExec = QueryExecutionFactory.sparqlService(url, qDescribe);
		Model results =   qExec.execDescribe();
		results.write(System.out);
		qExec.close();
		
		System.out.println("\n SELECT \n");
		url = "http://localhost:3030/test2/query";
		String qSelect = PREFIXES + "SELECT ?x ?y ?z where { " +
				   "GRAPH <http://localhost:3030/test2/data/C3BaseAndInfs.ttl> " +
				   "{  fcsmps:Homer ?y ?z } }" ;
		qExec = QueryExecutionFactory.sparqlService(url, qSelect);
		ResultSet r =  qExec.execSelect();
		//results.write(System.out);
		ResultSetFormatter.out(System.out, r);
		qExec.close();
	}*/
}
