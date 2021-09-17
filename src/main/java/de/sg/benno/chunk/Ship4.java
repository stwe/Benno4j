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

import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Objects;

import static de.sg.benno.Util.shortToInt;
import static de.sg.benno.chunk.Ship4.ShipType.*;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a Ship4. Maps th C structure Ship.
 * <pre>
 * struct Ship {
 *     char name[28];          // Name des Schiffs, NULL-terminiert
 *     uint16_t x_pos;         // X-Position des Schiffs auf der Karte
 *     uint16_t y_pos;         // Y-Position des Schiffs auf der Karte
 *     uint32_t a[3];          // 0x00000000 0x00000000 0x00000000
 *     uint32_t kurs_start;    // Y-Koordinate: Byte2[4:7]Byte3, X-Koordinate: Byte2[0:3]Byte4
 *     uint32_t kurs_ziel;     // Byte1: 0x00 für stehen, 0x37 für fahren
 *     uint32_t kurs_aktuell;
 *     uint32_t b;
 *     uint16_t lp;            // Zustand (Lebenspunkte) des Schiffes
 *     uint16_t c;             // 0x0000, 0x000c, 0x0010, 0x0011
 *     uint8_t d1;             // 0, 1, 2, 3, 5, 6
 *     uint8_t d2;             // 0x50
 *     uint8_t bewaffnung;     // Anzahl Kanonen
 *     uint8_t flags;          // z.B. 0x00, 0x02, 0x08, 0x0a, 0x06, 0x09;  Flag 0x04: zum Verkauf anbieten, 0x02: auf Patrouille
 *     uint16_t preis;         // Verkaufspreis = 18,75 * preis. preis reicht von 0x0000 bis 0x0180
 *     uint16_t id;            // aufsteigender Wert, beginnend bei 0, gesunkene Schiffe eingeschlossen
 *     uint16_t typ;           // Schiffstyp. Auch der fahrende Händler zählt als Schiff.
 *     uint8_t g;              // 1, 2, 5
 *     uint8_t spieler;        // Spieler, dem das Schiff gehört. 0-3: normale Spieler, 4: fliegender Händler, 5: Piraten
 *     uint8_t h1;             // 0 bis 6
 *     uint8_t h2;             // 0xff, 0x10, 0x0d, 0x01, 0x06, 0x0e, 0x0b, 0x11, 0x07, 0x0a
 *     uint8_t h3;             // 0, selten 1
 *     uint8_t h4;             // 0 bis 3
 *     uint16_t richtung;      // Gegenwärtige Fahrtrichtung (0 bis 7)
 *
 *     // nach 82 bytes je 36 bytes pro eintrag
 *
 *     Handelsroute handelsrouten[8];
 *     uint16_t j;
 *     Laderaum ladung[8];
 * }
 * </pre>
 */
public class Ship4 {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    public enum ShipType {
        SMALL_TRADING_SHIP(0x15, 0) {
            @Override
            public String toString() {
                return "Small trading ship";
            }
        },
        LARGE_TRADING_SHIP(0x17, 32) {
            @Override
            public String toString() {
                return "Large trading ship";
            }
        },
        SMALL_WAR_SHIP(0x19, 64) {
            @Override
            public String toString() {
                return "Small war ship";
            }
        },
        LARGE_WAR_SHIP(0x1b, 48) {
            @Override
            public String toString() {
                return "Large war ship";
            }
        },
        FLYING_DEALER(0x1d, 16) {
            @Override
            public String toString() {
                return "Flying dealer";
            }
        },
        PIRATE_SHIP(0x1f, 80) {
            @Override
            public String toString() {
                return "Pirate ship";
            }
        };

        public int type;
        public int gfxIndex;
        private static final HashMap<Integer, ShipType> map = new HashMap<>();

        static {
            for (var shipType : ShipType.values()) {
                map.put(shipType.type, shipType);
            }
        }

        public static ShipType valueOfType(int type) {
            return map.get(type);
        }

        ShipType(int type, int gfxIndex) {
            this.type = type;
            this.gfxIndex = gfxIndex;
        }
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    public String name;
    public int xPos;
    public int yPos;
    public final int[] a = new int[3];
    public int courseStart;
    public int courseTarget;
    public int courseCurrent;
    public int b;
    public int health;
    public int c;
    public int d1;
    public int d2;
    public int numberOfCannons;
    public int flags;
    public int price;
    public int id;
    public int type;
    public int g;
    public int player;
    public int h1;
    public int h2;
    public int h3;
    public int h4;
    public int direction;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Ship4} object.
     *
     * @param chunk {@link Chunk}
     */
    public Ship4(Chunk chunk) {
        LOGGER.debug("Creates Ship4 object.");

        readData(Objects.requireNonNull(chunk, "chunk must not be null"));
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * For convenience; get position as {@link Vector2i}.
     *
     * @return The position as {@link Vector2i}
     */
    public Vector2i getPosition() {
        return new Vector2i(xPos, yPos);
    }

    //-------------------------------------------------
    // Gfx
    //-------------------------------------------------

    /**
     * Get the gfx index by given {@link #type}.
     *
     * @return int
     */
    public int getCurrentGfx() {
        var index = 0;
        switch (ShipType.valueOfType(type)) {
            case SMALL_TRADING_SHIP:
                index = SMALL_TRADING_SHIP.gfxIndex;
                break;
            case LARGE_TRADING_SHIP:
                index = LARGE_TRADING_SHIP.gfxIndex;
                break;
            case LARGE_WAR_SHIP:
                index = LARGE_WAR_SHIP.gfxIndex;
                break;
            case FLYING_DEALER:
                index = FLYING_DEALER.gfxIndex;
                break;
            case SMALL_WAR_SHIP:
                index = SMALL_WAR_SHIP.gfxIndex;
                break;
            case PIRATE_SHIP:
                index = PIRATE_SHIP.gfxIndex;
                break;
            default:
        }

        return index + direction;
    }

    //-------------------------------------------------
    // Read
    //-------------------------------------------------

    /**
     * Reads the {@link Ship4} data from the given {@link Chunk}.
     *
     * @param chunk {@link Chunk}
     */
    private void readData(Chunk chunk) {
        LOGGER.debug("Start reading the Ship4 data...");

        var r = new byte[28];
        chunk.getData().get(r);
        name = new String(r).split("\0")[0];

        xPos = shortToInt(chunk.getData().getShort());
        yPos = shortToInt(chunk.getData().getShort());

        for (int i = 0; i < a.length; i++) {
            a[i] = chunk.getData().getInt();
        }

        courseStart = chunk.getData().getInt();
        courseTarget = chunk.getData().getInt();
        courseCurrent = chunk.getData().getInt();
        b = chunk.getData().getInt();
        health = shortToInt(chunk.getData().getShort());
        c = shortToInt(chunk.getData().getShort());
        d1 = chunk.getData().get();
        d2 = chunk.getData().get();
        numberOfCannons = chunk.getData().get();
        flags = chunk.getData().get();
        price = shortToInt(chunk.getData().getShort());
        id = shortToInt(chunk.getData().getShort());
        type = shortToInt(chunk.getData().getShort());
        g = chunk.getData().get();
        player = chunk.getData().get();
        h1 = chunk.getData().get();
        h2 = chunk.getData().get();
        h3 = chunk.getData().get();
        h4 = chunk.getData().get();
        direction = shortToInt(chunk.getData().getShort());

        LOGGER.debug("Ship4 data read successfully.");
    }
}
