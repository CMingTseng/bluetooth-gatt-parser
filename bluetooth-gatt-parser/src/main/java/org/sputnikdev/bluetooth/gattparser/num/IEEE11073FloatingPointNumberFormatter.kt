package org.sputnikdev.bluetooth.gattparser.num

import org.sputnikdev.bluetooth.gattparser.num.FloatingPointNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.TwosComplementNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.IEEE11073FloatingPointNumberFormatter
import java.lang.IllegalStateException
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
 * IEEE11073 floating point number formatter.
 * Stateless and threadsafe.
 *
 * @author Vlad Kolotov
 */
class IEEE11073FloatingPointNumberFormatter : FloatingPointNumberFormatter {
    private val twosComplementNumberFormatter = TwosComplementNumberFormatter()
    override fun deserializeSFloat(bits: BitSet?): Float? {
        val exponentBits = bits!![12, 16]
        val mantissaBits = bits[0, 12]
        val exponent = twosComplementNumberFormatter.deserializeInteger(exponentBits, 4, true)
        val mantissa = twosComplementNumberFormatter.deserializeInteger(mantissaBits, 12, true)
        if (exponent == 0) {
            if (mantissa == SFLOAT_NaN) {
                return Float.NaN
            } else if (mantissa == SFLOAT_POSITIVE_INFINITY) {
                return Float.POSITIVE_INFINITY
            } else if (mantissa == SFLOAT_NEGATIVE_INFINITY_SIGNED) {
                return Float.NEGATIVE_INFINITY
            }
        }
        return (mantissa.toDouble() * Math.pow(10.0, exponent.toDouble())).toFloat()
    }

    override fun deserializeFloat(bits: BitSet?): Float? {
        val exponentBits = bits!![24, 32]
        val mantissaBits = bits[0, 24]
        val exponent = twosComplementNumberFormatter.deserializeInteger(exponentBits, 8, true)
        val mantissa = twosComplementNumberFormatter.deserializeInteger(mantissaBits, 24, true)
        if (exponent == 0) {
            if (mantissa == FLOAT_NaN) {
                return Float.NaN
            } else if (mantissa == FLOAT_POSITIVE_INFINITY) {
                return Float.POSITIVE_INFINITY
            } else if (mantissa == FLOAT_NEGATIVE_INFINITY_SIGNED) {
                return Float.NEGATIVE_INFINITY
            }
        }
        return (mantissa.toDouble() * Math.pow(10.0, exponent.toDouble())).toFloat()
    }

    override fun deserializeDouble(bits: BitSet?): Double? {
        throw IllegalStateException("Operation not supported")
    }

    override fun serializeSFloat(number: Float?): BitSet? {
        throw IllegalStateException("Operation not supported")
    }

    override fun serializeFloat(number: Float?): BitSet? {
        throw IllegalStateException("Operation not supported")
    }

    override fun serializeDouble(number: Double?): BitSet? {
        throw IllegalStateException("Operation not supported")
    }

    companion object {
        const val SFLOAT_NaN = 0x07FF
        const val SFLOAT_NRes = 0x0800
        const val SFLOAT_POSITIVE_INFINITY = 0x07FE
        const val SFLOAT_NEGATIVE_INFINITY = 0x0802
        const val SFLOAT_RESERVED = 0x0801
        const val FLOAT_NaN = 0x007FFFFF
        const val FLOAT_NRes = 0x00800000
        const val FLOAT_POSITIVE_INFINITY = 0x007FFFFE
        const val FLOAT_NEGATIVE_INFINITY = 0x00800002
        const val FLOAT_RESERVED = 0x00800001
        private const val SFLOAT_NEGATIVE_INFINITY_SIGNED = -0x7fe
        private const val FLOAT_NEGATIVE_INFINITY_SIGNED = -0x7ffffe
    }
}