package org.deltik.mc.signedit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UserCommsTest {
    @Rule
    public TemporaryFolder pluginConfigFolder = new TemporaryFolder();

    @Test
    public void deployDirectoryStructure() throws IOException {
        UserComms userComms = new UserComms();

        userComms.deploy(pluginConfigFolder.getRoot().getAbsolutePath());

        Assert.assertTrue(Paths.get(
                pluginConfigFolder.getRoot().getAbsolutePath(), "locales", "originals"
        ).toFile().isDirectory());
        Assert.assertTrue(Paths.get(
                pluginConfigFolder.getRoot().getAbsolutePath(), "locales", "overrides"
        ).toFile().isDirectory());
        Assert.assertTrue(Paths.get(
                pluginConfigFolder.getRoot().getAbsolutePath(), "locales", "README.txt"
        ).toFile().length() > 0);
    }

    @Test
    public void deployCopiesOriginals() throws IOException {
        UserComms userComms = new UserComms();

        userComms.deploy(pluginConfigFolder.getRoot().getAbsolutePath());

        Pattern pattern;
        pattern = Pattern.compile(".*/Comms[_.].*");
        final Collection<String> list = ResourceList.getResources(pattern);
        for (final String name : list) {
            String fileName = Paths.get(name).getFileName().toString();
            File file = Paths.get(
                    pluginConfigFolder.getRoot().getAbsolutePath(), "locales", "originals", fileName
            ).toFile();
            Assert.assertTrue(file.isFile());

            Scanner scanner = new Scanner(file);
            Pattern contentCheckRegex = Pattern.compile("^# .* locale file$");
            boolean contentCheckPassed = false;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (contentCheckRegex.matcher(line).matches()) {
                    contentCheckPassed = true;
                    break;
                }
            }
            if (!contentCheckPassed) {
                Assert.fail(file.getAbsolutePath() + " does not contain regex match: " + contentCheckRegex.toString());
            }
        }
    }

    @Test
    public void deployRemovesUnexpectedFilesFromOriginalsCopy() throws IOException {
        UserComms userComms = new UserComms();
        File originalsDir = Paths.get(
                pluginConfigFolder.getRoot().getAbsolutePath(), "locales", "originals"
        ).toFile();
        originalsDir.mkdirs();
        File garbageDir = Paths.get(originalsDir.getAbsolutePath(), "FOLDER-NONSENSE", "NEST-ME").toFile();
        garbageDir.mkdirs();
        File garbageFile = Paths.get(garbageDir.getAbsolutePath(), "REMOVE-ME").toFile();
        garbageFile.createNewFile();
        File garbageFile2 = Paths.get(originalsDir.getAbsolutePath(), "REMOVE-ME").toFile();
        garbageFile2.createNewFile();

        Assert.assertTrue(garbageFile.exists());
        Assert.assertTrue(garbageFile2.exists());

        userComms.deploy(pluginConfigFolder.getRoot().getAbsolutePath());

        Assert.assertFalse(garbageFile.exists());
        Assert.assertFalse(garbageFile2.exists());
    }
}
