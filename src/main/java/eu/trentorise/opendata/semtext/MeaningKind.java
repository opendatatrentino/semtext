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

/**
 * The kind of a meaning.
 *
 * @author David Leoni <david.leoni@unitn.it>
 */
public enum MeaningKind {

    /**
     * An entity is something so important to us to be referred to it with a
     * name, for example 'New York City', 'Albert Einstein', 'W3C'.
     */
    ENTITY,
    /**
     * A concept is a group of objects referred with a common name, for example
     * 'city', 'person', 'organization'.
     */
    CONCEPT,
    /**
     * It is not known whether the meaning represents an entity or a
     * concept.
     */
    UNKNOWN;
}
