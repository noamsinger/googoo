package com.game.core;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom logging configurator that runs before JavaFX initialization.
 * This class is invoked by the JVM via the java.util.logging.config.class property.
 */
public class LoggingConfigurator {

    public LoggingConfigurator() {
        // Suppress harmless macOS JavaFX activation timeout warning
        Logger glassLogger = Logger.getLogger("com.sun.glass.ui.mac.MacApplication");
        glassLogger.setLevel(Level.SEVERE);

        // Also suppress parent loggers
        Logger.getLogger("com.sun.glass.ui.mac").setLevel(Level.SEVERE);
        Logger.getLogger("com.sun.glass.ui").setLevel(Level.WARNING);
        Logger.getLogger("com.sun.glass").setLevel(Level.WARNING);

        // Configure console handler
        Logger rootLogger = Logger.getLogger("");
        ConsoleHandler consoleHandler = null;

        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = (ConsoleHandler) handler;
                break;
            }
        }

        if (consoleHandler == null) {
            consoleHandler = new ConsoleHandler();
            rootLogger.addHandler(consoleHandler);
        }

        consoleHandler.setLevel(Level.ALL);
    }
}
