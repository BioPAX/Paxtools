/*
 * PathwayProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Proxy for pathway
 */
@Entity(name="l3pathway")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PathwayProxy extends ProcessProxy implements Pathway {
	public PathwayProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return Pathway.class;
	}

// pathway

	// Property PATHWAY-COMPONENTS

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=ProcessProxy.class)
	@JoinTable(name="l3pathway_pathway_component")
	public Set<Process> getPathwayComponent() {
		return ((Pathway)object).getPathwayComponent();
	}

	public void addPathwayComponent(Process PATHWAY_COMPONENT) {
		((Pathway)object).addPathwayComponent(PATHWAY_COMPONENT);
	}

	public void removePathwayComponent(Process PATHWAY_COMPONENT) {
		((Pathway)object).removePathwayComponent(PATHWAY_COMPONENT);
	}

	public void setPathwayComponent(Set<Process> PATHWAY_COMPONENTS) {
		((Pathway)object).setPathwayComponent(PATHWAY_COMPONENTS);
	}

	// Property PATHWAY-ORDER

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PathwayStepProxy.class)
	@JoinTable(name="l3pathway_pathway_order")
	public Set<PathwayStep> getPathwayOrder() {
		return ((Pathway)object).getPathwayOrder();
	}

	public void addPathwayOrder(PathwayStep newPathway_ORDER) {
		((Pathway)object).addPathwayOrder(newPathway_ORDER);
	}

	public void removePathwayOrder(PathwayStep oldPathway_ORDER) {
		((Pathway)object).removePathwayOrder(oldPathway_ORDER);
	}

	public void setPathwayOrder(Set<PathwayStep> newPathway_ORDER) {
		((Pathway)object).setPathwayOrder(newPathway_ORDER);
	}

	// Property ORGANISM

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= BioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public BioSource getOrganism() {
		return ((Pathway)object).getOrganism();
	}

	public void setOrganism(BioSource ORGANISM) {
		((Pathway)object).setOrganism(ORGANISM);
	}
}
