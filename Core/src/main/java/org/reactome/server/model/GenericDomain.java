package org.reactome.server.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericDomain extends Domain {
	private List<Domain> hasInstance;
	
	public GenericDomain() {
	}

	public List<Domain> getHasInstance() {
		return hasInstance;
	}

	public void setHasInstance(List<Domain> hasInstance) {
		this.hasInstance = hasInstance;
	}
	
}
