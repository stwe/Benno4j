/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.chunk;

import de.sg.benno.data.Building;

import java.util.*;

import static de.sg.benno.Util.*;
import static de.sg.ogl.Log.LOGGER;

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
     * The buildings list.
     */
    private final HashMap<Integer, Building> buildings;

    private final ArrayList<IslandHouse> islandHouseList = new ArrayList<>();
    private final ArrayList<IslandHouse> finalIslandHouseList = new ArrayList<>();

    private IslandHouse topLayer;
    private IslandHouse bottomLayer;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Island5} object.
     *
     * @param chunk {@link Chunk}
     * @param buildings The buildings list.
     */
    public Island5(Chunk chunk, HashMap<Integer, Building> buildings) {
        LOGGER.debug("Creates Island5 object.");

        this.buildings = Objects.requireNonNull(buildings, "buildings must not be null");

        readData(Objects.requireNonNull(chunk, "chunk must not be null"));
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

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Add an {@link IslandHouse} to the {@link #islandHouseList}.
     *
     * @param islandHouse {@link IslandHouse}
     */
    public void addIslandHouse(IslandHouse islandHouse) {
        islandHouseList.add(islandHouse);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Determines the top and bottom layer.
     */
    public void initLayer() {
        if (islandHouseList.isEmpty()) {
            throw new RuntimeException("No IslandHouse data found.");
        }

        // todo: hardcoded for the current savegame

        // the island is modified, first chunk is bottom
        finalIslandHouseList.add(islandHouseList.get(0));

        // a possible second chunk is top
        if (islandHouseList.size() == 2) {
            finalIslandHouseList.add(islandHouseList.get(1));
        }

        bottomLayer = finalIslandHouseList.get(0);
        topLayer = finalIslandHouseList.get(1);
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
            throw new RuntimeException("Invalid Island5 size data.");
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
            throw new RuntimeException("Invalid Island5 climate data.");
        }

        modified = byteToInt(chunk.getData().get()) == 1;

        duerrproz = byteToInt(chunk.getData().get());
        rotier = byteToInt(chunk.getData().get());
        seePlayerFlags = chunk.getData().getInt();
        duerrCount = chunk.getData().getInt();
        empty = chunk.getData().getInt();

        // ensure correct position in the chunk
        if (chunk.getData().position() - start != CHUNK_SIZE_IN_BYTES) {
            throw new RuntimeException("Unexpected error.");
        }

        LOGGER.debug("Island5 data read successfully.");
    }
}
