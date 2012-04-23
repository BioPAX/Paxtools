package org.biopax.paxtools.causality.data;

import org.biopax.paxtools.causality.model.Alteration;

public class GeneticProfile {
    private String id;
    private String name;
    private GENETIC_PROFILE_TYPE type;
    private String description;

    public GeneticProfile(String id, String name, String description, String type) {
        this(id, name, description, inferType(type));
    }

    public GeneticProfile(String id, String name, String description, GENETIC_PROFILE_TYPE type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;

    }

    private static GENETIC_PROFILE_TYPE inferType(String type) {
        try {
            return GENETIC_PROFILE_TYPE.valueOf(type);
        } catch (Exception e) {
            return GENETIC_PROFILE_TYPE.NOT_KNOWN;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GENETIC_PROFILE_TYPE getType() {
        return type;
    }

    public void setType(GENETIC_PROFILE_TYPE type) {
        this.type = type;
    }

    public static enum GENETIC_PROFILE_TYPE {
        NOT_KNOWN,
        COPY_NUMBER_ALTERATION,
        MRNA_EXPRESSION,
        METHYLATION,
        METHYLATION_BINARY,
        MUTATION_EXTENDED,
        PROTEIN_ARRAY_PROTEIN_LEVEL,
        PROTEIN_ARRAY_PHOSPHORYLATION;

        public static Alteration convertToAlteration(GENETIC_PROFILE_TYPE type) {
            switch(type) {
                case COPY_NUMBER_ALTERATION:
                    return Alteration.COPY_NUMBER;
                case MRNA_EXPRESSION:
                    return Alteration.EXPRESSION;
                case METHYLATION:
                    return Alteration.METHYLATION;
                case METHYLATION_BINARY:
                    return Alteration.METHYLATION;
                case MUTATION_EXTENDED:
                    return Alteration.MUTATION;
                case PROTEIN_ARRAY_PROTEIN_LEVEL:
                    return Alteration.PROTEIN_LEVEL;
                case PROTEIN_ARRAY_PHOSPHORYLATION:
                    return Alteration.PROTEIN_LEVEL;
                default:
                    return null;
            }
        }
    }
}
