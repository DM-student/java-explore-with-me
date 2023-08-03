package ru.practicum.main_service.server.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.CategoryDto;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CategoryDatabase {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CategoryDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CategoryDto> mapCategories(SqlRowSet rs) {
        List<CategoryDto> output = new ArrayList<>();
        while (rs.next()) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(rs.getInt("id"));
            categoryDto.setName(rs.getString("name"));
            output.add(categoryDto);
        }
        return output;
    }

    public CategoryDto getCategory(Integer id) {
        String sqlQuery =
                "SELECT * FROM categories WHERE id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        List<CategoryDto> output = mapCategories(rs);
        if (output.isEmpty()) {
            throw new NotFoundError("Не найдена категория.", id);
        }
        return output.get(0);
    }

    public List<CategoryDto> getCategoriesByName(String name) {
        String sqlQuery =
                "SELECT * FROM categories WHERE name = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, name);
        return mapCategories(rs);
    }

    public Map<Integer, CategoryDto> getCategoriesMap(List<Integer> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM categories WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sqlQuery.append(", ");
            sqlQuery.append(ids.get(i));
        }
        sqlQuery.append(");");

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery.toString());

        Map<Integer, CategoryDto> CategoriesMap = new HashMap<>();
        for (CategoryDto category : mapCategories(rs)) {
            CategoriesMap.put(category.getId(), category);
        }
        return CategoriesMap;
    }

    public List<CategoryDto> getAllCategories(int from, int limit) {
        String sqlQuery =
                "SELECT * FROM categories LIMIT ? OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, limit, from);

        return mapCategories(rs);
    }

    public void deleteCategory(int id) {
        String sqlQuery = "DELETE FROM categories " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    public CategoryDto createCategory(CategoryDto category) {
        String sqlQuery = "INSERT INTO categories (name) " +
                "VALUES (?) " +
                "RETURNING *";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, category.getName());
        return mapCategories(rs).get(0);
    }

    public CategoryDto patchCategory(CategoryDto category) {
        if (category.getName() != null) {
            String sqlQuery =
                    "UPDATE categories SET name = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, category.getName(), category.getId());
        }
        return getCategory(category.getId());
    }
}
