package com.expenseTracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expenseTracker.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	@Query("SELECT e FROM Expense e Where e.userId = :userId")
	List<Expense> findExpensesByUserId(@Param("userId") Long userId);

	@Query("SELECT e FROM Expense e Where e.userId = :userId AND e.categoryId = :categoryId")
	List<Expense> findExpensesByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

	@Query("SELECT e FROM Expense e Where e.userId = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
	List<Expense> findExpensesByuserIdAndexpenseDateBetween(@Param("userId") Long userId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT c.name, SUM(e.amount) FROM Expense e JOIN ExpenseCategories c ON e.categoryId = c.id Where e.expenseDate BETWEEN :startDate AND :endDate GROUP BY c.name")
	List<Object[]> findExpenseSummaryByCategory(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query("SELECT e.userId, u.firstName, e.categoryId, c.name, SUM(e.amount) FROM Expense e JOIN User u ON e.userId=u.id "
			+ "JOIN ExpenseCategories c ON e.categoryId = c.id Where e.expenseDate BETWEEN :startDate AND :endDate GROUP BY e.userId, u.firstName, e.categoryId, c.name")
	List<Object[]> findExpenseSummaryByDateRange(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);
}
