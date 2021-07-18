/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sg.benno.Util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

/**
 * Represents a DataFiles object.
 */
public class DataFiles {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Json file with the information of all buildings.
     */
    private static final String BUILDINGS_JSON = "history/buildings.json";

    /**
     * Json file with the information of the main menu.
     */
    private static final String BASE_JSON = "history/base.json";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A map with {@link Building} objects.
     */
    private final HashMap<Integer, Building> buildings = new HashMap<>();

    /**
     * A map with {@link Widget} objects.
     */
    private final HashMap<Integer, Widget> widgets = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link DataFiles} object.
     *
     * @throws IOException If an I/O error is thrown.
     */
    public DataFiles() throws IOException {
        LOGGER.debug("Creates DataFiles object.");

        readBuildings(getRootNodeData(BUILDINGS_JSON));
        readBaseWidgets(getRootNodeData(BASE_JSON));
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #buildings}.
     *
     * @return {@link #buildings}
     */
    public HashMap<Integer, Building> getBuildings() {
        return buildings;
    }

    /**
     * Get {@link #widgets}.
     *
     * @return {@link #widgets}
     */
    public HashMap<Integer, Widget> getWidgets() {
        return widgets;
    }

    //-------------------------------------------------
    // Read data
    //-------------------------------------------------

    /**
     * Creates the map with the building information from the Json data.
     *
     * @param rootNode {@link JsonNode}
     */
    private void readBuildings(JsonNode rootNode) {
        Objects.requireNonNull(rootNode, "rootNode must not be null");

        LOGGER.debug("Start reading the building data from file {} ...", BUILDINGS_JSON);

        var nodes = rootNode.get("object").get(2).get("objects");

        for (var node : nodes) {
            var building = new Building();

            var nodeArray = node.get("variables").get("variable");
            if (nodeArray.isArray()) {
                for (var var : nodeArray) {
                    var name = var.get("name").asText();

                    if (name.equals("Id")) {
                        building.id = var.get("valueInt").asInt() - 20000;
                    }

                    if (name.equals("Gfx")) {
                        building.gfx = var.get("valueInt").asInt();
                    }

                    // blocknr

                    if (name.equals("Posoffs")) {
                        building.posoffs = var.get("valueInt").asInt();
                    }

                    if (name.equals("Size")) {
                        var values = var.get("valueArray").get("value");
                        if (values.isArray()) {
                            building.width = values.get(0).get("valueInt").asInt();
                            building.height = values.get(1).get("valueInt").asInt();
                        }
                    }

                    // highflg
                    // einhoffs
                    // maxenergy
                    // maxbrand

                    if (name.equals("Rotate")) {
                        building.rotate = var.get("valueInt").asInt();
                    }

                    // randAnz

                    // sometimes missing
                    if (name.equals("AnimAnz")) {
                        building.animAnz = var.get("valueInt").asInt();
                    }

                    if (name.equals("AnimTime")) {
                        // skip TIMENEVER string nodes
                        if (var.has("valueInt")) {
                            building.animTime = var.get("valueInt").asInt();
                        }
                    }

                    if (name.equals("AnimFrame")) {
                        building.animFrame = var.get("valueInt").asInt();
                    }

                    // sometimes missing
                    if (name.equals("AnimAdd")) {
                        building.animAdd = var.get("valueInt").asInt();
                    }

                    // baugfx
                    // kreuzBase
                    // randwachs
                    // randAdd
                    // strandoff
                    // placeFlg
                    // noShotFlg
                    // strandflg
                    // ausbauflg
                    // tuerflg
                    // destroyflg
                }
            }

            if (building.id > 0) {
                buildings.put(building.id, building);
            }
        }

        LOGGER.debug("Building data read successfully. {} entries were found.", buildings.size());
    }

    /**
     * Creates the map with the widget information from the Json data.
     *
     * @param rootNode {@link JsonNode}
     */
    private void readBaseWidgets(JsonNode rootNode) {
        Objects.requireNonNull(rootNode, "rootNode must not be null");

        LOGGER.debug("Start reading the MainMenu data from file {} ...", BASE_JSON);

        var objectNodeArray = rootNode.get("object");
        if (objectNodeArray.isArray()) {
            for (var objectNode : objectNodeArray) {
                var objectsNodeArray = objectNode.get("objects");
                if (objectsNodeArray.isArray()) {
                    for (var objectsNode : objectsNodeArray) {
                        var widget = new Widget();
                        var varsNodeArray = objectsNode.get("variables").get("variable");
                        if (varsNodeArray.isArray()) {
                            for (var varNode : varsNodeArray) {
                                var name = varNode.get("name").asText();

                                if (name.equals("Id")) {
                                    widget.id = varNode.get("valueInt").asInt() - 30000;
                                }

                                if (name.equals("Blocknr")) {
                                    widget.blocknr = varNode.get("valueInt").asInt();
                                }

                                if (name.equals("Gfxnr")) {
                                    widget.gfx = varNode.get("valueInt").asInt();
                                }

                                // Kind value string
                                // Noselflg value int

                                if (name.equals("Pos")) {
                                    var values = varNode.get("valueArray").get("value");
                                    if (values.isArray()) {
                                        widget.x = values.get(0).get("valueInt").asInt();
                                        widget.y = values.get(1).get("valueInt").asInt();
                                    }
                                }

                                if (name.equals("Size")) {
                                    var values = varNode.get("valueArray").get("value");
                                    if (values.isArray()) {
                                        widget.width = values.get(0).get("valueInt").asInt();
                                        widget.height = values.get(1).get("valueInt").asInt();
                                    }
                                }
                            }
                        }

                        widgets.put(widget.id, widget);
                    }
                }
            }
        }

        LOGGER.debug("MainMenu data read successfully. {} entries were found.", widgets.size());
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Returns the root Json node.
     *
     * @param path Path to the Json file.
     *
     * @return {@link JsonNode}
     * @throws IOException If an I/O error is thrown.
     */
    private JsonNode getRootNodeData(String path) throws IOException {
        var inputStream = Util.getFileFromResourceAsStream(path);
        var reader = new InputStreamReader(Objects.requireNonNull(inputStream, "inputStream must not be null"));

        var mapper = new ObjectMapper();

        return mapper.readValue(reader, JsonNode.class);
    }
}
