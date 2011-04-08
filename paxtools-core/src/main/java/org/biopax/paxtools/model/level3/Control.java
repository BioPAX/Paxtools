package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * Definition: An interaction in which one entity regulates, modifies, or otherwise influences a continuant entity,
 * i.e. pathway or interaction.
 * <p/>
 * Usage: Conceptually, physical entities are involved in interactions (or events) and the events are controlled or
 * modified, not the physical entities themselves. For example, a kinase activating a protein is a frequent event in
 * signaling pathways and is usually represented as an 'activation' arrow from the kinase to the substrate in
 * signaling diagrams. This is an abstraction, called "Activity Flow" representation,
 * that can be ambiguous without context. In BioPAX, this information should be captured as the kinase catalyzing
 * (via an instance of the catalysis class) a Biochemical Reaction in which the substrate is phosphorylated.
 * Subclasses of control define types specific to the biological process that is being controlled and should be used
 * instead of the generic "control" class when applicable.
 * <p/>
 * A control can potentially have multiple controllers. This acts as a logical AND,
 * i.e. both controllers are needed to regulate the  controlled event. Alternatively multiple controllers can control
 * the same event and this acts as a logical OR, i.e. any one of them is sufficient to regulate the controlled event.
 * Using this structure it is possible to describe arbitrary control logic using BioPAX.
 * <p/>
 * Rationale: Control can be temporally non-atomic, for example a pathway can control another pathway in BioPAX.
 * Synonyms: regulation, mediation
 * <p/>
 * Examples: A small molecule that inhibits a pathway by an unknown mechanism.
 */
public interface Control extends Interaction
{


	/**
	 * The entity that is controlled, e.g., in a biochemical reaction, the reaction is controlled by an enzyme.
	 * Controlled is a sub-property of participants.
	 * @return The entity that is controlled
	 */
	public Set<Process> getControlled();

	/**
	 * The entity that is controlled, e.g., in a biochemical reaction, the reaction is controlled by an enzyme.
	 * Controlled is a sub-property of participants.
	 * @param controlled The entity that is controlled
	 */
	public void addControlled(Process controlled);

	/**
	 * The entity that is controlled, e.g., in a biochemical reaction, the reaction is controlled by an enzyme.
	 * Controlled is a sub-property of participants.
	 * @param controlled The entity that is controlled
	 */
	public void removeControlled(Process controlled);


	/**
	 * The controlling entity, e.g., in a biochemical reaction, an enzyme is the controlling entity of the reaction.
	 * Controller is a sub-property of participants.
	 * @return The controlling entity
	 */
	public Set<Controller> getController();

	/**
	 * The controlling entity, e.g., in a biochemical reaction, an enzyme is the controlling entity of the reaction.
	 * Controller is a sub-property of participants.
	 * @param controller The controlling entity
	 */
	public void addController(Controller controller);

	/**
	 * The controlling entity, e.g., in a biochemical reaction, an enzyme is the controlling entity of the reaction.
	 * Controller is a sub-property of participants.
	 * @param controller The controlling entity
	 */
	public void removeController(Controller controller);


	/**
	 * Defines the nature of the control relationship between the CONTROLLER and the CONTROLLED entities.
	 *
	 * @return
	 */
	public ControlType getControlType();

	public void setControlType(ControlType controlType);


}
