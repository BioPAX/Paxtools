package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;


public class ModificationFeatureImpl extends EntityFeatureImpl
		implements ModificationFeature
{
    private SequenceModificationVocabulary modificationType;

	public ModificationFeatureImpl() {
	}

    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	 	
        if(modificationType!=null)
        	sb.append("ModificationFeature: ").append(modificationType.getTerm());
        
        if(getFeatureLocation() != null) {
        	if(sb.length() == 0)
        		sb.append("ModificationFeature: ");
        	sb.append("@"+getFeatureLocation());
        }
        
        if(sb.length() == 0)
        	sb.append(super.toString());
        
        return sb.toString();
    }

	public Class<? extends ModificationFeature> getModelInterface()
	{
		return ModificationFeature.class;
	}

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
