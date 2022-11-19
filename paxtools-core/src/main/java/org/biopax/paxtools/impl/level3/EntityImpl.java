package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.Set;

import static org.biopax.paxtools.util.SetEquivalenceChecker.hasEquivalentIntersection;


public abstract class EntityImpl extends NamedImpl implements Entity
{
// ------------------------------ FIELDS ------------------------------

	private Set<Interaction> participantOf;
	/**
	 * This Set keeps statements describing the availability of this data (e.g. a copyright
	 * statement).
	 */
	private Set<String> availability;

	/**
	 * This Set keeps statements describing the data sources for this data.
	 */
	private Set<Provenance> dataSource;


	/**
	 * This Set keeps evidence related to this entity
	 */
	private Set<Evidence> evidence;

	/**
	 * Helper object for managing names
	 */

// --------------------------- CONSTRUCTORS ---------------------------

	public EntityImpl()
	{
		this.availability = BPCollections.I.createSet();
		this.dataSource = BPCollections.I.createSafeSet();
		this.participantOf = BPCollections.I.createSafeSet();
		this.evidence = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getAvailability()
	{
		return availability;
	}

	public void addAvailability(String availability_text)
	{
		if(availability_text != null && availability_text.length() > 0)
			availability.add(availability_text);
	}

	public void removeAvailability(String availability_text)
	{
		if(availability_text != null)
			this.availability.remove(availability_text);
	}

	public Set<Provenance> getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(Set<Provenance> dataSource)
	{
		this.dataSource = dataSource;
	}

	public void addDataSource(Provenance dataSource)
	{
		if(dataSource != null)
			this.dataSource.add(dataSource);
	}

	public void removeDataSource(Provenance dataSource)
	{
		if(dataSource != null)
			this.dataSource.remove(dataSource);
	}

// --------------------- Interface entity ---------------------

	public Set<Interaction> getParticipantOf()
	{
		return participantOf;
	}


	//
	// observable interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////
	public Set<Evidence> getEvidence()
	{
		return evidence;
	}

	public void addEvidence(Evidence newEvidence)
	{
		if(newEvidence != null)
			this.evidence.add(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence)
	{
		if(oldEvidence != null)
			this.evidence.remove(oldEvidence);
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		boolean equivalence = false;
		if (element instanceof Entity && element.getModelInterface() == this.getModelInterface())
		{
			Entity otherEntity = (Entity) element;
			equivalence =
				hasEquivalentIntersection(dataSource, otherEntity.getDataSource())
			    && hasEquivalentIntersection(
					new ClassFilterSet<>(getXref(), UnificationXref.class),
					new ClassFilterSet<>(otherEntity.getXref(), UnificationXref.class))
			    && hasEquivalentIntersection(evidence, otherEntity.getEvidence());
		}
		return equivalence;
	}

}
