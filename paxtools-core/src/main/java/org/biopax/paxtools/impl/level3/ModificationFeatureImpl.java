package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.model.BioPAXElement;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 */
@Entity
class ModificationFeatureImpl extends EntityFeatureImpl
		implements ModificationFeature
{
	@Transient
	public Class<? extends ModificationFeature> getModelInterface()
	{
		return ModificationFeature.class;
	}

	private SequenceModificationVocabulary modificationType;


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
		ModificationFeature that = (ModificationFeature) element;
		boolean value = super.atEquivalentLocation(that);
		if (value)
		{
			SequenceModificationVocabulary yourType = that.getModificationType();
			SequenceModificationVocabulary myType = getModificationType();
			value = (yourType == null) ?
			        myType == null :
			        myType.isEquivalent(yourType);
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
