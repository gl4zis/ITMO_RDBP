package ru.itmo.is.dto.response;


import org.springframework.core.io.Resource;

public record FileResponse(String name, Resource data) {
}
