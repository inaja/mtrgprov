package utilities;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.vocabulary.ReasonerVocabulary;

public class EntailmentUtilities 
{
	
	private static Resource config = ModelFactory.createDefaultModel()
									.createResource()
									.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "default");
	private static Reasoner reasoner = (Reasoner) RDFSRuleReasonerFactory.theInstance().create(config);

	
	/**
	 * This method takes a model, performs entailment on it, and calls saveDerivationCount.
	 * Calling saveDerivationCount, results in the derivation being written to file  
	 * @param model : the model that the reasoning will be performed on
	 * @param fileName : the name of the file where the derivations and their counts will be recorded.
	 * @return the difference between the base mode (passed as parameter) and the inference model.
	 */
	public static Model getRDFSEntailmentsOnly(Model model, String fileName) 
	{
		long timeStart = System.nanoTime();
		reasoner.setDerivationLogging(true);
		InfModel baseAndInf = ModelFactory.createInfModel(reasoner, model);
		InfModel empty = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
		empty.setNsPrefixes(model);
		Model entails = baseAndInf.difference(empty).difference(model);
		
		entails.setNsPrefixes(model);
		long timeEnd = System.nanoTime();
		long timeNeeded = timeEnd - timeStart;
		String timeTaken = Long.toString(timeNeeded);
		//printDerivations(baseAndInf);
		saveDerivationCount(baseAndInf, fileName, timeTaken);
		return entails;
	}
	
	/**
	 * This method takes a model, performs entailment on it, and calls saveDerivationCount.
	 * Calling saveDerivationCount, results in the derivation being written to file  
	 * @param model : the model that the reasoning will be performed on
	 * @param fileName : the name of the file where the derivations and their counts will be recorded.
	 * @return the difference between the base mode (passed as parameter) and the inference model.
	 */
	public static Model getEntailmentsOnly(Model model, String fileName) 
	{
		long timeStart = System.nanoTime();
		reasoner.setDerivationLogging(true);
		InfModel baseAndInf = ModelFactory.createInfModel(reasoner, model);
		InfModel empty = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
		empty.setNsPrefixes(model);
		Model entails = baseAndInf.difference(empty).difference(model);
		
		entails.setNsPrefixes(model);
		long timeEnd = System.nanoTime();
		long timeNeeded = timeEnd - timeStart;
		String timeTaken = Long.toString(timeNeeded);
		//printDerivations(baseAndInf);
		saveDerivationCount(baseAndInf, fileName, timeTaken);
		return entails;
	}
	
	/*public static Model getEntailmentsOnly(Model schema, Model data) 
	{
		reasoner.setDerivationLogging(true);
		Model model = schema.union(data);
		InfModel baseAndInf = ModelFactory.createInfModel(reasoner, schema, model);
		InfModel empty = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
		
		Model entails = baseAndInf.difference(empty).difference(model);
		entails.setNsPrefixes(model);
		return entails;
	}*/
	
	/*public static Model getEntailmentsOnly(Model model) 
	{
		reasoner.setDerivationLogging(true);
		InfModel baseAndInf = ModelFactory.createInfModel(reasoner, model);
		InfModel empty = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
		empty.setNsPrefixes(model);
		Model entails = baseAndInf.difference(empty).difference(model);
		
		entails.setNsPrefixes(model);
		
		return entails;
	}*/
	

	private static void saveDerivationCount(InfModel baseAndInfModel, String fileName, String reasoningTime) {
		String modelSize = Long.toString(baseAndInfModel.size());
		ArrayList<String> results = new ArrayList<String>();
		results.add("Total number of processed triples: " + modelSize + "\n");
		results.add("\nTime taken to reason is: " + reasoningTime + " milliseconds. \n");
		
		ArrayList<String> resultsWith0Derivs = new ArrayList<String>();
		StmtIterator i = baseAndInfModel.listStatements();
		
		int triplesWith0Counter = 0;
		int triplesWithMoreCounter = 0;
		while (i.hasNext()) {
			Statement stmt = i.nextStatement();
			int dCounter = 0;
			String tripleAndCount = "Statement: " + stmt.toString();
			for (Iterator<Derivation> id = baseAndInfModel.getDerivation(stmt); id.hasNext();id.next()) {
				dCounter++;
			}
			tripleAndCount = tripleAndCount + " has " + dCounter + " derivations";
			if (dCounter > 0) {
				results.add(tripleAndCount);
				triplesWithMoreCounter++;
			}
			else {
				resultsWith0Derivs.add(tripleAndCount);
				triplesWith0Counter++;
			}	
		}
		resultsWith0Derivs.add("Count of triples with at least one derivation: " + triplesWithMoreCounter);
		resultsWith0Derivs.add("Count of triples with at 0 derivations: " + triplesWith0Counter);
		Utilities.writeToFile(fileName, results, resultsWith0Derivs);
	}
	
	public static Model getEntailedGraph(Model model) 
	{
		return ModelFactory.createInfModel(reasoner, model);
	}
	
	public static Model getRDFSEntailedGraph(Model model) 
	{
		return ModelFactory.createRDFSModel(model);
	}
	
	public static Model getEntailedGraphAndCount(Model model, String fileName) 
	{
		long timeStart = System.nanoTime();
		reasoner.setDerivationLogging(true);
		InfModel baseAndInf = ModelFactory.createInfModel(reasoner, model);
		
		long timeEnd = System.nanoTime();
		long timeNeeded = timeEnd - timeStart;
		String timeTaken = Long.toString(timeNeeded);
		//printDerivations(baseAndInf);
		saveDerivationCount(baseAndInf, fileName, timeTaken);
		return baseAndInf;
	}
	
	/*
	private static void countDerivations(InfModel resultingModel) {
		StmtIterator i = resultingModel.listStatements();
		while (i.hasNext()) {
			int dCounter = 0;
			Statement stmt = i.nextStatement();
			System.out.println("Statement is: " + stmt);
			for (Iterator<Derivation> id = resultingModel.getDerivation(stmt); id.hasNext();id.next()) {
				dCounter++;
			}
			System.out.println("Has " + dCounter + " derivations");
		}
	}
		
	public static void printDerivations(InfModel resultingModel) {
		PrintWriter pwOut = new PrintWriter(System.out);
		StmtIterator i = resultingModel.listStatements();
		while (i.hasNext()) {
			Statement stmt = i.nextStatement();
			System.out.println("Statement is: " + stmt);
			for (Iterator<Derivation> id = resultingModel.getDerivation(stmt); id.hasNext();) {
				Derivation deriv = (Derivation) id.next();
				deriv.printTrace(pwOut, true);
			}
			pwOut.flush();
		}	
	}
	
	
	/*	
	public static Model[] getBaseAndEntailments(Model model, String fileName) 
	{
		reasoner.setDerivationLogging(true);
		Model[] result = new Model[2];
		
		//InfModel baseAndInf = ModelFactory.createRDFSModel(model);
		InfModel baseAndInf = ModelFactory.createInfModel(reasoner, model);
		InfModel empty = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
		
		Model inferences = baseAndInf.difference(empty).difference(model);
		inferences.setNsPrefixes(model);
		result[0] = baseAndInf;
		result[1] = inferences;
		
		//System.out.println("Printing derivations for both baseandinf");
		//printDerivations(baseAndInf);
		saveDerivationCount(baseAndInf, fileName);
		
		return result;
	}
	
*/	
	
	
	

}
