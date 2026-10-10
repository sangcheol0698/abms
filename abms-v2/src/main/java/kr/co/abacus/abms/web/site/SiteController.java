package kr.co.abacus.abms.web.site;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.audit.AuditQueryService;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.geo.Geocoder;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.MapMarker;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.site.Site.SiteInfo;
import kr.co.abacus.abms.site.Site;
import kr.co.abacus.abms.site.SiteService;
import kr.co.abacus.abms.site.SiteType;

/**
 * 사업장(본사·지사) 화면. 조회는 로그인 사용자 모두, 등록·수정은 부서 관리 권한.
 */
@Controller
@RequestMapping("/sites")
public class SiteController {

    private static final int NEARBY_LIMIT = 5;

    private final SiteService siteService;
    private final DepartmentRepository departmentRepository;
    private final EmployeeService employeeService;
    private final PartyService partyService;
    private final Geocoder geocoder;
    private final AuditQueryService auditQueryService;

    public SiteController(SiteService siteService, DepartmentRepository departmentRepository, EmployeeService employeeService,
                          PartyService partyService, Geocoder geocoder,
                          AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
        this.geocoder = geocoder;
        this.siteService = siteService;
        this.departmentRepository = departmentRepository;
        this.employeeService = employeeService;
        this.partyService = partyService;
    }

    @GetMapping
    public String list(Model model) {
        List<Site> sites = siteService.all();
        // 사업장 → 부서 → 인원을 각각 한 번씩만 조회한다.
        Map<Long, List<Department>> departments = departmentRepository.findAllBySiteIdIsNotNull().stream()
                .collect(Collectors.groupingBy(Department::getSiteId));
        Map<Long, List<kr.co.abacus.abms.employee.Employee>> members = employeeService.membersByDepartment(
                departments.values().stream().flatMap(List::stream).map(Department::id).toList());
        model.addAttribute("sites", sites);
        model.addAttribute("departmentCounts", departments.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));
        model.addAttribute("headcounts", departments.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToInt(d -> members.get(d.id()).size()).sum())));
        model.addAttribute("markers", sites.stream()
                .map(s -> MapMarker.of(s.getName(), s.getLocation().fullAddress(), s.getLocation(), "/sites/" + s.id()))
                .filter(Objects::nonNull)
                .toList());
        return "site/list";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        Site site = siteService.get(id);
        List<Department> departments = departmentRepository.findAllBySiteId(id).stream()
                .sorted(Comparator.comparing(Department::getName))
                .toList();
        Map<Long, Integer> memberCounts = employeeService.membersByDepartment(departments.stream().map(Department::id).toList())
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
        model.addAttribute("site", site);
        model.addAttribute("departments", departments);
        model.addAttribute("memberCounts", memberCounts);
        model.addAttribute("headcount", memberCounts.values().stream().mapToInt(Integer::intValue).sum());
        model.addAttribute("nearby", user.has(PermissionCode.PARTY_READ) ? nearby(site.getLocation()) : List.of());
        MapMarker marker = MapMarker.of(site.getName(), site.getLocation().fullAddress(), site.getLocation(), null);
        model.addAttribute("markers", marker == null ? List.of() : List.of(marker));
        model.addAttribute("auditHistory", auditQueryService.history("Site", id, 30));
        return "site/detail";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal LoginUser user, Model model) {
        requireWrite(user);
        return form(model, SiteForm.empty(), FormErrors.none(), null);
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @Valid @ModelAttribute("form") SiteForm form, BindingResult binding,
                         Model model, HttpServletResponse response, RedirectAttributes redirect) {
        requireWrite(user);
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), null);
        }
        try {
            Site site = siteService.create(user, geocoded(form.toInfo()));
            Toast.success(redirect, site.getName() + " 사업장을 등록했습니다.");
            return "redirect:/sites/" + site.id();
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), null);
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        requireWrite(user);
        Site site = siteService.get(id);
        return form(model, SiteForm.of(site), FormErrors.none(), site);
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @Valid @ModelAttribute("form") SiteForm form,
                         BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        requireWrite(user);
        Site site = siteService.get(id);
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), site);
        }
        try {
            siteService.update(user, id, geocoded(form.toInfo()));
            Toast.success(redirect, "사업장 정보를 수정했습니다.");
            return "redirect:/sites/" + id;
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), site);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        Site site = siteService.get(id);
        try {
            siteService.delete(user, id);
            Toast.success(redirect, site.getName() + " 사업장을 삭제했습니다.");
            return "redirect:/sites";
        } catch (BusinessException e) {
            Toast.error(redirect, e.getMessage());
            return "redirect:/sites/" + id;
        }
    }

    /** 사업장에서 가까운 협력사 (좌표가 있는 곳만, 가까운 순) */
    private List<NearbyParty> nearby(Location location) {
        if (!location.hasCoordinates()) {
            return List.of();
        }
        return partyService.all().stream()
                .map(p -> new NearbyParty(p, location.distanceKm(p.getLocation())))
                .filter(n -> n.distanceKm() != null)
                .sorted(Comparator.comparingDouble(NearbyParty::distanceKm))
                .limit(NEARBY_LIMIT)
                .toList();
    }

    /** 좌표 변환은 외부 API 호출이라 DB 트랜잭션 밖(여기)에서 한다. */
    private SiteInfo geocoded(SiteInfo info) {
        return info.withLocation(geocoder.complete(info.location()));
    }

    private String form(Model model, SiteForm form, FormErrors errors, @Nullable Site site) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("site", site);
        return "site/form";
    }

    private static void requireWrite(LoginUser user) {
        if (!user.has(PermissionCode.DEPARTMENT_WRITE)) {
            throw new org.springframework.security.access.AccessDeniedException("사업장 관리 권한이 없습니다.");
        }
    }

    public record NearbyParty(Party party, Double distanceKm) {

        public String distanceLabel() {
            return distanceKm < 1 ? Math.round(distanceKm * 1000) + "m" : String.format("%.1fkm", distanceKm);
        }

    }

    public record SiteForm(
            @NotBlank(message = "사업장명을 입력하세요.") @Size(max = 50, message = "50자 이하로 입력하세요.") @Nullable String name,
            @Nullable SiteType siteType,
            @Size(max = 20, message = "20자 이하로 입력하세요.") @Nullable String phone,
            @Size(max = 7, message = "7자 이하로 입력하세요.") @Nullable String zipCode,
            @Size(max = 255, message = "255자 이하로 입력하세요.") @Nullable String address,
            @Size(max = 100, message = "100자 이하로 입력하세요.") @Nullable String addressDetail,
            @DecimalMin("-90") @DecimalMax("90") @Nullable BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") @Nullable BigDecimal longitude,
            @Size(max = 2000, message = "메모는 2000자 이하로 입력하세요.") @Nullable String memo
    ) {

        static SiteForm empty() {
            return new SiteForm(null, SiteType.OFFICE, null, null, null, null, null, null, null);
        }

        static SiteForm of(Site s) {
            Location l = s.getLocation();
            return new SiteForm(s.getName(), s.getSiteType(), s.getPhone(), l.zipCode(), l.address(), l.addressDetail(),
                    l.latitude(), l.longitude(), s.getMemo());
        }

        SiteInfo toInfo() {
            return new SiteInfo(name, siteType, phone, Location.of(zipCode, address, addressDetail, latitude, longitude), memo);
        }

    }

}
