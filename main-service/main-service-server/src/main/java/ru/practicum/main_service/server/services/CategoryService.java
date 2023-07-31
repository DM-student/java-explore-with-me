package ru.practicum.main_service.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.database.CategoryDatabase;
import ru.practicum.main_service.server.dto.CategoryDto;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.ConflictError;

import java.util.List;

@Component
public class CategoryService {
    @Autowired
    private CategoryDatabase database;
    @Autowired
    private EventService eventService;

    public List<CategoryDto> getAll(int from, int limit) {
        return database.getAllCategories(from, limit);
    }

    public CategoryDto getById(int id) {
        return database.getCategory(id);
    }

    public CategoryDto delete(int id) {
        CategoryDto deletedCategory = getById(id);
        if (!eventService.getAllAdmin(null, null,
                List.of(id),
                null, null, 0, 100).isEmpty()) {
            throw new ConflictError("К категории уже привязаны события.");
        }
        database.deleteCategory(id);
        return deletedCategory;
    }

    public CategoryDto post(CategoryDto category) {
        if (!category.isValid()) {
            throw new BadRequestError("Ошибка объекта.", category);
        }
        List<CategoryDto> similarCategories = database.getCategoriesByName(category.getName());
        if (!similarCategories.isEmpty() && similarCategories.get(0).getId() != category.getId()) {
            throw new ConflictError("Название категории уже занято.", category);
        }
        return database.createCategory(category);
    }

    public CategoryDto update(CategoryDto category) {
        if (!category.isValid()) {
            throw new BadRequestError("Ошибка объекта.", category);
        }
        List<CategoryDto> similarCategories = database.getCategoriesByName(category.getName());
        if (!similarCategories.isEmpty() && similarCategories.get(0).getId() != category.getId()) {
            throw new ConflictError("Название категории уже занято.", category);
        }
        return database.patchCategory(category);
    }
}
