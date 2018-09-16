package miniT;

import operators.ParentOperator;
import operators.UpdatedOperator;

import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import utilities.Constants;
import utilities.graph.*;

public class ProvenanceHandler 
{	
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
		mprov.setNsPrefix(Constants.customPrefix, Constants.customNameSpace);	
		//mprov.setNsPrefix(egPrefix, egNamespace);	
		Resource rgprovGraphClass = mprov.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(Constants.rgprovNamespace + "Graph");
		}
		Resource RESTClient = mprov.getResource(Constants.customNameSpace + "RESTClient");
		if (RESTClient == null) {
			RESTClient = mprov.createResource(Constants.customNameSpace + "RESTClient");
		}
		Resource jersey225 = mprov.getResource(Constants.customNameSpace + "jersey225");
		if (jersey225 == null) {
			jersey225 =  mprov.createResource(Constants.customNameSpace + "jersey225");
			jersey225.addProperty(RDF.type, RESTClient);
		}
		
		
		// add to the passed model the triples that describe its fetching as per rgprov
		Resource rgprovFetchClass = mprov.getResource(Constants.rgprovNamespace + "Fetch");
		if (rgprovFetchClass == null) {
			rgprovFetchClass = mprov.createResource(Constants.rgprovNamespace + "Fetch");
		}
		Resource fetch = mprov.createResource(Constants.customNameSpace + "fetch" + graphNametimeFetched+timeStarted[2]);
		fetch.addProperty(RDF.type, rgprovFetchClass);
		
		Property provWasAssociatedWithProperty = mprov.getProperty(Constants.provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(Constants.provNamespace + "wasAssociatedWith");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "EndedAtTime");
		}
		
		fetch.addProperty(provWasAssociatedWithProperty, jersey225);
		fetch.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
		fetch.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		
		//may change this to prov:used
		Property accessedProperty = mprov.getProperty(Constants.customNameSpace + "accessed");
		if (accessedProperty == null) {
			accessedProperty = mprov.createProperty(Constants.customNameSpace + "accessed");
		}		
		Property rgprovCopiedProperty = mprov.getProperty(Constants.rgprovNamespace + "copied");
		if (rgprovCopiedProperty == null) {
			rgprovCopiedProperty = mprov.createProperty(Constants.rgprovNamespace + "copied");
		}		
		Property rgprovWasCopyResultProperty = mprov.getProperty(Constants.rgprovNamespace + "wasCopyResult");
		if (rgprovWasCopyResultProperty == null) {
			rgprovWasCopyResultProperty = mprov.createProperty(Constants.rgprovNamespace + "wasCopyResult");
		}
		Property rgprovWasExactCopyProperty = mprov.getProperty(Constants.rgprovNamespace + "wasExactCopy");
		if (rgprovWasExactCopyProperty == null) {
			rgprovWasExactCopyProperty = mprov.createProperty(Constants.rgprovNamespace + "wasExactCopy");
		}
		
		Resource graphOriginalNameResource = mprov.getResource(Constants.egNamespace + sg.getGraph_source_NAME());
		if (graphOriginalNameResource == null) {
			graphOriginalNameResource = mprov.createResource(Constants.egNamespace + sg.getGraph_source_NAME());
		}
		Resource graphCopyNameResource = mprov.getResource(Constants.customNameSpace + sg.getGraph_COPY_NAME());
		if (graphCopyNameResource == null) {
			graphCopyNameResource = mprov.createResource(Constants.customNameSpace + sg.getGraph_COPY_NAME());
		}
		graphCopyNameResource.addProperty(RDF.type, rgprovGraphClass);
		fetch.addProperty(accessedProperty, sg.getGraph_source_URI());
		fetch.addProperty(rgprovCopiedProperty, graphOriginalNameResource);
		graphCopyNameResource.addProperty(rgprovWasExactCopyProperty, fetch);
		graphCopyNameResource.addProperty(rgprovWasExactCopyProperty, graphOriginalNameResource); 
		
		Resource graphProvCopyResource = mprov.createResource(Constants.customNameSpace +  sg.getGraph_PROV_COPY_NAME());
		graphProvCopyResource.addProperty(RDF.type, rgprovGraphClass);
		Resource graphProvStarCopyResource = mprov.createResource(Constants.customNameSpace +  sg.getGraph_PROV_star_COPY_NAME());
		graphProvStarCopyResource.addProperty(RDF.type, rgprovGraphClass);
		Property provWasDerivedFromProperty = mprov.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(Constants.provNamespace + "wasDerivedFrom");
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
		Resource rgprovGraphClass = mresults.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mresults.createResource(Constants.rgprovNamespace + "Graph");
		}
		Property provWasDerivedFromProperty = mresults.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mresults.createProperty(Constants.provNamespace + "wasDerivedFrom");
		}
		Resource a1provStarCopyNameResource = mresults.getResource(Constants.customNameSpace + a1provStarCopyName);
		if (a1provStarCopyNameResource == null) {
			a1provStarCopyNameResource = mresults.createResource(Constants.customNameSpace + a1provStarCopyName);
		}
		Resource b2provStarCopyNameResource = mresults.getResource(Constants.customNameSpace + b2provStarCopyName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(Constants.customNameSpace + b2provStarCopyName);
		}
		Resource c3provNameResource = mresults.getResource(Constants.customNameSpace + c3provName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(Constants.customNameSpace + c3provName);
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
		Resource rgprovRDFSReasonerClass = mprov.getResource(Constants.rgprovNamespace + "RDFSReasoner");
		if (rgprovRDFSReasonerClass == null) {
			rgprovRDFSReasonerClass =  mprov.createResource(Constants.rgprovNamespace + "RDFSReasoner");
		}
		Resource Jena = mprov.getResource(Constants.egNamespace + "Jena");
		if (Jena == null) {
			Jena =  mprov.createResource(Constants.egNamespace + "Jena");
			Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
		}
		Resource jena3111 = mprov.getResource(Constants.egNamespace + "jena3.1.1");
		if (jena3111 == null) {
			jena3111 =  mprov.createResource(Constants.egNamespace + "jena3.1.1");
			jena3111.addProperty(RDF.type, Jena);
		}
		Resource rgprovGraphOpClass = mprov.getResource(Constants.rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(Constants.rgprovNamespace + "GraphOperation");
		}
		Resource rgprovGraphClass = mprov.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(Constants.rgprovNamespace + "Graph");
		}
		Property provWasAssociatedWithProperty = mprov.getProperty(Constants.provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(Constants.provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(Constants.provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(Constants.provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(Constants.provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(Constants.provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(Constants.provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "EndedAtTime");
		}

		Resource gOpC3 = mprov.createResource(Constants.customNameSpace + po.getGraphSTA1B2_NAME());
		gOpC3.addProperty(RDF.type, rgprovGraphClass);
		Resource gcopyA1 = mprov.getResource(Constants.customNameSpace + po.getA1().getGraph_COPY_NAME());
		Resource gcopyB2 = mprov.getResource(Constants.customNameSpace + po.getB2().getGraph_COPY_NAME());
		gOpC3.addProperty(provWasDerivedFromProperty, gcopyA1);
		gOpC3.addProperty(provWasDerivedFromProperty, gcopyB2);
		
		String stOp = po.getGraphStOpType();
		if (stOp.equalsIgnoreCase("union")) 
		{
			Resource rgprovUnionResource = mprov.getResource(Constants.rgprovNamespace + "Union");
			if (rgprovUnionResource == null) {
				rgprovUnionResource = mprov.createResource(Constants.rgprovNamespace + "Union");
				rgprovUnionResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource gu = mprov.createResource(Constants.customNameSpace + stOpActivityName+timeStarted[2]);
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
			Resource rgprovIntersectionResource = mprov.getResource(Constants.rgprovNamespace + "Intersection");
			if (rgprovIntersectionResource == null) {
				rgprovIntersectionResource = mprov.createResource(Constants.rgprovNamespace + "Intersection");
				rgprovIntersectionResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource gi = mprov.createResource(Constants.customNameSpace + stOpActivityName+timeStarted[2]);
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
			Resource rgprovDifferencenResource = mprov.getResource(Constants.rgprovNamespace + "Difference");
			if (rgprovDifferencenResource == null) {
				rgprovDifferencenResource = mprov.createResource(Constants.rgprovNamespace + "Difference");
				rgprovDifferencenResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource gd = mprov.createResource(Constants.customNameSpace + stOpActivityName+timeStarted[2]);
			
			gd.addProperty(RDF.type, rgprovDifferencenResource);
			gd.addProperty(provWasAssociatedWithProperty, jena3111);
			Property hadMinuend = mprov.createProperty(Constants.rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(Constants.rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, gcopyA1);
			gd.addProperty(hadSubtrahend, gcopyB2);
			gOpC3.addProperty(provWasGeneratedByProperty, gd);
			gd.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			gd.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
			
		} 
		else if (stOp.equalsIgnoreCase("difference2")) 
		{
			Resource rgprovDifferencenResource = mprov.getResource(Constants.rgprovNamespace + "Difference");
			if (rgprovDifferencenResource == null) {
				rgprovDifferencenResource = mprov.createResource(Constants.rgprovNamespace + "Difference");
				rgprovDifferencenResource.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			
			Resource gd = mprov.createResource(Constants.customNameSpace + stOpActivityName+timeStarted[2]);
			gd.addProperty(RDF.type, rgprovDifferencenResource);
			gd.addProperty(provWasAssociatedWithProperty, jena3111);
			Property hadMinuend = mprov.createProperty(Constants.rgprovNamespace + "hadMinuend");
			Property hadSubtrahend = mprov.createProperty(Constants.rgprovNamespace + "hadSubtrahend");
			gd.addProperty(hadMinuend, gcopyB2);
			gd.addProperty(hadSubtrahend, gcopyA1);
			gOpC3.addProperty(provWasGeneratedByProperty, gd);
			gd.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
			gd.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		}	
		//setGraphC3_PROV_MODEL(mprov);
		po.getC3().setGraph_PROV_MODEL(mprov);
	}
	
	public static void updateC3Entailment (String entailActivityName, ParentOperator po, String timeStarted[], String timeEnded[]) 
	{
		Model mprov = po.getC3().getGraph_PROV_MODEL();
		
		Resource rgprovRDFSReasonerClass = mprov.getResource(Constants.rgprovNamespace + "RDFSReasoner");
		if (rgprovRDFSReasonerClass == null) {
			rgprovRDFSReasonerClass =  mprov.createResource(Constants.rgprovNamespace + "RDFSReasoner");
		}
		Resource Jena = mprov.getResource(Constants.egNamespace + "Jena");
		if (Jena == null) {
			Jena =  mprov.createResource(Constants.egNamespace + "Jena");
			Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
		}
		Resource jena3111 = mprov.getResource(Constants.egNamespace + "jena3.1.1");
		if (jena3111 == null) {
			jena3111 =  mprov.createResource(Constants.egNamespace + "jena3.1.1");
			jena3111.addProperty(RDF.type, Jena);
		}
		Resource rgprovGraphClass = mprov.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(Constants.rgprovNamespace + "Graph");
		}
		Resource rgprovGraphOpClass = mprov.getResource(Constants.rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(Constants.rgprovNamespace + "GraphOperation");
		}
		Resource rgprovEntailmentClass = mprov.getResource(Constants.rgprovNamespace + "Entailment");
		if (rgprovEntailmentClass == null) {
			rgprovEntailmentClass =  mprov.createResource(Constants.rgprovNamespace + "Entailment");
			rgprovEntailmentClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
		}
		Resource rgprovRDFSEntailmentClass = mprov.getResource(Constants.rgprovNamespace + "RDFSEntailment");
		if (rgprovRDFSEntailmentClass == null) {
			rgprovRDFSEntailmentClass =  mprov.createResource(Constants.rgprovNamespace + "RDFSEntailment");
			rgprovRDFSEntailmentClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
		}
		
		Property provWasAssociatedWithProperty = mprov.getProperty(Constants.provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(Constants.provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(Constants.provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(Constants.provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(Constants.provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(Constants.provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(Constants.provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "EndedAtTime");
		}
		Resource gcopyA1 = mprov.getResource(Constants.customNameSpace + po.getA1().getGraph_COPY_NAME());
		Resource gcopyB2 = mprov.getResource(Constants.customNameSpace + po.getB2().getGraph_COPY_NAME());
		Resource gOpC3 = mprov.getResource(Constants.customNameSpace + po.getGraphSTA1B2_NAME());
		
		
		
		Resource ge = mprov.createResource(Constants.customNameSpace + entailActivityName);
		ge.addProperty(RDF.type, rgprovRDFSEntailmentClass);
		ge.addProperty(provWasAssociatedWithProperty, jena3111);
		ge.addProperty(provUsedProperty, gOpC3);
		ge.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
		ge.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		
		Resource gC3 = mprov.createResource(Constants.customNameSpace + po.getC3().getGraph_NAME());
		gC3.addProperty(RDF.type, rgprovGraphClass);
		gC3.addProperty(provWasGeneratedByProperty, ge);
		Property rgprovWasEntailedFromProperty = mprov.getProperty(Constants.rgprovNamespace + "wasEntailedFrom");
		if (rgprovWasEntailedFromProperty == null) {
			rgprovWasEntailedFromProperty = mprov.createProperty(Constants.rgprovNamespace + "wasEntailedFrom");
		}
		gC3.addProperty(rgprovWasEntailedFromProperty, gOpC3);
		
		gC3.addProperty(provWasDerivedFromProperty, gcopyA1);
		gC3.addProperty(provWasDerivedFromProperty, gcopyB2);
		
		po.getC3().setGraph_PROV_MODEL(mprov);
	}
	
	
	public static void updateProvOfSourceGraphAfterLoadingUpdate(String graphNametimeFetched, UpdatedSourceGraphInSystem usg, 
																	String timeStarted[], String timeEnded[]) 
	{
		Model mprov = usg.getGraphprime_PROV_star_COPY_MODEL();
		mprov.setNsPrefix(Constants.customPrefix, Constants.customNameSpace);
		
		Resource rgprovGraphClass = mprov.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(Constants.rgprovNamespace + "Graph");
		}
		Resource rgprovUpdateGraphClass = mprov.getResource(Constants.rgprovNamespace + "UpdateGraph");
		if (rgprovUpdateGraphClass == null) {
			rgprovUpdateGraphClass =  mprov.createResource(Constants.rgprovNamespace + "UpdateGraph");
			rgprovUpdateGraphClass.addProperty(RDFS.subClassOf, rgprovGraphClass);
		}
		Resource RESTClient = mprov.getResource(Constants.customNameSpace + "RESTClient");
		if (RESTClient == null) {
			RESTClient = mprov.createResource(Constants.customNameSpace + "RESTClient");
		}
		Resource jersey225 = mprov.getResource(Constants.customNameSpace + "jersey225");
		if (jersey225 == null) {
			jersey225 =  mprov.createResource(Constants.customNameSpace + "jersey225");
			jersey225.addProperty(RDF.type, RESTClient);
		}
		
		// add to the passed model the triples that describe its fetching as per rgprov
		Resource rgprovFetchResource = mprov.getResource(Constants.rgprovNamespace + "Fetch");
		if (rgprovFetchResource == null) {
			rgprovFetchResource = mprov.createResource(Constants.rgprovNamespace + "Fetch");
		}
		Resource fetch = mprov.createResource(Constants.customNameSpace + "fetch" + graphNametimeFetched+timeStarted[2]);
		fetch.addProperty(RDF.type, rgprovFetchResource);
		
		Property provWasAssociatedWithProperty = mprov.getProperty(Constants.provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(Constants.provNamespace + "wasAssociatedWith");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "EndedAtTime");
		}
		
		fetch.addProperty(provWasAssociatedWithProperty, jersey225);
		fetch.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
		fetch.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		
		//may change this to prov:used
		Property accessedProperty = mprov.getProperty(Constants.customNameSpace + "accessed");
		if (accessedProperty == null) {
			accessedProperty = mprov.createProperty(Constants.customNameSpace + "accessed");
		}
		Property rgprovCopiedProperty = mprov.getProperty(Constants.rgprovNamespace + "copied");
		if (rgprovCopiedProperty == null) {
			rgprovCopiedProperty = mprov.createProperty(Constants.rgprovNamespace + "copied");
		}
		Property rgprovWasCopyResultProperty = mprov.getProperty(Constants.rgprovNamespace + "wasCopyResult");
		if (rgprovWasCopyResultProperty == null) {
			rgprovWasCopyResultProperty = mprov.createProperty(Constants.rgprovNamespace + "wasCopyResult");
		}
		Property rgprovWasExactCopyProperty = mprov.getProperty(Constants.rgprovNamespace + "wasExactCopy");
		if (rgprovWasExactCopyProperty == null) {
			rgprovWasExactCopyProperty = mprov.createProperty(Constants.rgprovNamespace + "wasExactCopy");
		}
		
		Resource updateGraphOriginalNameResource = mprov.getResource(Constants.egNamespace + usg.getUpdateGraph_source_NAME());
		if (updateGraphOriginalNameResource == null) {
			updateGraphOriginalNameResource = mprov.createResource(Constants.egNamespace + usg.getUpdateGraph_source_NAME());
		}
		Resource updateGraphCopyNameResource = mprov.getResource(Constants.customNameSpace + usg.getUpdateGraph_COPY_NAME());
		if (updateGraphCopyNameResource == null) {
			updateGraphCopyNameResource = mprov.createResource(Constants.customNameSpace + usg.getUpdateGraph_COPY_NAME());
		}
		
		updateGraphOriginalNameResource.addProperty(RDF.type, rgprovUpdateGraphClass);
		updateGraphCopyNameResource.addProperty(RDF.type, rgprovUpdateGraphClass);
		
		fetch.addProperty(accessedProperty, usg.getUpdateGraph_URI());
		fetch.addProperty(rgprovCopiedProperty,updateGraphOriginalNameResource);
		updateGraphCopyNameResource.addProperty(rgprovWasCopyResultProperty, fetch);
		updateGraphCopyNameResource.addProperty(rgprovWasExactCopyProperty, updateGraphOriginalNameResource);
		
		Resource graphPrimeProvCopyResource = mprov.createResource(Constants.customNameSpace +  usg.getGraphprime_PROV_COPY_NAME());		
		graphPrimeProvCopyResource.addProperty(RDF.type, rgprovGraphClass);
		
		Property provWasRevisionOf = mprov.getProperty(Constants.provNamespace + "wasRevisionOf");
		if (provWasRevisionOf == null) {
			provWasRevisionOf = mprov.createProperty(Constants.provNamespace + "wasRevisionOf");
		}
		// THIS NEEDS FIXING
		
		//P'copy(B',2) is a new version of P'copy(B,2) , this creates the triple:
		//P'copy(B',2 prov:wasRevisionOf P'copy(B,2) .
//		Resource provPrimeB2prime = mprov.createResource(p.getGraphB2prime_PROV_URI());
//		Resource provB2 = mprov.getResource(p.getGraphB2_PROV_URI());
//		provPrimeB2prime.addProperty(wasRevisionOf, provB2);
		
		usg.setGraphprime_PROV_star_COPY_MODEL(mprov);
			
	}
	
	public static Model updateC3ProvAfterLoadingUpdate(Model c3PreviousProv, String c3provName, String b2provStarCopyName) 
	{
		Model mresults = c3PreviousProv;
		Resource rgprovGraphClass = mresults.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mresults.createResource(Constants.rgprovNamespace + "Graph");
		}
		Property provWasDerivedFromProperty = mresults.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mresults.createProperty(Constants.provNamespace + "wasDerivedFrom");
		}
		
		Resource b2provStarCopyNameResource = mresults.getResource(Constants.customNameSpace + b2provStarCopyName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(Constants.customNameSpace + b2provStarCopyName);
		}
		Resource c3provNameResource = mresults.getResource(Constants.customNameSpace + c3provName);
		if (b2provStarCopyNameResource == null) {
			b2provStarCopyNameResource = mresults.createResource(Constants.customNameSpace + c3provName);
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
	
		Resource rgprovRDFSReasonerClass = mprov.getResource(Constants.rgprovNamespace + "RDFSReasoner");
		if (rgprovRDFSReasonerClass == null) {
			rgprovRDFSReasonerClass =  mprov.createResource(Constants.rgprovNamespace + "RDFSReasoner");
		}
		Resource Jena = mprov.getResource(Constants.egNamespace + "Jena");
		if (Jena == null) {
			Jena =  mprov.createResource(Constants.egNamespace + "Jena");
			Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
		}
		Resource jena3111 = mprov.getResource(Constants.egNamespace + "jena3.1.1");
		if (jena3111 == null) {
			jena3111 =  mprov.createResource(Constants.egNamespace + "jena3.1.1");
			jena3111.addProperty(RDF.type, Jena);
		}
		Resource rgprovGraphClass = mprov.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(Constants.rgprovNamespace + "Graph");
		}
		Resource rgprovUpdateGraphClass = mprov.getResource(Constants.rgprovNamespace + "UpdateGraph");
		if (rgprovUpdateGraphClass == null) {
			rgprovUpdateGraphClass =  mprov.createResource(Constants.rgprovNamespace + "UpdateGraph");
			rgprovUpdateGraphClass.addProperty(RDFS.subClassOf, rgprovGraphClass);
		}
		Resource rgprovGraphOpClass = mprov.getResource(Constants.rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(Constants.rgprovNamespace + "GraphOperation");
		}
		Property provWasAssociatedWithProperty = mprov.getProperty(Constants.provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(Constants.provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(Constants.provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(Constants.provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(Constants.provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(Constants.provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(Constants.provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "EndedAtTime");
		}
		Resource gC3 = mprov.getResource(Constants.customNameSpace + uo.getC3prime().getGraph_NAME());
		if (gC3 == null) {
			gC3 = mprov.createResource(Constants.customNameSpace + uo.getC3prime().getGraph_NAME());
			gC3.addProperty(RDF.type, rgprovGraphClass);
		}
		Resource UpcopyResource = mprov.createResource(Constants.customNameSpace + uo.getB2prime().getUpdateGraph_COPY_NAME());
		UpcopyResource.addProperty(RDF.type, rgprovUpdateGraphClass);
		Resource gOpC3prime = mprov.createResource(Constants.customNameSpace + uo.getGraphSTA1B2prime_NAME());
		gOpC3prime.addProperty(RDF.type, rgprovGraphClass);
		gOpC3prime.addProperty(provWasDerivedFromProperty, UpcopyResource);
		gOpC3prime.addProperty(provWasDerivedFromProperty, gC3);
		
		if (graphUpdateType.equalsIgnoreCase("insert")) 
		{ 
			Resource rgprovInsertOpClass = mprov.getResource(Constants.rgprovNamespace + "InsertOperation");
			if (rgprovInsertOpClass == null) {
				rgprovInsertOpClass =  mprov.createResource(Constants.rgprovNamespace + "InsertOperation");
				rgprovInsertOpClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Property rgprovInsertedProperty = mprov.getProperty(Constants.rgprovNamespace + "inserted");
			if (rgprovInsertedProperty == null) {
				rgprovInsertedProperty = mprov.createProperty(Constants.rgprovNamespace + "inserted");
			}
			
			Resource insertIntoG3 = mprov.createResource(Constants.customNameSpace + "insert" + uo.getC3prime().getWithoutTTL_Graph_NAME() + timeCalled);
			insertIntoG3.addProperty(RDF.type, rgprovInsertOpClass);
			insertIntoG3.addProperty(provWasAssociatedWithProperty, jena3111);
			insertIntoG3.addProperty(provUsedProperty, gC3);
			insertIntoG3.addProperty(provStartedAtTimeProperty, timeCalled);
			gOpC3prime.addProperty(provWasGeneratedByProperty, insertIntoG3);
			if (queriedGraphStOpType.equalsIgnoreCase("union")) 
			{
				insertIntoG3.addProperty(rgprovInsertedProperty, UpcopyResource);
			}
			else if ((queriedGraphStOpType.equalsIgnoreCase("intersection")) 
						||  (queriedGraphStOpType.equalsIgnoreCase("difference1"))
						||  (queriedGraphStOpType.equalsIgnoreCase("difference2")))
			{
				Resource subsUpcopyResource = mprov.createResource(Constants.customNameSpace + "SubsetOf" + uo.getB2prime().getUpdateGraph_COPY_NAME());
				subsUpcopyResource.addProperty(RDF.type, rgprovUpdateGraphClass);
				subsUpcopyResource.addProperty(provWasDerivedFromProperty, UpcopyResource);
				
				Resource gcopyA1 = mprov.getResource(Constants.customNameSpace + uo.getA1().getGraph_COPY_NAME());
				subsUpcopyResource.addProperty(provWasDerivedFromProperty, gcopyA1);
				
				insertIntoG3.addProperty(rgprovInsertedProperty, subsUpcopyResource);
				gOpC3prime.addProperty(provWasDerivedFromProperty, subsUpcopyResource);
				gOpC3prime.addProperty(provWasDerivedFromProperty, gcopyA1);
			}
		}
		else // it is delete
		{
			Resource rgprovDeleteOpClass = mprov.getResource(Constants.rgprovNamespace + "DeleteOperation");
			if (rgprovDeleteOpClass == null) {
				rgprovDeleteOpClass =  mprov.createResource(Constants.rgprovNamespace + "DeleteOperation");
				rgprovDeleteOpClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
			}
			Resource deleteFromC3 = mprov.createResource(Constants.customNameSpace + "delete" + uo.getC3prime().getWithoutTTL_Graph_NAME() + timeCalled);
			deleteFromC3.addProperty(RDF.type, rgprovDeleteOpClass);
			deleteFromC3.addProperty(provWasAssociatedWithProperty, jena3111);
			deleteFromC3.addProperty(provUsedProperty, gC3);
			deleteFromC3.addProperty(provStartedAtTimeProperty, timeCalled);
			Property rgprovDeletedProperty = mprov.getProperty(Constants.rgprovNamespace + "deleted");
			if (rgprovDeletedProperty == null) {
				rgprovDeletedProperty = mprov.createProperty(Constants.rgprovNamespace + "deleted");
			}
			if (queriedGraphStOpType.equalsIgnoreCase("union")) 
			{
				
				Resource subUpcopyResource = mprov.createResource(Constants.customNameSpace + "SubsetOf" + uo.getB2prime().getUpdateGraph_COPY_NAME());
				subUpcopyResource.addProperty(RDF.type, rgprovUpdateGraphClass);
				subUpcopyResource.addProperty(provWasDerivedFromProperty, UpcopyResource);
				
				Resource gcopyA1 = mprov.getResource(Constants.customNameSpace + uo.getA1().getGraph_COPY_NAME());
				subUpcopyResource.addProperty(provWasDerivedFromProperty, gcopyA1);
				
				deleteFromC3.addProperty(rgprovDeletedProperty, subUpcopyResource);
				
				gOpC3prime.addProperty(provWasGeneratedByProperty, deleteFromC3);
				gOpC3prime.addProperty(provWasDerivedFromProperty, gcopyA1);
				gOpC3prime.addProperty(provWasDerivedFromProperty, subUpcopyResource);
				
			}
			else if ((queriedGraphStOpType.equalsIgnoreCase("intersection")) 
					||  (queriedGraphStOpType.equalsIgnoreCase("difference1"))
					||  (queriedGraphStOpType.equalsIgnoreCase("difference2")))
			{
				deleteFromC3.addProperty(rgprovDeletedProperty, UpcopyResource);
			}
		}
		uo.getC3prime().setGraph_PROV_MODEL(mprov);
		
	}
	
	public static void updateC3ProvEntailment (String entailActivityName, UpdatedOperator uo, String timeStarted[], String timeEnded[]) 
	{
		//Resource gC3prime = mprov.createResource(inNamespace + uo.getC3prime().getGraph_UpdatedNAME()); 
		//gC3prime.addProperty(RDF.type, rgprovGraphClass);
		 
		Model mprov = uo.getC3prime().getGraph_PROV_MODEL();
		
		Resource rgprovRDFSReasonerClass = mprov.getResource(Constants.rgprovNamespace + "RDFSReasoner");
		if (rgprovRDFSReasonerClass == null) {
			rgprovRDFSReasonerClass =  mprov.createResource(Constants.rgprovNamespace + "RDFSReasoner");
		}
		Resource Jena = mprov.getResource(Constants.egNamespace + "Jena");
		if (Jena == null) {
			Jena =  mprov.createResource(Constants.egNamespace + "Jena");
			Jena.addProperty(RDFS.subClassOf, rgprovRDFSReasonerClass);
		}
		Resource jena3111 = mprov.getResource(Constants.egNamespace + "jena3.1.1");
		if (jena3111 == null) {
			jena3111 =  mprov.createResource(Constants.egNamespace + "jena3.1.1");
			jena3111.addProperty(RDF.type, Jena);
		}
		Resource rgprovGraphOpClass = mprov.getResource(Constants.rgprovNamespace + "GraphOperation");
		if (rgprovGraphOpClass == null) {
			rgprovGraphOpClass =  mprov.createResource(Constants.rgprovNamespace + "GraphOperation");
		}
		Resource rgprovEntailmentClass = mprov.getResource(Constants.rgprovNamespace + "Entailment");
		if (rgprovEntailmentClass == null) {
			rgprovEntailmentClass =  mprov.createResource(Constants.rgprovNamespace + "Entailment");
			rgprovEntailmentClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
		}
		Resource rgprovRDFSEntailmentClass = mprov.getResource(Constants.rgprovNamespace + "RDFSEntailment");
		if (rgprovRDFSEntailmentClass == null) {
			rgprovRDFSEntailmentClass =  mprov.createResource(Constants.rgprovNamespace + "RDFSEntailment");
			rgprovRDFSEntailmentClass.addProperty(RDFS.subClassOf, rgprovGraphOpClass);
		}
		Resource rgprovGraphClass = mprov.getResource(Constants.rgprovNamespace + "Graph");
		if (rgprovGraphClass == null) {
			rgprovGraphClass =  mprov.createResource(Constants.rgprovNamespace + "Graph");
		}
		Resource rgprovUpdateGraphClass = mprov.getResource(Constants.rgprovNamespace + "UpdateGraph");
		if (rgprovUpdateGraphClass == null) {
			rgprovUpdateGraphClass =  mprov.createResource(Constants.rgprovNamespace + "UpdateGraph");
			rgprovUpdateGraphClass.addProperty(RDFS.subClassOf, rgprovGraphClass);
		}
		Property provWasAssociatedWithProperty = mprov.getProperty(Constants.provNamespace + "wasAssociatedWith");
		if (provWasAssociatedWithProperty == null) {
			provWasAssociatedWithProperty = mprov.createProperty(Constants.provNamespace + "wasAssociatedWith");
		}
		Property provUsedProperty = mprov.getProperty(Constants.provNamespace + "Used");
		if (provUsedProperty == null) {
			provUsedProperty = mprov.createProperty(Constants.provNamespace + "Used");
		}
		Property provWasGeneratedByProperty = mprov.getProperty(Constants.provNamespace + "wasGeneratedBy");
		if (provWasGeneratedByProperty == null) {
			provWasGeneratedByProperty = mprov.createProperty(Constants.provNamespace + "wasGeneratedBy");
		}
		Property provWasDerivedFromProperty = mprov.getProperty(Constants.provNamespace + "wasDerivedFrom");
		if (provWasDerivedFromProperty == null) {
			provWasDerivedFromProperty = mprov.createProperty(Constants.provNamespace + "wasDerivedFrom");
		}
		Property provStartedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "startedAtTime");
		if (provStartedAtTimeProperty == null) {
			provStartedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "startedAtTime");
		}
		Property provEndedAtTimeProperty = mprov.getProperty(Constants.provNamespace + "EndedAtTime");
		if (provEndedAtTimeProperty == null) {
			provEndedAtTimeProperty = mprov.createProperty(Constants.provNamespace + "EndedAtTime");
		}
		
		Resource gOpC3prime = mprov.createResource(Constants.customNameSpace + uo.getGraphSTA1B2prime_NAME());
		gOpC3prime.addProperty(RDF.type, rgprovGraphClass);
		
		Resource ge = mprov.createResource(Constants.customNameSpace + entailActivityName);
		ge.addProperty(RDF.type, rgprovRDFSEntailmentClass);
		ge.addProperty(provWasAssociatedWithProperty, jena3111);
		ge.addProperty(provUsedProperty, gOpC3prime);
		ge.addProperty(provStartedAtTimeProperty, timeStarted[0] + "^^xsd:dateTime");
		ge.addProperty(provEndedAtTimeProperty, timeEnded[0] + "^^xsd:dateTime");
		
		Resource gC3prime = mprov.createResource(Constants.customNameSpace +  uo.getC3prime().getGraph_UpdatedNAME());
		gC3prime.addProperty(RDF.type, rgprovGraphClass);
		gC3prime.addProperty(provWasGeneratedByProperty, ge);
		Property rgprovWasEntailedFromProperty = mprov.getProperty(Constants.rgprovNamespace + "wasEntailedFrom");
		if (rgprovWasEntailedFromProperty == null) {
			rgprovWasEntailedFromProperty = mprov.createProperty(Constants.rgprovNamespace + "wasEntailedFrom");
		}
		gC3prime.addProperty(rgprovWasEntailedFromProperty, gOpC3prime);
		Resource gC3 = mprov.getResource(Constants.customNameSpace + uo.getC3prime().getGraph_NAME());
		if (gC3 == null) {
			gC3 = mprov.createResource(Constants.customNameSpace + uo.getC3prime().getGraph_NAME());
			gC3.addProperty(RDF.type, rgprovGraphClass);
		}
		Property provWasRevisionOf = mprov.getProperty(Constants.provNamespace + "wasRevisionOf");
		if (provWasRevisionOf == null) {
			provWasRevisionOf = mprov.createProperty(Constants.provNamespace + "wasRevisionOf");
		}
		gC3prime.addProperty(provWasRevisionOf, gC3);
		
		Resource theAppliedUpdate;
		if(uo.getB2prime().isUseAllUpdate()){
			theAppliedUpdate = mprov.getResource(Constants.customNameSpace + uo.getB2prime().getUpdateGraph_COPY_NAME());
			if (theAppliedUpdate == null) {
				theAppliedUpdate = mprov.createResource(Constants.customNameSpace + uo.getB2prime().getUpdateGraph_COPY_NAME());
				theAppliedUpdate.addProperty(RDF.type, rgprovUpdateGraphClass);
			}
		}
		else {
			theAppliedUpdate = mprov.getResource(Constants.customNameSpace  + "SubsetOf" + uo.getB2prime().getUpdateGraph_COPY_NAME());
			if (theAppliedUpdate == null) {
				theAppliedUpdate = mprov.createResource(Constants.customNameSpace  + "SubsetOf" + uo.getB2prime().getUpdateGraph_COPY_NAME());
				theAppliedUpdate.addProperty(RDF.type, rgprovUpdateGraphClass);
			}
		}
				
		gC3prime.addProperty(provWasDerivedFromProperty, theAppliedUpdate);
		
		uo.getC3prime().setGraph_PROV_MODEL(mprov);
	}
			
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
