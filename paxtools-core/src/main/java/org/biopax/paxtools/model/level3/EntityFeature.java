package org.biopax.paxtools.model.level3;


import java.util.Set;

/**
 * Description: A characteristic of a physical entity that can change while the entity still retains its biological
 * identity.
 * <p/>
 * Rationale: Two phosphorylated forms of a protein are strictly speaking different chemical  molecules. It is,
 * however, standard in biology to treat them as different states of the same entity,
 * where the entity is loosely defined based on sequence. Entity Feature class and its subclassses captures these
 * variable characteristics. A Physical Entity in BioPAX represents a pool of  molecules rather than an individual
 * molecule. This is a notion imported from chemistry (See {@link PhysicalEntity}). Pools are defined by a set of
 * Entity Features in the sense that a single molecule must have all of the features in the set in order to be
 * considered a member of the pool. Since it is impossible to list and experimentally test all potential features for
 * an  entity, features that are not listed in the selection criteria is neglected Pools can also be defined by the
 * converse by specifying features  that are known to NOT exist in a specific context. As DNA,
 * RNA and Proteins can be hierarchically organized into families based on sequence homology so can entity features.
 * The memberFeature property allows capturing such hierarchical classifications among entity features.
 * <p/>
 * Usage: Subclasses of entity feature describe most common biological instances and should be preferred whenever
 * possible. One common use case for instantiating  entity feature is, for describing active/inactive states of
 * proteins where more specific feature information is not available.
 * <p/>
 * Examples: Open/close conformational state of channel proteins, "active"/"inactive" states,
 * excited states of photoreactive groups.
 */
public interface EntityFeature extends UtilityClass, Observable
{

	/**
	 * Inverse of {@link EntityReference#getEntityFeature()}
	 * @return the EntityReference that this EntityFeature belongs to.
	 */
	EntityReference getEntityFeatureOf();


	/**
	 * Inverse of {@link PhysicalEntity#getFeature()}
	 * Contents of this set is generated automatically and should not be modified.
	 * @return The list of PhysicalEntities that were observed to have this feature.
	 */
	Set<PhysicalEntity> getFeatureOf();

	/**
	 * Inverse of {@link PhysicalEntity#getNotFeature()}
	 * Contents of this set is generated automatically and should not be modified.
	 * @return The list of PhysicalEntities that were observed to NOT have this feature.
	 */
	Set<PhysicalEntity> getNotFeatureOf();


	/**
	 * Location of the feature on the sequence of the interactor.
	 * <p/>
	 * For modification features this is the modified base or residue. For binding features this is the
	 * binding site and for fragment features this is the location of the fragment on the "base" sequence.
	 * <p/>
	 * One feature may have more than one location, used e.g. for features which involve sequence positions close in
	 * the folded, three-dimensional state of a protein, but non-continuous along the sequence.
	 * <p/>
	 * Small Molecules can have binding features but currently it is not possible to define the binding site on the
	 * small molecules. In those cases this property should not be specified.
	 * @return Location of the feature on the sequence of the interactor.
	 */
	SequenceLocation getFeatureLocation();

	/**
	 * Location of the feature on the sequence of the interactor.
	 * <p/>
	 * For modification features this is the modified base or residue. For binding features this is the
	 * binding site and for fragment features this is the location of the fragment on the "base" sequence.
	 * <p/>
	 * One feature may have more than one location, used e.g. for features which involve sequence positions close in
	 * the folded, three-dimensional state of a protein, but non-continuous along the sequence.
	 * <p/>
	 * Small Molecules can have binding features but currently it is not possible to define the binding site on the
	 * small molecules. In those cases this property should not be specified.
	 * @param sequenceLocation of the feature
	 */
	void setFeatureLocation(SequenceLocation sequenceLocation);

	/**
	 * A controlled vocabulary term describing the type of the sequence location such as C-Terminal or SH2 Domain.
	 * Using Sequence Ontology (<a href ="http://www.sequenceontology.org">http://www.sequenceontology.org</a>) is
	 * recommended.
	 * @return A CV term describing the location of the feature
	 */
	SequenceRegionVocabulary getFeatureLocationType();

	/**
	 * A controlled vocabulary term describing the type of the sequence location such as C-Terminal or SH2 Domain.
	 * Using Sequence Ontology (<a href ="http://www.sequenceontology.org">http://www.sequenceontology.org</a>) is
	 * recommended.
	 * @param regionVocabulary A CV term describing the location of the feature
	 */
	void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary);


	/**
	 * An entity feature  that belongs to this homology grouping.
	 * <p/>
	 * Members of this set should be of the same class of this EntityFeature.
	 * <p/>
	 * Members of this set should be an EntityFeature of an EntityReference which is a memberEntityReference of the
	 * EntityReference of this feature.
	 * <p/>
	 * If this set is not empty than the sequenceLocation of this feature should be null.
	 * <p/>
	 * Example: a homologous phosphorylation site across a protein family.
	 * @return An entity feature  that belongs to this homology grouping.
	 */
	Set<EntityFeature> getMemberFeature();   //todo generify?

	/**
	 * An entity feature  that belongs to this homology grouping.
	 * <p/>
	 * Members of this set should be of the same class of this EntityFeature.
	 * <p/>
	 * Members of this set should be an EntityFeature of an EntityReference which is a memberEntityReference of the
	 * EntityReference of this feature.
	 * <p/>
	 * If this set is not empty than the sequenceLocation of this feature should be null.
	 * <p/>
	 * Example: a homologous phosphorylation site across a protein family.
	 * @param entityFeature An entity feature  that belongs to this homology grouping.
	 */
	void addMemberFeature(EntityFeature entityFeature);

	/**
	 * An entity feature  that belongs to this homology grouping.
	 * <p/>
	 * Members of this set should be of the same class of this EntityFeature.
	 * <p/>
	 * Members of this set should be an EntityFeature of an EntityReference which is a memberEntityReference of the
	 * EntityReference of this feature.
	 * <p/>
	 * If this set is not empty than the sequenceLocation of this feature should be null.
	 * <p/>
	 * Example: a homologous phosphorylation site across a protein family.
	 * @param entityFeature An entity feature that belongs to this homology grouping.
	 */
	void removeMemberFeature(EntityFeature entityFeature);

	/**
	 * @param that EntityFeature to be compared
	 * @return returns true iff the given feature is at the equivalent sequence location with this feature
	 */
	boolean atEquivalentLocation(EntityFeature that);

	/**
	 * Reverse of {@link #getMemberFeature()}
	 * @return the generic feature(s) that this feature belong to
	 */
	Set<EntityFeature> getMemberFeatureOf();
}
