package kr.co.abacus.abms.adapter.webapi.employee;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.support.IntegrationTestBase;

@AutoConfigureMockMvc
class EmployeeApiTest extends IntegrationTestBase {

    @Autowired
    private MockMvcTester mvcTester;

    @Autowired
    private ObjectMapper objectMapper;

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