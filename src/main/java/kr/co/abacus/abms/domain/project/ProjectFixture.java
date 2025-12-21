package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;

public class ProjectFixture {

    public static Project createProject() {
        return Project.create(createProjectCreateRequest());
    }

    public static Project createProject(Long partyId) {
        return Project.create(createProjectCreateRequest(partyId));
    }

    public static Project createProject(String code) {
        return Project.create(createProjectCreateRequest(code));
    }

    public static ProjectCreateRequest createProjectCreateRequest() {
        return new ProjectCreateRequest(
                1L,
                "PRJ-001",
                "테스트 프로젝트",
                "테스트 프로젝트 설명",
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
    }

    public static ProjectCreateRequest createProjectCreateRequest(Long partyId) {
        return new ProjectCreateRequest(
                partyId,
                "PRJ-001",
                "테스트 프로젝트",
                "테스트 프로젝트 설명",
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
    }

    public static ProjectCreateRequest createProjectCreateRequest(String code) {
        return new ProjectCreateRequest(
                1L,
                code,
                "테스트 프로젝트",
                "테스트 프로젝트 설명",
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
    }

    public static ProjectCreateRequest createProjectCreateRequest(String code, String name, Long partyId) {
        return new ProjectCreateRequest(
                partyId,
                code,
                name,
                "테스트 프로젝트 설명",
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
    }

}
