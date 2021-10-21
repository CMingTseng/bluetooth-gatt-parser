package org.sputnikdev.bluetooth.gattparser

import org.slf4j.LoggerFactory
import org.sputnikdev.bluetooth.gattparser.spec.BluetoothGattSpecificationReader
import org.sputnikdev.bluetooth.gattparser.spec.Characteristic
import org.sputnikdev.bluetooth.gattparser.spec.Field
import org.sputnikdev.bluetooth.gattparser.spec.Service
import java.lang.IllegalStateException
import java.net.URL
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
 * This class encapsulates functionality for reading and writing Bluetooth GATT characteristics
 * in a user-friendly manner.
 * <br></br>It is capable of dealing with services and characteristics defined by
 * [Bluetooth SIG](https://www.bluetooth.com/specifications/gatt) as well as user-defined services
 * and characteristics. A simple example of reading an "approved" GATT characteristic
 * ([Battery Level](https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.battery_level.xml)) would be:
 * <pre>
 * `BluetoothGattParser parser = BluetoothGattParserFactory.getDefault();
 * String characteristicUUID = "2A19"; // battery level characteristic
 * String batteryLevelFieldName = "Level"; // name of a field in the characteristic
 * byte[] rawData = new byte[] { 51 }; // raw data received from a bluetooth device
 * parser.parse(characteristicUUID, rawData).get(batteryLevelFieldName).getInteger();
` *
</pre> *
 * <br></br>The parser can be extended with user-defined services and characteristics by adding corresponding specification
 * definitions in GATT XML files (See an example [here](https://www.bluetooth.com/api/gatt/XmlFile?xmlFileName=org.bluetooth.characteristic.battery_level.xml)).
 * There are two options of doing so:
 *
 *  * By adding GATT XML files into classpath directories: "ext/gatt/service" and "ext/gatt/characteristic".
 * The parser will load specification files from those directories automatically.
 *  * By loading GATT XML files via [BluetoothGattParser.loadExtensionsFromFolder] method
 *
 * <br></br>The parser can be also extended with a custom characteristic parser,
 * see [BluetoothGattParser.registerParser].
 *
 * @author Vlad Kolotov
 */
class BluetoothGattParser internal constructor(
    private val specificationReader: BluetoothGattSpecificationReader,
    private val defaultParser: CharacteristicParser
) {
    private val logger = LoggerFactory.getLogger(
        GenericCharacteristicParser::class.java
    )
    private val customParsers: MutableMap<String, CharacteristicParser> = HashMap()

    /**
     * Checks whether a provided characteristic UUID is known by the parser.
     * @param characteristicUUID UUID of a GATT characteristic
     * @return true if the parser has loaded definitions for that characteristic, false otherwise
     */
    fun isKnownCharacteristic(characteristicUUID: String): Boolean {
        return specificationReader.getCharacteristicByUUID(getShortUUID(characteristicUUID)) != null
    }

    /**
     * Checks whether a provided service UUID is known by the parser.
     * @param serviceUUID UUID of a GATT service
     * @return true if the parser has loaded definitions for that service, false otherwise
     */
    fun isKnownService(serviceUUID: String): Boolean {
        return specificationReader.getService(getShortUUID(serviceUUID)) != null
    }

    /**
     * Performs parsing of a GATT characteristic value (byte array) into a user-friendly format
     * (a map of parsed characteristic fields represented by [GattResponse]).
     *
     * @param characteristicUUID UUID of a GATT characteristic
     * @param raw byte array of data received from bluetooth device
     * @return a map of parsed characteristic fields represented by [GattResponse]
     * @throws CharacteristicFormatException if a characteristic cannot be parsed
     */
    @Throws(CharacteristicFormatException::class)
    fun parse(characteristicUUID: String, raw: ByteArray): GattResponse {
        return GattResponse(parseFields(characteristicUUID, raw))
    }

    /**
     * Returns a list of fields represented by [GattRequest] for a write operation
     * (see [BluetoothGattParser.serialize]) of a specified GATT characteristic.
     * Some of the returned fields can be mandatory so they have to be set before serialization,
     * check [GattRequest.getRequiredFieldHolders] and [BluetoothGattParser.validate]
     *
     * @param characteristicUUID UUID of a GATT characteristic
     * @return list of fields represented by [GattRequest] for a write operation
     */
    fun prepare(characteristicUUID: String): GattRequest {
        var characteristicUUID = characteristicUUID
        characteristicUUID = getShortUUID(characteristicUUID)
        return GattRequest(
            characteristicUUID,
            specificationReader.getFields(
                specificationReader.getCharacteristicByUUID(
                    characteristicUUID
                )
            )
        )
    }

    /**
     * Returns a list of fields represented by [GattRequest] for a write operation
     * (see [BluetoothGattParser.serialize]) of a specified GATT characteristic which is to be
     * initialized with the provided initial data.
     * Some of the returned fields can be mandatory so they have to be set before serialization,
     * check [GattRequest.getRequiredFieldHolders] and [BluetoothGattParser.validate]
     *
     * @param characteristicUUID UUID of a GATT characteristic
     * @param initial initial data
     * @return list of fields represented by [GattRequest] for a write operation
     */
    fun prepare(characteristicUUID: String, initial: ByteArray): GattRequest {
        var characteristicUUID = characteristicUUID
        characteristicUUID = getShortUUID(characteristicUUID)
        return GattRequest(characteristicUUID, parseFields(characteristicUUID, initial))
    }
    /**
     * Performs serialization of a GATT request prepared by [BluetoothGattParser.prepare]
     * and filled by user (see [GattRequest.setField]) for a further communication to a bluetooth device.
     * Some of the fields can be mandatory so they have to be set before serialization,
     * check [GattRequest.getRequiredFieldHolders] and [BluetoothGattParser.validate].
     *
     * @param gattRequest a GATT request object
     * @param strict dictates whether validation has to be performed before serialization
     * (see [BluetoothGattParser.validate])
     * @return serialized fields as an array of bytes ready to send to a bluetooth device
     * @throws IllegalArgumentException if provided GATT request is not valid and strict parameter is set to true
     */
    /**
     * Performs serialization of a GATT request prepared by [BluetoothGattParser.prepare]
     * and filled by user (see [GattRequest.setField]) for a further communication to a bluetooth device.
     * Some of the fields can be mandatory so they have to be set before serialization,
     * check [GattRequest.getRequiredFieldHolders] and [BluetoothGattParser.validate].
     *
     * @param gattRequest a GATT request object
     * @return serialized fields as an array of bytes ready to send to a bluetooth device
     * @throws IllegalArgumentException if provided GATT request is not valid
     */
    @JvmOverloads
    fun serialize(gattRequest: GattRequest, strict: Boolean = true): ByteArray? {
        require(!(strict && !validate(gattRequest))) { "GATT request is not valid" }
        synchronized(customParsers) {
            val characteristicUUID = getShortUUID(gattRequest.characteristicUUID)
            if (strict && !isValidForWrite(characteristicUUID)) {
                throw CharacteristicFormatException(
                    "Characteristic is not valid for write: $characteristicUUID"
                )
            }
            return if (customParsers.containsKey(characteristicUUID)) {
                customParsers[characteristicUUID]!!.serialize(gattRequest.allFieldHolders)
            } else defaultParser.serialize(gattRequest.allFieldHolders)
        }
    }

    /**
     * Returns a GATT service specification by its UUID.
     * @param serviceUUID UUID of a GATT service
     * @return a GATT service specification by its UUID
     */
    fun getService(serviceUUID: String): Service {
        return specificationReader.getService(getShortUUID(serviceUUID))
    }

    /**
     * Returns a GATT characteristic specification by its UUID.
     * @param characteristicUUID UUID of a GATT characteristic
     * @return a GATT characteristic specification by its UUID
     */
    fun getCharacteristic(characteristicUUID: String): Characteristic {
        return specificationReader.getCharacteristicByUUID(getShortUUID(characteristicUUID))
    }

    /**
     * Returns a list of field specifications for a given characteristic.
     * Note that field references are taken into account. Referencing fields are not returned,
     * referenced fields returned instead (see [Field.getReference]).
     *
     * @param characteristicUUID UUID of a GATT characteristic
     * @return a list of field specifications for a given characteristic
     */
    fun getFields(characteristicUUID: String): List<Field> {
        return specificationReader.getFields(getCharacteristic(getShortUUID(characteristicUUID)))
    }

    /**
     * Registers a new characteristic parser (see [CharacteristicParser]) for a given characteristic.
     * @param characteristicUUID UUID of a GATT characteristic
     * @param parser a new instance of a characteristic parser
     */
    fun registerParser(characteristicUUID: String, parser: CharacteristicParser) {
        synchronized(customParsers) { customParsers.put(getShortUUID(characteristicUUID), parser) }
    }

    /**
     * Checks whether a given characteristic is valid for read operation
     * (see [BluetoothGattParser.parse]).
     * Note that not all standard and approved characteristics are valid for automatic read operations due to
     * malformed or incorrect GATT XML specification files.
     *
     * @param characteristicUUID UUID of a GATT characteristic
     * @return true if a given characteristic is valid for read operation
     */
    fun isValidForRead(characteristicUUID: String): Boolean {
        val characteristic =
            specificationReader.getCharacteristicByUUID(getShortUUID(characteristicUUID))
        return characteristic != null && characteristic.isValidForRead
    }

    /**
     * Checks whether a given characteristic is valid for write operation
     * (see [BluetoothGattParser.serialize]).
     * Note that not all standard and approved characteristics are valid for automatic write operations due to
     * malformed or incorrect GATT XML specification files.
     *
     * @param characteristicUUID UUID of a GATT characteristic
     * @return true if a given characteristic is valid for write operation
     */
    fun isValidForWrite(characteristicUUID: String): Boolean {
        val characteristic =
            specificationReader.getCharacteristicByUUID(getShortUUID(characteristicUUID))
        return characteristic != null && characteristic.isValidForWrite
    }

    /**
     * Checks if a GATT request object has all mandatory fields set (see [BluetoothGattParser.prepare]).
     *
     * @param gattRequest a GATT request object
     * @return true if a given GATT request is valid for write operation
     * (see [BluetoothGattParser.serialize])
     */
    fun validate(gattRequest: GattRequest): Boolean {
        val controlPointField = gattRequest.opCodesFieldHolder
        val requirement = controlPointField?.enumerationRequires
        if (requirement != null) {
            val required = gattRequest.getRequiredHolders(requirement)
            if (required.isEmpty()) {
                logger.info(
                    "GATT request is invalid; could not find any field by requirement: {}",
                    requirement
                )
                return false
            }
            for (holder in required) {
                if (!holder.isValueSet) {
                    logger.info("GATT request is invalid; field is not set: {}", holder.field.name)
                    return false
                }
            }
        }
        for (holder in gattRequest.getRequiredHolders("Mandatory")) {
            if (!holder.isValueSet) {
                logger.info("GATT request is invalid; field is not set: {}", holder.field.name)
                return false
            }
        }
        return true
    }

    /**
     * This method is used to load/register custom services and characteristics
     * (defined in GATT XML specification files,
     * see an example [here](https://www.bluetooth.com/api/gatt/XmlFile?xmlFileName=org.bluetooth.characteristic.battery_level.xml))
     * from a folder. The folder must contain two sub-folders for services and characteristics respectively:
     * "path"/service and "path"/characteristic. It is also possible to override existing services and characteristics
     * by matching UUIDs of services and characteristics in the loaded files.
     * @param path a root path to a folder containing definitions for custom services and characteristics
     */
    fun loadExtensionsFromFolder(path: String?) {
        specificationReader.loadExtensionsFromFolder(path)
    }

    /**
     * This method is used to load/register custom services and characteristics
     * (defined in GATT XML specification files,
     * see an example [here](https://www.bluetooth.com/api/gatt/XmlFile?xmlFileName=org.bluetooth.characteristic.battery_level.xml))
     * from a resource URLs. The URLs must point to json object, holding filenames (types) of gatt xml specs as values
     * and their short uuid's as keys.
     * @param servicesCatalogResource a path to a folder containing definitions for custom services
     * @param characteristicsCatalogResource a path to a folder containing definitions for custom characteristics
     * @throws IllegalStateException when either argument is null
     */
    @Throws(IllegalStateException::class)
    fun loadExtensionsFromCatalogResources(
        servicesCatalogResource: URL?,
        characteristicsCatalogResource: URL?
    ) {
        specificationReader.loadExtensionsFromCatalogResources(
            servicesCatalogResource,
            characteristicsCatalogResource
        )
    }

    /**
     * Returns text representation of the provided array of bytes. Example: [01, 05, ab]
     * @param raw bytes array
     * @param radix the radix to use in the string representation
     * @return array text representation
     */
    fun parse(raw: ByteArray, radix: Int): String {
        val hexFormatted = arrayOfNulls<String>(raw.size)
        var index = 0
        for (b in raw) {
            val num = Integer.toUnsignedString(java.lang.Byte.toUnsignedInt(b), radix)
            hexFormatted[index++] = "00$num".substring(num.length)
        }
        return Arrays.toString(hexFormatted)
    }

    /**
     * Serializes a string that represents an array of bytes (comma separated, e.g: [01, 05, ab]),
     * see ([.parse]).
     * @param raw a string representing an array of bytes
     * @param radix the radix to use in the string representation
     * @return serialized array
     */
    fun serialize(raw: String, radix: Int): ByteArray {
        val data = raw.replace("[", "").replace("]", "")
        val tokens = data.split(",").toTypedArray()
        val bytes = ByteArray(tokens.size)
        for (i in tokens.indices) {
            bytes[i] = (Integer.valueOf(tokens[i].trim { it <= ' ' }, radix) as Int).toByte()
        }
        return bytes
    }

    private fun getShortUUID(uuid: String): String {
        return if (uuid.length < 8) {
            uuid.toUpperCase()
        } else java.lang.Long.toHexString(
            java.lang.Long.valueOf(
                uuid.substring(0, 8),
                16
            )
        ).toUpperCase()
    }

    private fun parseFields(
        characteristicUUID: String,
        raw: ByteArray
    ): LinkedHashMap<String?, FieldHolder?>? {
        var characteristicUUID = characteristicUUID
        characteristicUUID = getShortUUID(characteristicUUID)
        synchronized(customParsers) {
            if (!isValidForRead(characteristicUUID)) {
                throw CharacteristicFormatException("Characteristic is not valid for read: $characteristicUUID")
            }
            val characteristic = specificationReader.getCharacteristicByUUID(characteristicUUID)
            return if (customParsers.containsKey(characteristicUUID)) {
                customParsers[characteristicUUID]!!.parse(characteristic, raw)
            } else defaultParser.parse(characteristic, raw)
        }
    }
}