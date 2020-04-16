package com.google.torrent.entity;


import bt.torrent.TorrentSessionState;
import com.google.torrent.HelloAppEngineApplication;
import com.google.torrent.cli.CliClient;
import com.google.torrent.service.ZippingThread;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.google.torrent.cli.CliClient.configureSecurity;
import static com.google.torrent.cli.CliClient.registerLog4jShutdownHook;
import static com.google.torrent.utils.GeneralUtils.isAlreadyExists;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TorrentThread extends Thread {
    //todo for every user store credentials for google drive in database

    private Long torrentThreadId;

    private String torrentName;

    private String magnetLink;

    private double status;

    private CliClient client;

    private ZippingThread zippingThread;

    public TorrentThread(Long torrentThreadId, String torrentName, String magnetLink, double status, CliClient client, ZippingThread zippingThread) {
        this.torrentThreadId = torrentThreadId;
        this.torrentName = torrentName;
        this.magnetLink = magnetLink;
        this.status = status;
        this.client = client;
        this.zippingThread = zippingThread;
        setZipPaths();
    }

    @Override
    public void run() {
        log.debug("initialized options for torrent");
        configureSecurity();
        log.debug("configured security");
        registerLog4jShutdownHook();
        log.debug("registered log4j shutDownHook");
        log.debug("Torrent client started to download");
        client.start();
        this.zippingThread.setDaemon(true);
        while (true) {
            if (this.getStatus() == 100) {
                log.info("Zipping started after successful downloading");
                this.zippingThread.start();
                return;
            }
        }
    }


    private void setZipPaths() {
        try {
            String torrentNameWithUnderscore = this.getTorrentName().replaceAll(" ", "_");
            String torrentIdFolderName = this.getTorrentThreadId() + "_" + this.getTorrentName().replaceAll(" ", "_");

            boolean exists = isAlreadyExists(this.torrentThreadId);
            if (exists) {
                torrentIdFolderName = getStoredFolderName(this.torrentThreadId);
            }

            Path filePath;
            String zip_folder;

            if (HelloAppEngineApplication.developEnvironment) {
                filePath = Paths.get("D:/PrFiles/IdeaProjects/uploads/" + torrentIdFolderName);
                zip_folder = "D:/PrFiles/IdeaProjects/zips/" + torrentIdFolderName + "/";
            } else {
                filePath = Paths.get("/home/" + System.getProperty("user.name") + "/downloads/" + torrentIdFolderName);
                zip_folder = "/home/" + System.getProperty("user.name") + "/zips/" + torrentIdFolderName + "/";
            }

            Path zipFolderPath = Paths.get(zip_folder);
            String zipFilePath = zip_folder + torrentNameWithUnderscore + ".zip";

            if (!Files.exists(zipFolderPath)) {
                log.debug("Creation of folder for zip with full path: " + zip_folder);
                Files.createDirectory(zipFolderPath);
                log.debug("Creation of zip file with hash: " + torrentThreadId);
            }
            this.zippingThread.setRoot(new File(filePath.toString()));
            this.zippingThread.setZipFolder(new File(zip_folder));
            this.zippingThread.setZip(new File(zipFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStoredFolderName(Long torrentHashCode) throws NoSuchFileException {
        // Might be caused for file with torrent name as hash code

        File folderName;

        if (HelloAppEngineApplication.developEnvironment) {
            folderName = new File("D:/PrFiles/IdeaProjects/uploads/");
        } else {
            folderName = new File("/home/" + System.getProperty("user.name") + "/downloads/");
        }

        for (File file : Objects.requireNonNull(folderName.listFiles())) {
            if (file.getName().contains(String.valueOf(torrentHashCode))) {
                return file.getName();
            }
        }
        throw new NoSuchFileException("It was requested already existed file, but we haven't find it in the folder. Something went wrong withdownloaded file while server has been processing it");
    }

    public double getStatus() {
        CliClient client = this.getClient();
        if (status == 0 && (client == null || client.getPrinter() == null || client.getPrinter().getSessionStateForStatus() == null)) {
            return 0;
        } else if (status == 100) {
            return 100;
        }
        TorrentSessionState sessionStateForStatus = client.getPrinter().getSessionStateForStatus();
        double percents = ((double) sessionStateForStatus.getPiecesComplete() / (double) sessionStateForStatus.getPiecesTotal()) * 100;
        return new BigDecimal(percents).setScale(2, RoundingMode.UP).doubleValue();
    }
}




