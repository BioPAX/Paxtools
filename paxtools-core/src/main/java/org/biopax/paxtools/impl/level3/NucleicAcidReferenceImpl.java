package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.util.BPCollections;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
@Proxy(proxyClass= NucleicAcidReference.class)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class NucleicAcidReferenceImpl extends SequenceEntityReferenceImpl implements NucleicAcidReference
{
	private Set<NucleicAcidRegionReference> subRegion;

	public NucleicAcidReferenceImpl()
	{
		this.subRegion = BPCollections.createSafeSet();
	}

	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = NucleicAcidRegionReferenceImpl.class)
	@JoinTable(name = "subRegion")
	public Set<NucleicAcidRegionReference> getSubRegion()
	{
		return subRegion;
	}

	protected void setSubRegion(Set<NucleicAcidRegionReference> subRegion)
	{
		this.subRegion = subRegion;
	}

	public void addSubRegion(NucleicAcidRegionReference regionReference)
	{
		if (regionReference != null)
		{
			subRegion.add(regionReference);
			regionReference.getSubRegionOf().add(this);
		}
	}

	public void removeSubRegion(NucleicAcidRegionReference regionReference)
	{
		if (regionReference != null)
		{
			subRegion.remove(regionReference);
		}
	}
}

