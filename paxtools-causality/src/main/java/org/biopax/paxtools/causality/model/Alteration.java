package org.biopax.paxtools.causality.model;

/**
 * @author Ozgun Babur
 */
public enum Alteration
{
	COPY_NUMBER(true),
	MUTATION(true),
	METHYLATION(true),
	EXPRESSION(false),
	PROTEIN_LEVEL(false),
	NON_GENOMIC(false),
	ANY(false);

	boolean genomic;

	private Alteration(boolean genomic)
	{
		this.genomic = genomic;
	}

	public boolean isGenomic()
	{
		return genomic;
	}
}
