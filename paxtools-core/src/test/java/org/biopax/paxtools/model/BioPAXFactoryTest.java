package org.biopax.paxtools.model;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by igor on 06/04/17.
 */
public class BioPAXFactoryTest {

    private final BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

    @Test
    public void create() throws Exception {
        SequenceSite ss = factory.create(SequenceSite.class,"ss");
        assertNotNull(ss);
    }

    @Test
    public void canInstantiate() {
        assertAll(
            () -> assertTrue(factory.canInstantiate(SequenceSite.class)),
            () -> assertFalse(factory.canInstantiate(Process.class)),
            () -> assertFalse(factory.canInstantiate(EntityReference.class)),
            () -> assertFalse(factory.canInstantiate(SimplePhysicalEntity.class)),
            () -> assertTrue(factory.canInstantiate(Interaction.class)),
            () -> assertTrue(factory.canInstantiate(Control.class)),
            () -> assertFalse(factory.canInstantiate(XReferrable.class)),
            () -> assertFalse(factory.canInstantiate(Observable.class)),
            () -> assertFalse(factory.canInstantiate(Xref.class)),
            () -> assertFalse(factory.canInstantiate(Controller.class)),
            () -> assertFalse(factory.canInstantiate(null))
        );
    }

    @Test
    public void levelStaticUtils() {
        assertEquals(factory.getLevel(), BioPAXLevel.getLevelFromNameSpace("http://www.biopax.org/release/biopax-level3.owl#"));
    }
}