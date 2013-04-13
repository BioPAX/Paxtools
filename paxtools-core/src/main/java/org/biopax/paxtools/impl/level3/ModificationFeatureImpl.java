package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 */
@Entity
@Proxy(proxyClass= ModificationFeature.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ModificationFeatureImpl extends EntityFeatureImpl
		implements ModificationFeature
{
    private SequenceModificationVocabulary modificationType;

	public ModificationFeatureImpl() {
	}

    @Override
    public String toString()
    {
        return (modificationType==null?"?":modificationType.getTerm())+"@"+this.getFeatureLocation();
    }

    @Transient
	public Class<? extends ModificationFeature> getModelInterface()
	{
		return ModificationFeature.class;
	}


	@ManyToOne(targetEntity = SequenceModificationVocabularyImpl.class)
	public SequenceModificationVocabulary getModificationType()
	{
		return modificationType;
	}

	public void setModificationType(SequenceModificationVocabulary featureType)
	{
		this.modificationType = featureType;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof ModificationFeature))
			return false;
		
		ModificationFeature that = (ModificationFeature) element;
		boolean value = super.atEquivalentLocation(that);
		if (value)
		{
			SequenceModificationVocabulary yourType = that.getModificationType();
			SequenceModificationVocabulary myType = getModificationType();
			value = (yourType == null) ?
			        myType == null :
			        yourType.isEquivalent(myType);
		}

		return value;
	}

	@Override
	public int equivalenceCode()
	{
		SequenceModificationVocabulary myType = this.getModificationType();
		int code = myType == null ?0:myType.hashCode();
		return code+13*super.locationCode();
	}
}
