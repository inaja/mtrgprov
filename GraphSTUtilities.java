package utilities;

import java.util.Map;

import org.apache.jena.rdf.model.*;

public class GraphSTUtilities 
{
	
	public static Model applyGraphSt (Model graph1, Model graph2, String stOpType){
		
		if (stOpType.equalsIgnoreCase("union")){
			Model resultGraph = modelUnion(graph1, graph2);
			Map<String,String> m1 = graph1.getNsPrefixMap();
			Map<String,String> m2 = graph2.getNsPrefixMap();
			m1.putAll(m2);
			resultGraph.setNsPrefixes(m1);
			return resultGraph;
		} else if (stOpType.equalsIgnoreCase("intersection")){
			Model resultGraph = modelIntersect(graph1, graph2);
			Map<String,String> m1 = graph1.getNsPrefixMap();
			Map<String,String> m2 = graph2.getNsPrefixMap();
			m1.putAll(m2);
			resultGraph.setNsPrefixes(m1);
			return resultGraph;
		} else if (stOpType.equalsIgnoreCase("difference1")){
			Model resultGraph = modelDifference(graph1, graph2);
			Map<String,String> m1 = graph1.getNsPrefixMap();
			Map<String,String> m2 = graph2.getNsPrefixMap();
			m1.putAll(m2);
			resultGraph.setNsPrefixes(m1);
			return resultGraph;
		} else if (stOpType.equalsIgnoreCase("difference2")){
			Model resultGraph = modelDifference(graph2, graph1);
			Map<String,String> m1 = graph1.getNsPrefixMap();
			Map<String,String> m2 = graph2.getNsPrefixMap();
			m1.putAll(m2);
			resultGraph.setNsPrefixes(m1);
			return resultGraph;
		} else {
			System.err.println("Error in Method applyGraphSt in Class GraphSTUtilities:\n"
							+ "Invalid Set theoretic operation!");
			return null;
		}
	}
	
	public static Model modelUnion (Model graph1, Model graph2) 
	{
		Model result = graph1.union(graph2);
		
		return result;
	}
	
	public static Model modelUnionFromFile (String graphName1, String graphName2) 
	{
		Model graph1 = MiscUtilities.readModelFromFile(graphName1);
		Model graph2 = MiscUtilities.readModelFromFile(graphName1);
		
		Model result = graph1.union(graph2);
		
		return result;
	}
	
	public static void modelUnionFromFileToFile (String graphName1, String graphName2, String resultGraphName, String format) 
	{
		Model graph1 = MiscUtilities.readModelFromFile(graphName1);
		Model graph2 = MiscUtilities.readModelFromFile(graphName1);
		
		Model result = graph1.union(graph2);
		MiscUtilities.writeModelToFile(result, resultGraphName, format);
	}

	private static Model modelIntersect (Model graph1, Model graph2) 
	{
		Model result = graph1.intersection(graph2);
		
		return result;
	}
	
	public static Model modelIntersectFromFile (String graphName1, String graphName2) 
	{
		Model graph1 = MiscUtilities.readModelFromFile(graphName1);
		Model graph2 = MiscUtilities.readModelFromFile(graphName1);
		
		Model result = graph1.intersection(graph2);
		
		return result;
	}
	
	public static void modelIntersectFromFileToFile (String graphName1, String graphName2, String resultGraphName, String format) 
	{
		Model graph1 = MiscUtilities.readModelFromFile(graphName1);
		Model graph2 = MiscUtilities.readModelFromFile(graphName1);
		
		Model result = graph1.intersection(graph2);
		MiscUtilities.writeModelToFile(result, resultGraphName, format);
	}
	
	private static Model modelDifference (Model graph1, Model graph2) 
	{
		Model result = graph1.difference(graph2);
		
		return result;
	} 
	
	public static Model modelDifferenceFromFile (String graphName1, String graphName2) 
	{
		Model graph1 = MiscUtilities.readModelFromFile(graphName1);
		Model graph2 = MiscUtilities.readModelFromFile(graphName1);
		
		Model result = graph1.difference(graph2);
		
		return result;
	}
	
	public static void modelDifferenceFromFileToFile (String graphName1, String graphName2, String resultGraphName, String format) 
	{
		Model graph1 = MiscUtilities.readModelFromFile(graphName1);
		Model graph2 = MiscUtilities.readModelFromFile(graphName1);
		
		Model result = graph1.difference(graph2);
		MiscUtilities.writeModelToFile(result, resultGraphName, format);
	}
}
