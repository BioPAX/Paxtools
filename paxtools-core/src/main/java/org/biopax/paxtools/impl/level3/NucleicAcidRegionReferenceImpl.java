package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Store;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
@Proxy(proxyClass= NucleicAcidReference.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = NucleicAcidReferenceImpl.class, mappedBy = "subRegion")
	public Set<NucleicAcidReference> getSubRegionOf()
	{
		return subRegionOf;
	}

	protected void setSubRegionOf(Set<NucleicAcidReference> subRegionOf)
	{
		this.subRegionOf = subRegionOf;
	}


	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@ManyToOne(targetEntity = SequenceLocationImpl.class)
	public SequenceLocation getAbsoluteRegion()
	{
		return this.absoluteRegion;
	}

	public void setAbsoluteRegion(SequenceLocation absoluteRegion)
	{
		this.absoluteRegion = absoluteRegion;

	}

	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

}
