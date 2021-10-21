package org.sputnikdev.bluetooth.gattparser

import org.slf4j.LoggerFactory
import org.sputnikdev.bluetooth.gattparser.CharacteristicParser
import org.sputnikdev.bluetooth.gattparser.GenericCharacteristicParser
import org.sputnikdev.bluetooth.gattparser.CharacteristicFormatException
import org.sputnikdev.bluetooth.gattparser.FieldHolder
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParserFactory
import org.sputnikdev.bluetooth.gattparser.num.RealNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.FloatingPointNumberFormatter
import org.sputnikdev.bluetooth.gattparser.spec.*
import java.io.UnsupportedEncodingException
import java.lang.IllegalStateException
import java.math.BigInteger
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
 * A generic implementation of a GATT characteristic parser capable of reading and writing standard/approved
 * Bluetooth GATT characteristics as well as user defined GATT characteristics. Quite often some parts of the Bluetooth
 * GATT specification is misleading and also incomplete, furthermore some "approved" GATT XML fields do not
 * follow the specification, therefore the implementation of this parser is based not only on Bluetooth GATT
 * specification (Core v5) but also based on some heuristic methods, e.g. by studying/following GATT XML files for
 * some services and characteristics.
 *
 * @author Vlad Kolotov
 */
class GenericCharacteristicParser(private val reader: BluetoothGattSpecificationReader) :
    CharacteristicParser {
    private val logger = LoggerFactory.getLogger(
        GenericCharacteristicParser::class.java
    )

    @Throws(CharacteristicFormatException::class)
    override fun parse(
        characteristic: Characteristic,
        raw: ByteArray
    ): LinkedHashMap<String, FieldHolder> {
        val result = LinkedHashMap<String, FieldHolder>()
        validate(characteristic)
        var offset = 0
        val fields = characteristic.value.fields
        val requires = FlagUtils.getReadFlags(fields, raw)
        requires.add("Mandatory")
        for (field in fields) {
            val requirements = field.requirements
            if (requirements != null && !requirements.isEmpty() && !requires.containsAll(
                    requirements
                )
            ) {
                // skipping field as per requirement in the Flags field
                continue
            }
            if (field.reference != null) {
                val subCharacteristic = parse(
                    reader.getCharacteristicByType(field.reference.trim { it <= ' ' }),
                    getRemainder(raw, offset)
                )
                result.putAll(subCharacteristic)
                val size = getSize(subCharacteristic.values)
                if (size == FieldFormat.FULL_SIZE) {
                    break
                }
                offset += size
            } else {
                if (FlagUtils.isFlagsField(field)) {
                    // skipping flags field
                    offset += field.format.size
                    continue
                }
                val fieldFormat = field.format
                result[field.name] = parseField(field, raw, offset)
                if (fieldFormat.size == FieldFormat.FULL_SIZE) {
                    // full size field, e.g. a string
                    break
                }
                offset += field.format.size
            }
        }
        return result
    }

    @Throws(CharacteristicFormatException::class)
    override fun serialize(fieldHolders: Collection<FieldHolder>): ByteArray {
        val bitSet = BitSet()
        var offset = 0
        for (holder in fieldHolders) {
            if (holder.isValueSet) {
                var size = holder.field.format.size
                val serialized = serialize(holder)
                if (size == FieldFormat.FULL_SIZE) {
                    size = serialized.length()
                }
                concat(bitSet, serialized, offset, size)
                offset += size
            }
        }
        // BitSet does not keep 0, fields could be set all to 0, resulting bitSet to be of 0 length,
        // however data array must not be empty, hence forcing to return an array with first byte of 0 value
        val data = if (bitSet.isEmpty) byteArrayOf(0) else bitSet.toByteArray()
        return if (data.size > 20) Arrays.copyOf(bitSet.toByteArray(), 20) else data
    }

    fun parse(field: Field, raw: ByteArray, offset: Int): Any {
        val fieldFormat = field.format
        val size = fieldFormat.size
        return when (fieldFormat.type) {
            FieldType.BOOLEAN -> parseBoolean(raw, offset)
            FieldType.UINT -> deserializeReal(
                raw,
                offset,
                size,
                false
            )
            FieldType.SINT -> deserializeReal(
                raw,
                offset,
                size,
                true
            )
            FieldType.FLOAT_IEE754 -> deserializeFloat(
                BluetoothGattParserFactory.iEEE754FloatingPointNumberFormatter,
                raw,
                offset,
                size
            )
            FieldType.FLOAT_IEE11073 -> deserializeFloat(
                BluetoothGattParserFactory.iEEE11073FloatingPointNumberFormatter,
                raw,
                offset,
                size
            )
            FieldType.UTF8S -> deserializeString(
                raw,
                offset,
                "UTF-8"
            )
            FieldType.UTF16S -> deserializeString(
                raw,
                offset,
                "UTF-16"
            )
            FieldType.STRUCT -> BitSet.valueOf(raw)[offset, offset + raw.size * 8].toByteArray()
            else -> throw IllegalStateException("Unsupported field format: " + fieldFormat.type)
        }
    }

    fun serialize(value: Boolean): BitSet {
        val bitSet = BitSet()
        if (value) {
            bitSet.set(0)
        }
        return bitSet
    }

    fun concat(target: BitSet, source: BitSet, offset: Int, size: Int) {
        for (i in 0 until size) {
            if (source[i]) {
                target.set(offset + i)
            }
        }
    }

    private fun serialize(holder: FieldHolder): BitSet {
        val fieldFormat = holder.field.format
        return when (fieldFormat.type) {
            FieldType.BOOLEAN -> serialize(
                holder.getBoolean(
                    null
                )
            )
            FieldType.UINT, FieldType.SINT -> serializeReal(
                holder
            )
            FieldType.FLOAT_IEE754 -> serializeFloat(
                BluetoothGattParserFactory.iEEE754FloatingPointNumberFormatter, holder
            )
            FieldType.FLOAT_IEE11073 -> serializeFloat(
                BluetoothGattParserFactory.iEEE11073FloatingPointNumberFormatter, holder
            )
            FieldType.UTF8S -> serializeString(holder, "UTF-8")
            FieldType.UTF16S -> serializeString(
                holder,
                "UTF-16"
            )
            FieldType.STRUCT -> BitSet.valueOf(holder.rawValue as ByteArray)
            else -> throw IllegalStateException("Unsupported field format: " + fieldFormat.type)
        }
    }

    private fun parseBoolean(raw: ByteArray, offset: Int): Boolean {
        return BitSet.valueOf(raw)[offset]
    }

    private fun parseField(field: Field, raw: ByteArray, offset: Int): FieldHolder {
        val fieldFormat = field.format
        if (fieldFormat.size != FieldFormat.FULL_SIZE && offset + fieldFormat.size > raw.size * 8) {
            throw CharacteristicFormatException(
                "Not enough bits to parse field \"" + field.name + "\". "
                        + "Data length: " + raw.size + " bytes. "
                        + "Looks like your device does not conform SIG specification."
            )
        }
        val value = parse(field, raw, offset)
        return FieldHolder(field, value)
    }

    private fun validate(characteristic: Characteristic) {
        if (!characteristic.isValidForRead) {
            logger.error("Characteristic cannot be parsed: \"{}\".", characteristic.name)
            throw CharacteristicFormatException(
                "Characteristic cannot be parsed: \"" +
                        characteristic.name + "\"."
            )
        }
    }

    private fun getSize(holders: Collection<FieldHolder>): Int {
        var size = 0
        for (holder in holders) {
            val field = holder.field
            if (field.format.size == FieldFormat.FULL_SIZE) {
                return FieldFormat.FULL_SIZE
            }
            size += field.format.size
        }
        return size
    }

    private fun serializeReal(holder: FieldHolder): BitSet {
        val realNumberFormatter = BluetoothGattParserFactory.twosComplementNumberFormatter
        val size = holder.field.format.size
        val signed = holder.field.format.type == FieldType.SINT
        return if (signed && size <= 32 || !signed && size < 32) {
            realNumberFormatter.serialize(holder.rawValue as Int, size, signed)
        } else if (signed && size <= 64 || !signed && size < 64) {
            realNumberFormatter.serialize(holder.rawValue as Long, size, signed)
        } else {
            realNumberFormatter.serialize(holder.rawValue as BigInteger, size, signed)
        }
    }

    private fun deserializeReal(raw: ByteArray, offset: Int, size: Int, signed: Boolean): Any {
        val realNumberFormatter = BluetoothGattParserFactory.twosComplementNumberFormatter
        val toIndex = offset + size
        return if (signed && size <= 32 || !signed && size < 32) {
            realNumberFormatter.deserializeInteger(
                BitSet.valueOf(raw)[offset, toIndex],
                size,
                signed
            )
        } else if (signed && size <= 64 || !signed && size < 64) {
            realNumberFormatter.deserializeLong(
                BitSet.valueOf(raw)[offset, toIndex],
                size,
                signed
            )
        } else {
            realNumberFormatter.deserializeBigInteger(
                BitSet.valueOf(raw)[offset, toIndex], size, signed
            )
        }
    }

    private fun deserializeFloat(
        formatter: FloatingPointNumberFormatter,
        raw: ByteArray,
        offset: Int,
        size: Int
    ): Any {
        val toIndex = offset + size
        return if (size == 16) {
            formatter.deserializeSFloat(BitSet.valueOf(raw)[offset, toIndex])
        } else if (size == 32) {
            formatter.deserializeFloat(BitSet.valueOf(raw)[offset, toIndex])
        } else if (size == 64) {
            formatter.deserializeDouble(BitSet.valueOf(raw)[offset, toIndex])
        } else {
            throw IllegalStateException("Unknown bit size for float numbers: $size")
        }
    }

    private fun serializeFloat(
        formatter: FloatingPointNumberFormatter,
        holder: FieldHolder
    ): BitSet {
        val size = holder.field.format.size
        return if (size == 16) {
            formatter.serializeSFloat(holder.getFloat(null))
        } else if (size == 32) {
            formatter.serializeFloat(holder.getFloat(null))
        } else if (size == 64) {
            formatter.serializeDouble(holder.getDouble(null))
        } else {
            throw IllegalStateException("Invalid bit size for float numbers: $size")
        }
    }

    private fun deserializeString(raw: ByteArray, offset: Int, encoding: String): String {
        return try {
            String(BitSet.valueOf(raw)[offset, offset + raw.size * 8].toByteArray(), charset(encoding) )
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException(e)
        }
    }

    private fun serializeString(holder: FieldHolder, encoding: String): BitSet {
        return try {
            BitSet.valueOf(holder.getString(null).toByteArray(charset(encoding)))
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException(e)
        }
    }

    private fun getRemainder(raw: ByteArray, offset: Int): ByteArray {
        val remained = BitSet.valueOf(raw)[offset, raw.size * 8].toByteArray()
        val remainedWithTrailingZeros = ByteArray(
            raw.size - Math.ceil(offset / 8.0)
                .toInt()
        )
        System.arraycopy(remained, 0, remainedWithTrailingZeros, 0, remained.size)
        return remainedWithTrailingZeros
    }
}