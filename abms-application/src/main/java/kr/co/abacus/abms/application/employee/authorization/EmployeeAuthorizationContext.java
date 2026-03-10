package kr.co.abacus.abms.application.employee.authorization;

record EmployeeAuthorizationContext(
        Long accountId,
        Long employeeId,
        Long departmentId
) {

}
