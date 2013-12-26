package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetEquivalenceChecker;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.Set;
import javax.persistence.Entity;

@Entity
@Proxy(proxyClass= Complex.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ComplexImpl extends PhysicalEntityImpl implements Complex
{
// ------------------------------ FIELDS ------------------------------

	private Set<PhysicalEntity> component;
	private Set<Stoichiometry> componentStoichiometry;

// --------------------------- CONSTRUCTORS ---------------------------

	public ComplexImpl()
	{
		this.component = BPCollections.createSafeSet();
		this.componentStoichiometry = BPCollections.createSafeSet();
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
	
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
			component.getComponentOf().remove(this);
		}
	}

	protected void setComponent(Set<PhysicalEntity> component)
	{
		this.component = component;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
		return getSimpleMembers(BPCollections.<SimplePhysicalEntity>createSet());
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
		Set<EntityReference> set = BPCollections.createSet();

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
		
		return SetEquivalenceChecker
				.isEquivalent(this.getComponent(), ((Complex) element).getComponent())
				&& super.semanticallyEquivalent(element);
	}
}