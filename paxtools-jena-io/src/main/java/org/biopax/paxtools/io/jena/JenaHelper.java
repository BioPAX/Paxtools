package org.biopax.paxtools.io.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * TODO:Class description
 * User: demir
 * Date: Aug 15, 2008
 * Time: 7:11:32 PM
 */
class JenaHelper
{
    private static final OntModelSpec spec;
    static
    {

                spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);

    }
    static OntModel createModel()
    {
        return ModelFactory.createOntologyModel(spec);
    }
}