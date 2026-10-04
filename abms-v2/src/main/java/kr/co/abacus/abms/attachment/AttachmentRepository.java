package kr.co.abacus.abms.attachment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByOwnerTypeAndOwnerIdOrderByIdDesc(AttachmentOwner ownerType, Long ownerId);

}
