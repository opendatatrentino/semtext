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
import com.google.common.collect.UnmodifiableIterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * Iterator for all the terms in the text (regardless of the sentences). Allows
 * also getting the current term/sentence without moving the iterator. Does not
 * support remove operation.
 */
public class TermIterator extends UnmodifiableIterator<Term> {

    private UnmodifiableIterator<Sentence> sentenceIter;
    private UnmodifiableIterator<Term> termIter;

    @Nullable
    private Sentence currentSentence;
    @Nullable
    private Term currentTerm;

    private TermIterator() {
        super();
        currentTerm = null;
        currentSentence = null;
    }

    public TermIterator(SemText semtext) {
        this();

        checkNotNull(semtext);
        this.sentenceIter = semtext.getSentences().iterator();
        if (sentenceIter.hasNext()) {
            currentSentence = sentenceIter.next();
            this.termIter = currentSentence.getTerms().iterator();
        }
    }

    public boolean hasCurrentTerm() {
        return currentTerm != null;
    }

    public boolean hasCurrentSentence() {
        return currentSentence != null;
    }

    /**
     * Returns the term currently pointed by the iterator. Call to this method
     * doesn't moves the iterator.
     *
     * @throws NoSuchElementException if there is no current term in the
     * iterator
     * @see #hasCurrentTerm()
     */
    public Term term() {
        if (currentTerm == null) {
            throw new NoSuchElementException("There is no current term in the iterator!");
        } else {
            return currentTerm;
        }
    }

    /**
     * Returns the sentence currently pointed by the iterator. Call to this
     * method doesn't moves the iterator.
     *
     * @throws NoSuchElementException if there is no current sentence in the
     * iterator
     * @see #hasCurrentSentence()
     */
    public Sentence sentence() {
        if (currentSentence == null) {
            throw new NoSuchElementException("There is no current sentence in the iterator!");
        } else {
            return currentSentence;
        }
    }

    @Override
    public boolean hasNext() {
        if (termIter != null && termIter.hasNext()) {
            return true;
        } else {
            while (sentenceIter.hasNext()) {
                termIter = sentenceIter.next().getTerms().iterator();
                if (termIter.hasNext()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Term next() {
        if (termIter != null && termIter.hasNext()) {
            currentTerm = termIter.next();
            return currentTerm;
        } else {
            while (sentenceIter.hasNext()) {
                currentSentence = sentenceIter.next();
                termIter = currentSentence.getTerms().iterator();
                if (termIter.hasNext()) {
                    currentTerm = termIter.next();
                    return currentTerm;
                }
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final TermIterator other = (TermIterator) obj;
        if (this.sentenceIter != other.sentenceIter && (this.sentenceIter == null || !this.sentenceIter.equals(other.sentenceIter))) {
            return false;
        }
        if (this.termIter != other.termIter && (this.termIter == null || !this.termIter.equals(other.termIter))) {
            return false;
        }
        if (this.currentSentence != other.currentSentence && (this.currentSentence == null || !this.currentSentence.equals(other.currentSentence))) {
            return false;
        }
        if (this.currentTerm != other.currentTerm && (this.currentTerm == null || !this.currentTerm.equals(other.currentTerm))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TermIterator{" + "sentenceIter=" + sentenceIter + ", termIter=" + termIter + '}';
    }

}
