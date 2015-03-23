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
 * A span of absolute offsets pairs obeying the constraint 0 ≤ start ≤ end. In
 * range notation span semantics is expressed as [start, end), that is, start
 * offset is closed, while end offset is open.
 *
 * @author David Leoni
 */
public interface Span {

    /**
     * An absolute start offset based on the text of the SemText containing this
     * span. The offset is positioned at the first character of the span, so for
     * span getStart would be 0.
     * <pre>
     * I see
     * 012345
     * </pre>
     *
     * In the special case of empty span, getStart and getEnd must concide.
     */
    public int getStart();

    /**
     * An absolute getEnd offset based on the text of the SemText containing
     * this span. The offset is positioned *after* the last character of the
     * sentence, so for span
     * <pre>
     * I see
     * 012345
     * </pre>
     *
     * getEnd would be 5. In the special case of empty span, getStart and getEnd
     * must concide.
     */
    public int getEnd();
}
