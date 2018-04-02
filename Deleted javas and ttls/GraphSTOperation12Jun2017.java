package miniT;

import utilities.*;
import org.apache.jena.rdf.model.*;

public class GraphSTOperation {
	
	public static Model applyGraphSt (Model graph1, Model graph2, String stOpType){
		
		if (stOpType.equalsIgnoreCase("union")){
			Model resultGraph = GraphSTUtilities.modelUnion(graph1, graph2);
			return resultGraph;
		} else if (stOpType.equalsIgnoreCase("intersection")){
			Model resultGraph = GraphSTUtilities.modelIntersect(graph1, graph2);
			return resultGraph;
		} else if (stOpType.equalsIgnoreCase("difference")){
			Model resultGraph = GraphSTUtilities.modelDifference(graph1, graph2);
			return resultGraph;
		} else {
			System.out.println("Error: Invalid Set theoretic operation!");
			return null;
		}
	}
	
	
}
