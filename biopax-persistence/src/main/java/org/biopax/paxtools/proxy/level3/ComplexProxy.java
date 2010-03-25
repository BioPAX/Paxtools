/*
 * ComplexProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*; // there is Entity class (and next javax.persistence.* would hide it)
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
public class ComplexProxy extends PhysicalEntityProxy implements Complex {

	public ComplexProxy() {
	}

	// Property COMPONENTS
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinTable(name = "l3complex_components")
	public Set<PhysicalEntity> getComponent() {
		return ((Complex)object).getComponent();
	}

	public void addComponent(PhysicalEntity component) {
		((Complex)object).addComponent(component);
	}

	public void removeComponent(PhysicalEntity component) {
		((Complex)object).removeComponent(component);
	}

	public void setComponent(Set<PhysicalEntity> component) {
		((Complex)object).setComponent(component);
	}

	// Property Component-STOICHIOMETRY

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = StoichiometryProxy.class)
	@JoinTable(name = "l3complex_compo_stoichiom")
	public Set<Stoichiometry> getComponentStoichiometry() {
		return ((Complex)object).getComponentStoichiometry();
	}

	public void addComponentStoichiometry(Stoichiometry stoichiometry) {
		((Complex)object).addComponentStoichiometry(stoichiometry);
	}

	public void removeComponentStoichiometry(Stoichiometry stoichiometry) {
		((Complex)object).removeComponentStoichiometry(stoichiometry);
	}

	public void setComponentStoichiometry(Set<Stoichiometry> stoichiometry) {
		((Complex)object).setComponentStoichiometry(stoichiometry);
	}

	public Set<SimplePhysicalEntity> getSimpleMembers()
	{
		return ((Complex) object).getSimpleMembers();
	}

	public Set<EntityReference> getMemberReferences()
	{
		return ((Complex) object).getMemberReferences();
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Complex.class;
	}
}
