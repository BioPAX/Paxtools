package org.biopax.paxtools.causality.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Change;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.conversion.EntrezGene;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author Arman Aksoy
 */

public class CBioPortalAccessor extends AlterationProviderAdaptor {
    private static Log log = LogFactory.getLog(CBioPortalAccessor.class);

    private static String portalURL = "http://www.cbioportal.org/public-portal/webservice.do?";
    protected final static String COMMAND = "cmd=";
    protected final static String DELIMITER = "\t";

    private List<CancerStudy> cancerStudies = new ArrayList<CancerStudy>();
    private CancerStudy currentCancerStudy = null;

    private Map<String, CancerStudy> cancerStudiesById = null;
    private Map<String, GeneticProfile> geneticProfilesById = null;
    private Map<String, CaseList> caseListsById = null;

    private Map<CancerStudy, List<GeneticProfile>> geneticProfilesCache
            = new HashMap<CancerStudy, List<GeneticProfile>>();
    private Map<CancerStudy, List<CaseList>> caseListCache = new HashMap<CancerStudy, List<CaseList>>();
    private CaseList currentCaseList = null;
    private List<GeneticProfile> currentGeneticProfiles = new ArrayList<GeneticProfile>();
    private CBioPortalOptions options;

    public CBioPortalAccessor() throws IOException {
		memory = new HashMap<String, AlterationPack>();
        setOptions(new CBioPortalOptions());
        initializeStudies();
        assert !cancerStudies.isEmpty();
        setCurrentCancerStudy(cancerStudies.get(0));
    }

    private void initializeStudies() throws IOException {
        cancerStudiesById = new HashMap<String, CancerStudy>();

        String urlStr = "getCancerStudies";
        for (String[] result : parseURL(urlStr)) {
            assert result.length == 3;
            CancerStudy cancerStudy = new CancerStudy(result[0], result[1], result[2]);
            cancerStudies.add(cancerStudy);
            cancerStudiesById.put(cancerStudy.getStudyId(), cancerStudy);
        }
    }

    private void setOptions(CBioPortalOptions cBioPortalOptions) {
        this.options = cBioPortalOptions;
    }

    public CBioPortalOptions getOptions() {
        return options;
    }

    public static String getPortalURL() {
        return CBioPortalAccessor.portalURL;
    }

    public static void setPortalURL(String portalURL) {
        CBioPortalAccessor.portalURL = portalURL;
    }

    public GeneticProfile getGeneticProfileById(String id) {
        return geneticProfilesById.get(id);
    }

    public CaseList getCaseListById(String id) {
        return caseListsById.get(id);
    }

    public CancerStudy getCancerStudyById(String id) {
        return cancerStudiesById.get(id);
    }

    private Change[] mergeChanges(Change[] changes1, Change[] changes2) {
        assert changes1.length == changes2.length;

        Change[] consChanges = new Change[changes1.length];

        for(int i=0; i < changes1.length; i++) {
            Change c1 = changes1[i];
            Change c2 = changes2[i];
            Change consensus;

            if(c1.equals(c2))
                consensus = c1;
            else if(c1.equals(Change.NO_DATA))
                consensus = c2;
            else if(c2.equals(Change.NO_DATA))
                consensus = c1;
            else if(c1.equals(Change.NO_CHANGE))
                consensus = c2;
            else if(c2.equals(Change.NO_CHANGE))
                consensus = c1;
            else {
                log.warn("Conflicting values on sample " + i + ": " + c1 + " vs " + c2
                        + ". Accepting the first value.");
                consensus = c1;
            }

            consChanges[i] = consensus;
        }

        return consChanges;
    }

    private Change[] getDataForCurrentStudy(GeneticProfile geneticProfile, String entrezGeneId, CaseList caseList)
            throws IOException {
        Map<String, Change> changesMap = new HashMap<String, Change>();

        String url = "getProfileData&case_set_id=" + caseList.getId() + "&"
                + "genetic_profile_id=" + geneticProfile.getId() + "&"
                + "gene_list=" + entrezGeneId;

        List<String[]> results = parseURL(url, false);
//        assert results.size() > 1; // todo check this assertion
        String[] header = results.get(0);

        for(int i=1; i < results.size(); i++) {
            String[] dataPoints = results.get(i);
            log.debug("Obtained result for "
                    + dataPoints[0] + ":" + dataPoints[1]
                    + " (" + geneticProfile.getId() + ")");

            for(int j=2; j < dataPoints.length; j++) {
                changesMap.put(header[j], inferChange(geneticProfile, dataPoints[j]));
            }
        }

        Change[] changes = new Change[caseList.getCases().length];
        int counter = 0;
        for (String aCase : caseList.getCases()) {
            Change change = changesMap.get(aCase);
            changes[counter++] =  change == null ? Change.NO_DATA : change;
        }

        return changes;
    }

    private Change inferChange(GeneticProfile geneticProfile, String dataPoint) {
        final String NaN = "NaN";
        final String NA = "NA";
        // TODO: Discuss these steps further
        switch (GeneticProfile.GENETIC_PROFILE_TYPE.convertToAlteration(geneticProfile.getType())) {
            case MUTATION:
                return dataPoint.equalsIgnoreCase(NaN) ? Change.NO_CHANGE :
					dataPoint.contains("fs") || dataPoint.contains("splice") ||
						dataPoint.contains(">") || dataPoint.contains("del") ||
						dataPoint.contains("-") || dataPoint.contains("+") ||
						dataPoint.contains("*")? Change.INHIBITING :
					 Change.UNKNOWN_CHANGE;
            case METHYLATION:
                Double methylationThreshold = options.get(CBioPortalOptions.PORTAL_OPTIONS.METHYLATION_THRESHOLD);
                return dataPoint.equalsIgnoreCase(NaN) || dataPoint.equalsIgnoreCase(NA)
                        ? Change.NO_DATA
                        : (Double.parseDouble(dataPoint) > methylationThreshold ? Change.INHIBITING : Change.NO_CHANGE);
            case COPY_NUMBER:
                if(dataPoint.equalsIgnoreCase(NA) || dataPoint.equalsIgnoreCase(NaN) || dataPoint.isEmpty()) {
                    return Change.NO_DATA;
                } else {
                    Double value = Double.parseDouble(dataPoint);
                    if(value < options.get(CBioPortalOptions.PORTAL_OPTIONS.CNA_LOWER_THRESHOLD))
                        return Change.INHIBITING;
                    else if(value > options.get(CBioPortalOptions.PORTAL_OPTIONS.CNA_UPPER_THRESHOLD))
                        return Change.ACTIVATING;
                    else
                        return Change.NO_CHANGE;
                }
            case EXPRESSION:
                if(dataPoint.equalsIgnoreCase(NaN) || dataPoint.equalsIgnoreCase(NA) || dataPoint.isEmpty()) {
                    return Change.NO_DATA;
                } else {
                    Double value = Double.parseDouble(dataPoint);

                    if(value > options.get(CBioPortalOptions.PORTAL_OPTIONS.EXP_UPPER_THRESHOLD))
                        return Change.ACTIVATING;
                    else if(value < options.get(CBioPortalOptions.PORTAL_OPTIONS.EXP_LOWER_THRESHOLD))
                        return Change.INHIBITING;
                    else
                        return Change.NO_CHANGE;
                }
            case PROTEIN_LEVEL:
                if(dataPoint.equalsIgnoreCase(NaN)) {
                    return Change.NO_DATA;
                } else {
                    Double value = Double.parseDouble(dataPoint);

                    if(value > options.get(CBioPortalOptions.PORTAL_OPTIONS.RPPA_UPPER_THRESHOLD))
                        return Change.ACTIVATING;
                    else if(value < options.get(CBioPortalOptions.PORTAL_OPTIONS.RPPA_LOWER_THRESHOLD))
                        return Change.INHIBITING;
                    else
                        return Change.NO_CHANGE;
                }
            // TODO: How to analyze?
            case NON_GENOMIC:
            case ANY:
        }

        return Change.NO_CHANGE;
    }

    @Override
    public AlterationPack getAlterations(Node node) {
		String symbol = getGeneSymbol(node);
		if (symbol != null) {
			return getAlterations(symbol);
		}

		return null;
	}
	
    public AlterationPack getAlterations(String symbol) {
		// Use cached value if there is any
        AlterationPack alterationPack = getFromMemory(symbol);
		if (alterationPack != null) return alterationPack;

		alterationPack = new AlterationPack(symbol);

        // A few sanity checks
        CancerStudy cancerStudy = currentCancerStudy;
        if(!getCancerStudies().contains(cancerStudy)) {
            String message = "Current cancer study is not valid: "
                    + (cancerStudy == null ? "null" : cancerStudy.getName());
            log.error(message);
            throw new IllegalArgumentException(message);
        } else try {
            if(!getCaseListsForCurrentStudy().contains(getCurrentCaseList())) {
                CaseList caseList = getCurrentCaseList();
                String message = "Current case list is not valid :"
                        + (caseList == null ? "null" : caseList.getDescription());
                log.error(message);
                throw new IllegalArgumentException(message);
            } else if(getCurrentGeneticProfiles().isEmpty()) {
                log.warn("Current genetic profiles do not have any elements in it!");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // Now to the genetic profile analyses
        for (GeneticProfile geneticProfile : currentGeneticProfiles) {
            if(!getCurrentGeneticProfiles().contains(geneticProfile)) {
              log.warn("the genetic profile "
                      + geneticProfile.getId() + " is not in the available profiles list. Skipping.");
              continue;
            }

            Change[] changes;
            try {
              changes = getDataForCurrentStudy(geneticProfile, symbol, currentCaseList);
            } catch (IOException e) {
              log.error("Could not get data for genetic profile " + geneticProfile.getId()
                      + ". Skipping...");
              continue;
            }

            Alteration alteration = GeneticProfile.GENETIC_PROFILE_TYPE.convertToAlteration(geneticProfile.getType());
            if (alteration == null) {
                System.err.println("Unsupported alteration = " + geneticProfile.getType());
            }

            Change[] altChanges = alterationPack.get(alteration);
            if(altChanges == null)
               alterationPack.put(alteration, changes);
            else
                alterationPack.put(alteration, mergeChanges(altChanges, changes));
        }

		memorize(symbol, alterationPack);
		alterationPack.complete();
        return alterationPack;
    }

    public List<CaseList> getCaseListsForCurrentStudy() throws IOException {
        caseListsById = new HashMap<String, CaseList>();
        List<CaseList> caseLists = caseListCache.get(getCurrentCancerStudy());
        if(caseLists != null) {
            for (CaseList caseList : caseLists)
                caseListsById.put(caseList.getId(), caseList);
            return caseLists;
        }

        caseLists = new ArrayList<CaseList>();
        String url = "getCaseLists&cancer_study_id=" + getCurrentCancerStudy().getStudyId();
        for (String[] results : parseURL(url)) {
            assert results.length == 5;
            String[] cases = results[4].split(" ");
            assert cases.length > 0;

            CaseList caseList = new CaseList(results[0], results[1], cases);
            caseLists.add(caseList);
            caseListsById.put(caseList.getId(), caseList);
        }

        caseListCache.put(getCurrentCancerStudy(), caseLists);
        return caseLists;
    }

    private List<String[]> parseURL(String urlPostFix) throws IOException {
        return parseURL(urlPostFix, true);
    }

    private List<String[]> parseURL(String urlPostFix, boolean skipHeader) throws IOException {
        List<String[]> list = new ArrayList<String[]>();

        String urlStr = portalURL + COMMAND + urlPostFix;
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();
        Scanner scanner = new Scanner(urlConnection.getInputStream());

        int lineNum = 0;
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineNum++;

            if(line.startsWith("#") || line.length() == 0 || (skipHeader && lineNum == 2))
                continue;

            list.add(line.split(DELIMITER));
        }

        return list;
    }

    public List<CancerStudy> getCancerStudies() {
        return cancerStudies;
    }

    public CancerStudy getCurrentCancerStudy() {
        return currentCancerStudy;
    }

    public void setCurrentCancerStudy(CancerStudy currentCancerStudy) {
        if(!cancerStudies.contains(currentCancerStudy))
            throw new IllegalArgumentException("This cancer study is not available through the initialized list.");

        this.currentCancerStudy = currentCancerStudy;
        setCurrentCaseList(null);
        if(caseListsById != null)
            caseListsById.clear();
        getCurrentGeneticProfiles().clear();
        if(geneticProfilesById != null)
            geneticProfilesById.clear();

        memory.clear();

        // Try to load the associated genetic profiles and case lists for fast loading.
        try {
            getCaseListsForCurrentStudy();
            getGeneticProfilesForCurrentStudy();
        } catch (IOException e) {
            log.warn("Could not buffer case lists/genetic profiles for the current study:"
                    + currentCancerStudy.getStudyId());
        }
    }

    public List<GeneticProfile> getGeneticProfilesForCurrentStudy() throws IOException {
        geneticProfilesById = new HashMap<String, GeneticProfile>();
        List<GeneticProfile> geneticProfiles = geneticProfilesCache.get(getCurrentCancerStudy());
        if(geneticProfiles != null) {
            for (GeneticProfile geneticProfile : geneticProfiles)
                geneticProfilesById.put(geneticProfile.getId(), geneticProfile);
            return geneticProfiles;
        }

        geneticProfiles = new ArrayList<GeneticProfile>();

        String url = "getGeneticProfiles" + "&cancer_study_id=" + getCurrentCancerStudy().getStudyId();
        for (String[] results : parseURL(url)) {
            assert results.length == 6;
            GeneticProfile geneticProfile = new GeneticProfile(results[0], results[1], results[2], results[4]);
            geneticProfiles.add(geneticProfile);
            geneticProfilesById.put(geneticProfile.getId(), geneticProfile);
        }

        assert !geneticProfiles.isEmpty();
        geneticProfilesCache.put(getCurrentCancerStudy(), geneticProfiles);
        return geneticProfiles;
    }

    public void setCurrentCaseList(CaseList caseList) {
        try {
            List<CaseList> caseListsForCurrentStudy = getCaseListsForCurrentStudy();
            if(caseList == null || caseListsForCurrentStudy.contains(caseList)) {
                currentCaseList = caseList;
            } else {
                throw new IllegalArgumentException("The case list is not available for current cancer study.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot obtain the case lists for the current study.");
        }
    }

    public CaseList getCurrentCaseList() {
        return currentCaseList;
    }

    public void setCurrentGeneticProfiles(List<GeneticProfile> geneticProfiles) {
        currentGeneticProfiles = geneticProfiles;
        memory.clear();
    }

    public List<GeneticProfile> getCurrentGeneticProfiles() {
        return currentGeneticProfiles;
    }
	
	protected String getGeneSymbol(Node node)
	{
		String egid = getEntrezGeneID(node);
		if (egid != null) return EntrezGene.getSymbol(egid);
		return null;
	}

    public void clearAlterationCache() {
        memory.clear();
    }
}
