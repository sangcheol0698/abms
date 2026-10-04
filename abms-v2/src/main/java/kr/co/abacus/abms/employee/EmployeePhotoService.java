package kr.co.abacus.abms.employee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import kr.co.abacus.abms.attachment.FileStorage;
import kr.co.abacus.abms.common.audit.AuditEventListener;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로필 사진 등록·삭제. 본인 또는 해당 직원의 정보를 수정할 수 있는 사용자만 바꿀 수 있다.
 * 사진 파일은 커밋 후 이전 파일을 지우고, 롤백되면 새로 저장한 파일을 지운다.
 */
@Service
@Transactional
public class EmployeePhotoService {

    private static final Logger log = LoggerFactory.getLogger(EmployeePhotoService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final FileStorage fileStorage;
    private final AuditEventListener auditLog;

    public EmployeePhotoService(EmployeeRepository employeeRepository, EmployeeService employeeService, FileStorage fileStorage,
                                AuditEventListener auditLog) {
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
        this.fileStorage = fileStorage;
        this.auditLog = auditLog;
    }

    @Transactional(readOnly = true)
    public boolean canChange(LoginUser user, Employee employee) {
        return employee.id().equals(user.employeeId()) || employeeService.canFullWrite(user, employee.getDepartmentId());
    }

    /** @param jpeg {@link ProfileImages#normalize} 로 만든 사진 */
    public Employee change(LoginUser user, Long employeeId, byte[] jpeg) {
        Employee employee = getForChange(user, employeeId);
        String path;
        try {
            path = fileStorage.store(new ByteArrayInputStream(jpeg), "jpg");
        } catch (IOException e) {
            throw new UncheckedIOException("사진을 저장하지 못했습니다.", e);
        }
        afterCompletion(path, false);
        String previous = employee.changePhoto(path);
        afterCompletion(previous, true);
        auditLog.record(employee, employeeId, "photo", previous == null ? null : "이전 사진", "새 사진");
        return employee;
    }

    public Employee remove(LoginUser user, Long employeeId) {
        Employee employee = getForChange(user, employeeId);
        String previous = employee.changePhoto(null);
        afterCompletion(previous, true);
        auditLog.record(employee, employeeId, "photo", previous == null ? null : "사진", null);
        return employee;
    }

    @Transactional(readOnly = true)
    public InputStream open(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> NotFoundException.of("직원", employeeId));
        if (employee.getPhotoPath() == null) {
            throw new NotFoundException("등록된 프로필 사진이 없습니다.");
        }
        try {
            return fileStorage.open(employee.getPhotoPath());
        } catch (IOException e) {
            throw new NotFoundException("프로필 사진 파일을 찾을 수 없습니다.");
        }
    }

    private Employee getForChange(LoginUser user, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> NotFoundException.of("직원", employeeId));
        if (!canChange(user, employee)) {
            throw new AccessDeniedException("이 직원의 프로필 사진을 바꿀 권한이 없습니다.");
        }
        return employee;
    }

    /**
     * @param onCommit true: 커밋되면 지운다 (교체·삭제된 이전 사진), false: 롤백되면 지운다 (새로 저장한 사진)
     */
    private void afterCompletion(@Nullable String path, boolean onCommit) {
        if (path == null || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if ((status == STATUS_COMMITTED) == onCommit) {
                    try {
                        fileStorage.delete(path);
                    } catch (IOException e) {
                        log.warn("프로필 사진 파일 정리 실패: {}", path, e);
                    }
                }
            }
        });
    }

}
