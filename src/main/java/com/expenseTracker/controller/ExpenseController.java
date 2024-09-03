package com.expenseTracker.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expenseTracker.model.Expense;
import com.expenseTracker.service.ExpenseService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

	@Autowired
	private ExpenseService expenseService;

	@PostMapping("/addExpense")
	public Expense createExpense(@RequestParam Long userId, @RequestParam Long categoryId,
			@RequestParam BigDecimal amount, @RequestParam String date, @RequestParam String description) {

		LocalDate expenseDate = LocalDate.parse(date);
		return expenseService.createExpense(userId, categoryId, amount, expenseDate, description);
	}

	@GetMapping("/getExpenseByUserId")
	public List<Expense> getExpenseByuserId(@RequestParam Long userId) {
		System.out.println("Received request for userId: " + userId);
		return expenseService.getExpenseByuserId(userId);
	}

	@GetMapping("/getExpenseByUserIdAndCategoryId")
	public List<Expense> getExpenseByuserIdAndCategoryId(@RequestParam Long userId, @RequestParam Long categoryId) {
		return expenseService.getExpenseByuserIdAndCategoryId(userId, categoryId);
	}

	@GetMapping("/getExpenseByUserIdAndexpenseDate")
	public List<Expense> getExpensesByuserIdAndexpenseDateBetween(@RequestParam Long userId,
			@RequestParam String startDate, @RequestParam String endDate) {
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();
		return expenseService.getExpensesByuserIdAndexpenseDateBetween(userId, start, end);
	}

	@GetMapping("/getExpenseSummaryByCategory")
	public Map<String, BigDecimal> getExpenseSummaryByCategory(@RequestParam String startDate,
			@RequestParam String endDate) {
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();
		return expenseService.getExpenseSummaryByCategory(start, end);
	}

	@GetMapping("/getExpenseSummaryByDateRange")
	public List<Object[]> getExpenseSummaryByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();
		return expenseService.getExpenseSummaryByDateRange(start, end);
	}

	@PostMapping("/sendReport")
	public ResponseEntity<String> sendExpenseReport(@RequestParam Long userId, @RequestParam String startDate,
			@RequestParam String endDate) throws MessagingException {
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

		expenseService.sendExpenseReportToUser(userId, start, end);
		return ResponseEntity.ok("Expense report sent successfully.");
	}

}
