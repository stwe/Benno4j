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

package de.sg.benno.chunk;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.util.TileUtil;
import de.sg.benno.data.Building;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.ScpFile;
import de.sg.benno.ogl.physics.Aabb;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.*;

import static de.sg.benno.util.TileUtil.ANGLE_TO_THE_HORIZONTAL;
import static de.sg.benno.util.Util.*;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an Island5. An Island5 includes one or more {@link IslandHouse} chunks.
 * Maps the 116-byte C structure TINSELSAVE and represents an Island5 object.
 * <pre>
 * typedef struct {
 *     uint8_t islandNumber;
 *     uint8_t width;
 *     uint8_t height;
 *     uint8_t strtduerrflg : 1;
 *     uint8_t nofixflg : 1;
 *     uint8_t vulkanflg : 1;
 *     uint16_t posx;
 *     uint16_t posy;
 *     uint16_t hirschreviercnt;
 *     uint16_t speedcnt;
 *     uint8_t stadtplayernr[11];
 *     uint8_t vulkancnt;
 *     uint8_t schatzflg;
 *     uint8_t rohstanz;
 *     uint8_t eisencnt;
 *     uint8_t playerflags;
 *     OreMountainData eisenberg[4];
 *     OreMountainData vulkanberg[4];
 *     Fertility fertility;
 *     uint16_t fileNumber;
 *     IslandSize size;
 *     IslandClimate climate;
 *     IslandModified modifiedFlag;
 *     uint8_t duerrproz;
 *     uint8_t rotier;
 *     uint32_t seeplayerflags;
 *     uint32_t duerrcnt;
 *     uint32_t leer3;
 * } TINSELSAVE;
 * </pre>
 */
public class Island5 {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The size of the original C structure TINSELSAVE in bytes.
     */
    private static final int CHUNK_SIZE_IN_BYTES = 116;

    //-------------------------------------------------
    // TINSELSAVE
    //-------------------------------------------------

    public int islandNumber;
    public int width;
    public int height;

    public int strtduerrFlag;
    public int nofixFlag;
    public int volcanoFlag;

    public int xPos;
    public int yPos;
    public int deerTerritoryCount;
    public int speedCount;

    public int[] cityPlayerNr = new int[11];
    public int volcanoCount;
    public int treasureFlag;
    public int resourceCount;
    public int ironCount;
    public int playerFlags;

    Island.OreMountainData[] ironMountain = new Island.OreMountainData[4];
    Island.OreMountainData[] volcanoMountain = new Island.OreMountainData[4];

    Island.Fertility fertility;

    public int fileNumber;
    public Island.IslandSize size;
    public Island.IslandClimate climate;

    /**
     * Flag that indicates if the island is original (empty INSELHAUS chunk) or
     * modified (filled INSELHAUS chunk).
     */
    public boolean modified;

    public int duerrproz;
    public int rotier;
    public int seePlayerFlags;
    public int duerrCount;
    public int empty;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link BennoFiles} object.
     */
    private final BennoFiles bennoFiles;

    /**
     * The map with all {@link Building} objects.
     */
    private final HashMap<Integer, Building> buildings;

    /**
     * A list with all added {@link IslandHouse} objects.
     */
    private final ArrayList<IslandHouse> islandHouseList = new ArrayList<>();

    /**
     * A (helper) list holding the {@link #topLayer} and {@link #bottomLayer}.
     */
    private final ArrayList<IslandHouse> finalIslandHouseList = new ArrayList<>();

    /**
     * The top layer.
     */
    private IslandHouse topLayer;

    /**
     * The bottom layer.
     */
    private IslandHouse bottomLayer;

    /**
     * An {@link Aabb} object for each {@link Zoom}.
     */
    private final HashMap<Zoom, Aabb> aabbs = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Island5} object.
     *
     * @param chunk {@link Chunk}
     * @param context {@link Context}.
     * @throws Exception If an error is thrown.
     */
    public Island5(Chunk chunk, Context context) throws Exception {
        LOGGER.debug("Creates Island5 object.");

        Objects.requireNonNull(context, "context must not be null");

        this.bennoFiles = context.bennoFiles;
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();

        readData(Objects.requireNonNull(chunk, "chunk must not be null"));

        createAabbs(context);
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
     * Get {@link #topLayer}.
     *
     * @return {@link #topLayer}
     */
    public IslandHouse getTopLayer() {
        return topLayer;
    }

    /**
     * Get {@link #bottomLayer}.
     *
     * @return {@link #bottomLayer}
     */
    public IslandHouse getBottomLayer() {
        return bottomLayer;
    }

    /**
     * Returns the {@link IslandTile} object from the given position of the {@link #bottomLayer}.
     *
     * @param x The x position on an island in world space.
     * @param y The y position on an island in world space.
     *
     * @return A nullable {@link IslandTile} Optional.
     */
    public Optional<IslandTile> getTileFromBottomLayer(int x, int y) {
        IslandTile result = null;

        if (getBottomLayer().getTile(x, y).isPresent()) {
            result = getBottomLayer().getTile(x, y).get();
        }

        return Optional.ofNullable(result);
    }

    /**
     * Returns the {@link IslandTile} object from the given position of the {@link #topLayer}.
     *
     * @param x The x position on an island in world space.
     * @param y The y position on an island in world space.
     *
     * @return A nullable {@link IslandTile} Optional.
     */
    public Optional<IslandTile> getTileFromTopLayer(int x, int y) {
        IslandTile result = null;

        if (getTopLayer().getTile(x, y).isPresent()) {
            result = getTopLayer().getTile(x, y).get();
        }

        return Optional.ofNullable(result);
    }

    /**
     * Get {@link Aabb} by {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     *
     * @return {@link Aabb}
     */
    public Aabb getAabb(Zoom zoom) {
        return aabbs.get(zoom);
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Add an {@link IslandHouse} to the {@link #islandHouseList}.
     *
     * @param islandHouse {@link IslandHouse}
     */
    public void addIslandHouse(IslandHouse islandHouse) {
        islandHouseList.add(Objects.requireNonNull(islandHouse, "islandHouse must not be null"));
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Sets the final top and bottom layer.
     *
     * @throws IOException If an I/O error is thrown.
     */
    public void setTopAndBottomLayer() throws IOException {
        LOGGER.debug("Modified flag: {}", modified);
        LOGGER.debug("Number of layers: {}", islandHouseList.size());
        LOGGER.debug("Start set top and bottom final layer...");

        if (!modified && islandHouseList.size() <= 1) {
            LOGGER.debug("Load the unmodified bottom layer from the island SCP file.");

            var scpFile = new ScpFile(bennoFiles.getScpFilePath(climate, getScpFileName()));
            if (scpFile.getNumberOfChunks() >= 2) {
                var scpFileChunk1 = scpFile.getChunks().get(1);
                if (scpFileChunk1.getId().equals("INSELHAUS")) {
                    throw new BennoRuntimeException("Not implemented yet.");
                    /*
                    // todo: in ScpFile erstellen
                    //std::make_shared<IslandHouse>(chunks[1]->chunk.data, chunks[1]->chunk.length, chunks[1]->chunk.name.c_str(), island.width, island.height);
                    var scpIslandHouse = new IslandHouse(vars);
                    islandHouseList.add(0, scpIslandHouse);
                    */
                }
            } else {
                throw new BennoRuntimeException("Invalid number of chunks.");
            }

            if (islandHouseList.size() == 2) {
                finalIslandHouseList.add(islandHouseList.get(0));
                finalIslandHouseList.add(islandHouseList.get(1));
            }

            if (islandHouseList.size() == 1) {
                finalIslandHouseList.add(islandHouseList.get(0));
                // create empty top
                var emptyIslandHouse = new IslandHouse(this);
                finalIslandHouseList.add(emptyIslandHouse);
            }
        } else {
            LOGGER.debug("The island is considered modified, first chunk is bottom.");

            if (islandHouseList.isEmpty()) {
                throw new BennoRuntimeException("Invalid number of layes.");
            }

            // first chunk is bottom
            finalIslandHouseList.add(islandHouseList.get(0));

            // a possible second chunk is top
            if (islandHouseList.size() == 2) {
                finalIslandHouseList.add(islandHouseList.get(1));
            } else {
                // create empty top
                // todo
                var emptyIslandHouse = new IslandHouse(this);
                finalIslandHouseList.add(emptyIslandHouse);
            }
        }

        bottomLayer = finalIslandHouseList.get(0);
        topLayer = finalIslandHouseList.get(1);

        LOGGER.debug("Set top and bottom final layer successfully.");
    }

    /*
    h/w = 0.5

    126,87 deg

    \    /
     \  /
      \/  arctan(sin(30)) = 26.565
 -----------


      a
  x ------       found x in screen space
   |     /  \
 b |   /      \
   | /  c       \
     \          /
       \      /
         \  /

    a = c * cos(26.565)
    b = sqrt(c² - a²)

    ------
   |     / | \
   |   /   |   \
   | / c   | b   \
     ------
     \  a        /
       \       /
         \   /

     c = sqrt(a² + b²)

         / \
       /     \
     /         \
    |\          / \
    |  \      /     \
  a |    \  /         \
    |    c  \         /
    |         \     /
    |           \ /
    -------------
        b

    a = c · cos(90 - 26.565)

    */

    /**
     * Create an {@link Aabb} for each {@link Zoom}.
     *
     * @param context {@link Context}
     * @throws Exception If an error is thrown.
     */
    private void createAabbs(Context context) throws Exception {
        for (var zoom : Zoom.values()) {
            float l = (zoom.getTileWidthHalf() * zoom.getTileWidthHalf()) + (zoom.getTileHeightHalf() * zoom.getTileHeightHalf());
            l = (float)Math.sqrt(l);

            float c = l * height;
            float a = c * (float)Math.cos(Math.toRadians(ANGLE_TO_THE_HORIZONTAL));
            float ao = a;
            float b = (c * c) - (a * a);
            b = (float)Math.sqrt(b);

            var aabb = new Aabb(context.engine);
            var screenStart = TileUtil.worldToScreen(xPos, yPos, zoom.getTileWidthHalf(), zoom.getTileHeightHalf());
            aabb.position = new Vector2f(screenStart);
            aabb.position.x -= a;

            c = l * width;
            a = (float)(c * Math.cos(Math.toRadians(90.0 - ANGLE_TO_THE_HORIZONTAL)));
            aabb.size.y = a + b;

            b = (c * c) - (a * a);
            b = (float)Math.sqrt(b);

            aabb.size.x = b + ao;

            aabbs.put(zoom, aabb);
        }
    }

    //-------------------------------------------------
    // Read
    //-------------------------------------------------

    /**
     * Reads the {@link Island5} data from a given {@link Chunk}.
     *
     * @param chunk {@link Chunk}
     */
    private void readData(Chunk chunk) {
        LOGGER.debug("Start reading the Island5 data...");

        var start = chunk.getData().position();

        islandNumber = byteToInt(chunk.getData().get());
        width = byteToInt(chunk.getData().get());
        height = byteToInt(chunk.getData().get());

        // todo: check results
        var flags = byteToInt(chunk.getData().get());
        strtduerrFlag = bitExtracted(flags, 1, 1);
        nofixFlag = bitExtracted(flags, 1, 2);
        volcanoFlag = bitExtracted(flags, 1, 3);

        xPos = shortToInt(chunk.getData().getShort());
        yPos = shortToInt(chunk.getData().getShort());
        deerTerritoryCount = shortToInt(chunk.getData().getShort());
        speedCount = shortToInt(chunk.getData().getShort());

        byte[] b0 = new byte[11];
        chunk.getData().get(b0);
        for (int i = 0; i < 11; i++) {
            cityPlayerNr[i] = byteToInt(b0[i]);
        }

        volcanoCount = byteToInt(chunk.getData().get());
        treasureFlag = byteToInt(chunk.getData().get());
        resourceCount = byteToInt(chunk.getData().get());
        ironCount = byteToInt(chunk.getData().get());
        playerFlags = byteToInt(chunk.getData().get());

        for (int i = 0; i < 4; i++) {
            ironMountain[i] = new Island.OreMountainData();
            ironMountain[i].good = byteToInt(chunk.getData().get());
            ironMountain[i].xPosOnIsland = byteToInt(chunk.getData().get());
            ironMountain[i].yPosOnIsland = byteToInt(chunk.getData().get());
            ironMountain[i].playerFlags = byteToInt(chunk.getData().get());
            ironMountain[i].type = byteToInt(chunk.getData().get());
            ironMountain[i].empty = byteToInt(chunk.getData().get());
            ironMountain[i].stock = shortToInt(chunk.getData().getShort());
        }

        for (int i = 0; i < 4; i++) {
            volcanoMountain[i] = new Island.OreMountainData();
            volcanoMountain[i].good = byteToInt(chunk.getData().get());
            volcanoMountain[i].xPosOnIsland = byteToInt(chunk.getData().get());
            volcanoMountain[i].yPosOnIsland = byteToInt(chunk.getData().get());
            volcanoMountain[i].playerFlags = byteToInt(chunk.getData().get());
            volcanoMountain[i].type = byteToInt(chunk.getData().get());
            volcanoMountain[i].empty = byteToInt(chunk.getData().get());
            volcanoMountain[i].stock = shortToInt(chunk.getData().getShort());
        }

        var validFertilityData = false;
        var fertilityData = chunk.getData().getInt();
        for (var fertilityConst : Island.Fertility.values()) {
            if (fertilityConst.value == fertilityData) {
                fertility = fertilityConst;
                validFertilityData = true;
            }
        }
        // todo
        if (!validFertilityData) {
            LOGGER.debug("Sets Fertility to a random value.");
            fertility = Island.Fertility.RANDOM;
        }

        fileNumber = shortToInt(chunk.getData().getShort());

        var validSizeData = false;
        var sizeData = shortToInt(chunk.getData().getShort());
        for (var sizeConst : Island.IslandSize.values()) {
            if (sizeConst.ordinal() == sizeData) {
                size = sizeConst;
                validSizeData = true;
            }
        }
        if (!validSizeData) {
            throw new BennoRuntimeException("Invalid Island5 size data.");
        }

        var validClimateData = false;
        var climateData = byteToInt(chunk.getData().get());
        for (var climateConst : Island.IslandClimate.values()) {
            if (climateConst.ordinal() == climateData) {
                climate = climateConst;
                validClimateData = true;
            }
        }
        if (!validClimateData) {
            throw new BennoRuntimeException("Invalid Island5 climate data.");
        }

        modified = byteToInt(chunk.getData().get()) == 1;

        duerrproz = byteToInt(chunk.getData().get());
        rotier = byteToInt(chunk.getData().get());
        seePlayerFlags = chunk.getData().getInt();
        duerrCount = chunk.getData().getInt();
        empty = chunk.getData().getInt();

        // ensure correct position in the chunk
        if (chunk.getData().position() - start != CHUNK_SIZE_IN_BYTES) {
            throw new BennoRuntimeException("Unexpected error.");
        }

        LOGGER.debug("Island5 data read successfully.");
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Work out the gfx and {@link TileGraphic.TileHeight} and set the result in
     * the given {@link TileGraphic}.
     *
     * @param tile A {@link IslandTile} of this island with the graphicId and some other info.
     * @param tileGraphic The {@link TileGraphic} to change.
     */
    public void setGfxInfo(IslandTile tile, TileGraphic tileGraphic) {
        var building = buildings.get(tile.getGraphicId());

        var gfx = building.gfx;

        var directions = 1;
        if (building.rotate > 0) {
            directions = 4;
        }

        var aniSteps = 1;
        if (building.animAnz > 0) {
            aniSteps = building.animAnz;
        }

        gfx += building.rotate * (tile.orientation % directions);

        switch (tile.orientation) {
            case 0:
                gfx += tile.yPosOnIsland * building.width + tile.xPosOnIsland;
                break;
            case 1:
                gfx += (building.height - tile.xPosOnIsland - 1) * building.width + tile.yPosOnIsland;
                break;
            case 2:
                gfx += (building.height - tile.yPosOnIsland - 1) * building.width + (building.width - tile.xPosOnIsland - 1);
                break;
            case 3:
                gfx += tile.xPosOnIsland * building.width + (building.width - tile.yPosOnIsland - 1);
                break;
            default:
                LOGGER.debug("Unknow rotation.");
        }

        gfx += building.width * building.height * directions * (tile.animationCount % aniSteps);

        tileGraphic.gfxIndex = gfx;
        tileGraphic.tileHeight = building.posoffs == 0 ? TileGraphic.TileHeight.SEA_LEVEL : TileGraphic.TileHeight.CLIFF;
    }

    /**
     * Returns the name of the island SCP file.
     * A xxxyy.scp file is the "naked" island, where xxx is one of lar/big/med/mit/lit and yy is a two-digit number.
     *
     * @return String
     */
    private String getScpFileName() {
        return size.toString() + String.format("%02d", islandNumber) + ".scp";
    }

    /**
     * Checks whether there is an {@link Island5} at the given position in the world.
     *
     * @param x The world position in x direction.
     * @param y The world position in y direction.
     * @param island5List A list of {@link Island5} objects.
     *
     * @return A nullable {@link Island5} Optional.
     */
    public static Optional<Island5> isIsland5OnPosition(int x, int y, ArrayList<Island5> island5List) {
        Island5 result = null;

        for (var island5 : island5List) {
            if ((x >= island5.xPos) &&
                    (y >= island5.yPos) &&
                    (x < island5.xPos + island5.width) &&
                    (y < island5.yPos + island5.height)) {
                result = island5;
            }
        }

        return Optional.ofNullable(result);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Island5.");

        aabbs.forEach((k, v) -> v.cleanUp());
    }
}
