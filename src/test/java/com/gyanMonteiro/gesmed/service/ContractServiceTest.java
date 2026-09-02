package com.gyanMonteiro.gesmed.service;

import com.gyanMonteiro.gesmed.dto.request.ContractRequestDTO;
import com.gyanMonteiro.gesmed.dto.response.ContractResponseDTO;
import com.gyanMonteiro.gesmed.entity.Contract;
import com.gyanMonteiro.gesmed.enums.ContractStatus;
import com.gyanMonteiro.gesmed.exceptions.BusinessException;
import com.gyanMonteiro.gesmed.exceptions.ResourceNotFoundException;
import com.gyanMonteiro.gesmed.mapper.ContractMapper;
import com.gyanMonteiro.gesmed.repository.ContractRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository repository;

    @Mock
    private ContractMapper mapper;

    @InjectMocks
    private ContractService service;

    private Contract buildContract(ContractStatus status) {
        Contract contract = new Contract();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("CONT-2026-001");
        contract.setStartDate(LocalDate.of(2026, 1, 1));
        contract.setEndDate(LocalDate.of(2026, 12, 31));
        contract.setStatus(status);
        return contract;
    }

    private ContractResponseDTO buildResponse(Contract contract) {
        return new ContractResponseDTO(
                contract.getId(),
                contract.getContractNumber(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getStatus(),
                null,
                null,
                List.of()
        );
    }

    @Nested
    class CreateTests {
        @Test
        @DisplayName("should create contract and return response")
        void shouldCreateContractAndReturnResponse() {
            ContractRequestDTO dto = new ContractRequestDTO(
                    "CONT-2026-001", "2026-01-01", "2026-12-31", UUID.randomUUID(), List.of()
            );
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(mapper.toEntity(dto)).thenReturn(contract);
            when(mapper.toResponse(contract)).thenReturn(response);

            ContractResponseDTO result = service.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(ContractStatus.ACTIVE);
            verify(repository).save(contract);
        }
    }

    @Nested
    class UpdateEndDateTests {
        @Test
        @DisplayName("should update end date and return response")
        void shouldUpdateEndDateAndReturnResponse() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractRequestDTO dto = new ContractRequestDTO(
                    null, null, "2027-12-31", null, null
            );
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            ContractResponseDTO result = service.updateEndDate(contract.getId(), dto);

            assertThat(result).isNotNull();
            verify(repository).save(contract);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when contract not found")
        void shouldThrowResourceNotFoundExceptionWhenContractNotFound() {
            UUID id = UUID.randomUUID();
            ContractRequestDTO dto = new ContractRequestDTO(null, null, "2027-12-31", null, null);

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateEndDate(id, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Contract not found");
        }
    }

    @Nested
    class FindByIdTests {
        @Test
        @DisplayName("should return contract when id exists")
        void shouldReturnContractWhenIdExists() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            ContractResponseDTO result = service.findById(contract.getId());

            assertThat(result).isNotNull();
            assertThat(result.contractNumber()).isEqualTo("CONT-2026-001");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when id does not exist")
        void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Contract not found");
        }
    }

    @Nested
    class FindByClientIdTests {
        @Test
        @DisplayName("should return contracts for given client")
        void shouldReturnContractsForGivenClient() {
            UUID clientId = UUID.randomUUID();
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findByClientId(clientId)).thenReturn(List.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            List<ContractResponseDTO> result = service.findByClientId(clientId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when client has no contracts")
        void shouldReturnEmptyListWhenClientHasNoContracts() {
            UUID clientId = UUID.randomUUID();
            when(repository.findByClientId(clientId)).thenReturn(List.of());

            List<ContractResponseDTO> result = service.findByClientId(clientId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByStatusTests {
        @Test
        @DisplayName("should return contracts with given status")
        void shouldReturnContractsWithGivenStatus() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findByStatus(ContractStatus.ACTIVE)).thenReturn(List.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            List<ContractResponseDTO> result = service.findByStatus(ContractStatus.ACTIVE);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no contracts exist with given status")
        void shouldReturnEmptyListWhenNoContractsExistWithGivenStatus() {
            when(repository.findByStatus(ContractStatus.EXPIRED)).thenReturn(List.of());

            List<ContractResponseDTO> result = service.findByStatus(ContractStatus.EXPIRED);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByContractNumberTests {
        @Test
        @DisplayName("should return contract with given contract number")
        void shouldReturnContractWithGivenContractNumber() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findByContractNumber("CONT-2026-001")).thenReturn(Optional.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            List<ContractResponseDTO> result = service.findByContractNumber("CONT-2026-001");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).contractNumber()).isEqualTo("CONT-2026-001");
        }

        @Test
        @DisplayName("should return empty list when no contract exists with given contract number")
        void shouldReturnEmptyListWhenNoContractExistsWithGivenContractNumber() {
            when(repository.findByContractNumber("CONT-2026-999")).thenReturn(Optional.empty());

            List<ContractResponseDTO> result = service.findByContractNumber("CONT-2026-999");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindAllTests {
        @Test
        @DisplayName("should return all contracts")
        void shouldReturnAllContracts() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findAll()).thenReturn(List.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            List<ContractResponseDTO> result = service.findAll();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no contracts exist")
        void shouldReturnEmptyListWhenNoContractsExist() {
            when(repository.findAll()).thenReturn(List.of());

            List<ContractResponseDTO> result = service.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class SetStatusCancelledTests {
        @Test
        @DisplayName("should cancel active contract")
        void shouldCancelActiveContract() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            service.setStatusCancelled(contract.getId());

            assertThat(contract.getStatus()).isEqualTo(ContractStatus.CANCELLED);
            verify(repository).save(contract);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is already cancelled")
        void shouldThrowBusinessExceptionWhenContractIsAlreadyCancelled() {
            Contract contract = buildContract(ContractStatus.CANCELLED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusCancelled(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is expired")
        void shouldThrowBusinessExceptionWhenContractIsExpired() {
            Contract contract = buildContract(ContractStatus.EXPIRED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusCancelled(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class SetStatusSuspendedTests {
        @Test
        @DisplayName("should suspend active contract")
        void shouldSuspendActiveContract() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            service.setStatusSuspended(contract.getId());

            assertThat(contract.getStatus()).isEqualTo(ContractStatus.SUSPENDED);
            verify(repository).save(contract);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is already suspended")
        void shouldThrowBusinessExceptionWhenContractIsAlreadySuspended() {
            Contract contract = buildContract(ContractStatus.SUSPENDED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusSuspended(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is expired")
        void shouldThrowBusinessExceptionWhenContractIsExpired() {
            Contract contract = buildContract(ContractStatus.EXPIRED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusSuspended(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is cancelled")
        void shouldThrowBusinessExceptionWhenContractIsCancelled() {
            Contract contract = buildContract(ContractStatus.CANCELLED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusSuspended(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class SetStatusActiveTests {
        @Test
        @DisplayName("should activate suspended contract")
        void shouldActivateSuspendedContract() {
            Contract contract = buildContract(ContractStatus.SUSPENDED);
            ContractResponseDTO response = buildResponse(contract);

            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));
            when(mapper.toResponse(contract)).thenReturn(response);

            service.setStatusActive(contract.getId());

            assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
            verify(repository).save(contract);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is already active")
        void shouldThrowBusinessExceptionWhenContractIsAlreadyActive() {
            Contract contract = buildContract(ContractStatus.ACTIVE);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusActive(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is expired")
        void shouldThrowBusinessExceptionWhenContractIsExpired() {
            Contract contract = buildContract(ContractStatus.EXPIRED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusActive(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when contract is cancelled")
        void shouldThrowBusinessExceptionWhenContractIsCancelled() {
            Contract contract = buildContract(ContractStatus.CANCELLED);
            when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.setStatusActive(contract.getId()))
                    .isInstanceOf(BusinessException.class);
        }
    }
}