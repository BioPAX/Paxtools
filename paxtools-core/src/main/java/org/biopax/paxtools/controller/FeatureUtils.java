package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * This class provides operations for comparing features of physical entities.
 */
public class FeatureUtils {

    public static final int FEATURE = 0;
    public static final int NOT_FEATURE = 1;
    public static final int UNKNOWN_FEATURE = 2;

    public static Log log = LogFactory.getLog(FeatureUtils.class);


    public static Set<EntityFeature> getFeatureIntersection(
            PhysicalEntity first,
            int firstClass,
            PhysicalEntity second,
            int secondClass)
    {
        Set<EntityFeature> intersection = getFeatureSetByType(first, firstClass);
        intersection.removeAll(getFeatureSetByType(second, secondClass));
        return intersection;
    }


    public static Set<EntityFeature> getFeatureSetByType(PhysicalEntity pe, int type)
    {

        Set<EntityFeature> modifiableSet =new HashSet<EntityFeature>();

        switch (type)
        {
            case FEATURE: modifiableSet.addAll(pe.getFeature());break;
            case NOT_FEATURE: modifiableSet.addAll(pe.getNotFeature());break;
            case UNKNOWN_FEATURE:
            {
                if(pe instanceof SimplePhysicalEntity)
                {
                    modifiableSet.addAll(((SimplePhysicalEntity) pe).getEntityReference().getEntityFeature());
                    modifiableSet.removeAll(pe.getFeature());
                    modifiableSet.removeAll(pe.getNotFeature());
                }
            }
        }
        return modifiableSet;
    }

    public static boolean checkERFeatureSet(EntityReference er, boolean fix)
    {
        boolean check = true;
        for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
        {
            for (EntityFeature ef : spe.getFeature())
            {
                check = scanAndAddToFeatureSet(er, fix, check, ef);
                //if not fixing return at first fail, otherwise go on;
                if(!fix && !check) return check;
            }
            for (EntityFeature ef : spe.getNotFeature())
            {
                check = scanAndAddToFeatureSet(er, fix, check, ef);
                //if not fixing return at first fail, otherwise go on;
                if(!fix && !check) return check;
            }
        }
        return check;
    }

    public static boolean checkMutuallyExclusiveSets(PhysicalEntity pe)
    {
        if(pe.getFeature().isEmpty() || pe.getNotFeature().isEmpty())
            return true;
        else
        {
            Set<EntityFeature> test = new HashSet<EntityFeature>(pe.getFeature());
            test.retainAll(pe.getNotFeature());
            return test.size() == 0;
        }
    }


    private static boolean scanAndAddToFeatureSet(EntityReference er, boolean fix, boolean check, EntityFeature ef) {
        if(!er.getEntityFeature().contains(ef))
        {
          check = false;
          if(fix)
          {
              er.addEntityFeature(ef);
          }
        }
        return check;
    }

    public static Set<EntityFeature> findFeaturesAddedToSecond(PhysicalEntity first, PhysicalEntity second, boolean fix)
    {

        if (checkCommonEntityReferenceForTwoPEs(first, second, fix)) return null;
        Set<EntityFeature> explicit = getFeatureIntersection(first, NOT_FEATURE, second, FEATURE);
        Set<EntityFeature> implicit = getFeatureIntersection(first, UNKNOWN_FEATURE, second, FEATURE);
        Set<EntityFeature> negativeImplicit = getFeatureIntersection(first, NOT_FEATURE, second, UNKNOWN_FEATURE);

        if(fix)
        {
            for (EntityFeature implied : implicit)
            {
                log.info("The feature "+ implied + "implied as a not-feature of " + first+". " +
                        "Adding it to the not-feature list");
                first.addNotFeature(implied);
            }

            for (EntityFeature implied : negativeImplicit)
            {
                log.info("The feature "+ implied + "implied as a feature of " + second+". " +
                        "Adding it to the feature list");
                second.addFeature(implied);
            }

        }
        explicit.retainAll(implicit);
        explicit.retainAll(negativeImplicit);
        return explicit;
    }

    private static boolean checkCommonEntityReferenceForTwoPEs(PhysicalEntity first, PhysicalEntity second, boolean fix)
    {
        if(first instanceof SimplePhysicalEntity)
        {
            EntityReference er = ((SimplePhysicalEntity) first).getEntityReference();
            if(!er.getEntityReferenceOf().contains(second))
            {
                log.warn("These two physicalEntities do not share an EntityReference. They can not be compared! Skipping");
                return false;
            }
            else if (!checkERFeatureSet(er,fix))
            {
                log.warn("ER feature set is incomplete!");
                if(!fix)
                {
                    log.warn("fixing...");
                }
                else
                {
                    log.warn("skipping");
                    return false;
                }
            }
            return true;
        }
        else
        {
                log.warn("These two physicalEntities do not share an EntityReference. They can not be compared! Skipping");
                return false;
          }

    }





}
