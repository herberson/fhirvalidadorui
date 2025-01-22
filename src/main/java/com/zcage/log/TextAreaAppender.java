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

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Simple example of creating a Log4j appender that will
 * write to a JTextArea.
 */
public class TextAreaAppender extends WriterAppender {

    static private JTextArea jTextArea = null;

    /** Set the target JTextArea for the logging information to appear. */
    static public void setTextArea(JTextArea jTextArea) {
        TextAreaAppender.jTextArea = jTextArea;
    }

    @Override
    /**
	 * Format and then append the loggingEvent to the stored
	 * JTextArea.
	 */
    public void append(LoggingEvent loggingEvent) {
        if (jTextArea != null) {
            final String message = this.layout.format(loggingEvent);

            // Append formatted message to textarea using the Swing Thread.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jTextArea.append(message);
                }
            });
        }
    }
}
