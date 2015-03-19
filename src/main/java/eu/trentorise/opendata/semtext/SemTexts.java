/*
 * Copyright 2015 Trento Rise.
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

import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import eu.trentorise.opendata.commons.Dict;
import static eu.trentorise.opendata.commons.OdtUtils.checkNotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Utilities toolbox for semtexts.
 *
 * @author David Leoni
 */
public class SemTexts {

    /**
     * Tolerance for probabilities
     */
    public static final double TOLERANCE = 0.001;

    /**
     * A meaning score must be DISAMBIGUATION_FACTOR times greater than any
     * other meaning to be automatically considered as SELECTED. This factor can
     * be used during automated conversions.
     */
    final static double DISAMBIGUATION_FACTOR = 1.5;

    /**
     * Determines the best meaning among the given ones according to their
     * probabilities. If no best meaning is found null is returned.
     *
     * @param meanings a sorted list of meanings, with the first ones being the
     * most important.
     * @return the disambiguated meaning or null if no meaning can be clearly
     * identified.
     */
    @Nullable
    public static Meaning disambiguate(Iterable<Meaning> meanings) {

        if (Iterables.isEmpty(meanings)) {
            return null;
        }

        int size = Iterables.size(meanings);

        if (size == 1) {
            Meaning m = meanings.iterator().next();
            if (m.getId() == null) {
                return null;
            } else {
                return m;
            }
        }

        Meaning first = Iterables.getFirst(meanings, null);

        if (first.getProbability() > DISAMBIGUATION_FACTOR / size
                && first.getId() != null) {
            return first;
        } else {
            return null;
        }

    }

    /**
     * Checks that the provided couple meaning status / selected meaning is
     * valid.
     *
     * @param prependedErrorMessage the exception message to use if the check
     * fails; will be converted to a string using String.valueOf(Object) and
     * prepended to more specific error messages.
     *
     * @throws InvalidArgumentException on invalid meaning status
     */
    public static void checkMeaningStatus(MeaningStatus meaningStatus, @Nullable Meaning selectedMeaning, @Nullable Object prependedErrorMessage) {
        if (MeaningStatus.SELECTED.equals(meaningStatus)
                || MeaningStatus.REVIEWED.equals(meaningStatus)) {
            checkNotNull(selectedMeaning, String.valueOf(prependedErrorMessage) + " -- Reason: Selected meaning can't be null when status is " + meaningStatus);
            checkNotEmpty(selectedMeaning.getId(), String.valueOf(prependedErrorMessage) + " -- Reason: Selected meaning must have a valid id when status is " + meaningStatus);
        } else {
            if (selectedMeaning != null) {
                throw new IllegalArgumentException(String.valueOf(prependedErrorMessage) + " -- Reason: Selected meaning must be null when meaning status is " + meaningStatus + ". Found instead meaning " + selectedMeaning);
            }
        }
    }

    /**
     * Checks that the provided meaning is valid.
     *
     * @param prependedErrorMessage the exception message to use if the check
     * fails; will be converted to a string using String.valueOf(Object) and
     * prepended to more specific error messages.
     *
     * @throws InvalidArgumentException on invalid meaning
     */
    public static void checkMeaning(Meaning m, @Nullable Object prependedErrorMessage) {
        checkScore(m.getProbability(), String.valueOf(prependedErrorMessage) + " -- Invalid meaning probability!");
        checkNotNull(m.getName(), String.valueOf(prependedErrorMessage) + " -- Invalid meaning name!");
    }

    /**
     *
     * Checks whether provided score has tolerance above -{@link #TOLERANCE}
     *
     * @param prependedErrorMessage the exception message to use if the check
     * fails; will be converted to a string using String.valueOf(Object) and
     * prepended to more specific error messages.
     *
     * @return the provided score if positive within tolerance
     * @throws IllegalArgumentException on invalid score
     */
    public static double checkPositiveScore(double score, @Nullable Object prependedErrorMessage) {
        if (score < -TOLERANCE) {
            throw new IllegalArgumentException(String.valueOf(prependedErrorMessage) + " -- Reason: Score must be greater or equal than -" + TOLERANCE + ", found instead: " + score);
        }
        return score;
    }

    /**
     * Checks the provided score is within valid bounds.
     * 
     * @param score must be between -{@link #TOLERANCE} ≤ score ≤ 1 + {@link
     * #TOLERANCE} @param prependedErrorMes sage This error message will be
     * prepended to a more specific one generated by this method. @throws
     * InvalidArgumentException
     *
     * @param prependedErrorMessage the exception message to use if the check
     * fails; will be converted to a string using String.valueOf(Object) and
     * prepended to more specific error messages.
     * 
     * @throws IllegalArgumentException on invalid score
     */
    public static void checkScore(double score, @Nullable Object prependedErrorMessage) {
        double prec = TOLERANCE;

        if (score < -prec || score > 1.0 + prec) {
            throw new IllegalArgumentException(String.valueOf(prependedErrorMessage) + " -- Score " + score + " exceeds bounds [" + (-prec) + ", " + 1.0 + prec + "].");
        }
    }

    /**
     * getStart must be less or equal than endoffset and they must be both
     * greater or equal than 0
     *
     * @param prependedErrorMessage the exception message to use if the check
     * fails; will be converted to a string using String.valueOf(Object) and
     * prepended to more specific error messages.
     *
     * @throws IllegalArgumentException
     */
    public static void checkSpan(int startOffset, int endOffset, @Nullable Object prependedErrorMessage) {
        Preconditions.checkArgument(startOffset >= 0
                && startOffset <= endOffset,
                String.valueOf(prependedErrorMessage) + " -- Reason: invalid bounds [" + startOffset + ", " + endOffset + ")");
    }

    /**
     *
     * Checks spans are all be valid spans (see {@link SemTexts#checkSpan(int, int, String)
     * }
     * and are be non-overlapping (a span getEnd offset may coincide with next
     * span getStart offset). Spans must be contained within startOffset and
     * endOffset (last span getEnd offset may coincide with endOffset).
     *
     * @param prependedErrorMessage the exception message to use if the check
     * fails; will be converted to a string using String.valueOf(Object) and
     * prepended to more specific error messages.
     *
     * @throws IllegalArgumentException
     */
    public static void checkSpans(Iterable<? extends Span> spans, int startOffset, int endOffset, @Nullable Object prependedErrorMessage) {

        checkNotNull(spans, prependedErrorMessage);
        checkSpan(startOffset, endOffset, prependedErrorMessage);

        // check containment        
        if (!Iterables.isEmpty(spans)) {
            int lowerBound = Iterables.getFirst(spans, null).getStart();
            int upperBound = Iterables.getLast(spans).getEnd();
            if (lowerBound < startOffset
                    || upperBound > endOffset) {
                throw new IllegalArgumentException(String.valueOf(prependedErrorMessage) + " -- Reason: Provided spans exceed container span! Expected: [" + startOffset + "," + endOffset + "] - Found: [" + lowerBound + "," + upperBound + "]");
            }
        }

        // check overlaps
        @Nullable
        Span lastSpan = null;
        for (Span span : spans) {
            checkSpan(span.getStart(), span.getEnd(), prependedErrorMessage);
            if (lastSpan != null) {
                if (lastSpan.getEnd() > span.getStart()) {
                    throw new IllegalArgumentException(String.valueOf(prependedErrorMessage) + " -- Found overlapping span! Span " + lastSpan + " overlaps with span " + span);
                }
                lastSpan = span;
            }
        }

    }

    /**
     * Creates a Dict out of the provided semantic texts.
     */
    public static Dict semTextsToDict(Iterable<SemText> semTexts) {
        Dict.Builder dictb = Dict.builder();
        for (SemText st : semTexts) {
            dictb.put(st.getLocale(), st.getText());
        }
        return dictb.build();
    }

    /**
     * Returns the provided dictionary as a list of semantic texts
     */
    public static ImmutableList<SemText> dictToSemTexts(Dict dict) {
        ImmutableList.Builder<SemText> retb = ImmutableList.builder();

        for (Locale locale : dict.locales()) {
            for (String s : dict.strings(locale)) {
                retb.add(SemText.of(locale, s));
            }
        }
        return retb.build();
    }

    /**
     * A new term is returned with the provided meanings merged to the existing
     * ones. New meanings will replace equals old meanings.
     */
    public static List<Meaning> mergeMeanings(Iterable<Meaning> oldMeanings, Iterable<Meaning> newMeanings) {

        Set<Meaning> dedupMeanings = new HashSet();

        for (Meaning m1 : oldMeanings) {
            dedupMeanings.add(m1);
        }

        for (Meaning m2 : newMeanings) {
            dedupMeanings.add(m2);
        }

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

        return ImmutableList.copyOf(mgs);
    }

    /**
     * Converts provided span to a Guava Range of the [start, end) form.
     */
    public static Range spanToRange(Span span) {
        return Range.closedOpen(span.getStart(), span.getEnd());
    }

    /**
     * Returns a copy of provided metadata with the newMetadata set under the
     * given namespace.
     *
     * @param newMetadata Must be an immutable object.
     */
    static ImmutableMap<String, ?> replaceMetadata(ImmutableMap<String, ?> metadata, String namespace, Object newMetadata) {
        ImmutableMap.Builder<String, Object> mapb = ImmutableMap.builder();
        for (String ns : metadata.keySet()) {
            if (!ns.equals(namespace)) {
                mapb.put(ns, metadata.get(ns));
            }
        }
        mapb.put(namespace, newMetadata);
        return mapb.build();
    }
}
