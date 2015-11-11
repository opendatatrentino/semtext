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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.trentorise.opendata.commons.NotFoundException;
import eu.trentorise.opendata.commons.TodUtils;

import static eu.trentorise.opendata.semtext.SemTexts.checkSpan;
import static eu.trentorise.opendata.semtext.SemTexts.checkSpans;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * A sentence which can contain terms.
 *
 * @author David Leoni
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Sentence implements Span, Serializable, HasMetadata {

    private static final long serialVersionUID = 1L;

    private int start;
    private int end;

    private ImmutableList<Term> terms;
    private ImmutableMap<String, ?> metadata;

    private Sentence() {
        this.start = 0;
        this.end = 0;
        this.terms = ImmutableList.of();
        this.metadata = ImmutableMap.of();
    }

    private Sentence(Sentence sentence) {
        this.start = sentence.getStart();
        this.end = sentence.getEnd();
        this.terms = sentence.getTerms();
        this.metadata = sentence.getMetadata();
    }

    /**
     * Constructs a Sentence.
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the sentence
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param terms Terms within the sentence.
     * @param metadata a map of metadata. Provided metadata objects must be
     * immutable.
     */
    private Sentence(int start, int end, Iterable<Term> terms, Map<String, ?> metadata) {
        this();

        checkSpan(start, end, "Sentence bounds are not correct!");
        checkSpans(terms, start, end, "Sentence terms are not correct!");

        this.start = start;
        this.end = end;
        this.terms = ImmutableList.copyOf(terms);
        this.metadata = ImmutableMap.copyOf(metadata);
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
     * Returns the terms contained in the sentence.
     */
    public ImmutableList<Term> getTerms() {
        return terms;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.start;
        hash = 89 * hash + this.end;
        hash = 89 * hash + (this.terms != null ? this.terms.hashCode() : 0);
        hash = 89 * hash + (this.metadata != null ? this.metadata.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sentence other = (Sentence) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (this.terms != other.terms && (this.terms == null || !this.terms.equals(other.terms))) {
            return false;
        }
        if (this.metadata != other.metadata && (this.metadata == null || !this.metadata.equals(other.metadata))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Sentence{" + "start=" + start + ", end=" + end + ", terms=" + terms + ", metadata=" + metadata + '}';
    }

    /**
     * Creates a sentence with the given terms.
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the sentence
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param terms the terms within the sentence.
     */
    public static Sentence of(int start, int end, Term... terms) {
        return of(start, end, ImmutableList.copyOf(terms), SemTexts.EMPTY_METADATA);
    }

    /**
     * Creates a sentence.
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the sentence
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param terms Terms within the sentence.
     */
    public static Sentence of(int start, int end, Iterable<Term> terms) {
        return of(start, end, terms, SemTexts.EMPTY_METADATA);
    }

    /**
     * Creates a sentence.
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the sentence
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param terms Terms within the sentence.
     * @param metadata a map of metadata. Provided metadata objects must be
     * immutable.
     */
    public static Sentence of(int start, int end, Iterable<Term> terms, Map<String, ?> metadata) {
        return new Sentence(start, end, terms, metadata);
    }

    /**
     * Creates a sentence of zero terms.
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the sentence
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     */
    public static Sentence of(int start, int end) {
        return of(start, end, ImmutableList.<Term>of(), SemTexts.EMPTY_METADATA);
    }

    /**
     * Returns a copy of this object with the provided metadata set under the
     * given namespace.
     *
     * @param metadata Must be an immutable object.
     */
    public Sentence withMetadata(String namespace, Object metadata) {
        Sentence ret = new Sentence(this);
        ret.metadata = TodUtils.putKey((Map<String, Object>) this.metadata, namespace, metadata);
        return ret;
    }

    /**
     * Returns a copy of this object with the provided terms set. New terms will
     * replace all the existing ones.
     */
    public Sentence withTerms(Iterable<Term> terms) {
        Sentence ret = new Sentence(this);
        checkSpans(terms, start, end, "Invalid terms!");
        ret.terms = ImmutableList.copyOf(terms);
        return ret;
    }
    
    /**
     * Returns a copy of this object with the provided terms set. New terms will
     * replace all the existing ones.
     */
    public Sentence withTerms(Term... terms) {
        return this.withTerms(ImmutableList.copyOf(terms));
    }
    

}
