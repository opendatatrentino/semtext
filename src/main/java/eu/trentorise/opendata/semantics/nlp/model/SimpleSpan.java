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
package eu.trentorise.opendata.semantics.nlp.model;

/**
 *
 * @author David Leoni
 */
public final class SimpleSpan implements Span {

    private int start;
    private int end;

    private SimpleSpan(int start, int end) {
        SemTexts.checkSpan(start, end, "Invalid span!");
        this.start = start;
        this.end = end;
    }
           
    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }
    
    public static SimpleSpan of(int start, int end){
        return new SimpleSpan(start, end);
    }

    @Override
    public String toString() {
        return "SimpleSpan{" + "start=" + start + ", end=" + end + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.start;
        hash = 29 * hash + this.end;
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
        final SimpleSpan other = (SimpleSpan) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return true;
    }
    
    
}
