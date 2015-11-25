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
package eu.trentorise.opendata.semtext.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.trentorise.opendata.semtext.HasMetadata;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author David Leoni
 */
class MetadataDeserializer extends StdDeserializer<Map<String, Object>> {

    private Class<? extends HasMetadata> hasMetadataClass;
   
    protected MetadataDeserializer(Class<? extends HasMetadata> hasMetadataClass) {
        super(Map.class);
        this.hasMetadataClass = hasMetadataClass;
    }

    @Override
    public Map<String, Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        ImmutableMap.Builder<String, Object> retb = ImmutableMap.builder();

        while (jp.nextToken() != JsonToken.END_OBJECT) {

            String namespace = jp.getCurrentName();
            // current token is "name",
            // move to next, which is "name"'s value
            jp.nextToken();

            ImmutableSet<String> namespaces = SemTextModule.getMetadataNamespaces(hasMetadataClass);
            if (namespaces.contains(namespace)) {
                TypeReference typeRef = SemTextModule.getMetadataTypeReference(hasMetadataClass, namespace);

                Object metadata;
                
                try {
                    metadata = jp.readValueAs(typeRef);
                }
                catch (Exception ex){
                    throw new SemTextMetadataException("Jackson error while deserializing metadata - ", hasMetadataClass, namespace, typeRef,  ex);
                }
                
                if (metadata == null){
                    throw new SemTextMetadataException("Found null metadata while deserializing!", hasMetadataClass, namespace, typeRef);
                }                                  
                
                retb.put(namespace, metadata);
            } else {
                throw new SemTextMetadataException("Found metadata under not registered namespace while deserializing!",hasMetadataClass, namespace, null);
            }
        }

        return retb.build();
    }

}
