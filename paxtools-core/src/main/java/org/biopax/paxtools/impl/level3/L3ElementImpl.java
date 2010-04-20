package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.*;


@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE) TODO: decide on inheritance strategy
abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;
	private Long proxyId = 0L;

	L3ElementImpl()
    {
	    comment = new HashSet<String>();
    }


	@Id
    @GeneratedValue(strategy= AUTO)
    @Column(name="proxy_id")
	private Long getProxyId() {
		return proxyId;
	}

	private void setProxyId(Long value) {
		proxyId = value;
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

    public void addComment(String comment)
    {
        this.comment.add(comment);
    }

    public void removeComment(String comment)
    {
        this.comment.remove(comment);
    }

	@Override
	public String toString()
	{
		return "L3ElementImpl{" +
		       "comment=" + comment +
		       ", proxyId=" + proxyId +
		       '}';
	}

	@Override
	@Column(nullable=false, unique=true)
	public String getRDFId()
	{
	    return super.getRDFId();
	}

}
