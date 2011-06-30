package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class RelationshipXrefImpl extends XrefImpl implements RelationshipXref
{

	private RelationshipTypeVocabulary relationshipType;

	public RelationshipXrefImpl() {
	}
	
	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
    public Class<? extends RelationshipXref> getModelInterface()
	{
		return RelationshipXref.class;
	}

	//
	// RelationshipXref interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    @ManyToOne(targetEntity = RelationshipTypeVocabularyImpl.class)//, cascade = {CascadeType.ALL})
	public RelationshipTypeVocabulary getRelationshipType()
	{
		return relationshipType;
	}

	public void setRelationshipType(RelationshipTypeVocabulary relationshipType)
	{
		this.relationshipType = relationshipType;
	}
	
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement other) {
		if(!(other instanceof RelationshipXref)) return false;
		
		RelationshipXref that = (RelationshipXref) other;
		
		return (
				(relationshipType != null) 
				? relationshipType.isEquivalent(that.getRelationshipType()) 
				: that.getRelationshipType()==null
				) && super.semanticallyEquivalent(other);
	}
}
