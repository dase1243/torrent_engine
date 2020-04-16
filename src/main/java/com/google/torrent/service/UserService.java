package com.google.torrent.service;

import com.google.torrent.entity.TorrentThread;
import com.google.torrent.entity.User;

import java.util.List;

public interface UserService {
    void save(User user);

    User findByUsername(String username);

    List<User> findAll();

    void deleteUser(Long valueOf);

    List<TorrentThread> getTorrentListForUserByName(String name);

    void updateUser(User user);
}
