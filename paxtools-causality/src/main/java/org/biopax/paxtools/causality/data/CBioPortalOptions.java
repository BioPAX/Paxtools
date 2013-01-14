package org.biopax.paxtools.causality.data;

import java.util.HashMap;

/**
 *  A wrapper for all available fine tuning options for
 *  @see CBioPortalAccessor .
 *
 *  Threshold defaults are:
 *      CNA: {-1, 1}
 *      EXP: {-2, 2}
 *      RPPA: {-1, 1}
 *      METHYLATION: 0.5
*/
public class CBioPortalOptions extends HashMap<CBioPortalOptions.PORTAL_OPTIONS, Double> {
    public enum PORTAL_OPTIONS {
        EXP_LOWER_THRESHOLD,
        EXP_UPPER_THRESHOLD,
        CNA_LOWER_THRESHOLD,
        CNA_UPPER_THRESHOLD,
        RPPA_LOWER_THRESHOLD,
        RPPA_UPPER_THRESHOLD,
        METHYLATION_THRESHOLD,
    }

    public CBioPortalOptions() {
        this.put(PORTAL_OPTIONS.EXP_LOWER_THRESHOLD, -2.0D);
        this.put(PORTAL_OPTIONS.EXP_UPPER_THRESHOLD, 2.0D);
        this.put(PORTAL_OPTIONS.CNA_LOWER_THRESHOLD, -1.0D);
        this.put(PORTAL_OPTIONS.CNA_UPPER_THRESHOLD, 1.0D);
        this.put(PORTAL_OPTIONS.RPPA_LOWER_THRESHOLD, -1.0D);
        this.put(PORTAL_OPTIONS.RPPA_UPPER_THRESHOLD, 1.0D);
        this.put(PORTAL_OPTIONS.METHYLATION_THRESHOLD, .5D);
    }
}
