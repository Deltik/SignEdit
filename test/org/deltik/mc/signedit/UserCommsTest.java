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
        UserComms userComms = new UserComms(pluginConfigFolder.getRoot().getAbsolutePath());

        userComms.deploy();

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
        UserComms userComms = new UserComms(pluginConfigFolder.getRoot().getAbsolutePath());

        userComms.deploy();

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
        UserComms userComms = new UserComms(pluginConfigFolder.getRoot().getAbsolutePath());
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

        userComms.deploy();

        Assert.assertFalse(garbageFile.exists());
        Assert.assertFalse(garbageFile2.exists());
    }
}
