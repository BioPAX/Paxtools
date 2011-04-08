package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.util.AutoComplete;

import java.util.Set;

/**
 * <b>Definition</b>: A physical entity whose structure is comprised of other physical entities bound to each other
 * non-covalently, at least one of which is a macromolecule (e.g. protein, DNA, or RNA). Complexes must be stable enough
 * to function as a biological unit; in general, the temporary association of an enzyme with its substrate(s) should not
 * be considered or represented as a complex. A complex is the physical product of an interaction (complexAssembly) and
 * is not itself considered an interaction.
 * <p/>
 * <b>Comment</b>: In general, complexes should not be defined recursively so that smaller complexes exist within larger
 * complexes, i.e. a complex should not be a COMPONENT of another complex (see comments on the COMPONENT property). The
 * boundaries on the size of complexes described by this class are not defined here, although elements of the cell as
 * large and dynamic as, e.g., a mitochondrion would typically not be described using this class (later versions of this
 * ontology may include a cellularComponent class to represent these). The strength of binding and the topology of the
 * components cannot be described currently, but may be included in future versions of the ontology, depending on
 * community need.
 * <p/>
 * <b>Examples</b>: Ribosome, RNA polymerase II. Other examples of this class include complexes of multiple protein
 * monomers and complexes of proteins and small molecules.
 */

public interface Complex extends PhysicalEntity {


    /**
     * Defines the PhysicalEntity subunits of this complex. This property should not contain other complexes, i.e. it
     * should always be a flat representation of the complex. For example, if two protein complexes join to form a
     * single larger complex via a complex assembly interaction, the component of the new complex should be the
     * individual proteins of the smaller complexes, not the two smaller complexes themselves. Exceptions are black-box
     * complexes (i.e. complexes in which the component property is empty), which may be used as component of other
     * complexes because their constituent parts are unknown / unspecified. The reason for keeping complexes flat is to
     * signify that there is no information stored in the way complexes are nested, such as assembly order. Otherwise,
     * the complex assembly order may be implicitly encoded and interpreted by some users, while others created
     * hierarchical complexes randomly, which could lead to data loss. Additionally, the physicalEntityParticipants used
     * in the component property are in the context of the complex, thus should not be reused between complexes.  For
     * instance, a protein may participate in two different complexes, but have different conformation in each.
     *
     * @return components of this complex
     */
    @AutoComplete(backward = true)
    @Key Set<PhysicalEntity> getComponent();

    /**
     * Defines the PhysicalEntity subunits of this complex. This property should not contain other complexes, i.e. it
     * should always be a flat representation of the complex. For example, if two protein complexes join to form a
     * single larger complex via a complex assembly interaction, the component of the new complex should be the
     * individual proteins of the smaller complexes, not the two smaller complexes themselves. Exceptions are black-box
     * complexes (i.e. complexes in which the component property is empty), which may be used as component of other
     * complexes because their constituent parts are unknown / unspecified. The reason for keeping complexes flat is to
     * signify that there is no information stored in the way complexes are nested, such as assembly order. Otherwise,
     * the complex assembly order may be implicitly encoded and interpreted by some users, while others created
     * hierarchical complexes randomly, which could lead to data loss. Additionally, the physicalEntityParticipants used
     * in the component property are in the context of the complex, thus should not be reused between complexes.  For
     * instance, a protein may participate in two different complexes, but have different conformation in each.
     *
     * @param component to be added as a new member
     */
    void addComponent(PhysicalEntity component);

    /**
     * Defines the PhysicalEntity subunits of this complex. This property should not contain other complexes, i.e. it
     * should always be a flat representation of the complex. For example, if two protein complexes join to form a
     * single larger complex via a complex assembly interaction, the component of the new complex should be the
     * individual proteins of the smaller complexes, not the two smaller complexes themselves. Exceptions are black-box
     * complexes (i.e. complexes in which the component property is empty), which may be used as component of other
     * complexes because their constituent parts are unknown / unspecified. The reason for keeping complexes flat is to
     * signify that there is no information stored in the way complexes are nested, such as assembly order. Otherwise,
     * the complex assembly order may be implicitly encoded and interpreted by some users, while others created
     * hierarchical complexes randomly, which could lead to data loss. Additionally, the physicalEntityParticipants used
     * in the component property are in the context of the complex, thus should not be reused between complexes.  For
     * instance, a protein may participate in two different complexes, but have different conformation in each.
     * @param component to be removed from members.
     */
    void removeComponent(PhysicalEntity component);



    /**
     * The stoichiometry of components in a complex.
     *
     * @return the stoichiometry of components in a complex.
     */
    @Key Set<Stoichiometry> getComponentStoichiometry();

    /**
     * The stoichiometry of components in a complex.
     *
     * @param stoichiometry add a stoichiometry for the member.
     */
    void addComponentStoichiometry(Stoichiometry stoichiometry);

      /**
     * The stoichiometry of components in a complex.
     *
     * @param stoichiometry remove a stoichiometry for the member.
     */
    void removeComponentStoichiometry(Stoichiometry stoichiometry);


    /**
     * Gets the member physical entities which are not complex. When the complex is nested, members of inner complexes
     * are added to the returned set recursively.
     *
     * @return non-complex member physical entities
     */
    Set<SimplePhysicalEntity> getSimpleMembers();

    /**
     * Gets the <code>EntityReference</code>s of the member simple physical entities. When the complex is nested,
     * contents of the member complexes are retrieved recursively.
     *
     * @return
     */
    Set<EntityReference> getMemberReferences();

}
