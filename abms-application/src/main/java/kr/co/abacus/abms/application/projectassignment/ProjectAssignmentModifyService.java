package kr.co.abacus.abms.application.projectassignment;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentManager;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentEndRequest;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentNotFoundException;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentUpdateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProjectAssignmentModifyService implements ProjectAssignmentManager {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final CurrentActorPermissionSupport permissionSupport;

    @Override
    public ProjectAssignment create(ProjectAssignmentCreateRequest createRequest) {
        return doCreate(createRequest);
    }

    @Override
    public ProjectAssignment create(CurrentActor actor, ProjectAssignmentCreateRequest createRequest) {
        validateCanManage(actor, createRequest.projectId());
        return doCreate(createRequest);
    }

    private ProjectAssignment doCreate(ProjectAssignmentCreateRequest createRequest) {
        Project project = projectRepository.findById(createRequest.projectId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        validateNoOverlap(null, createRequest.projectId(), createRequest.employeeId(), createRequest.startDate(), createRequest.endDate());

        ProjectAssignment projectAssignment = ProjectAssignment.assign(project, createRequest);

        return projectAssignmentRepository.save(projectAssignment);
    }

    @Override
    public ProjectAssignment update(Long assignmentId, ProjectAssignmentUpdateRequest updateRequest) {
        return doUpdate(assignmentId, updateRequest);
    }

    @Override
    public ProjectAssignment update(CurrentActor actor, Long assignmentId, ProjectAssignmentUpdateRequest updateRequest) {
        validateCanManage(actor, loadAssignment(assignmentId).getProjectId());
        return doUpdate(assignmentId, updateRequest);
    }

    private ProjectAssignment doUpdate(Long assignmentId, ProjectAssignmentUpdateRequest updateRequest) {
        ProjectAssignment assignment = loadAssignment(assignmentId);
        Project project = loadProject(assignment.getProjectId());

        validateNoOverlap(
                assignmentId,
                assignment.getProjectId(),
                updateRequest.employeeId(),
                updateRequest.startDate(),
                updateRequest.endDate()
        );

        assignment.update(project, updateRequest);
        return projectAssignmentRepository.save(assignment);
    }

    @Override
    public ProjectAssignment end(Long assignmentId, ProjectAssignmentEndRequest endRequest) {
        return doEnd(assignmentId, endRequest);
    }

    @Override
    public ProjectAssignment end(CurrentActor actor, Long assignmentId, ProjectAssignmentEndRequest endRequest) {
        validateCanManage(actor, loadAssignment(assignmentId).getProjectId());
        return doEnd(assignmentId, endRequest);
    }

    private ProjectAssignment doEnd(Long assignmentId, ProjectAssignmentEndRequest endRequest) {
        ProjectAssignment assignment = loadAssignment(assignmentId);
        Project project = loadProject(assignment.getProjectId());

        validateNoOverlap(
                assignmentId,
                assignment.getProjectId(),
                assignment.getEmployeeId(),
                assignment.getPeriod().startDate(),
                endRequest.endDate()
        );

        assignment.end(project, endRequest);
        return projectAssignmentRepository.save(assignment);
    }

    private ProjectAssignment loadAssignment(Long assignmentId) {
        return projectAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ProjectAssignmentNotFoundException("존재하지 않는 프로젝트 투입 정보입니다."));
    }

    private Project loadProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
    }

    private void validateNoOverlap(
            @Nullable Long assignmentId,
            Long projectId,
            Long employeeId,
            LocalDate startDate,
            @Nullable LocalDate endDate
    ) {
        boolean overlaps = projectAssignmentRepository.findByProjectIdAndEmployeeIdAndDeletedFalse(projectId, employeeId)
                .stream()
                .filter(existing -> !existing.getIdOrThrow().equals(assignmentId))
                .anyMatch(existing -> overlaps(existing.getPeriod().startDate(), existing.getPeriod().endDate(), startDate, endDate));

        if (overlaps) {
            throw new IllegalArgumentException("동일 프로젝트에 중복되는 투입 기간이 존재합니다.");
        }
    }

    private boolean overlaps(
            LocalDate existingStartDate,
            @Nullable LocalDate existingEndDate,
            LocalDate startDate,
            @Nullable LocalDate endDate
    ) {
        LocalDate normalizedExistingEnd = existingEndDate != null ? existingEndDate : LocalDate.MAX;
        LocalDate normalizedEnd = endDate != null ? endDate : LocalDate.MAX;

        return !existingStartDate.isAfter(normalizedEnd) && !startDate.isAfter(normalizedExistingEnd);
    }

    private void validateCanManage(CurrentActor actor, Long projectId) {
        Project project = loadProject(projectId);
        permissionSupport.validateDepartmentAccess(actor, "project.write", project.getLeadDepartmentId(), "프로젝트 변경 권한 범위를 벗어났습니다.");
    }

}
