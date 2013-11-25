package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.constraint.NOT;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.biopax.paxtools.pattern.constraint.ConBox.*;

/**
 * Miner for the transcriptional regulation pattern.
 * @author Ozgun Babur
 */
public class ControlsExpressionMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsExpressionMiner()
	{
		super(SIFType.CONTROLS_EXPRESSION_OF.getTag(), "This pattern finds relations where first " +
			"protein is controlling expression of the second protein. The output is like " +
			"\"A controls-expression B\". This pattern requires that expression to be modeled " +
			"with a TemplateReaction.", null);
	}

	/**
	 * Constructor for extending this class.
	 * @param name name
	 * @param description description
	 */
	public ControlsExpressionMiner(String name, String description)
	{
		super(name, description, null);
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
		p.add(isHuman(), "TF PR");
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

	/**
	 * Writes the result as "A controls-expression-change B", where A and B are gene
	 * symbols, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, true, getSourceLabel(), getTargetLabel());
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
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_EXPRESSION_OF;
	}

	/**
	 * Sets header of the output.
	 * @return header
	 */
	@Override
	public String getHeader()
	{
		return "Upstream\tRelation\tDownstream";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "TempReac"};
	}
}
