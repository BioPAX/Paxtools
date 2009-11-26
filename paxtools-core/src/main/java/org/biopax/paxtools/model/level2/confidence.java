package org.biopax.paxtools.model.level2;

/**
 * Confidence that the containing instance actually occurs or exists in vivo,
 * usually a statistical measure. The xref must contain at least on publication
 * that describes the method used to determine the confidence. There is
 * currently no standard way of describing confidence values, so any string is
 * valid for the confidence value. In the future, a controlled vocabulary of
 * accepted confidence values could become available, in which case it will
 * likely be adopted for use here to describe the value.
 * <p/>
 * <b>Examples:</b> The statistical significance of a result, e.g. "p<0.05".
 */
public interface confidence extends utilityClass, XReferrable
{

    /**
     * The value of the confidence measure.
     * @return A string representation of the confidence value
     */
    public String getCONFIDENCE_VALUE();

    /**
     * The value of the confidence measure.
     * @param CONFIDENCE_VALUE to be set.
     */
    public void setCONFIDENCE_VALUE(String CONFIDENCE_VALUE);
}