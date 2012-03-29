package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.Set;

/**
 */
public class Simplify {
    private static Log log = LogFactory.getLog(Simplify.class);
    private static PathAccessor complexPath = new PathAccessor("Complex/component*");

    public static boolean entityHasAChange(
            BioPAXElement element,
            Conversion conv,
            GroupMap map,
            Set<PEStateChange> changeSet) {
        SimplePhysicalEntity left = null;
        SimplePhysicalEntity right = null;
        PhysicalEntity leftRoot = null;
        PhysicalEntity rightRoot = null;

        if (element == null) {
            if (log.isWarnEnabled()) log.warn("Skipping ");
            return false;
        }

        for (PhysicalEntity pe : conv.getLeft()) {
            left = getAssociatedState(element, pe, map);
            if (left != null) {
                leftRoot = pe;
                break;
            }
        }
        for (PhysicalEntity pe : conv.getRight()) {
            right = getAssociatedState(element, pe, map);
            if (right != null) {
                rightRoot = pe;
                break;
            }
        }

        if (left == null || right == null || !leftRoot.equals(rightRoot)) {
            if (changeSet != null) {
                changeSet.add(new PEStateChange(left, right, leftRoot, rightRoot, element, conv));
                //TODO three things
                //If in complex - match complex.memberPE
                //If not in complex- match pe.memberPE
                //Match ER-level generics. - do not exist in Reactome -so TODO for now.


            }

            return true;
        }
        return false;
    }


    private static SimplePhysicalEntity getAssociatedState(BioPAXElement element, PhysicalEntity pe, GroupMap map) {
        if (pe instanceof Complex) {
            for (PhysicalEntity component : ((Complex) pe).getComponent()) {
                SimplePhysicalEntity viaComplex = getAssociatedState(element, component, map);
                if (viaComplex != null) {
                    return viaComplex;
                }
            }
        }
        if (checkEntity(map, pe, element)) return (SimplePhysicalEntity) pe;
        else {
            for (PhysicalEntity member : pe.getMemberPhysicalEntity()) {
                SimplePhysicalEntity viaGeneric = getAssociatedState(element, member, map);
                if (viaGeneric != null) return viaGeneric;
            }
        }
        return null;
    }

    private static boolean checkEntity(GroupMap map, PhysicalEntity pe, BioPAXElement element) {
        return pe instanceof SimplePhysicalEntity && (element.equals(((SimplePhysicalEntity) pe).getEntityReference()))
                || element.equals(map.getEntityReferenceOrGroup(pe));
    }
}
