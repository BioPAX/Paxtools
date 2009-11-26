package org.biopax.paxtools.model.level2;


/**
 * Describes a small molecule structure. Structure information is stored in the
 * property STRUCTURE-DATA, in one of three formats: the CML format (see
 * www.xml-cml.org), the SMILES format (see www.daylight.com/dayhtml/smiles/)
 * or the InChI format (http://www.iupac.org/inchi/). The STRUCTURE-FORMAT
 * property specifies which format is used.
 * <p/>
 * <b>Comment:</b> By virtue of the expressivity of CML, an instance of this
 * class can also provide additional information about a small molecule, such as
 * its chemical formula, names, and synonyms, if CML is used as the structure
 * format.
 *
 * <b>Examples:</b> The following SMILES string, which describes the structure
 * of glucose-6-phosphate:
 * 'C(OP(=O)(O)O)[CH]1([CH](O)[CH](O)[CH](O)[CH](O)O1)'.
 */
public interface chemicalStructure extends utilityClass
{

    public String getSTRUCTURE_DATA();

    public String getSTRUCTURE_FORMAT();

    public void setSTRUCTURE_DATA(String STRUCTURE_DATA);

    public void setSTRUCTURE_FORMAT(String STRUCTURE_FORMAT);
}