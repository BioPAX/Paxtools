package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.sequenceFeature;
import org.biopax.paxtools.model.level2.sequenceParticipant;
import org.biopax.paxtools.util.SetEquivalanceChecker;

import java.util.HashSet;
import java.util.Set;

/**
 */
class sequenceParticipantImpl extends physicalEntityParticipantImpl
	implements sequenceParticipant
{
// ------------------------------ FIELDS ------------------------------

	private Set<sequenceFeature> SEQUENCE_FEATURE_LIST;

// --------------------------- CONSTRUCTORS ---------------------------

	public sequenceParticipantImpl()
	{
		this.SEQUENCE_FEATURE_LIST = new HashSet<sequenceFeature>();
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return sequenceParticipant.class;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<sequenceFeature> getSEQUENCE_FEATURE_LIST()
	{
		return SEQUENCE_FEATURE_LIST;
	}

	public void addSEQUENCE_FEATURE_LIST(sequenceFeature SEQUENCE_FEATURE)
	{
		this.SEQUENCE_FEATURE_LIST.add(SEQUENCE_FEATURE);
	}

	public void removeSEQUENCE_FEATURE_LIST(sequenceFeature SEQUENCE_FEATURE)
	{
		this.SEQUENCE_FEATURE_LIST.remove(SEQUENCE_FEATURE);
	}

	public void setSEQUENCE_FEATURE_LIST(
		Set<sequenceFeature> SEQUENCE_FEATURE_LIST)
	{
		this.SEQUENCE_FEATURE_LIST = SEQUENCE_FEATURE_LIST;
	}

// -------------------------- OTHER METHODS --------------------------

	public boolean isInEquivalentState(physicalEntityParticipant pep)
	{
        if( pep instanceof sequenceParticipant )
        {

            final sequenceParticipant that = (sequenceParticipant) pep;
		    return
			    super.isInEquivalentState(that) &&
				    hasEquivalentFeatures(that);
        }
        else
        {
            return
			    super.isInEquivalentState(pep);
        }
            
    }

	private boolean hasEquivalentFeatures(sequenceParticipant that)
	{
		return SetEquivalanceChecker.isEquivalent(SEQUENCE_FEATURE_LIST,
                that.getSEQUENCE_FEATURE_LIST());
	}

	public int stateCode()
	{
		return 17 * super.stateCode() + 29 *
			(SEQUENCE_FEATURE_LIST != null ? SEQUENCE_FEATURE_LIST.hashCode() :
				0);
	}
}
