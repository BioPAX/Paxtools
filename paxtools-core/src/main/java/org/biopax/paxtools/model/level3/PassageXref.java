package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface PassageXref extends Xref
{

    // Property PUBLICATION
    PublicationXref getPublication();
    void setPublication(PublicationXref source);

    // Property EXACT
    String getExact();
    void setExact(String exact);

    // Property PREFIX
    String getPrefix();
    void setPrefix(String prefix);

    // Property SUFFIX
    String getSuffix();
    void setSuffix(String Suffix);

    // Property URL
    Set<String> getUrl();
    void addUrl(String url);
    void removeUrl(String url);


}
