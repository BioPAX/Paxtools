package org.biopax.paxtools.causality.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CBioPortalAccessorTest {
    private static Log log = LogFactory.getLog(CBioPortalAccessorTest.class);

    @Test
    public void testAccessorMethods() throws IOException {
        CBioPortalAccessor cBioPortalAccessor = new CBioPortalAccessor();
        // should have initialized the cancer studies
        assertFalse(cBioPortalAccessor.getCancerStudies().isEmpty());

        if(log.isDebugEnabled()) {
            log.debug("Obtained list of cancer studies:");
            for (CancerStudy cancerStudy : cBioPortalAccessor.getCancerStudies()) {
                log.debug(cancerStudy.getStudyId() + "\t" + cancerStudy.getName());
            }
        }

        CancerStudy cancerStudy = cBioPortalAccessor.getCancerStudies().get(0);

        // Should not throw any exceptions
        cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);

        try {
            cBioPortalAccessor.setCurrentCancerStudy(new CancerStudy("random", "cancer", "study"));
            fail("The class did not throw an exception when trying to use an invalid cancer study.");
        } catch (IllegalArgumentException e) {
            log.info("Caught the exception when trying to set a random cancer study which is not in the official list");
        }

        List<GeneticProfile> geneticProfilesForCurrentStudy = cBioPortalAccessor.getGeneticProfilesForCurrentStudy();

        // If all the genetic profiles are empty, then we don't expect it to be up on the cBio Portal
        // So not a perfect test, but a reasonable one is to check if the list is empty
        assertFalse(geneticProfilesForCurrentStudy.isEmpty());

        if(log.isDebugEnabled()) {
            log.debug("Obtained genetic profiles for " + cBioPortalAccessor.getCurrentCancerStudy().getStudyId());
            for (GeneticProfile geneticProfile : geneticProfilesForCurrentStudy) {
                log.debug(geneticProfile.getId() + "\t" + geneticProfile.getName() + "\t" + geneticProfile.getType());
            }
        }

        List<CaseList> caseLists = cBioPortalAccessor.getCaseListsForCurrentStudy();
        // Same goes for here, this list should not be empty, otherwise it is nonsense to have this cancer study
        assertFalse(caseLists.isEmpty());

        CaseList allCases = cBioPortalAccessor.getAllCasesForCurrentStudy();
        // We also need to have an "ALL" case list for each cancer study
        assertNotNull(allCases);

    }

}
