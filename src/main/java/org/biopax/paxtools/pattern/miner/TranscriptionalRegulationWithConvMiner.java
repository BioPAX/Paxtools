package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the transcriptional regulation pattern, modeled using Conversion.
 * @author Ozgun Babur
 */
public class TranscriptionalRegulationWithConvMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public TranscriptionalRegulationWithConvMiner()
	{
		super("Transcriptional-regulation-with-conversion", "This pattern finds relations where " +
			"first protein is controlling transcriptional activity of the second protein. " +
			"Proper way to model this relation is to use a TemplateReaction, however we see " +
			"that Conversion is also used in resources. This miner find patterns where a " +
			"Conversion with a single participant at right is used instead of a TemplateReaction." +
			" The output is either \"A transactivate B\" or \"A transinhibit B\".");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.transcriptionWithConversion();
		p.addConstraint(new Type(ProteinReference.class), "product ER");
		return p;
	}

	/**
	 * Writes the result as "A transactivate B" or "A transinhibit B", where A and B are gene
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
		return "product ER";
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
		Control con = (Control) m.get("Control", getPattern());
		return con.getControlType() != null && con.getControlType().toString().startsWith("I") ?
			"transinhibit" : "transactivate";
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
		return "Upstream\ttype\tDownstream";
	}

	@Override
	public String[] getPubmedHarvestableLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
