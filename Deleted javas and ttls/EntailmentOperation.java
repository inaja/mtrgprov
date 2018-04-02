package miniT;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.vocabulary.ReasonerVocabulary;

public class EntailmentOperation {
	
	private static Resource config = ModelFactory.createDefaultModel()
			.createResource()
			.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "default");
	private static Reasoner reasoner = (Reasoner) RDFSRuleReasonerFactory.theInstance().create(config);

	
	
}
