package com.sahrin.spas.service;

import com.sahrin.spas.dto.AdminAuditLogResponse;
import com.sahrin.spas.dto.PageResponse;
import com.sahrin.spas.entity.AdminAuditLog;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.repository.AdminAuditLogRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.AdminAuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuditServiceTest {

    @Mock
    private AdminAuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    private AdminAuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AdminAuditServiceImpl(auditLogRepository, userRepository);
    }

    @Test
    @DisplayName("logAction saves audit log with performedBy user when user exists")
    void testLogActionWithExistingUser() {
        User admin = new User("Admin User", "ADMIN001", "admin@spas.edu", "hash", Role.ADMIN, User.UserStatus.ACTIVE);
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));

        auditService.logAction("USER_APPROVED", "USER", 10L, "STU001", "Approved student", "admin@spas.edu");

        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AdminAuditLog saved = captor.getValue();
        assertEquals("USER_APPROVED", saved.getAction());
        assertEquals("USER", saved.getEntityType());
        assertEquals(10L, saved.getEntityId());
        assertEquals("STU001", saved.getEntityIdentifier());
        assertEquals("Approved student", saved.getDetails());
        assertEquals(admin, saved.getPerformedBy());
        assertEquals("admin@spas.edu", saved.getPerformedByEmail());
    }

    @Test
    @DisplayName("getAuditLogsPaged returns mapped PageResponse")
    void testGetAuditLogsPaged() {
        AdminAuditLog logEntry = new AdminAuditLog(
                "USER_STATUS_UPDATED", "USER", 5L, "LEC001",
                "Suspended account", null, "admin@spas.edu"
        );
        logEntry.setId(100L);
        logEntry.setPerformedAt(LocalDateTime.now());

        PageImpl<AdminAuditLog> page = new PageImpl<>(List.of(logEntry), PageRequest.of(0, 10), 1);
        when(auditLogRepository.searchLogs(eq("USER_STATUS_UPDATED"), isNull(), isNull(), any()))
                .thenReturn(page);

        PageResponse<AdminAuditLogResponse> response = auditService.getAuditLogsPaged(
                "USER_STATUS_UPDATED", null, null, 0, 10
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(100L, response.getContent().get(0).getId());
        assertEquals("USER_STATUS_UPDATED", response.getContent().get(0).getAction());
    }

    @Test
    @DisplayName("getAuditLogsPaged clamps size > 100 down to 100")
    void testGetAuditLogsPagedClampsSize() {
        PageImpl<AdminAuditLog> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(auditLogRepository.searchLogs(isNull(), isNull(), isNull(), eq(PageRequest.of(0, 100))))
                .thenReturn(page);

        PageResponse<AdminAuditLogResponse> response = auditService.getAuditLogsPaged(
                null, null, null, 0, 500
        );

        assertNotNull(response);
        verify(auditLogRepository).searchLogs(isNull(), isNull(), isNull(), eq(PageRequest.of(0, 100)));
    }
}
