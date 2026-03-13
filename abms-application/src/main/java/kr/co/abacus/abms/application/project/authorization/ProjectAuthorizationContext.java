package kr.co.abacus.abms.application.project.authorization;

record ProjectAuthorizationContext(
        Long accountId,
        Long employeeId,
        Long departmentId
) {
}
