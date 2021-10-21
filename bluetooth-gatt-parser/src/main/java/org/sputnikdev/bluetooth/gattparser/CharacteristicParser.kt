package org.sputnikdev.bluetooth.gattparser

import org.sputnikdev.bluetooth.gattparser.CharacteristicFormatException
import org.sputnikdev.bluetooth.gattparser.spec.Characteristic
import org.sputnikdev.bluetooth.gattparser.FieldHolder
import java.util.LinkedHashMap

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
 * A root interface for all GATT characteristic parsers in the framework. It defines simple read and write operations.
 *
 * @author Vlad Kolotov
 */
interface CharacteristicParser {
    /**
     * Read operation. This method reads raw data and converts it to a user-friends format: a map of parsed
     * characteristic field holders. The order of fields is guaranteed by [LinkedHashMap]
     *
     * @param characteristic an instance of characteristic specification object
     * @param raw byte array of data received from bluetooth device
     * @return a map of parsed characteristic fields
     * @throws CharacteristicFormatException if provided data cannot be parsed,
     * see [BluetoothGattParser.isValidForRead]
     */
    @Throws(CharacteristicFormatException::class)
    fun parse(
        characteristic: Characteristic?,
        raw: ByteArray?
    ): LinkedHashMap<String?, FieldHolder?>?

    /**
     * Write operation. This method serialises characteristic fields into a raw array of bytes ready to send
     * to a bluetooth device.
     *
     * @param fieldHolders a collection of field holders populated with user input
     * @return a raw array of bytes which is ready to be sent to a bluetooth device
     * @throws CharacteristicFormatException if provided fields cannot be serialized,
     * see [BluetoothGattParser.isValidForWrite]
     */
    @Throws(CharacteristicFormatException::class)
    fun serialize(fieldHolders: Collection<FieldHolder?>?): ByteArray?
}