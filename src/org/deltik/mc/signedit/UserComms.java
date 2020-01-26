/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
 *
 * This file is part of SignEdit for Bukkit.
 *
 * SignEdit for Bukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SignEdit for Bukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SignEdit for Bukkit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.deltik.mc.signedit;

import org.apache.commons.lang.exception.ExceptionUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class UserComms {
    private String targetDirectory;
    private File overridesDir;
    private File originalsDir;
    private String originalsSource;

    @Inject
    public UserComms(SignEditPlugin plugin) {
        this(plugin.getDataFolder());
        this.originalsSource = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    /**
     * @param dataFolder The Bukkit plugin data folder
     */
    public UserComms(File dataFolder) {
        this(dataFolder.getAbsolutePath());
    }

    /**
     * @param targetDirectory The absolute path to the Bukkit plugin data folder
     */
    public UserComms(String targetDirectory) {
        this.targetDirectory = targetDirectory;
        this.overridesDir = Paths.get(targetDirectory, "locales", "overrides").toFile();
        this.originalsDir = Paths.get(targetDirectory, "locales", "originals").toFile();
    }

    public ClassLoader getClassLoader() {
        try {
            List<URL> urlList = new ArrayList<>();
            urlList.add(overridesDir.toURI().toURL());
            urlList.add(originalsDir.toURI().toURL());
            if (originalsSource != null) {
                urlList.add(new File(originalsSource).toURI().toURL());
            }
            return new URLClassLoader(urlList.toArray(new URL[0]));
        } catch (MalformedURLException e) {
            getLogger().warning(
                    "Could not load user-defined locales; falling back to built-in locales. Details: "
            );
            getLogger().warning(ExceptionUtils.getStackTrace(e));
            return getClass().getClassLoader();
        }
    }

    /**
     * Publish Comms ResourceBundles into the file system
     */
    public void deploy() throws IOException {
        overridesDir.mkdirs();
        originalsDir.mkdirs();
        removeOldOriginals(originalsDir);

        for (String resourceName : getResourceNamesFromSelf()) {
            InputStream readStream = getClass().getResourceAsStream(resourceName);
            Path originalCopyPath = Paths.get(originalsDir.getAbsolutePath(), resourceName);
            Files.copy(readStream, originalCopyPath, StandardCopyOption.REPLACE_EXISTING);
        }

        InputStream documentationStream = getClass().getResourceAsStream(File.separator + "README.Comms.txt");
        Path documentationFilePath = Paths.get(targetDirectory, "locales", "README.txt");
        Files.copy(documentationStream, documentationFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Obtain a list of resources that provide Comms ResourceBundles
     */
    protected Collection<String> getResourceNamesFromSelf() {
        String separator = File.separator;

        Collection<String> absolutePathList;
        Pattern commsPattern = Pattern.compile("(^|.*" + separator + ")Comms.*\\.properties");
        if (originalsSource != null) {
            absolutePathList = ResourceList.getResources(originalsSource, commsPattern);
        } else {
            absolutePathList = ResourceList.getResources(commsPattern);
        }
        Collection<String> resourcePathList = new ArrayList<>();

        for (String item : absolutePathList) {
            File itemAsFile = new File(item);
            resourcePathList.add(separator + itemAsFile.getName());
        }

        return resourcePathList;
    }

    /**
     * Remove unexpected files from originals folder
     */
    protected void removeOldOriginals(File originalsDir) throws IOException {
        Collection<File> existingFiles = new HashSet<>(
                Arrays.asList(
                        Optional.ofNullable(originalsDir.listFiles()).orElse(new File[0])
                )
        );
        Collection<File> expectedFiles = new HashSet<>();

        for (String resourceName : getResourceNamesFromSelf()) {
            File expectedFile = Paths.get(originalsDir.getAbsolutePath(), resourceName).toFile();
            expectedFiles.add(expectedFile);
        }

        existingFiles.removeAll(expectedFiles);

        for (File fileToRemove : existingFiles) {
            if (fileToRemove.isDirectory()) {
                Files.walkFileTree(fileToRemove.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
                            throws IOException {
                        Files.delete(path);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path path, IOException e)
                            throws IOException {
                        Files.delete(path);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(fileToRemove.toPath());
            }
        }
    }
}
