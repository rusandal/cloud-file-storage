package ru.minikhanov.cloud_storage.models.security;

import lombok.*;
import ru.minikhanov.cloud_storage.models.security.ERole;

import javax.persistence.*;

@Setter
@Getter
@ToString
@Entity
@Table(name = "role_table")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private ERole name;
    public Role() {
    }
    public Role(ERole name) {
        this.name = name;
    }
    /*public Role (ERole eRole){
        this.name = eRole;
    }*/
}
