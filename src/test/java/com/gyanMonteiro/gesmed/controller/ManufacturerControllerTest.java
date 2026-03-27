package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.Exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerCreateResponseDTO;

import com.gyanMonteiro.gesmed.ResponseDTO.ManufacturerResponseDTO;
import com.gyanMonteiro.gesmed.Service.ManufacturerService;

import jakarta.validation.constraints.Max;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManufacturerController.class)
@AutoConfigureMockMvc(addFilters = false)
class ManufacturerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManufacturerService service;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;


    @Nested
    @DisplayName("POST /manufacturer")
    class CreateTests {
        @Test
        @DisplayName("Should return 200 with ID when manufacturer is created")
        void shouldCreateManufacturer() throws Exception {
            UUID id = UUID.randomUUID();
            ManufacturerCreateResponseDTO response = new ManufacturerCreateResponseDTO(id);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post("/manufacturer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test Manufacturer",
                                        "cnpj": "12.345.678/0001-99"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }
        @Test
        @DisplayName("Should return 400 with error message when request body is invalid")
        void shouldReturn400WhenPostBodyIsInvalid() throws Exception {
            UUID id = UUID.randomUUID();
            ManufacturerCreateResponseDTO response = new ManufacturerCreateResponseDTO(id);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post("/manufacturer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test Manufacturer",
                                        "cnpj": "1234567800019X"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.cnpj").value("CNPJ deve conter 14 dígitos"));
        }

        @Test
        @DisplayName("Should return 400 when required field is missing")
        void shouldReturn400WhenRequiredFieldIsMissing() throws Exception {
            UUID id = UUID.randomUUID();
            ManufacturerCreateResponseDTO response = new ManufacturerCreateResponseDTO(id);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post("/manufacturer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Test Manufacturer"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /manufacturer/{id}")
    class FindByIdTests {

        @Test
        @DisplayName("Should return 200 with manufacturer details when found")
        void shouldReturnManufacturer() throws Exception {
            UUID id = UUID.randomUUID();
            ManufacturerResponseDTO response = new ManufacturerResponseDTO(id, "CIMED", "12.345.678/0001-99", LocalDateTime.now(), true);

            when(service.findById(id)).thenReturn(response);

            mockMvc.perform(get("/manufacturer/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("CIMED"))
                    .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-99"))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Should return 404 when manufacturer not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            when(service.findById(id)).thenThrow(new ResourceNotFoundException("Manufacturer not found"));

            mockMvc.perform(get("/manufacturer/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /manufacturer/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Should return 200 with updated data")
        void shouldUpdateManufacturer() throws Exception {
            UUID id = UUID.randomUUID();
            ManufacturerResponseDTO response = new ManufacturerResponseDTO(id, "CIMED", "12.345.678/0001-99", LocalDateTime.now(), true);

            when(service.update(ArgumentMatchers.eq(id), ArgumentMatchers.any())).thenReturn(response);

            mockMvc.perform(put("/manufacturer/{id}", id).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "CIMED",
                                        "cnpj": "12.345.678/0001-99"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("CIMED"));
        }

        @Test
        @DisplayName("Should return 404 when manufacturer not found on update")
        void shouldReturn404WhenNotFoundOnUpdate() throws Exception {
            UUID id = UUID.randomUUID();

            when(service.update(ArgumentMatchers.eq(id), ArgumentMatchers.any())).thenThrow(new ResourceNotFoundException("Manufacturer not found"));

            mockMvc.perform(put("/manufacturer/{id}", id).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "CIMED",
                                        "cnpj": "12.345.678/0001-99"
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /manufacturer")
    class ListAllTests {
        @Test
        @DisplayName("Should return 200 with JSON list of all manufacturers")
        void shouldReturnAllManufacturers() throws Exception {
            ManufacturerResponseDTO m1 = new ManufacturerResponseDTO(UUID.randomUUID(), "CIMED", "12.345.678/0001-99", LocalDateTime.now(), true);
            ManufacturerResponseDTO m2 = new ManufacturerResponseDTO(UUID.randomUUID(), "NEO QUIMICA", "23.143.678/9254-92", LocalDateTime.now(), true);
            ManufacturerResponseDTO m3 = new ManufacturerResponseDTO(UUID.randomUUID(), "EUROFARMA", "19.386.678/5641-09", LocalDateTime.now(), true);

            List<ManufacturerResponseDTO> manufacturerList = List.of(m1, m2, m3);

            when(service.findAll()).thenReturn(manufacturerList);

            mockMvc.perform(get("/manufacturer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].name").value("CIMED"))
                    .andExpect(jsonPath("$[1].name").value("NEO QUIMICA"))
                    .andExpect(jsonPath("$[2].name").value("EUROFARMA"));
        }
    }

    @Nested
    @DisplayName("DELETE /manufacturer/{id}")
    class DeleteTests {
        @Test
        @DisplayName("Should return 204 with no body when manufacturer is deleted")
        void shouldReturn204WhenManufacturerIsDeleted() throws Exception {
            UUID id = UUID.randomUUID();

            doNothing().when(service).delete(id);

            mockMvc.perform(delete("/manufacturer/{id}", id))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }
    }
}