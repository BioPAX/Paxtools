package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= RelationshipXref.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	@Field(name=FIELD_KEYWORD, index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
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
