/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.renderer.Zoom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.sg.ogl.Log.LOGGER;

public class BennoFiles {

    private interface FileName {
        String getFileName();
    }

    public enum ZoomableBshFileName implements FileName {
        EFFEKTE("effekte.bsh"),
        FISCHE("fische.bsh"),
        GAUKLER("gaukler.bsh"),
        MAEHER("maeher.bsh"),
        NUMBERS("numbers.bsh"),
        SCHATTEN("schatten.bsh"),
        SHIP("ship.bsh"),
        SOLDAT("soldat.bsh"),
        STADTFLD("stadtfld.bsh"),
        TIERE("tiere.bsh"),
        TRAEGER("traeger.bsh");

        private final String fileName;

        ZoomableBshFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getFileName() {
            return fileName;
        }
    }

    public enum InterfaceBshFileName implements FileName {
        BAUHAUS("bauhaus.bsh"),
        START("start.bsh"),
        EDITOR("editor.bsh"),
        TOOLS("tools.bsh");

        private final String fileName;

        InterfaceBshFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getFileName() {
            return fileName;
        }
    }

    public enum OtherFileName implements FileName {
        PALETTE("stadtfld.col");

        private final String fileName;

        OtherFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getFileName() {
            return fileName;
        }
    }

    private final Path rootPath;

    private final HashMap<Zoom.ZoomId, List<Path>> zoomableBshFilePaths = new HashMap<>();
    private final HashMap<FileName, Path> filePaths = new HashMap<>();

    private PaletteFile paletteFile;
    private final HashMap<FileName, BshFile> bshFiles = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BennoFiles(String path) throws IOException {
        LOGGER.debug("Creates BennoFiles object.");

        this.rootPath = Paths.get(Objects.requireNonNull(path, "path must not be null"));

        initPaths();
        preloadFiles();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public Path getZoomableBshFilePath(Zoom.ZoomId zoomId, ZoomableBshFileName bshFileName) {
        for (var val : zoomableBshFilePaths.get(zoomId)) {
            if (val.toString().toLowerCase().contains(bshFileName.fileName)) {
                return val;
            }
        }

        throw new BennoRuntimeException("The BSH file " + bshFileName.getFileName() + " could not found at " + rootPath + ".");
    }

    public Path getFilePath(FileName fileName) {
        return filePaths.get(fileName);
    }

    public PaletteFile getPaletteFile() {
        return paletteFile;
    }

    public BshFile getBshFile(FileName fileName) throws IOException {
        if (!bshFiles.containsKey(fileName)) {
            loadBshFile(fileName);
        }

        return bshFiles.get(fileName);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    public void cleanUp() {
        LOGGER.debug("Clean up BennoFiles.");

        for (var bshFile : bshFiles.values()) {
            bshFile.cleanUp();
        }
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    private void initPaths() throws IOException {
        LOGGER.debug("Starts initializing filesystem from path {}...", rootPath);

        // Zoom graphics in GFX, MGFX, SGFX
        findZoomableBshFiles(Zoom.ZoomId.SGFX);
        findZoomableBshFiles(Zoom.ZoomId.MGFX);
        findZoomableBshFiles(Zoom.ZoomId.GFX);
        checkForZoomableBshFiles();

        // Interface graphics and other files (e.g. palette file)
        findToolGfxFiles();

        LOGGER.debug("Successfully initialized filesystem.");
    }

    private void preloadFiles() throws IOException {
        LOGGER.debug("Starts preloading some files ...");

        // preload palette file (stadtfld.col)
        paletteFile = new PaletteFile(filePaths.get(OtherFileName.PALETTE));

        // preload start.bsh
        //loadBshFile(InterfaceBshFileName.BAUHAUS);
        //loadBshFile(InterfaceBshFileName.EDITOR); //950
        loadBshFile(InterfaceBshFileName.START);
        //loadBshFile(InterfaceBshFileName.TOOLS); // 670

        LOGGER.debug("Successfully loaded files.");
    }

    private void loadBshFile(FileName fileName, boolean saveAsPng) throws IOException {
        bshFiles.put(
                fileName,
                new BshFile(filePaths.get(fileName), paletteFile.getPalette(), saveAsPng)
        );
    }

    private void loadBshFile(FileName fileName) throws IOException {
        loadBshFile(fileName, false);
    }

    //-------------------------------------------------
    // Find files
    //-------------------------------------------------

    private void findZoomableBshFiles(Zoom.ZoomId zoomId) throws IOException {
        var paths = listZoomableBshFiles(Paths.get(zoomId.toString()));

        if (paths.isEmpty()) {
            throw new BennoRuntimeException("No " + zoomId.toString() + " BSH files found at " + rootPath + ".");
        }

        zoomableBshFilePaths.put(zoomId, paths);
    }

    private void findToolGfxFiles() throws IOException {
        for (var bshFile : InterfaceBshFileName.values()) {
            var paths = listToolGfxFile(bshFile.fileName);
            addFilePath(paths, bshFile);
        }

        for (var otherFile : OtherFileName.values()) {
            var paths = listToolGfxFile(otherFile.fileName);
            addFilePath(paths, otherFile);
        }
    }

    private void addFilePath(List<Path> pathList, FileName fileName) {
        if (pathList.isEmpty()) {
            throw new BennoRuntimeException("The file " + fileName.getFileName() + " could not found at " + rootPath + ".");
        }

        LOGGER.debug("Found file {} at {}.", fileName.getFileName(), pathList.get(0));

        filePaths.put(fileName, pathList.get(0));
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void checkForZoomableBshFiles() {
        for (var zoomId : Zoom.ZoomId.values()) {
            for (var bshFile : ZoomableBshFileName.values()) {
                var result = false;
                String path = "";
                for (var val : zoomableBshFilePaths.get(zoomId)) {
                    if (val.toString().toLowerCase().contains(bshFile.fileName)) {
                        result = true;
                        path = val.toString();
                        break;
                    }
                }

                if (!result) {
                    throw new BennoRuntimeException("The BSH file " + bshFile.fileName + " could not found at " + rootPath + ".");
                }

                LOGGER.info("Found BSH file {}/{} at {}.", zoomId, bshFile.fileName, path);
            }
        }
    }

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

    private static boolean checkZoomPath(Path path, Path zoom) {
        if (path.toString().toLowerCase().contains(zoom.toString().toLowerCase())) {
            return isContain(path.toString().toLowerCase(), zoom.toString().toLowerCase());
        }

        return false;
    }

    private static boolean isContain(String source, String subItem){
        String str = "\\b" + subItem + "\\b";
        var pattern = Pattern.compile(str);
        var matcherSource = pattern.matcher(source);

        return matcherSource.find();
    }

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
