package org.sputnikdev.bluetooth.gattparser.num

import org.sputnikdev.bluetooth.gattparser.num.RealNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.TwosComplementNumberFormatter
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
 * Two's complement &amp; little-endian number formatter.
 * Stateless and threadsafe.
 */
class TwosComplementNumberFormatter : RealNumberFormatter {
    override fun deserializeInteger(bits: BitSet?, size: Int, signed: Boolean): Int? {
        var signed = signed
        require(size <= 32) { "size must be less or equal 32" }
        if (size == 1) {
            signed = false
        }
        val isNegative = signed && size > 1 && bits!![size - 1]
        var value = if (isNegative) -1 else 0
        var i = 0
        while (i < bits!!.length() && i < size) {
            if (isNegative && !bits[i]) {
                value = value xor (1 shl i)
            } else if (!isNegative && bits[i]) {
                value = value or (1 shl i)
            }
            i++
        }
        return value
    }

    override fun deserializeLong(bits: BitSet?, size: Int, signed: Boolean): Long? {
        var signed = signed
        require(size <= 64) { "size must be less or equal than 64" }
        if (size == 1) {
            signed = false
        }
        val isNegative = signed && size > 1 && bits!![size - 1]
        var value = if (isNegative) -1L else 0L
        var i = 0
        while (i < bits!!.length() && i < size) {
            if (isNegative && !bits[i]) {
                value = value xor (1L shl i)
            } else if (!isNegative && bits[i]) {
                value = value or (1L shl i)
            }
            i++
        }
        return value
    }

    override fun deserializeBigInteger(bits: BitSet?, size: Int, signed: Boolean): BigInteger? {
        var signed = signed
        if (size == 1) {
            signed = false
        }
        val isNegative = signed && size > 1 && bits!![size - 1]
        var value = if (isNegative) BigInteger.ONE.negate() else BigInteger.ZERO
        var i = 0
        while (i < bits!!.length() && i < size) {
            if (isNegative && !bits[i]) {
                value = value.clearBit(i)
            } else if (!isNegative && bits[i]) {
                value = value.setBit(i)
            }
            i++
        }
        return value
    }

    override fun serialize(number: Int?, size: Int, signed: Boolean): BitSet? {
        var signed = signed
        if (size == 1) {
            signed = false
        }
        val length = Math.min(size, Integer.SIZE)
        val bitSet = BitSet.valueOf(longArrayOf(number!!.toLong()))[0, length]
        if (signed && number < 0) {
            bitSet.set(length - 1)
        }
        return bitSet
    }

    override fun serialize(number: Long?, size: Int, signed: Boolean): BitSet? {
        var signed = signed
        if (size == 1) {
            signed = false
        }
        val length = Math.min(size, java.lang.Long.SIZE)
        val bitSet = BitSet.valueOf(longArrayOf(number!!))[0, length]
        if (signed && number < 0) {
            bitSet.set(length - 1)
        }
        return bitSet
    }

    override fun serialize(number: BigInteger?, size: Int, signed: Boolean): BitSet? {
        var signed = signed
        if (size == 1) {
            signed = false
        }
        val bitSet = BitSet(size)
        val length = Math.min(size, BIG_INTEGER_MAX_SIZE)
        for (i in 0 until length - if (signed) 1 else 0) {
            if (number!!.testBit(i)) {
                bitSet.set(i)
            }
        }
        if (signed && number!!.signum() == -1) {
            bitSet.set(length - 1)
        }
        return bitSet
    }

    companion object {
        private const val BIG_INTEGER_MAX_SIZE = 20 * 8
    }
}