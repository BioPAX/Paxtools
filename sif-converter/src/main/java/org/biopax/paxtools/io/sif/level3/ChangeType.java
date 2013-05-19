package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum handles types of modifications and contains utility methods for extracting them.
 */
public enum ChangeType {
	/**
	 * A modification type was explicitly removed. ( Very RARE since NOT-FEATURE is currently not adopted by most
	 * pathway databases.)
	 */
	EXIST_TO_NOT_EXIST,
	/**
	 * A modification type "disappeared. (Common removal type)
	 */
    EXIST_TO_UNKNOWN,
	/**
	 * A modification type "appeared" (Common addition type)
	 */
    UNKNOWN_TO_NOT_EXIST,
	/**
	 * No addition or removal.
	 */
    UNCHANGED,
	/**
	 *  A modification type that was known to be non-existing simply become unknown. No know instances, this is here
	 *  for completeness
	 */
	NOT_EXIST_TO_UNKNOWN,

	/**
	 *  The most common addition form - a modification type "appeared".
	 */
	UNKNOWN_TO_EXIST,

	/**
	 * A modification type was explicitly added.  ( Very RARE since NOT-FEATURE is currently not adopted by most
	 * pathway databases.)
	 */
	NOT_EXIST_TO_EXIST;

	/**
	 * Special static entity feature used for representing changes that effect existence of protein -i.e. translation
	 * or degradation.
	 */
    public static final EntityFeature EXISTENCE = BioPAXLevel.L3.getDefaultFactory().create(
            EntityFeature.class, "urn:org.biopax.paxtools.static/EXISTENCE");

	/**
	 * Special static entity feature used as a 'hack' to capture complex membership.
	 */
	public static final BindingFeature BINDING = BioPAXLevel.L3.getDefaultFactory().create(
            BindingFeature.class, "urn:org.biopax.paxtools.static/BINDING");

	/**
	 * This method returns the features that are "changed" between the left and right physical entities. IF these are
	 * "contained" in another PE such as a complex or generic it also considers changes to those containers .
	 * @param left a simple physical entity to be compared.
	 * @param right a simple physical entity to be compared
	 * @param leftRoot if left is in a complex or represented as a generic actual participating entity that contains
	 * or subsumes left.
	 * @param rightRoot if right is in a complex or represented as a generic actual participating entity that contains
	 * or subsumes right.
	 * @return A Map of features of spe, annotated with a change type, direction is from-left-to-right.
	 */
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
