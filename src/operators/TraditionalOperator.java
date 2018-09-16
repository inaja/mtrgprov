package operators;

import utilities.*;
import miniT.ProvenanceHandler;

import java.io.IOException;
import org.apache.jena.rdf.model.Model;

public class TraditionalOperator extends ParentOperator 
{
	private String DATASET4TRADITIONAL;
	private String DATASET4COPIES; 
	
	public TraditionalOperator (String graphA1_source_URI, String graphA1_source_PROV_URI, String graphB2_source_URI, String graphB2_source_PROV_URI) 
	{
		super(graphA1_source_URI,  graphA1_source_PROV_URI, graphB2_source_URI, graphB2_source_PROV_URI);
	}
	
	public void loadBothGraphsAndTheirProvs(String graphA1_source_NAME, String graphA1_source_PROV_NAME, String graphB2_source_NAME, String graphB2_source_PROV_NAME) throws IOException 
	{
		super.loadBothGraphsAndTheirProvs(graphA1_source_NAME, graphA1_source_PROV_NAME, graphB2_source_NAME, graphB2_source_PROV_NAME);
		
		MiscUtilities.writeModelToFile(a1.getGraph_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + a1.getGraph_COPY_NAME(), "TURTLE");
		MiscUtilities.writeModelToFile(a1.getGraph_PROV_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + a1.getGraph_PROV_COPY_NAME(), "TURTLE");
		MiscUtilities.writeModelToFile(a1.getGraph_PROV_star_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + a1.getGraph_PROV_star_COPY_NAME(), "TURTLE");
		
		MiscUtilities.writeModelToFile(b2.getGraph_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + b2.getGraph_COPY_NAME(), "TURTLE");
		MiscUtilities.writeModelToFile(b2.getGraph_PROV_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + b2.getGraph_PROV_COPY_NAME(), "TURTLE");
		MiscUtilities.writeModelToFile(b2.getGraph_PROV_star_COPY_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + b2.getGraph_PROV_star_COPY_NAME(), "TURTLE");
	}
	
	public void initializeC (String graphC3_NAME, String graphC3_PROV_NAME) 
	{
		super.initializeC(graphC3_NAME, graphC3_PROV_NAME);

		MiscUtilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/cacheStoreC/traditional/" + c3.getWithoutTTL_Graph_PROV_NAME()+"initialLoad.ttl", "ttl");
	}
	
	public void createSTGraph (String graphSTA1B2_NAME) 
	{
		super.createSTGraph(graphSTA1B2_NAME);
		
		MiscUtilities.writeModelToFile(graphSTA1B2_MODEL, graphStoresLocal_URI + "/tempMemoryC/traditional/" + c3.getWithoutTTL_Graph_NAME()+ "_ST.ttl", "ttl");
		
		MiscUtilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/tempMemoryC/traditional/" + c3.getWithoutTTL_Graph_PROV_NAME()+ "ST" + ".ttl", "ttl");
	}
	
	//public Model[] reasonRDFS(boolean uploadToFuseki) {
	public void reasonRDFS(boolean uploadToFuseki, String Op) 
	{
		String [] timeNowStart = MiscUtilities.getTime();
		String entailActivityName = ProvenanceHandler.createNameOfEntailOp(c3.getGraph_NAME(), timeNowStart[1]);
		Model results = EntailmentUtilities.getEntailmentsOnly(graphSTA1B2_MODEL, graphStoresLocal_URI + "/graphStoreC/traditional/derivationCounts" + timeNowStart[1] + Op + ".txt" );
		String [] timeNowEnd = MiscUtilities.getTime();
		c3.setGraph_INFS_MODEL(results);
		
		MiscUtilities.writeModelToFile(graphSTA1B2_MODEL, graphStoresLocal_URI + "/graphStoreC/traditional/" + c3.getWithoutTTL_Graph_BASE_NAME()+ Op + "-" + timeNowStart[1] + ".ttl", "ttl");
		MiscUtilities.writeModelToFile(results, graphStoresLocal_URI + "/graphStoreC/traditional/" + c3.getWithoutTTL_Graph_INFS_NAME() + Op + "-" + timeNowStart[1] + ".ttl", "ttl");
		
		ProvenanceHandler.updateC3Entailment(entailActivityName, this, timeNowStart, timeNowEnd);
		MiscUtilities.writeModelToFile(c3.getGraph_PROV_MODEL(), graphStoresLocal_URI + "/graphStoreC/provStoreC/traditional/" + c3.getWithoutTTL_Graph_PROV_NAME() + Op + "-" + timeNowStart[1]+ ".ttl", "ttl");
		// uploading to Fuseki
		if (uploadToFuseki) {
			try {
				SPARQLUtilities.uploadNewGraph(DATASET4COPIES, a1.getGraph_COPY_NAME(), a1.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4COPIES, b2.getGraph_COPY_NAME(), b2.getGraph_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4TRADITIONAL, c3.getGraph_BASE_NAME(), c3.getGraph_BASE_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4TRADITIONAL, c3.getGraph_INFS_NAME(), c3.getGraph_INFS_MODEL());
				SPARQLUtilities.uploadNewGraph(DATASET4TRADITIONAL, c3.getGraph_PROV_NAME(), c3.getGraph_PROV_MODEL());
			}
			catch (Exception e) {
				System.err.println("\n************************************************* \n"
									+ "WARNING from Method reasonRDFS in Class TraditionalOperator, \n"
									+ "COULD NOT UPLOAD TO FUSEKI .... \n"
									+ e.getMessage()
									+ "\n************************************************* \n");
			}
		}
		
		//return results;		
	}


	public void setGraphStOpType(String graphStOpType) 
	{
		super.setGraphStOpType(graphStOpType);
		
		if (graphStOpType.equalsIgnoreCase("union")) {
			DATASET4TRADITIONAL = Constants.DATASET4TRADITIONAL_UNION;
			DATASET4COPIES = Constants.DATASET4COPIES_UNION;
		} else if (graphStOpType.equalsIgnoreCase("intersection")) {
			DATASET4TRADITIONAL = Constants.DATASET4TRADITIONAL_INTERSECTION;
			DATASET4COPIES = Constants.DATASET4COPIES_INTERSECTION;
		} else if (graphStOpType.equalsIgnoreCase("difference1")) {
			DATASET4TRADITIONAL = Constants.DATASET4TRADITIONAL_DIFFERENCE1;
			DATASET4COPIES = Constants.DATASET4COPIES_DIFFERENCE1;
		} else if (graphStOpType.equalsIgnoreCase("difference2")) {
			DATASET4TRADITIONAL = Constants.DATASET4TRADITIONAL_DIFFERENCE2;
			DATASET4COPIES = Constants.DATASET4COPIES_DIFFERENCE2;
		} else throw new IllegalArgumentException("Invalid graph ST operation type: " 
			+ graphStOpType 
			+ ", was expecting either union, intersection, difference1, or difference2");
		
		this.graphStOpType = graphStOpType;
	}

}
