package app.repository;

import app.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByCode(String code);

    boolean existsByCode(String code);
}