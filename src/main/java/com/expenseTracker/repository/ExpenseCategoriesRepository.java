package com.expenseTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expenseTracker.model.ExpenseCategories;

public interface ExpenseCategoriesRepository extends JpaRepository<ExpenseCategories, Long> {

}
