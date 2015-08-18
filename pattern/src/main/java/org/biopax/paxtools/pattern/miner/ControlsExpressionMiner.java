package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.NOT;
import org.biopax.paxtools.pattern.constraint.Type;

import static org.biopax.paxtools.pattern.constraint.ConBox.*;

/**
 * Miner for the transcriptional regulation pattern.
 * @author Ozgun Babur
 */
public class ControlsExpressionMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsExpressionMiner()
	{
		super(SIFEnum.CONTROLS_EXPRESSION_OF);
	}

	/**
	 * Constructor for extending this class.
	 * @param nameSuppl name
	 * @param descriptionSuppl description
	 */
	public ControlsExpressionMiner(String nameSuppl, String descriptionSuppl)
	{
		super(SIFEnum.CONTROLS_EXPRESSION_OF, nameSuppl, descriptionSuppl);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.controlsExpressionWithTemplateReac();

		return p;
	}

	@Override
	public String getSourceLabel()
	{
		return "TF ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "product ER";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "TempReac"};
	}

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"TF SPE", "TF PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"product PE", "product SPE"};
	}
}
