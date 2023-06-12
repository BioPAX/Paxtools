package org.biopax.paxtools.normalizer;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.ShallowCopy;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.BPCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A non-public helper class to map "old" (original) to "new" (replacement) objects
 * in a BioPAX model and then to substitute/replace them all (update object properties, model).
 *
 * @author rodche
 */
class NormalizerMap {
    static final Logger log = LoggerFactory.getLogger(NormalizerMap.class);

    //a model to modify by replacing some objects
    final Model model;

    // biopax elements substitution map (old->new)
    final Map<BioPAXElement, BioPAXElement> subs;

    //a map that helps to make sure subs.values() all have different URIs
    final Map<String, BioPAXElement> uriToSub;

    final ShallowCopy copier;

    NormalizerMap(Model model) {
        subs = BPCollections.I.createMap();
        uriToSub = BPCollections.I.createMap();
        this.model = model;
        copier = new ShallowCopy(); //BioPAX L3
    }


    /**
     * Creates (by URI) and saves the replacement object,
     * but does not replace yet (call doSubs to replace).
     *
     * @param bpe
     * @param newUri
     */
    void put(BioPAXElement bpe, String newUri) {
        if (model.containsID(newUri)) {
            // will use existing original (model) object that has the new Uri
            map(bpe, model.getByID(newUri));
        } else if (uriToSub.containsKey(newUri)) {
            // re-use the new object that's already added to replace another original
            map(bpe, uriToSub.get(newUri));
        } else {
            // will use the object's shallow copy that gets new Uri
            BioPAXElement copy = copier.copy(bpe, newUri);
            map(bpe, copy);
        }
    }

    /**
     * Executes the batch replace - migrating
     * to the normalized equivalent objects.
     */
    void doSubs() {
        for (BioPAXElement e : subs.keySet()) {
            model.remove(e);
        }

        try {
            ModelUtils.replace(model, subs);
        } catch (Exception e) {
            log.error("Failed to replace BioPAX elements", e);
            return;
        }

        for (BioPAXElement e : subs.values()) {
            if (!model.containsID(e.getUri())) {
                model.add(e);
            }
        }

        for (BioPAXElement e : model.getObjects()) {
            ModelUtils.fixDanglingInverseProperties(e, model);
        }
    }

    private void map(BioPAXElement bpe, BioPAXElement newBpe) {
        subs.put(bpe, newBpe);
        uriToSub.put(newBpe.getUri(), newBpe);
    }
}
