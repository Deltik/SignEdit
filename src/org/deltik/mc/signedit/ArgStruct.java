package org.deltik.mc.signedit;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ArgStruct {
    public String subcommand;
    public int lineRelative;
    public List<String> remainder;

    public ArgStruct(String[] args) {
        parseArgs(args);
    }

    public ArgStruct parseArgs(String[] args) {
        List<String> argsArray = new LinkedList<>(Arrays.asList(args));
        try {
            subcommand = argsArray.remove(0).toLowerCase();
            if (StringUtils.isNumeric(subcommand)) {
                lineRelative = Integer.valueOf(subcommand);
                subcommand = "set";
            } else {
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
