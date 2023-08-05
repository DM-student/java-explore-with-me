package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.server.dto.CommentDto;
import ru.practicum.main_service.server.dto.CommentResponseDto;
import ru.practicum.main_service.server.services.CommentService;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

// Важное уточнение. Я решил выделить комментарии в отдельный эндпоинт так как по моему мнению,
// они должны вызываться отдельно, догружаясь постепенно
@RestController
public class CommentController {
    @Autowired
    private CommentService service;
    @Autowired
    private EarlyRequestHandler earlyRequestHandler;

    @GetMapping("/admin/comments/{commentId}")
    public CommentResponseDto adminGet(HttpServletRequest servletRequest,
                                       @PathVariable int commentId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.adminGetCommentById(commentId);
    }

    @GetMapping("/admin/comments/user/{userId}")
    public List<CommentResponseDto> adminGetForUser(HttpServletRequest servletRequest,
                                                    @PathVariable Integer userId,
                                                    @RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "10") Integer size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.adminGetAllCommentsForUser(userId, from, size);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> adminDelete(HttpServletRequest servletRequest,
                                                          @PathVariable int commentId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return new ResponseEntity<>(service.adminDeleteCommentById(commentId), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> delete(HttpServletRequest servletRequest,
                                                     @PathVariable int commentId, @RequestParam Integer userId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return new ResponseEntity<>(service.deleteCommentById(userId, commentId), HttpStatus.NO_CONTENT);
    }

    @GetMapping("/comments/{commentId}")
    public CommentResponseDto getById(HttpServletRequest servletRequest,
                                      @PathVariable int commentId, @RequestParam Integer userId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getCommentById(userId, commentId);
    }

    @GetMapping("/comments/event/{eventId}")
    public List<CommentResponseDto> getForEvent(HttpServletRequest servletRequest,
                                                @PathVariable Integer eventId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getAllCommentsForEvent(eventId, from, size);
    }

    @PostMapping("/comments/event/{eventId}")
    public ResponseEntity<CommentResponseDto> post(HttpServletRequest servletRequest,
                                                   @RequestBody CommentDto comment,
                                                   @PathVariable Integer eventId, @RequestParam Integer userId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        comment.setEventId(eventId);
        comment.setUserId(userId);

        return new ResponseEntity<>(service.postComment(comment), HttpStatus.CREATED);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> patch(HttpServletRequest servletRequest,
                                                    @RequestBody CommentDto comment,
                                                    @PathVariable Integer commentId,
                                                    @RequestParam Integer userId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        comment.setId(commentId);
        comment.setUserId(userId);

        return new ResponseEntity<>(service.updateComment(comment), HttpStatus.OK);
    }
}
