/*
 * Copyright 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package threader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A ThreadPrintStream replaces the normal System.out and ensures that output to
 * System.out goes to a different PrintStream for each thread. It does this by
 * using ThreadLocal to maintain a PrintStream for each thread.
 */
public class ThreadPrintStream extends PrintStream {

    /**
     * Changes System.out to a ThreadPrintStream which will send output to a
     * separate file for each thread.
     */
    public static void replaceSystemOut() {

        // Save the existing System.out
        PrintStream console = System.out;

        // Create a ThreadPrintStream and install it as System.out
        ThreadPrintStream threadOut = new ThreadPrintStream();
        System.setOut(threadOut);

        // Use the original System.out as the current thread's System.out
        threadOut.setThreadOut(console);
    }

    /**
     * Thread specific storage to hold a PrintStream for each thread
     */
    private ThreadLocal<PrintStream> out;

    private ThreadPrintStream() {
        super(new ByteArrayOutputStream(0));
        out = new ThreadLocal<PrintStream>();
    }

    /**
     * Sets the PrintStream for the currently executing thread.
     */
    public void setThreadOut(PrintStream out) {
        this.out.set(out);
    }

    /**
     * Returns the PrintStream for the currently executing thread.
     */
    public PrintStream getThreadOut() {
        return this.out.get();
    }

    @Override
    public boolean checkError() {
        return getThreadOut().checkError();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        getThreadOut().write(buf, off, len);
    }

    @Override
    public void write(int b) {
        getThreadOut().write(b);
    }

    @Override
    public void flush() {
        getThreadOut().flush();
    }

    @Override
    public void close() {
        getThreadOut().close();
    }
}
