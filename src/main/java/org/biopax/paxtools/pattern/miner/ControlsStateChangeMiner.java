package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.constraint.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.biopax.paxtools.pattern.constraint.ConBox.*;

/**
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ControlsStateChangeMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor for extending purposes.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	public ControlsStateChangeMiner(String name, String description)
	{
		super(name, description);
	}

	/**
	 * Constructor that sets name and description.
	 */
	public ControlsStateChangeMiner()
	{
		super(SIFType.CONTROLS_STATE_CHANGE.getTag(), "Finds relations between proteins where " +
			"the first one is controlling a reaction that changes the state of the second one. " +
			"The reaction has to be a Conversion and modified Protein should be represented with " +
			"different PhysicalEntity on each side. This pattern cannot determine the sign of " +
			"the effect because it is hard to predict the effect of the modification.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
//		Pattern p = PatternBox.controlsStateChange(true);
//		p.addConstraint(new Type(ProteinReference.class), "controller ER");
//		p.addConstraint(new Type(ProteinReference.class), "changed ER");
		Pattern p = new Pattern(ProteinReference.class, "controller PR");
//		p.addConstraint(hasXref("IGKC"), "controller PR");
		p.add(isHuman(), "controller PR");
		p.add(erToPE(), "controller PR", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "controller PR");
		p.add(new InputOrOutput(RelType.INPUT, true), "Conversion", "input PE");
		p.add(linkToSimple(), "input PE", "input simple PE");
		p.add(new Type(Protein.class), "input simple PE");
		p.add(peToER(), "input simple PE", "changed PR");
		p.add(new OtherSide(), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSimple(), "output PE", "output simple PE");
		p.add(peToER(), "output simple PE", "changed PR");

		return p;
	}

	/**
	 * Writes the result as "A B", where A and B are gene symbols, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, true, getSourceLabel(), getTargetLabel());
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
	public String getSourceLabel()
	{
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed PR";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_STATE_CHANGE;
	}

	@Override
	public String[] getPubmedHarvestableLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
