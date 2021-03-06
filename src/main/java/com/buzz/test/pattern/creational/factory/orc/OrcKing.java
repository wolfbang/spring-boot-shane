package com.buzz.test.pattern.creational.factory.orc;


import com.buzz.test.pattern.creational.factory.King;

public class OrcKing implements King {

    private static final String DESCRIPTION = "this is the Orc King!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
