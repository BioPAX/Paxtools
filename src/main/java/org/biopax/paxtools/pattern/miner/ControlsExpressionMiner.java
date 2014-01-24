package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Pattern;
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
//		Pattern p = PatternBox.transcriptionWithTemplateReac();
//		p.add(new Type(ProteinReference.class), "product ER");

		Pattern p = new Pattern(ProteinReference.class, "TF PR");
		p.add(erToPE(), "TF PR", "TF simple PE");
		p.add(linkToComplex(), "TF simple PE", "TF PE");
		p.add(peToControl(), "TF PE", "Control");
		p.add(controlToTempReac(), "Control", "TempReac");
		p.add(new NOT(participantER()), "TempReac", "TF PR");
		p.add(product(), "TempReac", "product PE");
		p.add(linkToSimple(), "product PE", "product simple PE");
		p.add(new Type(Protein.class), "product simple PE");
		p.add(peToER(), "product simple PE", "product PR");

		return p;
	}

	@Override
	public String getSourceLabel()
	{
		return "TF PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "product PR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "TempReac"};
	}
}
