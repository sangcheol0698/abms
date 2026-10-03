package kr.co.abacus.abms.department;

import java.util.List;

import kr.co.abacus.abms.common.web.Ui.SelectOption;

/**
 * 부서 선택 박스 옵션 (트리 깊이만큼 들여쓰기).
 */
public final class DepartmentOptions {

    private DepartmentOptions() {
    }

    public static List<SelectOption> of(DepartmentTree tree) {
        return tree.flatten().stream()
                .map(node -> new SelectOption(node.department().id().toString(),
                        "　".repeat(node.depth()) + node.department().getName()))
                .toList();
    }

}
