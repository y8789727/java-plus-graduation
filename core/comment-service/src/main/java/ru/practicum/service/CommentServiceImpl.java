package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.StateCommentDto;
import ru.practicum.dto.UpdateCommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.CommentStateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentState;
import ru.practicum.model.DateSort;
import ru.practicum.model.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<CommentDto> getComments(long userId) {
        UserDto author = getUserById(userId);

        List<Comment> comments = commentRepository.findAllByAuthorId(author.id());

        final Map<Long, UserDto> authors = getAuthorsByComments(comments);

        return comments.stream()
                .map(comment -> CommentMapper.mapToCommentDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, NewCommentDto commentDto) {
        UserDto author = getUserById(userId);

        EventFullDto event = eventClient.getEventById(commentDto.event())
                .orElseThrow(() -> new NotFoundException("Событие с id " + commentDto.event() + " не найдено"));

        Comment comment = commentRepository.save(CommentMapper.mapToComment(commentDto, author.id(), event.id(), CommentState.WAITING));
        return CommentMapper.mapToCommentDto(comment, author);
    }

    @Override
    @Transactional
    public CommentDto updateComment(long userId, UpdateCommentDto commentDto) {
        UserDto author = getUserById(userId);

        Comment comment = commentRepository.findById(commentDto.id())
                .orElseThrow(() -> new NotFoundException("Комментария с id " + commentDto.id() + " не найдено"));

        if (!comment.getAuthorId().equals(author.id())) {
            throw new AccessDeniedException("Редактировать может только автор комментария");
        }
        comment.setText(commentDto.text());
        return CommentMapper.mapToCommentDto(comment, author);
    }

    @Override
    @Transactional
    public void deleteComment(long userId, long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));
        if (comment.getAuthorId().equals(userId)) {
            commentRepository.delete(comment);
        } else {
            throw new AccessDeniedException("Удалять комментарий может только автор");
        }
    }

    @Override
    public List<StateCommentDto> getComments(String text, DateSort sort) {
        Iterable<Comment> comments = commentRepository.findAll(CommentRepository.Predicate.textFilter(text), getSortDate(sort));

        final Map<Long, UserDto> authors = getAuthorsByComments(comments);

        return StreamSupport.stream(comments.spliterator(), false)
                .map(comment -> CommentMapper.mapToAdminDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    @Transactional
    public StateCommentDto reviewComment(long comId, boolean approved) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));

        if (!comment.getState().equals(CommentState.WAITING)) {
            throw new CommentStateException("Подтверждение комментария может осуществляться только если статус равен WAITING");
        }

        if (approved) {
            comment.setState(CommentState.APPROVED);
        } else {
            comment.setState(CommentState.REJECTED);
        }

        return CommentMapper.mapToAdminDto(comment, userClient.findById(comment.getAuthorId()).orElse(new UserDto("", null, "")));
    }

    @Override
    @Transactional
    public void deleteComment(long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByState(CommentState state, DateSort sort) {
        Iterable<Comment> comments = commentRepository.findAll(CommentRepository.Predicate.stateFilter(state), getSortDate(sort));

        final Map<Long, UserDto> authors = getAuthorsByComments(comments);

        return StreamSupport.stream(comments.spliterator(), false)
                .map(comment -> CommentMapper.mapToCommentDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    public List<CommentDto> getCommentsByEvent(long eventId, DateSort sort) {
        Iterable<Comment> comments = commentRepository.findAll(CommentRepository.Predicate.eventFilter(eventId), getSortDate(sort));

        final Map<Long, UserDto> authors = getAuthorsByComments(comments);

        return StreamSupport.stream(comments.spliterator(), false)
                .map(comment -> CommentMapper.mapToCommentDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    private Sort getSortDate(DateSort sort) {
        return (sort == DateSort.DESC) ?
                Sort.by("created").descending() : Sort.by("created").ascending();
    }

    private UserDto getUserById(Long userId) {
        return userClient.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Map<Long, UserDto> getAuthorsByComments(Iterable<Comment> comments) {
        List<Long> userIds = StreamSupport.stream(comments.spliterator(), false)
                .map(Comment::getAuthorId)
                .toList();

        return userClient.findByIds(userIds, 0, userIds.size()).stream()
                .collect(Collectors.toMap(UserDto::id, Function.identity()));
    }
}