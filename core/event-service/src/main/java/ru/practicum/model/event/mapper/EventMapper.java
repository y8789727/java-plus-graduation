package ru.practicum.model.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;

@Mapper(componentModel = "spring",
        imports = {ru.practicum.model.category.mapper.CategoryMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", source = "initiatorId")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    Event toEntity(NewEventDto dto, Long initiatorId, Category category, State state);

    default Event toEntity(NewEventDto dto, Long initiatorId, Category category) {
        return toEntity(dto, initiatorId, category, State.PENDING);
    }

    @Mapping(target = "category", expression = "java(CategoryMapper.mapToCategoryDto(event.getCategory()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "id", expression = "java(event.getId())")
    EventFullDto toFullDto(Event event, UserShortDto initiator);

    @Mapping(target = "category", expression = "java(CategoryMapper.mapToCategoryDto(event.getCategory()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "id", expression = "java(event.getId())")
    EventShortDto toShortDto(Event event, UserShortDto initiator);

}