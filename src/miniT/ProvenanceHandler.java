package miniT;

import operators.ParentOperator;
import operators.UpdatedOperator;

import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import utilities.graph.*;

public class ProvenanceHandler {
	
	private static String rgprovNamespace = "http://www.ecs.soton.ac.uk/rgprov#";
	private static String provNamespace = "http://www.w3.org/ns/prov#";
	private static String egNamespace = "http://example.org/";
	private static String egPrefix = "";
	private static String inNamespace = "http://www.ecs.soton.ac.uk/inaja#";
	private static String inPrefix = "inaja";
	
	/**
	 * This method creates the triples that represent the provenance of a copied source graph as per RGPROV
	 * @param graphNametimeFetched: a String indicating the name of the graph
	 * @param sg: a String indicating the name of the source graph in the system
	 * @param timeStarted
	 * @param timeEnded
	 */
	public static void createProvOfSourceGraph (String graphNametimeFetched, SourceGraphInSystem sg, String timeStarted[], String timeEnded[]) 
	{
		Model mprov = sg.getGraph_PROV_star_COPY_MODEL(); 
		mprov.setNsPrefix(inPrefix, inNamespace);	
		//mprov.setNsPrefix(egPrefix, egNamespace);	
		Resource rgprovGraphClass = mprov.getResource(rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(rgprovNamespace + "Graph");
		}
		Resource RESTClient = mprov.getResource(inNamespace + "RESTClient");
		if (RESTClient == null) {
			RESTClient = mprov.createResource(inNamespace + "RESTClient");
		}
		Resource jersey225 = mprov.getResource(inNamespace + "jersey225");
		if (jersey225 == null) {
			jersey225 =  mprov.createResource(inNamespace + "jersey225");
			jersey225.addProperty(RDF.type, RESTClient);
		}
		
		
		// add to the passed model the triples that describe its fetching as per rgprov
		Resource rgprovFetchClass = mprov.getResource(rgprovNamespace + "Fetch");
		if (rgprovFetchClass == null) {
			rgprovFetchClass = mprov.createResource(rgprovNamespace + "Fetch");
		}
		Resource fetch = mprov.createResource(inNamespace + "fetch" + graphNametimeFetched+timeStarted[2]);
		fetch.addProperty(RDF.type, rgprovFetchClass);
		
		Property provWasAssociatedWithProperty = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(provNamespace + "EndedAtTime");
		}
		
		fetch.addProperty(provWasAssociatedWithProperty, jersey225);
		fetch.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
		fetch.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		
		//may change this to prov:used
		Property accessedProperty = mprov.getProperty(inNamespace + "accessed");
		if (accessedProperty == null) {
			accessedProperty = mprov.createProperty(inNamespace + "accessed");
		}		
		Property rgprovCopiedProperty = mprov.getProperty(rgprovNamespace + "copied");
		if (rgprovCopiedProperty == null) {
			rgprovCopiedProperty = mprov.createProperty(rgprovNamespace + "copied");
		}		
		Property rgprovWasCopyResultProperty = mprov.getProperty(rgprovNamespace + "wasCopyResult");
		if (rgprovWasCopyResultProperty == null) {
			rgprovWasCopyResultProperty = mprov.createProperty(rgprovNamespace + "wasCopyResult");
		}
		Property rgprovWasExactCopyProperty = mprov.getProperty(rgprovNamespace + "wasExactCopy");
		if (rgprovWasExactCopyProperty == null) {
			rgprovWasExactCopyProperty = mprov.createProperty(rgprovNamespace + "wasExactCopy");
		}
		
		Resource graphOriginalNameResource = mprov.getResource(egNamespace + sg.getGraph_source_NAME());
		if (graphOriginalNameResource == null) {
			graphOriginalNameResource = mprov.createResource(egNamespace + sg.getGraph_source_NAME());
		}
		Resource graphCopyNameResource = mprov.getResource(inNamespace + sg.getGraph_COPY_NAME());
		if (graphCopyNameResource == null) {
			graphCopyNameResource = mprov.createResource(inNamespace + sg.getGraph_COPY_NAME());
		}
		graphCopyNameResource.addProperty(RDF.type, rgprovGraphClass);
		fetch.addProperty(accessedProperty, sg.getGraph_source_URI());
		fetch.addProperty(rgprovCopiedProperty, graphOriginalNameResource);
		graphCopyNameResource.addProperty(rgprovWasExactCopyProperty, fetch);
		graphCopyNameResource.addProperty(rgprovWasExactCopyProperty, graphOriginalNameResource); 
		
		Resource graphProvCopyResource = mprov.createResource(inNamespace +  sg.getGraph_PROV_COPY_NAME());
		graphProvCopyResource.addProperty(RDF.type, rgprovGraphClass);
		Resource graphProvStarCopyResource = mprov.createResource(inNamespace +  sg.getGraph_PROV_star_COPY_NAME());
		graphProvStarCopyResource.addProperty(RDF.type, rgprovGraphClass);
		Property provWasDerivedFromProperty = mprov.getProperty(provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(provNamespace + "wasDerivedFrom");
		}
		graphProvStarCopyResource.addProperty(provWasDerivedFromProperty, graphProvCopyResource);
		sg.setGraph_PROV_star_COPY_MODEL(mprov); //this is redundant but i am being paranoid
	}
	
	/**
	 * This method creates, as per RGPROV, the triples that represent the provenance of a graph in the system that
	 * uses two copied source graphs. 
	 * @param unionOfTwoInSysSourceProvs: the union of the provenances of the two sources graphs
	 * @param c3provName: the name of the provenance graph of the graph in the system
	 * @param a1provStarCopyName: the name of the provenance graph of the source graph A1 in the system 
	 * 			after being edited to reflect the fetch operation
	 * @param b2provStarCopyName: the name of the provenance graph of the source graph B2 in the system 
	 * 			after being edited to reflect the fetch operation
	 * @return
	 */
	public static Model createInitialC3Prov(Model unionOfTwoInSysSourceProvs, String c3provName, String a1provStarCopyName, String b2provStarCopyName) {
		Model mresults = unionOfTwoInSysSourceProvs;
		Resource rgprovGraphClass = mresults.getResource(rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mresults.createResource(rgprovNamespace + "Graph");
		}
		Property provWasDerivedFromProperty = mresults.getProperty(provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mresults.createProperty(provNamespace + "wasDerivedFrom");
		}
		Resource a1provStarCopyNameResource = mresults.getResource(inNamespace + a1provStarCopyName);
		if (a1provStarCopyNameResource == null) {
			a1provStarCopyNameResource = mresults.createResource(inNamespace + a1provStarCopyName);
		}
		Resource b2provStarCopyNameResource = mresults.getResource(inNamespace + b2provStarCopyName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(inNamespace + b2provStarCopyName);
		}
		Resource c3provNameResource = mresults.getResource(inNamespace + c3provName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(inNamespace + c3provName);
		}
		c3provNameResource.addProperty(RDF.type, rgprovGraphClass);
		c3provNameResource.addProperty(provWasDerivedFromProperty, a1provStarCopyNameResource);
		c3provNameResource.addProperty(provWasDerivedFromProperty, b2provStarCopyNameResource);
		
		return mresults;
	}
	
	/** This method creates, as per RGPROV, the triples that represent the provenance of a graph in the system that
	 * was created using a graph set operation on two other graphs
	 * @param stOpActivityName: the name to be given to the resource that represents the set theoretic operation
	 * @param po: Operator or TraditionalOperator
	 * @param timeStarted
	 * @param timeEnded
	 */
	public static void updateC3SetOperation (String stOpActivityName,  ParentOperator po, String timeStarted[], String timeEnded[])
	{
		Model mprov = po.getC3().getGraph_PROV_MODEL();
		Resource rgprovRDFSReasonerClass = mprov.getResource(rgprovNamespace + "RDFSReasoner");
		if (rgprovRDFSReasonerClass == null) {
			rgprovRDFSReasonerClass =  mprov.createResource(rgprovNamespace + "RDFSReasoner");
		}
		Resource Jena = mprov.getResource(egNamespace + "Jena");
		if (Jena == null) {
			Jena =  mprov.createResource(egNamespace + "Jena");
			Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
		}
		Resource jena3111 = mprov.getResource(egNamespace + "jena3.1.1");
		if (jena3111 == null) {
			jena3111 =  mprov.createResource(egNamespace + "jena3.1.1");
			jena3111.addProperty(RDF.type, Jena);
		}
		Resource rgprovGraphOpClass = mprov.getResource(rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(rgprovNamespace + "GraphOperation");
		}
		Resource rgprovGraphClass = mprov.getResource(rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(rgprovNamespace + "Graph");
		}
		Property provWasAssociatedWithProperty = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(provNamespace + "EndedAtTime");
		}

		Resource gOpC3 = mprov.createResource(inNamespace + po.getGraphSTA1B2_NAME());
		gOpC3.addProperty(RDF.type, rgprovGraphClass);
		Resource gcopyA1 = mprov.getResource(inNamespace + po.getA1().getGraph_COPY_NAME());
		Resource gcopyB2 = mprov.getResource(inNamespace + po.getB2().getGraph_COPY_NAME());
		gOpC3.addProperty(provWasDerivedFromProperty, gcopyA1);
		gOpC3.addProperty(provWasDerivedFromProperty, gcopyB2);
		
		String stOp = po.getGraphStOpType();
		if (stOp.equalsIgnoreCase("union")) 
		{
			Resource rgprovUnionResource = mprov.getResource(rgprovNamespace + "Union");
			if (rgprovUnionResource == null) {
				rgprovUnionResource = mprov.createResource(rgprovNamespace + "Union");
				rgprovUnionResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource gu = mprov.createResource(inNamespace + stOpActivityName+timeStarted[2]);
			gu.addProperty(RDF.type, rgprovUnionResource);
			gu.addProperty(provWasAssociatedWithProperty, jena3111);
			gu.addProperty(provUsedProperty, gcopyA1);
			gu.addProperty(provUsedProperty, gcopyB2);
			gOpC3.addProperty(provWasGeneratedByProperty, gu);
			gu.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			gu.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		}
		else if (stOp.equalsIgnoreCase("intersection")) 
		{
			Resource rgprovIntersectionResource = mprov.getResource(rgprovNamespace + "Intersection");
			if (rgprovIntersectionResource == null) {
				rgprovIntersectionResource = mprov.createResource(rgprovNamespace + "Intersection");
				rgprovIntersectionResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource gi = mprov.createResource(inNamespace + stOpActivityName+timeStarted[2]);
			gi.addProperty(RDF.type, rgprovIntersectionResource);
			gi.addProperty(provWasAssociatedWithProperty, jena3111);
			gi.addProperty(provUsedProperty, gcopyA1);
			gi.addProperty(provUsedProperty, gcopyB2);
			gOpC3.addProperty(provWasGeneratedByProperty, gi);
			gi.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			gi.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
			
		} 
		else if (stOp.equalsIgnoreCase("difference1")) 
		{
			Resource rgprovDifferencenResource = mprov.getResource(rgprovNamespace + "Difference");
			if (rgprovDifferencenResource == null) {
				rgprovDifferencenResource = mprov.createResource(rgprovNamespace + "Difference");
				rgprovDifferencenResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource gd = mprov.createResource(inNamespace + stOpActivityName+timeStarted[2]);
			
			gd.addProperty(RDF.type, rgprovDifferencenResource);
			gd.addProperty(provWasAssociatedWithProperty, jena3111);
			Property hadMinuend = mprov.createProperty(rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, gcopyA1);
			gd.addProperty(hadSubtrahend, gcopyB2);
			gOpC3.addProperty(provWasGeneratedByProperty, gd);
			gd.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			gd.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
			
		} 
		else if (stOp.equalsIgnoreCase("difference2")) 
		{
			Resource rgprovDifferencenResource = mprov.getResource(rgprovNamespace + "Difference");
			if (rgprovDifferencenResource == null) {
				rgprovDifferencenResource = mprov.createResource(rgprovNamespace + "Difference");
				rgprovDifferencenResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			
			Resource gd = mprov.createResource(inNamespace + stOpActivityName+timeStarted[2]);
			gd.addProperty(RDF.type, rgprovDifferencenResource);
			gd.addProperty(provWasAssociatedWithProperty, jena3111);
			Property hadMinuend = mprov.createProperty(rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, gcopyB2);
			gd.addProperty(hadSubtrahend, gcopyA1);
			gOpC3.addProperty(provWasGeneratedByProperty, gd);
			gd.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			gd.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		}	
		//setGraphC3_PROV_MODEL(mprov);
	}
	
	public static void updateC3Entailment (String entailActivityName, ParentOperator po, String timeStarted[], String timeEnded[]) {
		Model mprov = po.getC3().getGraph_PROV_MODEL();
		
		Resource rgprovRDFSReasonerClass = mprov.getResource(rgprovNamespace + "RDFSReasoner");
		if (rgprovRDFSReasonerClass == null) {
			rgprovRDFSReasonerClass =  mprov.createResource(rgprovNamespace + "RDFSReasoner");
		}
		Resource Jena = mprov.getResource(egNamespace + "Jena");
		if (Jena == null) {
			Jena =  mprov.createResource(egNamespace + "Jena");
			Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
		}
		Resource jena3111 = mprov.getResource(egNamespace + "jena3.1.1");
		if (jena3111 == null) {
			jena3111 =  mprov.createResource(egNamespace + "jena3.1.1");
			jena3111.addProperty(RDF.type, Jena);
		}
		Resource rgprovGraphClass = mprov.getResource(rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(rgprovNamespace + "Graph");
		}
		Resource rgprovGraphOpClass = mprov.getResource(rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(rgprovNamespace + "GraphOperation");
		}
		Resource rgprovEntailmentClass = mprov.getResource(rgprovNamespace + "Entailment");
		if (rgprovEntailmentClass == null) {
			rgprovEntailmentClass =  mprov.createResource(rgprovNamespace + "Entailment");
			rgprovEntailmentClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
		}
		Resource rgprovRDFSEntailmentClass = mprov.getResource(rgprovNamespace + "RDFSEntailment");
		if (rgprovRDFSEntailmentClass == null) {
			rgprovRDFSEntailmentClass =  mprov.createResource(rgprovNamespace + "RDFSEntailment");
			rgprovRDFSEntailmentClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
		}
		
		Property provWasAssociatedWithProperty = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(provNamespace + "EndedAtTime");
		}
		Resource gcopyA1 = mprov.getResource(inNamespace + po.getA1().getGraph_COPY_NAME());
		Resource gcopyB2 = mprov.getResource(inNamespace + po.getB2().getGraph_COPY_NAME());
		Resource gOpC3 = mprov.getResource(inNamespace + po.getGraphSTA1B2_NAME());
		
		
		
		Resource ge = mprov.createResource(inNamespace + entailActivityName);
		ge.addProperty(RDF.type, rgprovRDFSEntailmentClass);
		ge.addProperty(provWasAssociatedWithProperty, jena3111);
		ge.addProperty(provUsedProperty, gOpC3);
		ge.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
		ge.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		
		Resource gC3 = mprov.createResource(inNamespace + po.getC3().getGraph_NAME());
		gC3.addProperty(RDF.type, rgprovGraphClass);
		gC3.addProperty(provWasGeneratedByProperty, ge);
		Property rgprovWasEntailedFromProperty = mprov.getProperty(rgprovNamespace + "wasEntailedFrom");
		if (rgprovWasEntailedFromProperty == null) {
			rgprovWasEntailedFromProperty = mprov.createProperty(rgprovNamespace + "wasEntailedFrom");
		}
		gC3.addProperty(rgprovWasEntailedFromProperty, gOpC3);
		
		gC3.addProperty(provWasDerivedFromProperty, gcopyA1);
		gC3.addProperty(provWasDerivedFromProperty, gcopyB2);
		
		//p.setGraphC3_PROV_MODEL(mprov);
	}
	
	
	public static void updateProvOfSourceGraphAfterLoadingUpdate(String graphNametimeFetched, UpdatedSourceGraphInSystem usg, 
																	String timeStarted[], String timeEnded[]) 
	{
			Model mprov = usg.getGraphprime_PROV_star_COPY_MODEL();
			mprov.setNsPrefix(inPrefix, inNamespace);
			
			Resource rgprovGraphClass = mprov.getResource(rgprovNamespace + "Graph");
			if (rgprovGraphClass == null) {
				rgprovGraphClass =  mprov.createResource(rgprovNamespace + "Graph");
			}
			Resource rgprovUpdateGraphClass = mprov.getResource(rgprovNamespace + "UpdateGraph");
			if (rgprovUpdateGraphClass == null) {
				rgprovUpdateGraphClass =  mprov.createResource(rgprovNamespace + "UpdateGraph");
				rgprovUpdateGraphClass.addProperty(RDFS.subClassOf, rgprovGraphClass);
			}
			Resource RESTClient = mprov.getResource(inNamespace + "RESTClient");
			if (RESTClient == null) {
				RESTClient = mprov.createResource(inNamespace + "RESTClient");
			}
			Resource jersey225 = mprov.getResource(inNamespace + "jersey225");
			if (jersey225 == null) {
				jersey225 =  mprov.createResource(inNamespace + "jersey225");
				jersey225.addProperty(RDF.type, RESTClient);
			}
			
			// add to the passed model the triples that describe its fetching as per rgprov
			Resource rgprovFetchResource = mprov.getResource(rgprovNamespace + "Fetch");
			if (rgprovFetchResource == null) {
				rgprovFetchResource = mprov.createResource(rgprovNamespace + "Fetch");
			}
			Resource fetch = mprov.createResource(inNamespace + "fetch" + graphNametimeFetched+timeStarted[2]);
			fetch.addProperty(RDF.type, rgprovFetchResource);
			
			Property provWasAssociatedWithProperty = mprov.getProperty(provNamespace + "wasAssociatedWith");
			if (provWasAssociatedWithProperty == null) {
				provWasAssociatedWithProperty = mprov.createProperty(provNamespace + "wasAssociatedWith");
			}
			Property provStartedAtTimeProperty = mprov.getProperty(provNamespace + "startedAtTime");
			if (provStartedAtTimeProperty == null) {
				provStartedAtTimeProperty = mprov.createProperty(provNamespace + "startedAtTime");
			}
			Property provEndedAtTimeProperty = mprov.getProperty(provNamespace + "EndedAtTime");
			if (provEndedAtTimeProperty == null) {
				provEndedAtTimeProperty = mprov.createProperty(provNamespace + "EndedAtTime");
			}
			
			fetch.addProperty(provWasAssociatedWithProperty, jersey225);
			fetch.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			fetch.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
			
			//may change this to prov:used
			Property accessedProperty = mprov.getProperty(inNamespace + "accessed");
			if (accessedProperty == null) {
				accessedProperty = mprov.createProperty(inNamespace + "accessed");
			}
			Property rgprovCopiedProperty = mprov.getProperty(rgprovNamespace + "copied");
			if (rgprovCopiedProperty == null) {
				rgprovCopiedProperty = mprov.createProperty(rgprovNamespace + "copied");
			}
			Property rgprovWasCopyResultProperty = mprov.getProperty(rgprovNamespace + "wasCopyResult");
			if (rgprovWasCopyResultProperty == null) {
				rgprovWasCopyResultProperty = mprov.createProperty(rgprovNamespace + "wasCopyResult");
			}
			Property rgprovWasExactCopyProperty = mprov.getProperty(rgprovNamespace + "wasExactCopy");
			if (rgprovWasExactCopyProperty == null) {
				rgprovWasExactCopyProperty = mprov.createProperty(rgprovNamespace + "wasExactCopy");
			}
			
			Resource updateGraphOriginalNameResource = mprov.getResource(egNamespace + usg.getUpdateGraph_source_NAME());
			if (updateGraphOriginalNameResource == null) {
				updateGraphOriginalNameResource = mprov.createResource(egNamespace + usg.getUpdateGraph_source_NAME());
			}
			Resource updateGraphCopyNameResource = mprov.getResource(inNamespace + usg.getUpdateGraph_COPY_NAME());
			if (updateGraphCopyNameResource == null) {
				updateGraphCopyNameResource = mprov.createResource(inNamespace + usg.getUpdateGraph_COPY_NAME());
			}
			
			updateGraphOriginalNameResource.addProperty(RDF.type, rgprovUpdateGraphClass);
			updateGraphCopyNameResource.addProperty(RDF.type, rgprovUpdateGraphClass);
			
			fetch.addProperty(accessedProperty, usg.getUpdateGraph_URI());
			fetch.addProperty(rgprovCopiedProperty,updateGraphOriginalNameResource);
			updateGraphCopyNameResource.addProperty(rgprovWasCopyResultProperty, fetch);
			updateGraphCopyNameResource.addProperty(rgprovWasExactCopyProperty, updateGraphOriginalNameResource);
			
			Resource graphPrimeProvCopyResource = mprov.createResource(inNamespace +  usg.getGraphprime_PROV_COPY_NAME());		
			graphPrimeProvCopyResource.addProperty(RDF.type, rgprovGraphClass);
			
			Property wasRevisionOf = mprov.getProperty(provNamespace + "wasRevisionOf");
			if (wasRevisionOf == null) {
				wasRevisionOf = mprov.createProperty(provNamespace + "wasRevisionOf");
			}
			// THIS NEEDS FIXING
			
			//P'copy(B',2) is a new version of P'copy(B,2) , this creates the triple:
			//P'copy(B',2 prov:wasRevisionOf P'copy(B,2) .
	//		Resource provPrimeB2prime = mprov.createResource(p.getGraphB2prime_PROV_URI());
	//		Resource provB2 = mprov.getResource(p.getGraphB2_PROV_URI());
	//		provPrimeB2prime.addProperty(wasRevisionOf, provB2);
			
			usg.setGraphprime_PROV_star_COPY_MODEL(mprov);
			
		}
	public static Model updateC3ProvAfterLoadingUpdate(Model c3PreviousProv, String c3provName, String b2provStarCopyName) {
		Model mresults = c3PreviousProv;
		Resource rgprovGraphClass = mresults.getResource(rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mresults.createResource(rgprovNamespace + "Graph");
		}
		Property provWasDerivedFromProperty = mresults.getProperty(provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mresults.createProperty(provNamespace + "wasDerivedFrom");
		}
		/*Resource a1provStarCopyNameResource = mresults.getResource(inNamespace + a1provStarCopyName);
		if (a1provStarCopyNameResource == null) {
			a1provStarCopyNameResource = mresults.createResource(inNamespace + a1provStarCopyName);
		}*/
		Resource b2provStarCopyNameResource = mresults.getResource(inNamespace + b2provStarCopyName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(inNamespace + b2provStarCopyName);
		}
		Resource c3provNameResource = mresults.getResource(inNamespace + c3provName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(inNamespace + c3provName);
		}
		c3provNameResource.addProperty(RDF.type, rgprovGraphClass);
		//c3provNameResource.addProperty(provWasDerivedFromProperty, a1provStarCopyNameResource);
		c3provNameResource.addProperty(provWasDerivedFromProperty, b2provStarCopyNameResource);
		
		return mresults;
		
	}

	//this method updates the provenance of c prime to reflect the insertion/deletion of triples
	public static void updateC3ProvSetOperation (UpdatedOperator uo, String timeCalled) 
	{
		String graphUpdateType = uo.getGraphUpdateType();
		String queriedGraphStOpType = uo.getQueriedGraphStOpType();
		
		Model mprov = uo.getC3prime().getGraph_PROV_MODEL();
	
		Resource rgprovGraphClass = mprov.getResource(rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(rgprovNamespace + "Graph");
		}
		Resource rgprovUpdateGraphClass = mprov.getResource(rgprovNamespace + "UpdateGraph");
		if (rgprovUpdateGraphClass == null) {
			rgprovUpdateGraphClass =  mprov.createResource(rgprovNamespace + "UpdateGraph");
			rgprovUpdateGraphClass.addProperty(RDFS.subClassOf, rgprovGraphClass);
		}
		Resource rgprovGraphOpClass = mprov.getResource(rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(rgprovNamespace + "GraphOperation");
		}
		Property provWasAssociatedWithProperty = mprov.getProperty(provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(provNamespace + "EndedAtTime");
		}
		Resource gC3 = mprov.getResource(inNamespace + uo.getC3prime().getGraph_NAME());
		if (gC3 == null) {
			gC3 = mprov.createResource(inNamespace + uo.getC3prime().getGraph_NAME());
			gC3.addProperty(RDF.type, rgprovGraphClass);
		}
		Resource UpcopyResource = mprov.createResource(inNamespace + uo.getB2prime().getUpdateGraph_COPY_NAME());
		UpcopyResource.addProperty(RDF.type, rgprovUpdateGraphClass);
		Resource gOpC3prime = mprov.createResource(inNamespace + uo.getGraphSTA1B2prime_NAME());
		gOpC3prime.addProperty(RDF.type, rgprovGraphClass);
		gOpC3prime.addProperty(provWasDerivedFromProperty, UpcopyResource);
		gOpC3prime.addProperty(provWasDerivedFromProperty, gC3);
		
		if (graphUpdateType.equalsIgnoreCase("insert")) 
		{ 
			Resource rgprovInsertOpClass = mprov.getResource(rgprovNamespace + "InsertOperation");
			if (rgprovInsertOpClass == null) {
				rgprovInsertOpClass =  mprov.createResource(rgprovNamespace + "InsertOperation");
				rgprovInsertOpClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Property rgprovInsertedProperty = mprov.getProperty(rgprovNamespace + "inserted");
			if (rgprovInsertedProperty == null) {
				rgprovInsertedProperty = mprov.createProperty(rgprovNamespace + "inserted");
			}
			
			if (queriedGraphStOpType.equalsIgnoreCase("union")) 
			{
				Resource rgprovRDFSReasonerClass = mprov.getResource(rgprovNamespace + "RDFSReasoner");
				if (rgprovRDFSReasonerClass == null) {
					rgprovRDFSReasonerClass =  mprov.createResource(rgprovNamespace + "RDFSReasoner");
				}
				Resource Jena = mprov.getResource(egNamespace + "Jena");
				if (Jena == null) {
					Jena =  mprov.createResource(egNamespace + "Jena");
					Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
				}
				Resource jena3111 = mprov.getResource(egNamespace + "jena3.1.1");
				if (jena3111 == null) {
					jena3111 =  mprov.createResource(egNamespace + "jena3.1.1");
					jena3111.addProperty(RDF.type, Jena);
				}
				Resource insertIntoG3 = mprov.createResource(inNamespace + "insert" + uo.getC3prime().getWithoutTTL_Graph_NAME() + timeCalled);
				insertIntoG3.addProperty(RDF.type, rgprovInsertOpClass);
				insertIntoG3.addProperty(provWasAssociatedWithProperty, jena3111);
				insertIntoG3.addProperty(rgprovInsertedProperty, UpcopyResource);
				insertIntoG3.addProperty(provUsedProperty, gC3);
				gOpC3prime.addProperty(provWasGeneratedByProperty, insertIntoG3);
				insertIntoG3.addProperty(provStartedAtTimeProperty, timeCalled);
			}
			else
			{
				
			}
			
		}
		else // it is delete
		{
			Resource rgprovDeleteOpClass = mprov.getResource(rgprovNamespace + "DeleteOperation");
			if (rgprovDeleteOpClass == null) {
				rgprovDeleteOpClass =  mprov.createResource(rgprovNamespace + "DeleteOperation");
				rgprovDeleteOpClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Property rgprovDeletedProperty = mprov.getProperty(rgprovNamespace + "deleted");
			if (rgprovDeletedProperty == null) {
				rgprovDeletedProperty = mprov.createProperty(rgprovNamespace + "deleted");
			}
			if (queriedGraphStOpType.equalsIgnoreCase("union")) 
			{
				Resource rgprovRDFSReasonerClass = mprov.getResource(rgprovNamespace + "RDFSReasoner");
				if (rgprovRDFSReasonerClass == null) {
					rgprovRDFSReasonerClass =  mprov.createResource(rgprovNamespace + "RDFSReasoner");
				}
				Resource Jena = mprov.getResource(egNamespace + "Jena");
				if (Jena == null) {
					Jena =  mprov.createResource(egNamespace + "Jena");
					Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
				}
				Resource jena3111 = mprov.getResource(egNamespace + "jena3.1.1");
				if (jena3111 == null) {
					jena3111 =  mprov.createResource(egNamespace + "jena3.1.1");
					jena3111.addProperty(RDF.type, Jena);
				}
				Resource deleteFromC3 = mprov.createResource(inNamespace + "delete" + uo.getC3prime().getWithoutTTL_Graph_NAME() + timeCalled);
				deleteFromC3.addProperty(RDF.type, rgprovDeleteOpClass);
				// fix this: deleteFromC3.addProperty(provWasAssociatedWithProperty, jena3111);
				Resource subUpcopyResource = mprov.createResource(inNamespace + "SubsetOf" + uo.getB2prime().getUpdateGraph_COPY_NAME());
				subUpcopyResource.addProperty(RDF.type, rgprovUpdateGraphClass);
				subUpcopyResource.addProperty(provWasDerivedFromProperty, UpcopyResource);
				Resource gcopyA1 = mprov.getResource(inNamespace + uo.getA1().getGraph_COPY_NAME());
				subUpcopyResource.addProperty(provWasDerivedFromProperty, gcopyA1);
				deleteFromC3.addProperty(rgprovDeletedProperty, subUpcopyResource);
				deleteFromC3.addProperty(provUsedProperty, gC3);
				gOpC3prime.addProperty(provWasGeneratedByProperty, deleteFromC3);
				gOpC3prime.addProperty(provWasDerivedFromProperty, gcopyA1);
				gOpC3prime.addProperty(provWasDerivedFromProperty, subUpcopyResource);
				deleteFromC3.addProperty(provStartedAtTimeProperty, timeCalled);
			}
			else
			{
				
			}
			
		}
		
		
		
	/*		
		
		
		
		Resource gcopyB2 = mprov.getResource(p.getGraphB2_COPY_NAME());
		
		
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
		*/
	}
	
		/*public static void updateC3ProvEntailment (String spaceName, String entailActivityName, Producer p) {
		  //Resource gC3prime = mprov.createResource(inNamespace + uo.getC3prime().getGraph_UpdatedNAME()); 
		//gC3prime.addProperty(RDF.type, rgprovGraphClass);
		 
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
		}*/
			
		
		//this method should be deleted once the provenance handler has been updated
		/*	public static String createProvOfSourceName(String graphName, String timeFetched) {
				String mprovName = "Pstar_copy_" + graphName + "-" + timeFetched; 
				return mprovName;
			}*/
			//this method should be deleted once the provenance handler has been updated
		/*	public static String createProvOfUpdateName(String graphName, String timeFetched) {
				String mprovName = "Pstar_copy_" + graphName + "_updated-" + timeFetched; 
				return mprovName;
			}
		*/	
	
	//this method should be deleted once the provenance handler has been updated
	public static String createNameOfEntailOp (String graphC3Name, String timeCalled) {
		return  "ge-"+ graphC3Name + "-" + timeCalled;
	}

	public static String createNameOfSTOp (String graphA1Name, String graphB2Name, String op, String timeCalled) 
	{	
		if (op.equalsIgnoreCase("union")){
			String nameOfSTOpGraph = "gu-" + graphA1Name + "-" + graphB2Name + "-" + timeCalled;
			return nameOfSTOpGraph;
		} else if (op.equalsIgnoreCase("intersection")){
			String nameOfSTOpGraph = "gi-" + graphA1Name + "-" + graphB2Name + "-" + timeCalled;
			return nameOfSTOpGraph;
		} else if (op.equalsIgnoreCase("difference1") || op.equalsIgnoreCase("difference2")){
			String nameOfSTOpGraph = "gd-" + graphA1Name + "-" + graphB2Name + "-" + timeCalled;
			return nameOfSTOpGraph;
		} else {
			System.err.println("Error in Method createNameOfSTOp in Class ProvenanceHandler:"
							+ "\nInvalid Set theoretic operation! Cannot create a name for ST Op Graph");
			return null;
		}
		
	}
	
}
