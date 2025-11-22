package ru.itmo.is.dto.response.bid;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.dto.response.user.UserResponse;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.bid.BidFile;

import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BidResponse {
    Long number;
    UserResponse sender;
    @Nullable
    UserResponse manager;
    @Nullable
    String comment;
    String text;
    Bid.Type type;
    List<Attachment> attachments;
    Bid.Status status;

    public BidResponse(Bid bid) {
        this.number = bid.getId();
        this.sender = new UserResponse(bid.getSender());
        this.manager = bid.getManager() == null ? null : new UserResponse(bid.getManager());
        this.comment = bid.getComment();
        this.text = bid.getText();
        this.type = bid.getType();
        this.attachments = bid.getFiles().stream().map(Attachment::new).toList();
        this.status = bid.getStatus();
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    static class Attachment {
        String filename;
        String downloadKey;

        private Attachment(BidFile bidFile) {
            this.filename = bidFile.getName();
            this.downloadKey = bidFile.getKey();
        }
    }
}
