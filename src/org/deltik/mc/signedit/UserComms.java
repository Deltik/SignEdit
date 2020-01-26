package org.deltik.mc.signedit;

import org.parboiled.common.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

public class UserComms {
    /**
     * Publish Comms ResourceBundles into the file system
     *
     * @param targetDirectory The absolute path to the directory where the ResourceBundles should be deployed
     */
    public void deploy(String targetDirectory) throws IOException {
        File overridesDir = Paths.get(targetDirectory, "locales", "overrides").toFile();
        File originalsDir = Paths.get(targetDirectory, "locales", "originals").toFile();

        overridesDir.mkdirs();
        originalsDir.mkdirs();
        removeOldOriginals(originalsDir);

        for (String resourceName : getResourceNamesFromSelf()) {
            InputStream readStream = getClass().getResourceAsStream(resourceName);
            File originalCopy = Paths.get(originalsDir.getAbsolutePath(), resourceName).toFile();
            originalCopy.createNewFile();
            OutputStream writeStream = new FileOutputStream(originalCopy);
            FileUtils.copyAll(readStream, writeStream);
        }

        InputStream documentationStream = getClass().getResourceAsStream(File.separator + "README.Comms.txt");
        File documentationFile = Paths.get(targetDirectory, "locales", "README.txt").toFile();
        documentationFile.createNewFile();
        OutputStream writeStream = new FileOutputStream(documentationFile);
        FileUtils.copyAll(documentationStream, writeStream);
    }

    /**
     * Obtain a list of resources that provide Comms ResourceBundles
     */
    protected Collection<String> getResourceNamesFromSelf() {
        String separator = File.separator;

        Collection<String> absolutePathList = ResourceList.getResources(
                Pattern.compile(".*" + separator + "Comms.*\\.properties")
        );
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
        Collection<File> existingFiles = new HashSet<>(Arrays.asList(Objects.requireNonNull(originalsDir.listFiles())));
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
