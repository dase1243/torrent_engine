package com.google.torrent.service;


import com.google.torrent.entity.TorrentThread;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TorrentThreadService {
    void saveTorrentThread(TorrentThread torrentThread);

    Map<Long, TorrentThread> getAllTorrentThreadsAsMap();

    List<TorrentThread> getAllTorrentThreadsAsList();

    Resource loadFileAsResource(Long torrentThreadId) throws IOException, InterruptedException;

    void deleteTorrentThread(Long torrentThreadId) throws IOException;

    TorrentThread getTorrentThreadById(Long torrentThreadId);

    void restartZipping(Long torrentThreadId) throws IOException;
}
