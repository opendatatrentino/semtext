package eu.trentorise.opendata.semtext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.trentorise.opendata.commons.NotFoundException;
import static eu.trentorise.opendata.semtext.SemTexts.checkSpan;
import static eu.trentorise.opendata.semtext.SemTexts.checkSpans;
import java.io.Serializable;
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
     * Creates a sentence of one term.
     */
    public static Sentence of(int startOffset, int endOffset, Term term) {
        
        
        return new Sentence(startOffset, endOffset, ImmutableList.of(term), HasMetadata.EMPTY);
    }

    /**
     * Creates a sentence.
     */
    public static Sentence of(int startOffset, int endOffset, Iterable<Term> terms) {
        return new Sentence(startOffset, endOffset, terms, HasMetadata.EMPTY);
    }

    /**
     * Creates a sentence of zero terms.
     */
    public static Sentence of(int startOffset, int endOffset) {
        return new Sentence(startOffset, endOffset, ImmutableList.<Term>of(), HasMetadata.EMPTY);
    }

    
    /**
     * Returns a copy of this object with the provided metadata set under the
     * given namespace.
     *
     * @param metadata Must be an immutable object.
     */
    public Sentence withMetadata(String namespace, Object metadata) {
        Sentence ret = new Sentence(this);        
        ret.metadata = SemTexts.replaceMetadata(this.metadata, namespace, metadata);
        return ret;
    }    
    
}
