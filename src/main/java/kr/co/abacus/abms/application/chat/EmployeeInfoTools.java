package kr.co.abacus.abms.application.chat;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;

@Component
@RequiredArgsConstructor
public class EmployeeInfoTools {

    private final EmployeeRepository employeeRepository;

    @Tool(description = "구성원(직원)의 정보를 이름으로 조회하는 도구입니다.")
    public Employee getEmployeeInfo(String name) {
        return employeeRepository.findByName(name).orElse(null);
    }

}
