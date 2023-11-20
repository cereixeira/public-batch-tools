package com.cereixeira.databases.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {
    public static List<File> getFilesBySuffix(String sInputPath, String suffix){
        List<File> lFiles = new ArrayList<>();
        Path inputPath = Paths.get(sInputPath);

        FilenameFilter csvFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(suffix)) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File[] aFiles = inputPath.toFile().listFiles(csvFilter);
        if(aFiles != null && aFiles.length>0){
            lFiles = Arrays.asList(aFiles);
        }
        return lFiles;
    }
}
