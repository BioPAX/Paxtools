package org.biopax.paxtools.io.pathwayCommons.model;

import java.util.List;

/**
 * Class mapping of http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
 */
public class SearchResponse {
    private Error error;
    private Long totalNumHits;
    private List<SearchHit> searchHits;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Long getTotalNumHits() {
        return totalNumHits;
    }

    public void setTotalNumHits(Long totalNumHits) {
        this.totalNumHits = totalNumHits;
    }

    public List<SearchHit> getSearchHits() {
        return searchHits;
    }

    public void setSearchHits(List<SearchHit> searchHits) {
        this.searchHits = searchHits;
    }
}
