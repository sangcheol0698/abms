package kr.co.abacus.abms.application.department;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DepartmentQueryService implements DepartmentFinder {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department find(UUID id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다: " + id));
    }

}
