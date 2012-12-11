package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 * This class takes two equivalent PhysicalEntity, and prepares an array of PEs that link those.
 * 
 * @author Ozgun Babur
 */
public class RelatedPEHandler
{
	PhysicalEntity[] pes;

	public RelatedPEHandler(PhysicalEntity small, PhysicalEntity big)
	{
		pes = fillArray(big, small, 1);
		
		if (pes == null)
		{
			throw new IllegalArgumentException("No link found between small PE = " +
				small.getRDFId() + " and big PE = " + big.getRDFId());
		}
	}
	
	protected PhysicalEntity[] fillArray(PhysicalEntity parent, PhysicalEntity target, int depth)
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
				PhysicalEntity[] pes = fillArray(mem, target, depth + 1);
				if (pes != null)
				{
					pes[depth - 1] = parent;
					return pes;
				}
			}
		}

		for (PhysicalEntity mem : parent.getMemberPhysicalEntity())
		{
			PhysicalEntity[] pes = fillArray(mem, target, depth + 1);
			if (pes != null)
			{
				pes[depth - 1] = parent;
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


}
