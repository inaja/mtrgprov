package utilities.graph;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class GraphInSystem {
	private String graph_NAME;
	private String graph_BASE_NAME, graph_INFS_NAME;
	private Model graph_BASE_MODEL, graph_INFS_MODEL; 
	
	private String graph_PROV_NAME;
	private Model graph_PROV_MODEL;
	
	private String graph_UpdatedNAME;// = Config.graphC3Updated_NAME;
	private String graph_UPDATEDBASE_NAME, graph_UPDATEDINFS_NAME;
	//private Model graph_UpdatedMODEL;
	private Model graph_UPDATEDBASE_MODEL, graph_UPDATEDINFS_MODEL; 
	
	/** This constructor is to be used by Operator and Traditional Operator.
	 * This constructor sets graph_NAME, graph_BASE_NAME, graph_INFS_NAME, and graph_PROV_NAME.
	 * Additionally it initializes graph_PROV_MODEL.
	 * @param graph_NAME : the name to be assigned to the graph.
	 * @param graph_PROV_NAME : the name to be assigned to the PROV graph
	 */
	public GraphInSystem(String graph_NAME, String graph_PROV_NAME) 
	{
		this.graph_NAME = graph_NAME;
		this.graph_BASE_NAME = graph_NAME.substring(0, graph_NAME.length() - 4) + "Base.ttl";
		this.graph_INFS_NAME = graph_NAME.substring(0, graph_NAME.length() - 4) + "Infs.ttl";
		this.graph_PROV_NAME = graph_PROV_NAME;
		graph_PROV_MODEL = ModelFactory.createDefaultModel();
	}
	
	/** This constructor is to be used by UpdatedOperator.
	 * It graph_NAME, graph_BASE_NAME, graph_INFS_NAME, and graph_PROV_NAME.
	 * Additionally it initializes graph_PROV_MODEL.
	 * @param graph_NAME : the name to be assigned to the graph.
	 * @param graph_PROV_NAME : the name to be assigned to the PROV graph
	
	 */
	public GraphInSystem(String graph_NAME, String graph_PROV_NAME, String graphC3Updated_NAME) 
	{
		this.graph_NAME = graph_NAME;
		this.graph_BASE_NAME = graph_NAME.substring(0, graph_NAME.length() - 4) + "Base.ttl";
		this.graph_INFS_NAME = graph_NAME.substring(0, graph_NAME.length() - 4) + "Infs.ttl";
		this.graph_PROV_NAME = graph_PROV_NAME;
		graph_PROV_MODEL = ModelFactory.createDefaultModel();
		
		this.graph_UpdatedNAME = graphC3Updated_NAME;
		this.graph_UPDATEDBASE_NAME = graph_UpdatedNAME.substring(0, graph_UpdatedNAME.length() - 4) + "Base.ttl";
		this.graph_UPDATEDINFS_NAME = graph_UpdatedNAME.substring(0, graph_UpdatedNAME.length() - 4) + "Infs.ttl";;
	}
	
	public String getGraph_NAME() {
		return graph_NAME;
	}
	public String getWithoutTTL_Graph_NAME() {
		return graph_NAME.substring(0, graph_NAME.length() - 4);
	}

	public void setGraph_NAME(String graph_NAME) {
		this.graph_NAME = graph_NAME;
	}

	public String getGraph_PROV_NAME() {
		return graph_PROV_NAME;
	}
	public String getWithoutTTL_Graph_PROV_NAME() {
		return graph_PROV_NAME.substring(0, graph_PROV_NAME.length() - 4);
	}

	public void setGraph_PROV_NAME(String graph_PROV_NAME) {
		this.graph_PROV_NAME = graph_PROV_NAME;
	}

	public Model getGraph_PROV_MODEL() {
		return graph_PROV_MODEL;
	}

	public void setGraph_PROV_MODEL(Model graph_PROV_MODEL) {
		this.graph_PROV_MODEL = graph_PROV_MODEL;
	}
	
	public String getGraph_BASE_NAME() {
		return graph_BASE_NAME;
	}
	public String getWithoutTTL_Graph_BASE_NAME() {
		return graph_BASE_NAME.substring(0, graph_BASE_NAME.length() - 4);
	}

	public void setGraph_BASE_NAME(String graph_BASE_NAME) {
		this.graph_BASE_NAME = graph_BASE_NAME;
	}

	public String getGraph_INFS_NAME() {
		return graph_INFS_NAME;
	}
	public String getWithoutTTL_Graph_INFS_NAME() {
		return graph_INFS_NAME.substring(0, graph_INFS_NAME.length() - 4);
	}

	public void setGraph_INFS_NAME(String graph_INFS_NAME) {
		this.graph_INFS_NAME = graph_INFS_NAME;
	}

	public Model getGraph_BASE_MODEL() {
		return graph_BASE_MODEL;
	}

	public void setGraph_BASE_MODEL(Model graph_BASE_MODEL) {
		this.graph_BASE_MODEL = graph_BASE_MODEL;
	}

	public Model getGraph_INFS_MODEL() {
		return graph_INFS_MODEL;
	}

	public void setGraph_INFS_MODEL(Model graph_INFS_MODEL) {
		this.graph_INFS_MODEL = graph_INFS_MODEL;
	}

	public String getGraph_UpdatedNAME() {
		return graph_UpdatedNAME;
	}

	public void setGraph_UpdatedNAME(String graph_UpdatedNAME) {
		this.graph_UpdatedNAME = graph_UpdatedNAME;
	}

	public String getGraph_UPDATEDBASE_NAME() {
		return graph_UPDATEDBASE_NAME;
	}

	public void setGraph_UPDATEDBASE_NAME(String graph_UPDATEDBASE_NAME) {
		this.graph_UPDATEDBASE_NAME = graph_UPDATEDBASE_NAME;
	}

	public String getGraph_UPDATEDINFS_NAME() {
		return graph_UPDATEDINFS_NAME;
	}

	public void setGraph_UPDATEDINFS_NAME(String graph_UPDATEDINFS_NAME) {
		this.graph_UPDATEDINFS_NAME = graph_UPDATEDINFS_NAME;
	}

	public Model getGraph_UPDATEDBASE_MODEL() {
		return graph_UPDATEDBASE_MODEL;
	}

	public void setGraph_UPDATEDBASE_MODEL(Model graph_UPDATEDBASE_MODEL) {
		this.graph_UPDATEDBASE_MODEL = graph_UPDATEDBASE_MODEL;
	}

	public Model getGraph_UPDATEDINFS_MODEL() {
		return graph_UPDATEDINFS_MODEL;
	}

	public void setGraph_UPDATEDINFS_MODEL(Model graph_UPDATEDINFS_MODEL) {
		this.graph_UPDATEDINFS_MODEL = graph_UPDATEDINFS_MODEL;
	}
	
}
