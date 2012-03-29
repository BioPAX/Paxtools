package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

/**
 * Utility class for switching to the equivalence based comparison of  BioPAXElement.
 *
 * BioPAXElement by default implements equals and hash code based on its RDF-ID.
 * On the other hand for many elements it is possible to determine semantic equivalence
 * among elements. For example two entityFeatures with exactly the same type and location
 * are equivalent. This logic is implemented in isEquivalent() and equivalenceCode()
 * methods.
 *
 * For most Java collections that uses hashCode and equals there is no easy way to plug-in a
 * comparator to switch to different comparison behaviour. This is a wrapper based solution
 * that redirects the calls to equals and hashCode to isEquivalent and equavalanceCode method
 * of the inner object.
 *
 * This wrapper also captures the last equivalent object that it was compared to.
 *
 */
public class EquivalenceWrapper
{

        private BioPAXElement bpe;
        private BioPAXElement eqBpe;

        public EquivalenceWrapper(BioPAXElement bpe) {

           this.bpe=bpe;
        }

        @Override
        public int hashCode() {
            return getBpe().equivalenceCode();
        }


        @Override
        public boolean equals(Object obj) {

            EquivalenceWrapper that = (EquivalenceWrapper) obj;
            if (getBpe().isEquivalent(that.bpe)) {
                this.eqBpe = that.bpe;
                return true;
            } else return false;
        }

    public BioPAXElement getEqBpe() {
        return eqBpe;
    }

    public BioPAXElement getBpe() {
        return bpe;
    }

}
