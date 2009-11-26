package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.chemicalStructure;

/**
 */
class chemicalStructureImpl extends BioPAXLevel2ElementImpl
	implements chemicalStructure
{
// ------------------------------ FIELDS ------------------------------

	private String STRUCTURE_FORMAT;
	private String STRUCTURE_DATA;

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result =
			29 + (STRUCTURE_FORMAT != null ? STRUCTURE_FORMAT.hashCode() : 0);
		result = 29 * result +
			(STRUCTURE_DATA != null ? STRUCTURE_DATA.hashCode() : 0);
		return result;
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BioPAXElement ---------------------

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final chemicalStructure that = (chemicalStructure) element;

		return
			(STRUCTURE_DATA != null ?
				STRUCTURE_DATA.equals(that.getSTRUCTURE_DATA()) :
				that.getSTRUCTURE_DATA() == null)
				&&

				(STRUCTURE_FORMAT != null ?
					STRUCTURE_FORMAT.equals(that.getSTRUCTURE_FORMAT()) :
					that.getSTRUCTURE_FORMAT() == null);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return chemicalStructure.class;
	}

// --------------------- Interface chemicalStructure ---------------------


	public String getSTRUCTURE_FORMAT()
	{
		return STRUCTURE_FORMAT;
	}

	public void setSTRUCTURE_FORMAT(String STRUCTURE_FORMAT)
	{
		this.STRUCTURE_FORMAT = STRUCTURE_FORMAT;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public String getSTRUCTURE_DATA()
	{
		return STRUCTURE_DATA;
	}

	public void setSTRUCTURE_DATA(String STRUCTURE_DATA)
	{
		this.STRUCTURE_DATA = STRUCTURE_DATA;
	}
}
