package eu.trentorise.opendata.semantics.nlp.model;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableMap;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.NotFoundException;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a meaning along with its probability.
 *
 * Equality is checked only considering the id and meaning kind.
 *
 * @author David Leoni <david.leoni@unitn.it>
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Meaning implements Comparable<Meaning>, Serializable, HasMetadata {

    private static final Meaning INSTANCE = new Meaning();

    private String id;
    private double probability;
    private MeaningKind kind;
    private MeaningStatus status;
    private Dict name;
    private ImmutableMap<String, Object> metadata;

    private Meaning() {
        this.id = "";
        this.kind = MeaningKind.UNKNOWN;
        this.probability = 0.0;
        this.name = Dict.of();
        this.metadata = ImmutableMap.of();
    }

    @Override
    public boolean hasMetadata(String namespace) {
        return metadata.containsKey(namespace);
    }

    @Override
    public ImmutableMap<String, ?> getMetadata() {
        return metadata;
    }

    @Override
    public Object getMetadata(String namespace) {
        Object ret = metadata.get(namespace);
        if (ret == null) {
            throw new NotFoundException("There is no metadata under the namespace " + namespace + " in " + this);
        } else {
            return ret;
        }
    }
    
    
    /**
     * Equality must only check the id and kind
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        return hash;
    }

    /**
     * Equality is checked only considering the id and kind
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Meaning other = (Meaning) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if (this.kind != other.kind) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param id the id of the entity or concept this meaning represents,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If unknown use an empty string.
     * @param probability Must be greater or equal than 0
     * @param meaningKind The kind can be either an entity or a concept.
     * @param name the name of the entity or concept this meaning represents.
     */
    private Meaning(String id, double probability, MeaningKind meaningKind, Dict name) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(meaningKind);
        if (probability < 0) {
            throw new IllegalArgumentException("Probability must be greater or equal than 0, found instead: " + probability);
        }
        this.id = id;
        this.probability = probability;
        this.kind = meaningKind;
        this.name = name;
    }

    /**
     * Meaning factory method.
     *
     * @param id the id of the entity or concept this meaning represents,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If unknown, use the empty string.
     * @param probability Must be greater or equal than 0
     * @param meaningKind The kind can be either an entity or a concept.
     */
    public static Meaning of(String id, double probability, MeaningKind meaningKind) {
        return new Meaning(id, probability, meaningKind, Dict.of());
    }

    /**
     * Meaning factory method.
     *
     * @param id the id of the entity or concept this meaning represents,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If unknown, use the empty string.
     * @param probability Must be greater or equal than 0
     * @param meaningKind The kind can be either an entity or a concept.
     * @param name the name of the entity or concept this meaning represents.
     */
    public static Meaning of(String id, double probability, MeaningKind meaningKind, Dict name) {
        return new Meaning(id, probability, meaningKind, name);
    }

    /**
     * Returns a meaning with empty id and {@link MeaningKind#UNKNOWN}
     */
    public static Meaning of() {
        return INSTANCE;
    }

    /**
     * Sorting is done based on the probability values
     */
    @Override
    public int compareTo(Meaning om) {
        double diff = this.getProbability() - om.getProbability();
        if (diff > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Determines the best meaning among the given ones according to their
     * probabilities. If no best meaning is found null is returned.
     *
     * @param meanings a sorted list of meanings
     * @return the disambiguated meaning or null if no meaning can be clearly
     * dismabiguated.
     */
    @Nullable
    public static Meaning disambiguate(List<Meaning> meanings) {

        final double FACTOR = 1.5;

        if (meanings.isEmpty()) {
            return null;
        }

        if (meanings.size() == 1) {
            Meaning m = meanings.iterator().next();
            if (m.getId() == null) {
                return null;
            } else {
                return m;
            }
        }

        if (meanings.get(0).getProbability() > FACTOR / meanings.size()
                && meanings.get(0).getId() != null) {
            return meanings.get(0);
        } else {
            return null;
        }

    }

    /**
     * @return the probability of this meaning
     */
    public double getProbability() {
        return probability;
    }

    /**
     * @return the id of the entity or the concept represented by this meaning,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If no id was assigned to meaning, returns
 the empty string.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the kind of the meaning.
     *
     * @return the kind of the meaning, which can either be an entity or a
     * concept
     */
    public MeaningKind getKind() {
        return kind;
    }

    /**
     * Gets the name of the entity or concept represented by this meaning
     *
     * @return the name of the entity or concept represented by the meaning. The
     * dictionary can be empty.
     */
    public Dict getName() {
        return name;
    }

}
