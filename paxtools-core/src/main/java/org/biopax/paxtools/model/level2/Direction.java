package org.biopax.paxtools.model.level2;

/**
 * Catalysis direction controlled vocabulary
 */
public enum Direction
{
	REVERSIBLE,
	PHYSIOL_LEFT_TO_RIGHT,
	PHYSIOL_RIGHT_TO_LEFT,
	IRREVERSIBLE_LEFT_TO_RIGHT,
	IRREVERSIBLE_RIGHT_TO_LEFT;

	public String toString()
	{
		if (this == REVERSIBLE) return "REVERSIBLE";
		else if (this == PHYSIOL_LEFT_TO_RIGHT) return "PHYSIOL_LEFT_TO_RIGHT";
		else if (this == PHYSIOL_RIGHT_TO_LEFT) return "PHYSIOL_RIGHT_TO_LEFT";
		else if (this == IRREVERSIBLE_LEFT_TO_RIGHT) return "IRREVERSIBLE_LEFT_TO_RIGHT";
		else if (this == IRREVERSIBLE_RIGHT_TO_LEFT) return "IRREVERSIBLE_RIGHT_TO_LEFT";
		else return null;
	}
}

