package com.financeira.api.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeira.api.application.dto.CategoryRequest;
import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.application.usecase.category.*;
import com.financeira.api.domain.exception.BusinessException;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CreateCategoryUseCase createUseCase;
    @MockBean UpdateCategoryUseCase updateUseCase;
    @MockBean DeleteCategoryUseCase deleteUseCase;
    @MockBean ListCategoriesUseCase  listUseCase;

    private CategoryResponse buildResponse(UUID id, String name) {
        return new CategoryResponse(id, name, "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldList_returnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(listUseCase.execute("user-123")).thenReturn(List.of(buildResponse(id, "Moradia")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Moradia"));
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldCreate_return201() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryRequest req = new CategoryRequest("Moradia", "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");
        when(createUseCase.execute(eq("user-123"), any())).thenReturn(buildResponse(id, "Moradia"));

        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Moradia"));
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldCreate_return422_whenDuplicate() throws Exception {
        CategoryRequest req = new CategoryRequest("Moradia", "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");
        when(createUseCase.execute(any(), any())).thenThrow(new BusinessException("já existe"));

        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldCreate_return400_whenNameBlank() throws Exception {
        CategoryRequest req = new CategoryRequest("", "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");

        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldUpdate_returnOk() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryRequest req = new CategoryRequest("Moradia", "🏠", BigDecimal.valueOf(2800), "#f87171", "expense", "essencial");
        when(updateUseCase.execute(eq("user-123"), eq(id), any())).thenReturn(buildResponse(id, "Moradia"));

        mockMvc.perform(put("/api/categories/" + id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldUpdate_return404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryRequest req = new CategoryRequest("Moradia", "🏠", BigDecimal.ZERO, "#fff", "expense", null);
        when(updateUseCase.execute(any(), eq(id), any())).thenThrow(new ResourceNotFoundException("não encontrada"));

        mockMvc.perform(put("/api/categories/" + id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldDelete_return204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteUseCase).execute("user-123", id);

        mockMvc.perform(delete("/api/categories/" + id).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldDelete_return404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("não encontrada")).when(deleteUseCase).execute(any(), eq(id));

        mockMvc.perform(delete("/api/categories/" + id).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn_4xx_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().is4xxClientError());
    }
}
