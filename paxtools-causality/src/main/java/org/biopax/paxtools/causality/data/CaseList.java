package org.biopax.paxtools.causality.data;

public class CaseList {
    private String[] cases;
    private String id;
    private String description;

    public CaseList(String id, String description, String[] cases) {
        this.id = id;
        this.description = description;
        this.cases = cases;
    }

    public String[] getCases() {
        return cases;
    }

    public void setCases(String[] cases) {
        this.cases = cases;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
