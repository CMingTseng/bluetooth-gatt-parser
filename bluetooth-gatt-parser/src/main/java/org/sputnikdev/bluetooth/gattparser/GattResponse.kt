package org.sputnikdev.bluetooth.gattparser

import org.sputnikdev.bluetooth.gattparser.FieldHolder
import java.util.*

/*-
 * #%L
 * org.sputnikdev:bluetooth-gatt-parser
 * %%
 * Copyright (C) 2017 Sputnik Dev
 * %%
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
 * #L%
 */ /**
 * Represents result of Bluetooth GATT characteristic deserialization. Defines some useful methods for accessing
 * deserialized field values in a user-friendly manner.
 *
 * @author Vlad Kolotov
 */
class GattResponse internal constructor(private val holders: LinkedHashMap<String, FieldHolder>) {
    /**
     * Returns field holders in this response as a Map (field name -&gt; field holder).
     * @return field holders
     */
    fun getHolders(): Map<String, FieldHolder> {
        return Collections.unmodifiableMap(holders)
    }

    /**
     * Returns a list of field names in this response.
     * @return a list of field names in this response
     */
    val fieldNames: Set<String>
        get() = holders.keys

    /**
     * Returns a list of field holders in this response
     * @return a list of field holders in this response
     */
    val fieldHolders: Collection<FieldHolder>
        get() = holders.values

    /**
     * Returns a field holder by its field name
     * @param fieldName field name
     * @return a field holder
     */
    operator fun get(fieldName: String): FieldHolder? {
        return holders[fieldName]
    }

    /**
     * Returns the number of fields in this response
     * @return the number of fields in this response
     */
    val size: Int
        get() = holders.size

    /**
     * Checks whether a field by its name exists in this response
     * @param fieldName field name
     * @return true if a requested fields exists, false otherwise
     */
    operator fun contains(fieldName: String): Boolean {
        return holders.containsKey(fieldName)
    }
}