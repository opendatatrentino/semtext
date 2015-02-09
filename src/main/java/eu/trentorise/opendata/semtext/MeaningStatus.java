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

/**
 * Represents the status of a meaning assigned to a {@link Term}
 *
 * @author David Leoni <david.leoni@unitn.it>
 *
 */
public enum MeaningStatus {

    /**
     * A meaning has been selected by the system, but the user should still
     * review it. Selected meaning must have a valid id.
     */
    SELECTED,
    /**
     * There is no selected meaning and entity/concept should be disambiguated by the user.
     */
    TO_DISAMBIGUATE,
    /**
     * The user reviewed the meaning and either accepted the meaning selected by
     * the system or indicated another one.
     */
    REVIEWED,
    
    /**
     * The user indicated that the entity/concept has candidates that are too
     * similar in the knowledge base, so there is no selected meaning.
     */
    NOT_SURE        

}
