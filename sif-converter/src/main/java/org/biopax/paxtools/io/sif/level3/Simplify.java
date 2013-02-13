package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.EquivalenceSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class Simplify
{
	private static Log log = LogFactory.getLog(Simplify.class);

	private static PathAccessor complexPath = new PathAccessor("Complex/component*");

	private static PathAccessor memberPath = new PathAccessor("PhysicalEntity/memberPhysicalEntity*");

	public static boolean entityHasAChange(BioPAXElement element, Conversion conv, GroupMap map,
			Set<PEStateChange> changeSet, Map<Conversion, Set<EntityReference>> extendedControls)
	{
		SimplePhysicalEntity left = null;
		SimplePhysicalEntity right = null;
		PhysicalEntity leftRoot = null;
		PhysicalEntity rightRoot = null;

		if (element == null)
		{
			if (log.isWarnEnabled()) log.warn("Skipping ");
			return false;
		}

		for (PhysicalEntity pe : conv.getLeft())
		{
			left = getAssociatedState(element, pe, map);
			if (left != null)
			{
				leftRoot = pe;
				break;
			}
		}
		for (PhysicalEntity pe : conv.getRight())
		{
			right = getAssociatedState(element, pe, map);
			if (right != null)
			{
				rightRoot = pe;
				break;
			}
		}

		if (left == null || right == null || !leftRoot.equals(rightRoot))
		{
			if (changeSet != null)
			{
				changeSet.add(new PEStateChange(left, right, leftRoot, rightRoot, element, conv));
				//Match ER-level generics. - do not exist in Reactome -so TODO for now.
				if (extendedControls != null) captureExtendedControls(conv, extendedControls, leftRoot, rightRoot);
			}

			return true;
		}
		return false;
	}


	private static void captureExtendedControls(Conversion conv, Map<Conversion,
			Set<EntityReference>> extendedControls,
			PhysicalEntity leftRoot, PhysicalEntity rightRoot)
	{
		if (leftRoot instanceof Complex || rightRoot instanceof Complex)
		{
			if (conv.getControlledOf().isEmpty()) //?? Todo check this
			{
				Set<SimplePhysicalEntity> leftSpe = new HashSet<SimplePhysicalEntity>();
				getSimpleMembers(leftRoot, leftSpe);
				EquivalenceSet leftComps = new EquivalenceSet(leftSpe);

				Set<SimplePhysicalEntity> rightSpe = new HashSet<SimplePhysicalEntity>();
				getSimpleMembers(rightRoot, rightSpe);
				EquivalenceSet rightComps = new EquivalenceSet(rightSpe);

				leftComps.retainAll(rightComps);
				for (BioPAXElement bpe:leftComps)
				{
					SimplePhysicalEntity pe = (SimplePhysicalEntity) bpe;
					Set<EntityReference> erSet = extendedControls.get(conv);
					if (erSet == null)
					{
						erSet = new HashSet<EntityReference>();
						extendedControls.put(conv, erSet);
					}
					erSet.add(pe.getEntityReference());
				}
			}
		}

	}


	public static void getSimpleMembers(PhysicalEntity root, Set<SimplePhysicalEntity> value)
	{
		if (root != null)
		{
			if (root instanceof Complex)
			{
				Complex complex = (Complex) root;
				for (PhysicalEntity component : complex.getComponent())
				{
					getSimpleMembers(component, value);
				}
			}

			if (root.getMemberPhysicalEntity().isEmpty())
			{
				if(root instanceof SimplePhysicalEntity)
					value.add((SimplePhysicalEntity) root);
			}
			else
			{
				for (PhysicalEntity generic : root.getMemberPhysicalEntity())
				{
					getSimpleMembers(generic, value);
				}
			}

		}
	}


	private static SimplePhysicalEntity getAssociatedState(BioPAXElement element, PhysicalEntity pe, GroupMap map)
	{
		if (pe instanceof Complex)
		{
			for (PhysicalEntity component : ((Complex) pe).getComponent())
			{
				SimplePhysicalEntity viaComplex = getAssociatedState(element, component, map);
				if (viaComplex != null)
				{
					return viaComplex;
				}
			}
		} else if (checkEntity(map, pe, element)) return (SimplePhysicalEntity) pe;
		for (PhysicalEntity member : pe.getMemberPhysicalEntity())
		{
			SimplePhysicalEntity viaGeneric = getAssociatedState(element, member, map);
			if (viaGeneric != null) return viaGeneric;
		}
		return null;
	}

	private static boolean checkEntity(GroupMap map, PhysicalEntity pe, BioPAXElement element)
	{
		return pe instanceof SimplePhysicalEntity &&
		       (element.equals(((SimplePhysicalEntity) pe).getEntityReference())) ||
		       element.equals(map.getEntityReferenceOrGroup(pe));
	}

	public static boolean entityHasAChange(BioPAXElement target, Conversion conv, GroupMap groupMap,
			Set<PEStateChange> peStateChanges)
	{
		return entityHasAChange(target, conv, groupMap, peStateChanges, null);
	}
}
