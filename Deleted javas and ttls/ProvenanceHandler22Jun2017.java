package miniT;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import utilities.GraphSTUtilities;
import utilities.Operator;
import utilities.Producer;
import utilities.Utilities;
import utilities.graph.SourceGraphInSystem;

public class ProvenanceHandler {
	
	private static String rgprovNamespace = "http://www.ecs.soton.ac.uk/rgprov#";
	private static String provNamespace = "http://www.w3.org/ns/prov#";
	
	public static void createProvOfSourceGraph (String graphNametimeFetched, SourceGraphInSystem sg) {
		Model mprov = sg.getGraph_PROV_star_COPY_MODEL(); 
				
		Resource jersey225 = mprov.getResource("jersey225");
		if (jersey225 == null) {
			jersey225 =  mprov.createResource("jersey225");
		}
		Resource RESTClient = mprov.getResource("RESTClient");
		if (RESTClient == null) {
			RESTClient = mprov.createResource("RESTClient");
		}
		jersey225.addProperty(RDF.type, RESTClient);
		
		// add to the passed model the triples that describe its fetching as per rgprov 	
		Resource fetch = mprov.createResource("fetch" + graphNametimeFetched);
		Property wasAssociatedWith = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (wasAssociatedWith == null) {
			wasAssociatedWith = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		fetch.addProperty(wasAssociatedWith, jersey225);
		
		Resource rgprovFetch = mprov.getResource(rgprovNamespace + "Fetch");
		if (rgprovFetch == null) {
			rgprovFetch = mprov.createResource(rgprovNamespace + "Fetch");
		}
		
		fetch.addProperty(RDF.type, rgprovFetch);
		Property accessed = mprov.getProperty(provNamespace + "accessed");
		if (accessed == null) {
			accessed = mprov.createProperty(provNamespace + "accessed");
		}
		
		Property copied = mprov.getProperty(rgprovNamespace + "copied");
		if (copied == null) {
			copied = mprov.createProperty(rgprovNamespace + "copied");
		}
		
		Property wasCopyResult = mprov.getProperty(rgprovNamespace + "wasCopyResult");
		if (wasCopyResult == null) {
			wasCopyResult = mprov.createProperty(rgprovNamespace + "wasCopyResult");
		}
		Property wasExactCopy = mprov.getProperty(rgprovNamespace + "wasExactCopy");
		if (wasExactCopy == null) {
			wasExactCopy = mprov.createProperty(rgprovNamespace + "wasExactCopy");
		}
		
		Resource graphResource = mprov.createResource(sg.getGraph_source_NAME());
		fetch.addProperty(accessed, sg.getGraph_source_URI());
		fetch.addProperty(copied,graphResource);
		Resource graphCopyResource = mprov.createResource(sg.getGraph_PROV_COPY_NAME());
		graphCopyResource.addProperty(wasCopyResult, fetch);
		graphCopyResource.addProperty(wasExactCopy, graphResource);
		
		sg.setGraph_PROV_star_COPY_MODEL(mprov); //this is redundant but i am being paranoid
	}
	
	public static void updateProvOfSourceGraphAfterLoadingUpdate(String graphNametimeFetched, SourceGraphInSystem sg) {
	/*	Model mprov = sg.getGraphprime_PROV_star_COPY_MODEL();
		
		Resource jersey225 = mprov.getResource("jersey225");
		if (jersey225 == null) {
			jersey225 =  mprov.createResource("jersey225");
		}
		Resource RESTClient = mprov.getResource("RESTClient");
		if (RESTClient == null) {
			RESTClient = mprov.createResource("RESTClient");
		}
		jersey225.addProperty(RDF.type, RESTClient);
		
		// add to the passed model the triples that describe its fetching as per rgprov
		Resource fetch = mprov.createResource("fetch" + graphNametimeFetched);
		Property wasAssociatedWith = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (wasAssociatedWith == null) {
			wasAssociatedWith = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		fetch.addProperty(wasAssociatedWith, jersey225);
		
		Resource rgprovFetch = mprov.getResource(rgprovNamespace + "Fetch");
		if (rgprovFetch == null) {
			rgprovFetch= mprov.createResource(rgprovNamespace + "Fetch");
		}
		fetch.addProperty(RDF.type, rgprovFetch);
		Property accessed = mprov.getProperty("accessed");
		if (accessed == null) {
			accessed = mprov.createProperty("accessed");
		}
		fetch.addProperty(accessed, sg.getUpdateGraph_URI());
		Property copied = mprov.getProperty(rgprovNamespace + "copied");
		if (copied == null) {
			copied = mprov.createProperty(rgprovNamespace + "copied");
		}
		
		Property wasCopyResult = mprov.getProperty(rgprovNamespace + "wasCopyResult");
		if (wasCopyResult == null) {
			wasCopyResult = mprov.createProperty(rgprovNamespace + "wasCopyResult");
		}
		Property wasExactCopy = mprov.getProperty(rgprovNamespace + "wasExactCopy");
		if (wasExactCopy == null) {
			wasExactCopy = mprov.createProperty(rgprovNamespace + "wasExactCopy");
		}
		
		Resource graphB2update = mprov.createResource(p.getGraphB2Update_URI());
		fetch.addProperty(accessed, p.getGraphB2Update_URI());
		fetch.addProperty(copied,graphB2update);
		Resource graphB2updateCopy = mprov.createResource(p.getGraphB2Update_COPY_NAME());
		graphB2updateCopy.addProperty(wasCopyResult, fetch);
		graphB2updateCopy.addProperty(wasExactCopy, graphB2update);
		
		Property wasRevisionOf = mprov.getProperty(provNamespace + "wasRevisionOf");
		if (wasRevisionOf == null) {
			wasRevisionOf = mprov.createProperty(provNamespace + "wasRevisionOf");
		}
		Resource graphB2 = mprov.getResource(sg.getUpdateGraph_URI());
		Resource graphB2prime = mprov.createResource(sg.getUpdateGraph_URI());
		graphB2prime.addProperty(wasRevisionOf, graphB2);
		
		Resource provPrimeB2prime = mprov.createResource(p.getGraphB2prime_PROV_URI());
		Resource provB2 = mprov.getResource(p.getGraphB2_PROV_URI());
		provPrimeB2prime.addProperty(wasRevisionOf, provB2);
		
		//P'copy(B',2) is a new version of P'copy(B,2) , this creates the triple:
		//P'copy(B',2 prov:wasRevisionOf P'copy(B,2) .
		
		p.setGraphB2prime_PROV_MODEL(mprov);
		p.setGraphC3_PROV_MODEL(p.getGraphC3_PROV_MODEL().union(mprov));
	*/
		
	}
	
	public static void updateC3SetOperation (String stOpActivityName,  Operator o) {
		
		Model mprov = o.getC3().getGraph_PROV_MODEL();
		Resource Jena =  mprov.createResource("Jena");
		Resource jena3111 = mprov.createResource("jena3.1.1");
		jena3111.addProperty(RDF.type, Jena);
		Property wasAssociatedWith = mprov.createProperty(provNamespace + "wasAssociatedWith");
		Property used =  mprov.createProperty(provNamespace + "Used");
		Property wasGeneratedBy = mprov.createProperty(provNamespace + "wasGeneratedBy");
		Property wasDerivedFrom = mprov.createProperty(provNamespace + "wasDerivedFrom");
		Resource gOpC3 = mprov.createResource(o.getGraphSTA1B2_NAME());
		Resource gcopyA1 = mprov.getResource(o.getA1().getGraph_COPY_NAME());
		Resource gcopyB2 = mprov.getResource(o.getB2().getGraph_COPY_NAME());
		gOpC3.addProperty(wasDerivedFrom, gcopyA1);
		gOpC3.addProperty(wasDerivedFrom, gcopyB2);
		
		String stOp = o.getGraphStOpType();
		if (stOp.equalsIgnoreCase("union")) {
			Resource gu = mprov.createResource(stOpActivityName);
			Resource rgprovUnion = mprov.createResource(rgprovNamespace + "Union");
			gu.addProperty(RDF.type, rgprovUnion);
			gu.addProperty(wasAssociatedWith, jena3111);
			gu.addProperty(used, gcopyA1);
			gu.addProperty(used, gcopyB2);
			gOpC3.addProperty(wasGeneratedBy, gu);
		}
		else if (stOp.equalsIgnoreCase("intersection")) {
			Resource gi = mprov.createResource(stOpActivityName);
			Resource rgprovIntersection = mprov.createResource(rgprovNamespace + "Intersection");
			gi.addProperty(RDF.type, rgprovIntersection);
			gi.addProperty(wasAssociatedWith, jena3111);
			gi.addProperty(used, gcopyA1);
			gi.addProperty(used, gcopyB2);
			gOpC3.addProperty(wasGeneratedBy, gi);
			
		} else if (stOp.equalsIgnoreCase("difference1")) {
			Resource gd = mprov.createResource(stOpActivityName);
			Resource rgprovDifference = mprov.createResource(rgprovNamespace + "Difference");
			gd.addProperty(RDF.type, rgprovDifference);
			gd.addProperty(wasAssociatedWith, jena3111);
			Property hadMinuend = mprov.createProperty(rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, gcopyA1);
			gd.addProperty(hadSubtrahend, gcopyB2);
			gOpC3.addProperty(wasGeneratedBy, gd);
			
		} else if (stOp.equalsIgnoreCase("difference2")) {
			Resource gd = mprov.createResource(stOpActivityName);
			Resource rgprovDifference = mprov.createResource(rgprovNamespace + "Difference");
			gd.addProperty(RDF.type, rgprovDifference);
			gd.addProperty(wasAssociatedWith, jena3111);
			Property hadMinuend = mprov.createProperty(rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, gcopyB2);
			gd.addProperty(hadSubtrahend, gcopyA1);
			gOpC3.addProperty(wasGeneratedBy, gd);
			
		}	
		//setGraphC3_PROV_MODEL(mprov);
	}
	
	public static void updateC3Entailment (String entailActivityName, Operator o) {
		Model mprov = o.getC3().getGraph_PROV_MODEL();
		Resource jena3111 = mprov.getResource("jena3.1.1");
		Property wasAssociatedWith = mprov.getProperty(provNamespace + "wasAssociatedWith");
		Property used =  mprov.getProperty(provNamespace + "Used");
		Property wasGeneratedBy = mprov.getProperty(rgprovNamespace + "wasGeneratedBy");
		Property wasDerivedFrom = mprov.getProperty(rgprovNamespace + "wasDerivedFrom");
		Resource gcopyA1 = mprov.getProperty(o.getA1().getGraph_COPY_NAME());
		Resource gcopyB2 = mprov.getProperty(o.getB2().getGraph_COPY_NAME());
		Resource gOpC3 = mprov.getResource(o.getGraphSTA1B2_NAME());
		
		Resource ge = mprov.createResource(entailActivityName);
		Resource rdfsEntailment = mprov.createResource(rgprovNamespace + "RDFSEntailment");
		ge.addProperty(RDF.type, rdfsEntailment);
		ge.addProperty(wasAssociatedWith, jena3111);
		ge.addProperty(used, gOpC3);
		
		Resource gC3 = mprov.createResource(o.getC3().getGraph_PROV_NAME());
		gC3.addProperty(wasGeneratedBy, ge);
		Property wasEntailedFrom = mprov.createProperty(rgprovNamespace + "wasEntailedFrom");
		gC3.addProperty(wasEntailedFrom, gOpC3);
		
		gC3.addProperty(wasDerivedFrom, gcopyA1);
		gC3.addProperty(wasDerivedFrom, gcopyB2);
		
		//p.setGraphC3_PROV_MODEL(mprov);
	}
	//this method should be deleted once the provenance handler has been updated
	public static void createProvOfSourceGraph (String spaceName, String graphNametimeFetched, String whichGraph, Producer p) {
		Model mprov; 
		if (whichGraph.equalsIgnoreCase("A1")) {
			mprov = p.getGraphA1_PROV_MODEL();
		} else if (whichGraph.equalsIgnoreCase("B2")) {
			mprov = p.getGraphB2_PROV_MODEL();
		} else {
			System.out.println("You passed the wrong source name graph");
			throw new IllegalArgumentException("Wrong source name graph");
		}
		
		Resource jersey225 = mprov.getResource(spaceName + "jersey225");
		Resource RESTClient = mprov.getResource(spaceName + "RESTClient");
		if (jersey225 == null && RESTClient == null) {
			jersey225 =  mprov.createResource( spaceName + "jersey225");
			RESTClient = mprov.createResource(spaceName + "RESTClient");
			jersey225.addProperty(RDF.type, RESTClient);
		}
		// add to the passed model the triples that describe its fetching as per rgprov 
				
		Resource fetch = mprov.createResource(spaceName + "fetch" + graphNametimeFetched);
		Property wasAssociatedWith = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (wasAssociatedWith == null) {
			wasAssociatedWith = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		fetch.addProperty(wasAssociatedWith, jersey225);
		
		Resource rgprovFetch = mprov.getResource(rgprovNamespace + "Fetch");
		if (rgprovFetch == null) {
			rgprovFetch = mprov.createResource(rgprovNamespace + "Fetch");
		}
		
		fetch.addProperty(RDF.type, rgprovFetch);
		Property accessed = mprov.getProperty(spaceName + "accessed");
		if (accessed == null) {
			accessed = mprov.createProperty(spaceName + "accessed");
		}
		
		Property copied = mprov.getProperty(rgprovNamespace + "copied");
		if (copied == null) {
			copied = mprov.createProperty(rgprovNamespace + "copied");
		}
		
		Property wasCopyResult = mprov.getProperty(rgprovNamespace + "wasCopyResult");
		if (wasCopyResult == null) {
			wasCopyResult = mprov.createProperty(rgprovNamespace + "wasCopyResult");
		}
		Property wasExactCopy = mprov.getProperty(rgprovNamespace + "wasExactCopy");
		if (wasExactCopy == null) {
			wasExactCopy = mprov.createProperty(rgprovNamespace + "wasExactCopy");
		}
		
		if (whichGraph.equalsIgnoreCase("A1")) {
			Resource graphA1 = mprov.createResource(p.getGraphA1_URI());
			fetch.addProperty(accessed, p.getGraphA1_URI());
			fetch.addProperty(copied,graphA1);
			Resource graphA1copy = mprov.createResource(p.getGraphA1_COPY_NAME());
			graphA1copy.addProperty(wasCopyResult, fetch);
			graphA1copy.addProperty(wasExactCopy, graphA1);
			
			p.setGraphA1_PROV_MODEL(mprov);
		} else if (whichGraph.equalsIgnoreCase("B2")) {
			Resource graphB2 = mprov.createResource(p.getGraphB2_URI());
			fetch.addProperty(accessed, p.getGraphB2_URI());
			fetch.addProperty(copied,graphB2);
			Resource graphB2copy = mprov.createResource(p.getGraphB2_COPY_NAME());
			graphB2copy.addProperty(wasCopyResult, fetch);
			graphB2copy.addProperty(wasExactCopy, graphB2);
			
			p.setGraphB2_PROV_MODEL(mprov);
		} else {
			System.out.println("You passed the wrong source name graph");
			throw new IllegalArgumentException("Wrong source name graph");
		}
		
	}
	
	//this method should be deleted once the provenance handler has been updated
	public static String createProvOfSourceName(String graphName, String timeFetched) {
		String mprovName = "Pstar_copy_" + graphName + "-" + timeFetched; 
		return mprovName;
	}
	//this method should be deleted once the provenance handler has been updated
	public static String createProvOfUpdateName(String graphName, String timeFetched) {
		String mprovName = "Pstar_copy_" + graphName + "_updated-" + timeFetched; 
		return mprovName;
	}
	
	public static String createNameOfSTOp (String graphA1Name, String graphB2Name, String op, String timeCalled) {
		
		if (op.equalsIgnoreCase("union")){
			String nameOfSTOpGraph = "gu-" + graphA1Name + "-" + graphB2Name + "-" + timeCalled;
			return nameOfSTOpGraph;
		} else if (op.equalsIgnoreCase("intersection")){
			String nameOfSTOpGraph = "gi-" + graphA1Name + "-" + graphB2Name + "-" + timeCalled;
			return nameOfSTOpGraph;
		} else if (op.equalsIgnoreCase("difference")){
			String nameOfSTOpGraph = "gd-" + graphA1Name + "-" + graphB2Name + "-" + timeCalled;
			return nameOfSTOpGraph;
		} else {
			System.out.println("Error: Invalid Set theoretic operation! Cannot create a name for ST Op Graph");
			return null;
		}
		
	}
	 
	//this method should be deleted once the provenance handler has been updated
	public static void updateC3SetOperation (String spaceName, String stOpActivityName,  Producer p) {
			
		Model mprov = p.getGraphC3_PROV_MODEL();
		Resource Jena =  mprov.createResource( spaceName + "Jena");
		Resource jena3111 = mprov.createResource(spaceName + "jena3.1.1");
		jena3111.addProperty(RDF.type, Jena);
		Property wasAssociatedWith = mprov.createProperty(provNamespace + "wasAssociatedWith");
		Property used =  mprov.createProperty(provNamespace + "Used");
		Property wasGeneratedBy = mprov.createProperty(provNamespace + "wasGeneratedBy");
		Property wasDerivedFrom = mprov.createProperty(provNamespace + "wasDerivedFrom");
		Resource gOpC3 = mprov.createResource(p.getGraphSTA1B2_NAME());
		Resource gcopyA1 = mprov.getResource(p.getGraphA1_COPY_NAME());
		Resource gcopyB2 = mprov.getResource(p.getGraphB2_COPY_NAME());
		gOpC3.addProperty(wasDerivedFrom, gcopyA1);
		gOpC3.addProperty(wasDerivedFrom, gcopyB2);
		
		String stOp = p.getGraphStOpType();
		if (stOp.equalsIgnoreCase("union")) {
			Resource gu = mprov.createResource(stOpActivityName);
			Resource rgprovUnion = mprov.createResource(rgprovNamespace + "Union");
			gu.addProperty(RDF.type, rgprovUnion);
			gu.addProperty(wasAssociatedWith, jena3111);
			gu.addProperty(used, mprov.getResource(p.getGraphA1_COPY_NAME()));
			gu.addProperty(used, mprov.getResource(p.getGraphB2_COPY_NAME()));
			gOpC3.addProperty(wasGeneratedBy, gu);
		}
		else if (stOp.equalsIgnoreCase("intersection")) {
			Resource gi = mprov.createResource(stOpActivityName);
			Resource rgprovIntersection = mprov.createResource(rgprovNamespace + "Intersection");
			gi.addProperty(RDF.type, rgprovIntersection);
			gi.addProperty(wasAssociatedWith, jena3111);
			gi.addProperty(used, mprov.getResource(p.getGraphA1_COPY_NAME()));
			gi.addProperty(used, mprov.getResource(p.getGraphB2_COPY_NAME()));
			gOpC3.addProperty(wasGeneratedBy, gi);
			
		} else if (stOp.equalsIgnoreCase("difference1")) {
			Resource gd = mprov.createResource(stOpActivityName);
			Resource rgprovDifference = mprov.createResource(rgprovNamespace + "Difference");
			gd.addProperty(RDF.type, rgprovDifference);
			gd.addProperty(wasAssociatedWith, jena3111);
			Property hadMinuend = mprov.createProperty(rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, mprov.getResource(p.getGraphA1_COPY_NAME()));
			gd.addProperty(hadSubtrahend, mprov.getResource(p.getGraphB2_COPY_NAME()));
			gOpC3.addProperty(wasGeneratedBy, gd);
			
		} else if (stOp.equalsIgnoreCase("difference2")) {
			Resource gd = mprov.createResource(stOpActivityName);
			Resource rgprovDifference = mprov.createResource(rgprovNamespace + "Difference");
			gd.addProperty(RDF.type, rgprovDifference);
			gd.addProperty(wasAssociatedWith, jena3111);
			Property hadMinuend = mprov.createProperty(rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, mprov.getResource(p.getGraphB2_COPY_NAME()));
			gd.addProperty(hadSubtrahend, mprov.getResource(p.getGraphA1_COPY_NAME()));
			gOpC3.addProperty(wasGeneratedBy, gd);
			
		}	
		p.setGraphC3_PROV_MODEL(mprov);
	}
	
	//this method should be deleted once the provenance handler has been updated
	public static String createNameOfEntailOp (String graphC3Name, String timeCalled) {
		return  "ge-"+ graphC3Name + "-" + timeCalled;
	}
	
	//this method should be deleted once the provenance handler has been updated
	public static void updateC3Entailment (String spaceName, String entailActivityName, Producer p) {
		Model mprov = p.getGraphC3_PROV_MODEL();
		Resource jena3111 = mprov.getResource("jena3.1.1");
		Property wasAssociatedWith = mprov.getProperty(provNamespace + "wasAssociatedWith");
		Property used =  mprov.getProperty(provNamespace + "Used");
		Property wasGeneratedBy = mprov.getProperty(rgprovNamespace + "wasGeneratedBy");
		Property wasDerivedFrom = mprov.getProperty(rgprovNamespace + "wasDerivedFrom");
		Resource gcopyA1 = mprov.getProperty(p.getGraphA1_COPY_NAME());
		Resource gcopyB2 = mprov.getProperty(p.getGraphB2_COPY_NAME());
		Resource gOpC3 = mprov.getResource(p.getGraphSTA1B2_NAME());
		
		Resource ge = mprov.createResource(entailActivityName);
		Resource rdfsEntailment = mprov.createResource(rgprovNamespace + "RDFSEntailment");
		ge.addProperty(RDF.type, rdfsEntailment);
		ge.addProperty(wasAssociatedWith, jena3111);
		ge.addProperty(used, gOpC3);
		
		Resource gC3 = mprov.createResource(p.getGraphC3_NAME());
		gC3.addProperty(wasGeneratedBy, ge);
		Property wasEntailedFrom = mprov.createProperty(rgprovNamespace + "wasEntailedFrom");
		gC3.addProperty(wasEntailedFrom, gOpC3);
		
		gC3.addProperty(wasDerivedFrom, gcopyA1);
		gC3.addProperty(wasDerivedFrom, gcopyB2);
		
		p.setGraphC3_PROV_MODEL(mprov);
	}
		
	//this method should be deleted once the provenance handler has been updated
	public static void createProvOfUpdatedGraphB2 (String spaceName, String graphNametimeFetched, Producer p) {
		Model mprov = ModelFactory.createDefaultModel().add(p.getGraphB2_PROV_MODEL());
		Resource jersey225 = mprov.getResource(spaceName + "jersey225");
		Resource RESTClient = mprov.getResource(spaceName + "RESTClient");
				
		if (jersey225 == null && RESTClient == null) {
			jersey225 =  mprov.createResource( spaceName + "jersey225");
			RESTClient = mprov.createResource(spaceName + "RESTClient");
			jersey225.addProperty(RDF.type, RESTClient);
		}
		
		// add to the passed model the triples that describe its fetching as per rgprov
		Resource fetch = mprov.createResource(spaceName + "fetch" + graphNametimeFetched);
		Property wasAssociatedWith = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (wasAssociatedWith == null) {
			wasAssociatedWith = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		fetch.addProperty(wasAssociatedWith, jersey225);
		
		Resource rgprovFetch = mprov.getResource(rgprovNamespace + "Fetch");
		if (rgprovFetch == null) {
			rgprovFetch= mprov.createResource(rgprovNamespace + "Fetch");
		}
		fetch.addProperty(RDF.type, rgprovFetch);
		Property accessed = mprov.getProperty(spaceName + "accessed");
		if (accessed == null) {
			accessed = mprov.createProperty(spaceName + "accessed");
		}
		fetch.addProperty(accessed, p.getGraphB2Update_URI());
		Property copied = mprov.getProperty(rgprovNamespace + "copied");
		if (copied == null) {
			copied = mprov.createProperty(rgprovNamespace + "copied");
		}
		
		Property wasCopyResult = mprov.getProperty(rgprovNamespace + "wasCopyResult");
		if (wasCopyResult == null) {
			wasCopyResult = mprov.createProperty(rgprovNamespace + "wasCopyResult");
		}
		Property wasExactCopy = mprov.getProperty(rgprovNamespace + "wasExactCopy");
		if (wasExactCopy == null) {
			wasExactCopy = mprov.createProperty(rgprovNamespace + "wasExactCopy");
		}
		
		Resource graphB2update = mprov.createResource(p.getGraphB2Update_URI());
		fetch.addProperty(accessed, p.getGraphB2Update_URI());
		fetch.addProperty(copied,graphB2update);
		Resource graphB2updateCopy = mprov.createResource(p.getGraphB2Update_COPY_NAME());
		graphB2updateCopy.addProperty(wasCopyResult, fetch);
		graphB2updateCopy.addProperty(wasExactCopy, graphB2update);
		
		Property wasRevisionOf = mprov.getProperty(provNamespace + "wasRevisionOf");
		if (wasRevisionOf == null) {
			wasRevisionOf = mprov.createProperty(provNamespace + "wasRevisionOf");
		}
		Resource graphB2 = mprov.getResource(p.getGraphB2_URI());
		Resource graphB2prime = mprov.createResource(p.getGraphB2prime_URI());
		graphB2prime.addProperty(wasRevisionOf, graphB2);
		
		Resource provPrimeB2prime = mprov.createResource(p.getGraphB2prime_PROV_URI());
		Resource provB2 = mprov.getResource(p.getGraphB2_PROV_URI());
		provPrimeB2prime.addProperty(wasRevisionOf, provB2);
		
		//P'copy(B',2) is a new version of P'copy(B,2) , this creates the triple:
		//P'copy(B',2 prov:wasRevisionOf P'copy(B,2) .
		
		p.setGraphB2prime_PROV_MODEL(mprov);
		p.setGraphC3_PROV_MODEL(p.getGraphC3_PROV_MODEL().union(mprov));
	}
}
