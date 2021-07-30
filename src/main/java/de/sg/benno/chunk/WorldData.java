package de.sg.benno.chunk;

import java.util.ArrayList;

public interface WorldData {

    // todo: use optional

    ArrayList<Island5> getIsland5List();
    ArrayList<Ship4> getShips4List();

    void cleanUp();
}
