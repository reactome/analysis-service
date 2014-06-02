package org.reactome.server.mapper;

import java.util.List;

import org.gk.model.GKInstance;
import org.gk.model.PersistenceAdaptor;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.server.controller.ReactomeModelPostMapper;
import org.reactome.server.controller.ReactomeToRESTfulAPIConverter;
import org.reactome.server.model.DatabaseObject;
import org.reactome.server.model.ReferenceDNASequence;
import org.reactome.server.model.ReferenceGeneProduct;
import org.reactome.server.model.ReferenceSequence;

public class ReferenceSequenceMapper extends ReactomeModelPostMapper {

	@Override
	public void postProcess(GKInstance inst, DatabaseObject obj,
			ReactomeToRESTfulAPIConverter converter) throws Exception {
		// Want to provide ReferenceDatabase and converted URL.
        //setURL(inst, obj);        
	}

	@Override
	public void fillDetailedView(GKInstance inst, DatabaseObject obj,
			ReactomeToRESTfulAPIConverter converter) throws Exception {
		setURL(inst, obj);
		setURLForReferenceDNASequences(inst, obj, converter);
	}
	
	private void setURLForReferenceDNASequences(GKInstance inst,
	                                            DatabaseObject obj,
	                                            ReactomeToRESTfulAPIConverter converter) throws Exception {
	    if (!inst.getSchemClass().isValidAttribute(ReactomeJavaConstants.referenceGene) ||
	        !(obj instanceof ReferenceGeneProduct))
	        return;
	    ReferenceGeneProduct geneProduct = (ReferenceGeneProduct) obj;
	    List<ReferenceDNASequence> genes = geneProduct.getReferenceGene();
	    for (ReferenceDNASequence gene : genes) {
	        GKInstance geneInst = inst.getDbAdaptor().fetchInstance(gene.getDbId());
	        if (geneInst == null)
	            continue;
	        setURL(geneInst, gene);
	    }
	}

	@Override
	public void postShellProcess(GKInstance inst, DatabaseObject obj)
			throws Exception {
		// Want to provide ReferenceSequence and converted URL.
		//setURL(inst, obj);
	}
	
	private void setURL(GKInstance inst, DatabaseObject obj) throws Exception {	
		if (!isValidObject(obj))
			return;
		ReferenceSequence rs = (ReferenceSequence) obj;
        PersistenceAdaptor dba = inst.getDbAdaptor();
        assignValidURLToDatabaseIdentifier(dba, rs);
	}

	@Override
	protected boolean isValidObject(DatabaseObject obj) {
		return true; //(obj instanceof ReferenceDNASequence);
	}

}
