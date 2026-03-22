package kr.co.abacus.abms.application.auth;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Component
public class DepartmentTreeResolver {

    private final DepartmentRepository departmentRepository;

    public Set<Long> resolve(Long rootDepartmentId) {
        Map<Long, List<Long>> childrenByParentId = departmentRepository.findAllByDeletedFalse().stream()
                .filter(department -> department.getParent() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        department -> {
                            var parent = department.getParent();
                            if (parent == null || parent.getId() == null) {
                                throw new IllegalStateException("상위 부서가 없습니다.");
                            }
                            return parent.getId();
                        },
                        LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(Department::getIdOrThrow, java.util.stream.Collectors.toList())
                ));

        Set<Long> departmentIds = new LinkedHashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(rootDepartmentId);

        while (!queue.isEmpty()) {
            Long departmentId = queue.poll();
            if (!departmentIds.add(departmentId)) {
                continue;
            }
            queue.addAll(childrenByParentId.getOrDefault(departmentId, List.of()));
        }

        return departmentIds;
    }
}
