package com.hotel.booking.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Customer;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.services.BookingService;
import com.hotel.booking.services.CustomerService;
import com.hotel.booking.utils.pagination.Paged;
import com.hotel.booking.utils.pagination.Paging;

@Service
public class BookingServiceImpl implements BookingService {

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	protected CustomerService customerService;

	@Override
	public Paged<Booking> listBookings(int current, int pageSize, String searchText) {
		PageRequest request = PageRequest.of(current - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
		Page<Booking> booking = bookingRepository.getBooking(searchText, request);
		return new Paged<>(booking, Paging.of(booking.getTotalPages(), current, pageSize));
	}

	@Override
	public Booking create(Booking booking) {
		return bookingRepository.save(booking);
	}

	@Override
	public Booking findById(Long id) {
		Booking booking = bookingRepository.getById(id);
		return booking;
	}

	@Override
	public List<Booking> findByEmail(String email) {
		return bookingRepository.findBookingByEmail(email);
	}

	@Override
	public int update(Long id, Booking request) {
		return bookingRepository.update(request.getName(), request.getEmail(), request.getDateCheckin(),
				request.getDateCheckout(), request.getNumberOfPerson(), request.getNumberOfRoom(),
				request.getRoomType(), request.getCurrentId(), request.isDeleted(), request.getPrice(),
				request.getPayment(), request.getStatus(), request.getId());
	}

	@Override
	public Booking updateBooking(Booking book) {
		return bookingRepository.save(book);
	}

	@Override
	public void deleteById(Long id) {
		bookingRepository.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = { Exception.class, Throwable.class })
	public void createBooking(Booking booking, Customer customer) throws Exception {
		try {
			create(booking);
			customerService.create(customer);
		} catch (Exception e) {
			throw new Exception("test");
		}
	}

}
