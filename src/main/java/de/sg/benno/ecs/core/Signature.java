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

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a Signature.
 */
public class Signature {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A set of components the entity must have.
     */
    private final BitSet all = new BitSet();

    /**
     * A set of components of which the entity must have at least one.
     */
    private final BitSet one = new BitSet();

    /**
     * A set of components the entity cannot have.
     */
    private final BitSet exclude = new BitSet();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Signature} object.
     */
    public Signature() {
        LOGGER.debug("Creates Signature object.");
    }

    //-------------------------------------------------
    // Matches
    //-------------------------------------------------

    /**
     * Checks whether the entity matches this signature.
     *
     * @param entity The {@link Entity} to check.
     *
     * @return boolean
     */
    public boolean matches(Entity entity) {
        var ec = entity.getComponentsBitSet();

        if (!containsAll(ec, all)) {
            return false;
        }

        if (!one.isEmpty()) {
            if (!one.intersects(ec)) {
                return false;
            }
        }

        if (!exclude.isEmpty()) {
            if (exclude.intersects(ec)) {
                return false;
            }
        }

        return true;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    @SafeVarargs
    public final void setAll(Class<? extends Component>... allTypes) {
        var types = new ArrayList<>(Arrays.asList(allTypes));
        for (var type : types) {
            all.set(EcsSettings.getComponentIndex(type));
        }
    }

    @SafeVarargs
    public final void setOne(Class<? extends Component>... oneTypes) {
        var types = new ArrayList<>(Arrays.asList(oneTypes));
        for (var type : types) {
            one.set(EcsSettings.getComponentIndex(type));
        }
    }

    @SafeVarargs
    public final void setExclude(Class<? extends Component>... excludeTypes) {
        var types = new ArrayList<>(Arrays.asList(excludeTypes));
        for (var type : types) {
            exclude.set(EcsSettings.getComponentIndex(type));
        }
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * A containsAll method for {@link BitSet}.
     *
     * @param s1 A {@link BitSet}.
     * @param s2 A {@link BitSet}.
     *
     * @return boolean
     */
    private static boolean containsAll(BitSet s1, BitSet s2) {
        BitSet intersection = (BitSet) s1.clone();
        intersection.and(s2);
        return intersection.equals(s2);
    }

    /**
     * {@link BitSet} to {@link String}.
     *
     * @param bits A {@link BitSet}.
     *
     * @return String
     */
    public static String getBitsString (BitSet bits) {
        var stringBuilder = new StringBuilder();

        var numBits = bits.length();
        for (var i = 0; i < numBits; i++) {
            stringBuilder.append(bits.get(i) ? "1" : "0");
        }

        return stringBuilder.toString();
    }
}
