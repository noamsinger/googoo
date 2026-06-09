package com.game.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class LoggingConfigurator {

    public static void enableDebug() {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger gameLogger = ctx.getLogger("com.game");
        gameLogger.setLevel(Level.DEBUG);
    }
}
