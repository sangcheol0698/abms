package kr.co.abacus.abms.application.department.required;

import java.util.Map;
import java.util.UUID;

public interface CustomDepartmentRepository {

    Map<UUID, Long> getDepartmentHeadcounts();
}
