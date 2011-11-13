package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
			collectSimpleMembersRecursive(pe, set);
		}
		return set;
	}

	protected void collectSimpleMembersRecursive(PhysicalEntity pe, Set<SimplePhysicalEntity> set)
	{
		if (pe instanceof ComplexImpl && pe != this)
		{
			((ComplexImpl) pe).collectSimpleMembersRecursive(pe, set);
		}
		else if (pe instanceof SimplePhysicalEntity)
		{
			set.add((SimplePhysicalEntity) pe);
		}
		for (PhysicalEntity mem : pe.getMemberPhysicalEntity())
		{
			collectSimpleMembersRecursive(mem, set);
		}
	}

	@Transient
	public Set<EntityReference> getMemberReferences()
	{
		Set<EntityReference> set = new HashSet<EntityReference>();

		for (SimplePhysicalEntity spe : getSimpleMembers())
		{
			EntityReference er = spe.getEntityReference();
			if(er!=null) set.add(er);
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
		if (!(element instanceof Complex))
			return false;
		
		return SetEquivalanceChecker
				.isEquivalent(this.getComponent(), ((Complex) element).getComponent())
				&& super.semanticallyEquivalent(element);
	}
}