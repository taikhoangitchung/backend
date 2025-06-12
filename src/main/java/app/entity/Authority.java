//package quiz.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//
//@Entity
//@Setter
//@Getter
//@NoArgsConstructor
//public class Authority  {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonIgnore
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, unique = true)
//    private Role role;
//
//    public Authority(Role role) {
//        this.role = role;
//    }
//
//    public enum Role {
//        ADMIN,
//        MANAGER,
//        LEADER,
//        MEMBER
//    }
//}
