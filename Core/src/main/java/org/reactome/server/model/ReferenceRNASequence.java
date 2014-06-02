/*
 * Created on Jun 27, 2012
 *
 */
package org.reactome.server.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gwu
 *
 */
@XmlRootElement
public class ReferenceRNASequence extends ReferenceSequence {
    private List<ReferenceDNASequence> referenceGene;
    
    public ReferenceRNASequence() {
        
    }

    public List<ReferenceDNASequence> getReferenceGene() {
        return referenceGene;
    }

    public void setReferenceGene(List<ReferenceDNASequence> referenceGene) {
        this.referenceGene = referenceGene;
    }
    
}
