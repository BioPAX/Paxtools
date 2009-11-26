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
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;
import org.hibernate.annotations.CollectionOfElements;

/**
 * Proxy for pathway
 */
@Entity(name="l3pathway")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PathwayProxy extends ProcessProxy implements Pathway, Serializable {
	public PathwayProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return Pathway.class;
	}

// Named
/*
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getName() {
		return ((Pathway)object).getName();
	}

	public void addName(String NAME_TEXT) {
		((Pathway)object).addName(NAME_TEXT);
	}

	public void removeName(String NAME_TEXT) {
		((Pathway)object).removeName(NAME_TEXT);
	}

	public void setName(Set<String> newNAME) {
		((Pathway)object).setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return ((Pathway)object).getDisplayName();
	}

	public void setDisplayName(String newDISPLAY_NAME) {
		((Pathway)object).setDisplayName(newDISPLAY_NAME);
	}

	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return ((Pathway)object).getStandardName();
	}

	public void setStandardName(String newSTANDARD_NAME) {
		((Pathway)object).setStandardName(newSTANDARD_NAME);
	}
*/

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
