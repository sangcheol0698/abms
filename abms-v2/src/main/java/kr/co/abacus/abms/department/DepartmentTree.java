package kr.co.abacus.abms.department;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * 전체 부서 목록으로 구성한 조직 트리. 하위 부서 탐색과 경로 계산에 쓴다.
 */
public final class DepartmentTree {

    private final Map<Long, Department> byId = new HashMap<>();
    private final Map<Long, List<Department>> childrenByParent = new HashMap<>();
    private final List<Department> roots = new ArrayList<>();

    public DepartmentTree(List<Department> departments) {
        departments.forEach(d -> byId.put(d.id(), d));
        Comparator<Department> order = Comparator.comparing(Department::getCode);
        for (Department d : departments.stream().sorted(order).toList()) {
            Long parentId = d.getParentId();
            if (parentId == null || !byId.containsKey(parentId)) {
                roots.add(d);
            } else {
                childrenByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(d);
            }
        }
    }

    public List<Department> roots() {
        return roots;
    }

    public List<Department> children(Long departmentId) {
        return childrenByParent.getOrDefault(departmentId, List.of());
    }

    public @Nullable Department get(@Nullable Long departmentId) {
        return departmentId == null ? null : byId.get(departmentId);
    }

    public String nameOf(@Nullable Long departmentId) {
        Department d = get(departmentId);
        return d == null ? "-" : d.getName();
    }

    /** 자기 자신을 포함한 모든 하위 부서 ID */
    public Set<Long> subtreeIds(Long departmentId) {
        Set<Long> result = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(departmentId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (result.add(current)) {
                children(current).forEach(child -> queue.add(child.id()));
            }
        }
        return result;
    }

    /** 루트부터 해당 부서까지의 경로 */
    public List<Department> path(Long departmentId) {
        List<Department> path = new ArrayList<>();
        Department current = byId.get(departmentId);
        Set<Long> visited = new LinkedHashSet<>();
        while (current != null && visited.add(current.id())) {
            path.addFirst(current);
            current = current.getParentId() == null ? null : byId.get(current.getParentId());
        }
        return path;
    }

    /** 트리를 깊이 우선으로 펼친 목록 (들여쓰기 표시용) */
    public List<Node> flatten() {
        List<Node> nodes = new ArrayList<>();
        for (Department root : roots) {
            flatten(root, 0, nodes);
        }
        return nodes;
    }

    private void flatten(Department department, int depth, List<Node> nodes) {
        nodes.add(new Node(department, depth));
        for (Department child : children(department.id())) {
            flatten(child, depth + 1, nodes);
        }
    }

    public boolean isAncestorOrSelf(Long ancestorId, Long departmentId) {
        return subtreeIds(ancestorId).contains(departmentId);
    }

    public record Node(Department department, int depth) {
    }

}
