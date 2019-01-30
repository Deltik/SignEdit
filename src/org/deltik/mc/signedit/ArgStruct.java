package org.deltik.mc.signedit;

import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ArgStruct {
    String subcommand;
    int lineRelative;
    List<String> remainder;

    public ArgStruct(String[] args) {
        parseArgs(args);
    }

    public String getSubcommand() {
        return subcommand;
    }

    public int getLineRelative() {
        return lineRelative;
    }

    public List<String> getRemainder() {
        return remainder;
    }

    public ArgStruct parseArgs(String[] args) {
        List<String> argsArray = new LinkedList<>(Arrays.asList(args));
        try {
            subcommand = argsArray.remove(0).toLowerCase();
            if (StringUtils.isNumeric(subcommand)) {
                lineRelative = Integer.valueOf(subcommand);
                subcommand = "set";
            } else if (subcommand.equals("set") || subcommand.equals("clear")) {
                lineRelative = Integer.valueOf(argsArray.remove(0));
            }
        } catch (IndexOutOfBoundsException e) {
            subcommand = "help";
            lineRelative = -1;
        } catch (NumberFormatException e) {
            subcommand = "set";
            lineRelative = -1;
        }
        remainder = argsArray;
        return this;
    }
}
