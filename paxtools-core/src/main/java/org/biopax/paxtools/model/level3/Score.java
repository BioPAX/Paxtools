package org.biopax.paxtools.model.level3;

public interface Score extends UtilityClass, XReferrable
{
    public String getValue();

    public void setValue(String value);

    public Provenance getScoreSource();

    public void setScoreSource(Provenance scoreSource);
}
