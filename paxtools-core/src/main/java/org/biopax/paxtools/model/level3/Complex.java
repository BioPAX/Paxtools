package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface Complex extends PhysicalEntity
{

// --------------------- ACCESORS and MUTATORS---------------------

	// Property COMPONENTS

	/**
	 * Subunits of this complex. This property can contain other complexes and physical entities.
	 * Complex nesting should NOT be used for representing assembly order. This is best captured by
	 * complexAssembly interactions. Nesting however can be used for organizational abstractions (
	 * small and large units of ribosome) or for representing so called "black-box" complexes,
	 * complexes whose components are unknown/unspecified. Unlike BioPAX level 2, component physical
	 * entities do not change their state when in a complex  ( although they do when they have a
	 * binding feature) and can be reused in other reactions and complexes.
	 *
	 * @return components of this complex
	 */
	Set<PhysicalEntity> getComponent();

	/**
	 * Subunits of this complex. This property can contain other complexes and physical entities.
	 * Complex nesting should NOT be used for representing assembly order. This is best captured by
	 * complexAssembly interactions. Nesting however can be used for organizational abstractions (
	 * small and large units of ribosome) or for representing so called "black-box" complexes,
	 * complexes whose components are unknown/unspecified. Unlike BioPAX level 2, component physical
	 * entities do not change their state when in a complex  ( although they do when they have a
	 * binding feature) and can be reused in other reactions and complexes.
	 *
	 * @param component to be added as a new member
	 */
	void addComponent(PhysicalEntity component);

	/**
	 * Subunits of this complex. This property can contain other complexes and physical entities.
	 * Complex nesting should NOT be used for representing assembly order. This is best captured by
	 * complexAssembly interactions. Nesting however can be used for organizational abstractions (
	 * small and large units of ribosome) or for representing so called "black-box" complexes,
	 * complexes whose components are unknown/unspecified. Unlike BioPAX level 2, component physical
	 * entities do not change their state when in a complex  ( although they do when they have a
	 * binding feature) and can be reused in other reactions and complexes.
	 * <p/>
	 *
	 * @param component to be removed from members.
	 */
	void removeComponent(PhysicalEntity component);

	/**
	 * Subunits of this complex. This property can contain other complexes and physical entities.
	 * Complex nesting should NOT be used for representing assembly order. This is best captured by
	 * complexAssembly interactions. Nesting however can be used for organizational abstractions (
	 * small and large units of ribosome) or for representing so called "black-box" complexes,
	 * complexes whose components are unknown/unspecified. Unlike BioPAX level 2, component physical
	 * entities do not change their state when in a complex  ( although they do when they have a
	 * binding feature) and can be reused in other reactions and complexes.
	 * <p/>
	 * WARNING: This method should only be used for batch operations and with care. For regular
	 * manipulation use add/remove instead
	 *
	 * @param component new list of components for this complex
	 */

	void setComponent(Set<PhysicalEntity> component);

// Property Component-STOICHIOMETRY

	/**
	 * The stoichiometry of components in a complex.
	 *
	 * @return the stoichiometry of components in a complex.
	 */
	Set<Stoichiometry> getComponentStoichiometry();

	/**
	 * The stoichiometry of components in a complex.
	 *
	 * @param stoichiometry add a stoichiometry for the member.
	 */
	void addComponentStoichiometry(Stoichiometry stoichiometry);

	void removeComponentStoichiometry(Stoichiometry stoichiometry);

	/**
	 * * WARNING: This method should only be used for batch operations and with care. For regular
	 * manipulation use add/remove instead
	 *
	 * @param stoichiometry
	 */
	void setComponentStoichiometry(Set<Stoichiometry> stoichiometry);

	/**
	 * Gets the member physical entities which are not complex. When the complex is nested, members
	 * of inner complexes are added to the returned set recursively.
	 *
	 * @return non-complex member physical entities
	 */
	Set<SimplePhysicalEntity> getSimpleMembers();

	/**
	 * Gets the <code>EntityReference</code>s of the member simple physical entities. When the
	 * complex is nested, contents of the member complexes are retrieved recursively.
	 * @return
	 */
	Set<EntityReference> getMemberReferences();

}
