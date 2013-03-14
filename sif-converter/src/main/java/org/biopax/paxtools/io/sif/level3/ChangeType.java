package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emek Demir // todo annotate
 */
public enum ChangeType {
    EXIST_TO_NOT_EXIST,
    EXIST_TO_UNKNOWN,
    UNKNOWN_TO_NOT_EXIST,
    UNCHANGED,
    NOT_EXIST_TO_UNKNOWN,
    UNKNOWN_TO_EXIST,
    NOT_EXIST_TO_EXIST;

    public static final EntityFeature EXISTENCE = BioPAXLevel.L3.getDefaultFactory().create(
            EntityFeature.class, "urn:org.biopax.paxtools.static/EXISTENCE");
    public static final BindingFeature BINDING = BioPAXLevel.L3.getDefaultFactory().create(
            BindingFeature.class, "urn:org.biopax.paxtools.static/BINDING");

    public static Map<EntityFeature, ChangeType> getDeltaFeatures(PhysicalEntity left, PhysicalEntity right,
                                                                  PhysicalEntity leftRoot, PhysicalEntity rightRoot) {
        Map<EntityFeature, ChangeType> result = new HashMap<EntityFeature, ChangeType>();
        if (left == null) {
            result.put(EXISTENCE, NOT_EXIST_TO_EXIST);

            for (EntityFeature entityFeature : right.getFeature()) {
                result.put(entityFeature, NOT_EXIST_TO_EXIST);
            }
        }
        else if (right == null)
        {
            result.put(EXISTENCE, EXIST_TO_NOT_EXIST);
            for (EntityFeature entityFeature : left.getFeature()) {
                result.put(entityFeature, EXIST_TO_NOT_EXIST);
            }
        }
        else
        {
            for (EntityFeature lFeat : left.getFeature()) {
                if (right.getFeature().contains(lFeat))
                {
                    result.put(lFeat, UNCHANGED);
                }
                else if (right.getNotFeature().contains(lFeat)) {
                    result.put(lFeat, EXIST_TO_NOT_EXIST);
                } else {
                    result.put(lFeat, EXIST_TO_UNKNOWN);
                }
            }
            for (EntityFeature lFeat : left.getNotFeature()) {
                if (right.getFeature().contains(lFeat)) {
                    result.put(lFeat, NOT_EXIST_TO_EXIST);
                }
                else if
                        (right.getNotFeature().contains(lFeat)) {
                    result.put(lFeat, UNCHANGED);
                } else {
                    result.put(lFeat, NOT_EXIST_TO_UNKNOWN);
                }
            }
            for (EntityFeature rFeat : right.getFeature()) {
                if (!result.containsKey(rFeat)) {
                    result.put(rFeat, UNKNOWN_TO_EXIST);
                }
            }
            for (EntityFeature rFeat : right.getNotFeature()) {
                if (!result.containsKey(rFeat)) {
                    result.put(rFeat, UNKNOWN_TO_NOT_EXIST);
                }
            }
            boolean leftInComplex = leftRoot instanceof Complex;
            boolean rightInComplex = rightRoot instanceof Complex;

            ChangeType bindingDirection = null;
            if (leftInComplex) {
                if (!rightInComplex || ((Complex) leftRoot).getComponent().contains(rightRoot)) {
                    bindingDirection = EXIST_TO_NOT_EXIST;
                } else if (((Complex) rightRoot).getComponent().contains(leftRoot)) {
                    bindingDirection = NOT_EXIST_TO_EXIST;
                }
            }
            else if (rightInComplex) {
                bindingDirection = NOT_EXIST_TO_EXIST;
            }
            if(bindingDirection!=null)
            {
                result.put(BINDING,bindingDirection);
            }
        }

        return result;

    }
}
