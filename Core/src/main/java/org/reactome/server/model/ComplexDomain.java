package org.reactome.server.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ComplexDomain extends Domain {
	// Based on the data model, only PhysicalEntity and Domain 
	// should be added into this List.
	private List<DatabaseObject> hasComponent;
	
	public ComplexDomain() {
	}

	public List<DatabaseObject> getHasComponent() {
		return hasComponent;
	}

	public void setHasComponent(List<DatabaseObject> hasComponent) {
		this.hasComponent = hasComponent;
	}
	

}
