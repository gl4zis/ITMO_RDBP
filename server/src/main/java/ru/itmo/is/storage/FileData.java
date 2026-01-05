package ru.itmo.is.storage;


import org.springframework.core.io.Resource;

public record FileData(String name, Resource data) {
}
