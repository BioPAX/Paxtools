package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.SequenceEntityReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Target;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
@Proxy(proxyClass=SequenceEntityReference.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
	@IndexedEmbedded(targetElement=BioSourceImpl.class)
	@Target(BioSourceImpl.class)
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
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_SEQUENCE, index=Index.TOKENIZED)
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
