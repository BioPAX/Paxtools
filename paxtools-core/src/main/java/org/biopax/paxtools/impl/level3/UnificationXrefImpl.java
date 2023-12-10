package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.UnificationXref;


public class UnificationXrefImpl extends XrefImpl implements UnificationXref {

  public UnificationXrefImpl() {
  }

  public Class<? extends UnificationXref> getModelInterface() {
    return UnificationXref.class;
  }

  @Override
  protected boolean semanticallyEquivalent(BioPAXElement other) {
    return (other instanceof UnificationXref) && super.semanticallyEquivalent(other);
  }

}
