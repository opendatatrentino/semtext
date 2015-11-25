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
package eu.trentorise.opendata.semtext.test.jackson;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Example of a possible immutable metadata object. Notice it doesn't have any
 * Jackson tags for serialization, to separate concerns they can be put in a
 * Jackson mixin class like {@link MyMetadataJackson}
 *
 * @author David Leoni
 */
@Immutable
@ParametersAreNonnullByDefault
public class MyMetadata {

    private final static MyMetadata INSTANCE = new MyMetadata();

    private String field;

    private MyMetadata() {
        field = "";
    }

    public String getField() {
        return field;
    }

    private MyMetadata(String field) {
        checkNotNull(field);
        this.field = field;
    }

    public static MyMetadata of() {
        return INSTANCE;
    }

    public static MyMetadata of(String field) {
        checkNotNull(field);
        return new MyMetadata(field);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.field != null ? this.field.hashCode() : 0);
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
        final MyMetadata other = (MyMetadata) obj;
        if ((this.field == null) ? (other.field != null) : !this.field.equals(other.field)) {
            return false;
        }
        return true;
    }

}
