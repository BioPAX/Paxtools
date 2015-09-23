package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.util.Set;


public class ComplexImpl extends PhysicalEntityImpl implements Complex
{
// ------------------------------ FIELDS ------------------------------

	private Set<PhysicalEntity> component;
	private Set<Stoichiometry> componentStoichiometry;

// --------------------------- CONSTRUCTORS ---------------------------

	public ComplexImpl()
	{
		this.component = BPCollections.I.createSafeSet();
		this.componentStoichiometry = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends Complex> getModelInterface()
	{
		return Complex.class;
	}

// --------------------- Interface Complex ---------------------

// --------------------- ACCESORS and MUTATORS---------------------
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

	public Set<SimplePhysicalEntity> getSimpleMembers()
	{
		return getSimpleMembers(BPCollections.I.<SimplePhysicalEntity>createSet());
	}

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

	public Set<EntityReference> getMemberReferences()
	{
		Set<EntityReference> set = BPCollections.I.createSet();

		for (SimplePhysicalEntity spe : getSimpleMembers())
		{
			EntityReference er = spe.getEntityReference();
			if(er!=null) set.add(er);
		}
		return set;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		return (element instanceof Complex) &&
				SetEquivalenceChecker.isEquivalent(this.getComponent(), ((Complex) element).getComponent())
					&& super.semanticallyEquivalent(element);
	}
}