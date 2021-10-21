package org.sputnikdev.bluetooth.gattparser.spec

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import org.sputnikdev.bluetooth.gattparser.spec.InformativeText

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
@XStreamAlias("Service")
class Service {
    @XStreamAsAttribute
    val name: String? = null

    @XStreamAsAttribute
    val uuid: String? = null

    @XStreamAsAttribute
    val type: String? = null

    @XStreamAlias("InformativeText")
    val informativeText: InformativeText? = null

    @XStreamAlias("Characteristics")
    val characteristics: Characteristics? = null
}