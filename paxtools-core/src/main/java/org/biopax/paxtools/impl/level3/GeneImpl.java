package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Gene;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed
class GeneImpl extends EntityImpl implements Gene
{
    private BioSource organism;

	@Transient
    public Class<? extends Gene> getModelInterface()
    {
        return Gene.class;
    }

	@ManyToOne(targetEntity = BioSourceImpl.class)
    public BioSource getOrganism()
    {
        return organism;
    }

    public void setOrganism(BioSource source)
    {
        this.organism = source;
    }
}
