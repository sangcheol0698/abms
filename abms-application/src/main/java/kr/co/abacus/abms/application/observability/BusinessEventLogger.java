package kr.co.abacus.abms.application.observability;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

@Component
public class BusinessEventLogger {

    private static final Logger log = LoggerFactory.getLogger("business-event");

    public void authEvent(String action, String username, String outcome, @Nullable String reason) {
        var builder = log.atInfo()
                .addKeyValue("event.category", "auth")
                .addKeyValue("event.action", action)
                .addKeyValue("auth.username", username)
                .addKeyValue("outcome", outcome);
        if (reason != null) {
            builder.addKeyValue("reason", reason);
        }
        builder.log("auth_event");
    }

    public void employeeEvent(String action, CurrentActor actor, Employee employee) {
        log.atInfo()
                .addKeyValue("event.category", "employee")
                .addKeyValue("event.action", action)
                .addKeyValue("accountId", actor.accountId())
                .addKeyValue("employeeId", employee.getIdOrThrow())
                .addKeyValue("departmentId", employee.getDepartmentId())
                .log("employee_event");
    }

    public void employeePromotion(
            CurrentActor actor,
            Employee employee,
            String fromPosition,
            String toPosition,
            String fromGrade,
            String toGrade
    ) {
        log.atInfo()
                .addKeyValue("event.category", "employee")
                .addKeyValue("event.action", "promote")
                .addKeyValue("accountId", actor.accountId())
                .addKeyValue("employeeId", employee.getIdOrThrow())
                .addKeyValue("fromPosition", fromPosition)
                .addKeyValue("toPosition", toPosition)
                .addKeyValue("fromGrade", fromGrade)
                .addKeyValue("toGrade", toGrade)
                .log("employee_promotion");
    }

    public void employeeTransfer(CurrentActor actor, Employee employee, Long fromDepartmentId, Long toDepartmentId) {
        log.atInfo()
                .addKeyValue("event.category", "employee")
                .addKeyValue("event.action", "transfer_department")
                .addKeyValue("accountId", actor.accountId())
                .addKeyValue("employeeId", employee.getIdOrThrow())
                .addKeyValue("fromDepartmentId", fromDepartmentId)
                .addKeyValue("toDepartmentId", toDepartmentId)
                .log("employee_transfer");
    }

    public void employeeTypeChange(CurrentActor actor, Employee employee, String fromType, String toType) {
        log.atInfo()
                .addKeyValue("event.category", "employee")
                .addKeyValue("event.action", "convert_employment_type")
                .addKeyValue("accountId", actor.accountId())
                .addKeyValue("employeeId", employee.getIdOrThrow())
                .addKeyValue("fromType", fromType)
                .addKeyValue("toType", toType)
                .log("employee_type_change");
    }

    public void projectEvent(String action, CurrentActor actor, Project project) {
        log.atInfo()
                .addKeyValue("event.category", "project")
                .addKeyValue("event.action", action)
                .addKeyValue("accountId", actor.accountId())
                .addKeyValue("projectId", project.getIdOrThrow())
                .addKeyValue("projectCode", project.getCode())
                .log("project_event");
    }

    public void projectAssignmentEvent(String action, @Nullable CurrentActor actor, ProjectAssignment assignment) {
        var builder = log.atInfo()
                .addKeyValue("event.category", "project_assignment")
                .addKeyValue("event.action", action)
                .addKeyValue("assignmentId", assignment.getIdOrThrow())
                .addKeyValue("projectId", assignment.getProjectId())
                .addKeyValue("employeeId", assignment.getEmployeeId());
        if (actor != null) {
            builder.addKeyValue("accountId", actor.accountId());
        }
        builder.log("project_assignment_event");
    }

    public void chatEvent(String action, Long accountId, String sessionId, String outcome, @Nullable String reason) {
        var builder = log.atInfo()
                .addKeyValue("event.category", "ai_chat")
                .addKeyValue("event.action", action)
                .addKeyValue("accountId", accountId)
                .addKeyValue("sessionId", sessionId)
                .addKeyValue("outcome", outcome);
        if (reason != null) {
            builder.addKeyValue("reason", reason);
        }
        builder.log("ai_chat_event");
    }
}
