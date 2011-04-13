package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * Definition: The form of a physical entity in a particular experiment, as it may be modified for purposes of
 * experimental design.
 * <p/>
 * Examples: A His-tagged protein in a binding assay. A protein can be tagged by multiple tags,
 * so can have more than 1 experimental form type terms
 */
public interface ExperimentalForm extends UtilityClass
{


	/**
	 * Descriptor of this experimental form from a controlled vocabulary.
	 * The contents of this set should not be modified. Use add/remove methods instead.
	 * @return a CV term from PSI-MI participant identification methdod, experimental role, experimental preparation.
	 */
	Set<ExperimentalFormVocabulary> getExperimentalFormDescription();

	/**
	 * Adds an experimental form description.
	 * @param experimentalFormType descriptor of this experimental form from a controlled vocabulary.
	 */
	void addExperimentalFormDescription(ExperimentalFormVocabulary experimentalFormType);

	/**
	 * Removes an experimental form description.
	 * @param experimentalFormType descriptor of this experimental form from a controlled vocabulary.
	 */
	void removeExperimentalFormDescription(ExperimentalFormVocabulary experimentalFormType);


	/**
	 * @return The gene or physical entity that this experimental form describes.
	 */
	Entity getExperimentalFormEntity();

	void setExperimentalFormEntity(Entity newPARTICIPANT);

	/**
	 * Contents of this set should not be modified. Please use add/remove methods instead.
	 * @return A feature of the experimental form of the participant of the interaction,
	 *         such as a protein tag. It is not expected to occur in vivo or be necessary for the interaction.
	 */
	Set<EntityFeature> getExperimentalFeature();

	/**
	 * This method adds an experimental feature
	 * @param experimentalFeature of the experimental form of the participant of the interaction,
	 *         such as a protein tag. It is not expected to occur in vivo or be necessary for the interaction.
	 */
	void addExperimentalFeature(EntityFeature experimentalFeature);

	/**
	 * This method removes an experimental feature
	 * @param experimentalFeature of the experimental form of the participant of the interaction,
	 *         such as a protein tag. It is not expected to occur in vivo or be necessary for the interaction.
	 */
	void removeExperimentalFeature(EntityFeature experimentalFeature);
}
