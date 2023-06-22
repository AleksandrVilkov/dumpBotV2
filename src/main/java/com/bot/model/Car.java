package com.bot.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Car implements ModelObject {
    private int id;
    private LocalDate createDate;
    private Concern concern;
    private Model model;
    private Engine engine;
    private BoltPattern boltPattern;
    private Brand brand;
}
