package org.reactome.server.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Domain extends DatabaseObject {
	private List<String> name;
	
	public Domain() {
	}

	public List<String> getName() {
		return name;
	}

	public void setName(List<String> name) {
		this.name = name;
	}
	
}
