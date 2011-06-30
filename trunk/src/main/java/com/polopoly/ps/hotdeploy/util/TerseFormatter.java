package com.polopoly.ps.hotdeploy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TerseFormatter extends Formatter {

    @Override
    public String format(LogRecord log) {
        StringBuffer result = new StringBuffer(100);

        result.append(log.getMessage());

        result.append("\n");

        if (log.getThrown() != null) {
            StringBuffer stackTrace = new StringBuffer(1000);
            StringWriter stringWriter = new StringWriter();
            log.getThrown().printStackTrace(new PrintWriter(stringWriter));

            int i = 0;
            int atLine = 0;

            do {
                i = stackTrace.indexOf("\n", i+1);
            } while(atLine < 5 && i != -1);

            result.append(stackTrace.substring(0, i+1));
        }

        return result.toString();
    }

}
