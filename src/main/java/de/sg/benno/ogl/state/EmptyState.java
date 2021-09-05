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
 * An empty {@link State} for default.
 */
public class EmptyState extends ApplicationState {

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link EmptyState} object.
     *
     * @param stateMachine {@link StateMachine}
     */
    public EmptyState(StateMachine stateMachine) {
        super(Objects.requireNonNull(stateMachine, "stateMachine must not be null"));
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {

    }

    @Override
    public void input() {

    }

    @Override
    public void update() {

    }

    @Override
    public void render() {

    }

    @Override
    public void renderImGui() throws Exception {

    }

    @Override
    public void cleanUp() {

    }
}
