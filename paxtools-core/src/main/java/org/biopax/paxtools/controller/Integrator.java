package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.*;


/**
 *
 * This class is intended to merge and to integrate biopax models
 * not necessarily from the same resource - if models allow such a
 * thing. This class has very similar functionality to the controller.Merger
 * but it differs in means of merging/integrating methodology.
 *
 * Integrator iterates all the conversions in from the <em>target</em> and
 * <em>source</em> model(s), and assigns scores indicating their similarity.
 * After the scoring process is completed, it then starts integrating conversions
 * having the highest score until it reaches the <em>threshold</em> value. After
 * this conversion based integration is accomplished, all the models are merged
 * into the <em>target</em>.
 *
 * Please note that this class is in its beta state.
 */
public class Integrator {

    private static final Log log = LogFactory.getLog(Integrator.class);
    private EditorMap editorMap;
    private Merger merger;
    private Model target, mergedSources = null;

    private boolean onlyMapping = false;
    private boolean selfRemove  = false;
    private boolean normalizeModels = false;

    /**
     *  This is the main score matrix
     *
     *      |   D   |   E   |   F   |
     * ------------------------------
     * A    |       |       |       |
     * ------------------------------
     * B    |       |       |       |
     * ------------------------------
     * C    |       |       |       |
     * ------------------------------
     */
    private Map<physicalEntityParticipant,
                Map<physicalEntityParticipant, Double>> pepScoreMatrix
                    = new HashMap<physicalEntityParticipant,
                        Map<physicalEntityParticipant, Double>>();

    /**
     * This is the pool where the scores and relevant conversions
     * will be stored. Other than this global one, there will be
     * a local copy to enable the user handle different threshold
     * values one at a time.
     */
    private List<ConversionScore> similarConversions;

    private final String[][] dbChanges
            =  {
                {"Chemical Entities of Biological Interest", "ChEBI"}
               };

    private Set<Set<String>> relatedTerms = new HashSet<Set<String>>();
    private String[][] termLists =
            {
                    {"active", "active1", "active2", "phosphorylation", "phosphate group", "phosphorylation site"},
                    {"inactive", "phosphorylation", "phosphate group", "phosphorylation site"}

            };

    private String[][] locLists =
                {
                        {"cytoplasm", "cytosol"}
                };

    /* Globalling tricks & fine-tuning */
    private final double SIZE_MISMATCH_PENALTY = 0.7;
    private final double BASE_SCORE = 0.4;
    private double SCORES_OVER = 100.0;
    private final double MAX_PEP_SCORE = 3.5;

    private final double STATS_OVER = 1000.0; // For info messages like "2/100 completed"

    private double threshold = SCORES_OVER; // Max. threshold


    /**
     *
     * @param editorMap map to be used in order to initialize merger
     * @param target target model into which integration will be done
     * @param sources targets that are going to be integrated into target
     *
     * @see org.biopax.paxtools.controller.Merger
     */
    public Integrator(EditorMap editorMap, Model target, Model... sources) {
        this.editorMap = editorMap;
        this.merger = new Merger(editorMap);
        this.target = target;

        log.info(sources.length + " source model(s) will be merged.");
        // Merge all "sources" into one single model
        for(Model source : sources) {
            if( mergedSources == null )
                mergedSources = source;
            else
                merger.merge(mergedSources, source);
        }
        log.info("Merging finished.");

        if( isNormalizeModels() ) {
            log.info("Normalizing models.");

            log.info("Normaling XREFs.");
            normalizeXrefs(target);
            normalizeXrefs(mergedSources);
            log.info("Normaling OCVs.");
            normalizeOpenControlledVocabulary(mergedSources);
            log.info("Normaling cellular locations.");
            normalizeCellularLocations(mergedSources);

            log.info("Normalization completed.");
        }
    }

    /**
     * Sets the threshold value (the smallest score for integrating
     * two conversions)
     *
     * @param threshold value
     *
     * @see #setScoresOver(double)
     */
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the threshold value (the smallest score for integrating
     * two conversions)
     *
     * @return a double value (default: 100.0)
     */
    public Double getThreshold() {
        return threshold;
    }

    /**
     * Enables/disables integration. If <em>only mapping</em> feature is
     * set to true, integrator will only assign scores to conversion and
     * exits. This option may help to build interactive programs.
     *
     * @param mapping true for skipping integration
     *
     * @see #integrate()
     */
    public void setOnlyMapping(boolean mapping) {
        this.onlyMapping = mapping;
    }

    /**
     *
     * @return true for enabled "only mapping", false otherwise
     *
     * @see #setOnlyMapping(boolean)
     */
    boolean isOnlyMapping() {
        return onlyMapping;
    }

    /**
     * Enables removal of elements from the <em>target</em> if they are contained
     * both in source and target, and have a match with another conversion. Useful for
     * integrating of a model by itself. Default is false.
     *
     * @param selfRemove true for enabling removal, false otherwise
     */
    public void setSelfRemove(boolean selfRemove) {
        this.selfRemove = selfRemove;
    }

    /**
     *
     * @return true for enabled removal, false otherwise
     *
     * @see #setSelfRemove(boolean)
     */
    boolean isSelfRemove() {
        return selfRemove;
    }

    /**
     * Fixes some of the known Open Controlled Vocabullary issues in the models.
     * It is best to try integration with this option enabled (true) and
     * disabled (false) to see which gives a better result. Default is false.
     *
     * @param normalizeModels true for normalization of OCVs
     */
    public void setNormalizeModels(boolean normalizeModels) {
        this.normalizeModels = normalizeModels;
    }

    /**
     *
     * @return true for normalization of OCVs, false otherwise (default)
     *
     * @see #setNormalizeModels(boolean)
     */
    boolean isNormalizeModels() {
        return normalizeModels;
    }


    /**
     * @see #setScoresOver(double)
     *
     * @return a double indicating maximum score
     */
    double getScoresOver() {
        return SCORES_OVER;
    }

    /**
     * A score between two conversions is in the interval (0, 1].
     * Setting a <em>scoresOver</em> value will the map this range to
     * (0, scoresOver]. Default value is 100.0, so the default score
     * range is (0,100]. This setting does not alter the integration
     * process. It only multiplies the scores with the given value.
     *
     * @param scoresOver a double score
     */
    public void setScoresOver(double scoresOver) {
        this.SCORES_OVER = scoresOver;
    }

    /**
         * Integrates <em>target</em> and <em>source</em>(s) and returns a
         * sorted (desc) list of conversion scores.
         *
         * @see #setNormalizeModels(boolean)
         * @see #setOnlyMapping(boolean)
         * @see #setScoresOver(double)
         * @see #setSelfRemove(boolean)
         * @see #setThreshold(Double)
         *
         * @return a sorted list of ConversionScores
         */
    public List<ConversionScore> integrate() {
        return integrate(null);
    }

    /**
     * Does the integration using user-provided scores list.
     *
     * @see #integrate()
     *
     * @param alternativeScores alternative scores, can be null
     * @return a sorted list of ConversionScores
     */
   public List<ConversionScore> integrate(List<ConversionScore> alternativeScores) {
        Map<physicalEntityParticipant, Map<physicalEntityParticipant, Double>>
                    pepScoreMatrix = this.pepScoreMatrix;
        List<ConversionScore> similarConversions;
        // There is something wrong with the sources, just quit
        if(mergedSources == null) {
            log.warn("Either target or source is empty, skipping integration.");
            return null;
        }

        log.info("Scoring all the PEPs.");
        /* If it is a first run, we need to calculate all scores,
         * but if it is not, we can save some CPU time.
         */
        if( pepScoreMatrix.isEmpty() ) { // first run
            createPEPScoreMatrix(target.getObjects(physicalEntityParticipant.class),
                                     mergedSources.getObjects(physicalEntityParticipant.class));

            log.info("Scoring PEPs finished.");

            log.info("Scoring conversions");
            this.similarConversions = createConversionScoreMap(pepScoreMatrix,
                                                            target.getObjects(conversion.class),
                                                            mergedSources.getObjects(conversion.class));
            log.info("Scoring conversions finished.");
        }

        if(this.similarConversions == null)
                this.similarConversions = new ArrayList<ConversionScore>();

        /* Original score matrixes won't be modified for a later use
         * Instead, we are going to copy them, and modify their copies.
         */
        log.info("Creating a copy of the PEP scores.");
        Map<physicalEntityParticipant,
            Map<physicalEntityParticipant, Double>> copyMatrix
                            = new HashMap<physicalEntityParticipant, Map<physicalEntityParticipant, Double>>();
            // Copy the contents of the matrix
        for(physicalEntityParticipant pepKey: pepScoreMatrix.keySet()) {
            copyMatrix.put(pepKey,
                    new HashMap<physicalEntityParticipant, Double>(pepScoreMatrix.get(pepKey)));
        }
        // We want to use the copy now
        pepScoreMatrix = copyMatrix;
        log.info("PEP scores copied.");

        similarConversions = (alternativeScores == null)
                                ? new ArrayList<ConversionScore>(this.similarConversions)
                                : alternativeScores;

        log.info("Conversion scores copied.");
        /* End of copies */

        log.info("Mapping conversions/PEPs with a threshold: " + getThreshold());
        mapConversions(similarConversions, pepScoreMatrix);
        log.info("Mapping finished.");

        // Sorting is essential for #equalizeEntities. If you are to
        // modify this sort, check there also!
        log.info("Sorting scores (" + similarConversions.size() + " scores).");
        Collections.sort(similarConversions);
        Collections.reverse(similarConversions);
        log.info("Sorting finished.");

        if( isOnlyMapping() ) {
            log.info("Skipping model integration.");
        } else {
            log.info("Entities of similar conversions are being eqalized.");
            equalizeEntities(similarConversions);

            log.info("Merging integrated models.");
            merger.merge(target, mergedSources);
            log.info("Merging finished.");
        }

        log.info("Integration completed.");
        return similarConversions;
    }

    private void equalizeEntities(List<ConversionScore> similarConversions) {
        Set<conversion> doNotModifySet = new HashSet<conversion>();
        Set<ConversionScore> containsSelfRemoved = new HashSet<ConversionScore>();

        for(ConversionScore convScore: similarConversions) {
            // Since we sorted the list, we are safe to break
            // But a continue will also do the trick, mostly
            // requiring little more time
            if( convScore.getScore() < getThreshold() )
                break;

            conversion conv1 = convScore.getConversion1(),
                       conv2 = convScore.getConversion2();
            // If they are already the same, pass
            if( conv1.getRDFId().equals(conv2.getRDFId()) )
                continue;

            // Do not modify it twice
            if( doNotModifySet.contains(conv2) ) {
                log.info(conv2.getRDFId() + " has already been modified. Skipped.");
                continue;
            }

            // Self remove operations
            if( isSelfRemove() ) {
                // Remove "conv2" from target, if the corresponding flag is set true
                BioPAXElement eqBPE = target.getByID(conv2.getRDFId());
                if( eqBPE != null ) {
                    target.remove( eqBPE );
                    log.info("Self removing: " + eqBPE.getRDFId());

                    // Collect other matches of will-be-removed element.
                    for(ConversionScore tempCS: similarConversions) {
                        if( tempCS.getConversion1().equals(eqBPE) )
                            containsSelfRemoved.add(tempCS);
                    }
                } else if( containsSelfRemoved.contains(convScore) )
                        continue;
            }

            // Three things to make equal: conversions themselves, matched PEPs, their controls
            equalize(conv1, conv2);
            if( convScore.isReverseMatch() )
                changeDirection(conv2);

            for(physicalEntityParticipant pep1: convScore.getMatchedPEPs() ) {
                physicalEntityParticipant pep2 = convScore.getMatch(pep1);
                // We got the match, now set lets build sets of PEPs of equal states
                equalizePEP(pep1, pep2);
            } // End of score maximazing

            for( control control1: conv1.isCONTROLLEDOf() ) {
                for( control control2: conv2.isCONTROLLEDOf() ) {
                    boolean allSimilar = true;
                    for(physicalEntityParticipant controller1: control1.getCONTROLLER() ) {
                        for(physicalEntityParticipant controller2: control2.getCONTROLLER() )  {
                            if( getScore(controller1, controller2) > BASE_SCORE ) {
                                equalizePEP(controller1, controller2);
                            } else {
                                allSimilar = false;
                            }
                        }
                    }
                    if( allSimilar // size 0 causes false equivalance, thus regard that case
                            && !(control1.getCONTROLLER().size() == 0 ^ control2.getCONTROLLER().size() == 0))
                    {
                        equalize(control1, control2);

                        if( convScore.isReverseMatch() && control2 instanceof catalysis)
                            changeDirection((catalysis) control2);
                    }
                }
            }

            // We are done with conv2
            doNotModifySet.add(conv2);
        }
    }

    /**
     * @deprecated setRDFId is not available anymore!
     */
    private void equalize(BioPAXElement e1, BioPAXElement e2) {
        // Operation below is enough for the time being
    	// TODO re-factoring: setRDFId is not available anymore! (We don't really want to change rdfIDs, do we?..)
        //e2.setRDFId(e1.getRDFId());

    	throw new UnsupportedOperationException("This needs re-factoring: bpe.setRDFId is not available anymore!");

    	//TODO ? use some alternative way to store that a1 equals e2, e.g., Set<String> matched,
    	//matched.add(e1.getRDFId()+e2.getRDFId()); matched.add(e2.getRDFId()+e1.getRDFId());
    }

    private boolean equals(BioPAXElement a, BioPAXElement b) {
    	throw new UnsupportedOperationException("not implemented yet.");
    	// TODO ? implement equals(BioPAXElement a, BioPAXElement b): can be smth. like the following... and use below
    	//return (a == null) ? b == null : a.equals(b) || matched.contains(a.getRDFId()+b.getRDFId());
    }

    private void equalizePEP(physicalEntityParticipant controller1, physicalEntityParticipant controller2) {
        // There is a special case for PEPs: we also need to update equivalent PEPs' fields
        Set<physicalEntityParticipant> tempEqvPeps = new HashSet<physicalEntityParticipant>();
        tempEqvPeps.addAll(getEquivalentsOfPEP(controller1));
        tempEqvPeps.addAll(getEquivalentsOfPEP(controller2));
        for(physicalEntityParticipant eqPep : tempEqvPeps)
            updatePepFields(eqPep, controller2);

        for(physicalEntityParticipant eqPep : tempEqvPeps)
            updatePepFields(controller2, eqPep);

        equalize(controller1, controller2);
    }

    private Set<physicalEntityParticipant> getEquivalentsOfPEP(physicalEntityParticipant onePep) {
        Set<physicalEntityParticipant> eqGrp = new HashSet<physicalEntityParticipant>();
        for(physicalEntityParticipant aPep : onePep.getPHYSICAL_ENTITY().isPHYSICAL_ENTITYof() ) {
            if(aPep.isInEquivalentState(onePep))
                eqGrp.add(aPep);
        }

        return eqGrp;
    }

    private void changeDirection(conversion conv) {
        SpontaneousType st = conv.getSPONTANEOUS();

        /* One possibility is below, but no need to operate
        if( st == ConversionDirectionType.NOT_SPONTANEOUS || st == null )
            return;
        */
        if( st == SpontaneousType.L_R )
            conv.setSPONTANEOUS(SpontaneousType.R_L);
        else if( st == SpontaneousType.R_L )
            conv.setSPONTANEOUS(SpontaneousType.L_R);
    }

    private void changeDirection(catalysis cat) {
        Direction ct = cat.getDIRECTION();

        if( ct == Direction.IRREVERSIBLE_LEFT_TO_RIGHT )
            cat.setDIRECTION(Direction.IRREVERSIBLE_RIGHT_TO_LEFT);
        else if( ct == Direction.IRREVERSIBLE_RIGHT_TO_LEFT )
            cat.setDIRECTION(Direction.IRREVERSIBLE_LEFT_TO_RIGHT);
        else if( ct == Direction.PHYSIOL_LEFT_TO_RIGHT )
            cat.setDIRECTION(Direction.PHYSIOL_RIGHT_TO_LEFT);
        else if( ct == Direction.PHYSIOL_RIGHT_TO_LEFT)
            cat.setDIRECTION(Direction.PHYSIOL_LEFT_TO_RIGHT);

        /* One possibility is below, but no need to operate
        else if( ct == CatalysisDirection.REVERSIBLE)
            return;
        */
    }

    private void mapConversions(Collection<ConversionScore> similarConversions,
                                Map<physicalEntityParticipant,
                                        Map<physicalEntityParticipant, Double>> pepScoreMatrix) {
        // To get rid of Concurrent modification :|
        Set<ConversionScore> toBeUpdated = new HashSet<ConversionScore>();

        for(ConversionScore convScore : similarConversions) {
            // Check if the score is equal to or higher than the threshold
            if( convScore.getScore() < getThreshold() )
                continue;

            // Get matches of PEPs of first conversion
            for(physicalEntityParticipant pep1: convScore.getMatchedPEPs() ) {
                physicalEntityParticipant pep2 = convScore.getMatch(pep1);

                // We got the match, now set their score to max
                pepScoreMatrix.get(pep1).put(pep2, MAX_PEP_SCORE);
            } // End of score maximazing

            // Remember this
            toBeUpdated.add(convScore);
        }

        // Now we know which scores are affected, lets replace them
        for(ConversionScore convScore: toBeUpdated) {
            // Remove it from similarConversion
            similarConversions.remove(convScore);

            // Add new score
            similarConversions.add( getScore(pepScoreMatrix,
                                        convScore.getConversion1(),
                                        convScore.getConversion2()) );
        }

    }

    private List<ConversionScore> createConversionScoreMap(Map<physicalEntityParticipant,
                                                Map<physicalEntityParticipant, Double>> pepScoreMatrix,
                                                           Set<conversion> convSet1, Set<conversion> convSet2) {
        List<ConversionScore> similarConversions = new ArrayList<ConversionScore>();

        double totalSize = convSet1.size() * convSet2.size();
        double convCnt = 0;

        for(conversion conv1: convSet1) {
            for(conversion conv2: convSet2) {
                // No need to compare conversions of different types
                if( !((conv1 instanceof biochemicalReaction && conv2 instanceof biochemicalReaction)
                   || (conv1 instanceof complexAssembly && conv2 instanceof complexAssembly)
                   || (conv1 instanceof transport && conv2 instanceof transport)) )
                {
                    convCnt++;
                    continue;
                }

                if( conv1.getRDFId().equals(conv2.getRDFId())) { // If they are the same
                    convCnt++;
                    continue;
                }

                ConversionScore convScore = getScore(pepScoreMatrix, conv1, conv2);
                similarConversions.add(convScore);

                if( convCnt % Math.ceil(totalSize/STATS_OVER) == 0 ) {
                    log.info( " - " + (convCnt / Math.ceil(totalSize/STATS_OVER))
                                    + "/" + STATS_OVER + " completed.");
                }

                convCnt++;

            }
        }

        return similarConversions;
    }

    private void createPEPScoreMatrix(Collection<physicalEntityParticipant> pepSet1,
                                      Collection<physicalEntityParticipant> pepSet2) {

        // If it is not empty, no need to calculate it again
        assert pepScoreMatrix.isEmpty();
        double totalSize = pepSet1.size() * pepSet2.size();

        double pepCnt = 0;
        for(physicalEntityParticipant pep1 : pepSet1) {
            // Create a new row for a PEP
            Map<physicalEntityParticipant, Double> pep1Row
                = new HashMap<physicalEntityParticipant, Double>();
            pepScoreMatrix.put(pep1, pep1Row);

            // Fill the row with the corresponding scores
            for(physicalEntityParticipant pep2 : pepSet2) {
                if( complexScoreHelper(pep1.getPHYSICAL_ENTITY(),
                        pep2.getPHYSICAL_ENTITY()) ) {
                    Double score = getScore(pep1, pep2);
                    pep1Row.put(pep2, score);
                }

                if( pepCnt % Math.ceil(totalSize/STATS_OVER) == 0 ) {
                    log.info( " - " + (pepCnt / Math.ceil(totalSize/STATS_OVER))
                                    + "/" + STATS_OVER + " completed.");
                }

                pepCnt++;
            }
        }

    }

    private boolean complexScoreHelper(physicalEntity cPe, physicalEntity pe) {
        if(cPe instanceof complex && pe instanceof complex) {
            for(physicalEntityParticipant tmpPep : ((complex) cPe) .getCOMPONENTS() ) {
                if( !complexScoreHelper(pe, tmpPep.getPHYSICAL_ENTITY()) )
                    return false;
            }
            return true;
        } else if( cPe instanceof complex ) {
            for(physicalEntityParticipant tmpPep : ((complex) cPe) .getCOMPONENTS() ) {
                if( complexScoreHelper(tmpPep.getPHYSICAL_ENTITY(), pe) )
                    return true;
            }
            return false;
        } else {
            return cPe.equals(pe);
        }
    }

    private Double getScore(physicalEntityParticipant pep1,
                                physicalEntityParticipant pep2) {
        double totalScore = .0;

        if((pep1 instanceof sequenceParticipant ^ pep2 instanceof sequenceParticipant)
            && !(pep1.getPHYSICAL_ENTITY() instanceof smallMolecule
                    && pep2.getPHYSICAL_ENTITY() instanceof smallMolecule) )
            return BASE_SCORE;

        if( pep1.getPHYSICAL_ENTITY().equals(pep2.getPHYSICAL_ENTITY()) )
            totalScore += 2.5;
        else if( complexScoreHelper(pep1.getPHYSICAL_ENTITY(), pep2.getPHYSICAL_ENTITY())
              && complexScoreHelper(pep2.getPHYSICAL_ENTITY(), pep1.getPHYSICAL_ENTITY()) )
            totalScore += 2.35;
        else if( complexScoreHelper(pep1.getPHYSICAL_ENTITY(), pep2.getPHYSICAL_ENTITY())
              || complexScoreHelper(pep2.getPHYSICAL_ENTITY(), pep1.getPHYSICAL_ENTITY()) )
            totalScore += 2;
        else
            return BASE_SCORE;

        if( pep1.isInEquivalentState(pep2) )
            totalScore += 1;
        else {
             if( isSeqParTermsSimilar(pep1, pep2) )
                totalScore += .8;
             else if( isCellularLocsSimilar(pep1, pep2) )
                totalScore += .8;
        }

        return totalScore;
    }

    private boolean isCellularLocsTermsSimilar(Set<String> fTerms, Set<String> sTerms) {
        for( String[] locList : locLists )
            for( String fterm : fTerms )
                for( String sterm : sTerms )
                    if( Arrays.asList(locList).contains(fterm) && Arrays.asList(locList).contains(sterm))
                        return true;

        return false;
    }

    private boolean isCellularLocsSimilar(physicalEntityParticipant fPep,
                                          physicalEntityParticipant sPep) {
        return !(fPep.getCELLULAR_LOCATION() != null && sPep.getCELLULAR_LOCATION() != null)
               || isCellularLocsTermsSimilar(fPep.getCELLULAR_LOCATION().getTERM(),
                                             sPep.getCELLULAR_LOCATION().getTERM());
    }

    private boolean isSeqParTermsSimilar(physicalEntityParticipant fPep,
                                            physicalEntityParticipant sPep) {
        if( relatedTerms.isEmpty() ) {
            for( String[] termL : termLists )  {
                Set<String> termSet = new HashSet<String>();
                termSet.addAll(Arrays.asList(termL));
                relatedTerms.add(termSet);
            }
        }

        if( fPep instanceof sequenceParticipant
                && sPep instanceof sequenceParticipant ) {
            for( sequenceFeature fsf : ((sequenceParticipant) fPep).getSEQUENCE_FEATURE_LIST() )
                for( sequenceFeature ssf : ((sequenceParticipant) sPep).getSEQUENCE_FEATURE_LIST() )
                    for(Set<String> similarTerm : relatedTerms)
                        if( fsf.getFEATURE_TYPE() != null && ssf.getFEATURE_TYPE() != null)
                            for( String fterm : fsf.getFEATURE_TYPE().getTERM() )
                                for( String sterm : ssf.getFEATURE_TYPE().getTERM() )
                                    if( similarTerm.contains(fterm) && similarTerm.contains(sterm))
                                        return true;
        }

        return false;
    }

    private PEPScore getScore(Map<physicalEntityParticipant,
                                                Map<physicalEntityParticipant, Double>> pepScoreMatrix,
                              Set<physicalEntityParticipant> PEPs1, Set<physicalEntityParticipant> PEPs2) {
        Double finalScore = 1.0;

        // This is the 1-to-1 mapping of the PEPs
        // PEPs1 -> PEPs2
        Map<physicalEntityParticipant, physicalEntityParticipant> pepMap
                = new HashMap<physicalEntityParticipant, physicalEntityParticipant>();

        /*
         * If the second set is smaller than the first one,
         * then because of the scoring algorithm, the matix
         * should be used transposed.
         */
        boolean transposeMatrix;
        Set<physicalEntityParticipant> firstSet, secondSet;
        int minSize, sizeDiff;

        if( PEPs2.size() > PEPs1.size() ) {
            transposeMatrix = false;
            firstSet = PEPs1;
            secondSet = PEPs2;
        } else {
            transposeMatrix = true;
            firstSet = PEPs2;
            secondSet = PEPs1;
        }

        sizeDiff = secondSet.size() - firstSet.size();
        // Extra penalty for one-side-conversions (e.g. ubiquination)
        minSize = firstSet.size() == 0 ? secondSet.size() : firstSet.size();

        for(physicalEntityParticipant pep1 : firstSet) {
            // We're gonna fill the set with scores, and get the maximum
            Map<Double, physicalEntityParticipant> scoreSet
                    = new HashMap<Double, physicalEntityParticipant>();

            for(physicalEntityParticipant pep2 : secondSet) {
                Double pepScore;
                if( (transposeMatrix
                        ? complexScoreHelper(pep2.getPHYSICAL_ENTITY(), pep1.getPHYSICAL_ENTITY())
                        : complexScoreHelper(pep1.getPHYSICAL_ENTITY(), pep2.getPHYSICAL_ENTITY())) ) {
                   pepScore = (transposeMatrix
                                            ? pepScoreMatrix.get(pep2).get(pep1)
                                            : pepScoreMatrix.get(pep1).get(pep2)
                                      );
                } else {
                    pepScore = this.BASE_SCORE;
                }

                scoreSet.put(pepScore, pep2);
            }

            // We have the scores, let's get the maximum
            Double maxScore = Collections.max(scoreSet.keySet());

            // We know the best match, multiply its score with the finalScore
            finalScore *= maxScore;

            // Check for transposed matrix
            if(transposeMatrix)
                pepMap.put(scoreSet.get(maxScore), pep1);
            else
                pepMap.put(pep1, scoreSet.get(maxScore));
        }

        // Here comes the last edit to final score
        finalScore = (finalScore / Math.pow(MAX_PEP_SCORE, minSize))        // Rate actual score over max.
                                * Math.pow(SIZE_MISMATCH_PENALTY, sizeDiff); // Give penalty for size mismatches

        return new PEPScore(finalScore, pepMap);
    }

    private ConversionScore getScore(Map<physicalEntityParticipant,
                                                Map<physicalEntityParticipant, Double>> pepScoreMatrix,
                                     conversion conv1, conversion conv2) {
        boolean reverseMatch;
        Double score;
        Map<physicalEntityParticipant, physicalEntityParticipant> pepMap
            = new HashMap<physicalEntityParticipant, physicalEntityParticipant>();

        // left-to-left, right-to-right, left-to-right, right-to-left
        PEPScore l_l, r_r, l_r, r_l;

        /* Two possiblity for a match, check for them and get the best match */

        // 1# left->left , right->right (aka "straight")
        l_l = getScore(pepScoreMatrix, conv1.getLEFT(), conv2.getLEFT());
        r_r = getScore(pepScoreMatrix, conv1.getRIGHT(), conv2.getRIGHT());
        Double straightScore = l_l.getScore() * r_r.getScore();

        // 2# left->right , right->left (aka "reverse")
        l_r = getScore(pepScoreMatrix, conv1.getLEFT(), conv2.getRIGHT());
        r_l = getScore(pepScoreMatrix, conv1.getRIGHT(), conv2.getLEFT());
        Double reverseScore = l_r.getScore() * r_l.getScore();

        /* */

        if(straightScore >= reverseScore) { // Straight match
            reverseMatch = false;
            score = straightScore;
            pepMap.putAll(l_l.getPEPMap());
            pepMap.putAll(r_r.getPEPMap());
        } else { // Reverse match
            reverseMatch = true;
            score = reverseScore;
            pepMap.putAll(l_r.getPEPMap());
            pepMap.putAll(r_l.getPEPMap());
        }
        score *= getScoresOver(); // (0,1] -> (0, Scores Over]
        return new ConversionScore(conv1, conv2, score, pepMap, reverseMatch);
    }


    /* Update functions below are modified to fulfill required object editor
       modifiying on the PEPs.
      */
    private void updatePepFields(physicalEntityParticipant update,
                                    physicalEntityParticipant existing) {
        if( !(update instanceof sequenceParticipant ^ existing instanceof sequenceParticipant) )
            updateObjectFields(update, existing);
    }

    private void updateObjectFields(BioPAXElement update, BioPAXElement existing) {
        Set<PropertyEditor> editors =  editorMap.getEditorsOf(update);

        for (PropertyEditor editor : editors) {
            if ( !editor.property.equals("PHYSICAL-ENTITY") ) {
                updateObjectFieldsForEditor(editor, update, existing);
            }

        }
    }

    private void updateObjectFieldsForEditor(PropertyEditor editor,
	                                            BioPAXElement update,
	                                            BioPAXElement existing) {

			for (Object updateValue : editor.getValueFromBean(update)) {
                boolean notDuplicate = true;

                try {
                    if( updateValue instanceof BioPAXElement ) {
                        for (Object existingValue : editor.getValueFromBean(existing)) {
                            if( ((BioPAXElement) existingValue).isEquivalent((BioPAXElement) updateValue) ) {
                                notDuplicate = false;
                                break;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.info("Empty property on bean, skipping...");
                }

                if( notDuplicate )
                    updateField(editor, updateValue, existing);
			}

	}

    private void updateField(PropertyEditor editor, Object updateValue,
	                   BioPAXElement existing) {
		editor.setValueToBean(updateValue, existing); //TODO:TEST
	}

    /* End of update functions */

    /* Method below are temporary but manual normalization for the time being */
    private void normalizeXrefs(Model model) {
        for(xref oneXref : model.getObjects(xref.class) ) {
            for( String[] dbChange : dbChanges ) {
                if( oneXref.getDB() != null )
                    oneXref.setDB(oneXref.getDB().replace(dbChange[0], dbChange[1]));
            }
        }
    }

    private void normalizeOpenControlledVocabulary(Model model) {
        for(openControlledVocabulary ocv1: target.getObjects(openControlledVocabulary.class)) {
            for(openControlledVocabulary ocv2: model.getObjects(openControlledVocabulary.class)) {
                if( isOCVsSemanticallyEquivalent(ocv1, ocv2) ) {
                    equalize(ocv1, ocv2);
                }
            }
        }
        for(openControlledVocabulary ocv1: model.getObjects(openControlledVocabulary.class)) {
            for(openControlledVocabulary ocv2: model.getObjects(openControlledVocabulary.class)) {
                if( isOCVsSemanticallyEquivalent(ocv1, ocv2) ) {
                    equalize(ocv1, ocv2);
                }
            }
        }
    }

    private boolean isOCVsSemanticallyEquivalent(openControlledVocabulary ocv1, openControlledVocabulary ocv2) {
        return ocv1.equals(ocv2) ||
                   ( (ocv1.getXREF().isEmpty() || ocv2.getXREF().isEmpty())
                                    ? OCVsHaveCommonTerm(ocv1, ocv2)
                                    : (!ocv1.findCommonUnifications(ocv2).isEmpty()
                                            || OCVsHaveCommonTerm(ocv1, ocv2)) );
    }

    private boolean OCVsHaveCommonTerm(openControlledVocabulary ocv1, openControlledVocabulary ocv2) {
        for (String s : ocv1.getTERM()) {
            if (ocv2.getTERM().contains(s)) {
                return true;
            }
        }
        return false;
    }

    private void normalizeCellularLocations(Model model) {
        openControlledVocabulary mostlyUsed = null;
        Integer maxOccurence = 0;

        Map<openControlledVocabulary, Integer> termCounter
                = new HashMap<openControlledVocabulary, Integer>();
        for(BioPAXElement pep : target.getObjects(physicalEntityParticipant.class)) {
            openControlledVocabulary ov
                            = ((physicalEntityParticipant) pep).getCELLULAR_LOCATION();
            if( ov == null )
                continue;

            Integer cnt = termCounter.get(ov);
            if( cnt == null ) {
                cnt = 0;
                termCounter.put(ov, cnt);
            }

            cnt += 1;

            if( cnt > maxOccurence )
                mostlyUsed = ov;
        }

        if( mostlyUsed == null )
            return;

        ArrayList <physicalEntityParticipant> pepList = new ArrayList<physicalEntityParticipant>();
        pepList.addAll( model.getObjects(physicalEntityParticipant.class) );

        for(BioPAXElement pep : pepList) {
            openControlledVocabulary ov
                    = ((physicalEntityParticipant) pep).getCELLULAR_LOCATION();

            if( ov == null ) {
                if( model.getByID(mostlyUsed.getRDFId()) == null ) {
                    ov = model.addNew(openControlledVocabulary.class, mostlyUsed.getRDFId());
                    ov.setCOMMENT( mostlyUsed.getCOMMENT() );
                    ov.setTERM( mostlyUsed.getTERM() );
                    ov.setXREF( mostlyUsed.getXREF() );
                } else {
                    ov = (openControlledVocabulary) model.getByID(mostlyUsed.getRDFId());
                }

                ((physicalEntityParticipant) pep).setCELLULAR_LOCATION(ov);

            } else if ( ov.getTERM().isEmpty() ) {
                ov.setTERM( mostlyUsed.getTERM() );
            } else if (isCellularLocsTermsSimilar(ov.getTERM(), mostlyUsed.getTERM())) {
                ov.setTERM( mostlyUsed.getTERM() );
                ov.setXREF( mostlyUsed.getXREF() );
            }
        }
    }

    /* End of normalization methods */

}

/**
 * An encapsulation of the score and pep map
 */
class PEPScore {
    private Double score;
    private Map<physicalEntityParticipant, physicalEntityParticipant> pepMap;

    public PEPScore(Double score,
                    Map<physicalEntityParticipant, physicalEntityParticipant> pepMap) {
        this.score = score;
        this.pepMap = pepMap;
    }

    public Double getScore() {
        return score;
    }

    public Map<physicalEntityParticipant, physicalEntityParticipant> getPEPMap() {
        return pepMap;
    }
}