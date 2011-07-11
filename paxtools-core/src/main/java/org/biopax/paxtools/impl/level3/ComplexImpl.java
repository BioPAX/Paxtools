package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ComplexImpl extends PhysicalEntityImpl implements Complex
{
// ------------------------------ FIELDS ------------------------------

	private Set<PhysicalEntity> component;
	private Set<Stoichiometry> componentStoichiometry;

// --------------------------- CONSTRUCTORS ---------------------------

	public ComplexImpl()
	{
		this.component = new HashSet<PhysicalEntity>();
		this.componentStoichiometry = new HashSet<Stoichiometry>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	@Transient
	public Class<? extends Complex> getModelInterface()
	{
		return Complex.class;
	}

// --------------------- Interface Complex ---------------------

// --------------------- ACCESORS and MUTATORS---------------------
	
	@ManyToMany(targetEntity = PhysicalEntityImpl.class)
	@JoinTable(name="component") 
	public Set<PhysicalEntity> getComponent()
	{
		return component;
	}

	public void addComponent(PhysicalEntity component)
	{
		if (component != null) {
			this.component.add(component);
			component.getComponentOf().add(this);
		}
	}

	public void removeComponent(PhysicalEntity component)
	{
		if (component != null) {
			this.component.remove(component);
			component.getComponentOf().add(null);
		}
	}

	protected void setComponent(Set<PhysicalEntity> component)
	{
		this.component = component;
	}

	@ManyToMany(targetEntity = StoichiometryImpl.class)
	@JoinTable(name="complexstoichiometry")		
	public Set<Stoichiometry> getComponentStoichiometry()
	{
		return componentStoichiometry;
	}

	public void addComponentStoichiometry(
			Stoichiometry stoichiometry)
	{
		if(stoichiometry != null)
			this.componentStoichiometry.add(stoichiometry);
	}

	public void removeComponentStoichiometry(
			Stoichiometry stoichiometry)
	{
		if(stoichiometry != null)
			this.componentStoichiometry.remove(stoichiometry);
	}

	protected void setComponentStoichiometry(Set<Stoichiometry> stoichiometry)
	{
		this.componentStoichiometry = stoichiometry;
	}

	@Transient
	public Set<SimplePhysicalEntity> getSimpleMembers()
	{
		return getSimpleMembers(new HashSet<SimplePhysicalEntity>());
	}

	@Transient
	protected Set<SimplePhysicalEntity> getSimpleMembers(Set<SimplePhysicalEntity> set)
	{
		for (PhysicalEntity pe : this.getComponent())
		{
			if (pe instanceof SimplePhysicalEntity)
			{
				set.add((SimplePhysicalEntity) pe);
			}
			else if (pe instanceof ComplexImpl)
			{
				((ComplexImpl) pe).getSimpleMembers(set);
			}
		}
		return set;
	}

	@Transient
	public Set<EntityReference> getMemberReferences()
	{
		Set<EntityReference> set = new HashSet<EntityReference>();

		for (SimplePhysicalEntity spe : getSimpleMembers())
		{
			EntityReference er = spe.getEntityReference();
			if(er!=null)
				set.add(er); //TODO - ignoring reactome ERs
		}
		return set;
	}

	@Transient
	public Class<? extends PhysicalEntity> getPhysicalEntityClass()
	{
		return Complex.class;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		return SetEquivalanceChecker
				.isEquivalent(this.getComponent(), ((Complex) element).getComponent())
				&& super.semanticallyEquivalent(element);
	}
}