package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.SequenceEntityReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Analyze;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
@Proxy(proxyClass=SequenceEntityReference.class)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class SequenceEntityReferenceImpl extends EntityReferenceImpl
        implements SequenceEntityReference
{
    private BioSource organism;
    private String sequence;

    public SequenceEntityReferenceImpl() {
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
	@Field(name=FIELD_SEQUENCE, analyze=Analyze.YES)
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
    	
    	return  getOrganism() != null
                && getOrganism().isEquivalent(that.getOrganism())
                && getSequence() != null
                && getSequence().equalsIgnoreCase(that.getSequence());

    }

    @Override
    public int equivalenceCode()
    {
        return this.organism==null || this.sequence==null? hashCode():
                this.organism.equivalenceCode()+17*this.sequence.hashCode();
    }
}
