package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.SequenceEntityReference;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
abstract class SequenceEntityReferenceImpl
        extends EntityReferenceImpl
        implements SequenceEntityReference
{
    private BioSource organism;
    private String sequence;

    /**
     * Constructor.
     */
    SequenceEntityReferenceImpl()
    {
    }


    //
    // referenceSequenceEntity interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////

    // Property organism
	@ManyToOne(targetEntity = BioSourceImpl.class)
    public BioSource getOrganism()
    {
        return organism;
    }

    public void setOrganism(BioSource organism)
    {
        this.organism = organism;
    }

    // Property sequence

	@Lob
	public String getSequence()
    {
        return sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }
    
    @Override
    protected boolean semanticallyEquivalent(BioPAXElement element) {
    	if(!(element instanceof SequenceEntityReference)) return false;
    	SequenceEntityReference that = (SequenceEntityReference) element;
    	
    	return ((getOrganism() != null) ? getOrganism().isEquivalent(that.getOrganism()) : that.getOrganism()==null)
    		&& ((getSequence() != null) ? getSequence().equalsIgnoreCase(that.getSequence()) : that.getSequence()==null)
    		&& super.semanticallyEquivalent(element);
    }
}
