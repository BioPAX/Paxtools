package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**

 */
public class OrderedFetcherTest {
    @Test
    public void testFetch()
    {
        OrderedFetcher fetcher =
                new OrderedFetcher(true);
        Model model = Mock.model();
        Set<BioPAXElement> fetch = fetcher.fetch(model.getObjects(Pathway.class));
        assertTrue(model.getObjects().size() == fetch.size());
    }
}
