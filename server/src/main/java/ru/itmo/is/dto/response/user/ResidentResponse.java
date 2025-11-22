package ru.itmo.is.dto.response.user;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.dto.response.DormitoryResponse;
import ru.itmo.is.dto.response.UniversityResponse;
import ru.itmo.is.entity.user.Resident;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResidentResponse extends UserResponse {
    UniversityResponse university;
    DormitoryResponse dormitory;
    int roomNumber;
    int debt;
    @Nullable
    LocalDateTime lastCameOut;

    public ResidentResponse(
            Resident resident,
            int debt,
            @Nullable LocalDateTime lastCameOut
    ) {
        super(resident);
        this.university = new UniversityResponse(resident.getUniversity());
        this.dormitory = new DormitoryResponse(resident.getRoom().getDormitory());
        this.roomNumber = resident.getRoom().getNumber();
        this.debt = debt;
        this.lastCameOut = lastCameOut;
    }
}
