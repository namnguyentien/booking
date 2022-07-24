
package com.hotel.booking.controllers.admin;

import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hotel.booking.constants.JsonStructure;
import com.hotel.booking.dto.MailParamDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Customer;
import com.hotel.booking.entities.Room;
import com.hotel.booking.services.EmailService;
import com.hotel.booking.services.impl.BookingServiceImpl;
import com.hotel.booking.services.impl.CustomerServiceImpl;
import com.hotel.booking.services.impl.RoomServiceImpl;
import com.hotel.booking.utils.MailParamDtoUtil;

@Controller
@RequestMapping("admin/booking")
public class AdminBookingController {

	@Autowired
	private BookingServiceImpl bookingService;

	@Autowired
	private RoomServiceImpl roomService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CustomerServiceImpl customerService;

	@PostMapping("/create")
	public String store(Model model, @Valid @ModelAttribute("booking") Booking request, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ");
			return "admin/elements/room/create";
		}

//		Room room = roomService.findRoomById(booking.getCurrentId());
//
//		Set<RoomDetail> roomDetail = roomDetailService.findRoomByRoomId(room.getId());
//
//		room.setRoomDetails(roomDetail);
//
//		Period period = Period.between(
//				booking.getDateCheckin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
//				booking.getDateCheckout().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//		double diff = Math.abs(period.getDays());
//
//		// calculator price
//		String type = booking.getRoomType();
//		double price;
//		for (RoomDetail t : room.getRoomDetails()) {
//			if (t.getRoomType().equals(type)) {
//				price = diff * t.getPrice();
//				booking.setPrice(price);
//			}
//		}
//
//		if (booking.getId() != null) {
//			bookingService.updateBooking(booking);
//		}
//		redirectAttributes.addFlashAttribute("success", "Cập nhật booking thành công");
//
//		return "redirect:/admin/booking";
		try {
			List<Room> rooms = roomService.findAllRoom(request.getRoomType(), request.getDateCheckin(),
					request.getDateCheckout());
			if (rooms.size() == 0) {
				redirectAttributes.addFlashAttribute("error", "Không có phòng trống loại " + request.getRoomType());
				model.addAttribute("booking", request);
				return "redirect:/admin/booking";
			}

			Random random = new Random();

			int index = random.nextInt(rooms.size());

			Room r = rooms.get(index);

			Booking booking = new Booking();

			booking.setName(request.getName());
			booking.setEmail(request.getEmail());
			booking.setDateCheckin(request.getDateCheckin());
			booking.setDateCheckout(request.getDateCheckout());
			booking.setRoomType(request.getRoomType());

			Period period = Period.between(
					booking.getDateCheckin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
					booking.getDateCheckout().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			double diff = Math.abs(period.getDays());

			booking.setNumberOfPerson(request.getNumberOfPerson());

			double price = (diff + 1) * r.getPrice();
			booking.setPrice(price);

			booking.setNumberOfRoom(request.getNumberOfRoom());

			booking.setCurrentId(r.getId());
			booking.setPayment(request.getPayment());
			booking.setStatus(request.getStatus());

			// add to Customer
			Customer customer = new Customer();
			customer.setTotalPrice(booking.getPrice());
			customer.setEmail(booking.getEmail());
			customer.setName(booking.getName());
			if (customer.getPhoneNumber() == null || customer.getPhoneNumber() == "") {
				customer.setPhoneNumber("0");
			}
			Customer customerExists = customerService.findByEmail(booking.getEmail());

			if (Objects.nonNull(customerExists)) {
				customer.setId(customerExists.getId());
				bookingService.create(booking);
			} else {
				bookingService.createBooking(booking, customer);
			}

			MailParamDto mailParamDto = MailParamDtoUtil.mailParamDto(booking.getName(), booking.getEmail(),
					"http://localhost:8080", "abcd");

			emailService.sendMail(mailParamDto);

			redirectAttributes.addFlashAttribute("success", "Cập nhật booking thành công");

			return "redirect:/admin/booking";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra");
			model.addAttribute("booking", request);
			return "redirect:/admin/booking/create";
		}
	}

	@GetMapping(value = "")
	public String index(
			@RequestParam(value = "current", required = false, defaultValue = JsonStructure.Pagination.CURRENT) int current,
			@RequestParam(value = "pageSize", required = false, defaultValue = JsonStructure.Pagination.PAGE_SIZE) int pageSize,
			@RequestParam(value = "searchText", required = false, defaultValue = "") String searchText, Model model) {

		model.addAttribute("searchText", searchText);
		model.addAttribute("bookings", bookingService.listBookings(current, pageSize, searchText));

		return "admin/elements/booking/index";
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("booking", new Booking());
		return "admin/elements/booking/create";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") long id, Model model) {
		try {
			Booking booking = bookingService.findById(id);
			if (booking.getCurrentId() == null) {
				return "admin/elements/errors/404";
			}
			model.addAttribute("booking", booking);
			System.out.println(booking.getDateCheckin());

			return "admin/elements/booking/edit";
		} catch (Exception e) {
			return "admin/elements/errors/404";
		}
	}

	@PostMapping("{id}")
	public String update(@PathVariable("id") long id, @Valid @ModelAttribute("user") Booking booking,
			BindingResult result, RedirectAttributes redirectAttributes) throws Exception {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ");

			return "admin/elements/users/edit";
		}

		bookingService.update(id, booking);

		redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");

		return "redirect:/admin/booking";
	}

	@GetMapping(value = "/delete/{id}")
	public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Booking booking = bookingService.findById(id);
		if (booking == null) {
			redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra");
		}

		bookingService.deleteById(id);
		redirectAttributes.addFlashAttribute("success", "Xóa đặt phòng thành công");

		return "redirect:/admin/booking";
	}
}
