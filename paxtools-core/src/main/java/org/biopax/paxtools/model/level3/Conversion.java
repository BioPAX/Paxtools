package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * Definition: An interaction in which molecules of one or more {@link PhysicalEntity} pools are physically
 * transformed and become a member of one or more other PhysicalEntity pools.
 * <p/>
 * Rationale: Conversion and pools of entities are two central abstractions of chemistry. They can be quantized with
 * rate and concentration respectively. A conversion in BioPAX, similar to chemistry,
 * is stoichiometric and closed world, i.e. it is assumed that all of the participants are listed. These properties
 * are due to the law of mass conservation. This differs from Control for example where multiple entities might be
 * controlling the controlled and everything that is not listed is assumed to be unknown. Conversions in BioPAX can
 * be reversible -- the property names Left and Right were preferred specifically because they are direction neutral
 * as opposed to substrate and product or input and output.
 * <p/>
 * Usage: Subclasses of conversion represent different types of transformation reflected by the properties of
 * different physicalEntity. {@link BiochemicalReaction}s will change the {@link ModificationFeature}s on a
 * {@link PhysicalEntity}, {@link Transport} will change the Cellular Location and {@link ComplexAssembly} will
 * change {@link BindingFeature}s. Generic Conversion class should only be used when the modification does not fit
 * into a any of these classes.
 * <p/>
 * Example: Opening of a voltage gated channel.
 */
public interface Conversion extends Interaction
{


	/**
	 * The participants on the left side of the conversion interaction. Since conversion interactions may proceed
	 * in either the left-to-right or right-to-left direction, occupants of the left property may be either
	 * reactants or products. left is a sub-property of participants.
	 * @return The participants on the left side of the conversion interaction.
	 */
	public Set<PhysicalEntity> getLeft();

	/**
	 * Adds a participant to the left side of the conversion interaction. Since conversion interactions may proceed
	 * in either the left-to-right or right-to-left direction, occupants of the left property may be either
	 * reactants or products. left is a sub-property of participants.
	 * @param left participants to be added to the left side of the conversion interaction.
	 */
	void addLeft(PhysicalEntity left);

	/**
	 * Removes a participant from the left side of the conversion interaction. Since conversion interactions may
	 * proceed
	 * in either the left-to-right or right-to-left direction, occupants of the left property may be either
	 * reactants or products. left is a sub-property of participants.
	 * @param left participants to be removed from the left side of the conversion interaction.
	 */
	void removeLeft(PhysicalEntity left);


	/**
	 * Stoichiometry of the left ({@link #getLeft()}) and right({@link #getRight()}) participants.
	 * Note: This is a "bridge" workaround for the n-ary relationship problem. There is no default stoichiometry (
	 * e.g. 1). Leaving this out will create reactions with unknown stoichiometry.
	 * It is a best practice to define the stoichiometry of each participant.
	 * It is invalid to define more than one stoichiometry per participant.
	 * It is invalid to reuse stoichiometry instances across conversions.
	 * @return Stoichiometry of the left ({@link #getLeft()}) and right({@link #getRight()}) participants.
	 */
	Set<Stoichiometry> getParticipantStoichiometry();

	/**
	 * This method adds a stoichiometry for one of the participants of this conversion.
	 * @param stoichiometry to be added
	 */
	void addParticipantStoichiometry(Stoichiometry stoichiometry);

	/**
	 * This method removes a stoichiometry for one of the participants of this conversion.
	 * @param stoichiometry to be removed
	 */
	void removeParticipantStoichiometry(Stoichiometry stoichiometry);


	// Property RIGHT

	/**
	 * The participants on the right side of the conversion interaction. Since conversion interactions may proceed
	 * in either the right-to-right or right-to-right direction, occupants of the right property may be either
	 * reactants or products. right is a sub-property of participants.
	 * @return The participants on the right side of the conversion interaction.
	 */
	public Set<PhysicalEntity> getRight();

	/**
	 * Adds a participant to the right side of the conversion interaction. Since conversion interactions may proceed
	 * in either the right-to-right or right-to-right direction, occupants of the right property may be either
	 * reactants or products. right is a sub-property of participants.
	 * @param right participants to be added to the right side of the conversion interaction.
	 */
	void addRight(PhysicalEntity right);

	/**
	 * Removes a participant from the right side of the conversion interaction. Since conversion interactions may
	 * proceed
	 * in either the right-to-right or right-to-right direction, occupants of the right property may be either
	 * reactants or products. right is a sub-property of participants.
	 * @param right participants to be removed from the right side of the conversion interaction.
	 */
	void removeRight(PhysicalEntity right);


	/**
	 * Specifies whether a conversion occurs spontaneously or not.
	 * <p/>
	 * If the spontaneity is not known,this property should not be specified.
	 * @return whether if this conversion is spontaneous.
	 */
	public Boolean getSpontaneous();

	/**
	 * Specifies whether a conversion occurs spontaneously or not.
	 * <p/>
	 * If the spontaneity is not known,this property should not be specified.
	 * @param spontaneous Whether if this conversion is spontaneous.
	 */
	public void setSpontaneous(Boolean spontaneous);


	/**
	 * This property represents the direction of the reaction. If a reaction will run in a single direction under
	 * all biological contexts then it is considered irreversible and has a direction. Otherwise it is reversible.
	 * @return One of REVERSIBLE, LEFT-TO-RIGHT or RIGHT-TO-LEFT
	 */
	public ConversionDirectionType getConversionDirection();

	/**
	 * This property represents the direction of the reaction. If a reaction will run in a single direction under
	 * all biological contexts then it is considered irreversible and has a direction. Otherwise it is reversible.
	 * @param conversionDirection One of REVERSIBLE, LEFT-TO-RIGHT or RIGHT-TO-LEFT --  null for unknown.
	 */
	public void setConversionDirection(ConversionDirectionType conversionDirection);


}
