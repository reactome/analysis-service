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
public class FunctionalStatusType extends DatabaseObject {
    private String defintion;
    private List<String> name;
    
    public FunctionalStatusType() {
        
    }

    public String getDefintion() {
        return defintion;
    }

    public void setDefintion(String defintion) {
        this.defintion = defintion;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }
    
    
    
}
