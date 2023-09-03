package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Xref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.TreeSet;


public class ProvenanceImpl extends NamedImpl implements Provenance
{
	private final static Logger log = LoggerFactory.getLogger(ProvenanceImpl.class);
	
	public ProvenanceImpl() {
	}

    public Class<? extends Provenance> getModelInterface()
	{
		return Provenance.class;
	}

	@Override public String toString() {
		try {
			StringBuilder s = new StringBuilder();

			for (String name : new TreeSet<>(this.getName())) {
				s.append(name).append(";");
			}
			
			if (!getXref().isEmpty()) {
				TreeSet<Xref> xrefs = new TreeSet<>(Comparator.comparing(Xref::toString));
				xrefs.addAll(getXref()); //removes duplicate equivalent xrefs (same id and db)
				s.append(" (");
				for (Xref anXref : xrefs) {
					s.append(anXref).append(";");
				}
				s.append(")");
			}
			
			return s.toString();
		} catch (Exception e) {
			// possible in a persistent context (e.g., lazy collections init)
			log.warn("toString: ", e);
			return getUri();
		}
	}
}
