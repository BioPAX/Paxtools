package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.SetEquivalenceChecker;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;
import java.util.regex.Pattern;

@Entity
@Proxy(proxyClass= ControlledVocabulary.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ControlledVocabularyImpl extends XReferrableImpl implements
	ControlledVocabulary
{
	private final static Log LOG = LogFactory.getLog(CellVocabularyImpl.class);
	
	private Set<String> term;
	private static final Pattern PATTERN = Pattern.compile("\\]|\\[");

	/**
	 * Constructor.
	 */
	public ControlledVocabularyImpl()
	{
		this.term = BPCollections.createSet();
	}

	//
	// BioPAXElement, Xreferrable implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
	public Class<? extends ControlledVocabulary> getModelInterface()
	{
		return ControlledVocabulary.class;
	}


	
	//
	// ControlledVocabulary interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property term

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ElementCollection(fetch=FetchType.EAGER)
	@JoinTable(name="term")
	@Field(name = FIELD_TERM, analyze=Analyze.YES)
	@FieldBridge(impl=SetStringBridge.class)
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
		Set<String> terms = BPCollections.createSet();
		terms.addAll(term);
		terms.retainAll(that.getTerm());

		
		return getModelInterface().equals(that.getModelInterface()) 
				&& (term.isEmpty() && that.getTerm().isEmpty() || !terms.isEmpty() )
				&& SetEquivalenceChecker.hasEquivalentIntersection(
				new ClassFilterSet<Xref, UnificationXref>(getXref(), UnificationXref.class),
				new ClassFilterSet<Xref, UnificationXref>(that.getXref(), UnificationXref.class));
	}
	
	@Override
	public String toString()
	{
		try {
			return PATTERN.matcher(term.toString()).replaceAll("");
		} catch (Exception e) {
			// in a persistent context, there might be 
			// a lazy collection initialization issue with this method...
			LOG.warn("toString(): ", e);
			return getRDFId();
		}
	}
}
