package org.biopax.paxtools.model.level3;


/**
 * Definition: The biological source of an entity (e.g. protein, RNA or DNA). Some entities are
 * considered source-neutral (e.g. small molecules), and the biological source of others can be
 * deduced from their constituentss (e.g. complex, pathway). Examples: HeLa cells, human, and mouse
 * liver tissue.
 */
public interface BioSource extends UtilityClass, Named
{

	/**
	 * A cell type, e.g. 'HeLa'. This should reference a term in a controlled vocabulary of cell types.
	 * Best practice is to refer to <a href="http://www.obofoundry.org/cgi-bin/detail.cgi?id=cell">OBOCell
	 * Ontology</a>.
	 * @return cell type if this biosource is a cell line or a specific tissue. Null otherwise.
	 */
	CellVocabulary getCellType();

	/**
	 * A cell type, e.g. 'HeLa'. This should reference a term in a controlled vocabulary of cell types.
	 * Best practice is to refer to <a href="http://www.obofoundry.org/cgi-bin/detail.cgi?id=cell">OBOCell
	 * Ontology</a>.
	 * @param cellType if this biosource is a cell line or a specific tissue. Null for n/a.
	 */
	void setCellType(CellVocabulary cellType);


	/**
	 * An external controlled vocabulary of tissue types. A reference to the  <a href = http://www.brenda-enzymes
	 * .info/>BRENDA</a>
	 * @return An external controlled vocabulary of tissue types.
	 */
	TissueVocabulary getTissue();

	/**
	 * An external controlled vocabulary of tissue types. A reference to the  <a href = http://www.brenda-enzymes
	 * .info/>BRENDA</a>
	 * @param tissue An external controlled vocabulary of tissue types.
	 */
	void setTissue(TissueVocabulary tissue);
}
