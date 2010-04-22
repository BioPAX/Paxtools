package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;


import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

import static org.biopax.paxtools.model.SetEquivalanceChecker.*;


@javax.persistence.Entity
abstract class EntityImpl extends NamedImpl implements Entity
{
// ------------------------------ FIELDS ------------------------------

	private HashSet<Interaction> participantOf;
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
		this.availability = new HashSet<String>();
		this.dataSource = new HashSet<Provenance>();
		this.participantOf = new HashSet<Interaction>();
		this.evidence = new HashSet<Evidence>();
	

	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- ACCESORS and MUTATORS---------------------

	@ElementCollection
	public Set<String> getAvailability()
	{
		return availability;
	}

	public void setAvailability(Set<String> availability)
	{
		this.availability = availability;
	}

	public void addAvailability(String availability_text)
	{
		availability.add(availability_text);
	}

	public void removeAvailability(String availability_text)
	{
		this.availability.remove(availability_text);
	}

	@ManyToMany(cascade = {CascadeType.MERGE}, targetEntity = ProvenanceImpl.class)
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
		this.dataSource.add(dataSource);
	}

	public void removeDataSource(Provenance dataSource)
	{
		this.dataSource.remove(dataSource);
	}

// --------------------- Interface entity ---------------------

	@ManyToMany(targetEntity = InteractionImpl.class, mappedBy = participants)
	public Set<Interaction> getParticipantsOf()
	{
		return participantOf;
	}

	//
	// observable interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceImpl.class)
	public Set<Evidence> getEvidence()
	{
		return evidence;
	}

	public void addEvidence(Evidence newEvidence)
	{
		this.evidence.add(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence)
	{
		this.evidence.remove(oldEvidence);
	}

	private void setEvidence(Set<Evidence> newEvidence)
	{
		this.evidence = newEvidence;
	}


	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		boolean equivalance = false;
		if (element instanceof Entity)
		{
			Entity otherEntity = (Entity) element;

			equivalance = isEquivalentIntersection(
					dataSource, otherEntity.getDataSource())
			              && isEquivalentIntersection(
					new ClassFilterSet<UnificationXref>(getXref(), UnificationXref.class),
					new ClassFilterSet<UnificationXref>(otherEntity.getXref(),
							UnificationXref.class))
			              && isEquivalentIntersection(evidence, otherEntity.getEvidence());
		}
		return equivalance;
	}
}
