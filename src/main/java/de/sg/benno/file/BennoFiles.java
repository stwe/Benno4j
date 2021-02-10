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
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.sg.ogl.Log.LOGGER;

public class BennoFiles {

    public enum ZoomableBshFile {
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

        public final String fileName;

        ZoomableBshFile(String fileName) {
            this.fileName = fileName;
        }
    }

    public enum InterfaceBshFile {
        BAUHAUS("bauhaus.bsh"),
        START("start.bsh"),
        EDITOR("editor.bsh");

        public final String fileName;

        InterfaceBshFile(String fileName) {
            this.fileName = fileName;
        }
    }

    public enum OtherBshFile {
        PALETTE("stadtfld.col");

        public final String fileName;

        OtherBshFile(String fileName) {
            this.fileName = fileName;
        }
    }

    private final Path rootPath;

    private final HashMap<Zoom.ZoomId, List<Path>> zoomableBshFilePaths = new HashMap<>();
    private final HashMap<InterfaceBshFile, Path> interfaceBshFilePaths = new HashMap<>();
    private final HashMap<OtherBshFile, Path> otherBshFilePaths = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BennoFiles(String path) throws IOException {
        LOGGER.debug("Creates BennoFiles object.");

        rootPath = Paths.get(path);

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public HashMap<Zoom.ZoomId, List<Path>> getZoomableBshFilePaths() {
        return zoomableBshFilePaths;
    }

    public Optional<Path> getZoomableBshFilePath(Zoom.ZoomId zoomId, ZoomableBshFile bshFile) {
        for (var val : zoomableBshFilePaths.get(zoomId)) {
            if (val.toString().toLowerCase().contains(bshFile.fileName)) {
                return Optional.of(val);
            }
        }

        return Optional.empty();
    }

    public HashMap<InterfaceBshFile, Path> getInterfaceBshFilePaths() {
        return interfaceBshFilePaths;
    }

    public Path getInterfaceBshFilePath(InterfaceBshFile bshFile) {
        return interfaceBshFilePaths.get(bshFile);
    }

    public HashMap<OtherBshFile, Path> getOtherBshFilePaths() {
        return otherBshFilePaths;
    }

    public Path getOtherBshFilePath(OtherBshFile bshFile) {
        return otherBshFilePaths.get(bshFile);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    private void init() throws IOException {
        LOGGER.debug("Starts initializing filesystem from path {}...", rootPath);

        // Zoom graphics in GFX, MGFX, SGFX
        findZoomableBshFiles(Zoom.ZoomId.SGFX);
        findZoomableBshFiles(Zoom.ZoomId.MGFX);
        findZoomableBshFiles(Zoom.ZoomId.GFX);
        checkForZoomableBshFiles();

        // Interface graphics and other files (palette file)
        findInterfaceBshFiles();
        findOtherBshFiles();

        LOGGER.debug("Successfully initialized filesystem.");
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

    private void findInterfaceBshFiles() throws IOException {
        for (var bshFile : InterfaceBshFile.values()) {
            var paths = findInterfaceBshFile(bshFile);

            if (paths.isEmpty()) {
                throw new BennoRuntimeException("Interface BSH file " + bshFile.fileName + " found at " + rootPath + ".");
            }

            LOGGER.debug("Found " + bshFile.fileName + " at " + paths.get(0) + ".");

            interfaceBshFilePaths.put(bshFile, paths.get(0));
        }
    }

    private void findOtherBshFiles() throws IOException {
        for (var bshFile : OtherBshFile.values()) {
            var paths = findOtherBshFile(bshFile);

            if (paths.isEmpty()) {
                throw new BennoRuntimeException("Other BSH file " + bshFile.fileName + " found at " + rootPath + ".");
            }

            LOGGER.debug("Found " + bshFile.fileName + " at " + paths.get(0) + ".");

            otherBshFilePaths.put(bshFile, paths.get(0));
        }
    }

    //-------------------------------------------------
    // Check
    //-------------------------------------------------

    private void checkForZoomableBshFiles() {
        for (var zoomId : Zoom.ZoomId.values()) {
            for (var bshFile : ZoomableBshFile.values()) {
                var result = false;
                for (var val : zoomableBshFilePaths.get(zoomId)) {
                    if (val.toString().toLowerCase().contains(bshFile.fileName)) {
                        result = true;
                        break;
                    }
                }

                LOGGER.info("Does the file {}/{} exist? [{}]", zoomId, bshFile.fileName, result ? "yes" : "no");
            }
        }
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

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

    private List<Path> findInterfaceBshFile(InterfaceBshFile bshFile) throws IOException {
        List<Path> result;

        try (var walk = Files.walk(rootPath, 2)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().contains(bshFile.fileName))
                    .filter(p -> p.getParent().toString().toLowerCase().contains("toolgfx"))
                    .collect(Collectors.toList());
        }

        return result;
    }

    private List<Path> findOtherBshFile(OtherBshFile bshFile) throws IOException {
        List<Path> result;

        try (var walk = Files.walk(rootPath)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().contains(bshFile.fileName))
                    .filter(p -> p.getParent().toString().toLowerCase().contains("toolgfx"))
                    .collect(Collectors.toList());
        }

        return result;
    }
}
