package com.gyanMonteiro.gesmed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyanMonteiro.gesmed.Exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.ResponseDTO.ProductResponseDTO;
import com.gyanMonteiro.gesmed.Service.ProductService;
import com.gyanMonteiro.gesmed.entity.Manufacturer;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private ProductService service;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    @DisplayName("POST /product")
    class CreateTests {
        @Test
        @DisplayName("Should return 200 with ID when manufacturer is created")
        void shouldCreateManufacturer() throws Exception {
            UUID id = UUID.randomUUID();
            ProductCreateResponseDTO response = new ProductCreateResponseDTO(id);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post("/product")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "name": "Paracetamol",
                        "sku": "SKU-12345",
                        "unitOfMeasure": "mg",
                        "dosage": "500mg",
                        "manufacturerId": "%s"
                    }
                    """.formatted(UUID.randomUUID())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }
    }

    @Nested
    @DisplayName("GET /product/{id}")
    class FindByIdTests {
        @Test
        @DisplayName("Should return 200 with manufacturer details when found")
        void shouldReturnManufacturer() throws Exception {
            UUID id = UUID.randomUUID();
            UUID manufacturerId = UUID.randomUUID();
            Manufacturer manufacturer = new Manufacturer(manufacturerId, "CIMED", "12.345.678/0001-99", LocalDateTime.now(), true);
            ProductResponseDTO response = new ProductResponseDTO(id, "Paracetamol Atualizado", "SKU-99999", "mg", "750mg", LocalDateTime.now(), true, manufacturer.getId(), manufacturer.getName());

            when(service.findById(id)).thenReturn(response);

            mockMvc.perform(get("/product/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Paracetamol Atualizado"))
                    .andExpect(jsonPath("$.sku").value("SKU-99999"));
        }

        @Test
        @DisplayName("Should return 404 when manufacturer not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            when(service.findById(id)).thenThrow(new ResourceNotFoundException("Product not found"));

            mockMvc.perform(get("/product/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /product/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Should return 200 with updated data")
        void shouldUpdateManufacturer() throws Exception {
            UUID id = UUID.randomUUID();
            UUID manufacturerId = UUID.randomUUID();
            ProductResponseDTO response = new ProductResponseDTO(id, "Paracetamol", "SKU-99999", "mg", "750mg", LocalDateTime.now(), true, manufacturerId, "CIMED");

            when(service.update(ArgumentMatchers.eq(id), ArgumentMatchers.any())).thenReturn(response);

            mockMvc.perform(put("/product/{id}", id).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                    {
                        "name": "Paracetamol",
                        "sku": "SKU-99999",
                        "unitOfMeasure": "mg",
                        "dosage": "750mg",
                        "manufacturerId": "%s"
                    }
                    """.formatted(manufacturerId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Paracetamol"));
        }

        @Test
        @DisplayName("Should return 404 when manufacturer not found on update")
        void shouldReturn404WhenNotFoundOnUpdate() throws Exception {
            UUID id = UUID.randomUUID();

            when(service.update(ArgumentMatchers.eq(id), ArgumentMatchers.any())).thenThrow(new ResourceNotFoundException("Product not found"));

            mockMvc.perform(put("/product/{id}", id).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                        {
                            "name": "Paracetamol",
                            "sku": "SKU-99999",
                            "unitOfMeasure": "mg",
                            "dosage": "750mg",
                            "manufacturerId": "%s"
                        }
                        """.formatted(UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }
    }
}