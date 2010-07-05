package org.biopax.paxtools.model.level3;

/**
 * Tagger interface for non-complex physical entities
 */
public interface SimplePhysicalEntity extends PhysicalEntity
{

    EntityReference getEntityReference();

    void setEntityReference(EntityReference entityReference);
}
