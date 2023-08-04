package ru.practicum.main_service.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main_service.server.database.CommentDatabase;
import ru.practicum.main_service.server.database.EventDatabase;
import ru.practicum.main_service.server.database.ParticipationRequestsDatabase;
import ru.practicum.main_service.server.dto.CommentDto;
import ru.practicum.main_service.server.dto.CommentResponseDto;
import ru.practicum.main_service.server.dto.EventDtoResponse;
import ru.practicum.main_service.server.dto.MainServiceDtoConstants;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.ConflictError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class CommentService {
    DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;

    @Autowired
    private CommentDatabase commentDatabase;
    @Autowired
    private EventDatabase eventDatabase;
    @Autowired
    private ParticipationRequestsDatabase requestsDatabase;

    public CommentResponseDto adminGetCommentById(Integer id) {
        return commentDatabase.getComment(id);
    }

    public CommentResponseDto adminDeleteCommentById(Integer id) {
        CommentResponseDto oldComment = commentDatabase.getComment(id);
        commentDatabase.deleteComment(id);
        return oldComment;
    }

    public CommentResponseDto getCommentById(Integer userId, Integer id) {
        CommentResponseDto comment = commentDatabase.getComment(id);
        if(!Objects.equals(comment.getUser().getId(), userId)) {
            throw new ConflictError("Вы не можете получить доступ к чужому комментарию.");
        }
        return comment;
    }

    public List<CommentResponseDto> adminGetAllCommentsForUser(int userId, int from, int size) {
        return commentDatabase.getCommentsForAnUser(userId, from, size);
    }

    public List<CommentResponseDto> getAllCommentsForEvent(int eventId, int from, int size) {
        return commentDatabase.getCommentsForAnEvent(eventId, from, size);
    }

    public CommentResponseDto postComment(CommentDto comment) {
        comment.setEdited(false);
        comment.setCreation_date(LocalDateTime.now().format(formatter));

        if(!comment.isValidToPost()) {
            throw new BadRequestError("Ошибка объекта", comment);
        }

        EventDtoResponse event = eventDatabase.getEvent(comment.getEventId());
        if(LocalDateTime.parse(event.getEventDate(), formatter).isAfter(LocalDateTime.now())) {
            throw new ConflictError("Нельзя опубликовать комментарий о событии, что ещё не случилось.");
        }
        if(!Objects.equals(event.getInitiator().getId(), comment.getUserId())
                &&
                !requestsDatabase.hasConfirmedRequestFromUser(comment.getEventId(), comment.getUserId())) {
            throw new ConflictError("Нельзя опубликовать комментарий о событии если вы не владелец или участник.");
        }
        return commentDatabase.createComment(comment);
    }

    public CommentResponseDto updateComment(CommentDto comment) {
        comment.setEdited(true);

        if(!comment.isValidToEdit()) {
            throw new BadRequestError("Ошибка объекта", comment);
        }

        CommentResponseDto oldComment = commentDatabase.getComment(comment.getId());
        if(!Objects.equals(oldComment.getUser().getId(), comment.getUserId())) {
            throw new ConflictError("Вы не автор комментария.");
        }
        return commentDatabase.editComment(comment);
    }
}
