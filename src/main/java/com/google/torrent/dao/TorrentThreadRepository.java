package com.google.torrent.dao;

import bt.runtime.BtClient;
import com.google.torrent.cli.CliClient;
import com.google.torrent.entity.TorrentThread;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TorrentThreadRepository {

    private Map<Long, TorrentThread> torrentThreads = new HashMap<>();

    public void deleteTorrentThread(Long encodedName) throws IOException {
        TorrentThread deleteFile = this.findById(encodedName);
        if (deleteFile == null) {
            throw new FileNotFoundException();
        }

        if (deleteFile.getClient() != null) {
            CliClient cliClient = deleteFile.getClient();
            BtClient btClient = cliClient.getClient();
            if (btClient != null) {
                btClient.stop();
            }
        }

        try {
            FileUtils.deleteDirectory(deleteFile.getZippingThread().getRoot());
            FileUtils.deleteDirectory(deleteFile.getZippingThread().getZipFolder());
        } catch (IOException e) {
            System.out.println("Either zip or folder doesn't exist");
        }
        this.deleteById(encodedName);
    }

    public List<TorrentThread> findAll() {
        return new ArrayList<>(torrentThreads.values());
    }

    public Map<Long, TorrentThread> findAllAsMap() {
        return torrentThreads;
    }

    public void deleteById(Long torrentThreadId) {
        torrentThreads.remove(torrentThreadId);
    }

    public void deleteAll(Iterable<? extends TorrentThread> entities) {
        entities.forEach(o ->
                torrentThreads.remove(o.getTorrentThreadId()));
    }

    public void save(TorrentThread entity) {
        torrentThreads.put(entity.getTorrentThreadId(), entity);
    }

    public void saveAll(Iterable<TorrentThread> entities) {
        entities.forEach(torrentThread -> torrentThreads.put(torrentThread.getTorrentThreadId(), torrentThread));
    }

    public TorrentThread findById(Long torrentThreadId) {
        return torrentThreads.get(torrentThreadId);
    }

    public boolean existsById(Long torrentThreadId) {
        return torrentThreads.get(torrentThreadId) != null;
    }

    public TorrentThread getOne(Long torrentThreadId) {
        return torrentThreads.get(torrentThreadId);
    }
}
