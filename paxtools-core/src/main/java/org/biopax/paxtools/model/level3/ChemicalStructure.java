package org.biopax.paxtools.model.level3;


public interface ChemicalStructure extends UtilityClass
{
	// Property STRUCTURE-DATA

	String getStructureData();

	 void setStructureData(String structureData);


	// Property STRUCTURE-FORMAT

	StructureFormatType getStructureFormat();

	void setStructureFormat(StructureFormatType structureFormat);

}
