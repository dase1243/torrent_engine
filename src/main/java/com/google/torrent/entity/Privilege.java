package com.google.torrent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Data
@Entity
@Table(name = "privelege")
@AllArgsConstructor
@NoArgsConstructor
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "privileges", targetEntity = Role.class, fetch = FetchType.EAGER)
    private Collection<Role> roles;

    public Privilege(final String name) {
        super();
        this.name = name;
    }
}


