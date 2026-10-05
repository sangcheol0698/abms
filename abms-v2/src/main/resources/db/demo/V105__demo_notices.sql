-- 데모: 공지사항 (상단 고정 안내 1건, 안내 팝업 1건)
INSERT INTO `tb_notice` (`title`, `body`, `importance`, `pinned`, `popup`, `starts_at`, `ends_at`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
('ABMS 사용 안내',
 '## 자주 쓰는 기능\n\n- **⌘K (Ctrl+K)**: 화면 이동·검색·명령 실행\n- **G → P**: 프로젝트 목록으로 이동 (`?` 로 전체 단축키 보기)\n- 직원·협력사·프로젝트 상세의 **변경 이력**에서 누가 무엇을 바꿨는지 확인할 수 있습니다.\n\n문의는 경영기획본부로 해 주세요.',
 'NORMAL', TRUE, FALSE, NULL, NULL, NOW(6), NOW(6), 1, 1),
('10월 월 마감 일정 안내',
 '10월 손익 마감을 **11월 5일(목) 18시**에 진행합니다.\n\n- 10월분 **매출 발행(세금계산서)** 처리를 11월 4일까지 완료해 주세요.\n- 투입 인력의 **투입 기간 변경**은 마감 전까지 반영해야 원가에 포함됩니다.\n\n마감 후에는 손익 현황에서 마감 해제 요청이 필요합니다.',
 'IMPORTANT', FALSE, TRUE, NULL, '2026-11-06 00:00:00', NOW(6), NOW(6), 1, 1);
