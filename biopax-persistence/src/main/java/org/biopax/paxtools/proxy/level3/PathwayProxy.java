/*
 * PathwayProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for pathway
 */
@Entity(name="l3pathway")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PathwayProxy extends ProcessProxy<Pathway> implements Pathway {

// pathway

	// Property PATHWAY-COMPONENTS

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=ProcessProxy.class)
	@JoinTable(name="l3pathway_pathway_component")
	public Set<Process> getPathwayComponent() {
		return object.getPathwayComponent();
	}

	public void addPathwayComponent(Process PATHWAY_COMPONENT) {
		object.addPathwayComponent(PATHWAY_COMPONENT);
	}

	public void removePathwayComponent(Process PATHWAY_COMPONENT) {
		object.removePathwayComponent(PATHWAY_COMPONENT);
	}

	public void setPathwayComponent(Set<Process> PATHWAY_COMPONENTS) {
		object.setPathwayComponent(PATHWAY_COMPONENTS);
	}

	// Property PATHWAY-ORDER

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PathwayStepProxy.class)
	@JoinTable(name="l3pathway_pathway_order")
	public Set<PathwayStep> getPathwayOrder() {
		return object.getPathwayOrder();
	}

	public void addPathwayOrder(PathwayStep newPathway_ORDER) {
		object.addPathwayOrder(newPathway_ORDER);
	}

	public void removePathwayOrder(PathwayStep oldPathway_ORDER) {
		object.removePathwayOrder(oldPathway_ORDER);
	}

	public void setPathwayOrder(Set<PathwayStep> newPathway_ORDER) {
		object.setPathwayOrder(newPathway_ORDER);
	}

	// Property ORGANISM

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= BioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public BioSource getOrganism() {
		return object.getOrganism();
	}

	public void setOrganism(BioSource ORGANISM) {
		object.setOrganism(ORGANISM);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Pathway.class;
	}

	@Transient
	public Set<Control> getControllerOf()
	{
		return object.getControllerOf();
	}
}
