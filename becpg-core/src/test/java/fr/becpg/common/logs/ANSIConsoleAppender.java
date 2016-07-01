/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.common.logs;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
 
/**
 * Colour-coded console appender for Log4J.
 */
public class ANSIConsoleAppender extends ConsoleAppender
{
    private static final int NORMAL = 0;
    private static final int BRIGHT = 1;
    private static final int FOREGROUND_RED = 31;
    private static final int FOREGROUND_GREEN = 32;
    private static final int FOREGROUND_YELLOW = 33;
    private static final int FOREGROUND_BLUE = 34;
    private static final int FOREGROUND_CYAN = 36;
 
    private static final String PREFIX = "\u001b[";
    private static final String SUFFIX = "m";
    private static final char SEPARATOR = ';';
    private static final String END_COLOUR = PREFIX + SUFFIX;   
 
    private static final String FATAL_COLOUR = PREFIX
      + BRIGHT + SEPARATOR + FOREGROUND_RED + SUFFIX;
    private static final String ERROR_COLOUR = PREFIX
      + NORMAL + SEPARATOR + FOREGROUND_RED + SUFFIX;
    private static final String WARN_COLOUR = PREFIX
      + NORMAL + SEPARATOR + FOREGROUND_YELLOW + SUFFIX;
    private static final String INFO_COLOUR = PREFIX
      + NORMAL+ SEPARATOR + FOREGROUND_GREEN + SUFFIX;
    private static final String DEBUG_COLOUR = PREFIX
      + NORMAL + SEPARATOR + FOREGROUND_CYAN + SUFFIX;
    private static final String TRACE_COLOUR = PREFIX
      + NORMAL + SEPARATOR + FOREGROUND_BLUE + SUFFIX;   
 
    /**
     * Wraps the ANSI control characters around the
     * output from the super-class Appender.
     */
    protected void subAppend(LoggingEvent event)
    {
        this.qw.write(getColour(event.getLevel()));
        super.subAppend(event);
        this.qw.write(END_COLOUR);   
 
        if(this.immediateFlush)
        {
            this.qw.flush();
        }
    }   
 
    /**
     * Get the appropriate control characters to change
     * the colour for the specified logging level.
     */
    private String getColour(Level level)
    {
        switch (level.toInt())
        {
            case Priority.FATAL_INT: return FATAL_COLOUR;
            case Priority.ERROR_INT: return ERROR_COLOUR;
            case Priority.WARN_INT: return WARN_COLOUR;
            case Priority.INFO_INT: return INFO_COLOUR;
            case Priority.DEBUG_INT:return DEBUG_COLOUR;
            default: return TRACE_COLOUR;
        }
    }
}
