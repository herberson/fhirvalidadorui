/*
 * Copyright (C) 2023 Tarea Gerenciamento Ltda. (contato@tarea.com.br)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zcage.log;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextAreaHandler extends Handler {

    static private JTextArea jTextArea = null;

    static public void setTextArea(JTextArea jTextArea) {
        TextAreaHandler.jTextArea = jTextArea;
    }

    private Level level = Level.INFO; // The logging level for this handler, which is configurable.

    /*
	 * Must include filtering mechanism as it is not included in the (lame) Abstract Handler class.
	 */
    public TextAreaHandler() {
        Filter filter = new Filter() {
            public boolean isLoggable(LogRecord record) {
                return record.getLevel().intValue() >= level.intValue();
            }
        };
        this.setFilter(filter);
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (jTextArea != null) {
            // Must filter our own logRecords, (lame) Abstract Handler does not do it for us.
            if (!getFilter().isLoggable(logRecord))
                return;

            final String message = new SimpleFormatter().format(logRecord);

            // Append formatted message to textareas using the Swing Thread.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jTextArea.append(message);
                }
            });
        }        
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    /**
     * Must capture level to use in our custom filter, because this is not done in the
     * abstract class.
     */
    @Override
    public void setLevel(Level level) {
        this.level = level;
        super.setLevel(level);
    }
}
