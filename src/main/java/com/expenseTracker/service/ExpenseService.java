package com.expenseTracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expenseTracker.model.Expense;
import com.expenseTracker.model.ExpenseCategories;
import com.expenseTracker.repository.ExpenseCategoriesRepository;
import com.expenseTracker.repository.ExpenseRepository;

import jakarta.mail.MessagingException;

@Service
public class ExpenseService {

	@Autowired
	private ExpenseRepository expenseRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserService userService;

	@Autowired
	private ExpenseCategoriesRepository expenseCategoriesRepository;

	public Expense createExpense(Long userId, Long categoryId, BigDecimal amount, LocalDate expenseDate,
			String description) {
		Expense expense = Expense.builder().userId(userId).categoryId(categoryId).amount(amount)
				.expenseDate(expenseDate).description(description).createdAt(LocalDateTime.now()).build();
		return expenseRepository.save(expense);
	}

	public List<Expense> getExpenseByuserId(Long userId) {
		System.out.println("Fetching expenses for userId: " + userId);
		return expenseRepository.findExpensesByUserId(userId);
	}

	public List<Expense> getExpenseByuserIdAndCategoryId(Long userId, Long categoryId) {
		System.out.println("Fetching expenses for userId: " + userId);

		return expenseRepository.findExpensesByUserIdAndCategoryId(userId, categoryId);
	}

	public List<Expense> getExpensesByuserIdAndexpenseDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
		return expenseRepository.findExpensesByuserIdAndexpenseDateBetween(userId, startDate, endDate);
	}

	public Map<String, BigDecimal> getExpenseSummaryByCategory(LocalDate startDate, LocalDate endDate) {
		List<Object[]> results = expenseRepository.findExpenseSummaryByCategory(startDate, endDate);
		Map<String, BigDecimal> summary = new HashMap<>();
		for (Object[] result : results) {
			String category = (String) result[0];
			BigDecimal totalAmount = (BigDecimal) result[1];
			summary.put(category, totalAmount);
		}
		return summary;
	}

	public List<Object[]> getExpenseSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
		return expenseRepository.findExpenseSummaryByDateRange(startDate, endDate);
	}

	public void sendExpenseReportToUser(Long userId, LocalDate startDate, LocalDate endDate) throws MessagingException {

		long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

		LocalDate startDate1 = startDate.minusDays(daysBetween + 1); // start date of previous period
		LocalDate endDate1 = endDate.minusDays(daysBetween + 1);

		List<Expense> currentExpenses = expenseRepository.findExpensesByuserIdAndexpenseDateBetween(userId, startDate,
				endDate);
		List<Expense> previousExpenses = expenseRepository.findExpensesByuserIdAndexpenseDateBetween(userId, startDate1,
				endDate1);
		String emailContent = generateEmailContent(currentExpenses, previousExpenses, userId, startDate, endDate,
				startDate1, endDate1);
		String userEmail = userService.getEmailByUserId(userId);
		emailService.sendExpenseReport(userEmail, "Your Monthly Expense and Comparison Report", emailContent);
	}

	private String generateEmailContent(List<Expense> currentExpenses, List<Expense> previousExpenses, Long userId,
			LocalDate startDate, LocalDate endDate, LocalDate startDate1, LocalDate endDate1) {

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");
		Locale pakLocale = new Locale("en", "PK");
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(pakLocale);

		currentExpenses.sort(Comparator.comparing(Expense::getExpenseDate));
		previousExpenses.sort(Comparator.comparing(Expense::getExpenseDate));

		StringBuilder content = new StringBuilder();
		content.append("<h2>Expense Report from ").append(startDate.format(dateFormatter)).append(" to ")
				.append(endDate.format(dateFormatter)).append(":</h2>");
		content.append("<table border='1' style='border-collapse: collapse; width: 60%;'>");
		content.append("<tr>").append("<th>Date</th>").append("<th>CategoryName</th>").append("<th>Description</th>")
				.append("<th>Amount</th>").append("</tr>");

		Map<String, StringBuilder> aggregatedExpenses = new LinkedHashMap<>();
		Map<String, BigDecimal> totalAmounts = new LinkedHashMap<>();

		Map<Long, String> categoryMap = expenseCategoriesRepository.findAll().stream()
				.collect(Collectors.toMap(ExpenseCategories::getId, ExpenseCategories::getName));

		for (Expense expense : currentExpenses) {
			String categoryName = categoryMap.get(expense.getCategoryId());
			String key = categoryName + " | " + expense.getExpenseDate().format(dateFormatter);

			aggregatedExpenses.putIfAbsent(key, new StringBuilder());
			aggregatedExpenses.get(key).append(expense.getDescription()).append(", ");

			totalAmounts.putIfAbsent(key, BigDecimal.ZERO);
			totalAmounts.put(key, totalAmounts.get(key).add(expense.getAmount()));
		}

		for (Map.Entry<String, StringBuilder> entry : aggregatedExpenses.entrySet()) {
			String[] parts = entry.getKey().split(" \\| ");
			String categoryName = parts[0];
			String date = parts[1];

			content.append("<tr>").append("<td>").append(date).append("</td>").append("<td>").append(categoryName)
					.append("</td>").append("<td>").append(entry.getValue().toString().replaceAll(", $", ""))
					.append("</td>").append("<td style='text-align: right;'>")
					.append(currencyFormat.format(totalAmounts.get(entry.getKey()).setScale(0, RoundingMode.DOWN)))
					.append("</td>").append("</tr>");
		}

		BigDecimal grandTotal = totalAmounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		content.append("<tr>")
				.append("<td colspan='3' style='text-align: right;'><strong>Total Expenses:</strong></td>")
				.append("<td style='text-align: right;'>")
				.append(currencyFormat.format(grandTotal.setScale(0, RoundingMode.DOWN))).append("</td>")
				.append("</tr>");
		content.append("</table>");

		// Comparison Section
		content.append("<h2>Comparison with Previous Month:</h2>");

		Map<String, BigDecimal> previousMonthExpenses = calculateCategoryTotals(
				expenseRepository.findExpensesByuserIdAndexpenseDateBetween(userId, startDate1, endDate1), categoryMap);
		Map<String, BigDecimal> currentMonthExpenses = calculateCategoryTotals(currentExpenses, categoryMap);

		content.append("<table border='1' style='border-collapse: collapse; width: 60%;'>");
		content.append("<tr><th>Category</th><th>Previous Month</th><th>Current Month</th><th>Difference</th></tr>");

		Set<String> allCategories = new HashSet<>();
		allCategories.addAll(previousMonthExpenses.keySet());
		allCategories.addAll(currentMonthExpenses.keySet());

		for (String category : allCategories) {
			BigDecimal previousTotal = previousMonthExpenses.getOrDefault(category, BigDecimal.ZERO);
			BigDecimal currentTotal = currentMonthExpenses.getOrDefault(category, BigDecimal.ZERO);
			BigDecimal difference = currentTotal.subtract(previousTotal);

			String differenceStyle = difference.compareTo(BigDecimal.ZERO) > 0 ? "color:red;" : "color:green;";

			content.append("<tr><td>").append(category).append("</td>").append("<td style='text-align: right;'>")
					.append(currencyFormat.format(previousTotal)).append("</td>")
					.append("<td style='text-align: right;'>").append(currencyFormat.format(currentTotal))
					.append("</td>").append("<td style='text-align: right; ").append(differenceStyle).append("'>")
					.append(currencyFormat.format(difference)).append("</td></tr>");
		}

		content.append("</table>");
		return content.toString();
	}

	private Map<String, BigDecimal> calculateCategoryTotals(List<Expense> expenses, Map<Long, String> categoryMap) {
		Map<String, BigDecimal> categoryTotals = new HashMap<>();

		for (Expense expense : expenses) {
			String categoryName = categoryMap.get(expense.getCategoryId());
			categoryTotals.putIfAbsent(categoryName, BigDecimal.ZERO);
			categoryTotals.put(categoryName, categoryTotals.get(categoryName).add(expense.getAmount()));
		}

		return categoryTotals;
	}

}
