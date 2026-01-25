package ru.practicum.dto.user;

public class UserDtoMapper {
    public static UserShortDto mapUserDtoToUserShortDto(UserDto userDto) {
        return new UserShortDto(userDto.id(), userDto.name());
    }
}
