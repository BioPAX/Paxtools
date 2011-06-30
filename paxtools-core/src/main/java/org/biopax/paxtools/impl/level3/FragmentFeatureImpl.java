package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.FragmentFeature;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
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
