package eu.trentorise.opendata.semantics.nlp.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.trentorise.opendata.commons.NotFoundException;
import static eu.trentorise.opendata.semantics.nlp.model.Checker.checkSpan;
import static eu.trentorise.opendata.semantics.nlp.model.Checker.checkSpans;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;



/**
 * A sentence which can contain terms.
 *
 * @author David Leoni <david.leoni@unitn.it>
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Sentence implements Span, Serializable, HasMetadata {

    private int start;
    private int end;

    private ImmutableList<Term> terms;
    private ImmutableMap<String, ?> metadata;

    private Sentence() {
        this.start = 0;
        this.start = 0;
        this.terms = ImmutableList.of();
        this.metadata = ImmutableMap.of();
    }

    private Sentence(int start, int end, List<Term> terms, Map<String, ?> metadata) {
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
        hash = 37 * hash + this.start;
        hash = 37 * hash + this.end;
        hash = 37 * hash + (this.terms != null ? this.terms.hashCode() : 0);
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
        return true;
    }

    /**
     * Creates a sentence of one term.
     */
    public static Sentence of(int startOffset, int endOffset, Term term) {
        
        
        return new Sentence(startOffset, endOffset, ImmutableList.of(term), HasMetadata.EMPTY);
    }

    /**
     * Creates a sentence.
     */
    public static Sentence of(int startOffset, int endOffset, List<Term> terms) {
        return new Sentence(startOffset, endOffset, terms, HasMetadata.EMPTY);
    }

    /**
     * Creates a sentence of zero terms.
     */
    public static Sentence of(int startOffset, int endOffset) {
        return new Sentence(startOffset, endOffset, ImmutableList.<Term>of(), HasMetadata.EMPTY);
    }

}
