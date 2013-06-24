package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.ConBox;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the transcriptional regulation pattern.
 * @author Ozgun Babur
 */
public class ControlsExpressionChangeMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsExpressionChangeMiner()
	{
		super("Controls-expression-change", "This pattern finds relations where first protein " +
			"is controlling expression of the second protein. The output is like " +
			"\"A controls-expression-change B\". This pattern requires that " +
			"expression to be modeled with a TemplateReaction.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
//		Pattern p = PatternBox.transcriptionWithTemplateReac();
//		p.addConstraint(new Type(ProteinReference.class), "product ER");

		Pattern p = new Pattern(ProteinReference.class, "TF PR");
		p.addConstraint(ConBox.isHuman(), "TF PR");
		p.addConstraint(ConBox.erToPE(), "TF PR", "TF simple PE");
		p.addConstraint(ConBox.linkToComplex(), "TF simple PE", "TF PE");
		p.addConstraint(ConBox.peToControl(), "TF PE", "Control");
		p.addConstraint(ConBox.controlToTempReac(), "Control", "TempReac");
		p.addConstraint(ConBox.product(), "TempReac", "product PE");
		p.addConstraint(ConBox.linkToSimple(), "product PE", "product simple PE");
		p.addConstraint(new Type(Protein.class), "product simple PE");
		p.addConstraint(ConBox.peToER(), "product simple PE", "product PR");
		p.addConstraint(ConBox.equal(false), "TF PR", "product PR");

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

	/**
	 * The relation can be either transactivate for transactivation, or transinhibit for
	 * transinhibition.
	 * @param m the match
	 * @return type
	 */
	@Override
	public String getRelationType(Match m)
	{
		return "controls-expression-change";
	}

	@Override
	public boolean isDirected()
	{
		return true;
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
	public String[] getPubmedHarvestableLabels()
	{
		return new String[]{"Control", "TempReac"};
	}
}
