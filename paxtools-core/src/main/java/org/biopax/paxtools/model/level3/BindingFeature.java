package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.util.AutoComplete;

public interface BindingFeature extends EntityFeature
{

	/**
	 * A binding feature represents a "half" of the bond between two entities. This property points to
	 * another binding feature which represents the other half. The bond can be covalent or
	 * non-covalent.
	 *
	 * @return paired binding feature.
	 */
	@AutoComplete(forward = false)
    @Key
	BindingFeature getBindsTo();

	/**
	 * A binding feature represents a "half" of the bond between two entities. This property points to
	 * another binding feature which represents the other half. The bond can be covalent or
	 * non-covalent.
	 *
	 * @param bindsTo paired binding feature.
	 */
	void setBindsTo(BindingFeature bindsTo);


    /**
     * IntraMolecular flag is true iff this binding feature represents a bond within the same molecule,  for example a
     * disulfide bond within the same molecule.
     * A true value true implies that this.isEntityFeatureOf() == this.getBindsTo.isEntityFeatureOf() although the
     * inverse is not true e.g a chain of actin proteins.
     * @return true iff this binding feature represents a bond within the same molecule.
     */
    @Key
    Boolean getIntraMolecular();

	/**
	 * IntraMolecular flag is true iff this binding feature represents a bond within the same molecule,  for example a
	 * disulfide bond within the same molecule.
	 * A true value implies that this.isEntityFeatureOf() == this.getBindsTo.isEntityFeatureOf() although the
	 * inverse is not true e.g a chain of actin proteins.
	 * @param intramolecular whether if this  binding feature represents a bond within the same molecule.
	 */
	void setIntraMolecular(Boolean intramolecular);

}
