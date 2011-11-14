package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity @org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class NucleicAcidRegionReferenceImpl extends NucleicAcidReferenceImpl
		implements NucleicAcidRegionReference
{

	private SequenceLocation absoluteRegion;

	private Set<SequenceRegionVocabulary> regionType;

	private NucleicAcidReference containerEntityReference;

	private Set<NucleicAcidReference> subRegionOf;

	public NucleicAcidRegionReferenceImpl()
	{
		regionType = new HashSet<SequenceRegionVocabulary>();
		this.subRegionOf = new HashSet<NucleicAcidReference>();
	}


	@ManyToMany(targetEntity = NucleicAcidReferenceImpl.class, mappedBy = "subRegion")
	public Set<NucleicAcidReference> getSubRegionOf()
	{
		return subRegionOf;
	}

	protected void setSubRegionOf(Set<NucleicAcidReference> subRegionOf)
	{
		this.subRegionOf = subRegionOf;
	}


	@ManyToOne(targetEntity = SequenceLocationImpl.class)//, cascade = {CascadeType.ALL})
	public SequenceLocation getAbsoluteRegion()
	{
		return this.absoluteRegion;
	}

	public void setAbsoluteRegion(SequenceLocation absoluteRegion)
	{
		this.absoluteRegion = absoluteRegion;

	}

	@ManyToMany(targetEntity = SequenceRegionVocabularyImpl.class)
	@JoinTable(name = "regionType")
	public Set<SequenceRegionVocabulary> getRegionType()
	{
		return this.regionType;
	}

	public void addRegionType(SequenceRegionVocabulary regionType)
	{
		if (regionType != null) this.regionType.add(regionType);
	}

	public void removeRegionType(SequenceRegionVocabulary regionType)
	{
		if (regionType != null) this.regionType.remove(regionType);
	}

	protected void setRegionType(Set<SequenceRegionVocabulary> regionType)
	{
		this.regionType = regionType;
	}

	@ManyToOne(targetEntity = NucleicAcidReferenceImpl.class)//, cascade = {CascadeType.ALL})
	public NucleicAcidReference getContainerEntityReference()
	{
		return this.containerEntityReference;
	}

	public void setContainerEntityReference(NucleicAcidReference containerEntityReference)
	{
		this.containerEntityReference = containerEntityReference;
	}

}
