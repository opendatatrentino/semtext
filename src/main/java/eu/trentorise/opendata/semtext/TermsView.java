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
import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable view for terms in a SemText. Officially supported methods are only iterator(),
 * get(), isEmpty(), size(), contains() and containsAll().
 *
 * NOTE: this is just a view to ease traversal, if you need a proper list of
 * terms please build one by yourself.
 *
 * @author David Leoni
 */
@Immutable
@ParametersAreNonnullByDefault
final class TermsView implements List<Term> {

    private SemText semText;

    private TermsView() {
        this.semText = SemText.of();
    }

    private TermsView(SemText semText) {
        this();
        checkNotNull(semText);
        this.semText = semText;
    }

    @Override
    public TermIterator iterator() {
        return new TermIterator(semText);
    }

    @Override
    public int size() {
        int ret = 0;
        for (Sentence sen : semText.getSentences()) {
            ret += sen.getTerms().size();
        }
        return ret;
    }

    public static TermsView of(SemText semText) {
        return new TermsView(semText);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (TermIterator iter = iterator(); iter.hasNext();) {
            Term term = iter.next();
            if (term.equals(o)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean containsAll(Collection<?> clctn) {
        for (Object obj : clctn) {
            if (!(contains(obj))) {
                return false;
            }
        }
        return true;
    }
    

    @Override
    public Term get(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException("Tried to get term at negative index " + i + "!");
        }
        int count = 0;
        for (Sentence sen : semText.getSentences()) {
            int termsInSentence = sen.getTerms().size();
            if (i < count + sen.getTerms().size()) {
                return sen.getTerms().get(i - count);
            } else {
                count += termsInSentence;
            }
        }
        throw new IndexOutOfBoundsException("Tried to get term at index " + i + " , but semText has only " + semText.terms().size() + " terms");
    }

   @Override
    public Object[] toArray() {
        return Iterators.toArray(iterator(), Term.class);
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return (T[]) toArray();
    }    
    
    /**
     * @deprecated not supported.
     */
    @Override
    public ListIterator listIterator(int i) {
        throw new UnsupportedOperationException("Not supported.");
    }

 
    /**
     * @deprecated not supported.
     */
    @Override
    public boolean add(Term e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }


    /**
     * @deprecated not supported.
     */
    @Override
    public boolean addAll(Collection<? extends Term> clctn) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public boolean addAll(int i, Collection<? extends Term> clctn) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public boolean removeAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public boolean retainAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public Term set(int i, Term e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public void add(int i, Term e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public Term remove(int i) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public ListIterator<Term> listIterator() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @deprecated not supported.
     */
    @Override
    public List<Term> subList(int i, int i1) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.semText != null ? this.semText.hashCode() : 0);
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
        final TermsView other = (TermsView) obj;
        if (this.semText != other.semText && (this.semText == null || !this.semText.equals(other.semText))) {
            return false;
        }
        return true;
    }

    
    
}
