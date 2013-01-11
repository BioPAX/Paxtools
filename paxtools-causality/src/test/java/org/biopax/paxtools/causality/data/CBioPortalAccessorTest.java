package org.biopax.paxtools.causality.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@Ignore // This test depends on cBio Portal, so it shouldn't be part of the routine tests.
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

        CancerStudy cancerStudy = cBioPortalAccessor.getCancerStudies().get(16);

        // Should not throw any exceptions
        cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);

        // Should throw an exception since it is a random study
        try {
            cBioPortalAccessor.setCurrentCancerStudy(new CancerStudy("random", "cancer", "study"));
            fail("The class did not throw an exception when trying to use an invalid cancer study.");
        } catch (IllegalArgumentException e) {
            log.debug("Caught the exception when trying to set a random cancer study which is not in the official list");
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

        // Use all genetic profiles
        cBioPortalAccessor.setCurrentGeneticProfiles(geneticProfilesForCurrentStudy);

        List<CaseList> caseLists = cBioPortalAccessor.getCaseListsForCurrentStudy();
        // Same goes for here, this list should not be empty, otherwise it is nonsense to have this cancer study
        assertFalse(caseLists.isEmpty());
        cBioPortalAccessor.setCurrentCaseList(caseLists.iterator().next());
        int numOfCases = cBioPortalAccessor.getCurrentCaseList().getCases().length;
        assertTrue(numOfCases > 0);

		AlterationPack alt = cBioPortalAccessor.getAlterations("7157"); // a.k.a. TP53
		alt.complete();
        int altSize = alt.getSize();
        assertEquals(altSize, numOfCases);
        log.debug("alt.size = " + altSize);
        log.debug("numOfCases = " + numOfCases);

        // Now the following should fail since we change the current cancer study,
        // but do not reset the case list and genetic profiles
        cBioPortalAccessor.setCurrentCancerStudy(cBioPortalAccessor.getCancerStudies().get(10));
        try {
            cBioPortalAccessor.getAlterations("7157");
            fail("Did not fail although the case list and genetic profiles are not assigned.");
        } catch (IllegalArgumentException e) {
            log.debug("Failed due to missing case list assignment.");
        }

	}
}
