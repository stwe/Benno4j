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

package de.sg.benno.ogl.state;

import java.util.Objects;

/**
 * Common methods and properties.
 */
public abstract class ApplicationState implements State {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link StateMachine}.
     */
    private final StateMachine stateMachine;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link ApplicationState} object.
     *
     * @param stateMachine The parent {@link StateMachine}.
     */
    public ApplicationState(StateMachine stateMachine) {
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine must not be null");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #stateMachine}.
     *
     * @return {@link #stateMachine}
     */
    public StateMachine getStateMachine() {
        return stateMachine;
    }
}
