package de.sg.benno;

import de.sg.benno.ogl.OglApplication;
import de.sg.benno.ogl.state.StateContext;
import de.sg.benno.ogl.state.StateMachine;

import java.io.IOException;

public class TestApp extends OglApplication {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A {@link StateMachine} object.
     */
    private StateMachine stateMachine;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public TestApp() throws IOException, IllegalAccessException {
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        this.stateMachine = new StateMachine(new StateContext() {
        });
    }

    @Override
    public void input() {
        stateMachine.input();
    }

    @Override
    public void update() {
        stateMachine.update();
    }

    @Override
    public void render() {
        stateMachine.render();
    }

    @Override
    public void renderImGui() throws Exception {
        stateMachine.renderImGui();
    }

    @Override
    public void cleanUp() {
        stateMachine.cleanUp();
    }
}
