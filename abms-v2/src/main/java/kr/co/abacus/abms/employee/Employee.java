package kr.co.abacus.abms.employee;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.security.ScopedEmployee;

/**
 * 직원 Aggregate Root.
 * <p>
 * 삭제 후 복구가 가능해야 하므로 다른 엔티티와 달리 삭제 행을 자동으로 숨기지 않는다.
 * 조회 시 {@code deleted} 조건을 명시적으로 건다.
 */
@Entity
@Table(name = "tb_employee")
public class Employee extends BaseEntity implements Auditable, ScopedEmployee {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final String DELETED_EMAIL_PREFIX = "deleted.";
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\- ]{7,20}$");
    private static final int MAX_SKILLS_LENGTH = 500;

    @Column(nullable = false)
    private Long departmentId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private @Nullable String phone;

    @Column(nullable = false)
    private LocalDate joinDate;

    /** 경력 시작일 (이전 직장 포함). 없으면 입사일부터 경력으로 본다. */
    private @Nullable LocalDate careerStartDate;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmployeePosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private @Nullable EmployeeJob job;

    /** 보유 기술 (쉼표로 구분, 중복 제거) */
    @Column(length = 500)
    private @Nullable String skills;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private @Nullable WorkType workType;

    /** 프로필 사진 저장 경로 (없으면 기본 아바타) */
    @Column(length = 200)
    private @Nullable String photoPath;

    private @Nullable LocalDate resignationDate;

    @Column(columnDefinition = "TEXT")
    private @Nullable String memo;

    protected Employee() {
    }

    public static Employee create(EmployeeProfile profile) {
        Employee employee = new Employee();
        employee.apply(profile);
        employee.status = EmployeeStatus.ACTIVE;
        return employee;
    }

    public void update(EmployeeProfile profile) {
        requireNotResigned("퇴사한 직원은 정보를 수정할 수 없습니다.");
        apply(profile);
    }

    /** 본인 연락처·보유 기술 수정 (SELF 권한) */
    public void updateOwnContact(@Nullable String phone, @Nullable String skills) {
        requireNotResigned("퇴사한 직원은 정보를 수정할 수 없습니다.");
        this.phone = normalizePhone(phone);
        this.skills = normalizeSkills(skills);
    }

    /** 본인 정보 수정 (SELF 권한): 이름, 생년월일만 변경 가능 */
    public void updateOwnProfile(String name, LocalDate birthDate) {
        requireNotResigned("퇴사한 직원은 정보를 수정할 수 없습니다.");
        this.name = requireText(name, "이름");
        this.birthDate = Objects.requireNonNull(birthDate);
    }

    /** 프로필 사진 교체. 이전 사진 경로를 돌려준다. (파일 정리용) */
    public @Nullable String changePhoto(@Nullable String photoPath) {
        String previous = this.photoPath;
        this.photoPath = photoPath;
        return previous;
    }

    private void apply(EmployeeProfile p) {
        this.departmentId = Objects.requireNonNull(p.departmentId(), "부서는 필수입니다.");
        this.name = requireText(p.name(), "이름");
        this.email = normalizeEmail(p.email());
        this.joinDate = Objects.requireNonNull(p.joinDate(), "입사일은 필수입니다.");
        this.birthDate = Objects.requireNonNull(p.birthDate(), "생년월일은 필수입니다.");
        this.position = Objects.requireNonNull(p.position());
        this.type = Objects.requireNonNull(p.type());
        this.grade = Objects.requireNonNull(p.grade());
        this.memo = p.memo() == null || p.memo().isBlank() ? null : p.memo().trim();
        this.phone = normalizePhone(p.phone());
        if (p.careerStartDate() != null && p.careerStartDate().isAfter(this.joinDate)) {
            throw new BusinessException("경력 시작일은 입사일보다 늦을 수 없습니다.");
        }
        this.careerStartDate = p.careerStartDate();
        this.job = p.job();
        this.skills = normalizeSkills(p.skills());
        this.workType = p.workType();
    }

    private static @Nullable String normalizePhone(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String phone = value.trim();
        if (!PHONE.matcher(phone).matches()) {
            throw new BusinessException("연락처 형식이 올바르지 않습니다: " + value);
        }
        return phone;
    }

    /** "java, Spring ,java" → "java, Spring" (대소문자 무시 중복 제거, 입력 순서 유지) */
    static @Nullable String normalizeSkills(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        java.util.Map<String, String> unique = new java.util.LinkedHashMap<>();
        for (String skill : value.split("[,\\n]")) {
            String trimmed = skill.trim();
            if (!trimmed.isEmpty()) {
                unique.putIfAbsent(trimmed.toLowerCase(java.util.Locale.ROOT), trimmed);
            }
        }
        String joined = String.join(", ", unique.values());
        if (joined.length() > MAX_SKILLS_LENGTH) {
            throw new BusinessException("보유 기술은 " + MAX_SKILLS_LENGTH + "자 이하로 입력하세요.");
        }
        return joined.isEmpty() ? null : joined;
    }

    public java.util.List<String> skillList() {
        return skills == null ? java.util.List.of() : java.util.Arrays.stream(skills.split(", ")).toList();
    }

    /** 기준일까지의 총 경력 개월 수 (경력 시작일이 없으면 입사일 기준, 퇴사자는 퇴사일까지) */
    public long careerMonths(LocalDate asOf) {
        return monthsBetween(careerStartDate != null ? careerStartDate : joinDate, asOf);
    }

    /** 기준일까지의 근속 개월 수 (퇴사자는 퇴사일까지) */
    public long tenureMonths(LocalDate asOf) {
        return monthsBetween(joinDate, asOf);
    }

    private long monthsBetween(LocalDate from, LocalDate asOf) {
        LocalDate end = resignationDate != null && resignationDate.isBefore(asOf) ? resignationDate : asOf;
        return end.isBefore(from) ? 0 : java.time.temporal.ChronoUnit.MONTHS.between(from, end);
    }

    public void resign(LocalDate resignationDate) {
        if (status == EmployeeStatus.RESIGNED) {
            throw new BusinessException("이미 퇴사한 직원입니다.");
        }
        if (!resignationDate.isAfter(joinDate)) {
            throw new BusinessException("퇴사일은 입사일 이후여야 합니다.");
        }
        this.resignationDate = resignationDate;
        this.status = EmployeeStatus.RESIGNED;
    }

    public void takeLeave() {
        if (status != EmployeeStatus.ACTIVE) {
            throw new BusinessException("재직 중인 직원만 휴직 처리할 수 있습니다.");
        }
        this.status = EmployeeStatus.ON_LEAVE;
    }

    public void activate() {
        if (status == EmployeeStatus.ACTIVE) {
            throw new BusinessException("이미 재직 중인 직원입니다.");
        }
        this.status = EmployeeStatus.ACTIVE;
        this.resignationDate = null;
    }

    public void promote(EmployeePosition newPosition, @Nullable EmployeeGrade newGrade) {
        requireNotResigned("퇴사한 직원은 승진할 수 없습니다.");
        if (newPosition.level() < position.level()) {
            throw new BusinessException("현재 직급보다 낮은 직급으로 변경할 수 없습니다.");
        }
        EmployeeGrade targetGrade = newGrade != null ? newGrade : grade;
        if (position == newPosition && grade == targetGrade) {
            throw new BusinessException("동일한 직급과 등급으로는 승진할 수 없습니다.");
        }
        this.position = newPosition;
        this.grade = targetGrade;
    }

    /** 삭제 시 이메일을 마스킹해 같은 이메일로 신규 등록이 가능하도록 한다. */
    @Override
    public void softDelete(@Nullable Long deletedBy) {
        super.softDelete(deletedBy);
        this.email = DELETED_EMAIL_PREFIX + System.currentTimeMillis() + "." + email;
    }

    public void restore() {
        restoreDeleted();
        if (email.startsWith(DELETED_EMAIL_PREFIX)) {
            int secondDot = email.indexOf('.', DELETED_EMAIL_PREFIX.length());
            if (secondDot > 0 && secondDot + 1 < email.length()) {
                this.email = email.substring(secondDot + 1);
            }
        }
    }

    /** 복구 시 원래 이메일 (마스킹 해제 결과) */
    public String originalEmail() {
        if (isDeleted() && email.startsWith(DELETED_EMAIL_PREFIX)) {
            int secondDot = email.indexOf('.', DELETED_EMAIL_PREFIX.length());
            if (secondDot > 0) {
                return email.substring(secondDot + 1);
            }
        }
        return email;
    }

    public boolean isResigned() {
        return status == EmployeeStatus.RESIGNED;
    }

    private void requireNotResigned(String message) {
        if (status == EmployeeStatus.RESIGNED) {
            throw new BusinessException(message);
        }
    }

    private static String requireText(@Nullable String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private static String normalizeEmail(@Nullable String value) {
        String email = requireText(value, "이메일").toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new BusinessException("이메일 형식이 올바르지 않습니다: " + value);
        }
        return email;
    }

    public String initial() {
        return name.isEmpty() ? "?" : name.substring(0, 1);
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public EmployeePosition getPosition() {
        return position;
    }

    public EmployeeType getType() {
        return type;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public EmployeeGrade getGrade() {
        return grade;
    }

    public @Nullable String getPhotoPath() {
        return photoPath;
    }

    /**
     * 프로필 사진 주소 (없으면 null → 기본 아바타). 사진이 바뀌면 주소도 바뀌어 브라우저 캐시를 오래 둘 수 있다.
     */
    public @Nullable String photoUrl() {
        if (photoPath == null || getId() == null) {
            return null;
        }
        String file = photoPath.substring(photoPath.lastIndexOf('/') + 1);
        String version = file.contains(".") ? file.substring(0, file.indexOf('.')) : file;
        return "/employees/" + getId() + "/photo?v=" + (version.length() > 12 ? version.substring(0, 12) : version);
    }

    public @Nullable LocalDate getResignationDate() {
        return resignationDate;
    }

    public @Nullable String getMemo() {
        return memo;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    public @Nullable LocalDate getCareerStartDate() {
        return careerStartDate;
    }

    public @Nullable EmployeeJob getJob() {
        return job;
    }

    public @Nullable String getSkills() {
        return skills;
    }

    public @Nullable WorkType getWorkType() {
        return workType;
    }

    @Override
    public String auditLabel() {
        return "직원";
    }

    @Override
    public String auditName() {
        return name;
    }

}
