package com.springboot.authentication.dao;

import com.springboot.authentication.dto.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDetailsDTO getUserByPublicId(UUID id) {

        String sql = """
            SELECT
            
                u.public_id AS publicId,
                u.salutation AS salutation,
                u.first_name AS firstName,
                u.middle_name AS middleName,
                u.last_name AS lastName,
                u.full_name AS fullName,
                u.email AS email,
                u.phone AS phone,
                u.role AS role,
                u.is_email_verified AS isEmailVerified,
                u.is_phone_verified AS isPhoneVerified,
                u.is_locked AS isLocked,
                u.is_2fa_enabled AS is2FaEnabled,
                u.created_at AS createdAt,
                cb.full_name AS createdByName,
                u.updated_at AS updatedAt,
                ub.full_name AS updatedByName

            FROM users u
            LEFT JOIN users cb ON cb.id = u.created_by
            LEFT JOIN users ub ON ub.id = u.updated_by
            WHERE u.public_id = ?
            AND u.status = 'active'
            AND u.is_locked = false
            ORDER BY u.updated_at DESC
        """;
        return executeQuery(
                sql,
                List.of(id),
                UserDetailsDTO.class
        ).stream().findFirst().orElse(null);
    }

    public <T> List<T> executeQuery(String baseQuery, List<Object> params, Class<T> mappedType) {
        return jdbcTemplate.query(baseQuery, ps -> {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i)); // JDBC is 1-indexed
                }
            }
        }, rs -> {
            List<T> result = new ArrayList<>();
            BeanPropertyRowMapper<T> rowMapper = new BeanPropertyRowMapper<>(mappedType);
            int rowNum = 0;
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs, rowNum++));
            }
            return result;
        });
    }

}
