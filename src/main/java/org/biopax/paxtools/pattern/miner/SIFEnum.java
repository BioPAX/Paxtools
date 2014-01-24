package org.biopax.paxtools.pattern.miner;

import java.util.Arrays;
import java.util.List;

/**
 * Enum for representing supported SIF edge types.
 *
 * @author Ozgun Babur
 */
public enum SIFEnum implements SIFType
{
	CONTROLS_STATE_CHANGE_OF("First protein is controlling a reaction that changes the state of " +
		"the second protein.", true, ControlsStateChangeOfMiner.class,
		CSCOButIsParticipantMiner.class, CSCOBothControllerAndParticipantMiner.class,
		CSCOThroughControllingSmallMoleculeMiner.class, CSCOThroughBindingSmallMoleculeMiner.class),
	CONTROLS_TRANSPORT_OF("First protein is controlling a reaction that changes the cellular " +
		"location of the second protein.", true, ControlsTransportMiner.class),
	CONTROLS_EXPRESSION_OF("First protein is controlling a conversion or a template reaction that" +
		"changes expression of the second protein.", true, ControlsExpressionMiner.class,
		ControlsExpressionWithConvMiner.class),
	CONTROLS_DEGRADATION_OF("First protein is controlling a reaction that degrades second " +
		"protein, i.e. second protein is input to a reaction with no output", true,
		ControlsDegradationMiner.class),
	CATALYSIS_PRECEDES("First protein is controlling a reaction whose output molecule is input" +
		" to another reaction controlled by the second protein.", true,
		CatalysisPrecedesMiner.class),
	IN_COMPLEX_WITH("Proteins appear as members of the same complex.", false,
		InComplexWithMiner.class),
	INTERACTS_WITH("Proteins appear as participants of the same MolecularInteraction", false,
		InteractsWithMiner.class),
	NEIGHBOR_OF("Proteins appear as participants or controllers of the same interaction.", false,
		NeighborOfMiner.class),
	CONSUMPTION_CONTROLLED_BY("The small molecule is consumed by a reaction that is controlled by" +
		" a protein", true, ConsumptionControlledByMiner.class),
	CONTROLS_PRODUCTION_OF("The protein is controlling a reaction of which the small molecule is " +
		"an output", true, ControlsProductionOfMiner.class),
	CONTROLS_TRANSPORT_OF_CHEMICAL("The protein is controlling a reaction that changes cellular " +
		"location of the small molecule.", true, ControlsTransportOfChemicalMiner.class),
	CHEMICAL_AFFECTS("A small molecule has an effect on a protein.", true,
		ChemicalAffectsThroughControlMiner.class, ChemicalAffectsThroughBindingMiner.class),
	REACTS_WITH("A small molecule is input to a biochemical reaction together with another small " +
		"molecule. None of the molecules are also output.", false, ReactsWithMiner.class),
	USED_TO_PRODUCE("A small molecule is input to a biochemical reaction that produces " +
		"another small molecule. Both small molecules appear at only one side of the reaction.",
		true, UsedToProduceMiner.class),
	;

	/**
	 * Constructor with parameters.
	 * @param description description of the edge type
	 * @param directed whether the edge type is directed
	 */
	private SIFEnum(String description, boolean directed, Class<? extends SIFMiner>... miners)
	{
		this.description = description;
		this.directed = directed;
		this.miners = Arrays.asList(miners);
	}

	/**
	 * Description of the SIF type.
	 */
	private String description;

	/**
	 * Some SIF edges are directed and others are not.
	 */
	private boolean directed;

	/**
	 * SIF Miners to use during a search.
	 */
	private List<Class<? extends SIFMiner>> miners;

	/**
	 * Tag of a SIF type is derived from the enum name.
	 * @return tag
	 */
	public String getTag()
	{
		return name().toLowerCase().replaceAll("_", "-");
	}

	/**
	 * Asks if the edge is directed.
	 * @return true if directed
	 */
	public boolean isDirected()
	{
		return directed;
	}

	/**
	 * Gets the description of the SIF type.
	 * @return description
	 */
	public String getDescription()
	{
		return description;
	}

	@Override
	public List<Class<? extends SIFMiner>> getMiners()
	{
		return miners;
	}

	public static SIFEnum typeOf(String tag)
	{
		tag = tag.toUpperCase().replaceAll("-", "_");
		SIFEnum type = null;
		try
		{
			type = valueOf(tag);
		}
		catch (IllegalArgumentException e){}
		return type;
	}
}
