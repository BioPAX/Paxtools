package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 22, 2008
 * Time: 12:59:03 AM
 */
class DegradationImpl extends ConversionImpl implements Degradation
{
    public Class<? extends Degradation> getModelInterface()
    {
        return Degradation.class;
    }

}
