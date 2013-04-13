package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass = SequenceInterval.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SequenceIntervalImpl extends SequenceLocationImpl
        implements SequenceInterval {

    private SequenceSite sequenceIntervalBegin;
    private SequenceSite sequenceIntervalEnd;

    public SequenceIntervalImpl() {
    }

    //
    // utilityClass (BioPAXElement) interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Transient
    public Class<? extends SequenceInterval> getModelInterface() {
        return SequenceInterval.class;
    }

    protected boolean semanticallyEquivalent(BioPAXElement element) {
        if (!(element instanceof SequenceInterval))
            return false;

        final SequenceInterval that = (SequenceInterval) element;
        return
          sequenceIntervalBegin != null &&
          sequenceIntervalBegin.isEquivalent(that.getSequenceIntervalBegin()) &&
          sequenceIntervalEnd != null &&
          sequenceIntervalEnd.isEquivalent(that.getSequenceIntervalEnd());
    }

    public int equivalenceCode() {
        int result = 29 + (sequenceIntervalBegin != null ?
                sequenceIntervalBegin.equivalenceCode() : 0);
        result = 29 * result +
                (sequenceIntervalEnd != null ? sequenceIntervalEnd.equivalenceCode() :
                        0);
        return result;
    }

    //
    // sequenceInterval interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    @ManyToOne(targetEntity = SequenceSiteImpl.class)//, cascade={CascadeType.ALL})
    public SequenceSite getSequenceIntervalBegin() {
        return sequenceIntervalBegin;
    }

    public void setSequenceIntervalBegin(SequenceSite sequenceIntervalBegin) {
        this.sequenceIntervalBegin = sequenceIntervalBegin;
    }

    @ManyToOne(targetEntity = SequenceSiteImpl.class)//, cascade={CascadeType.ALL})
    public SequenceSite getSequenceIntervalEnd() {
        return sequenceIntervalEnd;
    }

    public void setSequenceIntervalEnd(SequenceSite sequenceIntervalEnd) {
        this.sequenceIntervalEnd = sequenceIntervalEnd;
    }

    @Override
    public String toString()
    {
        return this.getSequenceIntervalBegin()+"-"+this.getSequenceIntervalEnd();
    }

}

