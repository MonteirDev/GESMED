package com.gyanMonteiro.gesmed.controller;

import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.dto.response.ProductCreateResponseDTO;
import com.gyanMonteiro.gesmed.dto.response.ProductResponseDTO;
import com.gyanMonteiro.gesmed.service.ProductService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        @Test
        @DisplayName("Should return 400 with error message when request body is invalid")
        void shouldReturn400WhenPostBodyIsInvalid() throws Exception {
            UUID id = UUID.randomUUID();
            ProductCreateResponseDTO response = new ProductCreateResponseDTO(id);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post("/product")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Produto Teste",
                                        "sku": "SKUU-0001",
                                        "unitofMeasure": "mg",
                                        "dosage": "10mg",
                                        "manufacturerId": "550e8400-e29b-41d4-a716-446655440000"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.sku").value("SKU must start with 3 uppercase letters followed by a hyphen and 5 numbers (ex: ABC-12345)"));
        }

        @Test
        @DisplayName("Should return 400 when required field is missing")
        void shouldReturn400WhenRequiredFieldIsMissing() throws Exception {
            UUID id = UUID.randomUUID();
            ProductCreateResponseDTO response = new ProductCreateResponseDTO(id);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post("/product")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Produto Teste",
                                        "sku": "SKU-00001",
                                        "dosage": "10mg",
                                        "manufacturerId": "550e8400-e29b-41d4-a716-446655440000"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
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

    @Nested
    @DisplayName("GET /manufacturer")
    class ListAllTests {
        @Test
        @DisplayName("Should return 200 with JSON list of all manufacturers")
        void shouldReturnAllManufacturers() throws Exception {
            UUID manufactureId = UUID.randomUUID();
            String manufactureName = "Fabricante 1";

            ProductResponseDTO p1 = new ProductResponseDTO(
                    UUID.randomUUID(),
                    "Produto 1",
                    "SKU-00001",
                    "mg",
                    "10mg",
                    LocalDateTime.now(),
                    true,
                    manufactureId,
                    manufactureName
            );

            ProductResponseDTO p2 = new ProductResponseDTO(
                    UUID.randomUUID(),
                    "Produto 2",
                    "SKU-00002",
                    "mg",
                    "10mg",
                    LocalDateTime.now(),
                    true,
                    manufactureId,
                    manufactureName
            );

            ProductResponseDTO p3 = new ProductResponseDTO(
                    UUID.randomUUID(),
                    "Produto 3",
                    "SKU-00003",
                    "mg",
                    "10mg",
                    LocalDateTime.now(),
                    true,
                    manufactureId,
                    manufactureName
            );

            List<ProductResponseDTO> productList = List.of(p1, p2, p3);

            when(service.findAll()).thenReturn(productList);

            mockMvc.perform(get("/product"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].name").value("Produto 1"))
                    .andExpect(jsonPath("$[1].name").value("Produto 2"))
                    .andExpect(jsonPath("$[2].name").value("Produto 3"));
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

            mockMvc.perform(delete("/product/{id}", id))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }
    }
}