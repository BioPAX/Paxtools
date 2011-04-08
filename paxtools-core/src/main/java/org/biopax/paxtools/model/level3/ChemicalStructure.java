package org.biopax.paxtools.model.level3;


/**
 * Definition: The chemical structure of a small molecule.
 * <p/>
 * Usage: Structure information is stored in the property structureData, in one of the three formats: <a
 * href ="www.xml-cml.org">CML</a>, <a href = "www.daylight.com/dayhtml/smiles/">SMILES</a> or <a href="http://www
 * .iupac.org/inchi/">InChI</a>.The structureFormat property specifies which format is used.
 * <p/>
 * Examples: The following SMILES string describes the structure of glucose-6-phosphate: 'C(OP(=O)(O)O)[CH]1([CH](O)
 * [CH](O)[CH](O)[CH](O)O1)'.
 */
public interface ChemicalStructure extends UtilityClass
{


	/**
	 * This property holds a string defining chemical structure,in one of the three formats:
	 * <a href ="www.xml-cml.org">CML</a>, <a href = "www.daylight.com/dayhtml/smiles/">SMILES</a> or
	 * <a href="http://www.iupac.org/inchi/">InChI</a>. If, for example,the CML format is used,
	 * then the value of this property is a string containing the XML encoding of the CML data.
	 * @return a string defining chemical structure
	 */
	@Key String getStructureData();

	/**
	 * This property holds a string of data defining chemical structure,in one of the three formats:
	 * <a href ="www.xml-cml.org">CML</a>, <a href = "www.daylight.com/dayhtml/smiles/">SMILES</a> or
	 * <a href="http://www.iupac.org/inchi/">InChI</a>. If, for example,the CML format is used,
	 * then the value of this property is a string containing the XML encoding of the CML data.
	 * @param structureData a string defining chemical structure
	 */
	 void setStructureData(String structureData);


	/**
	 * This property specifies which format is used to define chemical structure.
	 * @return format used to define chemical structure
	 */
	@Key StructureFormatType getStructureFormat();

	/**
	 * This property specifies which format is used to define chemical structure.
	 * @param structureFormat format used to define chemical structure
	 */
	void setStructureFormat(StructureFormatType structureFormat);

}
