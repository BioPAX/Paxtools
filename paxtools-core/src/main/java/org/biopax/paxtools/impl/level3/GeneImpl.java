package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Gene;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 22, 2008
 * Time: 1:07:24 AM
 */
class GeneImpl extends EntityImpl implements Gene
{
    private BioSource organism;

    public Class<? extends Gene> getModelInterface()
    {
        return Gene.class;
    }
    public BioSource getOrganism()
    {
        return organism;
    }

    public void setOrganism(BioSource source)
    {
        this.organism = source;
    }
}
