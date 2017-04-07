/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import java.util.Collection;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface LXSerializable {
    public void save(LX lx, JsonObject object);
    public void load(LX lx, JsonObject object);

    public static class Utils {

        public static JsonObject toObject(LX lx, LXSerializable serializable) {
            JsonObject obj = new JsonObject();
            serializable.save(lx,  obj);
            return obj;
        }

        public static JsonObject toObject(LX lx, Map<String, ? extends LXSerializable> serializables) {
            JsonObject map = new JsonObject();
            for (String key : serializables.keySet()) {
                JsonObject obj = new JsonObject();
                serializables.get(key).save(lx, obj);
                map.add(key, obj);
            }
            return map;
        }

        public static JsonArray toArray(LX lx, LXSerializable[] serializables) {
            JsonArray arr = new JsonArray();
            for (LXSerializable serializable : serializables) {
                arr.add(toObject(lx, serializable));
            }
            return arr;
        }

        public static JsonArray toArray(LX lx, Collection<? extends LXSerializable> serializables) {
            JsonArray arr = new JsonArray();
            for (LXSerializable serializable : serializables) {
                arr.add(toObject(lx, serializable));
            }
            return arr;
        }
    }

}
