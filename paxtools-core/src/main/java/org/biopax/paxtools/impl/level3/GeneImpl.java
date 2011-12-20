package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Gene;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
 @Proxy(proxyClass= Gene.class)
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GeneImpl extends EntityImpl implements Gene
{
    private BioSource organism;

    public GeneImpl() {
	}
    
	@Transient
    public Class<? extends Gene> getModelInterface()
    {
        return Gene.class;
    }

	@ManyToOne(targetEntity = BioSourceImpl.class)//, cascade = {CascadeType.ALL})
    public BioSource getOrganism()
    {
        return organism;
    }

    public void setOrganism(BioSource source)
    {
        this.organism = source;
    }
}
