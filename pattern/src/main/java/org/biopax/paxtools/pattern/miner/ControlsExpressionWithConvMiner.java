package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.Type;

/**
 * Miner for the transcriptional regulation pattern, modeled using Conversion.
 * @author Ozgun Babur
 */
public class ControlsExpressionWithConvMiner extends ControlsExpressionMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsExpressionWithConvMiner()
	{
		super("-with-conversion", "Proper way to model this relation is to use a TemplateReaction" +
			", however we see that Conversion is also used in resources. This miner find patterns" +
			" where a Conversion with a single participant at right is used instead of a " +
			"TemplateReaction.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.controlsExpressionWithConversion();
		return p;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"TF SPE", "TF PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"right PE", "right SPE"};
	}
}
