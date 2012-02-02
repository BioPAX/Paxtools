package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class Simplify
{
    private static Log log= LogFactory.getLog(Simplify.class);

    public static boolean entityHasAChange(
        BioPAXElement element,
        Conversion conv,
        GroupMap map,
        Map<BioPAXElement, Set<PEStateChange>> stateChanges)
    {
        Set<SimplePhysicalEntity> left = getAssociatedStates(element, conv.getLeft(), map);
        Set<SimplePhysicalEntity> right = getAssociatedStates(element, conv.getRight(), map);

        if (left.isEmpty() || right.isEmpty()) return true;

        for (SimplePhysicalEntity lpe : left) {
            for (SimplePhysicalEntity rpe : right) {
                if (!lpe.equals(rpe)) {
                    if (stateChanges!=null)
                    {
                        Set<PEStateChange> changeSet = stateChanges.get(element);
                        if (changeSet == null)
                        {
                            changeSet = new HashSet<PEStateChange>();
                            stateChanges.put(element, changeSet);
                        }
                        changeSet.add(new PEStateChange(lpe, rpe, element, conv));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static Set<SimplePhysicalEntity> getAssociatedStates(BioPAXElement element, Set<PhysicalEntity> pes,
                                                           GroupMap map) {
        Set<SimplePhysicalEntity> set = new HashSet<SimplePhysicalEntity>();

        if (element == null) {
            if (log.isWarnEnabled()) log.warn("Skipping ");
            return set; // empty
        }

        for (PhysicalEntity pe : pes)
        {
            addMappedElement(element, map, set, pe);
            if (pe instanceof Complex) {
                for (PhysicalEntity member : ((Complex) pe).getComponent()) {
                    addMappedElement(element, map, set, member);
                }
            }
        }
        return set;
    }

    private static void addMappedElement(BioPAXElement element, GroupMap map, Set<SimplePhysicalEntity> set,
                                         PhysicalEntity pe)
    {
        if ((element.equals(map.getEntityReferenceOrGroup(pe))) && pe instanceof SimplePhysicalEntity) 
        {
            set.add((SimplePhysicalEntity) pe);
        }
    }

}
