/*
 * Copyright (c) 2018 Andrei Tomashpolskiy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.torrent.cli;

import bt.metainfo.TorrentFile;
import bt.torrent.fileselector.SelectionResult;
import bt.torrent.fileselector.TorrentFileSelector;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CliFileSelector extends TorrentFileSelector {
    private static final String PROMPT_MESSAGE_FORMAT = "Download '%s'? (hit <Enter> or type 'y' to confirm or type 'n' to skip)";
    private static final String ILLEGAL_KEYPRESS_WARNING = "*** Invalid key pressed. Please, use only <Enter>, 'y' or 'n' ***";

    private AtomicReference<Thread> currentThread;
    private AtomicBoolean shutdown;

    public CliFileSelector() {
        this.currentThread = new AtomicReference<>(null);
        this.shutdown = new AtomicBoolean(false);
    }

    private static String getPromptMessage(TorrentFile file) {
        return String.format(PROMPT_MESSAGE_FORMAT, String.join("/", file.getPathElements()));
    }

    @Override
    protected SelectionResult select(TorrentFile file) {
        while (!shutdown.get()) {
            System.out.println(getPromptMessage(file));
            return SelectionResult.select().build();
        }
        throw new IllegalStateException("Shutdown");
    }

    private String readNextCommand(Scanner scanner) throws IOException {
        currentThread.set(Thread.currentThread());
        try {
            return scanner.nextLine().trim();
        } finally {
            currentThread.set(null);
        }
    }

    public void shutdown() {
        this.shutdown.set(true);
        Thread currentThread = this.currentThread.get();
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }
}
