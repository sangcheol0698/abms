package kr.co.abacus.abms.access;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GroupPermissionGrantRepository extends JpaRepository<GroupPermissionGrant, Long> {

    List<GroupPermissionGrant> findAllByPermissionGroupId(Long permissionGroupId);

    List<GroupPermissionGrant> findAllByPermissionGroupIdIn(Collection<Long> permissionGroupIds);

    @Modifying
    @Query("delete from GroupPermissionGrant g where g.permissionGroupId = :groupId")
    void deleteAllByGroupId(Long groupId);

}
