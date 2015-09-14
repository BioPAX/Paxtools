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

    openControlledVocabulary getCELLTYPE();

    void setCELLTYPE(openControlledVocabulary CELLTYPE);


    String getNAME();

    void setNAME(String NAME);


    unificationXref getTAXON_XREF();

    void setTAXON_XREF(unificationXref TAXON_XREF);


    openControlledVocabulary getTISSUE();

    void setTISSUE(openControlledVocabulary TISSUE);
}