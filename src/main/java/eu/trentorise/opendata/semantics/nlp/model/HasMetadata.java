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

import com.google.common.collect.ImmutableMap;

/**
 *
 * @author David Leoni
 */
public interface HasMetadata {

    ImmutableMap<String,?> EMPTY  =  ImmutableMap.<String,Object>of();
    
    /**
     * Returns whether or not the sentence has getMetadata under the given
     * namespace.
     *
     * @see #getMetadata(java.lang.String)
     */
    public boolean hasMetadata(String namespace);

    /**
     * Returns metadata as immutable map.
     */
    public ImmutableMap<String, ?> getMetadata();        
    
    /**
     * Safe way to get metadata associated with the sentence for a given
     * namespace.
     *
     * @throws NotFoundException if no getMetadata is available for the given
     * namespace.
     * @see #hasMetadata(java.lang.String)
     */
    public Object getMetadata(String namespace);    
    
}