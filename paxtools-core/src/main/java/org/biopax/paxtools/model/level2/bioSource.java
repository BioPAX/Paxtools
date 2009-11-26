package org.biopax.paxtools.model.level2;

/**
 * The biological source of an entity (e.g. protein, RNA or DNA). Some entities
 * are considered source-neutral (e.g. small molecules), and the biological
 * source of others can be deduced from their constituentss (e.g. complex,
 * pathway).
 *
 * <b>Examples:</b> HeLa cells, human, and mouse liver tissue.
 */
public interface bioSource extends externalReferenceUtilityClass
{

// --------------------- ACCESORS and MUTATORS---------------------

    public openControlledVocabulary getCELLTYPE();

    public void setCELLTYPE(openControlledVocabulary CELLTYPE);


    public String getNAME();

    public void setNAME(String NAME);


    public unificationXref getTAXON_XREF();

    public void setTAXON_XREF(unificationXref TAXON_XREF);


    public openControlledVocabulary getTISSUE();

    public void setTISSUE(openControlledVocabulary TISSUE);
}