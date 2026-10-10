package kr.co.abacus.abms.web.party;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.attachment.AttachmentOwner;
import kr.co.abacus.abms.attachment.AttachmentSection;
import kr.co.abacus.abms.attachment.AttachmentService;
import kr.co.abacus.abms.common.audit.AuditQueryService;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.geo.Geocoder;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.MapMarker;
import kr.co.abacus.abms.common.web.PageView;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.party.Party.PartyInfo;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.party.PartyType;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;

@Controller
@RequestMapping("/parties")
public class PartyController {

    private final PartyService partyService;
    private final ProjectService projectService;
    private final DepartmentService departmentService;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final Geocoder geocoder;
    private final AttachmentService attachmentService;
    private final AuditQueryService auditQueryService;

    public PartyController(PartyService partyService, ProjectService projectService, DepartmentService departmentService,
                           ProjectRevenuePlanRepository revenuePlanRepository, Geocoder geocoder,
                           AuditQueryService auditQueryService,
                           AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
        this.auditQueryService = auditQueryService;
        this.geocoder = geocoder;
        this.revenuePlanRepository = revenuePlanRepository;
        this.partyService = partyService;
        this.projectService = projectService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal LoginUser user, @RequestParam(required = false) @Nullable String q,
                       @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model) {
        require(user, PermissionCode.PARTY_READ);
        var result = partyService.search(q, PageRequest.of(Math.max(page, 0), 20, Sort.by("name")));
        String baseUrl = UriComponentsBuilder.fromPath("/parties").query(request.getQueryString()).build().toUriString();
        model.addAttribute("page", PageView.of(result, baseUrl));
        model.addAttribute("q", q);
        java.util.List<Long> partyIds = result.getContent().stream().map(Party::id).toList();
        model.addAttribute("projectCounts", projectService.partyProjectCounts(partyIds));
        model.addAttribute("primaryContacts", partyService.primaryContacts(partyIds));
        if (Htmx.targets(request, "party-results")) {
            return "party/results";
        }
        return "party/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal LoginUser user, Model model) {
        require(user, PermissionCode.PARTY_WRITE);
        return form(model, PartyForm.empty(), FormErrors.none(), null);
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @Valid @ModelAttribute("form") PartyForm form, BindingResult binding,
                         Model model, HttpServletResponse response, RedirectAttributes redirect) {
        require(user, PermissionCode.PARTY_WRITE);
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), null);
        }
        try {
            Party party = partyService.create(geocoded(form.toInfo()));
            Toast.success(redirect, party.getName() + " 협력사를 등록했습니다.");
            return "redirect:/parties/" + party.id();
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), null);
        }
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        require(user, PermissionCode.PARTY_READ);
        Party party = partyService.get(id);
        model.addAttribute("party", party);
        model.addAttribute("contacts", partyService.contacts(id));
        java.util.List<Project> projects = user.has(PermissionCode.PROJECT_READ)
                ? projectService.byParty(id).stream().filter(p -> projectService.canRead(user, p)).toList()
                : java.util.List.<Project>of();
        model.addAttribute("projects", projects);
        model.addAttribute("insight", PartyInsight.of(projects,
                projects.isEmpty() ? java.util.List.of() : revenuePlanRepository.findAllByProjectIdIn(projects.stream().map(Project::id).toList()),
                java.time.LocalDate.now()));
        model.addAttribute("projectCount", projectService.partyProjectCount(id));
        model.addAttribute("tree", departmentService.tree());
        MapMarker marker = MapMarker.of(party.getName(), party.getLocation().fullAddress(), party.getLocation(), null);
        model.addAttribute("markers", marker == null ? java.util.List.of() : java.util.List.of(marker));
        model.addAttribute("auditHistory", auditQueryService.history("Party", id, 30));
        model.addAttribute("attachments", AttachmentSection.of(attachmentService, user, AttachmentOwner.PARTY, id));
        return "party/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        require(user, PermissionCode.PARTY_WRITE);
        Party party = partyService.get(id);
        return form(model, PartyForm.of(party), FormErrors.none(), party);
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @Valid @ModelAttribute("form") PartyForm form,
                         BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        require(user, PermissionCode.PARTY_WRITE);
        Party party = partyService.get(id);
        if (binding.hasErrors()) {
            response.setStatus(422);
            return form(model, form, FormErrors.of(binding), party);
        }
        try {
            partyService.update(id, geocoded(form.toInfo()));
            Toast.success(redirect, "협력사 정보를 수정했습니다.");
            return "redirect:/parties/" + id;
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), party);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        require(user, PermissionCode.PARTY_WRITE);
        partyService.delete(id, user.accountId());
        Toast.success(redirect, "협력사를 삭제했습니다.");
        return "redirect:/parties";
    }

    /** 좌표 변환은 외부 API 호출이라 DB 트랜잭션 밖(여기)에서 한다. */
    private PartyInfo geocoded(PartyInfo info) {
        return info.withLocation(geocoder.complete(info.location()));
    }

    private String form(Model model, PartyForm form, FormErrors errors, @Nullable Party party) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("party", party);
        return "party/form";
    }

    private static void require(LoginUser user, PermissionCode code) {
        if (!user.has(code)) {
            throw new AccessDeniedException("협력사 " + (code == PermissionCode.PARTY_READ ? "조회" : "관리") + " 권한이 없습니다.");
        }
    }

    public record PartyForm(
            @NotBlank(message = "협력사명을 입력하세요.") @Size(max = 50, message = "50자 이하로 입력하세요.") @Nullable String name,
            @Size(max = 30, message = "30자 이하로 입력하세요.") @Nullable String ceoName,
            @Nullable PartyType partyType,
            @Pattern(regexp = "^$|^[0-9]{3}-?[0-9]{2}-?[0-9]{5}$", message = "사업자등록번호는 000-00-00000 형식으로 입력하세요.") @Nullable String businessNumber,
            @Size(max = 50, message = "50자 이하로 입력하세요.") @Nullable String industry,
            @Size(max = 20, message = "20자 이하로 입력하세요.") @Nullable String phone,
            @Size(max = 7, message = "7자 이하로 입력하세요.") @Nullable String zipCode,
            @Size(max = 255, message = "255자 이하로 입력하세요.") @Nullable String address,
            @Size(max = 100, message = "100자 이하로 입력하세요.") @Nullable String addressDetail,
            @DecimalMin("-90") @DecimalMax("90") @Nullable BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") @Nullable BigDecimal longitude,
            @Size(max = 255, message = "255자 이하로 입력하세요.") @Nullable String website,
            @Size(max = 2000, message = "메모는 2000자 이하로 입력하세요.") @Nullable String memo
    ) {

        static PartyForm empty() {
            return new PartyForm(null, null, PartyType.CLIENT, null, null, null, null, null, null, null, null, null, null);
        }

        static PartyForm of(Party p) {
            Location l = p.getLocation();
            return new PartyForm(p.getName(), p.getCeoName(), p.getPartyType(), p.getBusinessNumber(), p.getIndustry(), p.getPhone(),
                    l.zipCode(), l.address(), l.addressDetail(), l.latitude(), l.longitude(), p.getWebsite(), p.getMemo());
        }

        PartyInfo toInfo() {
            return new PartyInfo(name == null ? "" : name, ceoName, partyType, businessNumber, industry, phone, Location.of(zipCode, address, addressDetail, latitude, longitude), website, memo);
        }

    }

}
