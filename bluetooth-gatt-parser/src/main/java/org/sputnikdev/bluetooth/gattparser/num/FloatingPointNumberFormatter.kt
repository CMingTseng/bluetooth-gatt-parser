package org.sputnikdev.bluetooth.gattparser.num

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
 * Decimal number formatter interface. Defines methods for serialization and deserialization of floating point numbers.
 *
 * @author Vlad Kolotov
 */
interface FloatingPointNumberFormatter {
    /**
     * Performs deserialization of a bit sequence representing a short decimal number encoded as a sfloat number
     * into a Float object. See specification for sfloat data type, for example in the IEEE11073 standard.
     *
     * @param bits a sequence of bits
     * @return decoded Float object
     */
    fun deserializeSFloat(bits: BitSet?): Float?

    /**
     * Performs deserialization of a bit sequence representing a decimal number encoded as a float number
     * into a Float object.
     *
     * @param bits a sequence of bits
     * @return decoded Float object
     */
    fun deserializeFloat(bits: BitSet?): Float?

    /**
     * Performs deserialization of a bit sequence representing a decimal number encoded as a double number
     * into a Double object.
     *
     * @param bits a sequence of bits
     * @return decoded Double object
     */
    fun deserializeDouble(bits: BitSet?): Double?

    /**
     * Performs serialization of a Float number into a sequence of bits representing that number in a sfloat format.
     * See a specification of sfloat data type, for example of IEEE11073 standard.
     *
     * @param number a Float number
     * @return serialized sequence of bits
     */
    fun serializeSFloat(number: Float?): BitSet?

    /**
     * Performs serialization of a Float number into a sequence of bits representing that number in a float format.
     *
     * @param number a Float number
     * @return serialized sequence of bits
     */
    fun serializeFloat(number: Float?): BitSet?

    /**
     * Performs serialization of a Double number into a sequence of bits representing that number in a double format.
     *
     * @param number a Double number
     * @return serialized sequence of bits
     */
    fun serializeDouble(number: Double?): BitSet?
}