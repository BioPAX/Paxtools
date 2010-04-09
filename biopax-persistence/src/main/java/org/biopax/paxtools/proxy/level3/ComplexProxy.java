/*
 * ComplexProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*; // there is Entity class (and next javax.persistence.* would hide it)
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

/**
 * Proxy for complex
 */
@javax.persistence.Entity(name = "l3complex")
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ComplexProxy extends PhysicalEntityProxy<Complex> implements Complex {

	// Property COMPONENTS
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinTable(name = "l3complex_components")
	public Set<PhysicalEntity> getComponent() {
		return object.getComponent();
	}

	public void addComponent(PhysicalEntity component) {
		object.addComponent(component);
	}

	public void removeComponent(PhysicalEntity component) {
		object.removeComponent(component);
	}

	public void setComponent(Set<PhysicalEntity> component) {
		object.setComponent(component);
	}

	// Property Component-STOICHIOMETRY

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = StoichiometryProxy.class)
	@JoinTable(name = "l3complex_compo_stoichiom")
	public Set<Stoichiometry> getComponentStoichiometry() {
		return object.getComponentStoichiometry();
	}

	public void addComponentStoichiometry(Stoichiometry stoichiometry) {
		object.addComponentStoichiometry(stoichiometry);
	}

	public void removeComponentStoichiometry(Stoichiometry stoichiometry) {
		object.removeComponentStoichiometry(stoichiometry);
	}

	public void setComponentStoichiometry(Set<Stoichiometry> stoichiometry) {
		object.setComponentStoichiometry(stoichiometry);
	}

	@Transient
	public Set<SimplePhysicalEntity> getSimpleMembers()
	{
		return object.getSimpleMembers();
	}

	@Transient
	public Set<EntityReference> getMemberReferences()
	{
		return object.getMemberReferences();
	}

	@Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return Complex.class;
	}
}
