package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.FragmentFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Set;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class FragmentFeatureImpl extends EntityFeatureImpl implements FragmentFeature
{
	public FragmentFeatureImpl() {
	}
	
	@Override @Transient
	public Class<? extends FragmentFeature> getModelInterface()
	{
		return FragmentFeature.class;
	}

	/**
	 * Inverse of {@link org.biopax.paxtools.model.level3.PhysicalEntity#getFeature()}
	 */
	@Override public Set<PhysicalEntity> getFeatureOf()
	{
		return featureOf;
	}

	@Override
	public int equivalenceCode()
	{
		return super.locationCode();
	}


	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		return super.atEquivalentLocation(((FragmentFeature) element));
	}
}
