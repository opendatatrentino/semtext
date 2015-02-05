package eu.trentorise.opendata.semantics.nlp.model;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.LocalizedString;
import eu.trentorise.opendata.commons.NotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public final class SemText implements Serializable, HasMetadata, Span {

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
     * @param locale if unknown use {@link Locale#ROOT}
     */
    private SemText(String text, Locale locale, Iterable<Sentence> sentences, Map<String, ?> metadata) {
        this();
        checkNotNull(text);
        checkNotNull(locale);
        checkNotNull(sentences);
        checkNotNull(metadata);
        this.text = text;
        this.locale = locale;
        this.sentences = ImmutableList.copyOf(sentences);
        this.sentences = ImmutableList.copyOf(sentences);
        this.metadata = ImmutableMap.copyOf(metadata);
    }

    private SemText(SemText semText) {
        this.text = semText.getText();
        this.locale = semText.getLocale();
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
            throw new NotFoundException("There is no metadata under the namespace " + namespace + " in " + this);
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
     * @return the getLocale of the whole text. If getLocale is unknown,
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
    public TermIterator terms() {
        return new TermIterator(this);
    }

    /**
     * Returns a copy of the this SemText without terms intersecting provided
     * ranges.
     *
     * NOTE: Currently the method only works with text of one sentence! If it is
     * not an IllegalStateException will be thrown!.
     *
     */
    // note: Current version is inefficient, tried RangeMap.remove, but it only removes matching subsegments! 
    public SemText delete(Iterable<Range<Integer>> deletionRanges) {
        checkNotNull(deletionRanges);

        ImmutableList.Builder<Term> termsB = ImmutableList.builder();

        if (sentences.size() > 1) {
            throw new IllegalStateException("The merge method currently only works with text of at most one sentence!");
        }

        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        TermIterator termIter = terms();

        for (Range r : deletionRanges) {
            rangeSet.add(r);
        }

        while (termIter.hasNext()) {
            Term term = termIter.next();
            RangeSet<Integer> intersection = rangeSet.subRangeSet(SemTexts.spanToRange(term));
            if (intersection.isEmpty()) {
                termsB.add(term);
            }
        }

        return this.withTerms(termsB.build());
    }

    /**
     * @see #merge(java.lang.Iterable)
     */
    public SemText merge(Term... terms) {
        return merge(Arrays.asList(terms));
    }

    /**
     * Returns a new semantic text having existing terms plus the provided ones.
     * If new terms overlaps with other ones, existing overlapping terms are
     * removed. If a term is precisely overlapping an existing one, resulting
     * term will have the selected meaning and meaning status of the provided
     * term and the list of meanings will be a merge of the meanings found in
     * the provided term plus the meanings of the existing term.
     *
     * NOTE: Currently the method only works with text of one sentence! If it is
     * not an IllegalStateException will be thrown!.
     */
    public SemText merge(Iterable<Term> termsToMerge) {
        checkNotNull(termsToMerge);

        if (sentences.size() > 1) {
            throw new IllegalStateException("The merge method currently only works with text of at most one sentence!");
        }

        ImmutableList.Builder<Term> newTermsB = ImmutableList.builder();
        TermIterator origTermIter = terms();
        @Nullable
        Term curOrigTerm = origTermIter.hasNext() ? origTermIter.next() : null;

        Term lastTerm = null;

        for (Term termToAdd : termsToMerge) {
            while (curOrigTerm != null
                    && curOrigTerm.getEnd() <= termToAdd.getStart()) {
                newTermsB.add(curOrigTerm);
                curOrigTerm = origTermIter.hasNext() ? origTermIter.next() : null;
            }
            if (curOrigTerm != null // terms coincide, we merge
                    && termToAdd.getStart() == curOrigTerm.getStart()
                    && termToAdd.getEnd() == curOrigTerm.getEnd()) {

                newTermsB.add(termToAdd.with(Iterables.concat(termToAdd.getMeanings(), curOrigTerm.getMeanings())));

            } else { // terms don't coincide
                newTermsB.add(termToAdd);
            }
            while (curOrigTerm != null
                    && curOrigTerm.getStart() < termToAdd.getEnd()) {
                curOrigTerm = origTermIter.hasNext() ? origTermIter.next() : null;
            }
            lastTerm = termToAdd;
        }

        if (lastTerm == null) {
            return this;
        } else {
            while (curOrigTerm != null
                    && curOrigTerm.getStart() >= lastTerm.getEnd()) {
                newTermsB.add(curOrigTerm);
                curOrigTerm = origTermIter.hasNext() ? origTermIter.next() : null;
            }
            return this.withTerms(newTermsB.build());
        }

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
        for (Sentence s : sentences) {
            if (s.getEnd() > text.length()) {
                throw new IllegalArgumentException("Tried to change text of semantic text, but there is a sentence longer thean the provided text! Semtext is\n:" + this + " new text to set is: " + text);
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
        ret.metadata = SemTexts.replaceMetadata(this.metadata, namespace, metadata);
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
     */
    public static SemText of(String text,
            Locale locale,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning) {
        return new SemText(text,
                locale,
                ImmutableList.of(Sentence.of(0,
                                text.length(),
                                Term.of(0, text.length(),
                                        meaningStatus,
                                        selectedMeaning,
                                        ImmutableList.<Meaning>of()))),
                HasMetadata.EMPTY);
    }

    /**
     * Returns a semantic text made of one sentence and one term spanning the
     * whole text.
     *
     * @param locale if unknown use {@link Locale#ROOT}
     * @param selectedMeaning if unknown use null
     * @param meanings a list of suggested meanings. First one is the most
     * probable. Meaning propabilities will be normalized.
     *
     */
    public static SemText of(String text,
            Locale locale,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            List<Meaning> meanings) {
        return new SemText(text,
                locale,
                ImmutableList.of(Sentence.of(0,
                                text.length(),
                                Term.of(0, text.length(), meaningStatus, selectedMeaning, meanings))),
                HasMetadata.EMPTY);
    }

    /**
     * Creates SemText with the provided string. Locale is set to
     * {@link Locale#ROOT}
     */
    public static SemText of(String text) {
        return new SemText(text, Locale.ROOT, ImmutableList.<Sentence>of(), HasMetadata.EMPTY);
    }

    /**
     * Creates SemText with the provided string. Locale is set to english and
     * the string is not enriched.
     *
     * @param text
     * @param locale The getLocale of the text. If unknown use
     * {@link Locale#ROOT}.
     */
    public static SemText of(String text, Locale locale) {
        return new SemText(text, locale, ImmutableList.<Sentence>of(), HasMetadata.EMPTY);
    }

    /**
     * @param locale if unknown use {@link Locale#ROOT}
     */
    public static SemText of(String text, Locale locale, Sentence... sentences) {
        return ofSentences(text, locale, ImmutableList.copyOf(sentences));
    }

    /**
     * @param locale Locale of the whole text. if unknown use
     * {@link Locale#ROOT}
     */
    public static SemText ofSentences(String text, Locale locale, Iterable<Sentence> sentences) {
        return new SemText(text, locale, sentences, HasMetadata.EMPTY);
    }

    /**
     * @param locale Locale of the whole text. if unknown use
     * {@link Locale#ROOT}
     */
    public static SemText ofTerms(String text, Locale locale, Iterable<Term> terms) {
        return new SemText(text,
                locale,
                ImmutableList.of(Sentence.of(0, text.length(), terms)),
                HasMetadata.EMPTY);
    }

    /**
     * @param locale Locale of the whole text. if unknown use
     * {@link Locale#ROOT}
     */
    public static SemText ofTerms(String text, Locale locale, Term... terms) {
        return ofTerms(text, locale, ImmutableList.copyOf(terms));
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
     * Converts provided localized string into a SemText.
     */
    public static SemText of(LocalizedString string) {
        return SemText.of(string.getString(), string.getLocale());
    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    public int getEnd() {
        return text.length();
    }

}
