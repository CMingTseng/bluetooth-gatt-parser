package org.sputnikdev.bluetooth.gattparser

import org.sputnikdev.bluetooth.gattparser.num.RealNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.TwosComplementNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.FloatingPointNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.IEEE754FloatingPointNumberFormatter
import org.sputnikdev.bluetooth.gattparser.num.IEEE11073FloatingPointNumberFormatter
import org.sputnikdev.bluetooth.gattparser.spec.BluetoothGattSpecificationReader
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
 * A factory class for some main objects in the library:
 * [BluetoothGattParser], [BluetoothGattSpecificationReader].
 *
 * @author Vlad Kolotov
 */
object BluetoothGattParserFactory {
    /**
     * Returns two's complement number formatter.
     * @return two's complement number formatter
     */
    val twosComplementNumberFormatter: RealNumberFormatter = TwosComplementNumberFormatter()

    /**
     * Returns IEEE754 floating point number formatter.
     * @return IEEE754 floating point number formatter
     */
    val iEEE754FloatingPointNumberFormatter: FloatingPointNumberFormatter =
        IEEE754FloatingPointNumberFormatter()

    /**
     * Returns IEEE11073 floating point number formatter.
     * @return IEEE11073 floating point number formatter
     */
    val iEEE11073FloatingPointNumberFormatter: FloatingPointNumberFormatter =
        IEEE11073FloatingPointNumberFormatter()

    @Volatile
    private var reader: BluetoothGattSpecificationReader? = null

    @Volatile
    private var defaultParser: BluetoothGattParser? = null

    /**
     * Returns GATT specification reader.
     *
     * @return GATT specification reader
     */
    val specificationReader: BluetoothGattSpecificationReader?
        get() {
            if (reader == null) {
                synchronized(BluetoothGattParserFactory::class.java) {
                    if (reader == null) {
                        reader = BluetoothGattSpecificationReader()
                    }
                }
            }
            return reader
        }

    /**
     * Returns Bluetooth GATT parser.
     * @return Bluetooth GATT parser
     */
    @JvmStatic
    val default: BluetoothGattParser?
        get() {
            if (defaultParser == null) {
                synchronized(BluetoothGattParserFactory::class.java) {
                    if (defaultParser == null) {
                        val reader = specificationReader
                        defaultParser = BluetoothGattParser(
                            reader!!, GenericCharacteristicParser(
                                reader
                            )
                        )
                    }
                }
            }
            return defaultParser
        }
}