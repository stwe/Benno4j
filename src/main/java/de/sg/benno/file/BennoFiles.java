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

package de.sg.benno.file;

import de.sg.benno.BennoConfig;
import de.sg.benno.BennoRuntimeException;
import de.sg.benno.util.TileAtlas;
import de.sg.benno.util.Util;
import de.sg.benno.chunk.Island;
import de.sg.benno.data.DataFiles;
import de.sg.benno.renderer.Zoom;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Determines the {@link Path} to the most important files for preloading.
 */
public class BennoFiles {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    /**
     * BSH files that are available in different zoom levels.
     */
    public enum ZoomableBshFileName {

        /**
         * Effects
         */
        EFFEKTE_BSH("effekte.bsh"),

        /**
         * Sea life
         */
        FISCHE_BSH("fische.bsh"),

        /**
         * Jugglers
         */
        GAUKLER_BSH("gaukler.bsh"),

        /**
         * Farmers
         */
        MAEHER_BSH("maeher.bsh"),

        /**
         * Numbers 0-9
         */
        NUMBERS_BSH("numbers.bsh"),

        /**
         * Shadows
         */
        SCHATTEN_BSH("schatten.bsh"),

        /**
         * Ships
         */
        SHIP_BSH("ship.bsh"),

        /**
         * Soldiers
         */
        SOLDAT_BSH("soldat.bsh"),

        /**
         * Buildings and terrain
         */
        STADTFLD_BSH("stadtfld.bsh"),

        /**
         * Animals
         */
        TIERE_BSH("tiere.bsh"),

        /**
         * Workers
         */
        TRAEGER_BSH("traeger.bsh");

        private final String fileName;

        ZoomableBshFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return fileName;
        }
    }

    /**
     * All other files in ToolGfx. Interface graphics and more.
     */
    public enum FileName {
        /**
         * Building thumbnails and terrain.
         */
        BAUHAUS_BSH("bauhaus.bsh"),

        /**
         * Menu graphics.
         */
        START_BSH("start.bsh"),

        /**
         * Editor interface graphics.
         */
        EDITOR_BSH("editor.bsh"),

        /**
         * Interface graphics.
         */
        TOOLS_BSH("tools.bsh"),

        /**
         * The palette file.
         */
        PALETTE_COL("stadtfld.col");

        private final String fileName;

        FileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return fileName;
        }
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The game's root {@link Path}.
     */
    private final Path rootPath;

    /**
     * The root {@link Path} to the savegames.
     */
    private final Path savegamePath;

    /**
     * A {@link ArrayList} with {@link Path} objects to the savegame files.
     */
    private final ArrayList<Path> savegameFilePaths = new ArrayList<>();

    /**
     * A {@link HashMap} with {@link Path} objects to zoomable BSH files.
     */
    private final HashMap<Zoom, List<Path>> zoomableBshFilePaths = new HashMap<>();

    /**
     * A {@link HashMap} with the remaining {@link Path} objects in ToolGfx, for example to the file with the
     * color palette or files needed for the menus.
     */
    private final HashMap<FileName, Path> filePaths = new HashMap<>();

    /**
     * A {@link HashMap} with {@link Path} objects to SCP files.
     */
    private final HashMap<Island.IslandClimate, List<Path>> scpFilePaths = new HashMap<>();

    /**
     * A {@link PaletteFile} object.
     */
    private PaletteFile paletteFile;

    /**
     * A {@link HashMap} of {@link BshFile} objects.
     */
    private final HashMap<Path, BshFile> bshFiles = new HashMap<>();

    /**
     * A {@link DataFiles} object.
     */
    private DataFiles dataFiles;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link BennoFiles} object.
     *
     * @throws IOException If an I/O error is thrown.
     */
    public BennoFiles() throws IOException {
        LOGGER.debug("Creates BennoFiles object.");

        this.rootPath = Paths.get(Objects.requireNonNull(BennoConfig.ROOT_PATH, "root path must not be null"));
        var home = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        this.savegamePath = Path.of(home + Objects.requireNonNull(BennoConfig.SAVEGAME_PATH, "savegame path must not be null"));

        LOGGER.debug("Home directory found at {}.", home);
        LOGGER.debug("Search savegames at {}.", this.savegamePath);

        initPaths();
        preloadFiles();
        createTileAtlasIfRequired();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #savegameFilePaths}.
     *
     * @return {@link #savegameFilePaths}
     */
    public ArrayList<Path> getSavegameFilePaths() {
        return savegameFilePaths;
    }

    /**
     * Get zoomable BSH file {@link Path} from {@link #zoomableBshFilePaths}.
     *
     * @param zoom {@link de.sg.benno.renderer.Zoom}
     * @param zoomableBshFileName {@link ZoomableBshFileName}
     *
     * @return {@link Path}
     */
    public Path getZoomableBshFilePath(Zoom zoom, ZoomableBshFileName zoomableBshFileName) {
        for (var val : zoomableBshFilePaths.get(zoom)) {
            if (val.toString().toLowerCase().contains(zoomableBshFileName.toString())) {
                return val;
            }
        }

        throw new BennoRuntimeException("The BSH file " + zoomableBshFileName + " could not found at " + rootPath + ".");
    }

    /**
     * Get SCP file {@link Path} from {@link #scpFilePaths}.
     *
     * @param climate {@link de.sg.benno.chunk.Island.IslandClimate}
     * @param scpFileName The name of the SCP file.
     *
     * @return {@link Path}
     */
    public Path getScpFilePath(Island.IslandClimate climate, String scpFileName) {
        if (scpFileName.isEmpty()) {
            throw new BennoRuntimeException("Invalid SCP filename given.");
        }

        for (var val : scpFilePaths.get(climate)) {
            if (val.toString().toLowerCase().contains(scpFileName)) {
                return val;
            }
        }

        throw new BennoRuntimeException("The SCP file " + scpFileName + " could not found at " + rootPath + ".");
    }

    /**
     * Get {@link Path} from {@link #filePaths}.
     *
     * @param fileName {@link FileName}
     *
     * @return {@link Path}
     */
    public Path getFilePath(FileName fileName) {
        return filePaths.get(fileName);
    }

    /**
     * Get {@link #paletteFile}.
     *
     * @return {@link #paletteFile}
     */
    public PaletteFile getPaletteFile() {
        return paletteFile;
    }

    /**
     * Get a {@link BshFile}. This will be loaded if not already done.
     *
     * @param path A {@link Path} to a {@link BshFile}.
     *
     * @return {@link BshFile}
     * @throws IOException If an I/O error is thrown.
     */
    public BshFile getBshFile(Path path) throws IOException {
        if (!bshFiles.containsKey(path)) {
            loadBshFile(path);
        }

        return bshFiles.get(path);
    }

    /**
     * Convenience function to get <i>stadtfld.bsh</i> {@link BshFile} by {@link de.sg.benno.renderer.Zoom}.
     *
     * @param zoom {@link de.sg.benno.renderer.Zoom}
     *
     * @return The <i>stadtfld.bsh</i> {@link BshFile}
     * @throws IOException If an I/O error is thrown.
     */
    public BshFile getStadtfldBshFile(Zoom zoom) throws IOException {
        return getBshFile(getZoomableBshFilePath(zoom, BennoFiles.ZoomableBshFileName.STADTFLD_BSH));
    }

    /**
     * Convenience function to get <i>ship.bsh</i> {@link BshFile} by {@link de.sg.benno.renderer.Zoom}.
     *
     * @param zoom {@link de.sg.benno.renderer.Zoom}
     *
     * @return The <i>ship.bsh</i> {@link BshFile}
     * @throws IOException If an I/O error is thrown.
     */
    public BshFile getShipBshFile(Zoom zoom) throws IOException {
        return getBshFile(getZoomableBshFilePath(zoom, ZoomableBshFileName.SHIP_BSH));
    }

    /**
     * Get {@link #dataFiles}.
     *
     * @return {@link #dataFiles}
     */
    public DataFiles getDataFiles() {
        return dataFiles;
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up {@link #bshFiles}.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up BennoFiles.");

        for (var bshFile : bshFiles.values()) {
            bshFile.cleanUp();
        }
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Searches for files and saves the paths found as {@link Path}.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void initPaths() throws IOException {
        LOGGER.debug("Starts initializing filesystem from root path {}...", rootPath);

        // store all savegame paths in a list
        findSavegameFiles();

        // store the path of all zoom graphics from GFX, MGFX, SGFX in a list
        findZoomableBshFiles(Zoom.SGFX);
        findZoomableBshFiles(Zoom.MGFX);
        findZoomableBshFiles(Zoom.GFX);

        // checks that all zoomable BSH files have been found
        checkForZoomableBshFiles();

        // store the path of all SCP files in a list
        findScpFiles(Island.IslandClimate.NORTH);
        findScpFiles(Island.IslandClimate.SOUTH);

        // other files (e.g. palette file, gui files) in ToolGfx
        findToolGfxFiles();

        LOGGER.debug("Successfully initialized filesystem.");
    }

    //-------------------------------------------------
    // Load
    //-------------------------------------------------

    /**
     * Preloads some files.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void preloadFiles() throws IOException {
        LOGGER.debug("Starts preloading some files ...");

        LOGGER.debug("Preload palette file.");
        paletteFile = new PaletteFile(getFilePath(FileName.PALETTE_COL));

        loadBshFile(getFilePath(FileName.BAUHAUS_BSH));
        //loadBshFile(getFilePath(FileName.EDITOR_BSH));

        //LOGGER.debug("Preload main menu graphics.");
        //loadBshFile(getFilePath(FileName.START_BSH));

        //loadBshFile(InterfaceBshFileName.TOOLS); // 670

        LOGGER.debug("Preload buildings and terrain.");
        loadBshFile(getZoomableBshFilePath(Zoom.GFX, ZoomableBshFileName.STADTFLD_BSH), Zoom.GFX, BennoConfig.CREATE_STADTFLD_GFX_PNG);
        loadBshFile(getZoomableBshFilePath(Zoom.MGFX, ZoomableBshFileName.STADTFLD_BSH), Zoom.MGFX, BennoConfig.CREATE_STADTFLD_MGFX_PNG);
        loadBshFile(getZoomableBshFilePath(Zoom.SGFX, ZoomableBshFileName.STADTFLD_BSH), Zoom.SGFX, BennoConfig.CREATE_STADTFLD_SGFX_PNG);

        LOGGER.debug("Preload ships.");
        loadBshFile(getZoomableBshFilePath(Zoom.GFX, ZoomableBshFileName.SHIP_BSH), Zoom.GFX, BennoConfig.CREATE_SHIP_GFX_PNG);
        loadBshFile(getZoomableBshFilePath(Zoom.MGFX, ZoomableBshFileName.SHIP_BSH), Zoom.MGFX, BennoConfig.CREATE_SHIP_MGFX_PNG);
        loadBshFile(getZoomableBshFilePath(Zoom.SGFX, ZoomableBshFileName.SHIP_BSH), Zoom.SGFX, BennoConfig.CREATE_SHIP_SGFX_PNG);

        LOGGER.debug("Preload data files.");
        dataFiles = new DataFiles();

        LOGGER.debug("Successfully preload files.");
    }

    /**
     * Loads a BSH file from the given {@link Path} and creates a {@link BshFile} object.
     * The loaded {@link BshFile} object is saved in {@link #bshFiles}.
     *
     * @param path The {@link Path} to the BSH file.
     * @param zoom The {@link Zoom} of the BSH file.
     * @param saveAsPng Is true if the textures should also be saved as Png.
     * @throws IOException If an I/O error is thrown.
     */
    private void loadBshFile(Path path, Zoom zoom, boolean saveAsPng) throws IOException {
        bshFiles.put(
                path,
                new BshFile(
                        Objects.requireNonNull(path, "path must not be null"),
                        Objects.requireNonNull(paletteFile, "paletteFile must not be null").getPalette(),
                        zoom,
                        saveAsPng
                )
        );
    }

    /**
     * Loads a BSH file from the given {@link Path} and creates a {@link BshFile} object.
     * The loaded {@link BshFile} object is saved in {@link #bshFiles}.
     *
     * @param path The {@link Path} to the BSH file.
     * @param saveAsPng Is true if the textures should also be saved as Png.
     * @throws IOException If an I/O error is thrown.
     */
    private void loadBshFile(Path path, boolean saveAsPng) throws IOException {
        loadBshFile(path, null, saveAsPng);
    }

    /**
     * Loads a BSH file from the given {@link Path} and creates a {@link BshFile} object.
     * The loaded {@link BshFile} object is saved in {@link #bshFiles}.
     *
     * @param path The {@link Path} to the BSH file.
     * @throws IOException If an I/O error is thrown.
     */
    private void loadBshFile(Path path) throws IOException {
        loadBshFile(path, false);
    }

    /**
     * Creates the tile atlas images.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void createTileAtlasIfRequired() throws IOException {
        if (!BennoConfig.CREATE_ATLAS_IMAGES) {
            // check atlas images in SGFX
            for (var i = 0; i < TileAtlas.NR_OF_SGFX_ATLAS_IMAGES; i++) {
                Util.getFileFromResourceAsStream(BennoConfig.ATLAS_SGFX_PATH + i + ".png");
            }

            // check atlas images in MGFX
            for (var i = 0; i < TileAtlas.NR_OF_MGFX_ATLAS_IMAGES; i++) {
                Util.getFileFromResourceAsStream(BennoConfig.ATLAS_MGFX_PATH + i + ".png");
            }

            // check atlas images in GFX
            for (var i = 0; i < TileAtlas.NR_OF_GFX_ATLAS_IMAGES; i++) {
                Util.getFileFromResourceAsStream(BennoConfig.ATLAS_GFX_PATH + i + ".png");
            }
        }

        if (BennoConfig.CREATE_ATLAS_IMAGES) {
            LOGGER.warn("Start tile atlas creation ...");

            createSgfxAtlas();
            createMgfxAtlas();
            createGfxAtlas();

            LOGGER.warn("Tile atlas images successfully created.");

            throw new BennoRuntimeException("Deactivate the creation of the tile atlas files, rebuild and restart the program.");
        }
    }

    //-------------------------------------------------
    // Tile atlas
    //-------------------------------------------------

    /**
     * Creates and stores the SGFX atlas images.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void createSgfxAtlas() throws IOException {
        if (Util.getMaxTextureSize() < (int)TileAtlas.MAX_SGFX_HEIGHT * TileAtlas.NR_OF_SGFX_ROWS) {
            throw new BennoRuntimeException("The supported texture size should be at least " + (int)TileAtlas.MAX_SGFX_HEIGHT * TileAtlas.NR_OF_SGFX_ROWS);
        }

        var stadtfldFile = getStadtfldBshFile(Zoom.SGFX);
        var bshTextures = stadtfldFile.getBshTextures();
        var atlasImages = new ArrayList<BufferedImage>();

        var c = 0;
        for (var i = 0; i < TileAtlas.NR_OF_SGFX_ATLAS_IMAGES; i++) {
            // new atlas
            var atlas = new BufferedImage(
                    (int)TileAtlas.MAX_SGFX_WIDTH * TileAtlas.NR_OF_SGFX_ROWS,
                    (int)TileAtlas.MAX_SGFX_HEIGHT * TileAtlas.NR_OF_SGFX_ROWS,
                    BufferedImage.TYPE_INT_ARGB
            );

            // draw bsh images
            for (var y = 0; y < TileAtlas.NR_OF_SGFX_ROWS; y++) {
                for (var x = 0; x < TileAtlas.NR_OF_SGFX_ROWS; x++) {
                    var g = atlas.getGraphics();

                    // only if index exists
                    if (c >= 0 && c < bshTextures.size()) {
                        // draw in atlas
                        g.drawImage(
                                bshTextures.get(c).getBufferedImage(),
                                x * (int)TileAtlas.MAX_SGFX_WIDTH,
                                y * (int)TileAtlas.MAX_SGFX_HEIGHT,
                                null
                        );
                    }

                    c++;
                }
            }

            // store atlas
            atlasImages.add(atlas);
        }

        writeAtlasImages(BennoConfig.ATLAS_OUT_PATH + BennoConfig.ATLAS_SGFX_PATH, atlasImages);
    }

    /**
     * Creates and stores the MGFX atlas images.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void createMgfxAtlas() throws IOException {
        if (Util.getMaxTextureSize() < (int)TileAtlas.MAX_MGFX_HEIGHT * TileAtlas.NR_OF_MGFX_ROWS) {
            throw new BennoRuntimeException("The supported texture size should be at least " + (int)TileAtlas.MAX_MGFX_HEIGHT * TileAtlas.NR_OF_MGFX_ROWS);
        }

        var stadtfldFile = getStadtfldBshFile(Zoom.MGFX);
        var bshTextures = stadtfldFile.getBshTextures();
        var atlasImages = new ArrayList<BufferedImage>();

        var c = 0;
        for (var i = 0; i < TileAtlas.NR_OF_MGFX_ATLAS_IMAGES; i++) {
            // new atlas
            var atlas = new BufferedImage(
                    (int)TileAtlas.MAX_MGFX_WIDTH * TileAtlas.NR_OF_MGFX_ROWS,
                    (int)TileAtlas.MAX_MGFX_HEIGHT * TileAtlas.NR_OF_MGFX_ROWS,
                    BufferedImage.TYPE_INT_ARGB
            );

            // draw bsh images
            for (var y = 0; y < TileAtlas.NR_OF_MGFX_ROWS; y++) {
                for (var x = 0; x < TileAtlas.NR_OF_MGFX_ROWS; x++) {
                    var g = atlas.getGraphics();

                    // only if index exists
                    if (c >= 0 && c < bshTextures.size()) {
                        // draw in atlas
                        g.drawImage(
                                bshTextures.get(c).getBufferedImage(),
                                x * (int)TileAtlas.MAX_MGFX_WIDTH,
                                y * (int)TileAtlas.MAX_MGFX_HEIGHT,
                                null
                        );
                    }

                    c++;
                }
            }

            // store atlas
            atlasImages.add(atlas);
        }

        writeAtlasImages(BennoConfig.ATLAS_OUT_PATH + BennoConfig.ATLAS_MGFX_PATH, atlasImages);
    }

    /**
     * Creates and stores the GFX atlas images.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void createGfxAtlas() throws IOException {
        if (Util.getMaxTextureSize() < (int)TileAtlas.MAX_GFX_HEIGHT * TileAtlas.NR_OF_GFX_ROWS) {
            throw new BennoRuntimeException("The supported texture size should be at least " + (int)TileAtlas.MAX_GFX_HEIGHT * TileAtlas.NR_OF_GFX_ROWS);
        }

        var stadtfldFile = getStadtfldBshFile(Zoom.GFX);
        var bshTextures = stadtfldFile.getBshTextures();
        var atlasImages = new ArrayList<BufferedImage>();

        var c = 0;
        for (var i = 0; i < TileAtlas.NR_OF_GFX_ATLAS_IMAGES; i++) {
            // new atlas
            var atlas = new BufferedImage(
                    (int)TileAtlas.MAX_GFX_WIDTH * TileAtlas.NR_OF_GFX_ROWS,
                    (int)TileAtlas.MAX_GFX_HEIGHT * TileAtlas.NR_OF_GFX_ROWS,
                    BufferedImage.TYPE_INT_ARGB
            );

            // draw bsh images
            for (var y = 0; y < TileAtlas.NR_OF_GFX_ROWS; y++) {
                for (var x = 0; x < TileAtlas.NR_OF_GFX_ROWS; x++) {
                    var g = atlas.getGraphics();

                    // only if index exists
                    if (c >= 0 && c < bshTextures.size()) {
                        // draw in atlas
                        g.drawImage(
                                bshTextures.get(c).getBufferedImage(),
                                x * (int)TileAtlas.MAX_GFX_WIDTH,
                                y * (int)TileAtlas.MAX_GFX_HEIGHT,
                                null
                        );
                    }

                    c++;
                }
            }

            // store atlas
            atlasImages.add(atlas);
        }

        writeAtlasImages(BennoConfig.ATLAS_OUT_PATH + BennoConfig.ATLAS_GFX_PATH, atlasImages);
    }

    /**
     * Writes {@link BufferedImage} objects to PNG files.
     *
     * @param path The output path.
     * @param bufferedImages The {@link BufferedImage} objects to be written.
     * @throws IOException If an I/O error is thrown.
     */
    private void writeAtlasImages(String path, ArrayList<BufferedImage> bufferedImages) throws IOException {
        var c = 0;

        Files.createDirectories(Paths.get(path));

        for (var atlas : bufferedImages) {
            String filename = path + c + ".png";

            var file = new File(filename);
            if (!file.exists()) {
                var result = file.createNewFile();
                if (!result) {
                    throw new BennoRuntimeException("Unexpected error.");
                }
            }

            ImageIO.write(atlas, "PNG", file);

            atlas.getGraphics().dispose();

            c++;
        }
    }

    //-------------------------------------------------
    // Find files
    //-------------------------------------------------

    /**
     * Searches for all savegame files and saves the found {@link Path} objects in a list.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void findSavegameFiles() throws IOException {
        for (var path : listSavegameFiles()) {
            LOGGER.debug("Found savegame file at {}.", path);
            savegameFilePaths.add(path);
        }
    }

    /**
     * Searches for zoomable BSH files and saves the found {@link Path} objects in a list.
     *
     * @param zoom {@link de.sg.benno.renderer.Zoom}.
     * @throws IOException If an I/O error is thrown.
     */
    private void findZoomableBshFiles(Zoom zoom) throws IOException {
        var paths = listZoomableBshFiles(Paths.get(zoom.toString()));

        if (paths.isEmpty()) {
            throw new BennoRuntimeException("No " + zoom + " BSH files found at " + rootPath + ".");
        }

        zoomableBshFilePaths.put(zoom, paths);

        LOGGER.debug("Found {} BSH files in {}.", paths.size(), zoom.toString());
    }

    /**
     * Searches for SCP files and saves the found {@link Path} objects in a list.
     *
     * @param climate {@link de.sg.benno.chunk.Island.IslandClimate}.
     * @throws IOException If an I/O error is thrown.
     */
    private void findScpFiles(Island.IslandClimate climate) throws IOException {
        var paths = listScpFiles(Paths.get(climate.toString()));

        if (paths.isEmpty()) {
            throw new BennoRuntimeException("No " + climate + " SCP files found at " + rootPath + ".");
        }

        scpFilePaths.put(climate, paths);

        LOGGER.debug("Found {} SCP files in {}.", paths.size(), climate.toString());
    }

    /**
     * Searches in the ToolGfx directory for files needed by menues or to load the color palette.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void findToolGfxFiles() throws IOException {
        for (var file : FileName.values()) {
            var paths = listToolGfxFile(file.toString());
            addFilePath(paths, file);
        }
    }

    /**
     * Checks whether the given file was found and saves the first entry found.
     *
     * @param paths A list of {@link Path} objects.
     * @param fileName A {@link FileName} to check and add.
     */
    private void addFilePath(List<Path> paths, FileName fileName) {
        if (paths.isEmpty()) {
            throw new BennoRuntimeException("The file " + fileName.toString() + " could not found at " + rootPath + ".");
        }

        LOGGER.debug("Found file {} at {}.", fileName.toString(), paths.get(0));

        filePaths.put(fileName, paths.get(0));
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Checks whether all {@link ZoomableBshFileName} were found.
     */
    private void checkForZoomableBshFiles() {
        for (var zoomId : Zoom.values()) {
            for (var bshFile : ZoomableBshFileName.values()) {
                var result = false;
                String path = "";
                for (var val : zoomableBshFilePaths.get(zoomId)) {
                    if (val.toString().toLowerCase().contains(bshFile.toString())) {
                        result = true;
                        path = val.toString();
                        break;
                    }
                }

                if (!result) {
                    throw new BennoRuntimeException("The BSH file " + bshFile.toString() + " could not found at " + rootPath + ".");
                }

                LOGGER.info("Found BSH file {}/{} at {}.", zoomId, bshFile.toString(), path);
            }
        }
    }

    /**
     * Searches for savegames.
     *
     * @return A list of {@link Path} objects.
     * @throws IOException If an I/O error is thrown.
     */
    private List<Path> listSavegameFiles() throws IOException {
        List<Path> result;

        try (var walk = Files.walk(savegamePath, 1)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".gam"))
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Searches for zoomable BSH files.
     *
     * @param zoomPath The {@link Path} to a {@link de.sg.benno.renderer.Zoom}.
     *
     * @return A list of {@link Path} objects.
     * @throws IOException If an I/O error is thrown.
     */
    private List<Path> listZoomableBshFiles(Path zoomPath) throws IOException {
        List<Path> result;

        try (var walk = Files.walk(rootPath)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".bsh"))
                    .filter(p -> checkPath(p.getParent(), zoomPath))
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Searches for SCP files.
     *
     * @param climatePath The {@link Path} to a {@link de.sg.benno.chunk.Island.IslandClimate}.
     *
     * @return A list of {@link Path} objects.
     * @throws IOException If an I/O error is thrown.
     */
    private List<Path> listScpFiles(Path climatePath) throws IOException  {
        List<Path> result;

        try (var walk = Files.walk(rootPath)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".scp"))
                    .filter(p -> checkPath(p.getParent(), climatePath))
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Checks if we are in a given {@link Path}.
     *
     * @param path A given {@link Path}.
     * @param otherPath A given other {@link Path}.
     *
     * @return boolean
     */
    private static boolean checkPath(Path path, Path otherPath) {
        if (path.toString().toLowerCase().contains(otherPath.toString().toLowerCase())) {
            return isContain(path.toString().toLowerCase(), otherPath.toString().toLowerCase());
        }

        return false;
    }

    /**
     * Checks if String contains exact a keyword.
     *
     * @param source The source to be checked.
     * @param subItem The keyword.
     *
     * @return boolean
     */
    private static boolean isContain(String source, String subItem){
        String str = "\\b" + subItem + "\\b";
        var pattern = Pattern.compile(str);
        var matcherSource = pattern.matcher(source);

        return matcherSource.find();
    }

    /**
     * Looks for a file in the ToolGfx directory.
     *
     * @param fileName The name of the file to be searched for.
     *
     * @return A list of {@link Path} objects.
     * @throws IOException If an I/O error is thrown.
     */
    private List<Path> listToolGfxFile(String fileName) throws IOException {
        List<Path> result;

        try (var walk = Files.walk(rootPath, 2)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().contains(fileName))
                    .filter(p -> p.getParent().toString().toLowerCase().contains("toolgfx"))
                    .collect(Collectors.toList());
        }

        return result;
    }
}
