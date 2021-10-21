package org.sputnikdev.bluetooth.gattparser.num

import org.sputnikdev.bluetooth.gattparser.num.FloatingPointNumberFormatter
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
 * IEEE754 floating point number formatter.
 * Stateless and threadsafe.
 *
 * @author Vlad Kolotov
 */
class IEEE754FloatingPointNumberFormatter : FloatingPointNumberFormatter {
    override fun deserializeSFloat(bits: BitSet?): Float? {
        throw IllegalStateException("Operation not supported")
    }

    override fun deserializeFloat(bits: BitSet?): Float? {
        return java.lang.Float.intBitsToFloat(bits!!.toLongArray()[0].toInt())
    }

    override fun deserializeDouble(bits: BitSet?): Double? {
        return java.lang.Double.longBitsToDouble(bits!!.toLongArray()[0])
    }

    override fun serializeSFloat(number: Float?): BitSet? {
        throw IllegalStateException("Operation not supported")
    }

    override fun serializeFloat(number: Float?): BitSet? {
        return BitSet.valueOf(longArrayOf(java.lang.Float.floatToRawIntBits(number!!).toLong()))
    }

    override fun serializeDouble(number: Double?): BitSet? {
        return BitSet.valueOf(longArrayOf(java.lang.Double.doubleToRawLongBits(number!!)))
    }
}