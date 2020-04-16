package com.google.torrent.service;

import com.google.torrent.dao.RoleRepository;
import com.google.torrent.dao.TorrentThreadRepository;
import com.google.torrent.dao.UserRepository;
import com.google.torrent.entity.TorrentThread;
import com.google.torrent.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TorrentThreadRepository torrentThreadRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long valueOf) {
        userRepository.deleteById(valueOf);
    }

    @Override
    public List<TorrentThread> getTorrentListForUserByName(String name) {
        List<TorrentThread> torrentThreadList = new ArrayList<>();
        for (Long torrentId : this.findByUsername(name).getTorrents()) {
            torrentThreadList.add(torrentThreadRepository.getOne(torrentId));
        }
        return torrentThreadList;
    }

    @Override
    public void updateUser(User user) {
        userRepository.saveAndFlush(user);
    }
}
