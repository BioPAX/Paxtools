package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.Map;

/**

 */
public class GroupMap 
{
    private Log log= LogFactory.getLog(GroupMap.class);
    private Map<BioPAXElement, Group> map;

    public GroupMap(Map<BioPAXElement, Group> element2GroupMap) 
    {
        this.map = element2GroupMap;
    }

    protected BioPAXElement getEntityReferenceOrGroup(BioPAXElement bpe)
    {

        BioPAXElement entity = map.get(bpe);
        if (entity == null)
        {
            if (bpe instanceof EntityReference)
            {
                entity = bpe;
            } else if (bpe instanceof SimplePhysicalEntity)
            {
                EntityReference er = ((SimplePhysicalEntity) bpe).getEntityReference();
                if (er != null)
                {
                    entity = map.get(er);
                    if (entity == null) entity = er;
                } else
                {
                    if (log.isWarnEnabled())
                        log.warn("SimplePhysicalEntity with ID " + bpe.getRDFId() + " has NULL EntityReference");
                }
            }
        }
        return entity;
    }


    public Map<BioPAXElement, Group> getMap()
    {
        return map;
    }
}
