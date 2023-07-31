package ru.practicum.main_service.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main_service.server.database.CompilationDatabase;
import ru.practicum.main_service.server.dto.CompilationDto;
import ru.practicum.main_service.server.dto.CompilationDtoResponse;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.ConflictError;

import java.util.List;

@Service
public class CompilationService {
    @Autowired
    private CompilationDatabase database;

    public List<CompilationDtoResponse> getAll(int from, int size) {
        return database.getAllCompilations(from, size);
    }

    public List<CompilationDtoResponse> getAll(int from, int limit, boolean pinned) {
        return database.getAllCompilations(from, limit, pinned);
    }

    public CompilationDtoResponse getById(int id) {
        return database.getCompilation(id);
    }

    public CompilationDtoResponse delete(int id) {
        CompilationDtoResponse deletedCompilation = getById(id);
        database.deleteCompilation(id);
        return deletedCompilation;
    }

    public CompilationDtoResponse post(CompilationDto compilation) {
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        if (!compilation.isValid()) {
            throw new BadRequestError("Ошибка объекта.", compilation);
        }
        List<CompilationDtoResponse> similarCompilations = database.getByTitle(compilation.getTitle());
        if (!similarCompilations.isEmpty() && similarCompilations.get(0).getId() != compilation.getId()) {
            throw new ConflictError("Название подборки уже занято.", compilation);
        }
        return database.createCompilation(compilation);
    }

    public CompilationDtoResponse update(CompilationDto compilation) {
        if (!compilation.isValidSkipNulls()) {
            throw new BadRequestError("Ошибка объекта.", compilation);
        }
        List<CompilationDtoResponse> similarCompilations = database.getByTitle(compilation.getTitle());
        if (!similarCompilations.isEmpty() && similarCompilations.get(0).getId() != compilation.getId()) {
            throw new ConflictError("Название подборки уже занято.", compilation);
        }
        return database.patchCompilation(compilation);
    }
}
