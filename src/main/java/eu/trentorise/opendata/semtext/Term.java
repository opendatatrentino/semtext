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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import eu.trentorise.opendata.commons.NotFoundException;
import static eu.trentorise.opendata.semtext.SemTexts.checkMeaningStatus;
import static eu.trentorise.opendata.semtext.SemTexts.checkSpan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents text marked with some meaning. Text can contain spaces, like i.e.
 * "hot dog".
 *
 * @author David Leoni <david.leoni@unitn.it>
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Term implements Span, Serializable, HasMetadata {

    private static final long serialVersionUID = 1L;

    private int start;
    private int end;
    private ImmutableList<Meaning> meanings;

    private MeaningStatus meaningStatus;
    private Meaning selectedMeaning;
    private ImmutableMap<String, ?> metadata;

    /**
     * so serialization libraries don't complain
     */
    private Term() {
        this.start = 0;
        this.end = 0;
        this.meanings = ImmutableList.of();
        this.meaningStatus = MeaningStatus.TO_DISAMBIGUATE;
        this.selectedMeaning = null;
        this.metadata = ImmutableMap.of();
    }

    /**
     * Returns the status of the possible meaning associated to this term.
     */
    public MeaningStatus getMeaningStatus() {
        return meaningStatus;
    }

    /**
     * Constructs a Term. Meaning probabilities are stored deduplicated and
     * normalized so total sum is 1.0 .
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param meaningStatus Must have a corresponding correct
     * {@code selectedMeaning}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param selectedMeaning Must have a corresponding correct
     * {@code meaningStatus}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param meanings the suggested meanings. Internally, a new collection is
     * created to store the deduplicated meanings with normalized probabilities.
     * @param metadata a map of metadata. Provided metadata objects must be
     * immutable.
     */
    private Term(int start,
            int end,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            Iterable<Meaning> meanings,
            Map<String, ?> metadata) {
        this();

        checkSpan(start, end, "Term span is invalid!");
        checkNotNull(meanings);
        checkNotNull(meaningStatus);

        checkMeaningStatus(meaningStatus, selectedMeaning, "Term has invalid selected meaning!");

        normalizeMeanings(meanings, selectedMeaning);

        this.start = start;
        this.end = end;
        this.meaningStatus = meaningStatus;
        this.metadata = ImmutableMap.copyOf(metadata);
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    /**
     * Returns the sorted meanings, the first having the highest probability
     */
    public ImmutableList<Meaning> getMeanings() {
        return meanings;
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
     *
     * Returns the selected meaning. Note selected meaning can be partial, that
     * is, have empty URL or meaning kind set to UNKNOWN, or both.
     *
     * <ul>
     * <li> SELECTED: Returns the selected meaning. Meaning will have a
     * non-empty ID. </li>
     * <li> TO_DISAMBIGUATE: Returns null </li>
     * <li> NOT_SURE: Returns null </li>
     * <li> VALIDATED: Returns the selected meaning, with a valid ID. </li>
     * </ul>
     */
    @Nullable
    public Meaning getSelectedMeaning() {
        return selectedMeaning;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.start;
        hash = 97 * hash + this.end;
        hash = 97 * hash + (this.meanings != null ? this.meanings.hashCode() : 0);
        hash = 97 * hash + (this.meaningStatus != null ? this.meaningStatus.hashCode() : 0);
        hash = 97 * hash + (this.selectedMeaning != null ? this.selectedMeaning.hashCode() : 0);
        hash = 97 * hash + (this.metadata != null ? this.metadata.hashCode() : 0);
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
        final Term other = (Term) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (this.meanings != other.meanings && (this.meanings == null || !this.meanings.equals(other.meanings))) {
            return false;
        }
        if (this.meaningStatus != other.meaningStatus) {
            return false;
        }
        if (this.selectedMeaning != other.selectedMeaning && (this.selectedMeaning == null || !this.selectedMeaning.equals(other.selectedMeaning))) {
            return false;
        }
        if (this.metadata != other.metadata && (this.metadata == null || !this.metadata.equals(other.metadata))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Term{" + "start=" + start + ", end=" + end + ", meanings=" + meanings + ", meaningStatus=" + meaningStatus + ", selectedMeaning=" + selectedMeaning + ", metadata=" + metadata + '}';
    }

    /**
     * Copy constructor
     */
    private Term(Term term) {
        this.start = term.getStart();
        this.end = term.getEnd();
        this.meanings = term.getMeanings();
        this.selectedMeaning = term.getSelectedMeaning();
        this.metadata = term.getMetadata();
    }

    /**
     * Stores in the term normalized versions of the provided meaning
     * probabilities and also the provided selected meaning. Inputs are not
     * changed.
     *
     * @param meanings won't be changed by the method. When deduplicating,
     * meanings occurring first will be used.
     * @param selectedMeaning if unknown use null
     */
    private void normalizeMeanings(Iterable<Meaning> meanings, @Nullable Meaning selectedMeaning) {
        checkNotNull(meanings);

        Set<Meaning> dedupMeanings = Sets.newHashSet(meanings);

        float total = 0;
        for (Meaning m : dedupMeanings) {
            total += m.getProbability();
        }
        if (total <= 0) {
            total = dedupMeanings.size();
        }

        List<Meaning> mgs = new ArrayList();
        for (Meaning m : dedupMeanings) {
            Meaning newM = m.withProbability(m.getProbability() / total);

            mgs.add(newM);
        }

        Collections.sort(mgs, Collections.reverseOrder());

        this.meanings = ImmutableList.copyOf(mgs);

        this.selectedMeaning = selectedMeaning;

    }

    /**
     * Returns a new term with the provided meanings. Internally, a new list of
     * normalized and deduplicated meanings will be stored.
     *
     * @param meanings won't be changed by the method. When deduplicating,
     * meanings occurring first will be used.
     */
    public Term with(Iterable<Meaning> meanings) {
        Term ret = new Term(this);
        ret.normalizeMeanings(meanings, selectedMeaning);
        return ret;
    }

    /**
     * A new term is returned with the provided pair meaning status and selected
     * meaning set.
     *
     * @param meaningStatus
     * @param selectedMeaning if unknown use null
     */
    public Term with(MeaningStatus meaningStatus, @Nullable Meaning selectedMeaning) {
        Term ret = new Term(this);
        checkMeaningStatus(meaningStatus, selectedMeaning, "Trying to modify a term with invalid meaning status / selected meaning!");
        ret.normalizeMeanings(ret.meanings, selectedMeaning);
        return ret;
    }

    /**
     * Returns a copy of this object with the provided metadata set under the
     * given namespace.
     *
     * @param metadata Must be an immutable object.
     */
    public Term withMetadata(String namespace, Object metadata) {
        Term ret = new Term(this);
        ret.metadata = SemTexts.replaceMetadata(this.metadata, namespace, metadata);
        return ret;
    }

    /**
     * Factory method for a Term with only one meaning. Meaning probabilities
     * are stored deduplicated and normalized so total sum is 1.0 .
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param meaningStatus Must have a corresponding correct
     * {@code selectedMeaning}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param selectedMeaning Must have a corresponding correct
     * {@code meaningStatus}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     */
    public static Term of(int start,
            int end,
            MeaningStatus meaningStatus,
            Meaning selectedMeaning) {

        return of(start,
                end,
                meaningStatus,
                selectedMeaning,
                ImmutableList.<Meaning>of(),
                SemTexts.EMPTY_METADATA);
    }

    /**
     * Factory method for a Term. Meaning probabilities are stored deduplicated
     * and normalized so total sum is 1.0 .
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param meaningStatus Must have a corresponding correct
     * {@code selectedMeaning}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param selectedMeaning Must have a corresponding correct
     * {@code meaningStatus}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param meanings the suggested meanings. Internally, a new collection is
     * created to store the deduplicated meanings with normalized probabilities.
     */
    public static Term of(int start,
            int end,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            Iterable<Meaning> meanings) {

        return of(start,
                end,
                meaningStatus,
                selectedMeaning,
                meanings,
                SemTexts.EMPTY_METADATA);
    }

    /**
     * Factory method for a Term. Meaning probabilities are stored deduplicated
     * and normalized so total sum is 1.0 .
     *
     * @param start 0-indexed span offset start. Position is absolute with
     * respect to the text stored in the {@code SemText} container.
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * {@code SemText} container.
     * @param meaningStatus Must have a corresponding correct
     * {@code selectedMeaning}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param selectedMeaning Must have a corresponding correct
     * {@code meaningStatus}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param meanings the suggested meanings. Internally, a new collection is
     * created to store the deduplicated meanings with normalized probabilities.
     * @param metadata a map of metadata. Provided metadata objects must be
     * immutable.
     */
    public static Term of(int start,
            int end,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            Iterable<Meaning> meanings,
            Map<String, ?> metadata) {
        return new Term(start,
                end,
                meaningStatus,
                selectedMeaning,
                meanings,
                metadata);
    }

}
