package org.biopax.paxtools.model.level3;

import java.util.Set;

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

	/**
	* This method returns:
	* <ul>
	*  <li>The entity reference of this PhysicalEntity plus</li>
	*  <li>If this PhysicalEntity has member PhysicalEntities their generic EntityReferences iteratively plus</li>
	*  <li>If the EntityReference of this PhysicalEntity has member EntityReferences their members iteratively</li>
	* </ul>
	*/
	Set<EntityReference> getGenericEntityReferences();

}
