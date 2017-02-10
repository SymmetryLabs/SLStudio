/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.osc;

public class OscMalformedDataException extends OscException {

    private static final long serialVersionUID = 1L;

    private final byte[] data;
    private final int offset;
    private final int len;

    OscMalformedDataException(String message, byte[] data, int offset, int len) {
        super(message);
        this.data = data;
        this.offset = offset;
        this.len = len;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLength() {
        return this.len;
    }

}