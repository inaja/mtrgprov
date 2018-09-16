package utilities.graph;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import miniT.ProvenanceHandler;
import utilities.Constants;
import utilities.SPARQLUtilities;
import utilities.MiscUtilities;

public class SourceGraphInSystem 
{
	//private String NAMESPACE;
	private String graphStoresLocal_URI = Constants.LOCAL_URI;  
	//"d:/alleclipses/workspaces/afterformatworkplacejaxrs/Minithesis/src/miniT/GraphStores";
	
	private String graph_source_URI; // its name on the the outside server
	private String graph_source_NAME; //its name on the outside server if different from its URI
	private String graph_source_PROV_URI; // its provenance's URI on the outside server
	private String graph_source_PROV_NAME; //its provenance's name on the outside server if different from its URI
	private String graph_COPY_NAME; //its name on our server
	private String graph_PROV_COPY_NAME; //its copied (original) provenance's name on our server
	private String graph_PROV_star_COPY_NAME; //its new provenance's after fetching it
	private Model graph_MODEL; //the model
	private Model graph_PROV_MODEL; //its copied (original) provenance's model
	private Model graph_PROV_COPY_MODEL;
	private Model graph_PROV_star_COPY_MODEL; //its new provenance's model after reflecting fetching it 
	
	
	/**
	 * This constructor is to be used by Operator and TradionalOperator.
	 * @param graph_source_URI : the URI where the source graph is
	 * @param graph_source_PROV_URI : the URI where the source provenance graph is
	 */
	public SourceGraphInSystem(String graph_source_URI, String graph_source_PROV_URI) {
		this.graph_source_URI = graph_source_URI;
		this.graph_source_PROV_URI = graph_source_PROV_URI;
	}

	/** This method is to be used by Operator and TradionalOperator.
	 * It loads from Fuseki the model accessible by the URI provided to the constructor 
	 * as well as the provenance of the graph.
	 * It sets, in addition to the graph_source_NAME and graph_source_PROV_NAME: graph_COPY_NAME,
	 * graph_PROV_COPY_NAME, and graph_PROV_star_COPY_NAME.
	 * It sets, after loading, the graph_MODEL, graph_PROV_MODEL, and graph_PROV_COPY_MODEL. 
	 * It calls the Provenance Handler to set graph_PROV_star_COPY_MODEL.
	 * @param dataset
	 * @param graph_source_NAME : the name that will be assigned to the graph, presumed 
	 * 							different than the URI as it is internal
	 * @param graph_source_PROV_NAME : the name that will be assigned to the PROV graph, 
	 * 								presumed different than the URI as it is internal
	 * @throws IOException 
	 */
	public void loadGraphAndItsProv(String dataset, String graph_source_NAME, 
									String graph_source_PROV_NAME) throws IOException 
	{
		setGraph_source_NAME(graph_source_NAME);
		setGraph_source_PROV_NAME(graph_source_PROV_NAME);
		setGraph_COPY_NAME("Gcopy_" + graph_source_NAME);
		
		//first load both the source and prov graphs 
		String[] timeNowStart = MiscUtilities.getTime();
		Model m = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(dataset, graph_source_URI);
		Model mprov = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(dataset,graph_source_PROV_URI);
		String timeNowEnd[] = MiscUtilities.getTime();
		
		setGraph_MODEL(m);
		setGraph_PROV_MODEL(mprov);
		
		setGraph_PROV_COPY_NAME("Pcopy("+getWithoutTTL_Graph_source_PROV_NAME()+")" + ".ttl");
		setGraph_PROV_COPY_MODEL(ModelFactory.createDefaultModel().union(mprov));
		setGraph_PROV_star_COPY_NAME("P_star_copy("+getWithoutTTL_Graph_source_PROV_NAME()+")" + ".ttl");
		setGraph_PROV_star_COPY_MODEL(ModelFactory.createDefaultModel().union(mprov));
		
		// third update provenance and produce new provenance graph
		// String timeNowEnd = Utilities.getTime();
		// this sets setGraph_PROV_star_COPY_MODEL
		ProvenanceHandler.createProvOfSourceGraph(getWithoutTTL_Graph_source_NAME (), this, timeNowStart, timeNowEnd);
		
	}
	/** This method is to be used by UpdatedOperator to retrieve the non-updated source graph.
	 * It loads from Fuseki the model accessible by the URI provided to the constructor 
	 * @param graph_source_NAME : the name that will be assigned to the graph, presumed 
	 * 							different than the URI as it is internal
	 * @param graph_source_PROV_NAME : the name that will be assigned to the PROV graph, 
	 * 								presumed different than the URI as it is internal
	 * @throws IOException 
	 */
	public void loadGraphOnly(String dataset, String graph_source_NAME) throws IOException 
	{
		setGraph_source_NAME(graph_source_NAME);
		Model m = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(dataset, graph_source_URI);
		setGraph_MODEL(m);
	}


	public String getGraphStoresLocal_URI() {
		return graphStoresLocal_URI;
	}
	public void setGraphStoresLocal_URI(String graphStoresLocal_URI) {
		this.graphStoresLocal_URI = graphStoresLocal_URI;
	}
	
	public String getGraph_source_URI() {
		return graph_source_URI;
	}

	public void setGraph_source_URI(String graph_source_URI) {
		this.graph_source_URI = graph_source_URI;
	}

	public String getGraph_source_NAME() {
		return graph_source_NAME;
	}

	public void setGraph_source_NAME(String graph_source_NAME) {
		this.graph_source_NAME = graph_source_NAME;
	}

	public String getGraph_source_PROV_URI() {
		return graph_source_PROV_URI;
	}

	public void setGraph_source_PROV_URI(String graph_source_PROV_URI) {
		this.graph_source_PROV_URI = graph_source_PROV_URI;
	}

	public String getGraph_source_PROV_NAME() {
		return graph_source_PROV_NAME;
	}

	public void setGraph_source_PROV_NAME(String graph_source_PROV_NAME) {
		this.graph_source_PROV_NAME = graph_source_PROV_NAME;
	}

	public String getGraph_COPY_NAME() {
		return graph_COPY_NAME;
	}

	public void setGraph_COPY_NAME(String graph_COPY_NAME) {
		this.graph_COPY_NAME = graph_COPY_NAME;
	}

	public String getGraph_PROV_COPY_NAME() {
		return graph_PROV_COPY_NAME;
	}

	public void setGraph_PROV_COPY_NAME(String graph_PROV_COPY_NAME) {
		this.graph_PROV_COPY_NAME = graph_PROV_COPY_NAME;
	}

	public String getGraph_PROV_star_COPY_NAME() {
		return graph_PROV_star_COPY_NAME;
	}

	public void setGraph_PROV_star_COPY_NAME(String graph_PROV_star_COPY_NAME) {
		this.graph_PROV_star_COPY_NAME = graph_PROV_star_COPY_NAME;
	}

	public Model getGraph_MODEL() {
		return graph_MODEL;
	}

	public void setGraph_MODEL(Model graph_MODEL) {
		this.graph_MODEL = graph_MODEL;
	}

	public Model getGraph_PROV_MODEL() {
		return graph_PROV_MODEL;
	}

	public void setGraph_PROV_MODEL(Model graph_PROV_MODEL) {
		this.graph_PROV_MODEL = graph_PROV_MODEL;
	}

	public Model getGraph_PROV_COPY_MODEL() {
		return graph_PROV_COPY_MODEL;
	}

	public void setGraph_PROV_COPY_MODEL(Model graph_PROV_COPY_MODEL) {
		this.graph_PROV_COPY_MODEL = graph_PROV_COPY_MODEL;
	}

	public Model getGraph_PROV_star_COPY_MODEL() {
		return graph_PROV_star_COPY_MODEL;
	}

	public void setGraph_PROV_star_COPY_MODEL(Model graph_PROV_star_COPY_MODEL) {
		this.graph_PROV_star_COPY_MODEL = graph_PROV_star_COPY_MODEL;
	}
	
	public String getWithoutTTL_Graph_source_NAME () {
		String result = graph_source_NAME.substring(0, graph_source_NAME.length() - 4);
		return result;
	}
	public String getWithoutTTL_Graph_source_PROV_NAME () {
		String result = graph_source_PROV_NAME.substring(0, graph_source_PROV_NAME.length() - 4);
		return result;
	}
	public String getWithoutTTL_Graph_COPY_NAME () {
		String result = graph_COPY_NAME.substring(0, graph_COPY_NAME.length() - 4);
		return result;
	}
	public String getWithoutTTL_Graph_PROV_COPY_NAME () {
		String result = graph_PROV_COPY_NAME.substring(0, graph_PROV_COPY_NAME.length() - 4);
		return result;
	}
	public String getWithoutTTL_Graph_PROV_star_COPY_NAME () {
		String result = graph_PROV_star_COPY_NAME.substring(0, graph_PROV_star_COPY_NAME.length() - 4);
		return result;
	}
	
}
