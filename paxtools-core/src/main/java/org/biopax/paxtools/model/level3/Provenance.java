package org.biopax.paxtools.model.level3;

/**
 * Definition: The direct source of a pathway data or score. This does not store
 * the trail of sources from the generation of the data to this point, only the
 * last known source, such as a database. The XREF property may contain a
 * publicationXref referencing a publication describing the data source (e.g. a
 * database publication). A unificationXref may be used e.g. when pointing to an
 * entry in a database of databases describing this database. Examples: A
 * database, scoring method or person name.
 */
public interface Provenance extends UtilityClass, XReferrable,Named
{

}
