package com.game.core;

import ch.qos.logback.core.PropertyDefinerBase;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LogDirPropertyDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        Path logDir = Paths.get(System.getProperty("user.home"), ".config", "googoo", "logs");
        return logDir.toAbsolutePath().toString();
    }
}
