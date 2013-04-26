package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
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
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ControlsStateChangeButIsParticipantMiner extends ControlsStateChangeMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsStateChangeButIsParticipantMiner()
	{
		super("Controls-state-change-but-a-participant", "Mines the same relation as with " +
			"Controls-state-change, however, this time the controller is modeled as a " +
			"participant of the Conversion. This is in fact a modeling error, but this pattern " +
			"exists in some resources. The controller PhysicalEntity appears at both left and " +
			"right of the Conversion.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.controlsStateChangeButIsParticipant(true);
		p.addConstraint(new Type(ProteinReference.class), "controller ER");
		p.addConstraint(new Type(ProteinReference.class), "changed ER");
		return p;
	}
}
