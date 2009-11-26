package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.HashSet;
import java.util.Set;


abstract class EntityImpl extends L3ElementImpl implements Entity {
// ------------------------------ FIELDS ------------------------------

    private HashSet<Interaction> participantOf;
    /**
     * This Set keeps statements describing the availability of this data (e.g. a
     * copyright statement).
     */
    private Set<String> availability;

    /**
     * This Set keeps statements describing the data sources for this data.
     */
    private Set<Provenance> dataSource;

    /**
     * This Set keeps optional Xrefs
     */
    private ReferenceHelper referenceHelper;

    /**
     * This Set keeps evidence related to this entity
     */

    private Set<Evidence> evidence;

    /**
     * Helper object for managing names
     */

    private final NameHelper nameHelper;


// --------------------------- CONSTRUCTORS ---------------------------

    public EntityImpl() {
        this.availability = new HashSet<String>();
        this.dataSource = new HashSet<Provenance>();
        this.referenceHelper = new ReferenceHelper(this);
        this.participantOf = new HashSet<Interaction>();
        this.evidence = new HashSet<Evidence>();
        this.nameHelper = new NameHelper();

    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Xreferrable ---------------------


    public Set<Xref> getXref() {
        return referenceHelper.getXref();
    }

    public void setXref(Set<Xref> Xref) {
        referenceHelper.setXref(Xref);
    }

    public void addXref(Xref Xref) {
        referenceHelper.addXref(Xref);
    }

    public void removeXref(Xref Xref) {
        referenceHelper.removeXref(Xref);
    }

// --------------------- Interface entity ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

    public Set<String> getAvailability() {
        return availability;
    }

    public void setAvailability(Set<String> availability) {
        this.availability = availability;
    }

    public void addAvailability(String availability_text) {
        availability.add(availability_text);
    }

    public void removeAvailability(String availability_text) {
        this.availability.remove(availability_text);
    }

    public Set<Provenance> getDataSource() {
        return dataSource;
    }

    public void setDataSource(Set<Provenance> dataSource) {
        this.dataSource = dataSource;
    }

    public void addDataSource(Provenance dataSource) {
        this.dataSource.add(dataSource);
    }

    public void removeDataSource(Provenance dataSource) {
        this.dataSource.remove(dataSource);
    }

// --------------------- Interface entity ---------------------


    public Set<Interaction> getParticipantsOf() {
        return participantOf;
    }

    //
    // observable interface implementation
    //
    /////////////////////////////////////////////////////////////////////////////

    public Set<Evidence> getEvidence() {
        return evidence;
    }

    public void addEvidence(Evidence evidence) {
        this.evidence.add(evidence);
    }

    public void removeEvidence(Evidence evidence) {
        this.evidence.remove(evidence);
    }

    public void setEvidence(Set<Evidence> evidence) {
        this.evidence = evidence;
    }

    	//
	// named interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	public Set<String> getName()
	{
		return nameHelper.getName();
	}

	public void setName(Set<String> name)
	{
		nameHelper.setName(name);
	}

	public void addName(String name)
	{
		nameHelper.addName(name);
	}

	public void removeName(String name)
	{
		nameHelper.removeName(name);
	}

	public String getDisplayName()
	{
		return nameHelper.getDisplayName();
	}

	public void setDisplayName(String displayName)
	{
		nameHelper.setDisplayName(displayName);
	}

	public String getStandardName()
	{
		return nameHelper.getStandardName();
	}

	public void setStandardName(String standardName)
	{
		nameHelper.setStandardName(standardName);
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element) {
		if(!(element instanceof Entity)) return false;
		Entity that = (Entity) element;
		
		return SetEquivalanceChecker.isEquivalentIntersection(getDataSource(), that.getDataSource())
			&& SetEquivalanceChecker.isEquivalentIntersection(
					new ClassFilterSet<UnificationXref>(getXref(), UnificationXref.class), 
    				new ClassFilterSet<UnificationXref>(that.getXref(), UnificationXref.class)
    				)
    		&& SetEquivalanceChecker.isEquivalentIntersection(getEvidence(), that.getEvidence());
	}
}
