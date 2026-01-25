package ru.practicum.dto;

public record CommentDto(
        Long id,
        String author,
        String text
) {
}