package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.BioPAXElement;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed
class RelationshipXrefImpl extends XrefImpl implements RelationshipXref
{

	private RelationshipTypeVocabulary relationshipType;

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

    @ManyToOne(targetEntity = RelationshipTypeVocabularyImpl.class)
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
