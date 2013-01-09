package org.biopax.paxtools.causality.model;

/**
 * @author Ozgun Babur
 */
public enum Alteration
{
	COPY_NUMBER(true, false),
	MUTATION(true, false),
	METHYLATION(true, false),
	EXPRESSION(false, false),
	PROTEIN_LEVEL(false, false),
	NON_GENOMIC(false, true),
	GENOMIC(true, true),
	ANY(false, true),
	ACTIVATING(false, true),
	INHIBITING(false, true);

	boolean genomic;
	boolean summary;

	private Alteration(boolean genomic, boolean summary)
	{
		this.genomic = genomic;
		this.summary = summary;
	}

	public boolean isGenomic()
	{
		return genomic;
	}

	public boolean isSummary()
	{
		return summary;
	}
}
