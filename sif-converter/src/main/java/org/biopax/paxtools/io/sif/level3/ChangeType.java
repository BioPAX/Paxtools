package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.util.HashMap;
import java.util.Map;

/**
 */
public enum ChangeType {
    EXIST_TO_NOT_EXIST,
    EXIST_TO_UNKNOWN,
    UNKNOWN_TO_NOT_EXIST,
    UNCHANGED,
    NOT_EXIST_TO_UNKNOWN,
    UNKNOWN_TO_EXIST,
    NOT_EXIST_TO_EXIST;

    public static Map<EntityFeature, ChangeType> getDeltaFeatures(PhysicalEntity left, PhysicalEntity right)
    {
        Map<EntityFeature, ChangeType> result = new HashMap<EntityFeature, ChangeType>();
        for (EntityFeature lFeat : left.getFeature()) {
            if (right.getFeature().contains(lFeat)) {
                result.put(lFeat, UNCHANGED);
            }
            if (right.getNotFeature().contains(lFeat)) {
                result.put(lFeat, EXIST_TO_NOT_EXIST);
            } else {
                result.put(lFeat, EXIST_TO_UNKNOWN);
            }
        }
        for (EntityFeature lFeat : left.getNotFeature()) {
            if (right.getFeature().contains(lFeat)) {
                result.put(lFeat, NOT_EXIST_TO_EXIST);
            }
            if (right.getNotFeature().contains(lFeat)) {
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
        return result;
    }

}
