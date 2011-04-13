package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.util.AutoComplete;

import java.util.Set;

/**
 * Definition: An entity reference is a grouping of several physical entities across different
 * contexts and molecular states, that share common physical properties and often named and treated
 * as a single entity with multiple states by biologists.
 * <p/>
 * Rationale:   Many protein, small molecule and gene databases share this point of view, and such a
 * grouping is an important prerequisite for interoperability with those databases. Biologists would
 * often group different pools of molecules in different contexts under the same name. For example
 * cytoplasmic and extracellular calcium have different effects on the cell's behavior, but they are
 * still called calcium. For DNA, RNA and Proteins the grouping is defined based on a wildtype
 * sequence, for small molecules it is defined by the chemical structure.
 * <p/>
 * Usage: Entity references store the information common to a set of molecules in various states
 * described in the BioPAX document, including database cross-references. For instance, the P53
 * protein can be phosphorylated in multiple different ways. Each separate P53 protein (pool) in a
 * phosphorylation state would be represented as a different protein (child of physicalEntity) and
 * all things common to all P53 proteins, including all possible phosphorylation sites, the sequence
 * common to all of them and common references to protein databases containing more information
 * about P53 would be stored in a Entity Reference.
 * <p/>
 * Comments: This grouping has three semantic implications: <ol> <li>Members of different pools
 * share many physical and biochemical properties. This includes their chemical structure, sequence,
 * organism and set of molecules they react with. They will also share a lot of secondary
 * information such as their names, functional groupings, annotation terms and database identifiers.
 * <li> A small number of transitions seperates these pools. In other words it is relatively easy
 * and frequent for a molecule to transform from one physical entity to another that belong to the
 * same reference entity. For example an extracellular calcium can become cytoplasmic, and p53 can
 * become phosphorylated. However no calcium virtually becomes sodium, or no p53 becomes mdm2. In
 * the former it is the sheer energy barrier of a nuclear reaction, in the latter sheer statistical
 * improbability of synthesizing the same sequence without a template. If one thinks about the
 * biochemical network as molecules transforming into each other, and remove edges that respond to
 * transcription, translation, degradation and covalent modification of small molecules, each
 * remaining component is a reference entity. <li> Some of the pools in the same group can overlap.
 * p53-p@ser15 can overlap with p53-p@thr18. Most of the experiments in molecular biology will only
 * check for one state variable, rarely multiple, and never for the all possible combinations. So
 * almost all statements that refer to the state of the molecule talk about a pool that can overlap
 * with other pools. However no overlaps is possible between molecules of different groups. </ol>
 */

public interface EntityReference extends UtilityClass, Named, Observable
{


	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 * @return a set of entityFeatures
	 */
	@AutoComplete(backward = true)
	Set<EntityFeature> getEntityFeature();

	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 * @param feature to be added.
	 */
	void addEntityFeature(EntityFeature feature);

	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 * @param feature to be removed.
	 */
	void removeEntityFeature(EntityFeature feature);


	/**
	 * Inverse of {@link SimplePhysicalEntity#getEntityReference()}
	 *
	 * @return a  set of {@link SimplePhysicalEntity} that has this EntityReference
	 */
	Set<SimplePhysicalEntity> getEntityReferenceOf();


	/**
	 * @return Controlled vocabulary terms that is used to describe the type of grouping such as
	 *         homology or functional group.
	 */
	Set<EntityReferenceTypeVocabulary> getEntityReferenceType();

	/**
	 * Adds the given cv to the list of types
	 *
	 * @param type A controlled vocabulary term that is used to describe the type of grouping such as
	 *             homology or functional group.
	 */
	void addEntityReferenceType(EntityReferenceTypeVocabulary type);

	/**
	 * Removes the given cv from the list of types
	 *
	 * @param type A controlled vocabulary term that is used to describe the type of grouping such as
	 *             homology or functional group.
	 */
	void removeEntityReferenceType(EntityReferenceTypeVocabulary type);


	/**
	 * @return Entity references that qualifies for the definition of this group. For example a member
	 *         of a PFAM protein family.
	 */
	Set<EntityReference> getMemberEntityReference();  //todo generify?

/**
	 * Adds the given entityReference to the member list
	 *
	 * @param entityReference An entity reference that qualifies for the definition of this group. For
	 *                        example a member of a PFAM protein family.
	 */
	void addMemberEntityReference(EntityReference entityReference);

	/**
	 * Removes the given entityReference from the member list
	 *
	 * @param entityReference An entity reference that qualifies for the definition of this group. For
	 *                        example a member of a PFAM protein family.
	 */
	void removeMemberEntityReference(EntityReference entityReference);

	/**
	 * Reverse of {@link #getMemberEntityReference()}
	 *
	 * @return EntityReferences that this EntityReference is a member of.
	 */
	public Set<EntityReference> getMemberEntityReferenceOf();

}
