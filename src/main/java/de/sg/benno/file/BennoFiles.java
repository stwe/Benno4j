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
 * Determines the {@link Path} to the most important files and preloads files.
 */
public class BennoFiles {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * BSH files that are available in different zoom levels.
     */
    public enum ZoomableBshFileName {
        EFFEKTE_BSH("effekte.bsh"),
        FISCHE_BSH("fische.bsh"),
        GAUKLER_BSH("gaukler.bsh"),
        MAEHER_BSH("maeher.bsh"),
        NUMBERS_BSH("numbers.bsh"),
        SCHATTEN_BSH("schatten.bsh"),
        SHIP_BSH("ship.bsh"),
        SOLDAT_BSH("soldat.bsh"),
        STADTFLD_BSH("stadtfld.bsh"),
        TIERE_BSH("tiere.bsh"),
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
     * All other files.
     */
    public enum FileName {
        BAUHAUS_BSH("bauhaus.bsh"),
        START_BSH("start.bsh"),
        EDITOR_BSH("editor.bsh"),
        TOOLS_BSH("tools.bsh"),
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
     * A {@link HashMap} with the remaining {@link Path} objects, for example to the file with the color palette
     * or files needed for the menus.
     */
    private final HashMap<FileName, Path> filePaths = new HashMap<>();

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
     * @param path The game's root {@link Path}.
     * @throws IOException If an I/O error is thrown.
     */
    public BennoFiles(String path) throws IOException {
        LOGGER.debug("Creates BennoFiles object.");

        this.rootPath = Paths.get(Objects.requireNonNull(path, "path must not be null"));
        var home = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        this.savegamePath = Path.of(home + BennoConfig.SAVEGAME_PATH);

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

        // savegames
        findSavegameFiles();

        // zoom graphics in GFX, MGFX, SGFX
        findZoomableBshFiles(Zoom.SGFX);
        findZoomableBshFiles(Zoom.MGFX);
        findZoomableBshFiles(Zoom.GFX);
        checkForZoomableBshFiles();

        // other files (e.g. palette file, gui files)
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

        // todo: remove true
        //loadBshFile(getZoomableBshFilePath(Zoom.ZoomId.MGFX, ZoomableBshFileName.STADTFLD_BSH), true);

        LOGGER.debug("Successfully loaded files.");
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
                new BshFile(path, paletteFile.getPalette(), saveAsPng)
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
     * Searches for all zoomable BSH files and saves the found {@link Path} objects in a list.
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
     * @param zoom The {@link Path} to a {@link de.sg.benno.renderer.Zoom}.
     *
     * @return A list of {@link Path} objects.
     * @throws IOException If an I/O error is thrown.
     */
    private List<Path> listZoomableBshFiles(Path zoom) throws IOException {
        List<Path> result;

        try (var walk = Files.walk(rootPath)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".bsh"))
                    .filter(p -> checkZoomPath(p.getParent(), zoom))
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Checks if we are in the given zoom {@link Path}.
     *
     * @param path A given {@link Path}.
     * @param zoom A given {@link Path} to a {@link de.sg.benno.renderer.Zoom}.
     *
     * @return boolean
     */
    private static boolean checkZoomPath(Path path, Path zoom) {
        if (path.toString().toLowerCase().contains(zoom.toString().toLowerCase())) {
            return isContain(path.toString().toLowerCase(), zoom.toString().toLowerCase());
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
