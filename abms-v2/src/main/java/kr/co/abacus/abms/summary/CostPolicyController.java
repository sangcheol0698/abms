package kr.co.abacus.abms.summary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.employee.EmployeeType;

/**
 * 원가 정책 관리 (summary.manage). 비율은 화면에서 % 로 입력받는다.
 */
@Controller
@RequestMapping("/admin/cost-policies")
public class CostPolicyController {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CostPolicyService costPolicyService;

    public CostPolicyController(CostPolicyService costPolicyService) {
        this.costPolicyService = costPolicyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("policies", costPolicyService.all());
        model.addAttribute("currentYear", Year.now().getValue());
        return "admin/costPolicies";
    }

    @PostMapping
    public String create(@RequestParam(required = false) @Nullable Integer year, @RequestParam(required = false) @Nullable EmployeeType type,
                         @RequestParam(required = false) @Nullable BigDecimal overheadPercent,
                         @RequestParam(required = false) @Nullable BigDecimal sgaPercent, RedirectAttributes redirect) {
        if (year == null || type == null || overheadPercent == null || sgaPercent == null) {
            throw new BusinessException("연도, 고용유형, 제경비율, 판관비율을 모두 입력하세요.");
        }
        costPolicyService.create(year, type, toRate(overheadPercent), toRate(sgaPercent));
        Toast.success(redirect, year + "년 " + type.label() + " 원가 정책을 추가했습니다.");
        return "redirect:/admin/cost-policies";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam BigDecimal overheadPercent, @RequestParam BigDecimal sgaPercent,
                         RedirectAttributes redirect) {
        costPolicyService.update(id, toRate(overheadPercent), toRate(sgaPercent));
        Toast.success(redirect, "원가 정책을 수정했습니다. 손익은 재집계 시 반영됩니다.");
        return "redirect:/admin/cost-policies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        costPolicyService.delete(id);
        Toast.success(redirect, "원가 정책을 삭제했습니다.");
        return "redirect:/admin/cost-policies";
    }

    private static BigDecimal toRate(BigDecimal percent) {
        return percent.divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

}
