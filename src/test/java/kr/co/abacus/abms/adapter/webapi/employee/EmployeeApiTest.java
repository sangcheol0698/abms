package kr.co.abacus.abms.adapter.webapi.employee;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class EmployeeApiTest extends ApiIntegrationTestBase {

    @Test
    void create() throws JsonProcessingException {
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId());
        String responseJson = objectMapper.writeValueAsString(request);

        assertThat(mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson))
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.employeeId", value -> assertThat(value).isNotNull())
            .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo(request.email()));
    }

}