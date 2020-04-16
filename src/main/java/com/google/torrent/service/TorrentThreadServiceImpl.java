package com.google.torrent.service;

import com.google.torrent.dao.TorrentThreadRepository;
import com.google.torrent.entity.TorrentThread;
import com.google.torrent.utils.MyFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

@Slf4j
@Service
public class TorrentThreadServiceImpl implements TorrentThreadService {
    private TorrentThreadRepository torrentThreadRepository;

    @Autowired
    public TorrentThreadServiceImpl(TorrentThreadRepository torrentThreadRepository) {
        this.torrentThreadRepository = torrentThreadRepository;
    }

    public String getTorrentZipPath(Long torrentThreadId) {
        return String.valueOf(torrentThreadRepository.getOne(torrentThreadId).getZippingThread().getZip());
    }

    @Override
    public void saveTorrentThread(TorrentThread torrentThread) {
        torrentThreadRepository.save(torrentThread);
    }

    @Override
    public Map<Long, TorrentThread> getAllTorrentThreadsAsMap() {
        return torrentThreadRepository.findAllAsMap();
    }

    @Override
    public List<TorrentThread> getAllTorrentThreadsAsList() {
        return torrentThreadRepository.findAll();
    }

    @Override
    public Resource loadFileAsResource(Long torrentThreadId) throws IOException {
        log.debug("Loading torrent file as resource: com.google.torrent.service.TorrentThreadServiceImpl.loadFileAsResource");
        try {
            String zipFilePath = getZipAsResource(torrentThreadId);
            log.debug("Creation of resource for downloading");
            if (!Files.exists(Paths.get(zipFilePath))) {
                torrentThreadRepository.getOne(torrentThreadId).getZippingThread().start();
            }
            Resource resource = new UrlResource(Paths.get(zipFilePath).toUri());
            log.debug("Creation is successful");
            log.debug("File name: " + resource.getFilename());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + torrentThreadId);
            }
        } catch (ZipException e) {
            throw new ZipException(e.getMessage());
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + torrentThreadId, ex);
        }
    }

    private String getZipAsResource(Long torrentThreadId) throws IOException {
        if (torrentThreadRepository.getOne(torrentThreadId) == null) {
            throw new FileNotFoundException("Required torrent haven't been downloaded yet or have been already deleted");
        }
        if (torrentThreadRepository.getOne(torrentThreadId).getZippingThread().isAlive()) {
            throw new ZipException("Zip not ready for downloading yet");
        }
        return getTorrentZipPath(torrentThreadId);
    }

    @Override
    public void deleteTorrentThread(Long torrentThreadId) throws IOException {
        torrentThreadRepository.deleteTorrentThread(torrentThreadId);
    }

    @Override
    public TorrentThread getTorrentThreadById(Long torrentThreadId) {
        return torrentThreadRepository.findById(torrentThreadId);
    }

    @Override
    public void restartZipping(Long torrentThreadId) throws IOException {
        deleteTorrentThreadZip(torrentThreadId);

        log.debug("Deletion of zip directory");
        TorrentThread torrentThread = torrentThreadRepository.getOne(torrentThreadId);
        torrentThread.getZippingThread().setDaemon(true);
        log.debug("Starting daemon zipping");
        torrentThread.getZippingThread().start();
    }

    private void deleteTorrentThreadZip(Long torrentThreadId) throws IOException {
        FileUtils.forceDelete(torrentThreadRepository.getOne(torrentThreadId).getZippingThread().getZip());
    }
}
