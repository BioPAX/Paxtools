package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

@Entity
abstract class NucleicAcidRegionReferenceImpl<T extends NucleicAcidRegionReference>
		extends SequenceEntityReferenceImpl
		implements NucleicAcidRegionReference<T>
{
	private Set<T> subRegion;
	private SequenceLocation absoluteRegion;
	private Set<SequenceRegionVocabulary> regionType;
	private NucleicAcidReference containerEntityReference;
	private Set<T> subRegionOf;

	NucleicAcidRegionReferenceImpl()
	{
	}

	@ManyToMany(targetEntity = NucleicAcidRegionReferenceImpl.class)
	public Set<T> getSubRegion()
	{
		return subRegion;
	}

	protected void setSubRegion(Set<T> subRegion)
	{
		this.subRegion = subRegion;
	}

	public void addSubRegion(T regionReference)
	{
		subRegion.add(regionReference);
		this.subRegionOf.add(regionReference);
	}

	public void removeSubRegion(T regionReference)
	{
		subRegion.remove(regionReference);
		this.subRegionOf.remove(regionReference);

	}

	public Set<T> getSubRegionOf()
	{
		return subRegionOf;
	}

	@OneToOne(targetEntity = SequenceLocationImpl.class)
	public SequenceLocation getAbsoluteRegion()
	{
		return this.absoluteRegion;
	}

	public void setAbsoluteRegion(SequenceLocation absoluteRegion)
	{
		this.absoluteRegion = absoluteRegion;

	}

	@ManyToMany(targetEntity = SequenceRegionVocabularyImpl.class)
	public Set<SequenceRegionVocabulary> getRegionType()
	{
		return this.regionType;
	}

	public void addRegionType(SequenceRegionVocabulary regionType)
	{
		this.regionType.add(regionType);
	}

	public void removeRegionType(SequenceRegionVocabulary regionType)
	{
		this.regionType.remove(regionType);
	}

	protected void setRegionType(Set<SequenceRegionVocabulary> regionType)
	{
		this.regionType = regionType;
	}

	@OneToMany(targetEntity = NucleicAcidReferenceImpl.class)
	public NucleicAcidReference getContainerEntityReference()
	{
		return this.containerEntityReference;
	}
	public void setContainerEntityReference(NucleicAcidReference containerEntityReference)
	{
		this.containerEntityReference = containerEntityReference;
	}

}
