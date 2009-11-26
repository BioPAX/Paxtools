package org.biopax.paxtools.model.level3;


public interface SequenceInterval extends SequenceLocation
{

    // Property SEQUENCE-INTERVAL-BEGIN

    SequenceSite getSequenceIntervalBegin();

    void setSequenceIntervalBegin(SequenceSite newSEQUENCE_INTERVAL_BEGIN);


    // Property SEQUENCE-INTERVAL-END

    SequenceSite getSequenceIntervalEnd();

    void setSequenceIntervalEnd(SequenceSite newSEQUENCE_INTERVAL_END);
}

