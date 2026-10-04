package kr.co.abacus.abms.project;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.site.Site;
import kr.co.abacus.abms.site.SiteRepository;

/**
 * 프로젝트의 실제 수행 위치를 정한다.
 * - 별도 장소: 입력한 주소
 * - 고객사 상주: 입력한 주소, 없으면 협력사 주소
 * - 자사: 주관 부서(없으면 상위 부서)의 사업장 주소
 * - 원격·미정: 위치 없음
 */
@Service
@Transactional(readOnly = true)
public class ProjectPlaceService {

    private final PartyRepository partyRepository;
    private final SiteRepository siteRepository;

    public ProjectPlaceService(PartyRepository partyRepository, SiteRepository siteRepository) {
        this.partyRepository = partyRepository;
        this.siteRepository = siteRepository;
    }

    public ResolvedPlace resolve(Project project, DepartmentTree tree) {
        WorkPlace workPlace = project.getWorkPlace();
        if (workPlace == null || workPlace == WorkPlace.REMOTE) {
            return new ResolvedPlace(workPlace, Location.EMPTY, null, homeSite(project, tree));
        }
        Site home = homeSite(project, tree);
        return switch (workPlace) {
            case OTHER -> new ResolvedPlace(workPlace, project.getWorkLocation(), "입력한 주소", home);
            case CLIENT_SITE -> {
                if (!project.getWorkLocation().isEmpty()) {
                    yield new ResolvedPlace(workPlace, project.getWorkLocation(), "입력한 주소", home);
                }
                Location partyLocation = partyRepository.findById(project.getPartyId()).map(Party::getLocation).orElse(Location.EMPTY);
                yield new ResolvedPlace(workPlace, partyLocation, "협력사 주소", home);
            }
            case OFFICE -> new ResolvedPlace(workPlace, home == null ? Location.EMPTY : home.getLocation(),
                    home == null ? null : "주관 부서 사업장(" + home.getName() + ")", home);
            case REMOTE -> throw new IllegalStateException();
        };
    }

    /** 주관 부서의 근무 사업장 */
    private @Nullable Site homeSite(Project project, DepartmentTree tree) {
        Long siteId = tree.siteIdOf(project.getLeadDepartmentId());
        return siteId == null ? null : siteRepository.findById(siteId).orElse(null);
    }

    /**
     * 직원의 현재 근무지: 오늘 투입 중인 프로젝트 중 수행 위치가 있는 곳(가장 최근 시작), 없으면 소속 부서의 사업장.
     */
    public @Nullable Workplace workplaceOf(Long departmentId, List<ProjectAssignment> assignments, java.util.Map<Long, Project> projects,
                                           DepartmentTree tree, java.time.LocalDate today) {
        Optional<Workplace> onProject = assignments.stream()
                .filter(a -> a.isActiveOn(today) && projects.containsKey(a.getProjectId()))
                .sorted(Comparator.comparing((ProjectAssignment a) -> a.getPeriod().startDate()).reversed())
                .map(a -> {
                    Project project = projects.get(a.getProjectId());
                    ResolvedPlace place = resolve(project, tree);
                    return place.location().isEmpty() ? null
                            : new Workplace(project.getName(), place.label(), place.location(), "/projects/" + project.id());
                })
                .filter(java.util.Objects::nonNull)
                .findFirst();
        if (onProject.isPresent()) {
            return onProject.get();
        }
        Long siteId = tree.siteIdOf(departmentId);
        return siteId == null ? null : siteRepository.findById(siteId)
                .map(site -> new Workplace(site.getName(), site.getSiteType().label(), site.getLocation(), "/sites/" + site.id()))
                .orElse(null);
    }

    /** 위치에서 가장 가까운 사업장 (좌표가 있는 사업장만) */
    public Optional<NearestSite> nearestSite(Location location) {
        if (!location.hasCoordinates()) {
            return Optional.empty();
        }
        List<Site> sites = siteRepository.findAll();
        return sites.stream()
                .filter(s -> s.getLocation().hasCoordinates())
                .map(s -> new NearestSite(s, location.distanceKm(s.getLocation())))
                .min(Comparator.comparingDouble(NearestSite::distanceKm));
    }

    /**
     * @param location 실제 수행 위치 (없으면 빈 위치)
     * @param source   위치 출처 설명 (입력한 주소 / 협력사 주소 / 주관 부서 사업장(…))
     * @param homeSite 주관 부서의 근무 사업장
     */
    public record ResolvedPlace(@Nullable WorkPlace workPlace, Location location, @Nullable String source, @Nullable Site homeSite) {

        public String label() {
            return workPlace == null ? "미정" : workPlace.label();
        }

    }

    /** 근무지 (이름, 구분 설명, 위치, 링크) */
    public record Workplace(String name, String kind, Location location, String href) {
    }

    public record NearestSite(Site site, double distanceKm) {

        public String distanceLabel() {
            return distanceKm < 1 ? Math.round(distanceKm * 1000) + "m" : String.format("%.1fkm", distanceKm);
        }

    }

}
