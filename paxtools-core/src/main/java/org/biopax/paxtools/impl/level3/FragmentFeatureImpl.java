package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.FragmentFeature;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
 @Proxy(proxyClass= FragmentFeature.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
		if(!(element instanceof FragmentFeature))
			return false;
		else
			return super.atEquivalentLocation(((FragmentFeature) element));
	}
}
