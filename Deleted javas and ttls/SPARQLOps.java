package miniT;

import org.apache.jena.rdf.model.Model;

public class SPARQLOps {
	public static void applyUpdateOp (String graphName, String updateGraphName, String updateOp, String originalST) {
		if (originalST.equalsIgnoreCase("union") ) {
			if (updateOp.equalsIgnoreCase("Insert")) {
				
			} else if (updateOp.equalsIgnoreCase("Delete")) {
				
			}
		} else if (originalST.equalsIgnoreCase("intersection") ) {
			if (updateOp.equalsIgnoreCase("Insert")) {
				
			} else if (updateOp.equalsIgnoreCase("Delete")) {
				
			}
			
		} else if (originalST.equalsIgnoreCase("difference1")) {
			if (updateOp.equalsIgnoreCase("Insert")) {
				
			} else if (updateOp.equalsIgnoreCase("Delete")) {
				
			}
		} else if (originalST.equalsIgnoreCase("difference2")) {
			if (updateOp.equalsIgnoreCase("Insert")) {
				
			} else if (updateOp.equalsIgnoreCase("Delete")) {
				
			}
		}
	}

}
