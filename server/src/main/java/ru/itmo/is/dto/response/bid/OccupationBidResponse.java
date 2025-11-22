package ru.itmo.is.dto.response.bid;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.bid.OccupationBid;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OccupationBidResponse extends BidResponse {
    Integer universityId;
    Integer dormitoryId;

    public OccupationBidResponse(OccupationBid occupationBid) {
        super(occupationBid);
        this.universityId = occupationBid.getUniversity().getId();
        this.dormitoryId = occupationBid.getDormitory().getId();
    }
}
