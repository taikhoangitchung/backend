//package quiz.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import quiz.entity.Authority;
//
//import java.util.List;
//
//public interface AuthorityRepository extends JpaRepository<Authority, Long> {
//    boolean existsByRole(Authority.Role role);
//
//    Authority findByRole(Authority.Role role);
//
//    List<Authority> findAllByRoleNot(Authority.Role role);
//}
