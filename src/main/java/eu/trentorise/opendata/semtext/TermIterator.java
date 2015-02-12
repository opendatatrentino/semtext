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

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.UnmodifiableIterator;
import java.util.NoSuchElementException;

/**
 * Iterator for all the terms in the text (regardless of the sentences). Does
 * not support remove operation.
 */
public class TermIterator extends UnmodifiableIterator<Term> {

    private UnmodifiableIterator<Sentence> sentenceIter;
    private UnmodifiableIterator<Term> termIter;

    public TermIterator(SemText semtext) {
        checkNotNull(semtext);
        this.sentenceIter = semtext.getSentences().iterator();
        if (sentenceIter.hasNext()) {
            this.termIter = sentenceIter.next().getTerms().iterator();
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
            return termIter.next();
        } else {
            while (sentenceIter.hasNext()) {
                termIter = sentenceIter.next().getTerms().iterator();
                if (termIter.hasNext()) {
                    return termIter.next();
                }
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.sentenceIter != null ? this.sentenceIter.hashCode() : 0);
        hash = 29 * hash + (this.termIter != null ? this.termIter.hashCode() : 0);
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
        return true;
    }

    @Override
    public String toString() {
        return "TermIterator{" + "sentenceIter=" + sentenceIter + ", termIter=" + termIter + '}';
    }

}
