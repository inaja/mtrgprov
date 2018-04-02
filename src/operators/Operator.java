package operators;

import utilities.Config;
import utilities.EntailmentUtilities;
import utilities.SPARQLUtilities;
import utilities.Utilities;
import miniT.ProvenanceHandler;

import java.io.IOException;
import org.apache.jena.rdf.model.Model;


public class Operator extends ParentOperator 
{	
	private String DATASET4MT;
	private String DATASET4COPIES;

	public Operator (String graphA1_source_URI, String graphA1_source_PROV_URI, String graphB2_source_URI, String graphB2_source_PROV_URI) 
	{
		super(graphA1_source_URI,  graphA1_source_PROV_URI, graphB2_source_URI, graphB2_source_PROV_URI);
	}
	
	public void loadBothGraphsAndTheirProvs(String graphA1_source_NAME, String graphA1_source_PROV_NAME, String graphB2_source_NAME, String graphB2_source_PROV_NAME) throws IOException 
	{
		super.loadBothGraphsAndTheirProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);
		
		Utilities.writeModelToFile(a1.getGraph_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + a1.getGraph_COPY_NAME(), "TURTLE");
		Utilities.writeModelToFile(a1.getGraph_PROV_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + a1.getGraph_PROV_COPY_NAME(), "TURTLE");
		Utilities.writeModelToFile(a1.getGraph_PROV_star_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + a1.getGraph_PROV_star_COPY_NAME(), "TURTLE");
		
		Utilities.writeModelToFile(b2.getGraph_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + b2.getGraph_COPY_NAME(), "TURTLE");
		Utilities.writeModelToFile(b2.getGraph_PROV_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + b2.getGraph_PROV_COPY_NAME(), "TURTLE");
		Utilities.writeModelToFile(b2.getGraph_PROV_star_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + b2.getGraph_PROV_star_COPY_NAME(), "TURTLE");
	}
	
	public void initialiazeC(String graphC3_NAME, String graphC3_PROV_NAME) 
	{
		super.initializeC(graphC3_NAME, graphC3_PROV_NAME);
		
		Utilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + c3.getWithoutTTL_Graph_PROV_NAME()+"initialLoad.ttl", "ttl");
	}
	
	public void createSTGraph (String graphSTA1B2_NAME) 
	{
		super.createSTGraph(graphSTA1B2_NAME);
		
		Utilities.writeModelToFile(graphSTA1B2_MODEL, graphStoresLocal_URI + "/tempMemoryC/nontraditional/" + c3.getWithoutTTL_Graph_NAME() + "_ST.ttl", "ttl");
		
		Utilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/cacheStoreC/nontraditional/" + c3.getWithoutTTL_Graph_PROV_NAME() + "ST.ttl", "ttl");
	}
	
	public void reasonRDFS(boolean uploadToFuseki, String Op) {
		String [] timeNowStart = Utilities.getTime();
		String entailActivityName = ProvenanceHandler.createNameOfEntailOp(c3.getGraph_NAME(), timeNowStart[1]);
		Model mEntailed = EntailmentUtilities.getEntailedGraphAndCount(graphSTA1B2_MODEL, graphStoresLocal_URI + "/graphStoreC/nontraditional/derivationCounts" + timeNowStart[1] + Op + ".txt");
		String [] timeNowEnd = Utilities.getTime();
		c3.setGraph_MODEL(mEntailed);

		Utilities.writeModelToFile(mEntailed, graphStoresLocal_URI + "/graphStoreC/nontraditional/" + c3.getWithoutTTL_Graph_NAME()+ Op + "-" + timeNowStart[1] + ".ttl", "ttl");
		
		ProvenanceHandler.updateC3Entailment(entailActivityName, this, timeNowStart, timeNowEnd);
		Utilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/graphStoreC/provStoreC/nontraditional/" + c3.getWithoutTTL_Graph_PROV_NAME()+ Op + "-" + timeNowStart[1] + ".ttl", "ttl");
		
		// uploading to Fuseki
		if (uploadToFuseki) {
			try {
				SPARQLUtilities.uploadNewGraph(DATASET4COPIES, a1.getGraph_COPY_NAME(), a1.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4COPIES, b2.getGraph_COPY_NAME(), b2.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4MT, c3.getGraph_NAME(), c3.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4MT, c3.getGraph_PROV_NAME(), c3.getGraph_PROV_MODEL());
			}
			catch (Exception e) {
				System.err.println("\n************************************************* \n"
								   + "WARNING from Method reasonRDFS in Class Operator, \n"
								   + "COULD NOT UPLOAD TO FUSEKI .... \n"
								   + e.getMessage()
								   + "\n************************************************* \n");
			}
		}
	}
	
	public void setGraphStOpType(String graphStOpType) 
	{
		super.setGraphStOpType(graphStOpType);
		
		if (graphStOpType.equalsIgnoreCase("union")) {
			DATASET4MT = Config.DATASET_UNION;
			DATASET4COPIES = Config.DATASET4COPIES_UNION;
		} else if (graphStOpType.equalsIgnoreCase("intersection")) {
			DATASET4MT = Config.DATASET_INTERSECTION;
			DATASET4COPIES = Config.DATASET4COPIES_INTERSECTION;
		} else if (graphStOpType.equalsIgnoreCase("difference1")) {
			DATASET4MT = Config.DATASET_DIFFERENCE1;
			DATASET4COPIES = Config.DATASET4COPIES_DIFFERENCE1;
		} else if (graphStOpType.equalsIgnoreCase("difference2")) {
			DATASET4MT = Config.DATASET_DIFFERENCE2;
			DATASET4COPIES = Config.DATASET4COPIES_DIFFERENCE2;
		} else throw new IllegalArgumentException("Invalid graph ST operation type: " 
				+ graphStOpType 
				+ ", was expecting either union, intersection, difference1, or difference2");
		
		this.graphStOpType = graphStOpType;
	}
	
}
