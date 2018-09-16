package operators;

import java.io.IOException;

import miniT.ProvenanceHandler;

import org.apache.jena.rdf.model.Model;

import utilities.*;
import utilities.graph.GraphInSystem;
import utilities.graph.SourceGraphInSystem;

public class ParentOperator 
{
	protected String graphStoresLocal_URI = Constants.LOCAL_URI;
	protected String DATASET_ORIGINALS = Constants.DATASET_ORIGINALS;
	
	protected SourceGraphInSystem a1, b2;
	
	protected String graphStOpType; // the type of set theoretic operation
	protected String stOpActivityName; // the name to be given to the st op activity in Provenance Handler
	protected String graphSTA1B2_NAME; // the name of set theoretic resulting graph
	protected Model graphSTA1B2_MODEL; // the set theoretic resulting graph
	protected GraphInSystem c3;
	
	protected ParentOperator (String graphA1_source_URI, String graphA1_source_PROV_URI, 
							String graphB2_source_URI, String graphB2_source_PROV_URI) 
	{
		a1 = new SourceGraphInSystem(graphA1_source_URI, graphA1_source_PROV_URI);
		b2 = new SourceGraphInSystem(graphB2_source_URI, graphB2_source_PROV_URI);
	}

	protected void loadBothGraphsAndTheirProvs(String graphA1_source_NAME, String graphA1_source_PROV_NAME,
											String graphB2_source_NAME, String graphB2_source_PROV_NAME) throws IOException 
	{
		a1.loadGraphAndItsProv(DATASET_ORIGINALS, graphA1_source_NAME, graphA1_source_PROV_NAME);
		b2.loadGraphAndItsProv(DATASET_ORIGINALS, graphB2_source_NAME, graphB2_source_PROV_NAME);
	}
	
	protected void initializeC (String graphC3_NAME, String graphC3_PROV_NAME) 
	{
		c3 = new GraphInSystem(graphC3_NAME, graphC3_PROV_NAME);
		Model mUnionProvs = GraphSTUtilities.modelUnion(a1.getGraph_PROV_star_COPY_MODEL(), b2.getGraph_PROV_star_COPY_MODEL());
		Model c3firstProv = ProvenanceHandler.createInitialC3Prov(mUnionProvs, c3.getGraph_PROV_NAME(), a1.getGraph_PROV_star_COPY_NAME(), b2.getGraph_PROV_star_COPY_NAME());
		c3.setGraph_PROV_MODEL(c3firstProv);
	}
	
	protected void createSTGraph (String graphSTA1B2_NAME) 
	{
		setGraphSTA1B2_NAME(graphSTA1B2_NAME);
		
		String [] timeNowStart = MiscUtilities.getTime();
		setStOpActivityName(ProvenanceHandler.createNameOfSTOp(a1.getGraph_COPY_NAME(), b2.getGraph_COPY_NAME(), graphStOpType, timeNowStart[1]));
		setGraphSTA1B2_MODEL(GraphSTUtilities.applyGraphSt(a1.getGraph_MODEL(), b2.getGraph_MODEL(), graphStOpType));
		String [] timeNowEnd = MiscUtilities.getTime();

		c3.setGraph_BASE_MODEL(graphSTA1B2_MODEL);
		
		ProvenanceHandler.updateC3SetOperation(stOpActivityName, this, timeNowStart, timeNowEnd);
		
	}
	
	public String getStOpActivityName() {
		return stOpActivityName;
	}

	public void setStOpActivityName(String stOpActivityName) {
		this.stOpActivityName = stOpActivityName;
	}

	protected String getGraphStoresLocal_URI() {
		return graphStoresLocal_URI;
	}

	public void setGraphStoresLocal_URI(String graphStoresLocal_URI) 
	{
		this.graphStoresLocal_URI = graphStoresLocal_URI;
		a1.setGraphStoresLocal_URI(graphStoresLocal_URI);
		b2.setGraphStoresLocal_URI(graphStoresLocal_URI);
	}

	public String getDATASET_ORIGINALS() {
		return DATASET_ORIGINALS;
	}

	public void setDATASET_ORIGINALS(String dATASET_ORIGINALS) {
		DATASET_ORIGINALS = dATASET_ORIGINALS;
	}

	public SourceGraphInSystem getA1() {
		return a1;
	}

	public void setA1(SourceGraphInSystem a1) {
		this.a1 = a1;
	}

	public SourceGraphInSystem getB2() {
		return b2;
	}

	public void setB2(SourceGraphInSystem b2) {
		this.b2 = b2;
	}
	public String getGraphSTA1B2_NAME() {
		return graphSTA1B2_NAME;
	}

	public void setGraphSTA1B2_NAME(String graphSTA1B2_NAME) {
		this.graphSTA1B2_NAME = graphSTA1B2_NAME;
	}

	public Model getGraphSTA1B2_MODEL() {
		return graphSTA1B2_MODEL;
	}

	public void setGraphSTA1B2_MODEL(Model graphSTA1B2_MODEL) {
		this.graphSTA1B2_MODEL = graphSTA1B2_MODEL;
	}

	public GraphInSystem getC3() {
		return c3;
	}

	public void setC3(GraphInSystem c3) {
		this.c3 = c3;
	}
	
	public String getGraphStOpType() {
		return graphStOpType;
	}
	
	public void setGraphStOpType(String graphStOpType) {
		this.graphStOpType = graphStOpType;
	}
}
