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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchEngineTest {
	final String indexLocation = System.getProperty("java.io.tmpdir") + File.separator;
	final static Logger log = LoggerFactory.getLogger(SearchEngineTest.class);

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
		
		//now - same but using the long name (with 'of' an 'and') as filter value:
		response = searchEngine.search("*", 0, Provenance.class, new String[] {"Kyoto Encyclopedia of Genes and Genomes"}, null);
		assertEquals(1, response.getHits().size());
		
		//filter by data source (Provenance) URI - to tell Provenance objects having same name (but perhaps different organism or version)
		// from each other if necessary -
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
		
		//there are three BMP pathways (one is an empty pathway), and a trivial sub-pathway (not bmp):
		//"http://purl.org/pc2/7/Pathway_3f75176b9a6272a62f9257f0540dc63b" ("bmppathway", "BMP receptor signaling")
		//"http://purl.org/pc2/7/Pathway_b8fa8401d3053b57a10d4c29a3211258" ("BMP signaling pathway")
		//"http://identifiers.org/reactome/REACT_12034.3" ("Signaling by BMP" - the one we want...)
		//"http://purl.org/pc2/7/Pathway_aaa00bbf872fc777ce1e5eafc108752b" ("proteasomal ubiquitin-dependent protein catabolic process", a black-box)
		
		SearchEngine searchEngine = new SearchEngine(model, indexLocation + "index2");
		searchEngine.index();
		assertTrue(new File(indexLocation + "index2").exists());
		
		// A wide search in the default fields -
		SearchResult response = searchEngine.search("signaling by bmp", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());		
		int i=0;
		for(BioPAXElement bpe : response.getHits()) {
			log.debug(String.format("Hit %d: %s; size: %s; excerpt: %s",
					++i, bpe.getUri(), bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
		}		
		assertEquals(3, response.getHits().size());
		assertEquals(3, response.getTotalHits());
		assertEquals("http://purl.org/pc2/7/Pathway_3f75176b9a6272a62f9257f0540dc63b", response.getHits().get(0).getUri());
		//the order of hits is alright, though we'd love to see REACT_12034.3 on top...
		
		// more accurate search in all the default fields using quotation marks around -
		response = searchEngine.search("\"signaling by bmp\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
		assertEquals(1, response.getTotalHits());
		assertEquals("http://identifiers.org/reactome/REACT_12034.3", response.getHits().get(0).getUri());
		
		//Next, narrow search in 'name' field only using quoted string
		response = searchEngine.search("name:\"Signaling by BMP\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());
//		for(BioPAXElement bpe : response.getHits()) {
//			log.debug(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getUri(),
//					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
//					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
//		}
		//there are three BMP pathways (one is an empty pathway), and a sub-pathway (should not match) -
		assertEquals(1, response.getHits().size());
		assertEquals(1, response.getTotalHits());
		assertEquals("http://identifiers.org/reactome/REACT_12034.3", response.getHits().get(0).getUri());
		
		
		//Next, search in 'pathway' field only, using quoted string
		response = searchEngine.search("pathway:\"signaling by bmp\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());		
//		for(BioPAXElement bpe : response.getHits()) {
//			log.debug(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getUri(),
//					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
//					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
//		}		
		//there is one pathway, and no sub-pathways of it
		assertEquals(1, response.getHits().size());
		assertEquals(1, response.getTotalHits());
		assertEquals("http://identifiers.org/reactome/REACT_12034.3", response.getHits().get(0).getUri());
		
		response = searchEngine.search("pathway:\"bmp receptor signaling\"", 0, Pathway.class, null, null);
		assertNotNull(response);
		assertFalse(response.getHits().isEmpty());		
//		for(BioPAXElement bpe : response.getHits()) {
//			log.debug(String.format("Hit: %s; size: %s; excerpt: %s", bpe.getUri(),
//					bpe.getAnnotations().get(HitAnnotation.HIT_SIZE.name())
//					, bpe.getAnnotations().get(HitAnnotation.HIT_EXCERPT.name())));
//		}	
		assertEquals(2, response.getTotalHits());
		// - the second pathway is a trivial one, member of Pathway_3f75176b9a6272a62f9257f0540dc63b and of many other parent pathways.
		
		//check the top pathway is the main one (parent)
		assertEquals("http://purl.org/pc2/7/Pathway_3f75176b9a6272a62f9257f0540dc63b", response.getHits().get(0).getUri());
	}
}
