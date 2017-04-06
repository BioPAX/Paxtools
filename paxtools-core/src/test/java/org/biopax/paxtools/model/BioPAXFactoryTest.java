package org.biopax.paxtools.model;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by igor on 06/04/17.
 */
public class BioPAXFactoryTest {

    private final BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

    @Test
    public void create() throws Exception {
        SequenceSite ss = factory.create(SequenceSite.class,"ss");
    }

    @Test
    public void canInstantiate() throws Exception {
        assertTrue(factory.canInstantiate(SequenceSite.class));
        assertFalse(factory.canInstantiate(Process.class));
        assertFalse(factory.canInstantiate(EntityReference.class));
        assertFalse(factory.canInstantiate(SimplePhysicalEntity.class));
    }
}