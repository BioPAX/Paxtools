package org.biopax.paxtools.pattern.util;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.level3.*;

import java.util.HashSet;
import java.util.Set;

/**
 * This class takes two PhysicalEntity objects linked with generic or complex member relationships,
 * and prepares an array of PEs that link those.
 * 
 * @author Ozgun Babur
 */
public class PhysicalEntityChain
{
	/**
	 * Array that links two ends of the chain.
	 */
	public PhysicalEntity[] pes;

	/**
	 * Accessor to the modification term.
	 */
	protected static final PathAccessor PE2TERM =
		new PathAccessor("PhysicalEntity/feature:ModificationFeature/modificationType/term");

	/**
	 * Accessor to the modification term.
	 */
	protected static final PathAccessor PE2FEAT =
		new PathAccessor("PhysicalEntity/feature:ModificationFeature");

	/**
	 * Constructor with endpoints. The end points are differentiated with the terms small and big.
	 * This refers to a hierarchy in the relation. Small means you will get to this end while going
	 * towards MEMBER direction, and big means this end is at the COMPLEX direction. To understand
	 * this please see the directions in LinkedPE.
	 * @param small member end of the chain
	 * @param big complex end of the chain
	 * @see org.biopax.paxtools.pattern.constraint.LinkedPE
	 */
	public PhysicalEntityChain(PhysicalEntity small, PhysicalEntity big)
	{
		pes = fillArray(big, small, 1, 0);
		
		if (pes == null)
		{
			throw new IllegalArgumentException("No link found between small PE = " +
				small.getRDFId() + " and big PE = " + big.getRDFId());
		}
		assert !containsNull(pes);
	}

	/**
	 * Checks if any element in the chain is null.
	 * @param pes element array
	 * @return true if null found
	 */
	private boolean containsNull(PhysicalEntity[] pes)
	{
		for (PhysicalEntity pe : pes)
		{
			if (pe == null) return true;
		}
		return false;
	}

	/**
	 * Creates the chain that links the given endpoints.
	 * @param parent current element
	 * @param target target at the member end
	 * @param depth current depth
	 * @param dir current direction to traverse homologies
	 * @return array of entities
	 */
	protected PhysicalEntity[] fillArray(PhysicalEntity parent, PhysicalEntity target, int depth,
		int dir)
	{
		if (parent == target)
		{
			PhysicalEntity[] pes = new PhysicalEntity[depth];
			pes[0] = target;
			return pes;
		}
		
		if (parent instanceof Complex)
		{
			for (PhysicalEntity mem : ((Complex) parent).getComponent())
			{
				PhysicalEntity[] pes = fillArray(mem, target, depth + 1, 0);
				if (pes != null)
				{
					pes[pes.length - depth] = parent;
					return pes;
				}
			}
		}

		if (dir <= 0)
		for (PhysicalEntity mem : parent.getMemberPhysicalEntity())
		{
			PhysicalEntity[] pes = fillArray(mem, target, depth + 1, -1);
			if (pes != null)
			{
				pes[pes.length - depth] = parent;
				return pes;
			}
		}

		if (dir >= 0)
		for (PhysicalEntity grand : parent.getMemberPhysicalEntityOf())
		{
			PhysicalEntity[] pes = fillArray(grand, target, depth + 1, 1);
			if (pes != null)
			{
				pes[pes.length - depth] = parent;
				return pes;
			}
		}

		return null;
	}

	/**
	 * Retrieves the cellular location of the PhysicalEntity.
	 * @return cellular location of the PhysicalEntity
	 */
	public Set<String> getCellularLocations()
	{
		Set<String> locs = new HashSet<String>();
		for (PhysicalEntity pe : pes)
		{
			CellularLocationVocabulary voc = pe.getCellularLocation();
			if (voc != null)
			{
				for (String term : voc.getTerm())
				{
					if (term != null && term.length() > 0) locs.add(term);
				}
			}
		}
		return locs;
	}

	/**
	 * Checks if two chains intersect without ignoring endpoint intersection.
	 * @param rpeh second chain
	 * @return true if they intersect
	 */
	public boolean intersects(PhysicalEntityChain rpeh)
	{
		return intersects(rpeh, false);
	}

	/**
	 * Checks if two chains intersect.
	 * @param rpeh second chain
	 * @param ignoreEndPoints flag to ignore intersections at the endpoints of the chains
	 * @return true if they intersect
	 */
	public boolean intersects(PhysicalEntityChain rpeh, boolean ignoreEndPoints)
	{
		for (PhysicalEntity pe1 : pes)
		{
			for (PhysicalEntity pe2 : rpeh.pes)
			{
				if (pe1 == pe2)
				{
					if (ignoreEndPoints)
					{
						if ((pes[0] == pe1 || pes[pes.length-1] == pe1) &&
							(rpeh.pes[0] == pe2 || rpeh.pes[rpeh.pes.length-1] == pe2))
						{
							continue;
						}
					}

					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the chain has a member with an activity label.
	 * @return the activity status found
	 */
	public Activity checkActivityLabel()
	{
		boolean active = false;
		boolean inactive = false;

		for (PhysicalEntity pe : pes)
		{
			for (Object o : PE2TERM.getValueFromBean(pe))
			{
				String s = (String) o;
				if (s.contains("inactiv")) inactive = true;
				else if (s.contains("activ")) active = true;
			}

			for (String s : pe.getName())
			{
				if (s.contains("inactiv")) inactive = true;
				else if (s.contains("activ")) active = true;
			}
		}

		if (active) if (inactive) return Activity.BOTH; else return Activity.ACTIVE;
		else if (inactive) return Activity.INACTIVE; else return Activity.NONE;
	}

	/**
	 * Values for activity.
	 */
	public enum Activity
	{
		ACTIVE,
		INACTIVE,
		BOTH,
		NONE
	}

	/**
	 * Collects modifications from the elements of the chain.
	 * @return modifications
	 */
	public Set<ModificationFeature> getModifications()
	{
		Set<ModificationFeature> set = new HashSet<ModificationFeature>();

		for (PhysicalEntity pe : pes)
		{
			set.addAll(PE2FEAT.getValueFromBean(pe));
		}
		return set;
	}
}
