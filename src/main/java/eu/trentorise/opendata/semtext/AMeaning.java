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

import eu.trentorise.opendata.semtext.jackson.SemTextModule.MeaningMetadataDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import eu.trentorise.opendata.commons.BuilderStylePublic;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.SimpleStyle;
import eu.trentorise.opendata.commons.TodUtils;
import eu.trentorise.opendata.commons.exceptions.TodNotFoundException;
import eu.trentorise.opendata.semtext.exceptions.SemTextNotFoundException;
import eu.trentorise.opendata.semtext.jackson.SemTextModule.MeaningMetadataDeserializer;

import static eu.trentorise.opendata.semtext.SemTexts.TOLERANCE;
import static eu.trentorise.opendata.semtext.SemTexts.checkPositiveScore;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.immutables.value.Value;

/**
 * Represents a meaning along with its probability.
 *
 * Equality is checked only considering the id and meaning kind.
 *
 * @author David Leoni <david.leoni@unitn.it>
 */

@Value.Immutable
@BuilderStylePublic
@JsonSerialize(as = Meaning.class)
@JsonDeserialize(as = Meaning.class)
abstract class AMeaning implements Comparable<Meaning>, Serializable, HasMetadata {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean hasMetadata(String namespace) {
        return getMetadata().containsKey(namespace);
    }

    @Value.Default
    @JsonDeserialize(using = MeaningMetadataDeserializer.class)
    @Override
    public ImmutableMap<String, ?> getMetadata() {
        return ImmutableMap.of();
    }

    @Override
    public Object getMetadata(String namespace) {
        Object ret = getMetadata().get(namespace);
        if (ret == null) {
            throw new SemTextNotFoundException("There is no metadata under the namespace " + namespace + " in " + this);
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
        hash = 29 * hash + (getId() != null ? getId().hashCode() : 0);
        hash = 29 * hash + (getKind() != null ? getKind().hashCode() : 0);
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
        if ((getId() == null) ? (other.getId() != null) : !getId().equals(other.getId())) {
            return false;
        }
        if (getKind() != other.getKind()) {
            return false;
        }
        return true;
    }

    @Value.Check
    protected void check() {
        checkPositiveScore(getProbability(), "Invalid probability for meaning!");

    }

    /**
     * Meaning factory method.
     *
     * @param id
     *            the id of the entity or concept this meaning represents,
     *            <a href="http://www.w3.org/TR/json-ld/#node-identifiers"
     *            target="_blank"> as specified in JSON-LD </a>. If unknown, use
     *            the empty string.
     * @param kind
     *            The kind can be either an entity or a concept.
     * @param probability
     *            Must be greater or equal than 0
     * 
     */
    public static Meaning of(String id, MeaningKind kind, double probability) {
        return Meaning.builder()
                .setId(id)
                .setKind(kind)
                .setProbability(probability)
                .build();
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
     * The probability of this meaning. Must be greater or equal than 0
     */
    @Value.Default
    public double getProbability() {
        return 0.0;
    }

    /**
     * The id of the entity or the concept represented by this meaning,
     * <a href="http://www.w3.org/TR/json-ld/#node-identifiers" target=
     * "_blank"> as specified in JSON-LD</a>. If no id was assigned to meaning,
     * returns the empty string.
     */
    @Value.Default
    public String getId() {
        return "";
    }

    /**
     * The kind of the meaning. By default it is {@link MeaningKind#UNKNOWN}
     *
     */
    @Value.Default
    public MeaningKind getKind() {
        return MeaningKind.UNKNOWN;
    }

    /**
     * The name of the entity or concept represented by this meaning. By default
     * the dictionary is empty.
     *
     */
    @Value.Default
    public Dict getName() {
        return Dict.of();
    }

    /**
     * The description of the entity or concept represented by this meaning. By
     * default the dictionary is empty.
     *
     */
    @Value.Default
    public Dict getDescription() {
        return Dict.of();
    }

    /**
     * Returns a copy of this object with the provided metadata set under the
     * given namespace.
     *
     * @param metadata
     *            Must be an immutable object.
     */
    public Meaning withMetadata(String namespace, Object metadata) {

        return ((Meaning)this).withMetadata(TodUtils.putKey((Map<String, Object>) getMetadata(), namespace, metadata));
    }
}
