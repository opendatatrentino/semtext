/* 
 * Copyright 2015 TrentoRISE  (trentorise.eu) .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.trentorise.opendata.semtext;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableMap;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.NotFoundException;
import static eu.trentorise.opendata.semtext.SemTexts.TOLERANCE;
import static eu.trentorise.opendata.semtext.SemTexts.checkPositiveScore;
import java.io.Serializable;
import java.util.Map;
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

    private static final long serialVersionUID = 1L;

    private static final Meaning INSTANCE = new Meaning();

    private String id;
    private MeaningKind kind;
    private double probability;
    private Dict name;
    private ImmutableMap<String, ?> metadata;

    private Meaning() {
        this.id = "";
        this.kind = MeaningKind.UNKNOWN;
        this.probability = 0.0;
        this.name = Dict.of();
        this.metadata = ImmutableMap.of();
    }

    /**
     * shallow copy constructor
     */
    private Meaning(Meaning m) {
        this.id = m.getId();
        this.kind = m.getKind();
        this.probability = m.getProbability();
        this.name = m.getName();
        this.metadata = m.getMetadata();
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
     * @param kind The kind can be either an entity or a concept.
     * @param probability Must be greater or equal than 0
     * @param name the name of the entity or concept this meaning represents. If
     * unknwon, use {@link Dict#of()}
     * @param metadata metadata will be stored in an immutable map.
     */
    private Meaning(String id, MeaningKind kind, double probability, Dict name, Map<String, ?> metadata) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(kind);
        checkNotNull(metadata);

        checkPositiveScore(probability, "Invalid probability for meaning!");

        this.id = id;
        this.kind = kind;
        this.probability = probability;
        this.name = name;
        this.metadata = ImmutableMap.copyOf(metadata);
    }
    
   /**
     * Meaning factory method.
     *
     * @param id the id of the entity or concept this meaning represents,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If unknown, use the empty string.
     * @param kind The kind can be either an entity or a concept.
     * @param probability Must be greater or equal than 0
    
     */
    public static Meaning of(String id, MeaningKind kind, double probability) {
        return of(id, kind, probability, Dict.of(), SemTexts.EMPTY_METADATA);
    }    

    /**
     * Meaning factory method.
     *
     * @param id the id of the entity or concept this meaning represents,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If unknown, use the empty string.
     * @param kind The kind can be either an entity or a concept.
     * @param probability Must be greater or equal than 0
     * @param name the name of the entity or concept this meaning represents. If
     * unknwon, use {@link Dict#of()}
     */
    public static Meaning of(String id, MeaningKind kind, double probability, Dict name) {
        return of(id, kind, probability, name, SemTexts.EMPTY_METADATA);
    }

    /**
     * Meaning factory method.
     *
     * @param id the id of the entity or concept this meaning represents,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If unknown, use the empty string.
     * @param kind The kind can be either an entity or a concept.
     * @param probability Must be greater or equal than 0
     * @param name the name of the entity or concept this meaning represents. If
     * unknwon, use {@link Dict#of()}
     * @param metadata the metadata to associate to the meaning. Objects
     * contained in the map must be immutable.
     */
    public static Meaning of(String id, MeaningKind kind, double probability, Dict name, Map<String, ?> metadata) {
        return new Meaning(id, kind, probability, name, metadata);
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
     * @return the probability of this meaning
     */
    public double getProbability() {
        return probability;
    }

    /**
     * @return the id of the entity or the concept represented by this meaning,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target="_blank">
     * as specified in JSON-LD </a>. If no id was assigned to meaning, returns
     * the empty string.
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

    /**
     * Returns a shallow copy of this meaning with provided probability set.
     *
     * @param probability must be greater than -{@link SemTexts#TOLERANCE}
     */
    public Meaning withProbability(double probability) {
        if (probability < -TOLERANCE) {
            throw new IllegalArgumentException("Probability must be greater or equal than -" + TOLERANCE + ", found instead: " + probability);
        }
        Meaning ret = new Meaning(this);
        ret.probability = probability;
        return ret;
    }

    @Override
    public String toString() {
        return "Meaning{" + "id=" + id + ", kind=" + kind + ", probability=" + probability + ", name=" + name + ", metadata=" + metadata + '}';
    }

    /**
     * Returns a copy of this object with the provided metadata set under the
     * given namespace.
     *
     * @param metadata Must be an immutable object.
     */
    public Meaning withMetadata(String namespace, Object metadata) {
        Meaning ret = new Meaning(this);
        ret.metadata = SemTexts.replaceMetadata(this.metadata, namespace, metadata);
        return ret;
    }
}
