package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 **/

@Entity
abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;
    private Long proxyId;

    L3ElementImpl()
    {
        this.comment = new HashSet<String>();
    }

    @ElementCollection
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

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long getProxyId() {
        return proxyId;
    }

    private void setProxyId(Long value) {
        proxyId = value;
    }

}
