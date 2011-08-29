package org.biopax.paxtools.io.pathwayCommons.model;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.List;

/**
 * Class mapping of http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
 */
public class SearchHit {
    private String uri;
    private Class<? extends BioPAXElement> biopaxClass;

    private List<String> names;
    private List<String> dataSources;
    private List<String> organisms;
    private List<String> pathways;
    private List<String> excerpts;
    private List<String> actualHitUris;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Class<? extends BioPAXElement> getBioPAXClass() {
        return biopaxClass;
    }

    public void setBioPAXClass(Class<? extends BioPAXElement> bioPAXClass) {
        this.biopaxClass = bioPAXClass;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public List<String> getOrganisms() {
        return organisms;
    }

    public void setOrganisms(List<String> organisms) {
        this.organisms = organisms;
    }

    public List<String> getPathways() {
        return pathways;
    }

    public void setPathways(List<String> pathways) {
        this.pathways = pathways;
    }

    public List<String> getExcerpts() {
        return excerpts;
    }

    public void setExcerpts(List<String> excerpts) {
        this.excerpts = excerpts;
    }

    public List<String> getActualHitUris() {
        return actualHitUris;
    }

    public void setActualHitUris(List<String> actualHitUris) {
        this.actualHitUris = actualHitUris;
    }
}
