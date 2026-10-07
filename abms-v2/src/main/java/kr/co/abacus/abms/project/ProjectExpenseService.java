package kr.co.abacus.abms.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.project.ProjectExpense.ExpenseInfo;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.ClosedMonthGuard;

/**
 * 프로젝트 직접비 관리. 직접비는 등록 즉시 귀속일이 속한 월의 비용이 되므로, 마감된 월에는 등록·수정·삭제할 수 없다.
 */
@Service
@Transactional
public class ProjectExpenseService {

    private final ProjectService projectService;
    private final ProjectExpenseRepository expenseRepository;
    private final ClosedMonthGuard closedMonthGuard;

    public ProjectExpenseService(ProjectService projectService, ProjectExpenseRepository expenseRepository,
                                 ClosedMonthGuard closedMonthGuard) {
        this.projectService = projectService;
        this.expenseRepository = expenseRepository;
        this.closedMonthGuard = closedMonthGuard;
    }

    @Transactional(readOnly = true)
    public List<ProjectExpense> expenses(Long projectId) {
        return expenseRepository.findAllByProjectIdOrderByExpenseDateDescIdDesc(projectId);
    }

    @Transactional(readOnly = true)
    public ProjectExpense get(Long projectId, Long expenseId) {
        return expenseRepository.findById(expenseId)
                .filter(expense -> expense.getProjectId().equals(projectId))
                .orElseThrow(() -> NotFoundException.of("직접비", expenseId));
    }

    public ProjectExpense add(LoginUser user, Long projectId, ExpenseInfo info) {
        Project project = projectService.getForWrite(user, projectId);
        closedMonthGuard.checkOpen(info.expenseDate(), "직접비 등록");
        return expenseRepository.save(ProjectExpense.create(project, info));
    }

    public void update(LoginUser user, Long projectId, Long expenseId, ExpenseInfo info) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectExpense expense = get(projectId, expenseId);
        // 금액·귀속일이 바뀌면 이전·이후 월 모두의 비용이 달라진다. (분류·내용·메모만 바꾸는 것은 허용)
        if (!expense.getExpenseDate().equals(info.expenseDate()) || !expense.getAmount().equals(info.amount())) {
            closedMonthGuard.checkOpen(expense.getExpenseDate(), "직접비 수정");
            closedMonthGuard.checkOpen(info.expenseDate(), "직접비 수정");
        }
        expense.update(project, info);
    }

    public void delete(LoginUser user, Long projectId, Long expenseId) {
        projectService.getForWrite(user, projectId);
        ProjectExpense expense = get(projectId, expenseId);
        closedMonthGuard.checkOpen(expense.getExpenseDate(), "직접비 삭제");
        expense.softDelete(user.accountId());
    }

}
