/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.chunk;

/**
 * Common stuff for all Islands.
 */
public class Island {

    /**
     * Maps the 8-byte C structure TERZBERG.
     * <pre>
     * typedef struct {
     *     uint8_t ware;
     *     uint8_t posx;
     *     uint8_t posy;
     *     uint8_t playerflags;
     *     uint8_t kind;
     *     uint8_t leer1;
     *     uint16_t lager;
     * } TERZBERG;
     * </pre>
     */
    public static class OreMountainData {

        /**
         * The goods that lie here.
         */
        public int good;

        /**
         * The x position on island.
         */
        public int xPosOnIsland;

        /**
         * The y position on island.
         */
        public int yPosOnIsland;

        /**
         * The player who knows the place.
         */
        public int playerFlags;

        /**
         * The type of goods.
         */
        public int type;

        /**
         * Is unknown.
         */
        public int empty;

        /**
         * The quantity of the goods.
         */
        public int stock;
    }

    /**
     * Represents the fertility of an island.
     */
    public enum Fertility {
        RANDOM(0x0000) {
            @Override
            public String toString() {
                return "Random";
            }
        },
        NONE(0x1181) {
            @Override
            public String toString() {
                return "None";
            }
        },
        TOBACCO_ONLY(0x1183) {
            @Override
            public String toString() {
                return "Tobacco only";
            }
        },
        WINE_ONLY(0x11A1) {
            @Override
            public String toString() {
                return "Wine only";
            }
        },
        SUGAR_ONLY(0x1189) {
            @Override
            public String toString() {
                return "Sugar only";
            }
        },
        COCOA_ONLY(0x11C1) {
            @Override
            public String toString() {
                return "Cocoa only";
            }
        },
        WOOL_ONLY(0x1191) {
            @Override
            public String toString() {
                return "Wool only";
            }
        },
        SPICES_ONLY(0x1185) {
            @Override
            public String toString() {
                return "Spices only";
            }
        },
        TOBACCO_AND_SPICES(0x1187) {
            @Override
            public String toString() {
                return "Tobacco and Spices";
            }
        },
        TOBACCO_AND_SUGAR(0x118B) {
            @Override
            public String toString() {
                return "Tobacco and Sugar";
            }
        },
        SPICES_AND_SUGAR(0x118D) {
            @Override
            public String toString() {
                return "Spices and Sugar";
            }
        },
        WOOL_AND_WINE(0x11B1) {
            @Override
            public String toString() {
                return "Wool and Wine";
            }
        },
        WOOL_AND_COCOA(0x11D1) {
            @Override
            public String toString() {
                return "Wool and Cocoa";
            }
        },
        WINE_AND_COCOA(0x11E1) {
            @Override
            public String toString() {
                return "Wine and Cocoa";
            }
        },

        UNKNOWN0(4549) {
            @Override
            public String toString() {
                return "UNKNOWN0";
            }
        },

        UNKNOWN1(4515) {
            @Override
            public String toString() {
                return "UNKNOWN1";
            }
        },

        UNKNOWN2(4501) {
            @Override
            public String toString() {
                return "UNKNOWN2";
            }
        };

        public final int value;

        Fertility(int value) {
            this.value = value;
        }
    }

    /**
     * Represents the size of an island.
     */
    public enum IslandSize {
        LITTLE {
            @Override
            public String toString() {
                return "Little";
            }
        },
        MIDDLE {
            @Override
            public String toString() {
                return "Middle";
            }
        },
        MEDIAN {
            @Override
            public String toString() {
                return "Median";
            }
        },
        BIG {
            @Override
            public String toString() {
                return "Big";
            }
        },
        LARGE {
            @Override
            public String toString() {
                return "Large";
            }
        }
    }

    /**
     * Represents the climate of an island.
     */
    public enum IslandClimate {
        NORTH {
            @Override
            public String toString() {
                return "North";
            }
        },
        SOUTH {
            @Override
            public String toString() {
                return "South";
            }
        },
        ANY {
            @Override
            public String toString() {
                return "Any";
            }
        }
    }
}
