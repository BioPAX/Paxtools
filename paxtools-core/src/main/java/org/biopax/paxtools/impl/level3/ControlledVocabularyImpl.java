package org.biopax.paxtools.impl.level3;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.util.Set;


public class ControlledVocabularyImpl extends XReferrableImpl implements
	ControlledVocabulary
{
	private final static Log LOG = LogFactory.getLog(CellVocabularyImpl.class);
	
	private Set<String> term;

	/**
	 * Constructor.
	 */
	public ControlledVocabularyImpl()
	{
		this.term = BPCollections.I.createSet();
	}

	//
	// BioPAXElement, Xreferrable implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends ControlledVocabulary> getModelInterface()
	{
		return ControlledVocabulary.class;
	}


	
	//
	// ControlledVocabulary interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property term

	public Set<String> getTerm()
	{
		return term;
	}

	public void setTerm(Set<String> term)
	{
		this.term = term;
	}

	public void addTerm(String term)
	{
		if(term != null && term.length()>0)
			this.term.add(term);
	}

	public void removeTerm(String term)
	{
		if(term != null)
			this.term.remove(term);
	}
	
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element) {
		if(! (element instanceof ControlledVocabulary)) return false;
		
		ControlledVocabulary that = (ControlledVocabulary) element;
		Set<String> terms = BPCollections.I.createSet();
		terms.addAll(term);
		terms.retainAll(that.getTerm());

		
		return getModelInterface().equals(that.getModelInterface()) 
				&& (term.isEmpty() && that.getTerm().isEmpty() || !terms.isEmpty() )
				&& SetEquivalenceChecker.hasEquivalentIntersection(
					new ClassFilterSet<Xref, UnificationXref>(getXref(), UnificationXref.class),
					new ClassFilterSet<Xref, UnificationXref>(that.getXref(), UnificationXref.class)
				);
	}
	
	@Override
	public String toString()
	{
		String ret = getUri();
		try {
			// in a persistent context, there can be lazy collection initialization exception...
			if(!term.isEmpty())
				ret = getModelInterface().getSimpleName() +
					"_" + StringUtils.join(term, ",");
		} catch (Exception e) {		
			LOG.warn("toString(): ", e);
		}
		return ret;
	}
}
