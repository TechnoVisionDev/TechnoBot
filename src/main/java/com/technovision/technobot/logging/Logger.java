package com.technovision.technobot.logging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Logs information to console.
 *
 * @author Sparky
 */
public class Logger {

    private Object obj;

    public Logger(Object object) {
        if (!object.getClass().isAnnotationPresent(Loggable.class))
            System.out.println("Could not register " + object.getClass().getName() + "'s logger because the object is not annotated with Loggable.");
        else obj = object;
    }

    public void log(LogLevel level, String message) {
        String date = new SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
        System.out.println("[" + date + "]" +
                           " [" + obj.getClass().getDeclaredAnnotation(Loggable.class).display() + "] " +
                           "[" + level + "] " + message);
    }

    public enum LogLevel {
        SEVERE, WARNING, INFO
    }

}
