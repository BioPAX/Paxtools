package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * Created by IntelliJ IDEA. User: Emek Date: Feb 13, 2008 Time: 4:40:42 AM
 */
class ModificationFeatureImpl extends EntityFeatureImpl
		implements ModificationFeature
{
	public Class<? extends ModificationFeature> getModelInterface()
	{
		return ModificationFeature.class;
	}

	private SequenceModificationVocabulary modificationType;

	// Property FEATURE-TYPE

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
