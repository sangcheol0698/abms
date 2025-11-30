package kr.co.abacus.abms.domain.project;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;

class ProjectTest {

    @Test
    void create() {
        Project project = Project.create(new ProjectCreateRequest(
            UUID.randomUUID(),
            "PROJECT_123",
            "This is a test project",
            null,
            ProjectStatus.CANCELLED,
            32_000_000L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        ));

        assertThat(project.getCode()).isEqualTo("PROJECT_123");
        assertThat(project.getName()).isEqualTo("This is a test project");
        assertThat(project.getDescription()).isNull();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
        assertThat(project.getContractAmount()).isEqualTo(Money.wons(32_000_000L));
        assertThat(project.getPeriod()).isEqualTo(new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));
    }

    @Test
    void update() {
        Project project = Project.create(new ProjectCreateRequest(
            UUID.randomUUID(),
            "PROJECT_123",
            "This is a test project",
            null,
            ProjectStatus.CANCELLED,
            32_000_000L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        ));

        project.update(new ProjectUpdateRequest(
            UUID.randomUUID(),
            "Updated Project Name",
            "Updated description",
            ProjectStatus.IN_PROGRESS,
            45_000_000L,
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 12, 31)
        ));

        assertThat(project.getName()).isEqualTo("Updated Project Name");
        assertThat(project.getDescription()).isEqualTo("Updated description");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(project.getContractAmount()).isEqualTo(Money.wons(45_000_000L));
        assertThat(project.getPeriod()).isEqualTo(new Period(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 12, 31)));
    }

    @Test
    void complete() {
        Project project = Project.create(new ProjectCreateRequest(
            UUID.randomUUID(),
            "PROJECT_123",
            "Test Project",
            null,
            ProjectStatus.IN_PROGRESS,
            32_000_000L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        ));

        project.complete();

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void cancel() {
        Project project = Project.create(new ProjectCreateRequest(
            UUID.randomUUID(),
            "PROJECT_123",
            "Test Project",
            null,
            ProjectStatus.IN_PROGRESS,
            32_000_000L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        ));

        project.cancel();

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
    }

}