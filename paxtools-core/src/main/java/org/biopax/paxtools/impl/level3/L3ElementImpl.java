package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.bridge.builtin.StringBridge;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NamedQueries({
	@NamedQuery(name="org.biopax.paxtools.impl.level3.elementByRdfId",
		query="from org.biopax.paxtools.model.BioPAXElement as el where upper(el.RDFId) = upper(:rdfid)"),
	@NamedQuery(name="org.biopax.paxtools.impl.level3.elementByRdfIdEager",
		query="from org.biopax.paxtools.model.BioPAXElement as el fetch all properties where upper(el.RDFId) = upper(:rdfid)")
})
abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;

    L3ElementImpl()
    {
        this.comment = new HashSet<String>();
    }

    @ElementCollection
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_COMMENT, index = Index.TOKENIZED)
	@FieldBridge(impl=StringBridge.class)
    public Set<String> getComment()
    {
        return this.comment;
    }

    public void setComment(Set<String> comment)
    {
        this.comment = comment;
    }

    public void addComment(String COMMENT)
    {
        this.comment.add(COMMENT);
    }

    public void removeComment(String COMMENT)
    {
        this.comment.remove(COMMENT);
    }

}
