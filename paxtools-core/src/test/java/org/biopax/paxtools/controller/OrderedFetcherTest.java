package org.biopax.paxtools.controller;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**

 */
public class OrderedFetcherTest {
    @Test
    public void testFetch()
    {
        OrderedFetcher fetcher = new OrderedFetcher(true);
        
        String s = "L3" + File.separator + "biopax3-short-metabolic-pathway.owl";
        System.out.println(s);
        InputStream in = getClass().getClassLoader().getResourceAsStream(s);
        Model model = new SimpleIOHandler().convertFromOWL(in);
        
        Set<BioPAXElement> fetch = fetcher.fetch(model.getObjects(Pathway.class));
        assertTrue(model.getObjects().size() == fetch.size());
    }
}
