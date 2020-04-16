package com.google.torrent.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZippingThread extends Thread {
    private Long torrentThreadId;
    private File root;
    private File zip;
    private File zipFolder;

    @Override
    public void run() {
        log.debug("Concurrent zipping started for " + this.getTorrentThreadId());
        ZipUtil.pack(this.root, this.zip);
        log.debug("Zip finished successfully");
    }
}
