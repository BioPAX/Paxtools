package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface FigureXref extends Xref
{

    // Property PUBLICATION
    PublicationXref getPublication();
    void setPublication(PublicationXref source);

    // Property CODE
    String getCode();
    void setCode(String code);

    // Property URL
    Set<String> getUrl();
    void addUrl(String url);
    void removeUrl(String url);

    // Property CAPTION
    String getCaption();
    void setCaption(String caption);

}
