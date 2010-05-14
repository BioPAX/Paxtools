package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.util.AutoComplete;

public interface BindingFeature extends EntityFeature
{

	// Property boundTo
	/**
	 * A binding feature represents a "half" of the bond between two entities. This property points to
	 * another binding feature which represents the other half. The bond can be covalent or
	 * non-covalent.
	 *
	 * @return paired binding feature.
	 */
	@AutoComplete(forward = false)
	BindingFeature getBindsTo();

	/**
	 * A binding feature represents a "half" of the bond between two entities. This property points to
	 * another binding feature which represents the other half. The bond can be covalent or
	 * non-covalent.
	 *
	 * @param bindsTo paired binding feature.
	 */
	void setBindsTo(BindingFeature bindsTo);

	//property intramolecular

	Boolean getIntraMolecular();

	void setIntraMolecular(Boolean intramolecular);

}
