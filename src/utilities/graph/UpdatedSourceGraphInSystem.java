package utilities.graph;

import java.io.IOException;

import miniT.ProvenanceHandler;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import utilities.SPARQLUtilities;
import utilities.Utilities;

public class UpdatedSourceGraphInSystem 
{

	private String graphOriginal_source_NAME;
	
	//ideally these should be the same as graph_source_URI and graph_source_PROV_URI 
	//however i am treating them differently
//	private String graphprime_source_URI; // its name on the the outside server
	private String graphprime_source_PROV_URI; // its provenance's URI on the outside server
	
//	private String graphprime_source_NAME; //its name on the outside server if different from its URI
	private String graphprime_source_PROV_NAME; //its provenance's name on the outside server if different from its URI
	
//	private String graphprime_COPY_NAME; //its name on our server
//  private Model graphprime_MODEL; //this is the whole updated graph prime which i don't need
	private String graphprime_PROV_COPY_NAME;  //its copied (original) provenance's name on our server
	private String graphprime_PROV_star_COPY_NAME;	 //its new provenance's after fetching it
	private Model graphprime_PROV_MODEL; //its copied (original) provenance's model
	private Model graphprime_PROV_COPY_MODEL;
	private Model graphprime_PROV_star_COPY_MODEL; //its new provenance's model after reflecting fetching it 
	
	private String updateGraph_URI; // the name on the outside server
	private String updateGraph_source_NAME;
	private Model updateGraph_MODEL;
	private String updateGraph_COPY_NAME;
	private String updateGraphSubset_NAME;
	private Model updateGraphSubset_MODEL; //???
	private boolean useAllUpdate = false; //if true then use all the update, if false, use the subset
			
	/**
	 * This constructor is to be used by UpdatedOperator.
	 * @param updateGraph_URI : the URI of the triples to be inserted or deleted
	 * @param graphprime_PROV_URI : the new provenance of the source graph
	 */
	public UpdatedSourceGraphInSystem(String updateGraph_URI, String graphprime_PROV_URI) {
		this.updateGraph_URI = updateGraph_URI;
		this.graphprime_source_PROV_URI = graphprime_PROV_URI;
	}
	
	/** This method is to be used by UpdatedOperator.
	 * It just loads from Fuseki the update, i.e., the triples to be inserted/deleted (saved as a graph)
	 * but does NOT apply it. It also loads the new provenance of the updated source graph.
	 * It sets, after loading, graphOriginal_source_NAME, updateGraph_source_NAME, graphprime_source_PROV_NAME,
	 * and updateGraph_COPY_NAME
	 * It calls the Provenance Handler to set graphprime_PROV_star_COPY_MODEL. 
	 * @param dataset
	 * @param graphOriginalName
	 * @param updateGraph_source_NAME
	 * @param graph_source_PROV_NAME
	 * @throws IOException
	 */
	public void loadUpdateAndItsProv(String dataset, String graphOriginalName, String updateGraph_source_NAME, String graph_source_PROV_NAME) throws IOException  
	{
		setGraphOriginal_source_NAME(graphOriginalName);
		setUpdateGraph_source_NAME(updateGraph_source_NAME);
		setGraphprime_source_PROV_NAME(graph_source_PROV_NAME);
		setUpdateGraph_COPY_NAME("Ucopy_" + updateGraph_COPY_NAME);
		
		//first load both the update and prov graphs from Fuseki
		String [] timeNowStart = Utilities.getTime();
		Model m = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(dataset, updateGraph_URI);
		Model mprov = SPARQLUtilities.loadOriginalGraphFromFusekiFullURI(dataset, graphprime_source_PROV_URI);
		String [] timeNowEnd = Utilities.getTime();
		
		setUpdateGraph_MODEL(m);
		setGraphprime_PROV_MODEL(mprov);
		
		setGraphprime_PROV_COPY_NAME("Pprimecopy("+getWithoutTTLGraphprime_source_PROV_NAME()+")" + ".ttl");
		setGraphprime_PROV_star_COPY_MODEL(ModelFactory.createDefaultModel().union(mprov));
		setGraphprime_PROV_star_COPY_NAME("Pprime_star_copy("+getWithoutTTLGraphprime_source_PROV_NAME()+")" + ".ttl");
		setGraphprime_PROV_star_COPY_MODEL(mprov.union(ModelFactory.createDefaultModel().union(mprov)));
		
		//fourth update the provenance and produce new provenance graph	 
		ProvenanceHandler.updateProvOfSourceGraphAfterLoadingUpdate(getWithoutTTLUpdateGraph_COPY_NAME(), this, timeNowStart, timeNowEnd);
	}

	public boolean isUseAllUpdate() {
		return useAllUpdate;
	}

	public void setUseAllUpdate(boolean useAllUpdate) {
		this.useAllUpdate = useAllUpdate;
	}

//	public String getGraphprime_source_URI() {
//		return graphprime_source_URI;
//	}

//	public void setGraphprime_source_URI(String graphprime_source_URI) {
//		this.graphprime_source_URI = graphprime_source_URI;
//	}

	public String getGraphprime_source_PROV_URI() {
		return graphprime_source_PROV_URI;
	}

	public void setGraphprime_source_PROV_URI(String graphprime_source_PROV_URI) {
		this.graphprime_source_PROV_URI = graphprime_source_PROV_URI;
	}

//	public String getGraphprime_source_NAME() {
//		return graphprime_source_NAME;
//	}

//	public void setGraphprime_source_NAME(String graphprime_source_NAME) {
//		this.graphprime_source_NAME = graphprime_source_NAME;
//	}

	public String getGraphprime_source_PROV_NAME() {
		return graphprime_source_PROV_NAME;
	}

	public void setGraphprime_source_PROV_NAME(String graphprime_source_PROV_NAME) {
		this.graphprime_source_PROV_NAME = graphprime_source_PROV_NAME;
	}

//	public String getGraphprime_COPY_NAME() {
//		return graphprime_COPY_NAME;
//	}

//	public void setGraphprime_COPY_NAME(String graphprime_COPY_NAME) {
//		this.graphprime_COPY_NAME = graphprime_COPY_NAME;
//	}

	public String getGraphprime_PROV_COPY_NAME() {
		return graphprime_PROV_COPY_NAME;
	}

	public void setGraphprime_PROV_COPY_NAME(String graphprime_PROV_COPY_NAME) {
		this.graphprime_PROV_COPY_NAME = graphprime_PROV_COPY_NAME;
	}

	public String getGraphprime_PROV_star_COPY_NAME() {
		return graphprime_PROV_star_COPY_NAME;
	}

	public void setGraphprime_PROV_star_COPY_NAME(
			String graphprime_PROV_star_COPY_NAME) {
		this.graphprime_PROV_star_COPY_NAME = graphprime_PROV_star_COPY_NAME;
	}

	public Model getGraphprime_PROV_MODEL() {
		return graphprime_PROV_MODEL;
	}

	public void setGraphprime_PROV_MODEL(Model graphprime_PROV_MODEL) {
		this.graphprime_PROV_MODEL = graphprime_PROV_MODEL;
	}

	public Model getGraphprime_PROV_COPY_MODEL() {
		return graphprime_PROV_COPY_MODEL;
	}

	public void setGraphprime_PROV_COPY_MODEL(Model graphprime_PROV_COPY_MODEL) {
		this.graphprime_PROV_COPY_MODEL = graphprime_PROV_COPY_MODEL;
	}

	public Model getGraphprime_PROV_star_COPY_MODEL() {
		return graphprime_PROV_star_COPY_MODEL;
	}

	public void setGraphprime_PROV_star_COPY_MODEL(
			Model graphprime_PROV_star_COPY_MODEL) {
		this.graphprime_PROV_star_COPY_MODEL = graphprime_PROV_star_COPY_MODEL;
	}

	public String getUpdateGraph_URI() {
		return updateGraph_URI;
	}

	public void setUpdateGraph_URI(String updateGraph_URI) {
		this.updateGraph_URI = updateGraph_URI;
	}

	public String getUpdateGraph_source_NAME() {
		return updateGraph_source_NAME;
	}

	public void setUpdateGraph_source_NAME(String updateGraph_source_NAME) {
		this.updateGraph_source_NAME = updateGraph_source_NAME;
	}
	
	public Model getUpdateGraph_MODEL() {
		return updateGraph_MODEL;
	}

	public void setUpdateGraph_MODEL(Model updateGraph_source_MODEL) {
		this.updateGraph_MODEL = updateGraph_source_MODEL;
	}

	public String getUpdateGraph_COPY_NAME() {
		return updateGraph_COPY_NAME;
	}

	public void setUpdateGraph_COPY_NAME(String updateGraph_COPY_NAME) {
		this.updateGraph_COPY_NAME = updateGraph_COPY_NAME;
	}


	public String getUpdateGraphSubset_NAME() {
		return updateGraphSubset_NAME;
	}

	public void setUpdateGraphSubset_NAME(String updateGraphSubset_NAME) {
		this.updateGraphSubset_NAME = updateGraphSubset_NAME;
	}

	public Model getUpdateGraphSubset_MODEL() {
		return updateGraphSubset_MODEL;
	}

	public void setUpdateGraphSubset_MODEL(Model updateGraphSubset_MODEL) {
		this.updateGraphSubset_MODEL = updateGraphSubset_MODEL;
	}

//	public String getWithoutTTLGraphprime_source_NAME() {
//		String result = graphprime_source_NAME.substring(0, graphprime_source_NAME.length() - 4);
//		return result;
//	}

	public String getWithoutTTLGraphprime_source_PROV_NAME() {
		String result = graphprime_source_PROV_NAME.substring(0, graphprime_source_PROV_NAME.length() - 4);
		return result;
	}

//	public String getWithoutTTLGraphprime_COPY_NAME() {
//		String result = graphprime_COPY_NAME.substring(0, graphprime_COPY_NAME.length() - 4);
//		return result;
//	}

	public String getWithoutTTLGraphprime_PROV_COPY_NAME() {
		String result = graphprime_PROV_COPY_NAME.substring(0, graphprime_PROV_COPY_NAME.length() - 4);
		return result;
	}

	public String getWithoutTTLGraphprime_PROV_star_COPY_NAME() {
		String result = graphprime_PROV_star_COPY_NAME.substring(0, graphprime_PROV_star_COPY_NAME.length() - 4);
		return result;
	}

	public String geWithoutTTLtUpdateGraph_source_NAME() {
		String result = updateGraph_source_NAME.substring(0, updateGraph_source_NAME.length() - 4);
		return result;
	}

	public String getWithoutTTLUpdateGraph_COPY_NAME() {
		String result = updateGraph_COPY_NAME.substring(0, updateGraph_COPY_NAME.length() - 4);
		return result;
	}

	public String getWithoutTTLUpdateGraphSubset_NAME() {
		String result = updateGraphSubset_NAME.substring(0, updateGraphSubset_NAME.length() - 4);
		return result;
	}

	public String getGraphOriginal_source_NAME() {
		return graphOriginal_source_NAME;
	}

	public void setGraphOriginal_source_NAME(String graphOriginal_source_NAME) {
		this.graphOriginal_source_NAME = graphOriginal_source_NAME;
	}

}
