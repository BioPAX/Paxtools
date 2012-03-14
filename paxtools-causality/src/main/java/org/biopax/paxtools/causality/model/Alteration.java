package org.biopax.paxtools.causality.model;

/**
 * @author Ozgun Babur
 */
public enum Alteration
{
	COPY_NUMBER_INCREASE(1, true),
	COPY_NUMBER_DECREASE(-1, true),
	MUTATION_LOSS_OF_FUNCTION(-1, true),
	MUTATION_GAIN_OF_FUNCTION(1, true),
	METHYLATION_INCREASE(-1, true),
	METHYLATION_DECREASE(1, true),
	EXPRESSION_INCREASE(1, false),
	EXPRESSION_DECREASE(-1, false),
	PROTEIN_LEVEL_INCREASE(1, false),
	PROTEIN_LEVEL_DECREASE(-1, false);

	int sign;
	boolean genomic;

	private Alteration(int sign, boolean genomic)
	{
		this.sign = sign;
		this.genomic = genomic;
	}

	public int getSign()
	{
		return sign;
	}

	public boolean isGenomic()
	{
		return genomic;
	}
}
