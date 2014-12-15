/**
 * 
 */
package org.biopax.paxtools.search;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.search.SearchEngine.HitAnnotation;
import org.junit.Test;


public class SearchEngineTest {
	final String indexLocation = System.getProperty("java.io.tmpdir") + File.separator;

	@Test
	public final void testSearch() throws IOException {
		SimpleIOHandler reader = new SimpleIOHandler();
		Model model = reader.convertFromOWL(getClass().getResourceAsStream("/pathwaydata1.owl"));
		SearchEngine searchEngine = new SearchEngine(model, indexLocation + "index1");
		searchEngine.index();
		assertTrue(new File(indexLocation + "index1").exists());
		
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
		assertEquals(4, hit.getAnnotations().get(SearchEngine.HitAnnotation.HIT_SIZE.name()));
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
		
		response = searchEngine.search("glycolysis", 0, SmallMoleculeReference.class, null, null);
		assertTrue(response.getHits().isEmpty()); //parent pathway names are searched for in the keywords, names, etc. default fields
		
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
	
	@Test
	public final void testHitsOrder() throws IOException {
		SimpleIOHandler reader = new SimpleIOHandler();
		Model model = reader.convertFromOWL(new GZIPInputStream(
				getClass().getResourceAsStream("/three-bmp-pathways.owl.gz")));
		
		//there are three BMP pathways (one is an empty pathway), and a sub-pathway (not bmp):
		//"http://purl.org/pc2/7/Pathway_3f75176b9a6272a62f9257f0540dc63b" ("bmppathway", "BMP receptor signaling")
		//"http://purl.org/pc2/7/Pathway_b8fa8401d3053b57a10d4c29a3211258" ("BMP signaling pathway")
		//"http://identifiers.org/reactome/REACT_12034.3" ("Signaling by BMP" - the one we want...)
		
		SearchEngine searchEngine = new SearchEngine(model, indexLocation + "index2");
		searchEngine.index();
		assertTrue(new File(indexLocation + "index2").exists());
		
		// search in default fields
		SearchResult response = searchEngine.search("signaling by bmp", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
		
		for(BioPAXElement bpe : response.getHits()) {
			System.out.println(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getRDFId(), 
					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
		}
		
		assertEquals(3, response.getHits().size());
		assertEquals(3, response.getTotalHits());
		
		
		//Next, search in 'name' field only using quoted string
		response = searchEngine.search("name:\"signaling by bmp\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
		
		for(BioPAXElement bpe : response.getHits()) {
			System.out.println(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getRDFId(), 
					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
		}
		
		//there are three BMP pathways (one is an empty pathway), and a sub-pathway (should not match) -
		assertEquals(1, response.getHits().size());
		assertEquals(1, response.getTotalHits());
		
		
		//Next, search in 'pathway' field only using quoted string
		response = searchEngine.search("pathway:\"signaling by bmp\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
		
		for(BioPAXElement bpe : response.getHits()) {
			System.out.println(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getRDFId(), 
					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
		}
		
		//there are three BMP pathways (one is an empty pathway), and a sub-pathway (should not match) -
		assertEquals(1, response.getHits().size());
		assertEquals(1, response.getTotalHits());
		assertEquals("http://identifiers.org/reactome/REACT_12034.3", response.getHits().get(0).getRDFId());
		
		
		response = searchEngine.search("pathway:\"bmp receptor signaling\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
		
		for(BioPAXElement bpe : response.getHits()) {
			System.out.println(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getRDFId(), 
					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
		}
	}
}
