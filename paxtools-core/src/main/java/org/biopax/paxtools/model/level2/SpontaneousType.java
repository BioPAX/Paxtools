package org.biopax.paxtools.model.level2;

/**
 */
public enum SpontaneousType
{
	L_R,
	R_L,
	NOT_SPONTANEOUS;

//  To string is also used for exporting to BioPAX - breaks the writer
//  This is better handled in L3- sorry Dude..  
//	public String toString()
//	{
//		if (this == L_R) return "Left to Right";
//		else if (this == R_L) return "Right to Left";
//		else if (this == NOT_SPONTANEOUS) return "NOT_SPONTANEOUS";
//		else return null;
//	}
}
