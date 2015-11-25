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
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.LocalizedString;
import eu.trentorise.opendata.commons.TodUtils;
import eu.trentorise.opendata.semtext.exceptions.SemTextNotFoundException;

import static eu.trentorise.opendata.commons.TodUtils.checkNotEmpty;
import static eu.trentorise.opendata.semtext.SemTexts.spanToRange;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable lightweight class to hold semantically enriched text. Containes
 * sentences, which in turn can contain terms. Both are spans, and actual text
 * is only stored in root SemText object. Span offsets are always absolute and
 * calculated with respects to it. Spans can't overlap. All of semantic text
 * items (sentences, terms, meaning, semtext itself) can hold metadata.
 *
 * @author David Leoni <david.leoni@unitn.it>
 */
@Immutable
@ParametersAreNonnullByDefault
public final class SemText implements Serializable, HasMetadata {

    private static final Logger LOG = Logger.getLogger(SemText.class.getName());

    private static final long serialVersionUID = 1L;

    private static final SemText INSTANCE = new SemText();

    private String text;
    private Locale locale;

    private ImmutableList<Sentence> sentences;
    private ImmutableMap<String, ?> metadata;

    /**
     * Text getLocale is set to {Locale#ROOT}
     */
    private SemText() {
        this.text = "";
        this.locale = Locale.ROOT;
        this.sentences = ImmutableList.of();
        this.metadata = ImmutableMap.of();
    }

    /**
     * @param locale if unknown use {@link Locale#ROOT}. If null is passed it
     * will be automatically converted to Locale.ROOT.
     */
    private SemText(@Nullable Locale locale, String text, Iterable<Sentence> sentences, Map<String, ?> metadata) {
        this();
        checkNotNull(text);
        checkNotNull(sentences);
        checkNotNull(metadata);
        if (locale == null) { // little remedy for nasty deserializers that cast "" into null.
            this.locale = Locale.ROOT;
        } else {
            this.locale = locale;
        }

        this.text = text;
        this.sentences = ImmutableList.copyOf(sentences);
        this.metadata = ImmutableMap.copyOf(metadata);
    }

    private SemText(SemText semText) {
        this.locale = semText.getLocale();
        this.text = semText.getText();
        this.sentences = semText.getSentences();
        this.metadata = semText.getMetadata();
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
            throw new SemTextNotFoundException("There is no metadata under the namespace " + namespace + " in " + this);
        } else {
            return ret;
        }
    }

    /**
     * Gets the sentences of the text
     *
     * @return the sentences in which the text is divided.
     */
    public ImmutableList<Sentence> getSentences() {
        return sentences;
    }

    /**
     * Gets the language of the whole text
     *
     * @return the locale of the whole text. If locale is unknown,
     * {@link Locale#ROOT} is returned.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the original text
     *
     * @return the whole text, without annotations
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the text corresponding to of a given span.
     */
    public String getText(Span span) {
        return text.substring(span.getStart(), span.getEnd());
    }

    /**
     * Returns an terms that walks through all the terms, regardless of the
     * sentences.
     */
    public List<Term> terms() {
        return TermsView.of(this);
    }

    /**
     * Returns a new SemText with all the terms matching the provided regex text
     * deleted.
     *
     */
    public SemText deleteTerms(Pattern pattern) {
        checkNotNull(pattern);
        checkNotEmpty(pattern.pattern(), "Pattern can't be empty!!");

        Matcher m = pattern.matcher(text);
        List ranges = new ArrayList();
        while (m.find()) {
            ranges.add(Range.closedOpen(m.start(), m.end()));
        }
        return deleteTerms(ranges);
    }

    /**
     * Returns a copy of the this SemText without terms intersecting provided
     * ranges.
     *
     */
    // note: Current version is inefficient, tried RangeMap.remove, but it only removes matching subsegments! 
    public SemText deleteTerms(Iterable<Range<Integer>> deletionRanges) {
        checkNotNull(deletionRanges);

        ImmutableList.Builder<Sentence> sentencesB = ImmutableList.builder();

        RangeSet<Integer> rangeSet = TreeRangeSet.create();

        for (Range r : deletionRanges) {
            rangeSet.add(r);
        }

        for (Sentence sentence : sentences) {
            ImmutableList.Builder<Term> termsB = ImmutableList.builder();
            for (Term term : sentence.getTerms()) {
                RangeSet<Integer> intersection = rangeSet.subRangeSet(SemTexts.spanToRange(term));
                if (intersection.isEmpty()) {
                    termsB.add(term);
                }

            }
            sentencesB.add(sentence.withTerms(termsB.build()));
        }

        return this.withSentences(sentencesB.build());
    }

    /**
     * @see #merge(java.lang.Iterable)
     */
    public SemText merge(Term... terms) {
        return merge(Arrays.asList(terms));
    }

    /**
     * Returns the terms enclosed by enclosingSpan as a new TreeRangeMap.
     */
    private static TreeRangeMap<Integer, Term> enclosingNewTerms(Span enclosingSpan, RangeMap<Integer, Term> termRanges) {
        TreeRangeMap<Integer, Term> ret = TreeRangeMap.create();

        Range enclosingRange = spanToRange(enclosingSpan);

        RangeMap<Integer, Term> subRangeMap = termRanges.subRangeMap(enclosingRange);

        for (Map.Entry<Range<Integer>, Term> termEntry : subRangeMap.asMapOfRanges().entrySet()) {
            if (enclosingRange.encloses(spanToRange(termEntry.getValue()))) {
                ret.put(termEntry.getKey(), termEntry.getValue());
            }
        }

        return ret;
    }

    /**
     * Returns a new semantic text having existing terms plus the provided ones.
     * If new terms overlaps with other ones, existing overlapping terms are
     * removed. If a term is precisely overlapping an existing one, resulting
     * term will have the selected meaning and meaning status of the provided
     * term and the list of meanings will be a merge of the meanings found in
     * the provided term plus the meanings of the existing term.
     *
     * Terms to merge which are outside of existing sentences will be ignored.
     */
    // note: Current version is based on RangeMaps thus quite inefficient
    public SemText merge(Iterable<Term> termsToMerge) {

        SemTexts.checkSpans(termsToMerge, 0, text.length(), "Invalid spans for terms to merge!");

        if (sentences.size() > 1) {
            LOG.log(Level.WARNING, "Found more than one sentence while mergin terms into SemText {0}, output semtext will have only one sentence covering the whole text.", this.text);
        }

        ImmutableList.Builder<Sentence> newSentenceB = ImmutableList.builder();

        RangeMap<Integer, Term> termToMergeRanges = TreeRangeMap.create();

        for (Term termToMerge : termsToMerge) {
            termToMergeRanges.put(spanToRange(termToMerge), termToMerge);
        }

        for (Sentence sentence : sentences) {

            RangeMap<Integer, Term> mergeRanges = enclosingNewTerms(sentence, termToMergeRanges);
            Map<Range<Integer>, Term> mergeRangesMap = mergeRanges.asMapOfRanges();

            for (Term origTerm : sentence.getTerms()) {
                Term newTerm = mergeRangesMap.get(spanToRange(origTerm));

                if (newTerm == null) { // orig term doesn't coincide with new term
                    if (mergeRanges.subRangeMap(spanToRange(origTerm)).asMapOfRanges().isEmpty()) {
                        // origTerm does not overlap with new terms, add it                                                    
                        mergeRanges.put(spanToRange(origTerm), origTerm);
                    }
                } else { // orig term coincides with new term, merge it
                    mergeRanges.put(spanToRange(newTerm),
                            newTerm.with(Iterables.concat(newTerm.getMeanings(), origTerm.getMeanings())));
                }

            }

            newSentenceB.add(sentence.withTerms(mergeRangesMap.values()));

        }
        return withSentences(newSentenceB.build());
    }

    /**
     * Returns a copy of this object with the provided lcoale set.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     */
    public SemText with(Locale locale) {
        checkNotNull(locale);

        SemText ret = new SemText(this);
        ret.locale = locale;
        return ret;
    }

    /**
     * Returns a copy of this object with the provided text set.
     */
    public SemText with(String text) {
        checkNotNull(text);

        SemText ret = new SemText(this);
        ret.text = text;

        if (!sentences.isEmpty()) {
            int lastSentenceEnd = Iterables.getLast(sentences).getEnd();
            if (lastSentenceEnd > text.length()) {
                throw new IllegalArgumentException("Tried to change text of semantic text, but last sentence end " + lastSentenceEnd + " exceeds provided text length! Semtext is\n:" + this + " \n new text to set is: " + text);
            }
        }

        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.text != null ? this.text.hashCode() : 0);
        hash = 29 * hash + (this.locale != null ? this.locale.hashCode() : 0);
        hash = 29 * hash + (this.sentences != null ? this.sentences.hashCode() : 0);
        hash = 29 * hash + (this.metadata != null ? this.metadata.hashCode() : 0);
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
        final SemText other = (SemText) obj;
        if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
            return false;
        }
        if (this.locale != other.locale && (this.locale == null || !this.locale.equals(other.locale))) {
            return false;
        }
        if (this.sentences != other.sentences && (this.sentences == null || !this.sentences.equals(other.sentences))) {
            return false;
        }
        if (this.metadata != other.metadata && (this.metadata == null || !this.metadata.equals(other.metadata))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SemText{" + "text=" + text + ", locale=" + locale + ", sentences=" + sentences + ", metadata=" + metadata + '}';
    }

    /**
     * Returns a copy of this object with the provided terms set under one
     * sentence spanning the whole text. Terms existing in the current object
     * won't be copied.
     */
    public SemText withTerms(Iterable<Term> terms) {
        SemText ret = new SemText(this);
        ret.sentences = ImmutableList.of(Sentence.of(0, text.length(), terms));
        return ret;
    }

    /**
     * Returns a copy of this object with the provided terms set under one
     * sentence spanning the whole text. Terms existing in the current object
     * will be ignored.
     */
    public SemText withSentences(Iterable<Sentence> sentences) {
        SemText ret = new SemText(this);
        SemTexts.checkSpans(sentences, 0, text.length(), "Invalid sentences found!");
        ret.sentences = ImmutableList.copyOf(sentences);
        return ret;
    }

    /**
     * Returns a copy of this object with the provided metadata set under the
     * given namespace.
     *
     * @param metadata Must be an immutable object.
     */
    public SemText withMetadata(String namespace, Object metadata) {
        SemText ret = new SemText(this);
        ret.metadata = TodUtils.putKey((Map<String, Object>) this.metadata, namespace, metadata);
        return ret;
    }

    /**
     * Returns empty semantic text with unknown locale {@link Locale#ROOT}
     */
    public static SemText of() {
        return INSTANCE;
    }

    /**
     * Creates a semantic text of one term with only one meaning.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     * @param meaningStatus Must have a corresponding correct
     * {@code selectedMeaning}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param selectedMeaning Must have a corresponding correct
     * {@code meaningStatus}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     */
    public static SemText of(Locale locale,
            String text,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning) {
        return SemText.ofSentences(locale,
                text,
                ImmutableList.of(Sentence.of(0,
                                text.length(),
                                Term.of(0, text.length(),
                                        meaningStatus,
                                        selectedMeaning,
                                        ImmutableList.<Meaning>of()))),
                SemTexts.EMPTY_METADATA);
    }

    /**
     * Returns a semantic text made of one sentence and one term spanning the
     * whole text.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     * @param meaningStatus Must have a corresponding correct
     * {@code selectedMeaning}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param selectedMeaning Must have a corresponding correct
     * {@code meaningStatus}. See
     * {@link SemTexts#checkMeaningStatus(eu.trentorise.opendata.semtext.MeaningStatus, eu.trentorise.opendata.semtext.Meaning, java.lang.Object) SemTexts.checkMeaningStatus}
     * method.
     * @param meanings a list of suggested meanings. First one is the most
     * probable. Internally a new copy of meaning probabilities will be created
     * and normalized.
     *
     */
    public static SemText of(Locale locale,
            String text,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            Iterable<Meaning> meanings) {
        return SemText.ofSentences(
                locale,
                text,
                ImmutableList.of(Sentence.of(0,
                                text.length(),
                                Term.of(0, text.length(), meaningStatus, selectedMeaning, meanings))),
                SemTexts.EMPTY_METADATA);
    }

    /**
     * Returns the sem text as a localized string
     */
    public LocalizedString asLocalizedString() {
        return LocalizedString.of(locale, text);
    }

    /**
     * Returns the sem text as a dictionary with one entry.
     */
    public Dict asDict() {
        return Dict.of(locale, text);
    }

    /**
     * Creates SemText with the given string. Locale is set to
     * {@link Locale#ROOT}
     */
    public static SemText of(String text) {
        return SemText.ofSentences(Locale.ROOT, text, ImmutableList.<Sentence>of(), SemTexts.EMPTY_METADATA);
    }

    /**
     * Creates SemText with the given string. Locale is set to english and
     * the string is not enriched.
     *
     * @param text
     * @param locale The getLocale of the text. If unknown use
     * {@link Locale#ROOT}.
     */
    public static SemText of(Locale locale, String text) {
        return SemText.ofSentences(locale, text, ImmutableList.<Sentence>of(), SemTexts.EMPTY_METADATA);
    }




    /**
     * Creates a SemText with given sentences
     *
     * @param locale if unknown use {@link Locale#ROOT}
     */
    public static SemText ofSentences(Locale locale, String text, Iterable<Sentence> sentences) {
        return SemText.ofSentences(locale, text, sentences, SemTexts.EMPTY_METADATA);
    }

    /**
     * Creates a SemText with given sentences
     *
     * @param locale if unknown use {@link Locale#ROOT}
     */
    public static SemText of(Locale locale, String text, Sentence... sentences) {
        return SemText.ofSentences(locale, text, ImmutableList.copyOf(sentences));
    }    
    
    /**
     * Creates a SemText with provided sentences and metadata
     *
     * @param locale if unknown use {@link Locale#ROOT}
     * @param metadata a map of immutable objects.
     */
    public static SemText ofSentences(Locale locale, String text, Iterable<Sentence> sentences, Map<String, ?> metadata) {
        return new SemText(locale, text, sentences, metadata);
    }

    

    /**
     * Creates a SemText with provided terms, which will be put into a
     * {@link Sentence} spanning the whole text.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     */
    public static SemText of(Locale locale, String text, Term... terms) {
        return SemText.ofTerms(locale, text, ImmutableList.copyOf(terms));
    }
    
    /**
     * Creates a SemText with provided terms, which will be put into a
     * {@link Sentence} spanning the whole text.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     */
    public static SemText ofTerms(Locale locale, String text, Iterable<Term> terms) {
        return new SemText(locale,
                text,
                ImmutableList.of(Sentence.of(0, text.length(), terms)),
                SemTexts.EMPTY_METADATA);
    }

    /**
     * Creates a SemText with provided terms, which will be put into a
     * {@link Sentence} spanning the whole text.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     * @param metadata a map of immutable objects.
     */
    public static SemText ofTerms(Locale locale, String text, Iterable<Term> terms, Map<String, ?> metadata) {
        return new SemText(locale,
                text,
                ImmutableList.of(Sentence.of(0, text.length(), terms)),
                metadata);
    }    
    
    /**
     * Tries its best to return a meaningful semantic text in one of the
     * provided languages.
     *
     * @see eu.trentorise.opendata.commons.Dict#anyString(java.lang.Iterable)
     */
    public static SemText of(Dict dict, Iterable<Locale> locales) {
        return SemText.of(dict.anyString(locales));
    }

    /**
     * Tries its best to return a meaningful semantic text in one of the
     * provided languages.
     *
     * @see eu.trentorise.opendata.commons.Dict#anyString(java.lang.Iterable)
     */
    public static SemText of(Dict dict, Locale... locales) {
        return SemText.of(dict.anyString(locales));
    }

    /**
     * Creates a semtext out of the provided localized string.
     */
    public static SemText of(LocalizedString string) {
        return SemText.of(string.getLocale(), string.getString());
    }

}
