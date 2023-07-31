package ru.practicum.main_service.server.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main_service.server.database.EventDatabase;
import ru.practicum.main_service.server.database.ParticipationRequestsDatabase;
import ru.practicum.main_service.server.database.UserDatabase;
import ru.practicum.main_service.server.dto.EventDtoResponse;
import ru.practicum.main_service.server.dto.MainServiceDtoConstants;
import ru.practicum.main_service.server.dto.ParticipationRequestDto;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.ConflictError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RequestsService {
    DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;

    @Autowired
    private ParticipationRequestsDatabase database;
    @Autowired
    private EventDatabase eventDatabase;
    @Autowired
    private UserDatabase userDatabase;

    public ParticipationRequestDto createRequest(int userId, int eventId) {
        EventDtoResponse event = eventDatabase.getEvent(eventId);
        userDatabase.getUser(userId);

        if (!event.getState().equals("PUBLISHED")) {
            throw new ConflictError("Заявку на участие нельзя заполнить на неопубликованное событие.");
        }
        if (userId == event.getInitiator().getId()) {
            throw new ConflictError("Заявку на участие нельзя заполнить на своё событие.");
        }
        if(database.hasRequestFromUser(eventId, userId)) {
            throw new ConflictError("Уже есть заявка от данного пользователя.");
        }
        ParticipationRequestDto request = new ParticipationRequestDto();
        request.setRequester(userId);
        request.setEvent(eventId);
        request.setCreated(formatter.format(LocalDateTime.now()));

        if(event.getParticipantLimit() == 0) { // Исходя из постман-тестов - в этом случае премодерация игнорируется.
            request.setStatus("CONFIRMED");
            return database.createRequest(request);
        } if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictError("Лимит заявок уже достигнут.");
        }

        if(event.getRequestModeration()) {
            request.setStatus("PENDING");
        } else {
            request.setStatus("CONFIRMED");
        }
        return database.createRequest(request);
    }

    public Map<String, List<ParticipationRequestDto>> handleRequestsUpdate(int userId, int eventId, JSONObject json) {
        EventDtoResponse event = eventDatabase.getEvent(eventId);

        if(event.getInitiator().getId() != userId) {
            throw new BadRequestError("В событиях пользователя не найдено запрошенное.");
        }

        int[] ids = json.getJSONArray("requestIds").toList().stream().mapToInt(x -> (Integer) x).toArray();
        String status = json.getString("status");

        if(status.equals("CONFIRMED") && event.getConfirmedRequests() + ids.length > event.getParticipantLimit()) {
            throw new ConflictError("Если вы одобрите эти заявки - они превысят лимит.");
        }

        for(int id : ids) {
            ParticipationRequestDto request = database.getRequest(id);
            if(request.getStatus().equals("CONFIRMED") && status.equals("REJECTED")) {
                throw new ConflictError("Нельзя отменить уже одобренную заявку.");
            }
            if(database.updateRequestStatus(id, status).getEvent() != eventId) {
                throw new BadRequestError("Одна из заявок указанных в запросе не принадлежит нужному событию.");
            }
        }
        List<ParticipationRequestDto> confirmedRequests = database.getRequestsForEvent(eventId, "CONFIRMED");
        List<ParticipationRequestDto> rejectedRequests = database.getRequestsForEvent(eventId, "REJECTED");

        return Map.of("confirmedRequests", database.getRequestsForEvent(eventId, "CONFIRMED"),
                "rejectedRequests", database.getRequestsForEvent(eventId, "REJECTED"));
    }

    public ParticipationRequestDto cancelRequest(int userId, int requestId) {
        if(database.getRequest(requestId).getRequester() != userId) {
            throw new ConflictError("Заявка не принадлежит указанному пользователю.");
        }
        return database.updateRequestStatus(requestId, "CANCELED");
    }

    public List<ParticipationRequestDto> getRequests(int userId) {
        userDatabase.getUser(userId);
        return database.getRequestsForUser(userId);
    }

    public List<ParticipationRequestDto> getRequestsForEvent(int userId, int eventId) {
        EventDtoResponse event = eventDatabase.getEvent(eventId);

        if(event.getInitiator().getId() != userId) {
            throw new BadRequestError("В событиях пользователя не найдено запрошенное.");
        }

        return database.getRequestsForEvent(eventId);
    }
}
