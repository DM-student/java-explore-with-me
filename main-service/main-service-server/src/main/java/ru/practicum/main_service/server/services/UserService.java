package ru.practicum.main_service.server.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.database.UserDatabase;
import ru.practicum.main_service.server.dto.UserDto;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.ConflictError;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UserService {
    @Autowired
    private UserDatabase userDB;

    public List<UserDto> getUsers(List<Integer> ids, int from, int size) {
        if (ids != null) {
            List<UserDto> users = new ArrayList<>();
            for (Integer id : ids) {
                try {
                    users.add(userDB.getUser(id));
                } catch (NotFoundError e) {
                    log.info("Была совершена попытка получить несуществующего пользователя.");
                }
            }
            return users;
        }
        return userDB.getUsers(from, size);
    }

    public UserDto deleteUser(int id) {
        UserDto deletedUser = userDB.getUser(id); // в случае провала само выкинет ошибку.
        userDB.deleteUser(id);
        return deletedUser;
    }

    public UserDto createUser(UserDto user) {
        if (!user.isValid()) {
            throw new BadRequestError("Ошибка валидации входящих данных.", user);
        }
        if (!userDB.getUsersByName(user.getName()).isEmpty()) {
            throw new ConflictError("Пользователь с этим именем уже существует.");
        }
        if (!userDB.getUsersByEmail(user.getEmail()).isEmpty()) {
            throw new ConflictError("Пользователь c этой почтой уже существует.");
        }
        return userDB.createUser(user);
    }
}
