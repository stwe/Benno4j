/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno.ecs.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Represents a Signature.
 */
public class Signature {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The signature {@link BitSet}.
     */
    private final BitSet signatureBitSet = new BitSet();

    /**
     * Stores the component types required for initializing the {@link #signatureBitSet}.
     */
    private final ArrayList<Class<? extends Component>> signatureComponentTypes = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Signature} object.
     *
     * @param signatureComponentTypes A list of component types representing this {@link Signature}.
     */
    @SafeVarargs
    public Signature(Class<? extends Component>... signatureComponentTypes) {
        this.signatureComponentTypes.addAll(Arrays.asList(signatureComponentTypes));
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #signatureBitSet}.
     *
     * @return {@link #signatureBitSet}
     */
    public BitSet getSignatureBitSet() {
        return signatureBitSet;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initializes the {@link #signatureBitSet}.
     *
     * @param allComponentTypes A list of all component types.
     */
    public void initSignatureBitSet(ArrayList<Class<? extends Component>> allComponentTypes) {
        for (var type : signatureComponentTypes) {
            signatureBitSet.set(allComponentTypes.indexOf(type));
        }
    }
}
