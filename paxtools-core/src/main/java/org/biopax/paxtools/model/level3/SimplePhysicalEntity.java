package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * Tagger interface for non-complex physical entities
 */
public interface SimplePhysicalEntity extends PhysicalEntity
{

    /**
     * Reference entity for this physical entity.
     * @return entity reference; i.e., that of a stateless canonical identifiable entity
     */
    EntityReference getEntityReference();

    /**
     * Reference entity for this physical entity.
     * @param entityReference a BioPAX EntityReference object (usually a more specific subclass of)
     */
    void setEntityReference(EntityReference entityReference);

	/**
	 * This method returns:
	 * <ul>
	 *  <li>The entity reference of this PhysicalEntity plus</li>
	 *  <li>If this PhysicalEntity has member PhysicalEntities their generic EntityReferences iteratively plus</li>
	 *  <li>If the EntityReference of this PhysicalEntity has member EntityReferences their members iteratively</li>
	 * </ul>
     * @return this and members' entity references (see above)
	*/
	Set<EntityReference> getGenericEntityReferences();

}
