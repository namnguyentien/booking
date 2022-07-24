package com.hotel.booking.repositories;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entities.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
	@Query("SELECT r FROM Room r WHERE r.name LIKE %?1%")
	Page<Room> listRooms(String searchText, Pageable pageable);

	@Modifying
	@Query("update Room r set r.deleted=true where r.id = ?1")
	void deleteById(Long id);

	@Query(value = "SELECT * FROM rooms r WHERE r.id=?1", nativeQuery = true)
	Room findRoomById(Long id);

	@Modifying
	@Query(value = "select * from rooms where id != ?1 ORDER BY RAND() limit 2 ", nativeQuery = true)
	List<Room> findRoomLimit(Long offset);

	@Modifying
	@Query("select r from Room r inner join  RoomDetail d on r.id = d.id where r.deleted=0 and d.roomType= :roomType ")
	List<Room> findAll(String roomType);

	@Modifying
	@Transactional
	@Query("update Room u " + "set u.deleted=?1 " + "where u.id = ?2")
	int update(boolean deleted, Long id);

	@Modifying
	@Transactional
	@Query("select r from Room r where r.roomType= :roomType and r.id in(select r1.id from Room r1 left join Booking b on r1.id = b.currentId where (b.dateCheckin > :dateCheckOut or b.dateCheckout < :dateCheckIn) and r1.deleted = '0' and b.deleted = '0')")
	List<Room> findAllRoom(String roomType, Date dateCheckIn, Date dateCheckOut);

	@Modifying
	@Query("select r from Room r where r.id= :roomId and r.id in(select r1.id from Room r1 left join Booking b on r1.id = b.currentId where (b.dateCheckin > :dateCheckOut or b.dateCheckout < :dateCheckIn) and r1.deleted = '0' and b.deleted = '0')")
	List<Room> findRoomByRoomId(Long roomId, Date dateCheckIn, Date dateCheckOut);
}
