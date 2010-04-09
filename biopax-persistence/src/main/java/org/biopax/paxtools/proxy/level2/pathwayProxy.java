/*
 * PathwayProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for pathway
 */
@Entity(name="l2pathway")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class pathwayProxy extends processProxy implements pathway {
	public pathwayProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return pathway.class;
	}

	public void addPATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT) {
		((pathway)object).addPATHWAY_COMPONENTS(PATHWAY_COMPONENT);
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=bioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public bioSource getORGANISM() {
		return ((pathway)object).getORGANISM();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=pathwayComponentProxy.class)
	@JoinTable(name="l2pathway_pathway_components")
	public Set<pathwayComponent> getPATHWAY_COMPONENTS() {
		return ((pathway)object).getPATHWAY_COMPONENTS();
	}

	public void removePATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT) {
		((pathway)object).removePATHWAY_COMPONENTS(PATHWAY_COMPONENT);
	}

	public void setORGANISM(bioSource ORGANISM) {
		((pathway)object).setORGANISM(ORGANISM);
	}

	public void setPATHWAY_COMPONENTS(Set<pathwayComponent> PATHWAY_COMPONENTS) {
		((pathway)object).setPATHWAY_COMPONENTS(PATHWAY_COMPONENTS);
	}
}
