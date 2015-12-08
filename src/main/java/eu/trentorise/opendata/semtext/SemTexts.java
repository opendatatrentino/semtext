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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.TodUtils;
import eu.trentorise.opendata.semtext.exceptions.SemTextNotFoundException;

import static eu.trentorise.opendata.commons.validation.Preconditions.checkNotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Utilities toolbox for semtexts.
 *
 * @author David Leoni
 */
public final class SemTexts {

	private SemTexts() {
	}

	/**
	 * Convenience instance for empty metadata
	 */
	public static final ImmutableMap<String, ?> EMPTY_METADATA = ImmutableMap.<String, Object> of();

	/**
	 * Tolerance for probabilities
	 */
	public static final double TOLERANCE = 0.001;

	/**
	 * A meaning score must be {@code DISAMBIGUATION_FACTOR} times greater than
	 * any other meaning to be automatically considered as SELECTED. This factor
	 * can be used during automated conversions.
	 */
	static final double DISAMBIGUATION_FACTOR = 1.5;

	/**
	 * Determines the best meaning among the given ones according to their
	 * probabilities. If no best meaning is found null is returned.
	 *
	 * @param meanings
	 *            a list of meanings.
	 * @return the disambiguated meaning or null if no meaning can be clearly
	 *         identified.
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

		List<Meaning> mgs = Lists.newArrayList(meanings);
		Collections.sort(mgs, Collections.reverseOrder());
		Meaning first = mgs.get(0);

		if (first.getProbability() > DISAMBIGUATION_FACTOR / size && first.getId() != null) {
			return first;
		} else {
			return null;
		}

	}

	/**
	 * Checks that the provided couple meaning status / selected meaning is
	 * valid. For {@code SELECTED} and {@code REVIEWED} statuses there must be a
	 * {@code selectedMeaning} with valid non-empty id, while {@code TO_DISAMBIGUATE} and
	 * {@code NOT_SURE} statuses must have a {@code null selectedMeaning}.
	 *
	 * @param prependedErrorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using String.valueOf(Object) and
	 *            prepended to more specific error messages.
	 *
	 * @throws InvalidArgumentException
	 *             on invalid meaning status / selectedMeaning couple
	 */
	public static void checkMeaningStatus(@Nullable MeaningStatus meaningStatus, @Nullable Meaning selectedMeaning,
			@Nullable Object prependedErrorMessage) {
		checkArgument(meaningStatus != null, "%s -- meaningStatus is null!", prependedErrorMessage);
		if (MeaningStatus.SELECTED.equals(meaningStatus) || MeaningStatus.REVIEWED.equals(meaningStatus)) {
			checkArgument(selectedMeaning != null, String.valueOf(prependedErrorMessage)
					+ " -- Reason: Selected meaning can't be null when status is " + meaningStatus);
			checkNotEmpty(selectedMeaning.getId(), String.valueOf(prependedErrorMessage)
					+ " -- Reason: Selected meaning must have a valid id when status is " + meaningStatus);
		} else {
			if (selectedMeaning != null) {
				throw new IllegalArgumentException(String.valueOf(prependedErrorMessage)
						+ " -- Reason: Selected meaning must be null when meaning status is " + meaningStatus
						+ ". Found instead meaning " + selectedMeaning);
			}
		}
	}

	/**
	 *
	 * Checks whether provided score has tolerance above -{@link #TOLERANCE}
	 *
	 * @param prependedErrorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using String.valueOf(Object) and
	 *            prepended to more specific error messages.
	 *
	 * @return the provided score if positive within tolerance
	 * @throws IllegalArgumentException
	 *             on invalid score
	 */
	public static double checkPositiveScore(double score, @Nullable Object prependedErrorMessage) {
		if (score < -TOLERANCE) {
			throw new IllegalArgumentException(String.valueOf(prependedErrorMessage)
					+ " -- Reason: Score must be greater or equal than -" + TOLERANCE + ", found instead: " + score);
		}
		return score;
	}

	/**
	 * Checks the provided score is within valid bounds.
	 *
	 * @param score
	 *            must be between -{@link #TOLERANCE} ≤ score ≤ 1 +
	 *            {@link #TOLERANCE}
	 *
	 * @param prependedErrorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using String.valueOf(Object) and
	 *            prepended to more specific error messages.
	 *
	 * @throws IllegalArgumentException
	 *             on invalid score
	 */
	public static void checkScore(double score, @Nullable Object prependedErrorMessage) {
		double prec = TOLERANCE;

		if (score < -prec || score > 1.0 + prec) {
			throw new IllegalArgumentException(String.valueOf(prependedErrorMessage) + " -- Score " + score
					+ " exceeds bounds [" + (-prec) + ", " + 1.0 + prec + "].");
		}
	}

	/**
	 * Checks provided offsets represent a valid span.
	 *
	 * {@code startOffset} must be less or equal than {@code endOffset} and they
	 * must be both greater or equal than 0
	 *
	 * @param prependedErrorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using String.valueOf(Object) and
	 *            prepended to more specific error messages.
	 *
	 * @throws IllegalArgumentException
	 *             on invalid span.
	 */
	public static void checkSpan(int startOffset, int endOffset, @Nullable Object prependedErrorMessage) {
		checkArgument(startOffset >= 0 && startOffset <= endOffset, "%s -- Reason: invalid bounds [%s , %s)",
				prependedErrorMessage, startOffset, endOffset);
	}

	/**
	 *
	 * Checks spans are all be valid spans (see
	 * {@link SemTexts#checkSpan(int, int, Object) } and are non-overlapping (a
	 * span end offset may coincide with next span start offset). Spans must be
	 * contained within {@code leftOffset} and {@code rightOffset} (last span
	 * end offset may coincide with {@code rightOffset}).
	 *
	 * @param prependedErrorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using String.valueOf(Object) and
	 *            prepended to more specific error messages.
	 *
	 * @throws IllegalArgumentException
	 *             on invalid spans
	 */
	public static void checkSpans(Iterable<? extends Span> spans, int leftOffset, int rightOffset,
			@Nullable Object prependedErrorMessage) {

		checkArgument(spans != null, "%s -- spans are null!", prependedErrorMessage);
		checkSpan(leftOffset, rightOffset, prependedErrorMessage);

		// check containment
		if (!Iterables.isEmpty(spans)) {
			int lowerBound = Iterables.getFirst(spans, null).getStart();
			int upperBound = Iterables.getLast(spans).getEnd();
			if (lowerBound < leftOffset || upperBound > rightOffset) {
				throw new IllegalArgumentException(String.valueOf(prependedErrorMessage)
						+ " -- Reason: Provided spans exceed container span! Expected: [" + leftOffset + ","
						+ rightOffset + "] - Found: [" + lowerBound + "," + upperBound + "]");
			}
		}

		// check overlaps
		@Nullable
		Span lastSpan = null;
		for (Span span : spans) {
			checkSpan(span.getStart(), span.getEnd(), prependedErrorMessage);
			if (lastSpan != null && lastSpan.getEnd() > span.getStart()) {
			    if (lastSpan.getStart() >= span.getEnd()){
			        throw new IllegalArgumentException(String.valueOf(prependedErrorMessage)
	                        + " -- Found out-of-order span! Span:\n " + lastSpan + " is after span:\n " + span);    
			    } else {
			        throw new IllegalArgumentException(String.valueOf(prependedErrorMessage)
	                        + " -- Found overlapping span! Span:\n " + lastSpan + " overlaps with span:\n " + span);
			    }
				
			}
			lastSpan = span;
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
	 * Returns the provided dictionary as an immutable list of semantic texts
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
	 * A new immutable list of sorted meanings is returned with the provided
	 * meanings merged to the existing ones. The first one has highest prob and
	 * probabilities are normalized so they sum up to 1.0. If a new meaning
	 * equals an existing meaning it will replace it.
	 */
	public static ImmutableList<Meaning> mergeMeanings(Iterable<Meaning> oldMeanings, Iterable<Meaning> newMeanings) {

		Set<Meaning> dedupMeanings = new HashSet();

		for (Meaning m1 : oldMeanings) {
			dedupMeanings.add(m1);
		}

		for (Meaning m2 : newMeanings) {
			dedupMeanings.add(m2);
		}

		double total = 0;
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
	 * Returns whether the two provided spans have equal boundaries.
	 */
	public static boolean spanEqual(@Nullable Span span1, @Nullable Span span2) {
		if (span1 == null) {
			return span2 == null;
		}
		if (span2 == null) {
			return false;
		}
		return span1.getStart() == span2.getStart() && span1.getEnd() == span2.getEnd();
	}


	/**
	 * If the semtext contains only one term, it is returned (the termneeds not
	 * to be spanning the whole text). Throws exception otherwise .
	 *
	 * @throws SemTextNotFoundExcecption
	 *             if there are zero or more than one term.
	 *             
	 * @since 1.1.0
	 */
	public static Term term(SemText semtext) {
		checkNotNull(semtext);

		List<Term> terms = semtext.terms();
		if (terms.isEmpty()) {
			throw new SemTextNotFoundException("Couldn't find any term in semtext " + semtext);
		}
		Iterator<Term> iter = terms.iterator();
		Term ret = iter.next();
		if (iter.hasNext()) {
			throw new SemTextNotFoundException("There is more than one term in semtext " + semtext);
		}

		return ret;
	}

	/**
	 * Returns the meaning if semantic text is made of a single word spanning
	 * the whole text with a {@link MeaningStatus#SELECTED} or
	 * {@link MeaningStatus#REVIEWED} meaning, throws exception otherwise.
	 * 
	 * @throws SemTextNotFoundExcpetion
	 *             if meaning is not definite or ambiguous (too many meanings)
	 * @since 1.1.0
	 */

	public static Meaning meaning(SemText semtext) {
		checkNotNull(semtext);

		Term term = term(semtext);

		MeaningStatus meaningStatus = term.getMeaningStatus();

		if (MeaningStatus.SELECTED.equals(meaningStatus) || MeaningStatus.REVIEWED.equals(term.getMeaningStatus())) {
			return term.getSelectedMeaning();
		} else {
			throw new SemTextNotFoundException(
					"Tried to extract undefined meaning " + meaningStatus + " from semtext " + semtext);
		}
	}

	/**
	 *
	 * Returns a MeaningKind if text represents a single entity or concept,
	 * throws exception otherwise
	 * 
	 * <p>
	 * To be an entity or concept, the text must contain exactly one word. and
	 * the word must have a meaning status {@link MeaningStatus#SELECTED} or
	 * {@link MeaningStatus#REVIEWED}.
	 * </p>
	 * 
	 * @throws SemTextNotFoundException
	 *             if there are zero or more than one meanings
	 * 
	 * @see MeaningStatus
	 * @since 1.1.0
	 */
	public static MeaningKind meaningKind(SemText semText) {
		Term term = term(semText);
		if (MeaningStatus.SELECTED.equals(term.getMeaningStatus())
				|| MeaningStatus.REVIEWED.equals(term.getMeaningStatus())) {
			return term.getSelectedMeaning().getKind();
		}
		throw new SemTextNotFoundException("Couldn't find any meaning in semtext " + semText);
	}

}
