package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_ENTITY)
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

	@ManyToMany(targetEntity = PhysicalEntityImpl.class, cascade={CascadeType.PERSIST})
	@JoinTable(name="component") 	
	public Set<PhysicalEntity> getComponent()
	{
		return component;
	}

	public void addComponent(PhysicalEntity component)
	{
		this.component.add(component);
		component.getComponentOf().add(this);
	}

	public void removeComponent(PhysicalEntity component)
	{
		this.component.remove(component);
		component.getComponentOf().add(null);
	}

	public void setComponent(Set<PhysicalEntity> component)
	{
		this.component = component;
		for (PhysicalEntity PhysicalEntity : component)
		{
			PhysicalEntity.getComponentOf().add(this);
		}
	}

	@OneToMany(targetEntity = StoichiometryImpl.class, cascade={CascadeType.ALL})
	@JoinTable(name="stoichiometry")		
	public Set<Stoichiometry> getComponentStoichiometry()
	{
		return componentStoichiometry;
	}

	public void addComponentStoichiometry(
			Stoichiometry stoichiometry)
	{

		this.componentStoichiometry.add(stoichiometry);
	}

	public void removeComponentStoichiometry(
			Stoichiometry stoichiometry)
	{
		this.componentStoichiometry.remove(stoichiometry);
	}

	protected void setComponentStoichiometry(
			Set<Stoichiometry> stoichiometry)
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
			set.add(spe.getEntityReference());
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
				.isEquivalent(this.getComponent(), ((Complex) element).getComponent());
	}
}