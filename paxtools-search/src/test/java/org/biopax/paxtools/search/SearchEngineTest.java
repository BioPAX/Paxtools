/**
 * 
 */
package org.biopax.paxtools.search;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.junit.Test;


public class SearchEngineTest {
	final String indexLocation = System.getProperty("java.io.tmpdir") + File.separator + "SearchEngineTest_index";

	@Test
	public final void testSearch() throws IOException {
		SimpleIOHandler reader = new SimpleIOHandler();
		Model model = reader.convertFromOWL(getClass().getResourceAsStream("/pathwaydata1.owl"));
		SearchEngine searchEngine = new SearchEngine(model, indexLocation);
		searchEngine.index();
		assertTrue(new File(indexLocation).exists());
		
		SearchResult response = searchEngine.search("ATP", 0, null, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
		assertEquals(7, response.getHits().size());
		assertEquals(7, response.getTotalHits());
		
		response = searchEngine.search("ATP", 0, Interaction.class, null, null);
		assertNotNull(response);
		assertEquals(2, response.getHits().size());
		
		response = searchEngine.search("ATP", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertEquals(1, response.getHits().size());
		
		BioPAXElement hit = response.getHits().get(0);
		assertEquals(5, hit.getAnnotations().get(SearchEngine.HitAnnotation.HIT_SIZE.name()));
		assertTrue(hit instanceof Pathway);
		assertEquals(5, hit.getAnnotations().keySet().size());
		
		//test a special implementation for wildcard queries
		response = searchEngine.search("*", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertEquals(1, response.getHits().size());
		
		//find all objects (this here works with page=0 as long as the 
		//total no. objects in the test model < max hits per page)
		response = searchEngine.search("*", 0, null, null, null);
		assertEquals(50, response.getHits().size());
			
		response = searchEngine.search("*", 0, PhysicalEntity.class, null, null);
		assertEquals(8, response.getHits().size());
		
		response = searchEngine.search("*", 0, PhysicalEntity.class, null, new String[] {"562"});
		assertEquals(2, response.getHits().size());
		
		response = searchEngine.search("*", 0, PhysicalEntity.class, null, new String[] {"Escherichia"});
		assertEquals(2, response.getHits().size());
		
		response = searchEngine.search("*", 0, PhysicalEntity.class, null, new String[] {"Escherichia coliü"});
		assertEquals(2, response.getHits().size());
		
		response = searchEngine.search("*", 0, Provenance.class, null, null);
		assertEquals(2, response.getHits().size());
		
		response = searchEngine.search("*", 0, Provenance.class, new String[] {"kegg"}, null);
		assertEquals(1, response.getHits().size());
		
		//datasource filter using a URI (required for -update-counts console command and datasources.html page to work)
		response = searchEngine.search("*", 0, Pathway.class, new String[] {"http://identifiers.org/kegg.pathway/"}, null);
		assertEquals(1, response.getHits().size());
		
		response = searchEngine.search("pathway:glycolysis", 0, SmallMoleculeReference.class, null, null);
		assertEquals(5, response.getHits().size());
		
		//test search with pagination
		searchEngine.setMaxHitsPerPage(10);
		response = searchEngine.search("*", 0, null, null, null);
		assertEquals(50, response.getTotalHits());
		assertEquals(10, response.getHits().size());
		response = searchEngine.search("*", 1, null, null, null);
		assertEquals(10, response.getHits().size());
		
	}
}
