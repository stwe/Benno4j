/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.BennoConfig;
import de.sg.benno.BennoRuntimeException;
import de.sg.benno.chunk.Island;
import de.sg.benno.data.DataFiles;
import de.sg.benno.renderer.Zoom;

import javax.swing.filechooser.FileSystemView;
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

import static de.sg.ogl.Log.LOGGER;

/**
 * Determines the {@link Path} to the most important files for preloading.
 */
public class BennoFiles {

    //-------------------------------------------------
    // Constants
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
    private final DataFiles dataFiles;

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

        dataFiles = new DataFiles();
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

    /**
     * Preloads some files.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void preloadFiles() throws IOException {
        LOGGER.debug("Starts preloading some files ...");

        // preload palette file (stadtfld.col)
        paletteFile = new PaletteFile(getFilePath(FileName.PALETTE_COL));

        // preload start.bsh
        //loadBshFile(InterfaceBshFileName.BAUHAUS);
        //loadBshFile(InterfaceBshFileName.EDITOR); // 950
        loadBshFile(getFilePath(FileName.START_BSH));
        //loadBshFile(InterfaceBshFileName.TOOLS); // 670

        //loadBshFile(getZoomableBshFilePath(Zoom.MGFX, ZoomableBshFileName.STADTFLD_BSH), true);

        LOGGER.debug("Successfully preload files.");
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
        bshFiles.put(
                path,
                new BshFile(path, Objects.requireNonNull(paletteFile, "paletteFile must not be null").getPalette(), saveAsPng)
        );
    }

    /**
     * Loads a BSH file and creates a {@link BshFile} object.
     * The loaded {@link BshFile} object is saved in {@link #bshFiles}.
     *
     * @param path The {@link Path} to the BSH file.
     * @throws IOException If an I/O error is thrown.
     */
    private void loadBshFile(Path path) throws IOException {
        loadBshFile(path, false);
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
