package com.google.torrent.controller;

import com.google.common.collect.Lists;
import com.google.torrent.HelloAppEngineApplication;
import com.google.torrent.cli.CliClient;
import com.google.torrent.cli.Options;
import com.google.torrent.entity.TorrentThread;
import com.google.torrent.entity.User;
import com.google.torrent.service.TorrentThreadService;
import com.google.torrent.service.UserService;
import com.google.torrent.service.ZippingThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static com.google.torrent.utils.GeneralUtils.isAlreadyExists;

@Slf4j
@Controller
public class ServerController {

    @Autowired
    private TorrentThreadService torrentThreadService;

    @Autowired
    private UserService userService;

    public static ResponseEntity<Resource> createDownloadFromResource(HttpServletRequest request, Resource resource) {
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.debug("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/auth/listAllTorrentThreads")
    public String listAllTorrentThreads(Model model, Principal principal) {
        model.addAttribute("torrents", torrentThreadService.getAllTorrentThreadsAsMap().values());
        List<TorrentThread> torrents = userService.getTorrentListForUserByName(principal.getName());
        model.addAttribute("user_torrents", Lists.newArrayList(torrents));
        return "listAllTorrentThreads";
    }

    @GetMapping("/fileUploadFormHTML")
    public String fileUploadFormHTML() {
        //todo: make uploading through html for torrent files
        //todo: how to convert torrent files into magnet links
        return "fileUploadForm";
    }

    @GetMapping("/auth/downloadTorrent/inputMagnetLink")
    public String inputMagnetLink() {
        return "inputMagnetLink";
    }

    @GetMapping("/auth/downloadTorrent/processInputLinkConcurrently")
    public String uploadMagnetLinkForBtConcurrently(@RequestParam("magnetLink") String magnetLink, @RequestParam("torrentName") String torrentName, Principal principal) throws IOException {
        log.debug("started request");

        Long torrentHashCode = (long) magnetLink.hashCode();

        String correctTorrentName = torrentName.replaceAll("[\\t.:,'\"\\\\\\s]", "_");
        String torrentIdFolderName = torrentHashCode + "_" + correctTorrentName;

        Path torrentFolder;

        if (HelloAppEngineApplication.developEnvironment) {
            torrentFolder = Paths.get("D:/PrFiles/IdeaProjects/uploads/" + torrentIdFolderName);
        } else {
            torrentFolder = Paths.get("/home/" + System.getProperty("user.name") + "/downloads/" + torrentIdFolderName);
        }

        boolean exists = isAlreadyExists(torrentHashCode);

        if (!exists) {
            Files.createDirectory(torrentFolder);
        } else {
            //Might be caused by invoking for empty folder
            TorrentThread uploadMagnetFile = torrentThreadService.getTorrentThreadById(torrentHashCode);
            if (uploadMagnetFile == null) {
                torrentThreadService.saveTorrentThread(new TorrentThread(
                        torrentHashCode,
                        correctTorrentName,
                        magnetLink,
                        100,
                        null,
                        new ZippingThread()
                ));
            }
            updateUserTorrentList(principal, torrentHashCode);
            ZippingThread alreadyExistTorrentZippingThread = torrentThreadService.getTorrentThreadById(torrentHashCode).getZippingThread();

            if (!alreadyExistTorrentZippingThread.getZip().exists()) {
                alreadyExistTorrentZippingThread.setDaemon(true);
                alreadyExistTorrentZippingThread.start();
            }

            return "fileAlreadyExists";
        }
        Options options = new Options(null,
                magnetLink,
                new File(String.valueOf(torrentFolder)),
                false,
                false,
                false,
                true,
                false,
                null,
                null,
                null,
                false);

        TorrentThread torrentThread = new TorrentThread(
                torrentHashCode,
                correctTorrentName,
                magnetLink,
                0,
                new CliClient(options),
                new ZippingThread()
        );
        torrentThread.setDaemon(true);
        torrentThread.start();
        torrentThreadService.saveTorrentThread(torrentThread);
        log.info(String.format("Add torrent with {%s} into pool of torrents and to current user with username {%s}", torrentHashCode, principal.getName()));
        updateUserTorrentList(principal, torrentHashCode);
        log.info("Torrent client started to download");
        return "successInDownloadStarting";
    }

    private void updateUserTorrentList(Principal principal, Long torrentHashCode) {
        User user = userService.findByUsername(principal.getName());
        user.getTorrents().add(torrentHashCode);
        userService.updateUser(user);
    }

    @GetMapping(value = "/auth/downloadTorrentFromServer/{encodedName:.+}")
    public void getFile(@PathVariable String encodedName, HttpServletResponse response, HttpServletRequest request) throws IOException, InterruptedException {
        Resource resource = torrentThreadService.loadFileAsResource(Long.valueOf(encodedName));

        response.setContentType(request.getServletContext().getMimeType(resource.getFile().getAbsolutePath()));

        response.setContentLengthLong(resource.getFile().length());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

        BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(resource.getFile()));
        BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());

        int bytesRead = 0;
        byte[] bytes = new byte[1024 * 8];

        while ((bytesRead = inStream.read(bytes)) != -1) {
            outStream.write(bytes, 0, bytesRead);
        }

        outStream.flush();
        inStream.close();
    }

    @PostMapping("/auth/deleteTorrents")
    public String deleteTorrentFilesFromServer(@RequestParam("idChecked") List<String> torrentThreadIds, Model model) {
        if (torrentThreadIds != null) {
            List<String> errorIdList = new ArrayList<>();
            for (String torrentId : torrentThreadIds) {
                try {
                    torrentThreadService.deleteTorrentThread(Long.valueOf(torrentId));
                } catch (IOException e) {
                    errorIdList.add(torrentId);
                }
            }
            model.addAttribute("errors", errorIdList);
        }
        return "listAllTorrentThreads";
    }

    @PostMapping("/auth/restartTorrent")
    public String restartTorrent(@RequestParam("restartId") String torrentId, Principal principal) throws IOException {
        Long torrentThreadId = Long.valueOf(torrentId);
        TorrentThread torrentThread = torrentThreadService.getTorrentThreadById(torrentThreadId);
        uploadMagnetLinkForBtConcurrently(torrentThread.getMagnetLink(), torrentThread.getTorrentName(), principal);
        torrentThreadService.deleteTorrentThread(Long.valueOf(torrentId));
        return "listAllTorrentThreads";
    }

    @PostMapping("/auth/restartZipping")
    public String restartZipping(@RequestParam("restartId") String torrentId) throws IOException {
        Long torrentThreadId = Long.valueOf(torrentId);
        torrentThreadService.restartZipping(torrentThreadId);
        return "listAllTorrentThreads";
    }
}
