/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
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
