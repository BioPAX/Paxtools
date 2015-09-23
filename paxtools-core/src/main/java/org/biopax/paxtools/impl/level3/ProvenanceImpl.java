package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

import java.util.Comparator;
import java.util.TreeSet;

@Entity
@Proxy(proxyClass= Provenance.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProvenanceImpl extends NamedImpl implements Provenance
{
	private final static Log LOG = LogFactory.getLog(ProvenanceImpl.class);
	
	public ProvenanceImpl() {
	}

	@Transient
    public Class<? extends Provenance> getModelInterface()
	{
		return Provenance.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.biopax.paxtools.impl.BioPAXElementImpl#toString()
	 * 
	 * TODO this probably makes inconsistent strings (on different systems); fix, if it matters....
	 * 
	 */
	@Override public String toString()
	{
		try {
			StringBuilder s = new StringBuilder();

			for (String name : new TreeSet<String>(this.getName())) 
				s.append(name).append(";");
			
			if (!getXref().isEmpty()) 
			{
				TreeSet<Xref> xrefs = new TreeSet<Xref>(new Comparator<Xref>() {
					@Override
					public int compare(Xref o1, Xref o2) {
						return o1.toString().compareTo(o2.toString());
					}					
				});
				xrefs.addAll(getXref());
				
				s.append(" (");
				for (Xref anXref : xrefs)
					s.append(anXref).append(";");
				s.append(")");
			}
			
			return s.toString();

		} catch (Exception e) {
			// possible issues - when in a persistent context (e.g., lazy
			// collections init...)
			LOG.warn("toString: ", e);
			return getUri();
		}
	}
}
