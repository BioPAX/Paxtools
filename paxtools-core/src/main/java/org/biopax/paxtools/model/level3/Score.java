package org.biopax.paxtools.model.level3;


/**
 * <b>Definition:</b> A score associated with a publication reference describing how the score was
 * determined, the name of the method and a comment briefly describing the method.
 * <p/>
 * <b>Usage:</b>  The xref must contain at least one publication that describes the method used to
 * determine the score value. There is currently no standard way of describing  values, so any
 * string is valid.
 * <p/>
 * <b>Examples:</b> The statistical significance of a result, e.g. "p<0.05".
 */
public interface Score extends UtilityClass, XReferrable
{

	/**
	 * This property defines the value of the score. This can be a numerical or categorical value.
	 *
	 * @return Numerical or categorical value of the score
	 */
	public String getValue();

	/**
	 * This property defines the value of the score. This can be a numerical or categorical value.
	 *
	 * @param value Numerical or categorical value of the score
	 */
	public void setValue(String value);

	/**
	 * This property defines the source of the scoring methodology.
	 *
	 * @return A publication or web site describing the scoring methodology and the range of values.
	 */
	public Provenance getScoreSource();

	/**
	 * This property defines the source of the scoring methodology.
	 *
	 * @param scoreSource publication or web site describing the scoring methodology and the range of
	 *                    values.
	 */
	public void setScoreSource(Provenance scoreSource);
}
