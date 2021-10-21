package org.sputnikdev.bluetooth.gattparser.spec

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import java.math.BigInteger

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
 *
 * @author Vlad Kolotov
 */
@XStreamAlias("Bit")
class Bit {
    @XStreamAsAttribute
    val index = 0

    @XStreamAsAttribute
    val size = 0

    @XStreamAsAttribute
    val name: String? = null

    @XStreamAlias("Enumerations")
    val enumerations: Enumerations? = null
    fun getFlag(value: Byte): String? {
        if (enumerations == null) {
            return null
        }
        for (enumeration in enumerations.enumerations) {
            if (enumeration.key == BigInteger.valueOf(value.toLong())) {
                return enumeration.requires
            }
        }
        return null
    }
}