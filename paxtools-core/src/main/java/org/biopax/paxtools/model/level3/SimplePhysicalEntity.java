package org.biopax.paxtools.model.level3;

/**
 * Tagger interface for non-complex physical entities
 */
public interface SimplePhysicalEntity extends PhysicalEntity
{

    /**
    * Reference entity for this physical entity.
     */
    EntityReference getEntityReference();

    /**
    * Reference entity for this physical entity.
     */
    void setEntityReference(EntityReference entityReference);
}
