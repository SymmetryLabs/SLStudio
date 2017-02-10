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

public class OscBlob implements OscArgument {
    private int byteLength;
    private byte[] data;

    public OscBlob(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Cannot pass null array to OscBlob constructor");
        }
        setData(data);
    }

    public byte[] getData() {
        return this.data;
    }

    public OscBlob setData(byte[] data) {
        this.data = data;
        this.byteLength = 4 + data.length;
        while (this.byteLength % 4 > 0) {
            ++this.byteLength;
        }
        return this;
    }

    public int getByteLength() {
        return this.byteLength;
    }

    @Override
    public char getTypeTag() {
        return OscTypeTag.BLOB;
    }
}
