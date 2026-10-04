package kr.co.abacus.abms.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.EmployeeAvatar;
import kr.co.abacus.abms.project.ProjectAssignmentRepository;
import kr.co.abacus.abms.support.Fixtures;

class AccessServiceTest {

    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final ProjectAssignmentRepository assignmentRepository = mock(ProjectAssignmentRepository.class);
    private final AccessService accessService = new AccessService(departmentRepository, assignmentRepository);
    private final LoginUser member = new LoginUser(1L, 10L, 100L, "m@test.co", "참여자", EmployeeAvatar.SKY_GLOW, "{noop}x", true, false,
            Fixtures.grants(PermissionScope.CURRENT_PARTICIPATION, PermissionCode.PROJECT_READ));

    @BeforeEach
    void setUp() {
        when(departmentRepository.findAll()).thenReturn(List.of());
        when(assignmentRepository.findActiveProjectIds(anyLong(), any())).thenReturn(List.of(7L));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void 웹_요청_안에서는_권한_범위를_한_번만_계산한다() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        for (int i = 0; i < 5; i++) {
            assertThat(accessService.scopeOf(member, PermissionCode.PROJECT_READ).projectIds()).containsExactly(7L);
        }

        verify(assignmentRepository, times(1)).findActiveProjectIds(anyLong(), any());
    }

    @Test
    void 요청_밖에서는_매번_계산한다() {
        accessService.scopeOf(member, PermissionCode.PROJECT_READ);
        accessService.scopeOf(member, PermissionCode.PROJECT_READ);

        verify(assignmentRepository, times(2)).findActiveProjectIds(anyLong(), any());
    }

}
