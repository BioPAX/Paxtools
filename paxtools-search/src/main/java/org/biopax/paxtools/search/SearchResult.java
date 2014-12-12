package org.biopax.paxtools.search;

import java.util.List;

import org.biopax.paxtools.model.BioPAXElement;


public class SearchResult {
	
	private List<BioPAXElement> hits;
	private long totalHits;
	private long maxHitsPerPage = 100;
	private int page = 0;
	
	public SearchResult() {
	}


	public List<BioPAXElement> getHits() {
		return hits;
	}


	public void setHits(List<BioPAXElement> hits) {
		this.hits = hits;
	}


	public long getTotalHits() {
		return totalHits;
	}


	public void setTotalHits(long totalHits) {
		this.totalHits = totalHits;
	}


	public long getMaxHitsPerPage() {
		return maxHitsPerPage;
	}


	public void setMaxHitsPerPage(long maxHitsPerPage) {
		this.maxHitsPerPage = maxHitsPerPage;
	}


	public int getPage() {
		return page;
	}


	public void setPage(int page) {
		this.page = page;
	}
	
}
