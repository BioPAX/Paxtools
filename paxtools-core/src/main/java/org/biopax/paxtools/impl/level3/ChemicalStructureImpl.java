package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ChemicalStructure;
import org.biopax.paxtools.model.level3.StructureFormatType;


public class ChemicalStructureImpl extends L3ElementImpl implements ChemicalStructure
{
	private StructureFormatType structureFormat;

	private String structureData;

	public ChemicalStructureImpl() {
	}

	public Class<? extends ChemicalStructure> getModelInterface()
	{
		return ChemicalStructure.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof ChemicalStructure))
			return false;
		
		final ChemicalStructure that = (ChemicalStructure) element;

		return
			(structureData != null ?
				structureData.equals(that.getStructureData()) :
				that.getStructureData() == null)
				&&

				(structureFormat != null ?
					structureFormat.equals(that.getStructureFormat()) :
					that.getStructureFormat() == null);
	}

	public int equivalenceCode()
	{
		int result =
			29 + (structureFormat != null ? structureFormat.hashCode() : 0);
		result = 29 * result +
			(structureData != null ? structureData.hashCode() : 0);
		return result;
	}

	//
	// ChemicalStructure interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////
	public String getStructureData()
	{
		return structureData;
	}

	public void setStructureData(String structureData)
	{
		this.structureData = structureData;
	}

	public StructureFormatType getStructureFormat()
	{
		return structureFormat;
	}

	public void setStructureFormat(StructureFormatType structureFormat)
	{
		this.structureFormat = structureFormat;
	}
}
