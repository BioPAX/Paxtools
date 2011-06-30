package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

import static org.biopax.paxtools.util.SetEquivalanceChecker.isEquivalentIntersection;


@javax.persistence.Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
		this.availability = new HashSet<String>();
		this.dataSource = new HashSet<Provenance>();
		this.participantOf = new HashSet<Interaction>();
		this.evidence = new HashSet<Evidence>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- ACCESORS and MUTATORS---------------------

	@ElementCollection
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	@FieldBridge(impl=SetStringBridge.class)
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
		if(availability_text != null && availability_text.length() > 0)
			availability.add(availability_text);
	}

	public void removeAvailability(String availability_text)
	{
		if(availability_text != null)
			this.availability.remove(availability_text);
	}

	@ManyToMany(targetEntity = ProvenanceImpl.class)//, cascade={CascadeType.ALL})
	@JoinTable(name="dataSource")
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

	@ManyToMany(targetEntity = InteractionImpl.class, mappedBy = "participant")
	public Set<Interaction> getParticipantOf()
	{
		return participantOf;
	}

	protected void setParticipantOf(Set<Interaction> participantOf)
	{
		this.participantOf= participantOf;
	}

	//
	// observable interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	@ManyToMany(targetEntity = EvidenceImpl.class)
	@JoinTable(name="evidence")
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

	protected void setEvidence(Set<Evidence> newEvidence)
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
					new ClassFilterSet<Xref,UnificationXref>(getXref(), UnificationXref.class),
					new ClassFilterSet<Xref,UnificationXref>(otherEntity.getXref(),
							UnificationXref.class))
			              && isEquivalentIntersection(evidence, otherEntity.getEvidence());
		}
		return equivalance;
	}
}
