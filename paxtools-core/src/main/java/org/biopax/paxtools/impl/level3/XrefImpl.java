package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public abstract class XrefImpl extends L3ElementImpl implements Xref {
  private String db;
  private String dbVersion;
  private String idVersion;
  private String refId;
  private Set<XReferrable> xrefOf;

  /**
   * Constructor.
   */
  public XrefImpl() {
    this.xrefOf = BPCollections.I.createSafeSet();
  }

  @Override
  protected boolean semanticallyEquivalent(BioPAXElement other) {
    if (!(other instanceof Xref)) return false;

    final Xref anXref = (Xref) other;

    return
        (db != null ?
            db.equalsIgnoreCase(anXref.getDb()) :
            anXref.getDb() == null)
            &&
            (refId != null ?
                refId.equals(anXref.getId()) :
                anXref.getId() == null)
            &&
            (dbVersion != null ?
                dbVersion.equalsIgnoreCase(anXref.getDbVersion()) :
                anXref.getDbVersion() == null)
            &&
            (idVersion != null ?
                idVersion.equals(anXref.getIdVersion()) :
                anXref.getIdVersion() == null);
  }

  @Override
  public int equivalenceCode() {
    int result = 29 + (db != null ? db.hashCode() : 0);
    result = 29 * result + (dbVersion != null ? dbVersion.hashCode() : 0);
    result = 29 * result + (idVersion != null ? idVersion.hashCode() : 0);
    result = 29 * result + (refId != null ? refId.hashCode() : 0);
    return result;
  }

  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public String getDbVersion() {
    return dbVersion;
  }

  public void setDbVersion(String dbVersion) {
    this.dbVersion = dbVersion;
  }

  public String getIdVersion() {
    return idVersion;
  }

  public void setIdVersion(String idVersion) {
    this.idVersion = idVersion;
  }


  public String getId() {
    return refId;
  }

  public void setId(String id) {
    this.refId = id;
  }

  public Set<XReferrable> getXrefOf() {
    return xrefOf;
  }

  @Override
  public String toString() {
    return getDb() +
        ((getDbVersion() == null) ? "" : "." + getDbVersion())
        + ":" + getId() +
        ((getIdVersion() == null) ? "" : "." + getIdVersion());
  }

}
