/*
 * Created on Jun 7, 2013
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
public class EntityFunctionalStatus extends DatabaseObject {
    private List<FunctionalStatus> functionalStatus;
    private PhysicalEntity physicalEntity;
    
    public EntityFunctionalStatus() {
    }

    public List<FunctionalStatus> getFunctionalStatus() {
        return functionalStatus;
    }

    public void setFunctionalStatus(List<FunctionalStatus> functionalStatus) {
        this.functionalStatus = functionalStatus;
    }

    public PhysicalEntity getPhysicalEntity() {
        return physicalEntity;
    }

    public void setPhysicalEntity(PhysicalEntity physicalEntity) {
        this.physicalEntity = physicalEntity;
    }
    
    
    
}
