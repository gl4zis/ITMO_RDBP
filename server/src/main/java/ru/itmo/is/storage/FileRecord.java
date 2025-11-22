package ru.itmo.is.storage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.bid.BidFile;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileRecord {
    String name;
    String key;

    public FileRecord(BidFile file) {
        this.name = file.getName();
        this.key = file.getKey();
    }
}
