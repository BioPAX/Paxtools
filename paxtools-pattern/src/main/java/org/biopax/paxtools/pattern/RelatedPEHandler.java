package org.biopax.paxtools.pattern;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.level3.*;

/**
 * This class takes two equivalent PhysicalEntity, and prepares an array of PEs that link those.
 * 
 * @author Ozgun Babur
 */
public class RelatedPEHandler
{
	public PhysicalEntity[] pes;

	protected static final PathAccessor modifAcc = 
		new PathAccessor("PhysicalEntity/feature:ModificationFeature/modificationType/term");

	public RelatedPEHandler(PhysicalEntity small, PhysicalEntity big)
	{
		pes = fillArray(big, small, 1, 0);
		
		if (pes == null)
		{
			throw new IllegalArgumentException("No link found between small PE = " +
				small.getRDFId() + " and big PE = " + big.getRDFId());
		}

//		for (PhysicalEntity pe : pes)
//		{
//			if (pe == null)
//			{
//				System.out.println();
//
//				fillArray(big, small, 1, 0);
//			}
//		}
	}
	
	protected PhysicalEntity[] fillArray(PhysicalEntity parent, PhysicalEntity target, int depth, int dir)
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
	
	public String getCellularLocation()
	{
		for (PhysicalEntity pe : pes)
		{
			CellularLocationVocabulary voc = pe.getCellularLocation();
			if (voc != null)
			{
				for (String term : voc.getTerm())
				{
					if (term != null && term.length() > 0) return term;
				}
			}
		}
		return null;
	}

	public boolean intersects(RelatedPEHandler rpeh)
	{
		for (PhysicalEntity pe1 : pes)
		{
			for (PhysicalEntity pe2 : rpeh.pes)
			{
				if (pe1 == pe2) return true;
			}
		}
		return false;
	}
	
	public Activity checkActivityLabel()
	{
		boolean active = false;
		boolean inactive = false;

		for (PhysicalEntity pe : pes)
		{
			for (Object o : modifAcc.getValueFromBean(pe))
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
	
	enum Activity
	{
		ACTIVE,
		INACTIVE,
		BOTH,
		NONE
	}
}
