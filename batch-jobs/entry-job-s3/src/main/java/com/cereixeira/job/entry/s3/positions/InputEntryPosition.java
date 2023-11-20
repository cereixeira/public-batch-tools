package com.cereixeira.job.entry.s3.positions;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

public enum InputEntryPosition {

    FILENAME(0, "fileName"),
    TIPOLOGIA(1, "typology"),
    ID(2, "id"),
    TIPO(3, "type")
    ;

    public final int index;
    public final String name;

    InputEntryPosition(int index, String name) {
        this.index = index;
        this.name = name;
    }

    private int getIndex(){
        return this.index;
    }
    private String getName(){
        return this.name;
    }

    public static Integer[] getIndexArray() {
        Function<InputEntryPosition, Integer> getIndex = InputEntryPosition::getIndex;
        IntFunction<Integer[]> integerArray = Integer[]::new;

        return Arrays.stream(InputEntryPosition.values())
                .map(getIndex)
                .toArray(integerArray);
    }

    public static String[] getNameArray() {
        Function<InputEntryPosition, String> getName = InputEntryPosition::getName;
        IntFunction<String[]> stringArray = String[]::new;

        return Arrays.stream(InputEntryPosition.values())
                .map(getName)
                .toArray(stringArray);
    }
}
