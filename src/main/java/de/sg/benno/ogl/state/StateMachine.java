/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.state;

import java.util.HashMap;
import java.util.Objects;

/**
 * Manages all {@link State} objects.
 */
public class StateMachine {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link StateContext}.
     */
    private final StateContext stateContext;

    /**
     * The {@link HashMap} which contains the {@link State} objects.
     */
    private final HashMap<String, State> states;

    /**
     * The current {@link State} object.
     */
    private State currentState;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link StateMachine} object.
     *
     * @param stateContext The {@link StateContext}
     */
    public StateMachine(StateContext stateContext) {
        this.stateContext = Objects.requireNonNull(stateContext, "stateContext must not be null");

        this.states = new HashMap<>();
        this.currentState = new EmptyState(this);
        this.states.put(null, currentState);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #stateContext}.
     *
     * @return {@link #stateContext}
     */
    public StateContext getStateContext() {
        return stateContext;
    }

    //-------------------------------------------------
    // Add && Change
    //-------------------------------------------------

    /**
     * Add a {@link State}.
     *
     * @param name The name of the {@link State}.
     * @param state The {@link State} object.
     */
    public void add(String name, State state) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(state, "state must not be null");

        states.put(name, state);
    }

    /**
     * Change to the {@link State} with the given name.
     *
     * @param name The name of the {@link State}.
     * @param params A list of params.
     * @throws Exception If an error is thrown.
     */
    public void change(String name, Object... params) throws Exception {
        Objects.requireNonNull(name, "name must not be null");

        currentState.cleanUp();

        currentState = states.get(name);
        currentState.init(params);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Initializing {@link #currentState}.
     *
     * @param params A list of params.
     * @throws Exception If an error is thrown.
     */
    public void init(Object... params) throws Exception {
        currentState.init(params);
    }

    /**
     * Get the {@link #currentState} input.
     */
    public void input() {
        currentState.input();
    }

    /**
     * Update {@link #currentState}.
     */
    public void update() {
        currentState.update();
    }

    /**
     * Render {@link #currentState}.
     */
    public void render() {
        currentState.render();
    }

    /**
     * Render {@link #currentState} ImGui.
     *
     * @throws Exception If an error is thrown.
     */
    public void renderImGui() throws Exception {
        currentState.renderImGui();
    }

    /**
     * Clean up.
     */
    public void cleanUp() {
        currentState.cleanUp();
    }
}
