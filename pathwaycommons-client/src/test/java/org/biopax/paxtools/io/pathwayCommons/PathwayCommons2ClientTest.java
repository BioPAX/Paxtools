package org.biopax.paxtools.io.pathwayCommons;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;

import cpath.service.jaxb.SearchResponse;

/**
 * INFO: when -DcPath2Url="http://...." system property is not set,
 * the default cpath2 endpoint URL is {@link PathwayCommons2Client#DEFAULT_ENDPOINT_URL}
 * (e.g., http://www.pathwaycommons.org/pc2/). So, it is possible that the 
 * default (official) service still provides an older cpath2 API than this PC2 client expects.
 * Take care. 
 */
@Ignore
public class PathwayCommons2ClientTest {
	
	@Test
	public final void testConnection() {
		PathwayCommons2Client client = new PathwayCommons2Client();
		Collection<String> vals = client.getValidTypes();
		assertFalse(vals.isEmpty());
		assertTrue(vals.contains("BioSource"));
	}
	
	
	@Test
	public final void testGetTopPathways() {
		PathwayCommons2Client client = new PathwayCommons2Client();
		
		SearchResponse result = null;
		try {
			result = client.getTopPathways();
		} catch (Exception e) {
			fail(client.getEndPointURL() + " is not compartible with this test! " + e);
		}
		
		assertNotNull(result);
		assertFalse(result.getSearchHit().isEmpty());
	}
}
